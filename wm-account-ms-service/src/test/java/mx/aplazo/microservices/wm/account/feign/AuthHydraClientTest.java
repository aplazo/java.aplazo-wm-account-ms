package mx.aplazo.microservices.wm.account.feign;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Client;
import feign.Feign;
import feign.Request;
import feign.Response;
import feign.RetryableException;
import feign.Retryer;
import feign.codec.Decoder;
import feign.codec.EncodeException;
import feign.codec.Encoder;
import mx.aplazo.exception.AplazoException;
import mx.aplazo.microservices.wm.account.feign.config.AuthHydraErrorDecoder;
import mx.aplazo.microservices.wm.account.feign.dto.HydraRevokeRequest;
import mx.aplazo.microservices.wm.account.feign.dto.HydraRevokeResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.openfeign.support.SpringMvcContract;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Exercises {@link AuthHydraClient} behavior with a stub {@link Client} (no live HTTP server),
 * plus the per-client {@link AuthHydraClient.AuthHydraClientConfig}. Covers AC-1 (successful
 * revocation) and AC-2 (non-2xx and timeout → fail-fast, no retry).
 */
class AuthHydraClientTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final Encoder encoder = (object, bodyType, template) -> {
        try {
            template.body(MAPPER.writeValueAsBytes(object), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new EncodeException(e.getMessage(), e);
        }
    };

    private final Decoder decoder = (response, type) -> {
        if (response.body() == null) {
            return null;
        }
        try (InputStream is = response.body().asInputStream()) {
            return MAPPER.readValue(is, MAPPER.constructType(type));
        }
    };

    private AuthHydraClient clientBackedBy(Client http) {
        return Feign.builder()
                .contract(new SpringMvcContract())
                .encoder(encoder)
                .decoder(decoder)
                .errorDecoder(new AuthHydraErrorDecoder())
                .retryer(Retryer.NEVER_RETRY)
                .client(http)
                .target(AuthHydraClient.class, "http://auth-hydra");
    }

    private static Response json(int status, String body, Request request) {
        return Response.builder()
                .status(status)
                .reason("test")
                .request(request)
                .headers(Map.<String, Collection<String>>of("Content-Type", List.of("application/json")))
                .body(body, StandardCharsets.UTF_8)
                .build();
    }

    private static HydraRevokeRequest sampleRequest() {
        return HydraRevokeRequest.builder()
                .subject("customer-123")
                .clientId("walmart-cashi")
                .externalReferenceId("trace-1")
                .build();
    }

    @Test
    @DisplayName("AC-1: 200 OK is decoded into HydraRevokeResponse and the flow continues")
    void revoke_success_returnsSessionsRevoked() throws Exception {
        final AtomicInteger calls = new AtomicInteger();
        final String body = MAPPER.writeValueAsString(
                HydraRevokeResponse.builder().sessionsRevoked(3).build());

        final AuthHydraClient client = clientBackedBy((request, options) -> {
            calls.incrementAndGet();
            return json(200, body, request);
        });

        final HydraRevokeResponse response = client.revoke(sampleRequest());

        assertEquals(3, response.getSessionsRevoked());
        assertEquals(1, calls.get(), "a single synchronous attempt is expected");
    }

    @Test
    @DisplayName("AC-2: a non-2xx aborts with AplazoException 500 and is not retried")
    void revoke_serverError_throwsAndDoesNotRetry() {
        final AtomicInteger calls = new AtomicInteger();

        final AuthHydraClient client = clientBackedBy((request, options) -> {
            calls.incrementAndGet();
            return json(503, "{\"error\":\"hydra down\"}", request);
        });

        final AplazoException ex = assertThrows(AplazoException.class, () -> client.revoke(sampleRequest()));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.getHttpStatus());
        assertEquals(1, calls.get(), "Retryer.NEVER_RETRY must not retry a failed revocation");
    }

    @Test
    @DisplayName("AC-2: a timeout propagates immediately and is not retried")
    void revoke_timeout_propagatesWithoutRetry() {
        final AtomicInteger calls = new AtomicInteger();

        final AuthHydraClient client = clientBackedBy((request, options) -> {
            calls.incrementAndGet();
            throw new IOException("connection timed out");
        });

        assertThrows(RetryableException.class, () -> client.revoke(sampleRequest()));
        assertEquals(1, calls.get(), "Retryer.NEVER_RETRY must not retry on timeout");
    }

    @Test
    @DisplayName("config: fail-fast error decoder, no retry, and configurable timeouts")
    void config_exposesFailFastBeans() {
        final AuthHydraClient.AuthHydraClientConfig config = new AuthHydraClient.AuthHydraClientConfig();

        assertInstanceOf(AuthHydraErrorDecoder.class, config.authHydraErrorDecoder());
        assertSame(Retryer.NEVER_RETRY, config.authHydraRetryer());

        final Request.Options options = config.authHydraRequestOptions(1500L, 4000L);
        assertEquals(1500, options.connectTimeoutMillis());
        assertEquals(4000, options.readTimeoutMillis());
    }
}
