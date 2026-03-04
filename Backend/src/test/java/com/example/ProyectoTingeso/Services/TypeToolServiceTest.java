package com.example.ProyectoTingeso.Services;

import com.example.ProyectoTingeso.Entities.TypeToolEntity;
import com.example.ProyectoTingeso.Repositories.TypeToolRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Parameterized tests for TypeToolService.
 * Replaces three separate tests for configurationDailyRateTypeTool,
 * configurationDebtTypeTool and registerReplacementTypeTool with a single
 * @ParameterizedTest.
 */
class TypeToolServiceTest {

    private final TypeToolRepository typeToolRepo = mock(TypeToolRepository.class);
    private final TypeToolService typeToolService = new TypeToolService(typeToolRepo);

    @ParameterizedTest(name = "[{index}] configure {0} = {1}")
    @CsvSource({
            "dailyRate,        500",
            "debtRate,         100",
            "replacementValue, 2000"
    })
    void configureTypeTool_updatesFieldAndSaves(String configType, int value) {
        long id = 1L;
        TypeToolEntity entity = new TypeToolEntity();
        when(typeToolRepo.findById(id)).thenReturn(Optional.of(entity));
        when(typeToolRepo.save(any(TypeToolEntity.class))).thenAnswer(i -> i.getArgument(0));

        TypeToolEntity result;
        switch (configType.trim()) {
            case "dailyRate":
                result = typeToolService.configurationDailyRateTypeTool(id, value);
                assertEquals(value, result.getDailyRate());
                break;
            case "debtRate":
                result = typeToolService.configurationDebtTypeTool(id, value);
                assertEquals(value, result.getDebtRate());
                break;
            default:
                result = typeToolService.registerReplacementTypeTool(id, value);
                assertEquals(value, result.getReplacementValue());
                break;
        }
        verify(typeToolRepo, atLeast(1)).save(any(TypeToolEntity.class));
    }

    @ParameterizedTest(name = "[{index}] {0} throws EntityNotFoundException when id not found")
    @CsvSource({ "dailyRate", "debtRate", "replacementValue" })
    void configureTypeTool_throwsWhenNotFound(String configType) {
        long id = 99L;
        when(typeToolRepo.findById(id)).thenReturn(Optional.empty());

        switch (configType.trim()) {
            case "dailyRate":
                assertThrows(EntityNotFoundException.class,
                        () -> typeToolService.configurationDailyRateTypeTool(id, 10));
                break;
            case "debtRate":
                assertThrows(EntityNotFoundException.class,
                        () -> typeToolService.configurationDebtTypeTool(id, 10));
                break;
            default:
                assertThrows(EntityNotFoundException.class,
                        () -> typeToolService.registerReplacementTypeTool(id, 10));
                break;
        }
    }
}
