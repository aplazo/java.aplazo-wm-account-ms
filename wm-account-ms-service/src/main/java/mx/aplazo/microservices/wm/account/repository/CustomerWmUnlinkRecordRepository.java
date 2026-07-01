package mx.aplazo.microservices.wm.account.repository;

import java.util.Optional;
import mx.aplazo.microservices.wm.account.model.entity.CustomerWmUnlinkRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Data access for {@code wm_integration.customer_wm_unlink_record} (ADR-012 D-04).
 *
 * <p>{@code wm-account-ms} is the sole writer of this table. The Unlink flow (BNPL-886) appends
 * one record per event; the Relink record update (C-NEW-03, BNPL-955 §3.8) targets the most
 * recent record with {@code relinked_at IS NULL}.
 *
 * @author Aplazo
 */
@Repository
public interface CustomerWmUnlinkRecordRepository extends JpaRepository<CustomerWmUnlinkRecord, Long> {

    /**
     * Most recent not-yet-relinked Unlink record for a customer — the target of the C-NEW-03
     * {@code relinked_at} update (BNPL-955 §3.8: {@code WHERE customer_id = ? AND relinked_at IS
     * NULL ORDER BY created_at DESC LIMIT 1}).
     */
    Optional<CustomerWmUnlinkRecord> findTopByCustomerIdAndRelinkedAtIsNullOrderByCreatedAtDesc(Long customerId);

    /**
     * Most recent Unlink record for a customer, regardless of relink state — for audit/history
     * lookups (AC-6).
     */
    Optional<CustomerWmUnlinkRecord> findTopByCustomerIdOrderByCreatedAtDesc(Long customerId);
}
