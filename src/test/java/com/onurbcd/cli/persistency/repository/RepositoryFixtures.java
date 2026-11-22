package com.onurbcd.cli.persistency.repository;

import com.onurbcd.cli.enums.BalanceType;
import com.onurbcd.cli.enums.CurrencyType;
import com.onurbcd.cli.enums.DocumentType;
import com.onurbcd.cli.enums.PaymentType;
import com.onurbcd.cli.enums.ReferenceType;
import com.onurbcd.cli.enums.SourceType;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.time.temporal.WeekFields;
import java.util.Locale;
import java.util.UUID;

/**
 * Inserts rows with plain SQL so the repository tests control every column and never depend on the entity id
 * generator. Every method returns the id of the inserted row.
 */
final class RepositoryFixtures {

    static final LocalDateTime CREATED = LocalDateTime.of(2025, 1, 2, 3, 4, 5);

    private final JdbcTemplate jdbc;

    RepositoryFixtures(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    int day(LocalDate date) {
        var id = date.getYear() * 10_000 + date.getMonthValue() * 100 + date.getDayOfMonth();
        var weekFields = WeekFields.ISO;

        jdbc.update("""
                        insert into day (id, calendar_date, calendar_year, calendar_quarter, calendar_month,
                            calendar_month_name, calendar_week_in_year, calendar_week_in_month, calendar_day_in_year,
                            calendar_day_in_week, calendar_day_in_month, calendar_weekday_name)
                        values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""",
                id, date, date.getYear(), (date.getMonthValue() - 1) / 3 + 1, date.getMonthValue(),
                date.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH), date.get(weekFields.weekOfYear()),
                date.get(weekFields.weekOfMonth()), date.getDayOfYear(), date.getDayOfWeek().getValue(),
                date.getDayOfMonth(), date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH));

        return id;
    }

    UUID incomeSource(String name, boolean active) {
        var id = UUID.randomUUID();

        jdbc.update("insert into income_source (id, name, active, created_date, last_modified_date)"
                + " values (?, ?, ?, ?, ?)", id, name, active, ts(), ts());

        return id;
    }

    UUID source(UUID incomeSourceId, String name, SourceType sourceType, BigDecimal balance) {
        var id = UUID.randomUUID();

        jdbc.update("insert into source (id, name, active, created_date, last_modified_date, income_source_id,"
                        + " source_type, currency_type, balance) values (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                id, name, true, ts(), ts(), incomeSourceId, sourceType.name(), CurrencyType.DIGITAL.name(), balance);

        return id;
    }

    UUID category(String name, UUID parentId, int level, boolean lastBranch) {
        var id = UUID.randomUUID();

        jdbc.update("insert into category (id, name, active, created_date, last_modified_date, parent_id, level,"
                        + " last_branch, description) values (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                id, name, true, ts(), ts(), parentId, level, lastBranch, "description of " + name);

        return id;
    }

    UUID billType(String name, UUID categoryId, String path) {
        var id = UUID.randomUUID();

        jdbc.update("insert into bill_type (id, name, active, created_date, last_modified_date, category_id, path)"
                + " values (?, ?, ?, ?, ?, ?, ?)", id, name, true, ts(), ts(), categoryId, path);

        return id;
    }

    UUID budget(UUID billTypeId, String name, int year, int month, int sequence, BigDecimal amount, boolean paid) {
        var id = UUID.randomUUID();

        jdbc.update("insert into budget (id, name, active, created_date, last_modified_date, amount, paid, ref_month,"
                        + " ref_year, sequence, bill_type_id) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                id, name, true, ts(), ts(), amount, paid, month, year, sequence, billTypeId);

        return id;
    }

    UUID document(String name) {
        var id = UUID.randomUUID();

        jdbc.update("insert into document (id, name, path, mime_type, size, hash) values (?, ?, ?, ?, ?, ?)",
                id, name, "/docs/" + name, "application/pdf", 1234L, "hash-" + name);

        return id;
    }

    UUID balance(int dayId, UUID sourceId, UUID categoryId, int sequence, BalanceType type, BigDecimal amount) {
        var id = UUID.randomUUID();

        jdbc.update("insert into balance (id, active, created_date, last_modified_date, sequence, day_id, source_id,"
                        + " category_id, amount, code, description, balance_type, payment_type)"
                        + " values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                id, true, ts(), ts(), sequence, dayId, sourceId, categoryId, amount, "code-" + sequence,
                "description " + sequence, type.name(), PaymentType.CASH.name());

        return id;
    }

    void balanceDocument(UUID balanceId, UUID documentId) {
        jdbc.update("insert into balance_document (balance_id, document_id) values (?, ?)", balanceId, documentId);
    }

    UUID secret(String name, boolean active) {
        var id = UUID.randomUUID();

        jdbc.update("insert into secret (id, name, active, created_date, last_modified_date, description, link,"
                        + " password, username) values (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                id, name, active, ts(), ts(), "description", "https://example.com", "pwd", "user");

        return id;
    }

    /**
     * Inserts a bill with the mandatory columns only; use {@link #withBillExtras} to fill the optional ones.
     */
    UUID bill(UUID budgetId, UUID billTypeId, int referenceDayId, int dueDayId, boolean closed) {
        var id = UUID.randomUUID();

        jdbc.update("insert into bill (id, active, created_date, last_modified_date, reference_day_id, due_date_id,"
                        + " value, bill_type_id, document_type, budget_id, reference_type, closed)"
                        + " values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                id, true, ts(), ts(), referenceDayId, dueDayId, new BigDecimal("150.5000"), billTypeId,
                DocumentType.BILL.name(), budgetId, ReferenceType.MONTH.name(), closed);

        return id;
    }

    void withBillExtras(UUID billId, Integer documentDateId, Integer paymentDateId, UUID billDocumentId,
            UUID receiptId, UUID balanceId) {
        jdbc.update("update bill set document_date_id = ?, payment_date_id = ?, bill_document_id = ?,"
                        + " receipt_id = ?, balance_id = ?, observation = ?, installment = ? where id = ?",
                documentDateId, paymentDateId, billDocumentId, receiptId, balanceId, "observation", 3, billId);
    }

    private static Timestamp ts() {
        return Timestamp.valueOf(CREATED);
    }
}
