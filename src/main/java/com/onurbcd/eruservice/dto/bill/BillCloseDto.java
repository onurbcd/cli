package com.onurbcd.eruservice.dto.bill;

import com.onurbcd.eruservice.constant.Constant;
import com.onurbcd.eruservice.constant.DtoConstant;
import com.onurbcd.eruservice.dto.PrimeSaveDto;
import com.onurbcd.eruservice.enums.PaymentType;
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

    @Size(max = DtoConstant.SIZE_250)
    private String observation;

    @NotNull
    private PaymentType paymentType;

    @NotNull
    private UUID sourceId;
}
