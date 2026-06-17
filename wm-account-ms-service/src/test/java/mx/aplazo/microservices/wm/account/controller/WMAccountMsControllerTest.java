package mx.aplazo.microservices.wm.account.controller;

import mx.aplazo.microservices.wm.account.helper.AbstractAplazoUnitTest;
import mx.aplazo.microservices.wm.account.model.request.WMAccountMsRequest;
import mx.aplazo.microservices.wm.account.model.response.WMAccountMsResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class WMAccountMsControllerTest extends AbstractAplazoUnitTest {

    private final WMAccountMsController controller = new WMAccountMsController();

    @Test
    @DisplayName("operation1 returns the expected greeting")
    void operation1_returnsGreeting() {
        final String result = controller.operation1();

        assertEquals("Hola", result);
    }

    @Test
    @DisplayName("operation2 returns null for the given request")
    void operation2_returnsNull() {
        final WMAccountMsRequest request = podamFactory.manufacturePojo(WMAccountMsRequest.class);

        final WMAccountMsResponse response = controller.operation2(request);

        assertNull(response);
    }
}
