package com.onurbcd.cli.persistency.entity;

import com.onurbcd.cli.enums.DocumentType;
import com.onurbcd.cli.enums.ReferenceType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static com.onurbcd.cli.TestConstants.BILL_TYPE_ID;
import static com.onurbcd.cli.TestConstants.DOCUMENT_ID;
import static com.onurbcd.cli.TestConstants.ID;
import static com.onurbcd.cli.TestConstants.RECEIPT_ID;
import static org.assertj.core.api.Assertions.assertThat;

class BillTest {

    private static final UUID OTHER_ID = UUID.fromString("99999999-9999-9999-0000-000000000001");
    private static final BigDecimal VALUE = new BigDecimal("120.5000");

    private static Day day(int id) {
        return new Day(id);
    }

    private static Document document(UUID id) {
        var document = new Document();
        document.setId(id);
        return document;
    }

    @Test
    @DisplayName("the builder fills every field")
    void givenBuilder_whenBuild_shouldFillEveryField() {
        var referenceDay = day(20240301);
        var documentDate = day(20240305);
        var dueDate = day(20240310);
        var paymentDate = day(20240309);
        var billDocument = document(DOCUMENT_ID);
        var receipt = document(RECEIPT_ID);
        var billType = BillType.builder().id(BILL_TYPE_ID).build();
        var budget = Budget.builder().id(ID).build();
        var balance = Balance.builder().id(OTHER_ID).build();

        var bill = Bill.builder()
                .id(ID)
                .name("light")
                .active(true)
                .referenceDay(referenceDay)
                .documentDate(documentDate)
                .dueDate(dueDate)
                .value(VALUE)
                .paymentDate(paymentDate)
                .billDocument(billDocument)
                .receipt(receipt)
                .observation("note")
                .installment((short) 2)
                .billType(billType)
                .documentType(DocumentType.BILLET)
                .budget(budget)
                .referenceType(ReferenceType.MONTH)
                .closed(true)
                .balance(balance)
                .build();

        assertThat(bill.getId()).isEqualTo(ID);
        assertThat(bill.getName()).isEqualTo("light");
        assertThat(bill.getActive()).isTrue();
        assertThat(bill.getReferenceDay()).isSameAs(referenceDay);
        assertThat(bill.getDocumentDate()).isSameAs(documentDate);
        assertThat(bill.getDueDate()).isSameAs(dueDate);
        assertThat(bill.getValue()).isEqualTo(VALUE);
        assertThat(bill.getPaymentDate()).isSameAs(paymentDate);
        assertThat(bill.getBillDocument()).isSameAs(billDocument);
        assertThat(bill.getReceipt()).isSameAs(receipt);
        assertThat(bill.getObservation()).isEqualTo("note");
        assertThat(bill.getInstallment()).isEqualTo((short) 2);
        assertThat(bill.getBillType()).isSameAs(billType);
        assertThat(bill.getDocumentType()).isEqualTo(DocumentType.BILLET);
        assertThat(bill.getBudget()).isSameAs(budget);
        assertThat(bill.getReferenceType()).isEqualTo(ReferenceType.MONTH);
        assertThat(bill.getClosed()).isTrue();
        assertThat(bill.getBalance()).isSameAs(balance);
    }

