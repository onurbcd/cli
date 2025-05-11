package com.onurbcd.eruservice.service;

import com.onurbcd.eruservice.annotation.PrimeService;
import com.onurbcd.eruservice.config.property.AdminProperties;
import com.onurbcd.eruservice.dto.Dtoable;
import com.onurbcd.eruservice.dto.balance.BalanceDto;
import com.onurbcd.eruservice.dto.balance.BalanceSaveDto;
import com.onurbcd.eruservice.dto.balance.BalanceSumDto;
import com.onurbcd.eruservice.dto.filter.BalanceFilter;
import com.onurbcd.eruservice.dto.filter.Filterable;
import com.onurbcd.eruservice.enums.BalanceType;
import com.onurbcd.eruservice.enums.Direction;
import com.onurbcd.eruservice.enums.Domain;
import com.onurbcd.eruservice.enums.QueryType;
import com.onurbcd.eruservice.mapper.BalanceToEntityMapper;
import com.onurbcd.eruservice.model.CreateBalance;
import com.onurbcd.eruservice.model.MultipartFile;
import com.onurbcd.eruservice.model.SequenceParam;
import com.onurbcd.eruservice.persistency.entity.Balance;
import com.onurbcd.eruservice.persistency.entity.Day;
import com.onurbcd.eruservice.persistency.entity.Entityable;
import com.onurbcd.eruservice.persistency.predicate.BalancePredicateBuilder;
import com.onurbcd.eruservice.persistency.repository.BalanceRepository;
import com.onurbcd.eruservice.util.CollectionUtil;
import com.onurbcd.eruservice.util.Constant;
import com.onurbcd.eruservice.util.NumberUtil;
import com.onurbcd.eruservice.validator.BalanceValidator;
import com.querydsl.core.types.Predicate;
import jakarta.persistence.EntityManager;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

