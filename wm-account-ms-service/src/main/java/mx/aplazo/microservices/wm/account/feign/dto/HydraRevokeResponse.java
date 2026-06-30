package mx.aplazo.microservices.wm.account.feign.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response body for the auth-hydra token revocation contract (C-NEW-01).
 *
 * <p>Returned on a {@code 200 OK} from {@code POST /hydra/revoke}.
 *
 * @author Aplazo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HydraRevokeResponse {

    /** Number of OAuth2 sessions revoked by auth-hydra for the requested subject/client. */
    @JsonProperty("sessionsRevoked")
    private Integer sessionsRevoked;
}