    @Test
    @DisplayName("the all-args constructor keeps every field in order")
    void givenAllArgs_whenConstruct_shouldKeepEveryField() {
        var referenceDay = day(20240301);
        var documentDate = day(20240305);
        var dueDate = day(20240310);
        var paymentDate = day(20240309);
        var billDocument = document(DOCUMENT_ID);
        var receipt = document(RECEIPT_ID);
        var billType = BillType.builder().id(BILL_TYPE_ID).build();
        var budget = Budget.builder().id(ID).build();
        var balance = Balance.builder().id(OTHER_ID).build();

        var bill = new Bill(referenceDay, documentDate, dueDate, VALUE, paymentDate, billDocument, receipt, "obs",
                (short) 3, billType, DocumentType.INVOICE, budget, ReferenceType.YEAR, false, balance);

        assertThat(bill.getReferenceDay()).isSameAs(referenceDay);
        assertThat(bill.getDocumentDate()).isSameAs(documentDate);
        assertThat(bill.getDueDate()).isSameAs(dueDate);
        assertThat(bill.getValue()).isEqualTo(VALUE);
        assertThat(bill.getPaymentDate()).isSameAs(paymentDate);
        assertThat(bill.getBillDocument()).isSameAs(billDocument);
        assertThat(bill.getReceipt()).isSameAs(receipt);
        assertThat(bill.getObservation()).isEqualTo("obs");
        assertThat(bill.getInstallment()).isEqualTo((short) 3);
        assertThat(bill.getBillType()).isSameAs(billType);
        assertThat(bill.getDocumentType()).isEqualTo(DocumentType.INVOICE);
        assertThat(bill.getBudget()).isSameAs(budget);
        assertThat(bill.getReferenceType()).isEqualTo(ReferenceType.YEAR);
        assertThat(bill.getClosed()).isFalse();
        assertThat(bill.getBalance()).isSameAs(balance);
    }

    @Test
    @DisplayName("setters store each field")
    void givenValues_whenSet_shouldReturnThemFromGetters() {
        var referenceDay = day(20240301);
        var documentDate = day(20240305);
        var dueDate = day(20240310);
        var paymentDate = day(20240309);
        var billDocument = document(DOCUMENT_ID);
        var receipt = document(RECEIPT_ID);
        var billType = BillType.builder().id(BILL_TYPE_ID).build();
        var budget = Budget.builder().id(ID).build();
        var balance = Balance.builder().id(OTHER_ID).build();
        var bill = new Bill();

        bill.setReferenceDay(referenceDay);
        bill.setDocumentDate(documentDate);
        bill.setDueDate(dueDate);
        bill.setValue(VALUE);
        bill.setPaymentDate(paymentDate);
        bill.setBillDocument(billDocument);
        bill.setReceipt(receipt);
        bill.setObservation("text");
        bill.setInstallment((short) 4);
        bill.setBillType(billType);
        bill.setDocumentType(DocumentType.BILLET);
        bill.setBudget(budget);
        bill.setReferenceType(ReferenceType.MONTH);
        bill.setClosed(true);
        bill.setBalance(balance);

        assertThat(bill.getReferenceDay()).isSameAs(referenceDay);
        assertThat(bill.getDocumentDate()).isSameAs(documentDate);
        assertThat(bill.getDueDate()).isSameAs(dueDate);
        assertThat(bill.getValue()).isEqualTo(VALUE);
        assertThat(bill.getPaymentDate()).isSameAs(paymentDate);
        assertThat(bill.getBillDocument()).isSameAs(billDocument);
        assertThat(bill.getReceipt()).isSameAs(receipt);
        assertThat(bill.getObservation()).isEqualTo("text");
        assertThat(bill.getInstallment()).isEqualTo((short) 4);
        assertThat(bill.getBillType()).isSameAs(billType);
        assertThat(bill.getDocumentType()).isEqualTo(DocumentType.BILLET);
        assertThat(bill.getBudget()).isSameAs(budget);
        assertThat(bill.getReferenceType()).isEqualTo(ReferenceType.MONTH);
        assertThat(bill.getClosed()).isTrue();
        assertThat(bill.getBalance()).isSameAs(balance);
    }

    @Test
    @DisplayName("equality follows the id")
    void givenIds_whenEqualsAndHashCode_shouldFollowId() {
        var bill = Bill.builder().id(ID).build();

        assertThat(bill).isEqualTo(Bill.builder().id(ID).build()).hasSameHashCodeAs(Bill.builder().id(ID).build());
        assertThat(bill).isNotEqualTo(Bill.builder().id(OTHER_ID).build())
                .doesNotHaveSameHashCodeAs(Bill.builder().id(OTHER_ID).build());
    }
}
