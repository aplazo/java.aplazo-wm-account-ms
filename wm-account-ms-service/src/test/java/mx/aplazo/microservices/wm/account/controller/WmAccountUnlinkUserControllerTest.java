package mx.aplazo.microservices.wm.account.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import mx.aplazo.exception.AplazoException;
import mx.aplazo.microservices.wm.account.helper.AbstractAplazoUnitTest;
import mx.aplazo.microservices.wm.account.model.response.UnlinkAccountResponse;
import mx.aplazo.microservices.wm.account.service.UnlinkService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

class WmAccountUnlinkUserControllerTest extends AbstractAplazoUnitTest {

    @Mock
    private UnlinkService unlinkService;

    @InjectMocks
    private WmAccountUnlinkUserController controller;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateAs(String subject) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(subject, null));
    }

    @Test
    @DisplayName("C-03 resolves customerId from the token and delegates to unlinkForCustomer")
    void unlink_resolvesCustomerFromToken() {
        authenticateAs("42");
        final UnlinkAccountResponse expected = UnlinkAccountResponse.builder().code("SUCCESS").build();
        when(unlinkService.unlinkForCustomer(42L)).thenReturn(expected);

        final UnlinkAccountResponse actual = controller.unlink();

        assertSame(expected, actual);
        verify(unlinkService).unlinkForCustomer(42L);
    }

    @Test
    @DisplayName("C-03 rejects a non-numeric token subject with 400 INVALID_REQUEST")
    void unlink_invalidSubject_returns400() {
        authenticateAs("not-a-number");

        final AplazoException ex = assertThrows(AplazoException.class, () -> controller.unlink());

        assertEquals(HttpStatus.BAD_REQUEST, ex.getHttpStatus());
        verifyNoInteractions(unlinkService);
    }

    @Test
    @DisplayName("C-03 rejects a missing authentication with 401")
    void unlink_noAuthentication_returns401() {
        SecurityContextHolder.clearContext();

        final AplazoException ex = assertThrows(AplazoException.class, () -> controller.unlink());

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getHttpStatus());
        verifyNoInteractions(unlinkService);
    }
}
