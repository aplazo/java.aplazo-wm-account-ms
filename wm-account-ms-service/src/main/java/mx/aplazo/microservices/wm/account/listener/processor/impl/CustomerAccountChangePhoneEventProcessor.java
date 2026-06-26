package mx.aplazo.microservices.wm.account.listener.processor.impl;

import lombok.extern.slf4j.Slf4j;
import mx.aplazo.microservices.wm.account.constants.CustomerAccountEventConstants;
import mx.aplazo.microservices.wm.account.listener.processor.CustomerAccountEventProcessor;
import mx.aplazo.sns.payload.AplazoBaseSnsPayload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CustomerAccountChangePhoneEventProcessor implements CustomerAccountEventProcessor {

    @Override
    public String getEventType() {
        return CustomerAccountEventConstants.EVENT_ACCOUNT_CHANGE_PHONE;
    }

    @Override
    public void process(AplazoBaseSnsPayload event) {
        log.info("[CustomerAccountChangePhoneEventProcessor] Processing event type: {}", event.getEventType());
    }
}
