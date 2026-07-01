package mx.aplazo.microservices.wm.account.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import java.util.UUID;
import mx.aplazo.microservices.wm.account.helper.AbstractAplazoUnitTest;
import mx.aplazo.microservices.wm.account.model.entity.CustomerWmExtended;
import mx.aplazo.microservices.wm.account.model.entity.CustomerWmStatus;
import mx.aplazo.microservices.wm.account.model.entity.CustomerWmUnlinkRecord;
import mx.aplazo.microservices.wm.account.repository.CustomerWmExtendedRepository;
import mx.aplazo.microservices.wm.account.repository.CustomerWmUnlinkRecordRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

class UnlinkAtomicWriterTest extends AbstractAplazoUnitTest {

    @Mock
    private CustomerWmUnlinkRecordRepository unlinkRecordRepository;

    @Mock
    private CustomerWmExtendedRepository customerWmExtendedRepository;

    @InjectMocks
    private UnlinkAtomicWriter writer;

    @Test
    @DisplayName("persistUnlink inserts the audit record and clears OAuth fields on the customer")
    void persistUnlink_recordsEventAndUnlinksCustomer() {
        final CustomerWmExtended customer = CustomerWmExtended.builder()
                .id(UUID.randomUUID())
                .customerId(42L)
                .xClientWm("wm-123")
                .status(CustomerWmStatus.LINKED)
                .code("auth-code")
                .oauthState("state")
                .oauthCodeVerifier("verifier")
                .oauthCallbackUrl("https://cb")
                .linkingAccountUrl("https://link")
                .build();

        writer.persistUnlink(customer, "USER_REQUEST", "USER", "detail");

        final ArgumentCaptor<CustomerWmUnlinkRecord> recordCaptor =
                ArgumentCaptor.forClass(CustomerWmUnlinkRecord.class);
        verify(unlinkRecordRepository).save(recordCaptor.capture());
        final CustomerWmUnlinkRecord record = recordCaptor.getValue();
        assertEquals(42L, record.getCustomerId());
        assertEquals("USER_REQUEST", record.getUnlinkReason());
        assertEquals("USER", record.getUnlinkInitiatedBy());
        assertEquals("detail", record.getUnlinkDetail());
        assertNotNull(record.getUnlinkedAt());

        final ArgumentCaptor<CustomerWmExtended> customerCaptor =
                ArgumentCaptor.forClass(CustomerWmExtended.class);
        verify(customerWmExtendedRepository).save(customerCaptor.capture());
        final CustomerWmExtended saved = customerCaptor.getValue();
        assertEquals(CustomerWmStatus.UNLINKED, saved.getStatus());
        assertNull(saved.getCode());
        assertNull(saved.getOauthState());
        assertNull(saved.getOauthCodeVerifier());
        assertNull(saved.getOauthCallbackUrl());
        assertNull(saved.getLinkingAccountUrl());
        assertNotNull(saved.getRefreshTokenExpiresAt());
    }
}
