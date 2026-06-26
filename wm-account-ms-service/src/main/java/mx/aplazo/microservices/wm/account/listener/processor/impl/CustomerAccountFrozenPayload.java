package mx.aplazo.microservices.wm.account.listener.processor.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Inner payload for {@code customer.account.frozen} events.
 * <p>
 * The outer {@link mx.aplazo.sns.payload.AplazoBaseSnsPayload#getPayload()} field is
 * Base64-encoded JSON that maps to this class.
 * <p>
 * {@code currentStatus} holds the risk catalog status:
 * <ul>
 *   <li>{@code BLOCKED} — customer is operationally blocked (BLOCKED / BLOCKED_RECURRENTS / BLOCKED_2ND_PURCHASE)</li>
 *   <li>{@code BANNED}  — customer is permanently banned</li>
 *   <li>{@code NORMAL}  — account restored to normal state</li>
 * </ul>
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerAccountFrozenPayload {

    private Long customerId;
    private String currentStatus;
    private String currentReason;
    private String previousStatus;
    private String previousReason;
}
