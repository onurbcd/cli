package com.onurbcd.eruservice.service.impl;

import com.onurbcd.eruservice.model.MultipartFile;
import com.onurbcd.eruservice.constant.Constant;
import com.onurbcd.eruservice.dto.bill.BillCloseDto;
import com.onurbcd.eruservice.dto.bill.BillDto;
import com.onurbcd.eruservice.dto.bill.BillOpenDto;
import com.onurbcd.eruservice.dto.budget.BudgetPatchDto;
import com.onurbcd.eruservice.dto.filter.BillFilter;
import com.onurbcd.eruservice.dto.filter.Filterable;
import com.onurbcd.eruservice.persistency.entity.Bill;
import com.onurbcd.eruservice.persistency.entity.BillType;
import com.onurbcd.eruservice.persistency.entity.Day;
import com.onurbcd.eruservice.persistency.entity.Source;
import com.onurbcd.eruservice.persistency.predicate.BillPredicateBuilder;
import com.onurbcd.eruservice.persistency.repository.BillRepository;
import com.onurbcd.eruservice.service.AbstractCrudService;
import com.onurbcd.eruservice.service.BillBalanceService;
import com.onurbcd.eruservice.service.BillDocumentService;
import com.onurbcd.eruservice.service.BillService;
import com.onurbcd.eruservice.service.BillTypeService;
import com.onurbcd.eruservice.service.BudgetService;
import com.onurbcd.eruservice.service.DayService;
import com.onurbcd.eruservice.enums.Error;
import com.onurbcd.eruservice.enums.QueryType;
import com.onurbcd.eruservice.mapper.BillOpenToEntityMapper;
import com.onurbcd.eruservice.model.BillBalanceParams;
import com.onurbcd.eruservice.model.BillDocParams;
import com.onurbcd.eruservice.service.validation.Action;
import com.querydsl.core.types.Predicate;
import jakarta.persistence.EntityManager;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

@Service
public class BillServiceImpl
        extends AbstractCrudService<Bill, BillDto, BillPredicateBuilder, BillOpenDto>
        implements BillService {

    private final BillRepository repository;

    private final BillOpenToEntityMapper toEntityMapper;

    private final DayService dayService;

    private final EntityManager entityManager;

    private final BillDocumentService documentService;

    private final BudgetService budgetService;

    private final BillTypeService billTypeService;

    private final BillBalanceService billBalanceService;

    protected BillServiceImpl(BillRepository repository, BillOpenToEntityMapper toEntityMapper, DayService dayService,
                              EntityManager entityManager, BillDocumentService documentService,
                              BudgetService budgetService, BillTypeService billTypeService,
                              BillBalanceService billBalanceService) {

        super(repository, toEntityMapper, QueryType.CUSTOM, BillPredicateBuilder.class);
        this.repository = repository;
        this.toEntityMapper = toEntityMapper;
        this.dayService = dayService;
        this.entityManager = entityManager;
        this.documentService = documentService;
        this.budgetService = budgetService;
        this.billTypeService = billTypeService;
        this.billBalanceService = billBalanceService;
    }

    @Override
    public UUID openBill(BillOpenDto billOpenDto, MultipartFile multipartFile) {
        var bill = toEntityMapper.apply(billOpenDto);
        var countByBudgetId = repository.countByBudgetId(billOpenDto.getBudgetId());
        Action.checkIf(countByBudgetId < 1).orElseThrow(Error.BILL_ALREADY_OPENED);
        var budgetValues = budgetService.getBudgetValues(billOpenDto.getBudgetId());
        Action.checkIf(Boolean.FALSE.equals(budgetValues.paid())).orElseThrow(Error.BILL_ALREADY_PAID);

        var billDocParams = BillDocParams
                .builder()
                .path(budgetValues.path())
                .referenceDayCalendarDate(billOpenDto.getReferenceDayCalendarDate())
                .multipartFile(multipartFile)
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
        return bill.getId();
    }

    @Override
    public void closeBill(UUID id, BillCloseDto billCloseDto, MultipartFile multipartFile) {
        var bill = getOrElseThrow(id);
        Action.checkIf(Boolean.FALSE.equals(bill.getClosed())).orElseThrow(Error.BILL_ALREADY_CLOSED);
        var billTypeValues = billTypeService.getValues(bill.getBillType().getId());

        var billDocParams = BillDocParams
                .builder()
                .path(billTypeValues.path())
                .referenceDayCalendarDate(bill.getReferenceDay().getCalendarDate())
                .multipartFile(multipartFile)
                .documentType(bill.getDocumentType())
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
        bill.setObservation(billCloseDto.getObservation());
        bill.setPaymentType(billCloseDto.getPaymentType());
        bill.setSource(entityManager.getReference(Source.class, billCloseDto.getSourceId()));
        bill.setClosed(Boolean.TRUE);
        bill.setBalance(balance);

        repository.save(bill);
        budgetService.update(BudgetPatchDto.of(Boolean.TRUE), bill.getBudget().getId());
    }

    @Override
    protected Predicate getPredicate(Filterable filter) {
        return BillPredicateBuilder.all((BillFilter) filter);
    }

    private void fillDay(@Nullable LocalDate localDateIn, Consumer<Day> dayConsumer) {
        Optional.ofNullable(localDateIn)
                .ifPresent(localDate -> {
                    var dayId = dayService.createId(localDate);
                    dayConsumer.accept(entityManager.getReference(Day.class, dayId));
                });
    }
}
