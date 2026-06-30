package mx.aplazo.microservices.wm.account.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Origin system of an Unlink request (contract C-02 / C-NEW-02, BNPL-955 §3.2).
 *
 * <p>Drives the anti-loop rule on the downstream notification (ADR-012 D-08): only
 * {@code APLAZO} triggers the outbound C-04 call to Cashi; {@code CASHI} skips it.
 *
 * @author Aplazo
 */
public enum UnlinkSource {

    APLAZO("APLAZO"),
    CASHI("CASHI");

    private final String value;

    UnlinkSource(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static UnlinkSource fromValue(String value) {
        for (UnlinkSource source : UnlinkSource.values()) {
            if (source.value.equals(value)) {
                return source;
            }
        }
        throw new IllegalArgumentException("Unknown unlink source: " + value);
    }

    @Override
    public String toString() {
        return value;
    }
}
