package mx.aplazo.microservices.wm.account.repository;

import java.util.Optional;
import java.util.UUID;
import mx.aplazo.microservices.wm.account.model.entity.CustomerWmExtended;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Data access for {@code wm_integration.customer_wm_extended} used by the Unlink flow.
 *
 * <p>Two lookup keys converge on the same canonical operation: {@code xClientWm} for the internal
 * endpoint (C-02, Triggers B/C) and {@code customerId} for the user endpoint (C-03, Trigger A —
 * the App only carries the customer identity in its token, not the Walmart client id).
 *
 * @author Aplazo
 */
@Repository
public interface CustomerWmExtendedRepository extends JpaRepository<CustomerWmExtended, UUID> {

    /** Lookup by Walmart client id (explicit {@code @Query} since the field starts with 'x'). */
    @Query("SELECT c FROM CustomerWmExtended c WHERE c.xClientWm = :xClientWm")
    Optional<CustomerWmExtended> findByXClientWm(@Param("xClientWm") String xClientWm);

    /** Lookup by Aplazo customer id (Trigger A — resolved from the ROLE_CUSTOMER token). */
    Optional<CustomerWmExtended> findByCustomerId(Long customerId);
}
