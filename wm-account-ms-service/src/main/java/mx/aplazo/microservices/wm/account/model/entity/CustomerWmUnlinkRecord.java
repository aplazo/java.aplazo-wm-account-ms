package mx.aplazo.microservices.wm.account.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * Audit/history record for a single Unlink event — table
 * {@code wm_integration.customer_wm_unlink_record} (ADR-012 D-04, BNPL-955 §5.1 Table B).
 *
 * <p>One row per Unlink event; supports multi-Unlink history per customer. {@code wm-account-ms}
 * is the sole writer (golden rule, ADR-012 D-04). {@code relinked_at} is populated on the most
 * recent {@code NULL} record when a Relink completes (C-NEW-03, written via the service layer).
 *
 * <p>The table itself is created by the schema migration (P4 / DBA) — not by this module.
 *
 * @author Aplazo
 */
@Entity
@Table(name = "customer_wm_unlink_record", schema = "wm_integration")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerWmUnlinkRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "unlinked_at")
    private LocalDateTime unlinkedAt;

    @Column(name = "unlink_reason", length = 64)
    private String unlinkReason;

    @Column(name = "unlink_initiated_by", length = 16)
    private String unlinkInitiatedBy;

    @Column(name = "unlink_detail", columnDefinition = "TEXT")
    private String unlinkDetail;

    @Column(name = "relinked_at")
    private LocalDateTime relinkedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
