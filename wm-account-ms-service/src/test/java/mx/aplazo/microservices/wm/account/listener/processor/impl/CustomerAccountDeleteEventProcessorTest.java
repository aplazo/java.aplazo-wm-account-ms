package mx.aplazo.microservices.wm.account.listener.processor.impl;

import mx.aplazo.microservices.wm.account.constants.CustomerAccountEventConstants;
import mx.aplazo.microservices.wm.account.helper.AbstractAplazoUnitTest;
import mx.aplazo.sns.payload.AplazoBaseSnsPayload;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@DisplayName("CustomerAccountDeleteEventProcessor")
class CustomerAccountDeleteEventProcessorTest extends AbstractAplazoUnitTest {

    private final CustomerAccountDeleteEventProcessor processor = new CustomerAccountDeleteEventProcessor();

    @Test
    @DisplayName("getEventType retorna la constante correcta")
    void getEventType_returnsCorrectConstant() {
        assertThat(processor.getEventType()).isEqualTo(CustomerAccountEventConstants.EVENT_ACCOUNT_DELETE);
    }

    @Test
    @DisplayName("process completa sin lanzar excepción")
    void process_completesWithoutException() {
        AplazoBaseSnsPayload payload = AplazoBaseSnsPayload.fromJsonString(
                "{\"eventType\":\"customer.account.delete\",\"payload\":\"dGVzdA==\",\"eventMeta\":{}}");

        assertThatCode(() -> processor.process(payload)).doesNotThrowAnyException();
    }
}