@Service
public class BalanceService extends AbstractCrudService<Balance, BalanceDto, BalancePredicateBuilder, BalanceSaveDto>
        implements CrudService, Sequenceable {

    private final BalanceRepository repository;
    private final BalanceValidator validationService;
    private final SequenceService sequenceService;
    private final DayService dayService;
    private final BalanceDocumentService balanceDocumentService;
    private final EntityManager entityManager;
    private final BalanceSourceService balanceSourceService;
    private final AdminProperties config;

    public BalanceService(BalanceRepository repository, BalanceToEntityMapper toEntityMapper,
                          BalanceValidator validationService,
                          @PrimeService(Domain.BALANCE_SEQUENCE) SequenceService sequenceService,
                          DayService dayService, BalanceDocumentService balanceDocumentService,
                          EntityManager entityManager, BalanceSourceService balanceSourceService,
                          AdminProperties config) {

        super(repository, toEntityMapper, QueryType.CUSTOM, BalancePredicateBuilder.class);
        this.repository = repository;
        this.validationService = validationService;
        this.sequenceService = sequenceService;
        this.dayService = dayService;
        this.balanceDocumentService = balanceDocumentService;
        this.entityManager = entityManager;
        this.balanceSourceService = balanceSourceService;
        this.config = config;
    }

    @Transactional
    public String save(BalanceSaveDto saveDto, @Nullable MultipartFile[] multipartFiles, @Nullable UUID id) {
        var currentBalance = id != null ? repository.get(id).orElse(null) : null;
        var currentAmount = Optional.ofNullable(currentBalance).map(Balance::getAmount).orElse(null);
        validate(saveDto, currentBalance, id);
        var createBalance = fillValues(saveDto, multipartFiles, currentBalance);
        var newBalance = repository.saveAndFlush(createBalance.getBalance());
        balanceDocumentService.deleteDocuments(createBalance.getDeleteDocuments());
        balanceSourceService.save(newBalance, currentAmount);
        return newBalance.getId().toString();
    }

    public List<BalanceSumDto> getSum(BalanceFilter filter) {
        var incomeValue = getSumByBalanceType(filter, BalanceType.INCOME);
        var outcomeValue = getSumByBalanceType(filter, BalanceType.OUTCOME);
        var diffValue = NumberUtil.subtract(incomeValue, outcomeValue);

        return List.of(
                BalanceSumDto.income(incomeValue),
                BalanceSumDto.outcome(outcomeValue),
                BalanceSumDto.diff(diffValue)
        );
    }

    public Balance save(BalanceSaveDto saveDto) {
        var balance = (Balance) super.fillValues(saveDto, null);
        var sequence = getSequence(null, saveDto.getDayCalendarDate(), saveDto.getBalanceType());
        var dayId = getDayId(saveDto, null);
        balance.setDay(entityManager.getReference(Day.class, dayId));
        balance.setSequence(sequence);
        var newBalance = repository.save(balance);
        balanceSourceService.save(newBalance, null);
        return newBalance;
    }

    @Override
    public void validate(Dtoable dto, Entityable entity, UUID id) {
        validationService.validate((BalanceSaveDto) dto, (Balance) entity, id);
    }

    @Override
    protected Predicate getPredicate(Filterable filter) {
        return BalancePredicateBuilder.all((BalanceFilter) filter);
    }

    @Override
    public BalanceDto getById(UUID id) {
        var balanceDto = (BalanceDto) super.getById(id);
        var documents = repository.getDocumentsById(id);

        if (CollectionUtil.isEmpty(documents)) {
            balanceDto.setDocuments(null);
        } else {
            documents.forEach(document -> document.setStoragePath(config.getStoragePath()));
            balanceDto.setDocuments(documents);
        }

        return balanceDto;
    }

    @Override
    public void delete(UUID id) {
        var balance = getOrElseThrow(id);
        var documents = repository.getDocuments(id);
        repository.deleteById(id);
        var sequenceParam = SequenceParam.of(balance, null);
        sequenceParam.setBalanceType(balance.getBalanceType());
        sequenceService.updateNextSequences(sequenceParam);
        balanceDocumentService.deleteDocuments(documents);
        balanceSourceService.delete(balance);
    }

    @Override
    public void updateSequence(UUID id, Direction direction) {
        var balance = getOrElseThrow(id);
        var sequenceParam = SequenceParam.of(balance, null);
        sequenceParam.setBalanceType(balance.getBalanceType());
        sequenceService.swapSequence(sequenceParam, direction);
    }

    @Override
    public void swapPosition(UUID id, Short targetSequence) {
        var balance = getOrElseThrow(id);
        var sequenceParam = SequenceParam.of(balance, targetSequence);
        sequenceParam.setBalanceType(balance.getBalanceType());
        sequenceService.swapPosition(sequenceParam);
    }

    private CreateBalance fillValues(BalanceSaveDto saveDto, @Nullable MultipartFile[] multipartFiles,
                                     @Nullable Balance currentBalance) {

        var balance = (Balance) super.fillValues(saveDto, currentBalance);
        var id = Optional.ofNullable(currentBalance).map(Balance::getId).orElse(null);
        var createDocument = balanceDocumentService.createDocuments(saveDto, multipartFiles, id);
        balance.setDocuments(createDocument.getSaveDocuments());
        balance.setName(Constant.BOGUS_NAME);
        var sequence = getSequence(currentBalance, saveDto.getDayCalendarDate(), balance.getBalanceType());
        balance.setSequence(sequence);
        var dayId = getDayId(saveDto, currentBalance);
        balance.setDay(entityManager.getReference(Day.class, dayId));
        return CreateBalance.builder().balance(balance).deleteDocuments(createDocument.getDeleteDocuments()).build();
    }

    private Short getSequence(@Nullable Balance current, LocalDate calendarDate, BalanceType balanceType) {
        return Optional
                .ofNullable(current)
                .map(Balance::getSequence)
                .orElseGet(getNextSequence(calendarDate, balanceType));
    }

    private Supplier<Short> getNextSequence(LocalDate calendarDate, BalanceType balanceType) {
        var sequenceParam = SequenceParam.of(calendarDate);
        sequenceParam.setBalanceType(balanceType);
        return () -> sequenceService.getNextSequence(sequenceParam);
    }

    private Integer getDayId(BalanceSaveDto saveDto, @Nullable Balance current) {
        return Optional
                .ofNullable(current)
                .map(Balance::getDay)
                .map(Day::getId)
                .orElseGet(() -> dayService.createId(saveDto.getDayCalendarDate()));
    }

    public BigDecimal getSumByBalanceType(BalanceFilter filter, BalanceType balanceType) {
        filter.setBalanceType(balanceType);
        return repository.getSum(BalancePredicateBuilder.all(filter));
    }
}
