package com.onurbcd.cli.dto.filter;

import com.onurbcd.cli.enums.DocumentType;
import com.onurbcd.cli.enums.ReferenceType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static com.onurbcd.cli.TestConstants.BILL_TYPE_ID;
import static org.assertj.core.api.Assertions.assertThat;

class BillFilterTest {

    @Test
    @DisplayName("the builder stores every field")
    void givenBuilder_shouldStoreFields() {
        var filter = BillFilter.builder()
                .active(true)
                .search(" term ")
                .referenceDayYear((short) 2024)
                .referenceDayMonth((short) 3)
                .documentDateCalendarDate(LocalDate.of(2024, 3, 1))
                .dueDateCalendarDate(LocalDate.of(2024, 3, 2))
                .paymentDateCalendarDate(LocalDate.of(2024, 3, 3))
                .billTypeId(BILL_TYPE_ID)
                .documentType(DocumentType.BILLET)
                .referenceType(ReferenceType.MONTH)
                .closed(true)
                .build();

        assertThat(filter).isInstanceOf(AbstractFilterable.class);
        assertThat(filter.isActive()).isTrue();
        assertThat(filter.getSearch()).isEqualTo("term");
        assertThat(filter.getReferenceDayYear()).isEqualTo((short) 2024);
        assertThat(filter.getReferenceDayMonth()).isEqualTo((short) 3);
        assertThat(filter.getDocumentDateCalendarDate()).isEqualTo(LocalDate.of(2024, 3, 1));
        assertThat(filter.getDueDateCalendarDate()).isEqualTo(LocalDate.of(2024, 3, 2));
        assertThat(filter.getPaymentDateCalendarDate()).isEqualTo(LocalDate.of(2024, 3, 3));
        assertThat(filter.getBillTypeId()).isEqualTo(BILL_TYPE_ID);
        assertThat(filter.getDocumentType()).isEqualTo(DocumentType.BILLET);
        assertThat(filter.getReferenceType()).isEqualTo(ReferenceType.MONTH);
        assertThat(filter.getClosed()).isTrue();
    }

    @Test
    @DisplayName("setters replace the fields")
    void givenSetters_shouldReplaceFields() {
        var filter = BillFilter.builder().build();

        filter.setReferenceDayYear((short) 2025);
        filter.setReferenceDayMonth((short) 4);
        filter.setDocumentDateCalendarDate(LocalDate.of(2025, 4, 1));
        filter.setDueDateCalendarDate(LocalDate.of(2025, 4, 2));
        filter.setPaymentDateCalendarDate(LocalDate.of(2025, 4, 3));
        filter.setBillTypeId(BILL_TYPE_ID);
        filter.setDocumentType(DocumentType.BILLET);
        filter.setReferenceType(ReferenceType.DAY);
        filter.setClosed(false);

        assertThat(filter.getReferenceDayYear()).isEqualTo((short) 2025);
        assertThat(filter.getReferenceDayMonth()).isEqualTo((short) 4);
        assertThat(filter.getDocumentDateCalendarDate()).isEqualTo(LocalDate.of(2025, 4, 1));
        assertThat(filter.getDueDateCalendarDate()).isEqualTo(LocalDate.of(2025, 4, 2));
        assertThat(filter.getPaymentDateCalendarDate()).isEqualTo(LocalDate.of(2025, 4, 3));
        assertThat(filter.getBillTypeId()).isEqualTo(BILL_TYPE_ID);
        assertThat(filter.getDocumentType()).isEqualTo(DocumentType.BILLET);
        assertThat(filter.getReferenceType()).isEqualTo(ReferenceType.DAY);
        assertThat(filter.getClosed()).isFalse();
    }
}
