package mx.aplazo.microservices.wm.account.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;
import mx.aplazo.exception.AplazoException;
import mx.aplazo.microservices.wm.account.feign.AuthHydraClient;
import mx.aplazo.microservices.wm.account.feign.PartnersUnlinkNotifyClient;
import mx.aplazo.microservices.wm.account.feign.dto.HydraRevokeRequest;
import mx.aplazo.microservices.wm.account.helper.AbstractAplazoUnitTest;
import mx.aplazo.microservices.wm.account.model.entity.CustomerWmExtended;
import mx.aplazo.microservices.wm.account.model.entity.CustomerWmStatus;
import mx.aplazo.microservices.wm.account.model.enums.UnlinkErrorCode;
import mx.aplazo.microservices.wm.account.model.enums.UnlinkInitiatedBy;
import mx.aplazo.microservices.wm.account.model.enums.UnlinkReason;
import mx.aplazo.microservices.wm.account.model.enums.UnlinkSource;
import mx.aplazo.microservices.wm.account.model.request.UnlinkAccountRequest;
import mx.aplazo.microservices.wm.account.model.response.UnlinkAccountResponse;
import mx.aplazo.microservices.wm.account.repository.CustomerWmExtendedRepository;
import mx.aplazo.microservices.wm.account.service.UnlinkAtomicWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

class UnlinkServiceImplTest extends AbstractAplazoUnitTest {

    private static final String X_CLIENT_WM = "wm-123";
    private static final Long CUSTOMER_ID = 42L;

    @Mock
    private CustomerWmExtendedRepository customerRepository;
    @Mock
    private AuthHydraClient authHydraClient;
    @Mock
    private UnlinkAtomicWriter atomicWriter;
    @Mock
    private PartnersUnlinkNotifyClient partnersUnlinkNotifyClient;

    @InjectMocks
    private UnlinkServiceImpl service;

    @BeforeEach
    void setUpClientId() {
        ReflectionTestUtils.setField(service, "oauthClientId", "wm-oauth-client");
    }

    private CustomerWmExtended customer(CustomerWmStatus status) {
        return CustomerWmExtended.builder()
                .id(UUID.randomUUID())
                .customerId(CUSTOMER_ID)
                .xClientWm(X_CLIENT_WM)
                .status(status)
                .build();
    }

    private UnlinkAccountRequest request() {
        return UnlinkAccountRequest.builder()
                .xClientWm(X_CLIENT_WM)
                .reason(UnlinkReason.USER_REQUEST)
                .source(UnlinkSource.CASHI)
                .initiatedBy(UnlinkInitiatedBy.PARTNER)
                .build();
    }

    @Test
    @DisplayName("AC-1: happy path revokes before writing, then notifies, and returns SUCCESS")
    void unlink_happyPath_revokesThenWritesThenNotifies() {
        when(customerRepository.findByXClientWm(X_CLIENT_WM))
                .thenReturn(Optional.of(customer(CustomerWmStatus.LINKED)));

        final UnlinkAccountResponse response = service.unlink(request());

        assertEquals("SUCCESS", response.getCode());

        // ADR-012 D-03 ordering: revoke (②) strictly before DB write (③) before notify (④).
        final InOrder inOrder = inOrder(authHydraClient, atomicWriter, partnersUnlinkNotifyClient);
        inOrder.verify(authHydraClient).revoke(any(HydraRevokeRequest.class));
        inOrder.verify(atomicWriter).persistUnlink(any(), any(), any(), any());
        inOrder.verify(partnersUnlinkNotifyClient).notify(any());
    }

    @Test
    @DisplayName("AC-1: Trigger A resolves by customerId and unlinks (USER_REQUEST/USER)")
    void unlinkForCustomer_happyPath() {
        when(customerRepository.findByCustomerId(CUSTOMER_ID))
                .thenReturn(Optional.of(customer(CustomerWmStatus.LINKED_TOKENIZED)));

        final UnlinkAccountResponse response = service.unlinkForCustomer(CUSTOMER_ID);

        assertEquals("SUCCESS", response.getCode());
        verify(atomicWriter).persistUnlink(any(),
                org.mockito.ArgumentMatchers.eq(UnlinkReason.USER_REQUEST.getValue()),
                org.mockito.ArgumentMatchers.eq(UnlinkInitiatedBy.USER.getValue()),
                org.mockito.ArgumentMatchers.isNull());
    }

