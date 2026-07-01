package mx.aplazo.microservices.wm.account.listener.processor.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Inner payload for {@code customer.account.change-phone-number} events.
 * <p>
 * The outer {@link mx.aplazo.sns.payload.AplazoBaseSnsPayload#getPayload()} field is
 * Base64-encoded JSON that maps to this class.
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerAccountChangePhonePayload {

    private Long customerId;
    private String newPhone;
}
