package com.onurbcd.cli.service;

import com.onurbcd.cli.annotation.PrimeService;
import com.onurbcd.cli.dto.incomesource.IncomeSourceDto;
import com.onurbcd.cli.enums.Domain;
import com.onurbcd.cli.mapper.IncomeSourceToDtoMapper;
import com.onurbcd.cli.mapper.IncomeSourceToEntityMapper;
import com.onurbcd.cli.persistency.entity.IncomeSource;
import com.onurbcd.cli.persistency.repository.IncomeSourceRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.onurbcd.cli.TestConstants.ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IncomeSourceServiceTest {

    @Mock
    private IncomeSourceRepository repository;
    @Mock
    private IncomeSourceToDtoMapper toDtoMapper;
    @Mock
    private IncomeSourceToEntityMapper toEntityMapper;

    @Test
    @DisplayName("should be registered as the income source service")
    void shouldBeQualifiedAsIncomeSource() {
        var annotation = IncomeSourceService.class.getAnnotation(PrimeService.class);

        assertThat(annotation.value()).isEqualTo(Domain.INCOME_SOURCE);
    }

    @Test
    @DisplayName("should read by id through JPA and the dto mapper")
    void givenEntity_whenGetById_shouldMapWithToDtoMapper() {
        var entity = IncomeSource.builder().id(ID).name("name").active(true).build();
        var dto = new IncomeSourceDto();
        when(repository.findById(ID)).thenReturn(Optional.of(entity));
        when(toDtoMapper.apply(entity)).thenReturn(dto);
        var service = new IncomeSourceService(repository, toDtoMapper, toEntityMapper);

        var result = service.getById(ID);

        assertThat(result).isSameAs(dto);
        verify(repository).findById(ID);
        verify(toDtoMapper).apply(entity);
        verifyNoMoreInteractions(repository, toDtoMapper);
        verifyNoInteractions(toEntityMapper);
    }
}
