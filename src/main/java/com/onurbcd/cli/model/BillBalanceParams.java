package com.onurbcd.cli.model;

import com.onurbcd.cli.dto.bill.BillCloseDto;
import com.onurbcd.cli.persistency.entity.Bill;
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
