package mx.aplazo.microservices.wm.account.feign.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response body from the post-Unlink notification to partners-ms — contract C-NEW-02
 * {@code POST /wm/unlink/notify} (BNPL-955 §3.7). Expected: {@code { "status": "NOTIFIED" }}.
 *
 * @author Aplazo
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UnlinkNotifyResponse {

    private String status;
}
