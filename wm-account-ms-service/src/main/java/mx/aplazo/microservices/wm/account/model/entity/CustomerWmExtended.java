package mx.aplazo.microservices.wm.account.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * Current link state of a Walmart customer — table {@code wm_integration.customer_wm_extended}.
 *
 * <p>This is the same physical table owned for {@code status=LINKED} by {@code partners-ms};
 * {@code wm-account-ms} is the sole writer of {@code status=UNLINKED} and the OAuth-field cleanup
 * performed on Unlink (ADR-012 D-01/D-04). Only the subset of columns the Unlink flow reads or
 * writes is mapped here. Column names mirror the {@code partners-ms} entity.
 *
 * @author Aplazo
 */
@Entity
@Table(name = "customer_wm_extended", schema = "wm_integration",
        uniqueConstraints = @UniqueConstraint(columnNames = "x_client_wm"))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerWmExtended {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "x_client_wm")
    private String xClientWm;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private CustomerWmStatus status;

    @Column(name = "code")
    private String code;

    @Column(name = "oauth_state", length = 36)
    private String oauthState;

    @Column(name = "oauth_code_verifier", length = 128)
    private String oauthCodeVerifier;

    @Column(name = "oauth_callback_url", columnDefinition = "TEXT")
    private String oauthCallbackUrl;

    @Column(name = "linking_account_url", columnDefinition = "TEXT")
    private String linkingAccountUrl;

    @Column(name = "refresh_token_expires_at")
    private LocalDateTime refreshTokenExpiresAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
