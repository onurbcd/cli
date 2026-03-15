package com.onurbcd.cli.dto.bill;

import com.onurbcd.cli.dto.PrimeSaveDto;
import com.onurbcd.cli.enums.PaymentType;
import com.onurbcd.cli.util.Constant;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.UUID;

@SuperBuilder
@Getter
@Setter
public class BillCloseDto extends PrimeSaveDto {

    public BillCloseDto() {
        super(Constant.BOGUS_NAME, Boolean.TRUE);
    }

    @NotNull
    private LocalDate paymentDateCalendarDate;

    @Size(max = 250)
    private String observation;

    @NotNull
    private PaymentType paymentType;

    @NotNull
    private UUID sourceId;
}
