package mx.aplazo.microservices.wm.account.model.enums;

import lombok.Generated;
import mx.aplazo.exception.AplazoErrorEnum;

/**
 * Canonical error codes for the Unlink operation (BNPL-955 §3.2 / §3.6). Reused by the BNPL-886
 * implementation when throwing {@code AplazoException}, so error responses stay aligned with the
 * accepted contract.
 *
 * <ul>
 *   <li>{@code INVALID_REQUEST} — 400, malformed body or missing required field.</li>
 *   <li>{@code CUSTOMER_NOT_FOUND} — 404, xClientWm does not exist.</li>
 *   <li>{@code CUSTOMER_ALREADY_UNLINKED} — idempotent already-unlinked response.</li>
 *   <li>{@code CONCURRENT_UNLINK_IN_PROGRESS} — 409, an Unlink is already executing.</li>
 *   <li>{@code CUSTOMER_NOT_ELIGIBLE_FOR_UNLINK} — 422, status outside the guard set.</li>
 *   <li>{@code HYDRA_REVOCATION_FAILED} — 500, auth-hydra call failed at step ② (Unlink aborted,
 *       no state change).</li>
 * </ul>
 *
 * @author Aplazo
 */
@Generated
public enum UnlinkErrorCode implements AplazoErrorEnum {

    INVALID_REQUEST,
    CUSTOMER_NOT_FOUND,
    CUSTOMER_ALREADY_UNLINKED,
    CONCURRENT_UNLINK_IN_PROGRESS,
    CUSTOMER_NOT_ELIGIBLE_FOR_UNLINK,
    HYDRA_REVOCATION_FAILED;

    @Override
    public String getName() {
        return this.name();
    }
}
