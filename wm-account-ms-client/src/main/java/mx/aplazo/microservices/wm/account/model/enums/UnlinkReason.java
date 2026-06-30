package mx.aplazo.microservices.wm.account.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Reason an Unlink was triggered (contract C-02 / C-NEW-02, BNPL-955 §3.2).
 *
 * <p>{@code USER_REQUEST}, {@code PHONE_CHANGED}, {@code ACCOUNT_STATUS_UPDATE} and
 * {@code ACCOUNT_DELETED} are the inbound values accepted by the shared Unlink operation.
 * {@code SECURITY_ALERT} is outbound-only (Aplazo → Cashi, C-04) but remains a valid stored
 * reason in {@code customer_wm_unlink_record} (BNPL-954 §7.1). The intentional asymmetry is
 * documented in BNPL-955 §3.4 / W8.
 *
 * @author Aplazo
 */
public enum UnlinkReason {

    USER_REQUEST("USER_REQUEST"),
    PHONE_CHANGED("PHONE_CHANGED"),
    ACCOUNT_STATUS_UPDATE("ACCOUNT_STATUS_UPDATE"),
    ACCOUNT_DELETED("ACCOUNT_DELETED"),
    SECURITY_ALERT("SECURITY_ALERT");

    private final String value;

    UnlinkReason(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static UnlinkReason fromValue(String value) {
        for (UnlinkReason reason : UnlinkReason.values()) {
            if (reason.value.equals(value)) {
                return reason;
            }
        }
        throw new IllegalArgumentException("Unknown unlink reason: " + value);
    }

    @Override
    public String toString() {
        return value;
    }
}
