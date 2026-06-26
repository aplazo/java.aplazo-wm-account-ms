package mx.aplazo.microservices.wm.account.constants;

public final class CustomerAccountEventConstants {

    private CustomerAccountEventConstants() {}

    public static final String EVENT_ACCOUNT_DELETE = "customer.account.delete";
    public static final String EVENT_ACCOUNT_DELETE_MONO = "customer.account.delete.mono";
    public static final String EVENT_ACCOUNT_CHANGE_PHONE = "customer.account.change-phone-number";
    public static final String EVENT_ACCOUNT_FROZEN = "customer.account.frozen";

    /** Risk catalog status published inside {@code customer.account.frozen} payload. */
    public static final String FROZEN_STATUS_BLOCKED = "BLOCKED";
    /** Risk catalog status published inside {@code customer.account.frozen} payload. */
    public static final String FROZEN_STATUS_BANNED = "BANNED";

    public static final String SQS_ENABLED_PROPERTY = "api.aplazo.customer.account.changes.sqs.enabled";
    public static final String SQS_QUEUE_NAME_PROPERTY = "${api.aplazo.customer.account.changes.sqs.queueName}";
}
