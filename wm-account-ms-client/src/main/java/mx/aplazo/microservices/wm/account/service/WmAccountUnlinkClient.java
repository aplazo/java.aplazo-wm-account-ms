package mx.aplazo.microservices.wm.account.service;

import mx.aplazo.microservices.wm.account.model.request.UnlinkAccountRequest;
import mx.aplazo.microservices.wm.account.model.response.UnlinkAccountResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * API contract for the shared Unlink operation owned by {@code wm-account-ms} — contract C-02
 * (BNPL-955 §3.2). Published in the client module so callers (e.g. {@code partners-ms} as the
 * Trigger B proxy) can consume it as a dependency, and implemented in the service module by the
 * controller — mirroring the {@code WalmartServiceClient}/{@code WalmartController} pattern.
 *
 * <p>Endpoint {@code POST /wm/account/unlink} — the {@code /internal/} prefix was dropped by
 * architect decision (ADR-012 D-12); auth is {@code ROLE_API} (ADR-012 D-13). The canonical
 * behavior (state guard → sync auth-hydra revoke → atomic DB write → sync partners-ms notify)
 * is implemented under BNPL-886; this interface only declares the contract.
 *
 * @author Aplazo
 */
@FeignClient(name = "WMAccountMsUnlink", url = "${aplazo.url.api.wm-account.api:http://localhost}")
public interface WmAccountUnlinkClient {

    @PostMapping(
            value = "/wm/account/unlink",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    UnlinkAccountResponse unlink(@RequestBody UnlinkAccountRequest request);
}
