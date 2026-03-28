package com.onurbcd.cli.service;

import com.onurbcd.cli.util.Constant;
import com.onurbcd.cli.dto.balance.BalanceSaveDto;
import com.onurbcd.cli.enums.BalanceType;
import com.onurbcd.cli.enums.ReferenceType;
import com.onurbcd.cli.param.BillBalanceParams;
import com.onurbcd.cli.persistency.entity.Balance;
import com.onurbcd.cli.persistency.entity.Bill;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class BillBalanceService {

    private final BalanceService balanceService;

    public Balance saveBalance(BillBalanceParams params) {
        var balanceSaveDto = createBalanceSaveDto(params);
        return balanceService.save(balanceSaveDto);
    }

    private BalanceSaveDto createBalanceSaveDto(BillBalanceParams params) {
        return BalanceSaveDto
                .builder()
                .name(Constant.BOGUS_NAME)
                .active(Boolean.TRUE)
                .sequence(Short.MAX_VALUE)
                .dayCalendarDate(params.getBillCloseDto().getPaymentDateCalendarDate())
                .sourceId(params.getBillCloseDto().getSourceId())
                .categoryId(params.getCategoryId())
                .amount(params.getBill().getValue())
                .code(Constant.BILL_CLOSE_CODE)
                .description(createDescription(params))
                .balanceType(BalanceType.OUTCOME)
                .paymentType(params.getBillCloseDto().getPaymentType())
                .documentsIds(null)
                .build();
    }

    private String createDescription(BillBalanceParams params) {
        return "Pagamento efetuado por " +
                params.getBillCloseDto().getPaymentType().getCode() +
                ": " +
                params.getBill().getDocumentType().getCode() +
                StringUtils.SPACE +
                params.getBill().getBudget().getName() +
                StringUtils.SPACE +
                "referente a " +
                createReference(params.getBill());
    }

    private String createReference(Bill bill) {
        var pattern = ReferenceType.YEAR == bill.getReferenceType() ?
                Constant.YEAR_PATTERN :
                Constant.MONTH_YEAR_PATTERN;

        return bill.getReferenceDay().getCalendarDate().format(DateTimeFormatter.ofPattern(pattern));
    }
}
