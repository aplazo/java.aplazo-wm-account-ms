package mx.aplazo.microservices.wm.account.controller;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import mx.aplazo.microservices.wm.account.helper.AbstractAplazoUnitTest;
import mx.aplazo.microservices.wm.account.model.request.UnlinkAccountRequest;
import mx.aplazo.microservices.wm.account.model.response.UnlinkAccountResponse;
import mx.aplazo.microservices.wm.account.service.UnlinkService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

class WmAccountUnlinkControllerTest extends AbstractAplazoUnitTest {

    @Mock
    private UnlinkService unlinkService;

    @InjectMocks
    private WmAccountUnlinkController controller;

    @Test
    @DisplayName("C-02 delegates the request to UnlinkService and returns its response")
    void unlink_delegatesToService() {
        final UnlinkAccountRequest request = podamFactory.manufacturePojo(UnlinkAccountRequest.class);
        final UnlinkAccountResponse expected = UnlinkAccountResponse.builder().code("SUCCESS").build();
        when(unlinkService.unlink(request)).thenReturn(expected);

        final UnlinkAccountResponse actual = controller.unlink(request);

        assertSame(expected, actual);
        verify(unlinkService).unlink(request);
    }
}
