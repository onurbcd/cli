package com.onurbcd.cli.service;

import com.onurbcd.cli.dto.Dtoable;
import com.onurbcd.cli.dto.ItemDto;
import com.onurbcd.cli.dto.filter.Filterable;
import com.onurbcd.cli.dto.filter.IncomeSourceFilter;
import com.onurbcd.cli.dto.incomesource.IncomeSourceDto;
import com.onurbcd.cli.dto.incomesource.IncomeSourceSaveDto;
import com.onurbcd.cli.enums.ErrorCode;
import com.onurbcd.cli.enums.QueryType;
import com.onurbcd.cli.exception.ApiException;
import com.onurbcd.cli.mapper.IncomeSourceToDtoMapper;
import com.onurbcd.cli.mapper.IncomeSourceToEntityMapper;
import com.onurbcd.cli.persistency.entity.IncomeSource;
import com.onurbcd.cli.persistency.entity.QIncomeSource;
import com.onurbcd.cli.persistency.predicate.IncomeSourcePredicateBuilder;
import com.onurbcd.cli.persistency.repository.IncomeSourceRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.shell.jline.tui.component.flow.SelectItem;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.onurbcd.cli.TestConstants.ID;
import static com.onurbcd.cli.TestConstants.PARENT_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AbstractCrudServiceTest {

    private static final LocalDateTime CREATED = LocalDateTime.of(2024, 3, 15, 10, 30);

    @Mock
    private IncomeSourceRepository repository;
    @Mock
    private IncomeSourceToDtoMapper toDtoMapper;
    @Mock
    private IncomeSourceToEntityMapper toEntityMapper;
    @Captor
    private ArgumentCaptor<IncomeSource> entityCaptor;

    static class JpaService extends
            AbstractCrudService<IncomeSource, IncomeSourceDto, IncomeSourcePredicateBuilder, IncomeSourceSaveDto> {

        JpaService(IncomeSourceRepository repository, IncomeSourceToDtoMapper toDto, IncomeSourceToEntityMapper toEntity) {
            super(repository, toDto, toEntity, QueryType.JPA, IncomeSourcePredicateBuilder.class);
        }

        IncomeSource callFindByIdOrElseThrow(UUID id) {
            return findByIdOrElseThrow(id);
        }

        IncomeSource callGetOrElseThrow(UUID id) {
            return getOrElseThrow(id);
        }

        Predicate callGetPredicate(Filterable filter) {
            return getPredicate(filter);
        }
    }

    static class CustomService extends
            AbstractCrudService<IncomeSource, IncomeSourceDto, IncomeSourcePredicateBuilder, IncomeSourceSaveDto> {

        CustomService(IncomeSourceRepository repository, IncomeSourceToEntityMapper toEntity) {
            super(repository, toEntity, QueryType.CUSTOM, IncomeSourcePredicateBuilder.class);
        }
    }

    private JpaService jpa() {
        return new JpaService(repository, toDtoMapper, toEntityMapper);
    }

    private CustomService custom() {
        return new CustomService(repository, toEntityMapper);
    }

    private static IncomeSource entity(UUID id) {
        return IncomeSource.builder().id(id).name("name").active(true).createdDate(CREATED).build();
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("without id, should not look anything up and persist the mapped entity with null id and created date")
        void givenNullId_whenSave_shouldCreateNewEntity() {
            var dto = IncomeSourceSaveDto.builder().name("name").active(true).build();
            var mapped = entity(PARENT_ID);
            var saved = entity(ID);
            when(toEntityMapper.apply(dto)).thenReturn(mapped);
            when(repository.save(mapped)).thenReturn(saved);

            var result = jpa().save(dto, null);

            assertThat(result).isEqualTo(ID.toString());
            verify(toEntityMapper).apply(dto);
            verify(repository).save(entityCaptor.capture());
            assertThat(entityCaptor.getValue()).isSameAs(mapped);
            assertThat(entityCaptor.getValue().getId()).isNull();
            assertThat(entityCaptor.getValue().getCreatedDate()).isNull();
            verifyNoMoreInteractions(repository, toEntityMapper);
            verifyNoInteractions(toDtoMapper);
        }

        @Test
        @DisplayName("with an existing id, should keep the current id and created date")
        void givenExistingId_whenSave_shouldKeepIdAndCreatedDate() {
            var dto = IncomeSourceSaveDto.builder().name("name").active(true).build();
            var current = entity(ID);
            var mapped = IncomeSource.builder().name("other").active(false).build();
            when(repository.findById(ID)).thenReturn(Optional.of(current));
            when(toEntityMapper.apply(dto)).thenReturn(mapped);
            when(repository.save(mapped)).thenReturn(mapped);

            var result = jpa().save(dto, ID);

            assertThat(result).isEqualTo(ID.toString());
            assertThat(mapped.getId()).isEqualTo(ID);
            assertThat(mapped.getCreatedDate()).isEqualTo(CREATED);
            verify(repository).findById(ID);
            verify(toEntityMapper).apply(dto);
            verify(repository).save(mapped);
            verifyNoMoreInteractions(repository, toEntityMapper);
        }

        @Test
        @DisplayName("with an unknown id, should throw not found and not persist")
        void givenUnknownId_whenSave_shouldThrowNotFound() {
            var dto = IncomeSourceSaveDto.builder().name("name").active(true).build();
            when(repository.findById(ID)).thenReturn(Optional.empty());
            var service = jpa();

            assertThatThrownBy(() -> service.save(dto, ID))
                    .isInstanceOfSatisfying(ApiException.class,
                            e -> assertThat(e.getError()).isEqualTo(ErrorCode.ENTITY_DOES_NOT_EXIST));

            verify(repository).findById(ID);
            verifyNoMoreInteractions(repository);
            verifyNoInteractions(toEntityMapper, toDtoMapper);
        }
    }

    @Nested
    @DisplayName("validate")
    class Validate {

        @Test
        @DisplayName("with null id and null entity, should not throw")
        void givenNullIdAndEntity_shouldNotThrow() {
            var service = jpa();
            var dto = IncomeSourceSaveDto.builder().build();

            service.validate(dto, null, null);

            verifyNoInteractions(repository, toEntityMapper, toDtoMapper);
        }

        @Test
        @DisplayName("with id and entity, should not throw")
        void givenIdAndEntity_shouldNotThrow() {
            var service = jpa();
            var dto = IncomeSourceSaveDto.builder().build();

            service.validate(dto, entity(ID), ID);

            verifyNoInteractions(repository, toEntityMapper, toDtoMapper);
        }

        @Test
        @DisplayName("with id and no entity, should throw not found for that id")
        void givenIdAndNoEntity_shouldThrowNotFound() {
            var service = jpa();
            var dto = IncomeSourceSaveDto.builder().build();

            assertThatThrownBy(() -> service.validate(dto, null, ID))
                    .isInstanceOfSatisfying(ApiException.class, e -> {
                        assertThat(e.getError()).isEqualTo(ErrorCode.ENTITY_DOES_NOT_EXIST);
                        assertThat(e.getMessage()).contains(ID.toString());
                    });
        }
    }

    @Nested
    @DisplayName("fillValues")
    class FillValues {

        @Test
        @DisplayName("without current entity, should map and clear id and created date")
        void givenNoEntity_shouldMapAndClear() {
            var dto = IncomeSourceSaveDto.builder().name("name").build();
            var mapped = entity(PARENT_ID);
            when(toEntityMapper.apply(dto)).thenReturn(mapped);

            var result = jpa().fillValues(dto, null);

            assertThat(result).isSameAs(mapped);
            assertThat(result.getId()).isNull();
            assertThat(result.getCreatedDate()).isNull();
            verify(toEntityMapper).apply(dto);
            verifyNoMoreInteractions(toEntityMapper);
        }

        @Test
        @DisplayName("with current entity, should copy its id and created date")
        void givenEntity_shouldCopyIdAndCreatedDate() {
            var dto = IncomeSourceSaveDto.builder().name("name").build();
            var mapped = IncomeSource.builder().name("n").build();
            when(toEntityMapper.apply(dto)).thenReturn(mapped);

            var result = jpa().fillValues(dto, entity(ID));

            assertThat(result.getId()).isEqualTo(ID);
            assertThat(result.getCreatedDate()).isEqualTo(CREATED);
            verify(toEntityMapper).apply(dto);
            verifyNoMoreInteractions(toEntityMapper);
        }
    }

    @Nested
    @DisplayName("getById")
    class GetById {

        @Test
        @DisplayName("with JPA query type, should find the entity and map it")
        void givenJpa_shouldMapEntity() {
            var found = entity(ID);
            var dto = new IncomeSourceDto();
            when(repository.findById(ID)).thenReturn(Optional.of(found));
            when(toDtoMapper.apply(found)).thenReturn(dto);

            var result = jpa().getById(ID);

            assertThat(result).isSameAs(dto);
            verify(repository).findById(ID);
            verify(toDtoMapper).apply(found);
            verifyNoMoreInteractions(repository, toDtoMapper);
        }

        @Test
        @DisplayName("with JPA query type and unknown id, should throw not found")
        void givenJpaUnknown_shouldThrow() {
            when(repository.findById(ID)).thenReturn(Optional.empty());
            var service = jpa();

            assertThatThrownBy(() -> service.getById(ID))
                    .isInstanceOfSatisfying(ApiException.class,
                            e -> assertThat(e.getError()).isEqualTo(ErrorCode.ENTITY_DOES_NOT_EXIST));

            verify(repository).findById(ID);
            verifyNoMoreInteractions(repository);
            verifyNoInteractions(toDtoMapper);
        }

        @Test
        @DisplayName("with custom query type, should return the single dto")
        void givenCustom_shouldReturnSingle() {
            var dto = new IncomeSourceDto();
            when(repository.getSingle(ID)).thenReturn(dto);

            var result = custom().getById(ID);

            assertThat(result).isSameAs(dto);
            verify(repository).getSingle(ID);
            verifyNoMoreInteractions(repository);
        }

        @Test
        @DisplayName("with custom query type and unknown id, should throw not found")
        void givenCustomUnknown_shouldThrow() {
            when(repository.getSingle(ID)).thenReturn(null);
            var service = custom();

            assertThatThrownBy(() -> service.getById(ID))
                    .isInstanceOfSatisfying(ApiException.class,
                            e -> assertThat(e.getError()).isEqualTo(ErrorCode.ENTITY_DOES_NOT_EXIST));

            verify(repository).getSingle(ID);
            verifyNoMoreInteractions(repository);
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("when one row is deleted, should not throw")
        void givenOneRow_shouldNotThrow() {
            when(repository.deleteUsingId(ID)).thenReturn(1);

            jpa().delete(ID);

            verify(repository).deleteUsingId(ID);
            verifyNoMoreInteractions(repository);
        }

        @ParameterizedTest(name = "[{index}] {0} rows deleted")
        @ValueSource(ints = {0, 2})
        @DisplayName("when the deleted row count is not one, should throw not found")
        void givenNotOneRow_shouldThrowNotFound(int rows) {
            when(repository.deleteUsingId(ID)).thenReturn(rows);
            var service = jpa();

            assertThatThrownBy(() -> service.delete(ID))
                    .isInstanceOfSatisfying(ApiException.class,
                            e -> assertThat(e.getError()).isEqualTo(ErrorCode.ENTITY_DOES_NOT_EXIST));

            verify(repository).deleteUsingId(ID);
            verifyNoMoreInteractions(repository);
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @ParameterizedTest(name = "[{index}] active={0}")
        @ValueSource(booleans = {true, false})
        @DisplayName("when one row is updated, should pass the dto active flag")
        void givenOneRow_shouldNotThrow(boolean active) {
            var dto = new IncomeSourceDto();
            dto.setActive(active);
            when(repository.updateActive(ID, active)).thenReturn(1);

            jpa().update(dto, ID);

            verify(repository).updateActive(ID, active);
            verifyNoMoreInteractions(repository);
        }

        @ParameterizedTest(name = "[{index}] {0} rows updated")
        @ValueSource(ints = {0, 2})
        @DisplayName("when the updated row count is not one, should throw not found")
        void givenNotOneRow_shouldThrowNotFound(int rows) {
            var dto = new IncomeSourceDto();
            dto.setActive(true);
            when(repository.updateActive(ID, true)).thenReturn(rows);
            var service = jpa();

            assertThatThrownBy(() -> service.update(dto, ID))
                    .isInstanceOfSatisfying(ApiException.class,
                            e -> assertThat(e.getError()).isEqualTo(ErrorCode.ENTITY_DOES_NOT_EXIST));

            verify(repository).updateActive(ID, true);
            verifyNoMoreInteractions(repository);
        }
    }

    @Nested
    @DisplayName("getAll")
    class GetAll {

        private final Pageable pageable = PageRequest.of(1, 5);

        @Test
        @DisplayName("with JPA query type, should query entities with the predicate and map each one")
        void givenJpa_shouldMapPage() {
            var filter = IncomeSourceFilter.of(true, " abc ");
            var found = entity(ID);
            var dto = new IncomeSourceDto();
            var predicate = expectedPredicate("abc", true);
            when(repository.findAll(predicate, pageable)).thenReturn(new PageImpl<>(List.of(found), pageable, 6));
            when(toDtoMapper.apply(found)).thenReturn(dto);

            var result = jpa().getAll(pageable, filter);

            assertThat(result.getContent()).containsExactly(dto);
            assertThat(result.getTotalElements()).isEqualTo(6);
            verify(repository).findAll(predicate, pageable);
            verify(toDtoMapper).apply(found);
            verifyNoMoreInteractions(repository, toDtoMapper);
        }

        @Test
        @DisplayName("with custom query type, should return the page from the custom query")
        @SuppressWarnings("unchecked")
        void givenCustom_shouldReturnCustomPage() {
            var filter = IncomeSourceFilter.of(null, null);
            var dto = new IncomeSourceDto();
            var page = new PageImpl<>(List.<IncomeSourceDto>of(dto), pageable, 6);
            var predicate = expectedPredicate(null, null);
            when(repository.getAll(predicate, pageable)).thenReturn(page);

            Page<Dtoable> result = custom().getAll(pageable, filter);

            assertThat(result).isSameAs(page);
            verify(repository).getAll(predicate, pageable);
            verifyNoMoreInteractions(repository);
            verifyNoInteractions(toDtoMapper);
        }
    }

    @Nested
    @DisplayName("getItems")
    class GetItems {

        @Test
        @DisplayName("should map every repository item to a select item with the id as string")
        void shouldMapItems() {
            var first = new ItemDto(ID, "first");
            var second = new ItemDto(PARENT_ID, "second");
            when(repository.getItems(PARENT_ID)).thenReturn(List.of(first, second));

            var result = jpa().getItems(PARENT_ID);

            assertThat(result).usingRecursiveComparison().isEqualTo(List.of(
                    SelectItem.of("first", ID.toString()), SelectItem.of("second", PARENT_ID.toString())));
            verify(repository).getItems(PARENT_ID);
            verifyNoMoreInteractions(repository);
        }

        @Test
        @DisplayName("when there are no items, should return an empty list")
        void givenNoItems_shouldReturnEmpty() {
            when(repository.getItems(null)).thenReturn(List.of());

            var result = jpa().getItems(null);

            assertThat(result).isEmpty();
            verify(repository).getItems(null);
            verifyNoMoreInteractions(repository);
        }
    }

    @Nested
    @DisplayName("findByIdOrElseThrow")
    class FindByIdOrElseThrow {

        @Test
        @DisplayName("when found, should return the entity")
        void givenFound_shouldReturn() {
            var found = entity(ID);
            when(repository.findById(ID)).thenReturn(Optional.of(found));

            assertThat(jpa().callFindByIdOrElseThrow(ID)).isSameAs(found);

            verify(repository).findById(ID);
            verifyNoMoreInteractions(repository);
        }

        @Test
        @DisplayName("when missing, should throw not found")
        void givenMissing_shouldThrow() {
            when(repository.findById(ID)).thenReturn(Optional.empty());
            var service = jpa();

            assertThatThrownBy(() -> service.callFindByIdOrElseThrow(ID))
                    .isInstanceOfSatisfying(ApiException.class,
                            e -> assertThat(e.getError()).isEqualTo(ErrorCode.ENTITY_DOES_NOT_EXIST));

            verify(repository).findById(ID);
            verifyNoMoreInteractions(repository);
        }
    }

    @Nested
    @DisplayName("getOrElseThrow")
    class GetOrElseThrow {

        @Test
        @DisplayName("when found, should return the entity")
        void givenFound_shouldReturn() {
            var found = entity(ID);
            when(repository.get(ID)).thenReturn(Optional.of(found));

            assertThat(jpa().callGetOrElseThrow(ID)).isSameAs(found);

            verify(repository).get(ID);
            verifyNoMoreInteractions(repository);
        }

        @Test
        @DisplayName("when missing, should throw not found")
        void givenMissing_shouldThrow() {
            when(repository.get(ID)).thenReturn(Optional.empty());
            var service = jpa();

            assertThatThrownBy(() -> service.callGetOrElseThrow(ID))
                    .isInstanceOfSatisfying(ApiException.class,
                            e -> assertThat(e.getError()).isEqualTo(ErrorCode.ENTITY_DOES_NOT_EXIST));

            verify(repository).get(ID);
            verifyNoMoreInteractions(repository);
        }
    }

    @Nested
    @DisplayName("getPredicate")
    class GetPredicate {

        @Test
        @DisplayName("should filter by search and active")
        void shouldBuildSearchAndActive() {
            var result = jpa().callGetPredicate(IncomeSourceFilter.of(false, "abc"));

            assertThat(result).isEqualTo(expectedPredicate("abc", false));
        }

        @Test
        @DisplayName("without search and active, should build an empty predicate")
        void givenNothing_shouldBuildEmpty() {
            var result = jpa().callGetPredicate(IncomeSourceFilter.of(null, null));

            assertThat(result).isEqualTo(new BooleanBuilder());
        }
    }

    private static Predicate expectedPredicate(String search, Boolean active) {
        var builder = new BooleanBuilder();

        if (search != null) {
            builder.and(QIncomeSource.incomeSource.name.containsIgnoreCase(search));
        }

        if (active != null) {
            builder.and(QIncomeSource.incomeSource.active.eq(active));
        }

        return builder;
    }
}