    @Test
    @DisplayName("AC-2: already UNLINKED is idempotent — no revoke, no write, no notify")
    void unlink_alreadyUnlinked_isIdempotent() {
        when(customerRepository.findByXClientWm(X_CLIENT_WM))
                .thenReturn(Optional.of(customer(CustomerWmStatus.UNLINKED)));

        final UnlinkAccountResponse response = service.unlink(request());

        assertEquals(UnlinkErrorCode.CUSTOMER_ALREADY_UNLINKED.name(), response.getCode());
        verifyNoInteractions(authHydraClient, atomicWriter, partnersUnlinkNotifyClient);
    }

    @Test
    @DisplayName("AC-3a: unknown customer returns 404 CUSTOMER_NOT_FOUND")
    void unlink_customerNotFound_returns404() {
        when(customerRepository.findByXClientWm(X_CLIENT_WM)).thenReturn(Optional.empty());

        final AplazoException ex = assertThrows(AplazoException.class, () -> service.unlink(request()));

        assertEquals(HttpStatus.NOT_FOUND, ex.getHttpStatus());
        assertEquals(UnlinkErrorCode.CUSTOMER_NOT_FOUND, ex.getCode());
        verifyNoInteractions(authHydraClient, atomicWriter, partnersUnlinkNotifyClient);
    }

    @Test
    @DisplayName("AC-3b: ineligible state returns 422 CUSTOMER_NOT_ELIGIBLE_FOR_UNLINK")
    void unlink_ineligibleState_returns422() {
        when(customerRepository.findByXClientWm(X_CLIENT_WM))
                .thenReturn(Optional.of(customer(CustomerWmStatus.REJECTED)));

        final AplazoException ex = assertThrows(AplazoException.class, () -> service.unlink(request()));

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, ex.getHttpStatus());
        assertEquals(UnlinkErrorCode.CUSTOMER_NOT_ELIGIBLE_FOR_UNLINK, ex.getCode());
        verifyNoInteractions(authHydraClient, atomicWriter, partnersUnlinkNotifyClient);
    }

    @Test
    @DisplayName("Security: auth-hydra failure aborts the Unlink with no DB write and no notify")
    void unlink_hydraFails_abortsWithNoStateChange() {
        when(customerRepository.findByXClientWm(X_CLIENT_WM))
                .thenReturn(Optional.of(customer(CustomerWmStatus.LINKED)));
        doThrow(new AplazoException(HttpStatus.INTERNAL_SERVER_ERROR, "boom",
                UnlinkErrorCode.HYDRA_REVOCATION_FAILED))
                .when(authHydraClient).revoke(any(HydraRevokeRequest.class));

        final AplazoException ex = assertThrows(AplazoException.class, () -> service.unlink(request()));

        assertEquals(UnlinkErrorCode.HYDRA_REVOCATION_FAILED, ex.getCode());
        verify(atomicWriter, never()).persistUnlink(any(), any(), any(), any());
        verifyNoInteractions(partnersUnlinkNotifyClient);
    }

    @Test
    @DisplayName("Security: a non-Aplazo failure (e.g. timeout) maps to HYDRA_REVOCATION_FAILED and aborts")
    void unlink_hydraTimeout_mappedToRevocationFailed() {
        when(customerRepository.findByXClientWm(X_CLIENT_WM))
                .thenReturn(Optional.of(customer(CustomerWmStatus.REAUTH_PENDING)));
        doThrow(new RuntimeException("read timed out"))
                .when(authHydraClient).revoke(any(HydraRevokeRequest.class));

        final AplazoException ex = assertThrows(AplazoException.class, () -> service.unlink(request()));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.getHttpStatus());
        assertEquals(UnlinkErrorCode.HYDRA_REVOCATION_FAILED, ex.getCode());
        verify(atomicWriter, never()).persistUnlink(any(), any(), any(), any());
        verifyNoInteractions(partnersUnlinkNotifyClient);
    }

    @Test
    @DisplayName("AC-5: notify failure does not roll back — UNLINKED persists and SUCCESS is returned")
    void unlink_notifyFails_noRollback() {
        when(customerRepository.findByXClientWm(X_CLIENT_WM))
                .thenReturn(Optional.of(customer(CustomerWmStatus.LINKED)));
        doThrow(new RuntimeException("partners-ms down"))
                .when(partnersUnlinkNotifyClient).notify(any());

        final UnlinkAccountResponse response = service.unlink(request());

        assertEquals("SUCCESS", response.getCode());
        verify(atomicWriter).persistUnlink(any(), any(), any(), any());
    }
}
