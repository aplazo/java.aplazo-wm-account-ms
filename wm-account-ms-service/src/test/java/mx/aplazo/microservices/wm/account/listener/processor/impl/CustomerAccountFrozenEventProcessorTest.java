package mx.aplazo.microservices.wm.account.listener.processor.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import mx.aplazo.microservices.wm.account.constants.CustomerAccountEventConstants;
import mx.aplazo.microservices.wm.account.helper.AbstractAplazoUnitTest;
import mx.aplazo.sns.payload.AplazoBaseSnsPayload;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@DisplayName("CustomerAccountFrozenEventProcessor")
class CustomerAccountFrozenEventProcessorTest extends AbstractAplazoUnitTest {

    private final CustomerAccountFrozenEventProcessor processor = new CustomerAccountFrozenEventProcessor();
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    @DisplayName("getEventType retorna la constante correcta")
    void getEventType_returnsCorrectConstant() {
        assertThat(processor.getEventType()).isEqualTo(CustomerAccountEventConstants.EVENT_ACCOUNT_FROZEN);
    }

    @Nested
    @DisplayName("Manejo por status")
    class StatusHandling {

        @Test
        @DisplayName("Status BLOCKED — procesa sin lanzar excepción")
        void process_blockedStatus_completesWithoutException() {
            AplazoBaseSnsPayload payload = buildPayload(1790L,
                    CustomerAccountEventConstants.FROZEN_STATUS_BLOCKED, "RISK_BLOCKED");

            assertThatCode(() -> processor.process(payload)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Status BANNED — procesa sin lanzar excepción")
        void process_bannedStatus_completesWithoutException() {
            AplazoBaseSnsPayload payload = buildPayload(1791L,
                    CustomerAccountEventConstants.FROZEN_STATUS_BANNED, "RISK_BANNED");

            assertThatCode(() -> processor.process(payload)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Status desconocido — procesa sin lanzar excepción")
        void process_unknownStatus_completesWithoutException() {
            AplazoBaseSnsPayload payload = buildPayload(1792L, "NORMAL", null);

            assertThatCode(() -> processor.process(payload)).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Payload inválido")
    class InvalidPayload {

        @Test
        @DisplayName("Payload no decodificable — no lanza excepción")
        void process_invalidBase64Payload_doesNotThrow() {
            AplazoBaseSnsPayload payload = AplazoBaseSnsPayload.fromJsonString(
                    "{\"eventType\":\"customer.account.frozen\",\"payload\":\"!!notbase64!!\",\"eventMeta\":{}}");

            assertThatCode(() -> processor.process(payload)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Payload Base64 con JSON inválido — no lanza excepción")
        void process_invalidJsonPayload_doesNotThrow() {
            String badBase64 = Base64.getEncoder().encodeToString("not-a-json".getBytes());
            AplazoBaseSnsPayload payload = AplazoBaseSnsPayload.fromJsonString(
                    "{\"eventType\":\"customer.account.frozen\",\"payload\":\"" + badBase64 + "\",\"eventMeta\":{}}");

            assertThatCode(() -> processor.process(payload)).doesNotThrowAnyException();
        }
    }

    // -------------------------------------------------------------------------

    private static AplazoBaseSnsPayload buildPayload(Long customerId, String status, String reason) {
        try {
            CustomerAccountFrozenPayload inner = new CustomerAccountFrozenPayload();
            inner.setCustomerId(customerId);
            inner.setCurrentStatus(status);
            inner.setCurrentReason(reason);

            String base64 = Base64.getEncoder().encodeToString(MAPPER.writeValueAsBytes(inner));
            return AplazoBaseSnsPayload.fromJsonString(
                    "{\"eventType\":\"customer.account.frozen\",\"payload\":\"" + base64 + "\",\"eventMeta\":{}}");
        } catch (Exception e) {
            throw new RuntimeException("Error building test payload", e);
        }
    }
}
