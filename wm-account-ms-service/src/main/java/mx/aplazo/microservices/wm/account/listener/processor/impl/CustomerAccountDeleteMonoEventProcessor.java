package mx.aplazo.microservices.wm.account.listener.processor.impl;

import lombok.extern.slf4j.Slf4j;
import mx.aplazo.microservices.wm.account.constants.CustomerAccountEventConstants;
import mx.aplazo.microservices.wm.account.listener.processor.CustomerAccountEventProcessor;
import mx.aplazo.sns.payload.AplazoBaseSnsPayload;
import org.springframework.stereotype.Component;

/**
 * Processes {@code customer.account.delete.mono} events published by {@code customer-mono-modular}.
 * <p>
 * Triggered when a customer self-deactivates their account via
 * {@code DELETE /api/v1/customer/delete-me} (soft-ban). Unlike {@code customer.account.delete}
 * (full account deletion from customer-registration-service), this event represents a
 * voluntary self-ban initiated by the customer from the app.
 * <p>
 * Inner payload is a Base64-encoded protobuf ({@code CustomerAccountDeletePayload}) with
 * fields: {@code customerId}, {@code comment}, {@code byUser} ("SELF"), {@code timestamp}.
 */
@Slf4j
@Component
public class CustomerAccountDeleteMonoEventProcessor implements CustomerAccountEventProcessor {

    @Override
    public String getEventType() {
        return CustomerAccountEventConstants.EVENT_ACCOUNT_DELETE_MONO;
    }

    @Override
    public void process(AplazoBaseSnsPayload event) {
        log.info("[CustomerAccountDeleteMonoEventProcessor] Processing event type: {}", event.getEventType());
    }
}
