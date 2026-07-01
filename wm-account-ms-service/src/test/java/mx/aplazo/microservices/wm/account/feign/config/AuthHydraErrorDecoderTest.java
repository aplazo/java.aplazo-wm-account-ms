package mx.aplazo.microservices.wm.account.feign.config;

import feign.Request;
import feign.Response;
import mx.aplazo.exception.AplazoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthHydraErrorDecoderTest {

    private static final String METHOD_KEY = "AuthHydraClient#revoke(HydraRevokeRequest)";

    private final AuthHydraErrorDecoder decoder = new AuthHydraErrorDecoder();

    private static Response responseWith(int status, String body) {
        final Request request = Request.create(
                Request.HttpMethod.POST,
                "http://auth-hydra/hydra/revoke",
                Collections.emptyMap(),
                Request.Body.empty(),
                null);

        final Response.Builder builder = Response.builder()
                .status(status)
                .reason("error")
                .request(request)
                .headers(Map.<String, Collection<String>>of("Content-Type", List.of("application/json")));

        if (body != null) {
            builder.body(body, StandardCharsets.UTF_8);
        }
        return builder.build();
    }

    @ParameterizedTest(name = "status {0} → 500 HYDRA_REVOCATION_FAILED")
    @ValueSource(ints = {400, 401, 404, 422, 500, 503})
    @DisplayName("any non-2xx is mapped to a 500 HYDRA_REVOCATION_FAILED AplazoException")
    void anyNon2xx_mapsToHydraRevocationFailed(int status) {
        final Exception result = decoder.decode(METHOD_KEY, responseWith(status, "{\"error\":\"down\"}"));

        final AplazoException ex = assertInstanceOf(AplazoException.class, result);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.getHttpStatus());
        assertTrue(ex.getMessage().contains(AuthHydraErrorDecoder.HYDRA_REVOCATION_FAILED),
                "message must carry the HYDRA_REVOCATION_FAILED code");
        assertTrue(ex.getMessage().contains(String.valueOf(status)),
                "message must include the upstream status");
    }

    @Test
    @DisplayName("a response with no body still maps to 500 HYDRA_REVOCATION_FAILED")
    void nullBody_mapsToHydraRevocationFailed() {
        final Exception result = decoder.decode(METHOD_KEY, responseWith(503, null));

        final AplazoException ex = assertInstanceOf(AplazoException.class, result);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.getHttpStatus());
        assertTrue(ex.getMessage().contains(AuthHydraErrorDecoder.HYDRA_REVOCATION_FAILED));
    }
}
