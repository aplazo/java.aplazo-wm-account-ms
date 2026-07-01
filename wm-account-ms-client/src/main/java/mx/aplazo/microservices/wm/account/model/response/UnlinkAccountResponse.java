package mx.aplazo.microservices.wm.account.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response body for the shared Unlink operation — contract C-02
 * {@code POST /wm/account/unlink} (BNPL-955 §3.2).
 *
 * <p>On success: {@code { "code": "SUCCESS", "message": "x-client-wm: {xClientWm} unlinked" }}.
 * The idempotent already-unlinked case returns {@code code = CUSTOMER_ALREADY_UNLINKED}.
 *
 * @author Aplazo
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UnlinkAccountResponse {

    /** Outcome code (e.g. {@code SUCCESS}, {@code CUSTOMER_ALREADY_UNLINKED}). */
    private String code;

    /** Human-readable message. */
    private String message;
}
