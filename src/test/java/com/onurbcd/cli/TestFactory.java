package com.onurbcd.cli;

import com.onurbcd.cli.dto.balance.BalanceDto;
import com.onurbcd.cli.dto.bill.BillDto;
import com.onurbcd.cli.dto.billtype.BillTypeDto;
import com.onurbcd.cli.dto.budget.BudgetDto;
import com.onurbcd.cli.dto.category.CategoryDto;
import com.onurbcd.cli.dto.document.DocumentDto;
import com.onurbcd.cli.dto.incomesource.IncomeSourceDto;
import com.onurbcd.cli.dto.secret.SecretDto;
import com.onurbcd.cli.dto.source.SourceDto;
import com.onurbcd.cli.enums.BalanceType;
import com.onurbcd.cli.enums.CurrencyType;
import com.onurbcd.cli.enums.DocumentType;
import com.onurbcd.cli.enums.PaymentType;
import com.onurbcd.cli.enums.ReferenceType;
import com.onurbcd.cli.enums.SourceType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.shell.jline.tui.component.flow.SelectItem;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static com.onurbcd.cli.TestConstants.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TestFactory {

    /**
     * Valid, fully populated balance linked to one document.
     */
    public static BalanceDto createBalanceDto() {
        var balance = new BalanceDto();
        balance.setId(ID);
        balance.setName("bogus");
        balance.setActive(false);
        balance.setSequence((short) 3);
        balance.setDayCalendarDate(LocalDate.of(2024, 3, 15));
        balance.setSourceId(SOURCE_ID);
        balance.setSourceName("source");
        balance.setCategoryId(CATEGORY_ID);
        balance.setCategoryName("category");
        balance.setAmount(new BigDecimal("12.50"));
        balance.setCode("CODE");
        balance.setDescription("description");
        balance.setBalanceType(BalanceType.INCOME);
        balance.setPaymentType(PaymentType.CASH);
        balance.setDocuments(Set.of(createDocumentDto()));
        return balance;
    }

    public static DocumentDto createDocumentDto() {
        return new DocumentDto(DOCUMENT_ID, "doc.pdf", "balance/doc.pdf", "application/pdf", 10L, "hash");
    }

    /**
     * Valid bill referencing 2024-03-15, inactive and not closed, with no linked documents.
     */
    public static BillDto createBillDto() {
        var bill = new BillDto();
        bill.setId(ID);
        bill.setName("bogus");
        bill.setActive(false);
        bill.setClosed(false);
        bill.setReferenceDayCalendarDate(LocalDate.of(2024, 3, 15));
        return bill;
    }

    /**
     * Inactive bill type with path "bills/water" linked to the default category.
     */
    public static BillTypeDto createBillTypeDto() {
        var billType = new BillTypeDto();
        billType.setId(ID);
        billType.setName("bogus");
        billType.setActive(false);
        billType.setPath("bills/water");
        billType.setCategoryId(CATEGORY_ID);
        billType.setCategoryName("category");
        return billType;
    }

    /**
     * Inactive, unpaid budget of 100.50 for 2024-03 at sequence 4, linked to the default bill type.
     */
    public static BudgetDto createBudgetDto() {
        var budget = new BudgetDto();
        budget.setId(ID);
        budget.setName("bogus");
        budget.setActive(false);
        budget.setSequence((short) 4);
        budget.setRefYear((short) 2024);
        budget.setRefMonth((short) 3);
        budget.setBillTypeId(BILL_TYPE_ID);
        budget.setBillTypeName("bill type");
        budget.setAmount(new BigDecimal("100.50"));
        budget.setPaid(false);
        return budget;
    }

    /**
     * Inactive category with description "description", linked to parent {@link TestConstants#PARENT_ID}.
     */
    public static CategoryDto createCategoryDto() {
        var category = new CategoryDto();
        category.setId(ID);
        category.setName("bogus");
        category.setActive(false);
        category.setParentId(PARENT_ID);
        category.setParentName("parent");
        category.setDescription("description");
        return category;
    }

    /**
     * Inactive income source named "bogus".
     */
    public static IncomeSourceDto createIncomeSourceDto() {
        var incomeSource = new IncomeSourceDto();
        incomeSource.setId(ID);
        incomeSource.setName("bogus");
        incomeSource.setActive(false);
        return incomeSource;
    }

    /**
     * Inactive secret named "bogus" with description, link, username and password.
     */
    public static SecretDto createSecretDto() {
        var secret = new SecretDto();
        secret.setId(ID);
        secret.setName("bogus");
        secret.setActive(false);
        secret.setDescription("description");
        secret.setLink("https://example.com");
        secret.setUsername("user");
        secret.setPassword("password");
        return secret;
    }

    /**
     * Inactive usable digital source named "bogus", linked to income source {@code PARENT_ID}, balance 100.50.
     */
    public static SourceDto createSourceDto() {
        var source = new SourceDto();
        source.setId(ID);
        source.setName("bogus");
        source.setActive(false);
        source.setIncomeSourceId(PARENT_ID);
        source.setIncomeSourceName("income");
        source.setSourceType(SourceType.USABLE);
        source.setCurrencyType(CurrencyType.DIGITAL);
        source.setBalance(new BigDecimal("100.50"));
        return source;
    }

    /**
     * Bill with every date, document, enum and reference id filled in (receipt and bill document use distinct ids).
     */
    public static BillDto createFullBillDto() {
        var bill = createBillDto();
        bill.setDocumentDateCalendarDate(LocalDate.of(2024, 3, 10));
        bill.setDueDateCalendarDate(LocalDate.of(2024, 3, 20));
        bill.setPaymentDateCalendarDate(LocalDate.of(2024, 3, 18));
        bill.setBillDocument(createDocumentDto());
        bill.setReceipt(new DocumentDto(RECEIPT_ID, "receipt.pdf", "bill/receipt.pdf", "application/pdf", 5L, "h2"));
        bill.setObservation("observation");
        bill.setInstallment((short) 2);
        bill.setDocumentType(DocumentType.BILLET);
        bill.setBudgetId(PARENT_ID);
        bill.setReferenceType(ReferenceType.values()[0]);
        bill.setPaymentType(PaymentType.PIX);
        bill.setSourceId(SOURCE_ID);
        return bill;
    }

    /**
     * Two select items whose names and values start with the given prefix.
     */
    public static List<SelectItem> createSelectItems(String prefix) {
        return List.of(SelectItem.of(prefix + "-1", prefix + "-id-1"), SelectItem.of(prefix + "-2", prefix + "-id-2"));
    }
}
