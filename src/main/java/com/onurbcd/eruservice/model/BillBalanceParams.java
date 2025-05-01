package com.onurbcd.eruservice.model;

import com.onurbcd.eruservice.dto.bill.BillCloseDto;
import com.onurbcd.eruservice.persistency.entity.Bill;
import lombok.*;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class BillBalanceParams {

    private BillCloseDto billCloseDto;
    private Bill bill;
    private UUID categoryId;
}
