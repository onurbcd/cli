package com.onurbcd.cli.dto.bill;

import com.onurbcd.cli.dto.PrimeSaveDto;
import com.onurbcd.cli.enums.PaymentType;
import com.onurbcd.cli.model.MultipartFile;
import com.onurbcd.cli.util.Constant;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.shell.component.context.ComponentContext;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.util.UUID;

import static com.onurbcd.cli.util.Constant.*;
import static com.onurbcd.cli.util.Converter.toUUID;
import static com.onurbcd.cli.util.DateUtil.parseLocalDate;
import static com.onurbcd.cli.util.EnumUtil.valueOf;
import static com.onurbcd.cli.util.FileUtil.fileToMultipartFile;
import static com.onurbcd.cli.util.FlowUtil.getString;

@SuperBuilder
@Getter
@Setter
@Validated
public class BillCloseDto extends PrimeSaveDto {

    @NotNull(message = "Bill id is required.")
    private UUID billId;

    @NotNull(message = "Payment date is required.")
    private LocalDate paymentDateCalendarDate;

    @NotNull(message = "Payment type is required.")
    private PaymentType paymentType;

    @NotNull(message = "Source id is required.")
    private UUID sourceId;

    private MultipartFile multipartFile;

    public static BillCloseDto of(ComponentContext<?> context) {
        return BillCloseDto.builder()
                .name(Constant.BOGUS_NAME)
                .billId(toUUID(getString(context, BILL_ID_ID)))
                .paymentDateCalendarDate(parseLocalDate(getString(context, PAYMENT_DATE_ID)))
                .paymentType(valueOf(PaymentType.class, getString(context, PAYMENT_TYPE_ID)))
                .sourceId(toUUID(getString(context, SOURCE_ID_ID)))
                .multipartFile(fileToMultipartFile(getString(context, RECEIPT_ID)))
                .build();
    }
}
