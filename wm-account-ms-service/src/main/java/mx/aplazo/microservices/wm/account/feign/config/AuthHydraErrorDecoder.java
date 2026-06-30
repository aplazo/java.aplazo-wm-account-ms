package mx.aplazo.microservices.wm.account.feign.config;

import feign.Response;
import feign.Util;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import mx.aplazo.exception.AplazoException;
import org.springframework.http.HttpStatus;

import java.io.Reader;
import java.nio.charset.StandardCharsets;

/**
 * Feign {@link ErrorDecoder} for the auth-hydra revocation client (C-NEW-01).
 *
 * <p>Token revocation is the PRIMARY security barrier of the Unlink flow (ADR-012 D-02/D-03):
 * any non-2xx response from auth-hydra is a hard pre-condition failure. There is no retry and no
 * fallback — the call must abort the Unlink before any DB write (BNPL-955 §3.6, §5.4 D955-09).
 *
 * <p>Therefore every non-2xx is mapped to a single {@code 500 HYDRA_REVOCATION_FAILED}
 * {@link AplazoException}, regardless of the upstream status, so the caller aborts with no
 * state change and no downstream notification.
 *
 * @author Aplazo
 */
@Slf4j
public class AuthHydraErrorDecoder implements ErrorDecoder {

    /** Error code surfaced to the Unlink caller when revocation fails (BNPL-955 §3.2). */
    public static final String HYDRA_REVOCATION_FAILED = "HYDRA_REVOCATION_FAILED";

    @Override
    public Exception decode(String methodKey, Response response) {
        final String body = safeBodyToString(response);
        log.error("[AuthHydraErrorDecoder] auth-hydra revocation failed: methodKey={}, status={}, body={}",
                methodKey, response.status(), body);

        final String message = String.format(
                "%s: auth-hydra responded %d for %s", HYDRA_REVOCATION_FAILED, response.status(), methodKey);

        return new AplazoException(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }

    private String safeBodyToString(Response response) {
        try {
            if (response == null || response.body() == null) {
                return null;
            }
            try (Reader reader = response.body().asReader(StandardCharsets.UTF_8)) {
                return Util.toString(reader);
            }
        } catch (Exception e) {
            log.warn("[AuthHydraErrorDecoder] could not read error body: {}", e.toString());
            return null;
        }
    }
}
