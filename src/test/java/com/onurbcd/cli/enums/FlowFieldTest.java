package com.onurbcd.cli.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

class FlowFieldTest {

    @Test
    @DisplayName("declares exactly these constants, in this order, each with its id and label")
    void givenAllConstants_whenRead_shouldExposeIdAndLabel() {
        var fields = new LinkedHashMap<String, List<String>>();
        Arrays.stream(FlowField.values())
                .forEach(field -> fields.put(field.name(), List.of(field.getId(), field.getName())));

        assertThat(fields).containsExactly(
                entry("NAME", List.of("name", "* Name:")),
                entry("DESCRIPTION", List.of("description", "Description:")),
                entry("PARENT_ID", List.of("parentId", "* Parent:")),
                entry("PATH", List.of("path", "* Path:")),
                entry("CATEGORY_ID", List.of("categoryId", "* Category:")),
                entry("INCOME_SOURCE_ID", List.of("incomeSourceId", "* Income Source:")),
                entry("SOURCE_TYPE", List.of("sourceType", "* Source Type:")),
                entry("CURRENCY_TYPE", List.of("currencyType", "* Currency Type:")),
                entry("BALANCE", List.of("balance", "* Balance:")),
                entry("DAY", List.of("day", "* Day (yyyy-MM-dd):")),
                entry("SOURCE", List.of("source", "* Source:")),
                entry("CATEGORY", List.of("category", "* Category:")),
                entry("AMOUNT", List.of("amount", "* Amount:")),
                entry("CODE", List.of("code", "* Code:")),
                entry("BALANCE_TYPE", List.of("balanceType", "* Balance Type:")),
                entry("DOCUMENTS", List.of("documents", "Documents:")),
                entry("DOCUMENTS_IDS", List.of("documentsIds", "Linked Documents:")),
                entry("LINK", List.of("link", "Link:")),
                entry("USERNAME", List.of("username", "* Username:")),
                entry("PASSWORD", List.of("password", "* Password:")),
                entry("REF_YEAR", List.of("refYear", "* Reference Year:")),
                entry("REF_MONTH", List.of("refMonth", "* Reference Month:")),
                entry("BILL_TYPE_ID", List.of("billTypeId", "* Bill Type:")),
                entry("PAID", List.of("paid", "* Paid:")),
                entry("PAYMENT_TYPE", List.of("paymentType", "* Payment Type:")),
                entry("REFERENCE_DAY", List.of("referenceDay", "* Reference Day (yyyy-MM-dd):")),
                entry("DOCUMENT_DATE", List.of("documentDate", "Document Date (yyyy-MM-dd):")),
                entry("DUE_DATE", List.of("dueDate", "* Due Date (yyyy-MM-dd):")),
                entry("OBSERVATION", List.of("observation", "Observation:")),
                entry("INSTALLMENT", List.of("installment", "Installment:")),
                entry("DOCUMENT_TYPE", List.of("documentType", "* Document Type:")),
                entry("BUDGET", List.of("budget", "* Budget:")),
                entry("REFERENCE_TYPE", List.of("referenceType", "* Reference Type:")),
                entry("DOCUMENT", List.of("document", "Document:")),
                entry("PAYMENT_DATE", List.of("paymentDate", "* Payment Date (yyyy-MM-dd):")),
                entry("SOURCE_ID", List.of("sourceId", "* Source:")),
                entry("RECEIPT", List.of("receipt", "Receipt:")),
                entry("BILL_ID", List.of("billId", "* Bill:")),
                entry("LINKED_DOCUMENT", List.of("documentId", "Linked Document:")),
                entry("LINKED_RECEIPT", List.of("receiptId", "Linked Receipt:")));
    }
}
