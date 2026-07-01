package mx.aplazo.microservices.wm.account.service;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import mx.aplazo.microservices.wm.account.model.entity.CustomerWmExtended;
import mx.aplazo.microservices.wm.account.model.entity.CustomerWmStatus;
import mx.aplazo.microservices.wm.account.model.entity.CustomerWmUnlinkRecord;
import mx.aplazo.microservices.wm.account.repository.CustomerWmExtendedRepository;
import mx.aplazo.microservices.wm.account.repository.CustomerWmUnlinkRecordRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Step ③ of the canonical Unlink flow: the single atomic PostgreSQL transaction that records the
 * Unlink event and flips the customer to {@code UNLINKED} (ADR-012 D-05).
 *
 * <p>Isolated in its own bean so the {@code @Transactional} boundary is applied through the Spring
 * proxy and wraps <b>only</b> the DB write — token revocation (step ②) runs before this call and
 * the partners-ms notification (step ④) runs after it, neither inside the transaction.
 *
 * @author Aplazo
 */
@Component
@RequiredArgsConstructor
public class UnlinkAtomicWriter {

    private final CustomerWmUnlinkRecordRepository unlinkRecordRepository;
    private final CustomerWmExtendedRepository customerWmExtendedRepository;

    /**
     * Atomically insert the audit record and update the customer row (status + OAuth cleanup).
     * Both statements commit or roll back together.
     *
     * @param customer    the customer row to flip to UNLINKED
     * @param reason      unlink reason (stored on the record)
     * @param initiatedBy acting party (stored on the record)
     * @param detail      optional caller-provided detail
     */
    @Transactional
    public void persistUnlink(CustomerWmExtended customer, String reason, String initiatedBy, String detail) {
        final LocalDateTime now = LocalDateTime.now();

        unlinkRecordRepository.save(CustomerWmUnlinkRecord.builder()
                .customerId(customer.getCustomerId())
                .unlinkedAt(now)
                .unlinkReason(reason)
                .unlinkInitiatedBy(initiatedBy)
                .unlinkDetail(detail)
                .build());

        customer.setStatus(CustomerWmStatus.UNLINKED);
        customer.setCode(null);
        customer.setOauthState(null);
        customer.setOauthCodeVerifier(null);
        customer.setOauthCallbackUrl(null);
        customer.setLinkingAccountUrl(null);
        customer.setRefreshTokenExpiresAt(now);
        customerWmExtendedRepository.save(customer);
    }
}
