package mx.aplazo.microservices.wm.account.listener.processor.impl;

import mx.aplazo.microservices.wm.account.constants.CustomerAccountEventConstants;
import mx.aplazo.microservices.wm.account.helper.AbstractAplazoUnitTest;
import mx.aplazo.sns.payload.AplazoBaseSnsPayload;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@DisplayName("CustomerAccountDeleteMonoEventProcessor")
class CustomerAccountDeleteMonoEventProcessorTest extends AbstractAplazoUnitTest {

    private final CustomerAccountDeleteMonoEventProcessor processor = new CustomerAccountDeleteMonoEventProcessor();

    @Test
    @DisplayName("getEventType retorna la constante correcta")
    void getEventType_returnsCorrectConstant() {
        assertThat(processor.getEventType()).isEqualTo(CustomerAccountEventConstants.EVENT_ACCOUNT_DELETE_MONO);
    }

    @Test
    @DisplayName("process completa sin lanzar excepción")
    void process_completesWithoutException() {
        AplazoBaseSnsPayload payload = AplazoBaseSnsPayload.fromJsonString(
                "{\"eventType\":\"customer.account.delete.mono\",\"payload\":\"dGVzdA==\",\"eventMeta\":{}}");

        assertThatCode(() -> processor.process(payload)).doesNotThrowAnyException();
    }
}
