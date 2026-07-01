package mx.aplazo.microservices.wm.account.listener.processor.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import mx.aplazo.microservices.wm.account.constants.CustomerAccountEventConstants;
import mx.aplazo.microservices.wm.account.listener.processor.CustomerAccountEventProcessor;
import mx.aplazo.sns.payload.AplazoBaseSnsPayload;
import org.springframework.stereotype.Component;

import java.util.Base64;

/**
 * Processes {@code customer.account.change-phone-number} events published by
 * {@code customer-registration-service} when a customer completes a phone change
 * via the Welcome Back flow.
 * <p>
 * If the customer has an active Cashi link, the unlink operation is triggered
 * with {@code reason: PHONE_CHANGED}. Cashi notification failure never blocks
 * or rolls back the phone update.
 */
@Slf4j
@Component
public class CustomerAccountChangePhoneEventProcessor implements CustomerAccountEventProcessor {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public String getEventType() {
        return CustomerAccountEventConstants.EVENT_ACCOUNT_CHANGE_PHONE;
    }

    @Override
    public void process(AplazoBaseSnsPayload event) {
        log.info("[CustomerAccountChangePhoneEventProcessor] Received event type: {}", event.getEventType());
        try {
            String json = new String(Base64.getDecoder().decode(event.getPayload()));
            CustomerAccountChangePhonePayload payload = OBJECT_MAPPER.readValue(json, CustomerAccountChangePhonePayload.class);
            triggerCashiUnlink(payload);
        } catch (Exception e) {
            log.warn("[CustomerAccountChangePhoneEventProcessor] Could not decode payload — {}", e.getMessage());
        }
    }

    private void triggerCashiUnlink(CustomerAccountChangePhonePayload payload) {
        log.info("[CustomerAccountChangePhoneEventProcessor] Phone change confirmed customerId={} — evaluating Cashi unlink",
                payload.getCustomerId());
        // BNPL-887: check if customer has active Cashi link (LINKED status)
        // and invoke unlink service with reason=PHONE_CHANGED once BNPL-887 is merged.
    }
}
