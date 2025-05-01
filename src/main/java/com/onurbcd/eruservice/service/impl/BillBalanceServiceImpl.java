package com.onurbcd.eruservice.service.impl;

import com.onurbcd.eruservice.constant.Constant;
import com.onurbcd.eruservice.dto.balance.BalanceSaveDto;
import com.onurbcd.eruservice.enums.BalanceType;
import com.onurbcd.eruservice.enums.ReferenceType;
import com.onurbcd.eruservice.persistency.entity.Balance;
import com.onurbcd.eruservice.persistency.entity.Bill;
import com.onurbcd.eruservice.service.BalanceService;
import com.onurbcd.eruservice.service.BillBalanceService;
import com.onurbcd.eruservice.model.BillBalanceParams;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class BillBalanceServiceImpl implements BillBalanceService {

    private final BalanceService balanceService;

    @Override
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
                .documentsIds(null)
                .build();
    }

    private String createDescription(BillBalanceParams params) {
        return new StringBuilder()
                .append("Pagamento efetuado por ")
                .append(params.getBillCloseDto().getPaymentType().getCode())
                .append(": ")
                .append(params.getBill().getDocumentType().getCode())
                .append(StringUtils.SPACE)
                .append(params.getBill().getBudget().getName())
                .append(StringUtils.SPACE)
                .append("referente a ")
                .append(createReference(params.getBill()))
                .toString();
    }

    private String createReference(Bill bill) {
        var pattern = ReferenceType.YEAR == bill.getReferenceType() ?
                Constant.YEAR_PATTERN :
                Constant.MONTH_YEAR_PATTERN;

        return bill.getReferenceDay().getCalendarDate().format(DateTimeFormatter.ofPattern(pattern));
    }
}
