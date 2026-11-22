package com.onurbcd.cli.service;

import com.onurbcd.cli.dto.billtype.BillTypeDto;
import com.onurbcd.cli.dto.billtype.BillTypeSaveDto;
import com.onurbcd.cli.dto.billtype.BillTypeValuesDto;
import com.onurbcd.cli.enums.ErrorCode;
import com.onurbcd.cli.exception.ApiException;
import com.onurbcd.cli.mapper.BillTypeToEntityMapper;
import com.onurbcd.cli.persistency.entity.BillType;
import com.onurbcd.cli.persistency.repository.BillTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static com.onurbcd.cli.TestConstants.CATEGORY_ID;
import static com.onurbcd.cli.TestConstants.ID;
import static com.onurbcd.cli.TestConstants.PARENT_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BillTypeServiceTest {

    private static final LocalDateTime CREATED = LocalDateTime.of(2024, 3, 15, 10, 30);

    @Mock
    private BillTypeRepository repository;
    @Mock
    private BillTypeToEntityMapper toEntityMapper;

    private BillTypeService service;

    @BeforeEach
    void setUp() {
        service = new BillTypeService(repository, toEntityMapper);
    }

    private static BillType billType(UUID id) {
        return BillType.builder().id(id).name("name").active(true).path("bills/water").createdDate(CREATED).build();
    }

    @Nested
    @DisplayName("getValues")
    class GetValues {

        @Test
        @DisplayName("when found, should return the values")
        void givenFound_shouldReturnValues() {
            var values = new BillTypeValuesDto("bills/water", CATEGORY_ID);
            when(repository.getValues(ID)).thenReturn(Optional.of(values));

            var result = service.getValues(ID);

            assertThat(result).isSameAs(values);
            verify(repository).getValues(ID);
            verifyNoMoreInteractions(repository);
        }

        @Test
        @DisplayName("when missing, should throw not found for the id")
        void givenMissing_shouldThrowNotFound() {
            when(repository.getValues(ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getValues(ID))
                    .isInstanceOfSatisfying(ApiException.class, e -> {
                        assertThat(e.getError()).isEqualTo(ErrorCode.ENTITY_DOES_NOT_EXIST);
                        assertThat(e.getMessage()).contains(ID.toString());
                    });

            verify(repository).getValues(ID);
            verifyNoMoreInteractions(repository);
        }
    }

    @Nested
    @DisplayName("save")
    class Save {

        private final BillTypeSaveDto dto = BillTypeSaveDto.builder().name("name").active(true).path("bills/water")
                .categoryId(CATEGORY_ID).build();

        @Test
        @DisplayName("without id, should not load anything and persist the mapped bill type as new")
        void givenNullId_shouldCreateNew() {
            var mapped = billType(PARENT_ID);
            var saved = billType(ID);
            when(toEntityMapper.apply(dto)).thenReturn(mapped);
            when(repository.save(mapped)).thenReturn(saved);

            var result = service.save(dto, null);

            assertThat(result).isEqualTo(ID.toString());
            assertThat(mapped.getId()).isNull();
            assertThat(mapped.getCreatedDate()).isNull();
            verify(toEntityMapper).apply(dto);
            verify(repository).save(mapped);
            verifyNoMoreInteractions(repository, toEntityMapper);
        }

        @Test
        @DisplayName("with an existing id, should load it with get() and keep its id and created date")
        void givenExistingId_shouldKeepIdentity() {
            var current = billType(ID);
            var mapped = BillType.builder().name("other").build();
            when(repository.get(ID)).thenReturn(Optional.of(current));
            when(toEntityMapper.apply(dto)).thenReturn(mapped);
            when(repository.save(mapped)).thenReturn(mapped);

            var result = service.save(dto, ID);

            assertThat(result).isEqualTo(ID.toString());
            assertThat(mapped.getId()).isEqualTo(ID);
            assertThat(mapped.getCreatedDate()).isEqualTo(CREATED);
            verify(repository).get(ID);
            verify(toEntityMapper).apply(dto);
            verify(repository).save(mapped);
            verifyNoMoreInteractions(repository, toEntityMapper);
        }

        @Test
        @DisplayName("with an unknown id, should throw not found and not persist")
        void givenUnknownId_shouldThrowNotFound() {
            when(repository.get(ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.save(dto, ID))
                    .isInstanceOfSatisfying(ApiException.class,
                            e -> assertThat(e.getError()).isEqualTo(ErrorCode.ENTITY_DOES_NOT_EXIST));

            verify(repository).get(ID);
            verifyNoMoreInteractions(repository, toEntityMapper);
        }
    }

    @Nested
    @DisplayName("getById")
    class GetById {

        @Test
        @DisplayName("should use the custom query type (single dto from the repository)")
        void shouldUseCustomQuery() {
            var dto = new BillTypeDto();
            when(repository.getSingle(ID)).thenReturn(dto);

            var result = service.getById(ID);

            assertThat(result).isSameAs(dto);
            verify(repository).getSingle(ID);
            verifyNoMoreInteractions(repository, toEntityMapper);
        }
    }
}
