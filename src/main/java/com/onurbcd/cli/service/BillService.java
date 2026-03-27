package com.onurbcd.cli.service;

import com.onurbcd.cli.config.property.AdminProperties;
import com.onurbcd.cli.dto.Dtoable;
import com.onurbcd.cli.dto.bill.BillCloseDto;
import com.onurbcd.cli.dto.bill.BillDto;
import com.onurbcd.cli.dto.bill.BillOpenDto;
import com.onurbcd.cli.dto.budget.BudgetPatchDto;
import com.onurbcd.cli.dto.filter.BillFilter;
import com.onurbcd.cli.dto.filter.Filterable;
import com.onurbcd.cli.enums.DocumentType;
import com.onurbcd.cli.enums.Error;
import com.onurbcd.cli.enums.QueryType;
import com.onurbcd.cli.mapper.BillOpenToEntityMapper;
import com.onurbcd.cli.model.BillBalanceParams;
import com.onurbcd.cli.model.BillDocParams;
import com.onurbcd.cli.persistency.entity.Bill;
import com.onurbcd.cli.persistency.entity.BillType;
import com.onurbcd.cli.persistency.entity.Day;
import com.onurbcd.cli.persistency.predicate.BillPredicateBuilder;
import com.onurbcd.cli.persistency.repository.BillRepository;
import com.onurbcd.cli.util.Constant;
import com.onurbcd.cli.validator.Action;
import com.querydsl.core.types.Predicate;
import jakarta.persistence.EntityManager;
import org.springframework.lang.Nullable;
import org.springframework.shell.component.flow.SelectItem;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

@Service
public class BillService extends AbstractCrudService<Bill, BillDto, BillPredicateBuilder, BillOpenDto> {

    private final BillRepository repository;
    private final BillOpenToEntityMapper toEntityMapper;
    private final DayService dayService;
    private final EntityManager entityManager;
    private final BillDocumentService documentService;
    private final BudgetService budgetService;
    private final BillTypeService billTypeService;
    private final BillBalanceService billBalanceService;
    private final AdminProperties config;

    public BillService(BillRepository repository, BillOpenToEntityMapper toEntityMapper, DayService dayService,
                       EntityManager entityManager, BillDocumentService documentService,
                       BudgetService budgetService, BillTypeService billTypeService,
                       BillBalanceService billBalanceService, AdminProperties config) {

        super(repository, toEntityMapper, QueryType.CUSTOM, BillPredicateBuilder.class);
        this.repository = repository;
        this.toEntityMapper = toEntityMapper;
        this.dayService = dayService;
        this.entityManager = entityManager;
        this.documentService = documentService;
        this.budgetService = budgetService;
        this.billTypeService = billTypeService;
        this.billBalanceService = billBalanceService;
        this.config = config;
    }

    @Override
    @Transactional
    public String save(Dtoable dto, UUID id) {
        return switch (dto) {
            case BillOpenDto billOpenDto -> openBill(billOpenDto);
            case BillCloseDto billCloseDto -> closeBill(billCloseDto.getBillId(), billCloseDto);
            default -> throw new IllegalArgumentException("Unsupported DTO type: " + dto.getClass().getSimpleName());
        };
    }

    @Override
    public BillDto getById(UUID id) {
        var billDto = (BillDto) super.getById(id);

        if (billDto.getBillDocument() != null && billDto.getBillDocument().isNotEmpty()) {
            billDto.getBillDocument().setStoragePath(config.getStoragePath());
        } else {
            billDto.setBillDocument(null);
        }

        if (billDto.getReceipt() != null && billDto.getReceipt().isNotEmpty()) {
            billDto.getReceipt().setStoragePath(config.getStoragePath());
        } else {
            billDto.setReceipt(null);
        }

        return billDto;
    }

    @Override
    protected Predicate getPredicate(Filterable filter) {
        return BillPredicateBuilder.all((BillFilter) filter);
    }

    public List<SelectItem> getOpenBills(Short year, Short month) {
        return repository.getOpenBills(year, month)
                .stream()
                .map(itemDto -> SelectItem.of(itemDto.getName(), itemDto.getId().toString()))
                .toList();
    }

    private String openBill(BillOpenDto billOpenDto) {
        var bill = toEntityMapper.apply(billOpenDto);
        var countByBudgetId = repository.countByBudgetId(billOpenDto.getBudgetId());
        Action.checkIf(countByBudgetId < 1).orElseThrow(Error.BILL_ALREADY_OPENED);
        var budgetValues = budgetService.getBudgetValues(billOpenDto.getBudgetId());
        Action.checkIf(Boolean.FALSE.equals(budgetValues.paid())).orElseThrow(Error.BILL_ALREADY_PAID);

        var billDocParams = BillDocParams
                .builder()
                .path(budgetValues.path())
                .referenceDayCalendarDate(billOpenDto.getReferenceDayCalendarDate())
                .multipartFile(billOpenDto.getMultipartFile())
                .documentType(billOpenDto.getDocumentType())
                .referenceType(billOpenDto.getReferenceType())
                .build();

        var billDocument = documentService.createDocument(billDocParams);
        bill.setName(Constant.BOGUS_NAME);
        fillDay(billOpenDto.getReferenceDayCalendarDate(), bill::setReferenceDay);
        fillDay(billOpenDto.getDocumentDateCalendarDate(), bill::setDocumentDate);
        fillDay(billOpenDto.getDueDateCalendarDate(), bill::setDueDate);
        bill.setValue(budgetValues.amount());
        bill.setBillDocument(billDocument);
        bill.setBillType(entityManager.getReference(BillType.class, budgetValues.billTypeId()));
        bill.setClosed(Boolean.FALSE);
        bill = repository.save(bill);
        return bill.getId().toString();
    }

    private String closeBill(UUID id, BillCloseDto billCloseDto) {
        var bill = getOrElseThrow(id);
        Action.checkIf(Boolean.FALSE.equals(bill.getClosed())).orElseThrow(Error.BILL_ALREADY_CLOSED);
        var billTypeValues = billTypeService.getValues(bill.getBillType().getId());

        var billDocParams = BillDocParams
                .builder()
                .path(billTypeValues.path())
                .referenceDayCalendarDate(bill.getReferenceDay().getCalendarDate())
                .multipartFile(billCloseDto.getMultipartFile())
                .documentType(DocumentType.VOUCHER)
                .referenceType(bill.getReferenceType())
                .build();

        var receipt = documentService.createDocument(billDocParams);

        var billBalanceParams = BillBalanceParams
                .builder()
                .billCloseDto(billCloseDto)
                .bill(bill)
                .categoryId(billTypeValues.categoryId())
                .build();

        var balance = billBalanceService.saveBalance(billBalanceParams);
        bill.setName(Constant.BOGUS_NAME);
        fillDay(billCloseDto.getPaymentDateCalendarDate(), bill::setPaymentDate);
        bill.setReceipt(receipt);
        bill.setClosed(Boolean.TRUE);
        bill.setBalance(balance);
        bill = repository.save(bill);
        budgetService.update(BudgetPatchDto.of(Boolean.TRUE), bill.getBudget().getId());
        return bill.getId().toString();
    }

    private void fillDay(@Nullable LocalDate localDateIn, Consumer<Day> dayConsumer) {
        Optional.ofNullable(localDateIn)
                .ifPresent(localDate -> {
                    var dayId = dayService.createId(localDate);
                    dayConsumer.accept(entityManager.getReference(Day.class, dayId));
                });
    }
}
