package mx.aplazo.microservices.wm.account.service;

import mx.aplazo.microservices.wm.account.model.request.UnlinkAccountRequest;
import mx.aplazo.microservices.wm.account.model.response.UnlinkAccountResponse;

/**
 * Shared Unlink operation — the reusable core every trigger converges on (ADR-012 D-03,
 * BNPL-955 §5.2). This interface declares the contract only; the {@code @Service}
 * implementation under {@code service.impl} is delivered by BNPL-886.
 *
 * <p>Two entry points resolve the customer differently and converge on the same canonical flow:
 * {@link #unlink(UnlinkAccountRequest)} resolves by {@code xClientWm} (C-02, Triggers B/C);
 * {@link #unlinkForCustomer(Long)} resolves by {@code customerId} (C-03, Trigger A — the App
 * carries only the customer identity in its token, not the Walmart client id).
 *
 * <p>Canonical behavior of the implementation:
 * <ol>
 *   <li>State guard on {@code customer_wm_extended.status} — proceed only for
 *       {LINKED, LINKED_TOKENIZED, REAUTH_PENDING}; idempotent {@code CUSTOMER_ALREADY_UNLINKED}
 *       for UNLINKED; {@code 404}/{@code 422} otherwise.</li>
 *   <li>[SYNC] auth-hydra token revocation BEFORE any DB write — PRIMARY security barrier
 *       (ADR-012 D-02/D-03). Failure aborts with no state change.</li>
 *   <li>Single atomic PostgreSQL transaction: INSERT customer_wm_unlink_record +
 *       UPDATE customer_wm_extended (status=UNLINKED + OAuth cleanup).</li>
 *   <li>[SYNC] partners-ms notify (C-NEW-02); no rollback on permanent failure (ADR-012 D-10).</li>
 * </ol>
 *
 * @author Aplazo
 */
public interface UnlinkService {

    /**
     * Execute the shared Unlink operation resolving the customer by {@code xClientWm}
     * (C-02 — Triggers B/C).
     *
     * @param request the unlink request (xClientWm, reason, source, initiatedBy, detail)
     * @return the operation outcome (code + message)
     */
    UnlinkAccountResponse unlink(UnlinkAccountRequest request);

    /**
     * Execute the shared Unlink operation for a user-initiated request (C-03, Trigger A),
     * resolving the customer by {@code customerId} taken from the ROLE_CUSTOMER token. The
     * remaining context is inferred: {@code source=APLAZO}, {@code initiatedBy=USER},
     * {@code reason=USER_REQUEST}.
     *
     * @param customerId the Aplazo customer id from the authenticated token
     * @return the operation outcome (code + message)
     */
    UnlinkAccountResponse unlinkForCustomer(Long customerId);
}
