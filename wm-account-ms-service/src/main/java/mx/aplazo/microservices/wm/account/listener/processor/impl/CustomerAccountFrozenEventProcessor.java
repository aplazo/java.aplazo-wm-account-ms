package mx.aplazo.microservices.wm.account.listener.processor.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import mx.aplazo.microservices.wm.account.constants.CustomerAccountEventConstants;
import mx.aplazo.microservices.wm.account.listener.processor.CustomerAccountEventProcessor;
import mx.aplazo.sns.payload.AplazoBaseSnsPayload;
import org.springframework.stereotype.Component;

import java.util.Base64;

/**
 * Processes {@code customer.account.frozen} events published by {@code customer-risk-ms}.
 * <p>
 * The inner payload is Base64-encoded JSON that maps to {@link CustomerAccountFrozenPayload},
 * with fields {@code customerId}, {@code currentStatus}, {@code currentReason},
 * {@code previousStatus}, and {@code previousReason}.
 * <p>
 * Handled {@code currentStatus} values:
 * <ul>
 *   <li>{@code BLOCKED} — customer is operationally blocked; a dedicated log entry is emitted.</li>
 *   <li>{@code BANNED}  — customer is permanently banned; the received status is logged.</li>
 *   <li>Any other value — the status is logged for observability.</li>
 * </ul>
 */
@Slf4j
@Component
public class CustomerAccountFrozenEventProcessor implements CustomerAccountEventProcessor {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public String getEventType() {
        return CustomerAccountEventConstants.EVENT_ACCOUNT_FROZEN;
    }

    @Override
    public void process(AplazoBaseSnsPayload event) {
        log.info("[CustomerAccountFrozenEventProcessor] Received event type: {}", event.getEventType());
        try {
            String json = new String(Base64.getDecoder().decode(event.getPayload()));
            CustomerAccountFrozenPayload payload = OBJECT_MAPPER.readValue(json, CustomerAccountFrozenPayload.class);
            handleStatus(payload);
        } catch (Exception e) {
            log.warn("[CustomerAccountFrozenEventProcessor] Could not decode payload — {}", e.getMessage());
        }
    }

    private void handleStatus(CustomerAccountFrozenPayload payload) {
        String status = payload.getCurrentStatus();

        if (CustomerAccountEventConstants.FROZEN_STATUS_BLOCKED.equalsIgnoreCase(status)) {
            log.info("[CustomerAccountFrozenEventProcessor] Customer {} is BLOCKED — reason: {}",
                    payload.getCustomerId(), payload.getCurrentReason());

        } else if (CustomerAccountEventConstants.FROZEN_STATUS_BANNED.equalsIgnoreCase(status)) {
            log.info("[CustomerAccountFrozenEventProcessor] Customer {} status: {}",
                    payload.getCustomerId(), status);

        } else {
            log.info("[CustomerAccountFrozenEventProcessor] Customer {} — unhandled status: {}",
                    payload.getCustomerId(), status);
        }
    }
}
