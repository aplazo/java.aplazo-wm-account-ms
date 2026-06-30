package mx.aplazo.microservices.wm.account.feign.model.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body for the post-Unlink notification to partners-ms — contract C-NEW-02
 * {@code POST /wm/unlink/notify} (BNPL-955 §3.7).
 *
 * <p>{@code source} drives the anti-loop rule in partners-ms (ADR-012 D-08): {@code APLAZO}
 * triggers the outbound C-04 call to Cashi; {@code CASHI} skips it.
 *
 * @author Aplazo
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UnlinkNotifyRequest {

    private String customerId;
    private String xClientWm;
    private String source;
    private String reason;
}
