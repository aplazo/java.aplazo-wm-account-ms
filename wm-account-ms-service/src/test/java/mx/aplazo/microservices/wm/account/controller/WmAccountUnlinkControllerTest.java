package mx.aplazo.microservices.wm.account.controller;

import static org.junit.jupiter.api.Assertions.assertThrows;

import mx.aplazo.microservices.wm.account.helper.AbstractAplazoUnitTest;
import mx.aplazo.microservices.wm.account.model.request.UnlinkAccountRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class WmAccountUnlinkControllerTest extends AbstractAplazoUnitTest {

    private final WmAccountUnlinkController controller = new WmAccountUnlinkController();

    @Test
    @DisplayName("unlink is a skeleton (BNPL-960) and throws until BNPL-886 wires the core")
    void unlink_throwsUntilCoreImplemented() {
        final UnlinkAccountRequest request = podamFactory.manufacturePojo(UnlinkAccountRequest.class);

        assertThrows(UnsupportedOperationException.class, () -> controller.unlink(request));
    }
}
