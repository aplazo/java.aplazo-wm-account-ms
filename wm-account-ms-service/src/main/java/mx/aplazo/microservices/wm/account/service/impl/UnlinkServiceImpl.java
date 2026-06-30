package mx.aplazo.microservices.wm.account.service.impl;

import java.util.EnumSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mx.aplazo.exception.AplazoException;
import mx.aplazo.microservices.wm.account.feign.AuthHydraClient;
import mx.aplazo.microservices.wm.account.feign.PartnersUnlinkNotifyClient;
import mx.aplazo.microservices.wm.account.feign.dto.HydraRevokeRequest;
import mx.aplazo.microservices.wm.account.feign.model.request.UnlinkNotifyRequest;
import mx.aplazo.microservices.wm.account.model.entity.CustomerWmExtended;
import mx.aplazo.microservices.wm.account.model.entity.CustomerWmStatus;
import mx.aplazo.microservices.wm.account.model.enums.UnlinkErrorCode;
import mx.aplazo.microservices.wm.account.model.enums.UnlinkInitiatedBy;
import mx.aplazo.microservices.wm.account.model.enums.UnlinkReason;
import mx.aplazo.microservices.wm.account.model.enums.UnlinkSource;
import mx.aplazo.microservices.wm.account.model.request.UnlinkAccountRequest;
import mx.aplazo.microservices.wm.account.model.response.UnlinkAccountResponse;
import mx.aplazo.microservices.wm.account.repository.CustomerWmExtendedRepository;
import mx.aplazo.microservices.wm.account.service.UnlinkAtomicWriter;
import mx.aplazo.microservices.wm.account.service.UnlinkService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * Canonical Unlink flow (ADR-012 D-03, BNPL-955 §5.2). Both entry points resolve the customer and
 * converge on {@link #execute}: ① state guard → ② synchronous auth-hydra revocation (before any
 * DB write) → ③ atomic DB write → ④ synchronous partners-ms notification → ⑤ 200.
 *
 * @author Aplazo
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UnlinkServiceImpl implements UnlinkService {

    private static final String CODE_SUCCESS = "SUCCESS";
    private static final Set<CustomerWmStatus> UNLINKABLE =
            EnumSet.of(CustomerWmStatus.LINKED, CustomerWmStatus.LINKED_TOKENIZED, CustomerWmStatus.REAUTH_PENDING);

    private final CustomerWmExtendedRepository customerRepository;
    private final AuthHydraClient authHydraClient;
    private final UnlinkAtomicWriter atomicWriter;
    private final PartnersUnlinkNotifyClient partnersUnlinkNotifyClient;

    /** OAuth2 client the revocation is scoped to (the single Walmart/Cashi client). */
    @Value("${api.aplazo.oauth.client-id}")
    private String oauthClientId;

    @Override
    public UnlinkAccountResponse unlink(UnlinkAccountRequest request) {
        final CustomerWmExtended customer = customerRepository.findByXClientWm(request.getXClientWm())
                .orElseThrow(() -> notFound("x-client-wm: " + request.getXClientWm()));
        return execute(customer,
                value(request.getReason()),
                value(request.getSource()),
                value(request.getInitiatedBy()),
                request.getDetail());
    }

    @Override
    public UnlinkAccountResponse unlinkForCustomer(Long customerId) {
        final CustomerWmExtended customer = customerRepository.findByCustomerId(customerId)
                .orElseThrow(() -> notFound("customerId: " + customerId));
        // Trigger A — context inferred for a user-initiated Unlink from the App.
        return execute(customer,
                UnlinkReason.USER_REQUEST.getValue(),
                UnlinkSource.APLAZO.getValue(),
                UnlinkInitiatedBy.USER.getValue(),
                null);
    }

    private UnlinkAccountResponse execute(CustomerWmExtended customer, String reason, String source,
                                          String initiatedBy, String detail) {
        final CustomerWmStatus status = customer.getStatus();

        // ① State guard.
        if (CustomerWmStatus.UNLINKED.equals(status)) {
            // Idempotent: no revocation, no write, no notify (ADR-012, BNPL-955 §3.x).
            return UnlinkAccountResponse.builder()
                    .code(UnlinkErrorCode.CUSTOMER_ALREADY_UNLINKED.name())
                    .message("x-client-wm: " + customer.getXClientWm() + " was already unlinked")
                    .build();
        }
        if (status == null || !UNLINKABLE.contains(status)) {
            throw new AplazoException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Customer is not eligible for unlink in its current state",
                    UnlinkErrorCode.CUSTOMER_NOT_ELIGIBLE_FOR_UNLINK);
        }

        // ② Token revocation — PRIMARY security barrier, before any DB write (ADR-012 D-03).
        revokeTokens(customer);

        // ③ Atomic DB write (INSERT record + UPDATE customer) in a single transaction.
        atomicWriter.persistUnlink(customer, reason, initiatedBy, detail);

        // ④ Synchronous notification to partners-ms (no rollback on failure — ADR-012 D-10).
        notifyPartners(customer, source, reason);

        // ⑤ Done.
        return UnlinkAccountResponse.builder()
                .code(CODE_SUCCESS)
                .message("x-client-wm: " + customer.getXClientWm() + " unlinked")
                .build();
    }

    private void revokeTokens(CustomerWmExtended customer) {
        try {
            authHydraClient.revoke(HydraRevokeRequest.builder()
                    .subject(String.valueOf(customer.getCustomerId()))
                    .clientId(oauthClientId)
                    .externalReferenceId(customer.getXClientWm())
                    .build());
        } catch (AplazoException e) {
            // Already mapped to 500 HYDRA_REVOCATION_FAILED by AuthHydraErrorDecoder.
            throw e;
        } catch (Exception e) {
            // Any other failure (e.g. timeout) must also abort the Unlink with no state change.
            throw new AplazoException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "auth-hydra token revocation failed", UnlinkErrorCode.HYDRA_REVOCATION_FAILED);
        }
    }

    private void notifyPartners(CustomerWmExtended customer, String source, String reason) {
        try {
            partnersUnlinkNotifyClient.notify(UnlinkNotifyRequest.builder()
                    .customerId(String.valueOf(customer.getCustomerId()))
                    .xClientWm(customer.getXClientWm())
                    .source(source)
                    .reason(reason)
                    .build());
        } catch (Exception e) {
            // ADR-012 D-10: tokens are already revoked and the DB is already written. Do NOT roll
            // back. Preserve UNLINKED, raise the operational alert, and still return success.
            log.error("wm_partners_unlink_notification_failed customerId={} xClientWm={}: {}",
                    customer.getCustomerId(), customer.getXClientWm(), e.getMessage(), e);
        }
    }

    private AplazoException notFound(String who) {
        return new AplazoException(HttpStatus.NOT_FOUND,
                "Customer not found for " + who, UnlinkErrorCode.CUSTOMER_NOT_FOUND);
    }

    private static String value(UnlinkReason reason) {
        return reason == null ? null : reason.getValue();
    }

    private static String value(UnlinkSource source) {
        return source == null ? null : source.getValue();
    }

    private static String value(UnlinkInitiatedBy initiatedBy) {
        return initiatedBy == null ? null : initiatedBy.getValue();
    }
}
