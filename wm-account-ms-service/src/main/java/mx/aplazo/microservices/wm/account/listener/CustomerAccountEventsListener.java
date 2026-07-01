package mx.aplazo.microservices.wm.account.listener;

import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mx.aplazo.microservices.wm.account.constants.CustomerAccountEventConstants;
import mx.aplazo.microservices.wm.account.listener.processor.CustomerAccountEventProcessor;
import mx.aplazo.sns.payload.AplazoBaseSnsPayload;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * SQS listener for customer account lifecycle events.
 * <p>
 * Handles the following event types:
 * <ul>
 *   <li>{@code customer.account.delete} — account deletion</li>
 *   <li>{@code customer.account.change-phone-number} — phone number change</li>
 *   <li>{@code customer.account.frozen} — risk status change (FROZEN / NORMAL)</li>
 * </ul>
 * <p>
 * Each event type is delegated to its own {@link CustomerAccountEventProcessor} implementation.
 * Controlled by {@code api.aplazo.customer.account.changes.sqs.enabled} (default: {@code true}).
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = CustomerAccountEventConstants.SQS_ENABLED_PROPERTY,
        havingValue = "true",
        matchIfMissing = true)
public class CustomerAccountEventsListener {

    private final List<CustomerAccountEventProcessor> processors;

    private Map<String, CustomerAccountEventProcessor> processorMap;

    @jakarta.annotation.PostConstruct
    void init() {
        processorMap = processors.stream()
                .collect(Collectors.toMap(
                        p -> p.getEventType().toLowerCase(),
                        Function.identity()));
        log.info("[CustomerAccountEventsListener] Initialized with processors: {}", processorMap.keySet());
    }

    @SqsListener(CustomerAccountEventConstants.SQS_QUEUE_NAME_PROPERTY)
    public void onMessage(String message) {
        log.info("[CustomerAccountEventsListener] Message received from SQS");

        AplazoBaseSnsPayload basePayload = AplazoBaseSnsPayload.fromJsonString(message);
        String eventType = basePayload.getEventType();
        log.info("[CustomerAccountEventsListener] Event type identified: {}", eventType);

        CustomerAccountEventProcessor processor = processorMap.get(eventType.toLowerCase());
        if (processor != null) {
            processor.process(basePayload);
        } else {
            log.warn("[CustomerAccountEventsListener] No processor registered for event type: {}", eventType);
        }
    }
}
