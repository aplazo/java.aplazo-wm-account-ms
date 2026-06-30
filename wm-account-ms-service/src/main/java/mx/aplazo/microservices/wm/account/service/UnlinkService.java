package mx.aplazo.microservices.wm.account.service;

import mx.aplazo.microservices.wm.account.model.request.UnlinkAccountRequest;
import mx.aplazo.microservices.wm.account.model.response.UnlinkAccountResponse;

/**
 * Shared Unlink operation — the reusable core every trigger converges on (ADR-012 D-03,
 * BNPL-955 §5.2). This interface declares the contract only; the {@code @Service}
 * implementation under {@code service.impl} is delivered by BNPL-886.
 *
 * <p>Expected canonical behavior of the implementation:
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
     * Execute the shared Unlink operation for the given request.
     *
     * @param request the unlink request (xClientWm, reason, source, initiatedBy, detail)
     * @return the operation outcome (code + message)
     */
    UnlinkAccountResponse unlink(UnlinkAccountRequest request);
}
