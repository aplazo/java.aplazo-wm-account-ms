package mx.aplazo.microservices.wm.account.listener.processor;

import mx.aplazo.sns.payload.AplazoBaseSnsPayload;

/**
 * Contract for processing a specific customer account event type.
 * <p>
 * Each implementation handles one {@code eventType} value (e.g. {@code customer.account.delete}).
 * The {@link mx.aplazo.microservices.wm.account.listener.CustomerAccountEventsListener} resolves
 * the correct processor at runtime based on {@link #getEventType()}.
 */
public interface CustomerAccountEventProcessor {

    /**
     * The {@code eventType} string this processor handles (e.g. {@code "customer.account.frozen"}).
     */
    String getEventType();

    /**
     * Processes the incoming SNS payload for this event type.
     *
     * @param event the deserialized SNS payload containing {@code eventType} and raw {@code payload}
     */
    void process(AplazoBaseSnsPayload event);
}
