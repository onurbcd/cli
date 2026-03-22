package com.onurbcd.cli.dto.balance;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.onurbcd.cli.dto.PrimeDto;
import com.onurbcd.cli.dto.document.DocumentDto;
import com.onurbcd.cli.enums.BalanceType;
import com.onurbcd.cli.enums.PaymentType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.lang.Nullable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@NoArgsConstructor
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class BalanceDto extends PrimeDto {

    private Short sequence;
    private Integer dayId;
    private LocalDate dayCalendarDate;
    private UUID sourceId;
    private String sourceName;
    private UUID categoryId;
    private String categoryName;
    private BigDecimal amount;
    private String code;
    private String description;
    private BalanceType balanceType;
    private PaymentType paymentType;
    private Set<DocumentDto> documents;

    @Nullable
    public Set<UUID> getDocumentsIds() {
        return Optional.ofNullable(documents)
                .map(documentsSet -> documentsSet.stream()
                        .map(DocumentDto::getId)
                        .collect(Collectors.toSet()))
                .orElse(null);
    }
}
