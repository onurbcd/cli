package com.onurbcd.cli.persistency.predicate;

import com.onurbcd.cli.dto.filter.BillFilter;
import com.onurbcd.cli.enums.DocumentType;
import com.onurbcd.cli.enums.ReferenceType;
import com.onurbcd.cli.persistency.entity.QBill;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.util.stream.Stream;

import static com.onurbcd.cli.TestConstants.BILL_TYPE_ID;
import static com.onurbcd.cli.TestConstants.ID;
import static org.assertj.core.api.Assertions.assertThat;

class BillPredicateBuilderTest {

    private static final QBill PATH = QBill.bill;

    private static final LocalDate DOCUMENT_DATE = LocalDate.of(2025, 7, 1);

    private static final LocalDate DUE_DATE = LocalDate.of(2025, 7, 10);

    private static final LocalDate PAYMENT_DATE = LocalDate.of(2025, 7, 9);

    private static BillFilter filter() {
        return BillFilter.builder().build();
    }

    @Nested
    @DisplayName("id")
    class Id {

        @Test
        @DisplayName("should filter by the bill id")
        void givenId_whenId_shouldFilterById() {
            assertThat(BillPredicateBuilder.id(ID)).isEqualTo(new BooleanBuilder().and(PATH.id.eq(ID)));
        }
    }

    @Nested
    @DisplayName("all")
    class All {

        static Stream<Arguments> singleCriterion() {
            var year = filter();
            year.setReferenceDayYear((short) 2025);
            var month = filter();
            month.setReferenceDayMonth((short) 7);
            var documentDate = filter();
            documentDate.setDocumentDateCalendarDate(DOCUMENT_DATE);
            var dueDate = filter();
            dueDate.setDueDateCalendarDate(DUE_DATE);
            var paymentDate = filter();
            paymentDate.setPaymentDateCalendarDate(PAYMENT_DATE);
            var billType = filter();
            billType.setBillTypeId(BILL_TYPE_ID);
            var documentType = filter();
            documentType.setDocumentType(DocumentType.INVOICE);
            var referenceType = filter();
            referenceType.setReferenceType(ReferenceType.MONTH);
            var closed = filter();
            closed.setClosed(false);
            var search = filter();
            search.setSearch(" abc ");
            var active = filter();
            active.setActive(false);

            return Stream.of(
                    Arguments.of("empty", filter(), new BooleanBuilder()),
                    Arguments.of("referenceDayYear", year,
                            new BooleanBuilder().and(PATH.referenceDay.calendarYear.eq((short) 2025))),
                    Arguments.of("referenceDayMonth", month,
                            new BooleanBuilder().and(PATH.referenceDay.calendarMonth.eq((short) 7))),
                    Arguments.of("documentDate", documentDate,
                            new BooleanBuilder().and(PATH.documentDate.calendarDate.eq(DOCUMENT_DATE))),
                    Arguments.of("dueDate", dueDate,
                            new BooleanBuilder().and(PATH.dueDate.calendarDate.eq(DUE_DATE))),
                    Arguments.of("paymentDate", paymentDate,
                            new BooleanBuilder().and(PATH.paymentDate.calendarDate.eq(PAYMENT_DATE))),
                    Arguments.of("billTypeId", billType, new BooleanBuilder().and(PATH.billType.id.eq(BILL_TYPE_ID))),
                    Arguments.of("documentType", documentType,
                            new BooleanBuilder().and(PATH.documentType.eq(DocumentType.INVOICE))),
                    Arguments.of("referenceType", referenceType,
                            new BooleanBuilder().and(PATH.referenceType.eq(ReferenceType.MONTH))),
                    Arguments.of("closed", closed, new BooleanBuilder().and(PATH.closed.eq(false))),
                    Arguments.of("search", search, new BooleanBuilder().and(PATH.observation.containsIgnoreCase("abc"))),
                    Arguments.of("active", active, new BooleanBuilder().and(PATH.active.eq(false))));
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("singleCriterion")
        @DisplayName("should apply only the informed criterion")
        void givenSingleCriterion_whenAll_shouldApplyOnlyIt(String name, BillFilter filter, Predicate expected) {
            assertThat(BillPredicateBuilder.all(filter)).isEqualTo(expected);
        }

        @Test
        @DisplayName("with all criteria, should combine them in order")
        void givenFullFilter_whenAll_shouldCombineCriteria() {
            var filter = BillFilter.builder()
                    .referenceDayYear((short) 2025).referenceDayMonth((short) 7)
                    .documentDateCalendarDate(DOCUMENT_DATE).dueDateCalendarDate(DUE_DATE)
                    .paymentDateCalendarDate(PAYMENT_DATE).billTypeId(BILL_TYPE_ID)
                    .documentType(DocumentType.BILL).referenceType(ReferenceType.DAY).closed(true)
                    .search("abc").active(true).build();

            assertThat(BillPredicateBuilder.all(filter)).isEqualTo(new BooleanBuilder()
                    .and(PATH.referenceDay.calendarYear.eq((short) 2025))
                    .and(PATH.referenceDay.calendarMonth.eq((short) 7))
                    .and(PATH.documentDate.calendarDate.eq(DOCUMENT_DATE))
                    .and(PATH.dueDate.calendarDate.eq(DUE_DATE))
                    .and(PATH.paymentDate.calendarDate.eq(PAYMENT_DATE))
                    .and(PATH.billType.id.eq(BILL_TYPE_ID))
                    .and(PATH.documentType.eq(DocumentType.BILL))
                    .and(PATH.referenceType.eq(ReferenceType.DAY))
                    .and(PATH.closed.eq(true))
                    .and(PATH.observation.containsIgnoreCase("abc"))
                    .and(PATH.active.eq(true)));
        }
    }
}
