package mx.aplazo.microservices.wm.account.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import mx.aplazo.microservices.wm.account.model.request.UnlinkAccountRequest;
import mx.aplazo.microservices.wm.account.model.response.UnlinkAccountResponse;
import mx.aplazo.microservices.wm.account.service.WmAccountUnlinkClient;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * HTTP handler for the shared Unlink operation — contract C-02 {@code POST /wm/account/unlink}
 * (BNPL-955 §3.2). Implements the client-module contract, mirroring the
 * {@code WalmartController}/{@code WalmartServiceClient} pattern.
 *
 * <p><b>Skeleton only (BNPL-960).</b> The endpoint is declared and wired to the contract, but the
 * canonical business logic — and the injection of {@link mx.aplazo.microservices.wm.account.service.UnlinkService}
 * — is delivered by BNPL-886. Until then the operation throws {@link UnsupportedOperationException}.
 *
 * @author Aplazo
 */
@Slf4j
@Tag(name = "WMAccountMsUnlink")
@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class WmAccountUnlinkController implements WmAccountUnlinkClient {

    private static final String NOT_IMPLEMENTED = "Unlink core business logic pending — BNPL-886";

    @Override
    @PreAuthorize("hasRole('ROLE_API')")
    public UnlinkAccountResponse unlink(UnlinkAccountRequest request) {
        // Skeleton (BNPL-960): contract wired, canonical flow implemented under BNPL-886.
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }
}
