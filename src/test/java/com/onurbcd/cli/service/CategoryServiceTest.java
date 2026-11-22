package com.onurbcd.cli.service;

import com.onurbcd.cli.annotation.PrimeService;
import com.onurbcd.cli.dto.ItemDto;
import com.onurbcd.cli.dto.category.CategoryDto;
import com.onurbcd.cli.dto.category.CategorySaveDto;
import com.onurbcd.cli.dto.filter.CategoryFilter;
import com.onurbcd.cli.enums.Domain;
import com.onurbcd.cli.enums.ErrorCode;
import com.onurbcd.cli.exception.ApiException;
import com.onurbcd.cli.mapper.CategoryToEntityMapper;
import com.onurbcd.cli.persistency.entity.Category;
import com.onurbcd.cli.persistency.predicate.CategoryPredicateBuilder;
import com.onurbcd.cli.persistency.repository.CategoryRepository;
import com.onurbcd.cli.validator.CategoryValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.shell.jline.tui.component.flow.SelectItem;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static com.onurbcd.cli.TestConstants.ID;
import static com.onurbcd.cli.TestConstants.PARENT_ID;
import static com.onurbcd.cli.TestConstants.SOURCE_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    private static final LocalDateTime CREATED = LocalDateTime.of(2024, 3, 15, 10, 30);
    private static final UUID GRANDPARENT_ID = UUID.fromString("99999999-9999-9999-9999-999999999999");

    @Mock
    private CategoryRepository repository;
    @Mock
    private CategoryToEntityMapper toEntityMapper;
    @Mock
    private CategoryValidator validator;
    @Captor
    private ArgumentCaptor<Category> categoryCaptor;

    private CategoryService service;

    @BeforeEach
    void setUp() {
        service = new CategoryService(repository, toEntityMapper, validator);
    }

    private static CategoryDto dto(UUID id, UUID parentId, int level, Boolean lastBranch) {
        var dto = new CategoryDto();
        dto.setId(id);
        dto.setParentId(parentId);
        dto.setLevel((short) level);
        dto.setLastBranch(lastBranch);
        dto.setCreatedDate(CREATED);
        return dto;
    }

    private static CategorySaveDto saveDto(UUID parentId) {
        return CategorySaveDto.builder().name("name").active(true).parentId(parentId).build();
    }

    private static Category mapped() {
        return Category.builder().name("name").active(true).level((short) 9).lastBranch(false).build();
    }

    @Test
    @DisplayName("should be registered as the category service")
    void shouldBeQualifiedAsCategory() {
        assertThat(CategoryService.class.getAnnotation(PrimeService.class).value()).isEqualTo(Domain.CATEGORY);
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("for a new category, should validate, fetch the parent and persist it as a last-branch child")
        void givenNewCategory_shouldCreateChildOfParent() {
            var saveDto = saveDto(PARENT_ID);
            var parent = dto(PARENT_ID, GRANDPARENT_ID, 2, false);
            var entity = mapped();
            var persisted = Category.builder().id(ID).build();
            when(repository.getSingle(PARENT_ID)).thenReturn(parent);
            when(toEntityMapper.apply(saveDto)).thenReturn(entity);
            when(repository.save(entity)).thenReturn(persisted);

            var result = service.save(saveDto, null);

            assertThat(result).isEqualTo(ID.toString());
            InOrder order = inOrder(validator, repository);
            order.verify(validator).validate(saveDto, null, null);
            order.verify(repository).getSingle(PARENT_ID);
            order.verify(repository).save(categoryCaptor.capture());
            var saved = categoryCaptor.getValue();
            assertThat(saved).isSameAs(entity);
            assertThat(saved.getId()).isNull();
            assertThat(saved.getCreatedDate()).isNull();
            assertThat(saved.getParent()).isEqualTo(new Category(PARENT_ID));
            assertThat(saved.getLevel()).isEqualTo((short) 3);
            assertThat(saved.getLastBranch()).isTrue();
            verifyNoMoreInteractions(repository, validator, toEntityMapper);
        }

        @Test
        @DisplayName("for a new category under a last-branch parent, should clear the parent's last-branch flag")
        void givenLastBranchParent_shouldClearParentFlag() {
            var saveDto = saveDto(PARENT_ID);
            var parent = dto(PARENT_ID, GRANDPARENT_ID, 1, true);
            var entity = mapped();
            when(repository.getSingle(PARENT_ID)).thenReturn(parent);
            when(toEntityMapper.apply(saveDto)).thenReturn(entity);
            when(repository.save(entity)).thenReturn(Category.builder().id(ID).build());

            var result = service.save(saveDto, null);

            assertThat(result).isEqualTo(ID.toString());
            assertThat(entity.getLevel()).isEqualTo((short) 2);
            InOrder order = inOrder(repository);
            order.verify(repository).getSingle(PARENT_ID);
            order.verify(repository).save(entity);
            order.verify(repository).updateLastBranch(false, PARENT_ID);
            verifyNoMoreInteractions(repository);
            verify(validator).validate(saveDto, null, null);
            verifyNoMoreInteractions(validator, toEntityMapper);
        }

        @ParameterizedTest(name = "[{index}] parent lastBranch={0}")
        @NullSource
        @ValueSource(booleans = false)
        @DisplayName("for a new category under a parent that is not flagged last branch, should not touch the flag")
        void givenNonLastBranchParent_shouldNotUpdateFlag(Boolean lastBranch) {
            var saveDto = saveDto(PARENT_ID);
            var entity = mapped();
            when(repository.getSingle(PARENT_ID)).thenReturn(dto(PARENT_ID, GRANDPARENT_ID, 2, lastBranch));
            when(toEntityMapper.apply(saveDto)).thenReturn(entity);
            when(repository.save(entity)).thenReturn(Category.builder().id(ID).build());

            service.save(saveDto, null);

            verify(repository).getSingle(PARENT_ID);
            verify(repository).save(entity);
            verifyNoMoreInteractions(repository);
            verify(validator).validate(saveDto, null, null);
            verifyNoMoreInteractions(validator);
        }

        @Test
        @DisplayName("for a new category whose parent does not exist, should fail with not found and not persist")
        void givenUnknownParent_shouldThrowNotFound() {
            var saveDto = saveDto(PARENT_ID);
            when(repository.getSingle(PARENT_ID)).thenReturn(null);

            assertThatThrownBy(() -> service.save(saveDto, null))
                    .isInstanceOfSatisfying(ApiException.class,
                            e -> assertThat(e.getError()).isEqualTo(ErrorCode.ENTITY_DOES_NOT_EXIST));

            verify(validator).validate(saveDto, null, null);
            verify(repository).getSingle(PARENT_ID);
            verifyNoMoreInteractions(repository, validator, toEntityMapper);
        }

        @Test
        @DisplayName("for an existing category, should keep its identity, parent, level and last-branch flag")
        void givenExistingCategory_shouldKeepStructure() {
            var saveDto = saveDto(PARENT_ID);
            var current = dto(ID, PARENT_ID, 4, false);
            var entity = mapped();
            when(repository.getSingle(ID)).thenReturn(current);
            when(toEntityMapper.apply(saveDto)).thenReturn(entity);
            when(repository.save(entity)).thenReturn(Category.builder().id(ID).build());

            var result = service.save(saveDto, ID);

            assertThat(result).isEqualTo(ID.toString());
            InOrder order = inOrder(repository, validator);
            order.verify(repository).getSingle(ID);
            order.verify(validator).validate(saveDto, current, ID);
            order.verify(repository).save(categoryCaptor.capture());
            var saved = categoryCaptor.getValue();
            assertThat(saved.getId()).isEqualTo(ID);
            assertThat(saved.getCreatedDate()).isEqualTo(CREATED);
            assertThat(saved.getParent()).isEqualTo(new Category(PARENT_ID));
            assertThat(saved.getLevel()).isEqualTo((short) 4);
            assertThat(saved.getLastBranch()).isFalse();
            verifyNoMoreInteractions(repository, validator, toEntityMapper);
        }

        @Test
        @DisplayName("for an existing category, should not look up a parent nor update any last-branch flag")
        void givenExistingCategoryWithLastBranch_shouldNotUpdateParentFlag() {
            var saveDto = saveDto(PARENT_ID);
            var current = dto(ID, PARENT_ID, 2, true);
            var entity = mapped();
            when(repository.getSingle(ID)).thenReturn(current);
            when(toEntityMapper.apply(saveDto)).thenReturn(entity);
            when(repository.save(entity)).thenReturn(Category.builder().id(ID).build());

            service.save(saveDto, ID);

            assertThat(entity.getLastBranch()).isTrue();
            verify(repository).getSingle(ID);
            verify(repository).save(entity);
            verifyNoMoreInteractions(repository);
            verify(validator).validate(saveDto, current, ID);
            verifyNoMoreInteractions(validator, toEntityMapper);
        }

        @Test
        @DisplayName("when validation fails, should propagate and not persist")
        void givenValidationFailure_shouldNotPersist() {
            var saveDto = saveDto(null);
            var failure = new ApiException(ErrorCode.CATEGORY_PARENT_IS_NULL);
            doThrow(failure).when(validator).validate(saveDto, null, null);

            assertThatThrownBy(() -> service.save(saveDto, null)).isSameAs(failure);

            verify(validator).validate(saveDto, null, null);
            verifyNoMoreInteractions(validator, repository, toEntityMapper);
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("when the parent keeps no children, should delete and flag the parent as last branch")
        void givenNoChildrenLeft_shouldFlagParent() {
            var category = dto(ID, PARENT_ID, 3, true);
            when(repository.getSingle(ID)).thenReturn(category);
            when(repository.countChildren(PARENT_ID)).thenReturn(0L);

            service.delete(ID);

            InOrder order = inOrder(repository, validator);
            order.verify(repository).getSingle(ID);
            order.verify(validator).validateDelete(category);
            order.verify(repository).deleteById(ID);
            order.verify(repository).countChildren(PARENT_ID);
            order.verify(repository).updateLastBranch(true, PARENT_ID);
            verifyNoMoreInteractions(repository, validator);
        }

        @ParameterizedTest(name = "[{index}] {0} children left")
        @ValueSource(longs = {1, 5})
        @DisplayName("when the parent still has children, should delete and keep the parent flag")
        void givenChildrenLeft_shouldKeepParentFlag(long children) {
            var category = dto(ID, PARENT_ID, 3, true);
            when(repository.getSingle(ID)).thenReturn(category);
            when(repository.countChildren(PARENT_ID)).thenReturn(children);

            service.delete(ID);

            verify(repository).getSingle(ID);
            verify(validator).validateDelete(category);
            verify(repository).deleteById(ID);
            verify(repository).countChildren(PARENT_ID);
            verifyNoMoreInteractions(repository, validator);
        }

        @Test
        @DisplayName("when the category does not exist, should fail with not found and delete nothing")
        void givenUnknownCategory_shouldThrowNotFound() {
            when(repository.getSingle(ID)).thenReturn(null);

            assertThatThrownBy(() -> service.delete(ID))
                    .isInstanceOfSatisfying(ApiException.class,
                            e -> assertThat(e.getError()).isEqualTo(ErrorCode.ENTITY_DOES_NOT_EXIST));

            verify(repository).getSingle(ID);
            verifyNoMoreInteractions(repository, validator);
        }

        @Test
        @DisplayName("when delete validation fails, should propagate and delete nothing")
        void givenValidationFailure_shouldNotDelete() {
            var category = dto(ID, PARENT_ID, 1, true);
            var failure = new ApiException(ErrorCode.CATEGORY_CANNOT_DELETE_LEVEL_ONE);
            when(repository.getSingle(ID)).thenReturn(category);
            doThrow(failure).when(validator).validateDelete(category);

            assertThatThrownBy(() -> service.delete(ID)).isSameAs(failure);

            verify(repository).getSingle(ID);
            verify(validator).validateDelete(category);
            verifyNoMoreInteractions(repository, validator);
        }
    }

    @Nested
    @DisplayName("getAll")
    class GetAll {

        @Test
        @DisplayName("should query with the category predicate through the custom query")
        void shouldUseCategoryPredicate() {
            var filter = CategoryFilter.of(true, "abc", PARENT_ID, (short) 2, false);
            var pageable = PageRequest.of(0, 10);
            var page = new PageImpl<>(List.of(new CategoryDto()), pageable, 1);
            when(repository.getAll(CategoryPredicateBuilder.all(filter), pageable)).thenReturn(page);

            var result = service.getAll(pageable, filter);

            assertThat(result).isSameAs(page);
            verify(repository).getAll(CategoryPredicateBuilder.all(filter), pageable);
            verifyNoMoreInteractions(repository);
        }
    }

    @Nested
    @DisplayName("getCategoryItems")
    class GetCategoryItems {

        @ParameterizedTest(name = "[{index}] excludeAtar={0}")
        @ValueSource(booleans = {true, false})
        @DisplayName("should map the repository items to select items, passing the exclusion flag")
        void shouldMapItems(boolean excludeAtar) {
            when(repository.getCategoryItems(SOURCE_ID, excludeAtar))
                    .thenReturn(List.of(new ItemDto(ID, "first"), new ItemDto(PARENT_ID, "second")));

            var result = service.getCategoryItems(SOURCE_ID, excludeAtar);

            assertThat(result).usingRecursiveComparison().isEqualTo(List.of(
                    SelectItem.of("first", ID.toString()), SelectItem.of("second", PARENT_ID.toString())));
            verify(repository).getCategoryItems(SOURCE_ID, excludeAtar);
            verifyNoMoreInteractions(repository);
        }
    }
}
