package com.onurbcd.cli;

import com.onurbcd.cli.dto.balance.BalanceDto;
import com.onurbcd.cli.dto.bill.BillDto;
import com.onurbcd.cli.dto.document.DocumentDto;
import com.onurbcd.cli.enums.BalanceType;
import com.onurbcd.cli.enums.PaymentType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
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
}
