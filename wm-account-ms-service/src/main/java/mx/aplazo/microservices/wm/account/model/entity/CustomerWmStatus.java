package mx.aplazo.microservices.wm.account.model.entity;

import lombok.Generated;

/**
 * Link state of a Walmart customer in {@code wm_integration.customer_wm_extended.status}.
 * Values mirror {@code partners-ms} so the column (mapped with {@code @Enumerated(STRING)})
 * stays consistent across both services.
 *
 * <p>The Unlink state guard (ADR-012 D-03, BNPL-955 §4) admits only
 * {@link #LINKED}, {@link #LINKED_TOKENIZED} and {@link #REAUTH_PENDING}; {@link #UNLINKED}
 * is the idempotent terminal state.
 *
 * @author Aplazo
 */
@Generated
public enum CustomerWmStatus {

    NEW_CUSTOMER_PENDING("new_customer_pending"),
    WM_EXISTING_NEW_CUSTOMER_PENDING("wm_existing_new_customer_pending"),
    CUSTOMER_PENDING("customer_pending"),
    WM_EXISTING_CUSTOMER_PENDING("wm_existing_customer_pending"),
    LINKED("linked"),
    UNLINKED("unlinked"),
    REVOKED("revoked"),
    REJECTED("rejected"),
    LINKED_TOKENIZED("linked_tokenized"),
    REAUTH_PENDING("reauth_pending");

    private final String value;

    CustomerWmStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
