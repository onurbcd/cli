package com.onurbcd.cli.dto.bill;

import com.onurbcd.cli.dto.PrimeDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.onurbcd.cli.TestConstants.DOCUMENT_ID;
import static com.onurbcd.cli.TestFactory.createBillDto;
import static com.onurbcd.cli.TestFactory.createDocumentDto;
import static org.assertj.core.api.Assertions.assertThat;

class BillDtoTest {

    @Test
    @DisplayName("is a PrimeDto and exposes the fields set through the setters")
    void givenFactoryBill_shouldExposeFields() {
        var bill = createBillDto();
        bill.setObservation("obs");
        bill.setInstallment((short) 2);
        bill.setBillTypeName("type");
        bill.setBudgetName("budget");
        bill.setBalanceName("balance");

        assertThat(bill).isInstanceOf(PrimeDto.class);
        assertThat(bill.getClosed()).isFalse();
        assertThat(bill.getObservation()).isEqualTo("obs");
        assertThat(bill.getInstallment()).isEqualTo((short) 2);
        assertThat(bill.getBillTypeName()).isEqualTo("type");
        assertThat(bill.getBudgetName()).isEqualTo("budget");
        assertThat(bill.getBalanceName()).isEqualTo("balance");
    }

    @Nested
    @DisplayName("getBillDocumentId")
    class GetBillDocumentId {

        @Test
        @DisplayName("without a bill document, should return null")
        void givenNoDocument_shouldReturnNull() {
            assertThat(new BillDto().getBillDocumentId()).isNull();
        }

        @Test
        @DisplayName("with a bill document, should return its id")
        void givenDocument_shouldReturnId() {
            var bill = new BillDto();
            bill.setBillDocument(createDocumentDto());

            assertThat(bill.getBillDocumentId()).isEqualTo(DOCUMENT_ID);
        }
    }

    @Nested
    @DisplayName("getReceiptId")
    class GetReceiptId {

        @Test
        @DisplayName("without a receipt, should return null")
        void givenNoReceipt_shouldReturnNull() {
            assertThat(new BillDto().getReceiptId()).isNull();
        }

        @Test
        @DisplayName("with a receipt, should return its id and not the bill document one")
        void givenReceipt_shouldReturnId() {
            var bill = new BillDto();
            bill.setReceipt(createDocumentDto());

            assertThat(bill.getReceiptId()).isEqualTo(DOCUMENT_ID);
            assertThat(bill.getBillDocumentId()).isNull();
        }
    }
}
