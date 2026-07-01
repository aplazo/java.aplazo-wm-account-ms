package mx.aplazo.microservices.wm.account.listener.processor.impl;

import mx.aplazo.microservices.wm.account.constants.CustomerAccountEventConstants;
import mx.aplazo.microservices.wm.account.helper.AbstractAplazoUnitTest;
import mx.aplazo.sns.payload.AplazoBaseSnsPayload;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@DisplayName("CustomerAccountChangePhoneEventProcessor")
class CustomerAccountChangePhoneEventProcessorTest extends AbstractAplazoUnitTest {

    private final CustomerAccountChangePhoneEventProcessor processor = new CustomerAccountChangePhoneEventProcessor();

    @Test
    @DisplayName("getEventType returns the correct constant")
    void getEventType_returnsCorrectConstant() {
        assertThat(processor.getEventType()).isEqualTo(CustomerAccountEventConstants.EVENT_ACCOUNT_CHANGE_PHONE);
    }

    @Test
    @DisplayName("process completes without throwing exception with valid payload")
    void process_completesWithoutException() {
        String base64Payload = Base64.getEncoder().encodeToString(
                "{\"customerId\":123,\"newPhone\":\"5551234567\"}".getBytes());
        AplazoBaseSnsPayload payload = AplazoBaseSnsPayload.fromJsonString(
                "{\"eventType\":\"customer.account.change-phone-number\",\"payload\":\"" + base64Payload + "\",\"eventMeta\":{}}");

        assertThatCode(() -> processor.process(payload)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("process does not throw when payload is not valid Base64")
    void process_invalidBase64Payload_doesNotThrow() {
        AplazoBaseSnsPayload payload = AplazoBaseSnsPayload.fromJsonString(
                "{\"eventType\":\"customer.account.change-phone-number\",\"payload\":\"not-valid-base64!!!\",\"eventMeta\":{}}");

        assertThatCode(() -> processor.process(payload)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("process does not throw when payload is null")
    void process_nullPayload_doesNotThrow() {
        AplazoBaseSnsPayload payload = AplazoBaseSnsPayload.fromJsonString(
                "{\"eventType\":\"customer.account.change-phone-number\",\"eventMeta\":{}}");

        assertThatCode(() -> processor.process(payload)).doesNotThrowAnyException();
    }
}
