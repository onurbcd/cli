package com.onurbcd.cli.service;

import com.onurbcd.cli.dto.balance.BalanceSaveDto;
import com.onurbcd.cli.dto.bill.BillCloseDto;
import com.onurbcd.cli.enums.BalanceType;
import com.onurbcd.cli.enums.DocumentType;
import com.onurbcd.cli.enums.PaymentType;
import com.onurbcd.cli.enums.ReferenceType;
import com.onurbcd.cli.param.BillBalanceParams;
import com.onurbcd.cli.persistency.entity.Balance;
import com.onurbcd.cli.persistency.entity.Bill;
import com.onurbcd.cli.persistency.entity.Budget;
import com.onurbcd.cli.persistency.entity.Day;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static com.onurbcd.cli.TestConstants.CATEGORY_ID;
import static com.onurbcd.cli.TestConstants.SOURCE_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BillBalanceServiceTest {

    private static final LocalDate PAYMENT_DATE = LocalDate.of(2024, 3, 18);
    private static final LocalDate REFERENCE_DATE = LocalDate.of(2024, 3, 1);

    @Mock
    private BalanceService balanceService;
    @Captor
    private ArgumentCaptor<BalanceSaveDto> dtoCaptor;
    @InjectMocks
    private BillBalanceService service;

    @ParameterizedTest(name = "[{index}] reference {0} -> {1}")
    @CsvSource({"YEAR, 2024", "MONTH, 03/2024", "DAY, 03/2024"})
    void givenBillClose_whenSaveBalance_shouldSaveOutcomeBalanceDescribingThePayment(ReferenceType referenceType,
                                                                                    String reference) {
        var referenceDay = new Day();
        referenceDay.setCalendarDate(REFERENCE_DATE);
        var budget = Budget.builder().name("Water").build();
        var bill = Bill.builder().value(new BigDecimal("55.90")).documentType(DocumentType.BILLET).budget(budget)
                .referenceType(referenceType).referenceDay(referenceDay).build();
        var closeDto = BillCloseDto.builder().paymentDateCalendarDate(PAYMENT_DATE).sourceId(SOURCE_ID)
                .paymentType(PaymentType.PIX).build();
        var params = new BillBalanceParams(closeDto, bill, CATEGORY_ID);
        var saved = Balance.builder().build();
        when(balanceService.save(any(BalanceSaveDto.class))).thenReturn(saved);

        var result = service.saveBalance(params);

        assertThat(result).isSameAs(saved);
        verify(balanceService).save(dtoCaptor.capture());
        var expected = BalanceSaveDto.builder()
                .name("bogus")
                .active(true)
                .sequence(Short.MAX_VALUE)
                .dayCalendarDate(PAYMENT_DATE)
                .sourceId(SOURCE_ID)
                .categoryId(CATEGORY_ID)
                .amount(new BigDecimal("55.90"))
                .code("created by bill close")
                .description("Pagamento efetuado por Pix: boleto Water referente a " + reference)
                .balanceType(BalanceType.OUTCOME)
                .paymentType(PaymentType.PIX)
                .documentsIds(null)
                .build();
        assertThat(dtoCaptor.getValue()).usingRecursiveComparison().isEqualTo(expected);
        verifyNoMoreInteractions(balanceService);
    }
}
