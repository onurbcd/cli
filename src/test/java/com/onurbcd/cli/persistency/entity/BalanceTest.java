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

        assertThat(balance)
                .returns(ID, Balance::getId)
                .returns("sale", Balance::getName)
                .returns(true, Balance::getActive)
                .returns((short) 3, Balance::getSequence)
                .satisfies(a -> assertThat(a.getDay()).isSameAs(day))
                .satisfies(a -> assertThat(a.getSource()).isSameAs(source))
                .satisfies(a -> assertThat(a.getCategory()).isSameAs(category))
                .returns(AMOUNT, Balance::getAmount)
                .returns("code-1", Balance::getCode)
                .returns("description", Balance::getDescription)
                .returns(BalanceType.INCOME, Balance::getBalanceType)
                .returns(PaymentType.PIX, Balance::getPaymentType)
                .satisfies(a -> assertThat(a.getDocuments()).isEmpty());
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

        assertThat(balance)
                .returns((short) 4, Balance::getSequence)
                .satisfies(a -> assertThat(a.getDay()).isSameAs(day))
                .satisfies(a -> assertThat(a.getSource()).isSameAs(source))
                .satisfies(a -> assertThat(a.getCategory()).isSameAs(category))
                .returns(AMOUNT, Balance::getAmount)
                .returns("code-2", Balance::getCode)
                .returns("text", Balance::getDescription)
                .returns(BalanceType.OUTCOME, Balance::getBalanceType)
                .returns(PaymentType.PIX, Balance::getPaymentType)
                .satisfies(a -> assertThat(a.getDocuments()).isSameAs(documents));
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

        assertThat(balance)
                .returns((short) 9, Balance::getSequence)
                .satisfies(a -> assertThat(a.getDay()).isSameAs(day))
                .satisfies(a -> assertThat(a.getSource()).isSameAs(source))
                .satisfies(a -> assertThat(a.getCategory()).isSameAs(category))
                .returns(AMOUNT, Balance::getAmount)
                .returns("code-3", Balance::getCode)
                .returns("desc", Balance::getDescription)
                .returns(BalanceType.OUTCOME, Balance::getBalanceType)
                .returns(PaymentType.PIX, Balance::getPaymentType)
                .satisfies(a -> assertThat(a.getDocuments()).isSameAs(documents));
    }

    @Test
    @DisplayName("the day and sequence accessors are derived from the day and the sequence")
    void givenDay_whenDerivedAccessors_shouldExposeDayAndSequence() {
        var balance = Balance.builder().day(day()).sequence((short) 5).build();

        assertThat(balance)
                .returns((short) 15, Balance::getDayInMonth)
                .returns((short) 2024, Balance::getSequenceYear)
                .returns((short) 3, Balance::getSequenceMonth)
                .returns((short) 5, Balance::getSequenceValue);
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
