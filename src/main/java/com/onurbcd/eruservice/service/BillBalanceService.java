package com.onurbcd.eruservice.service;

import com.onurbcd.eruservice.persistency.entity.Balance;
import com.onurbcd.eruservice.model.BillBalanceParams;

public interface BillBalanceService {

    Balance saveBalance(BillBalanceParams params);
}
