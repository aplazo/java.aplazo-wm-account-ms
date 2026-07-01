package mx.aplazo.microservices.wm.account.feign;

import feign.Request;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import mx.aplazo.microservices.wm.account.feign.config.AuthHydraErrorDecoder;
import mx.aplazo.microservices.wm.account.feign.dto.HydraRevokeRequest;
import mx.aplazo.microservices.wm.account.feign.dto.HydraRevokeResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.concurrent.TimeUnit;

/**
 * Outbound client for auth-hydra OAuth token revocation — contract C-NEW-01.
 *
 * <p>{@code POST /hydra/revoke} is step ② of the Unlink chain and the PRIMARY security barrier
 * (ADR-012 D-02/D-03): {@code wm-account-ms} calls it <b>synchronously</b> and must receive a
 * {@code 200} before performing any DB write. Authentication is service-to-service (IAM roles +
 * CloudMap, BNPL-955 §3.6) — no app-level credentials are attached by this client.
 *
 * <p>Resilience (BNPL-955 §5.4 D955-09): this is a hard pre-condition — <b>no retry</b> and no
 * fallback. Any non-2xx or timeout aborts the Unlink with no state change (see
 * {@link AuthHydraErrorDecoder} and {@link AuthHydraClientConfig}).
 *
 * @author Aplazo
 */
@FeignClient(
        name = "AuthHydraClient",
        url = "${aplazo.url.api.auth-hydra-url:http://localhost}",
        configuration = AuthHydraClient.AuthHydraClientConfig.class
)
public interface AuthHydraClient {

    @PostMapping(
            value = "/hydra/revoke",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    HydraRevokeResponse revoke(@RequestBody HydraRevokeRequest request);

    /**
     * Per-client Feign configuration: configurable blocking timeout, no retry, and the
     * fail-fast error decoder.
     */
    @Configuration(proxyBeanMethods = false)
    class AuthHydraClientConfig {

        @Bean
        ErrorDecoder authHydraErrorDecoder() {
            return new AuthHydraErrorDecoder();
        }

        /**
         * Hard pre-condition — a single attempt. Any failure propagates immediately so the
         * Unlink aborts (ADR-012: failure must always propagate, no retry).
         */
        @Bean
        Retryer authHydraRetryer() {
            return Retryer.NEVER_RETRY;
        }

        /**
         * Synchronous, blocking call with a configurable timeout
         * ({@code aplazo.url.api.auth-hydra.connect-timeout-ms} / {@code read-timeout-ms},
         * value to be validated with the squad — BNPL-977 NFR). {@code followRedirects=true}.
         */
        @Bean
        Request.Options authHydraRequestOptions(
                @Value("${aplazo.url.api.auth-hydra.connect-timeout-ms:2000}") long connectTimeoutMs,
                @Value("${aplazo.url.api.auth-hydra.read-timeout-ms:5000}") long readTimeoutMs) {
            return new Request.Options(
                    connectTimeoutMs, TimeUnit.MILLISECONDS,
                    readTimeoutMs, TimeUnit.MILLISECONDS,
                    true);
        }
    }
}
