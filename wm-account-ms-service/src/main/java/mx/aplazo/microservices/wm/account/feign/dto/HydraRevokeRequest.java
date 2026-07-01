package mx.aplazo.microservices.wm.account.feign.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body for the auth-hydra token revocation contract (C-NEW-01).
 *
 * <p>{@code POST /hydra/revoke} — revokes the Access Token, Refresh Token and Consent Session
 * for the given {@code subject} scoped to {@code clientId}. This is step ② of the Unlink chain
 * and the PRIMARY security barrier (ADR-012 D-02/D-03): it must succeed before any DB write.
 *
 * @author Aplazo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HydraRevokeRequest {

    /** OAuth2 subject whose sessions are revoked. */
    @JsonProperty("subject")
    private String subject;

    /** OAuth2 client the revocation is scoped to (the Walmart/Cashi client). */
    @JsonProperty("clientId")
    private String clientId;

    /** Optional correlation id for audit/tracing. Omitted from the payload when {@code null}. */
    @JsonProperty("externalReferenceId")
    private String externalReferenceId;
}
