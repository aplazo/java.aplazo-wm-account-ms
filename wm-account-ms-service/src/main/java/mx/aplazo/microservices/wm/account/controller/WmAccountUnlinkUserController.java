package mx.aplazo.microservices.wm.account.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mx.aplazo.exception.AplazoException;
import mx.aplazo.microservices.wm.account.model.enums.UnlinkErrorCode;
import mx.aplazo.microservices.wm.account.model.response.UnlinkAccountResponse;
import mx.aplazo.microservices.wm.account.service.UnlinkService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * User-initiated Unlink (Trigger A) — contract C-03 {@code POST /api/v1/wm/account/unlink}
 * {@code [ROLE_CUSTOMER]} (BNPL-955 §3.3).
 *
 * <p>The App carries only the customer identity in its token, not the Walmart client id, so this
 * endpoint resolves the customer by the {@code customerId} taken from the authenticated token and
 * lets {@link UnlinkService#unlinkForCustomer(Long)} look up {@code customer_wm_extended} for the
 * data the App cannot provide (xClientWm, OAuth fields). This refines BNPL-955 §3.3, which assumed
 * the App sends {@code X-Client-Wm}.
 *
 * @author Aplazo
 */
@Slf4j
@Tag(name = "WMAccountMsUnlink")
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/v1/wm/account", produces = MediaType.APPLICATION_JSON_VALUE)
public class WmAccountUnlinkUserController {

    private final UnlinkService unlinkService;

    @PostMapping("/unlink")
    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    public UnlinkAccountResponse unlink() {
        final Long customerId = currentCustomerId();
        log.info("Unlink request (C-03, Trigger A) customerId={}", customerId);
        return unlinkService.unlinkForCustomer(customerId);
    }

    /** Resolve the Aplazo customer id from the authenticated ROLE_CUSTOMER token subject. */
    private Long currentCustomerId() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new AplazoException(HttpStatus.UNAUTHORIZED,
                    "Missing authenticated customer", UnlinkErrorCode.INVALID_REQUEST);
        }
        try {
            return Long.valueOf(authentication.getName());
        } catch (NumberFormatException e) {
            throw new AplazoException(HttpStatus.BAD_REQUEST,
                    "Invalid customer identifier in token", UnlinkErrorCode.INVALID_REQUEST);
        }
    }
}
