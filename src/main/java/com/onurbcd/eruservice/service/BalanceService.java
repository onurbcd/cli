package com.onurbcd.eruservice.service;

import com.onurbcd.eruservice.model.MultipartFile;
import com.onurbcd.eruservice.dto.balance.BalanceSaveDto;
import com.onurbcd.eruservice.dto.balance.BalanceSumDto;
import com.onurbcd.eruservice.dto.filter.BalanceFilter;
import com.onurbcd.eruservice.persistency.entity.Balance;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

public interface BalanceService extends CrudService, Sequenceable {

    @Transactional
    String save(BalanceSaveDto saveDto, @Nullable MultipartFile[] multipartFiles, @Nullable UUID id);

    Set<BalanceSumDto> getSum(BalanceFilter filter);

    Balance save(BalanceSaveDto saveDto);
}
