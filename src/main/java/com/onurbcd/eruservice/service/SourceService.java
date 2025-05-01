package com.onurbcd.eruservice.service;

import com.onurbcd.eruservice.dto.filter.SourceFilter;
import com.onurbcd.eruservice.dto.source.BalanceSumDto;
import com.onurbcd.eruservice.model.UpdateSourceBalance;

import java.math.BigDecimal;

public interface SourceService extends CrudService {

    BigDecimal getUsableBalanceSum();

    BalanceSumDto getBalanceSum(SourceFilter filter);

    void updateBalance(UpdateSourceBalance param);
}
