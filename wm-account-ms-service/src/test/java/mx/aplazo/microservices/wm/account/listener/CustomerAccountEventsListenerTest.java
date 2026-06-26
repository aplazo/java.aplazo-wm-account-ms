package mx.aplazo.microservices.wm.account.listener;

import mx.aplazo.microservices.wm.account.constants.CustomerAccountEventConstants;
import mx.aplazo.microservices.wm.account.helper.AbstractAplazoUnitTest;
import mx.aplazo.microservices.wm.account.listener.processor.CustomerAccountEventProcessor;
import mx.aplazo.sns.payload.AplazoBaseSnsPayload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("CustomerAccountEventsListener — routing de eventos SQS")
class CustomerAccountEventsListenerTest extends AbstractAplazoUnitTest {

    @Mock
    private CustomerAccountEventProcessor deleteProcessor;

    @Mock
    private CustomerAccountEventProcessor changePhoneProcessor;

    @Mock
    private CustomerAccountEventProcessor frozenProcessor;

    private CustomerAccountEventsListener listener;

    @BeforeEach
    void initListener() {
        when(deleteProcessor.getEventType()).thenReturn(CustomerAccountEventConstants.EVENT_ACCOUNT_DELETE);
        when(changePhoneProcessor.getEventType()).thenReturn(CustomerAccountEventConstants.EVENT_ACCOUNT_CHANGE_PHONE);
        when(frozenProcessor.getEventType()).thenReturn(CustomerAccountEventConstants.EVENT_ACCOUNT_FROZEN);

        listener = new CustomerAccountEventsListener(List.of(deleteProcessor, changePhoneProcessor, frozenProcessor));
        listener.init();
    }

    private static String buildMessage(String eventType) {
        return "{\"eventType\":\"" + eventType + "\",\"payload\":\"dGVzdA==\",\"eventMeta\":{}}";
    }

    @Nested
    @DisplayName("Routing a processor correcto")
    class RoutingHappyPath {

        @Test
        @DisplayName("customer.account.delete → deleteProcessor")
        void onMessage_routesToDeleteProcessor() {
            listener.onMessage(buildMessage(CustomerAccountEventConstants.EVENT_ACCOUNT_DELETE));

            verify(deleteProcessor).process(any(AplazoBaseSnsPayload.class));
            verify(changePhoneProcessor, never()).process(any());
            verify(frozenProcessor, never()).process(any());
        }

        @Test
        @DisplayName("customer.account.change-phone-number → changePhoneProcessor")
        void onMessage_routesToChangePhoneProcessor() {
            listener.onMessage(buildMessage(CustomerAccountEventConstants.EVENT_ACCOUNT_CHANGE_PHONE));

            verify(changePhoneProcessor).process(any(AplazoBaseSnsPayload.class));
            verify(deleteProcessor, never()).process(any());
            verify(frozenProcessor, never()).process(any());
        }

        @Test
        @DisplayName("customer.account.frozen → frozenProcessor")
        void onMessage_routesToFrozenProcessor() {
            listener.onMessage(buildMessage(CustomerAccountEventConstants.EVENT_ACCOUNT_FROZEN));

            verify(frozenProcessor).process(any(AplazoBaseSnsPayload.class));
            verify(deleteProcessor, never()).process(any());
            verify(changePhoneProcessor, never()).process(any());
        }

        @Test
        @DisplayName("Matching es case-insensitive")
        void onMessage_routesCaseInsensitive() {
            listener.onMessage(buildMessage("CUSTOMER.ACCOUNT.DELETE"));

            verify(deleteProcessor).process(any(AplazoBaseSnsPayload.class));
        }
    }

    @Nested
    @DisplayName("Evento desconocido")
    class UnknownEvent {

        @Test
        @DisplayName("No lanza excepción para un eventType no registrado")
        void onMessage_unknownEventType_doesNotThrow() {
            assertThatCode(() -> listener.onMessage(buildMessage("customer.account.unknown")))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Ningún processor es invocado para un eventType no registrado")
        void onMessage_unknownEventType_noProcessorInvoked() {
            listener.onMessage(buildMessage("customer.account.unknown"));

            verify(deleteProcessor, never()).process(any());
            verify(changePhoneProcessor, never()).process(any());
            verify(frozenProcessor, never()).process(any());
        }
    }
}
