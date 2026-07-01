package mx.aplazo.microservices.wm.account.feign;

import mx.aplazo.microservices.wm.account.feign.model.request.UnlinkNotifyRequest;
import mx.aplazo.microservices.wm.account.feign.model.response.UnlinkNotifyResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Outbound client for the post-Unlink notification to partners-ms — contract C-NEW-02
 * {@code POST /wm/unlink/notify} (BNPL-955 §3.7, ADR-012 D-12). This is step ④ of the Unlink
 * chain: partners-ms sends the user confirmation email, publishes the {@code wm_account_unlinked}
 * analytics event, and (when {@code source=APLAZO}) calls Cashi outbound (C-04).
 *
 * <p><b>Contract definition only (BNPL-960).</b> The orchestration that invokes this client at
 * step ④ — including the C-NEW-02 retry policy (3 attempts: 0s, 30s, 5min) and the no-rollback
 * semantics on permanent failure (ADR-012 D-10) — is delivered by BNPL-886. Authentication is the
 * standard internal {@code ROLE_API} JWT applied by the Aplazo Feign interceptor.
 *
 * @author Aplazo
 */
@FeignClient(
        name = "PartnersUnlinkNotify",
        url = "${aplazo.url.api.partners-url:http://localhost}"
)
public interface PartnersUnlinkNotifyClient {

    @PostMapping(
            value = "/wm/unlink/notify",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    UnlinkNotifyResponse notify(@RequestBody UnlinkNotifyRequest request);
}
