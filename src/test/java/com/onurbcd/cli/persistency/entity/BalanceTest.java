package com.onurbcd.cli.persistency.entity;

import com.onurbcd.cli.enums.BalanceType;
import com.onurbcd.cli.enums.PaymentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static com.onurbcd.cli.TestConstants.CATEGORY_ID;
import static com.onurbcd.cli.TestConstants.DOCUMENT_ID;
import static com.onurbcd.cli.TestConstants.ID;
import static com.onurbcd.cli.TestConstants.SOURCE_ID;
import static org.assertj.core.api.Assertions.assertThat;

class BalanceTest {

    private static final UUID OTHER_ID = UUID.fromString("99999999-9999-9999-0000-000000000001");
    private static final BigDecimal AMOUNT = new BigDecimal("50.0000");

    private static Day day() {
        var day = new Day(20240315);
        day.setCalendarDayInMonth((short) 15);
        day.setCalendarMonth((short) 3);
        day.setCalendarYear((short) 2024);
        return day;
    }

    private static Document document() {
        var document = new Document();
        document.setId(DOCUMENT_ID);
        return document;
    }

    @Test
    @DisplayName("the builder fills every field and defaults the documents to an empty set")
    void givenBuilder_whenBuild_shouldFillEveryField() {
        var day = day();
        var source = Source.builder().id(SOURCE_ID).build();
        var category = new Category(CATEGORY_ID);

        var balance = Balance.builder()
                .id(ID)
                .name("sale")
                .active(true)
                .sequence((short) 3)
                .day(day)
                .source(source)
                .category(category)
                .amount(AMOUNT)
                .code("code-1")
                .description("description")
                .balanceType(BalanceType.INCOME)
                .paymentType(PaymentType.PIX)
                .build();

        assertThat(balance.getId()).isEqualTo(ID);
        assertThat(balance.getName()).isEqualTo("sale");
        assertThat(balance.getActive()).isTrue();
        assertThat(balance.getSequence()).isEqualTo((short) 3);
        assertThat(balance.getDay()).isSameAs(day);
        assertThat(balance.getSource()).isSameAs(source);
        assertThat(balance.getCategory()).isSameAs(category);
        assertThat(balance.getAmount()).isEqualTo(AMOUNT);
        assertThat(balance.getCode()).isEqualTo("code-1");
        assertThat(balance.getDescription()).isEqualTo("description");
        assertThat(balance.getBalanceType()).isEqualTo(BalanceType.INCOME);
        assertThat(balance.getPaymentType()).isEqualTo(PaymentType.PIX);
        assertThat(balance.getDocuments()).isEmpty();
    }

    @Test
    @DisplayName("the builder keeps the documents it is given")
    void givenDocuments_whenBuild_shouldKeepThem() {
        var document = document();

        var balance = Balance.builder().documents(Set.of(document)).build();

        assertThat(balance.getDocuments()).containsExactly(document);
    }

    @Test
    @DisplayName("the all-args constructor keeps every field in order")
    void givenAllArgs_whenConstruct_shouldKeepEveryField() {
        var day = day();
        var source = Source.builder().id(SOURCE_ID).build();
        var category = new Category(CATEGORY_ID);
        var documents = new HashSet<Document>();

        var balance = new Balance((short) 4, day, source, category, AMOUNT, "code-2", "text", BalanceType.OUTCOME,
                PaymentType.PIX, documents);

        assertThat(balance.getSequence()).isEqualTo((short) 4);
        assertThat(balance.getDay()).isSameAs(day);
        assertThat(balance.getSource()).isSameAs(source);
        assertThat(balance.getCategory()).isSameAs(category);
        assertThat(balance.getAmount()).isEqualTo(AMOUNT);
        assertThat(balance.getCode()).isEqualTo("code-2");
        assertThat(balance.getDescription()).isEqualTo("text");
        assertThat(balance.getBalanceType()).isEqualTo(BalanceType.OUTCOME);
        assertThat(balance.getPaymentType()).isEqualTo(PaymentType.PIX);
        assertThat(balance.getDocuments()).isSameAs(documents);
    }

    @Test
    @DisplayName("setters store each field")
    void givenValues_whenSet_shouldReturnThemFromGetters() {
        var day = day();
        var source = Source.builder().id(SOURCE_ID).build();
        var category = new Category(CATEGORY_ID);
        var documents = Set.of(document());
        var balance = new Balance();

        balance.setSequence((short) 9);
        balance.setDay(day);
        balance.setSource(source);
        balance.setCategory(category);
        balance.setAmount(AMOUNT);
        balance.setCode("code-3");
        balance.setDescription("desc");
        balance.setBalanceType(BalanceType.OUTCOME);
        balance.setPaymentType(PaymentType.PIX);
        balance.setDocuments(documents);

        assertThat(balance.getSequence()).isEqualTo((short) 9);
        assertThat(balance.getDay()).isSameAs(day);
        assertThat(balance.getSource()).isSameAs(source);
        assertThat(balance.getCategory()).isSameAs(category);
        assertThat(balance.getAmount()).isEqualTo(AMOUNT);
        assertThat(balance.getCode()).isEqualTo("code-3");
        assertThat(balance.getDescription()).isEqualTo("desc");
        assertThat(balance.getBalanceType()).isEqualTo(BalanceType.OUTCOME);
        assertThat(balance.getPaymentType()).isEqualTo(PaymentType.PIX);
        assertThat(balance.getDocuments()).isSameAs(documents);
    }

    @Test
    @DisplayName("the day and sequence accessors are derived from the day and the sequence")
    void givenDay_whenDerivedAccessors_shouldExposeDayAndSequence() {
        var balance = Balance.builder().day(day()).sequence((short) 5).build();

        assertThat(balance.getDayInMonth()).isEqualTo((short) 15);
        assertThat(balance.getSequenceYear()).isEqualTo((short) 2024);
        assertThat(balance.getSequenceMonth()).isEqualTo((short) 3);
        assertThat(balance.getSequenceValue()).isEqualTo((short) 5);
    }

    @Test
    @DisplayName("equality follows the id")
    void givenIds_whenEqualsAndHashCode_shouldFollowId() {
        var balance = Balance.builder().id(ID).build();

        assertThat(balance).isEqualTo(Balance.builder().id(ID).build()).hasSameHashCodeAs(Balance.builder().id(ID).build());
        assertThat(balance).isNotEqualTo(Balance.builder().id(OTHER_ID).build())
                .doesNotHaveSameHashCodeAs(Balance.builder().id(OTHER_ID).build());
    }
}
