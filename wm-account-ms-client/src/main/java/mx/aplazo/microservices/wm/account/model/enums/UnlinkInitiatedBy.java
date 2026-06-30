package mx.aplazo.microservices.wm.account.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Actor that initiated an Unlink (contract C-02, BNPL-955 §3.2). Persisted in
 * {@code customer_wm_unlink_record.unlink_initiated_by}.
 *
 * <p>Distinct from {@link UnlinkSource}: {@code source} is the origin system (APLAZO/CASHI),
 * while {@code initiatedBy} is the acting party. Trigger A → {@code USER}, Trigger B →
 * {@code PARTNER}, Trigger C (internal Nexus events) → {@code SYSTEM} (ADR-012 D-07),
 * Aplazo Ops → {@code OPS}.
 *
 * @author Aplazo
 */
public enum UnlinkInitiatedBy {

    USER("USER"),
    PARTNER("PARTNER"),
    SYSTEM("SYSTEM"),
    OPS("OPS");

    private final String value;

    UnlinkInitiatedBy(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static UnlinkInitiatedBy fromValue(String value) {
        for (UnlinkInitiatedBy initiatedBy : UnlinkInitiatedBy.values()) {
            if (initiatedBy.value.equals(value)) {
                return initiatedBy;
            }
        }
        throw new IllegalArgumentException("Unknown unlink initiatedBy: " + value);
    }

    @Override
    public String toString() {
        return value;
    }
}
