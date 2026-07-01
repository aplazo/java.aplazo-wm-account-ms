package mx.aplazo.microservices.wm.account.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import mx.aplazo.microservices.wm.account.model.enums.UnlinkInitiatedBy;
import mx.aplazo.microservices.wm.account.model.enums.UnlinkReason;
import mx.aplazo.microservices.wm.account.model.enums.UnlinkSource;

/**
 * Request body for the shared Unlink operation — contract C-02
 * {@code POST /wm/account/unlink} (BNPL-955 §3.2, ADR-012 D-12/D-13).
 *
 * <p>All triggers converge on this contract: Trigger B (partners-ms proxy) and the internal
 * Nexus consumer (Trigger C) populate it before invoking {@code wm-account-ms}. Validation
 * enforcement ({@code @Valid}) and the field-belongs-to-customer check are wired by BNPL-886.
 *
 * @author Aplazo
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UnlinkAccountRequest {

    /** Customer identifier created by Walmart (header {@code X-Client-Wm} on inbound triggers). */
    private String xClientWm;

    /** Why the Unlink was triggered. */
    private UnlinkReason reason;

    /** Origin system — drives the anti-loop rule on the downstream notification (ADR-012 D-08). */
    private UnlinkSource source;

    /** Acting party that initiated the Unlink. */
    private UnlinkInitiatedBy initiatedBy;

    /** Optional free-text detail, persisted in {@code customer_wm_unlink_record.unlink_detail}. */
    private String detail;
}
