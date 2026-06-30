package mx.aplazo.microservices.wm.account.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mx.aplazo.microservices.wm.account.model.request.UnlinkAccountRequest;
import mx.aplazo.microservices.wm.account.model.response.UnlinkAccountResponse;
import mx.aplazo.microservices.wm.account.service.UnlinkService;
import mx.aplazo.microservices.wm.account.service.WmAccountUnlinkClient;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Internal HTTP handler for the shared Unlink operation — contract C-02
 * {@code POST /wm/account/unlink} (BNPL-955 §3.2, ADR-012 D-12/D-13). Serves Trigger B
 * (partners-ms proxy) and Trigger C (Nexus consumer), which carry the {@code xClientWm} in the
 * request body. Implements the client-module contract, mirroring the
 * {@code WalmartController}/{@code WalmartServiceClient} pattern.
 *
 * @author Aplazo
 */
@Slf4j
@Tag(name = "WMAccountMsUnlink")
@RestController
@RequiredArgsConstructor
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class WmAccountUnlinkController implements WmAccountUnlinkClient {

    private final UnlinkService unlinkService;

    @Override
    @PreAuthorize("hasRole('ROLE_API')")
    public UnlinkAccountResponse unlink(UnlinkAccountRequest request) {
        log.info("Unlink request (C-02) xClientWm={} source={} initiatedBy={}",
                request.getXClientWm(), request.getSource(), request.getInitiatedBy());
        return unlinkService.unlink(request);
    }
}
