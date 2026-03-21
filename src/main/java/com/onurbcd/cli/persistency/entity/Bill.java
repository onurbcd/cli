package com.onurbcd.cli.persistency.entity;

import com.onurbcd.cli.enums.DocumentType;
import com.onurbcd.cli.enums.ReferenceType;
import com.onurbcd.cli.util.Constant;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
@AttributeOverride(name = "name", column = @Column(insertable = false, updatable = false))
@Table(name = "bill", uniqueConstraints = @UniqueConstraint(name = "uc_bill_budget_id", columnNames = "budget_id"))
public class Bill extends Prime {

    /**
     * Ano de referência: sempre existe.
     * <br/><br/>
     * Mês de referência: nos casos em que não existe, como da conta do IPTU, deve se marcar o mês em que a conta foi
     * gerada.
     * <br/><br/>
     * Dia de referência: sempre marcado como o dia 1, excepto no caso da faxina ou adestrador, em que é marcado o dia
     * em que aconteceu.
     */
    @NotNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "reference_day_id", nullable = false)
    private Day referenceDay;

    /**
     * Data de recebimento do documento.
     * <br/>
     * É nulo, pois vão existir contas sem documento, como o contador, faxina, adestrador, vigilante (neste caso pode
     * se depois scannear o recibo).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_date_id")
    private Day documentDate;

    /**
     * Data de vencimento.
     */
    @NotNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "due_date_id", nullable = false)
    private Day dueDate;

    /**
     * Valor, sempre maior que zero
     */
    @NotNull
    @Column(name = "value", nullable = false, precision = 19, scale = 4)
    @DecimalMin(Constant.POSITIVE_AMOUNT_MIN)
    @DecimalMax(Constant.AMOUNT_MAX)
    private BigDecimal value;

    /**
     * Data de pagamento: é nulo porque só é preenchido quando paga a conta.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_date_id")
    private Day paymentDate;

    /**
     * Documento com o valor a pagar: é nulo, pois vão existir contas sem documento, como o contador, faxina,
     * adestrador, vigilante (neste caso pode se depois scannear o recibo).
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bill_document_id")
    private Document billDocument;

    /**
     * Comprovante: é nulo porque só é preenchido quando paga a conta.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receipt_id")
    private Document receipt;

    @Size(max = 250)
    @Column(name = "observation", length = 250)
    private String observation;

    /**
     * Parcela: para o caso do convênio ou financiamento do carro.
     */
    @Min(1)
    @Column(name = "installment")
    private Short installment;

    @NotNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "bill_type_id", nullable = false)
    private BillType billType;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false, length = 7)
    private DocumentType documentType;

    @NotNull
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "budget_id", nullable = false, unique = true)
    private Budget budget;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "reference_type", length = 5, nullable = false)
    private ReferenceType referenceType;

    @NotNull
    @Column(name = "closed", nullable = false)
    private Boolean closed;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "balance_id")
    private Balance balance;

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
