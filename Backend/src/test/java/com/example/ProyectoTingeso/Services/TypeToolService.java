package com.example.ProyectoTingeso.Services;

import com.example.ProyectoTingeso.Entities.TypeToolEntity;
import com.example.ProyectoTingeso.Repositories.TypeToolRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;


class TypeToolServiceTest {

    @Mock
    private TypeToolRepository typeToolRepo;

    @InjectMocks
    private TypeToolService typeToolService;

    private TypeToolEntity typeTool;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        typeTool = new TypeToolEntity();
        typeTool.setIdTypeTool(1L);
        typeTool.setName("Martillo");
        typeTool.setCategory("Herramientas de Mano");
        typeTool.setReplacementValue(50000);
        typeTool.setDailyRate(5000);
        typeTool.setDebtRate(1000);
    }


    // ==================== Tests for findOrCreateTypeTool ====================

    @Test
    void whenFindOrCreateTypeTool_andToolExists_thenReturnExistingTool() {
        //Given
        String name = "Martillo";
        String category = "Herramientas de Mano";
        int replacementValue = 50000;
        int dailyRate = 5000;
        int debtRate = 1000;

        when(typeToolRepo.findByNameAndCategoryAndReplacementValueAndDailyRate(name, category, replacementValue, dailyRate))
                .thenReturn(Optional.of(typeTool));

        //When
        TypeToolEntity result = typeToolService.findOrCreateTypeTool(name, category, replacementValue, dailyRate, debtRate);

        //Then
        assertThat(result).isEqualTo(typeTool);
        verify(typeToolRepo, times(1)).findByNameAndCategoryAndReplacementValueAndDailyRate(name, category, replacementValue, dailyRate);
        verify(typeToolRepo, never()).save(any());
    }


    @Test
    void whenFindOrCreateTypeTool_andToolDoesNotExist_thenCreateNewTool() {
        //Given
        String name = "Destornillador";
        String category = "Herramientas de Mano";
        int replacementValue = 15000;
        int dailyRate = 2000;
        int debtRate = 500;

        when(typeToolRepo.findByNameAndCategoryAndReplacementValueAndDailyRate(name, category, replacementValue, dailyRate))
                .thenReturn(Optional.empty());
        when(typeToolRepo.save(any(TypeToolEntity.class))).thenReturn(typeTool);

        //When
        TypeToolEntity result = typeToolService.findOrCreateTypeTool(name, category, replacementValue, dailyRate, debtRate);

        //Then
        assertThat(result).isEqualTo(typeTool);
        verify(typeToolRepo, times(1)).save(any(TypeToolEntity.class));
    }


    @Test
    void whenFindOrCreateTypeTool_withNullName_thenThrowIllegalStateException() {
        //Given
        String name = null;
        String category = "Herramientas de Mano";
        int replacementValue = 50000;
        int dailyRate = 5000;
        int debtRate = 1000;

        when(typeToolRepo.findByNameAndCategoryAndReplacementValueAndDailyRate(name, category, replacementValue, dailyRate))
                .thenReturn(Optional.empty());

        //When & Then
        assertThatThrownBy(() -> typeToolService.findOrCreateTypeTool(name, category, replacementValue, dailyRate, debtRate))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("El nombre de la herramienta es obligatoria.");
    }


    @Test
    void whenFindOrCreateTypeTool_withEmptyName_thenThrowIllegalStateException() {
        //Given
        String name = "";
        String category = "Herramientas de Mano";
        int replacementValue = 50000;
        int dailyRate = 5000;
        int debtRate = 1000;

        when(typeToolRepo.findByNameAndCategoryAndReplacementValueAndDailyRate(name, category, replacementValue, dailyRate))
                .thenReturn(Optional.empty());

        //When & Then
        assertThatThrownBy(() -> typeToolService.findOrCreateTypeTool(name, category, replacementValue, dailyRate, debtRate))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("El nombre de la herramienta es obligatoria.");
    }


    @Test
    void whenFindOrCreateTypeTool_withNullCategory_thenThrowIllegalStateException() {
        //Given
        String name = "Martillo";
        String category = null;
        int replacementValue = 50000;
        int dailyRate = 5000;
        int debtRate = 1000;

        when(typeToolRepo.findByNameAndCategoryAndReplacementValueAndDailyRate(name, category, replacementValue, dailyRate))
                .thenReturn(Optional.empty());

        //When & Then
        assertThatThrownBy(() -> typeToolService.findOrCreateTypeTool(name, category, replacementValue, dailyRate, debtRate))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("La categoría de la herramienta es obligatoria.");
    }


    @Test
    void whenFindOrCreateTypeTool_withNegativeReplacementValue_thenThrowIllegalStateException() {
        //Given
        String name = "Martillo";
        String category = "Herramientas de Mano";
        int replacementValue = -50000;
        int dailyRate = 5000;
        int debtRate = 1000;

        when(typeToolRepo.findByNameAndCategoryAndReplacementValueAndDailyRate(name, category, replacementValue, dailyRate))
                .thenReturn(Optional.empty());

        //When & Then
        assertThatThrownBy(() -> typeToolService.findOrCreateTypeTool(name, category, replacementValue, dailyRate, debtRate))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("El valor de reposición debe ser un valor positivo.");
    }


    @Test
    void whenFindOrCreateTypeTool_withNegativeDailyRate_thenThrowEntityNotFoundException() {
        //Given
        String name = "Martillo";
        String category = "Herramientas de Mano";
        int replacementValue = 50000;
        int dailyRate = -5000;
        int debtRate = 1000;

        when(typeToolRepo.findByNameAndCategoryAndReplacementValueAndDailyRate(name, category, replacementValue, dailyRate))
                .thenReturn(Optional.empty());

        //When & Then
        assertThatThrownBy(() -> typeToolService.findOrCreateTypeTool(name, category, replacementValue, dailyRate, debtRate))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("La daily rate no puede ser nulo");
    }


    @Test
    void whenFindOrCreateTypeTool_withNegativeDebtRate_thenThrowEntityNotFoundException() {
        //Given
        String name = "Martillo";
        String category = "Herramientas de Mano";
        int replacementValue = 50000;
        int dailyRate = 5000;
        int debtRate = -1000;

        when(typeToolRepo.findByNameAndCategoryAndReplacementValueAndDailyRate(name, category, replacementValue, dailyRate))
                .thenReturn(Optional.empty());

        //When & Then
        assertThatThrownBy(() -> typeToolService.findOrCreateTypeTool(name, category, replacementValue, dailyRate, debtRate))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("La debt rate no puede ser nulo");
    }


    // ==================== Tests for configurationDailyRateTypeTool ====================

    @Test
    void whenConfigurationDailyRateTypeTool_thenUpdateDailyRate() {
        //Given
        long idTypeTool = 1L;
        int newDailyRate = 7000;

        when(typeToolRepo.findById(idTypeTool)).thenReturn(Optional.of(typeTool));
        when(typeToolRepo.save(typeTool)).thenReturn(typeTool);

        //When
        TypeToolEntity result = typeToolService.configurationDailyRateTypeTool(idTypeTool, newDailyRate);

        //Then
        assertThat(result.getDailyRate()).isEqualTo(newDailyRate);
        verify(typeToolRepo, times(1)).save(typeTool);
    }


    @Test
    void whenConfigurationDailyRateTypeTool_withNegativeDailyRate_thenThrowEntityNotFoundException() {
        //Given
        long idTypeTool = 1L;
        int newDailyRate = -5000;

        when(typeToolRepo.findById(idTypeTool)).thenReturn(Optional.of(typeTool));

        //When & Then
        assertThatThrownBy(() -> typeToolService.configurationDailyRateTypeTool(idTypeTool, newDailyRate))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("La tarifa diaria no puede ser nulo, ni menor a 0");
    }


    @Test
    void whenConfigurationDailyRateTypeTool_withNonExistentId_thenThrowEntityNotFoundException() {
        //Given
        long idTypeTool = 999L;
        int newDailyRate = 7000;

        when(typeToolRepo.findById(idTypeTool)).thenReturn(Optional.empty());

        //When & Then
        assertThatThrownBy(() -> typeToolService.configurationDailyRateTypeTool(idTypeTool, newDailyRate))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("No se encontró el tipo de herramienta con id: " + idTypeTool);
    }


    // ==================== Tests for configurationDebtTypeTool ====================

    @Test
    void whenConfigurationDebtTypeTool_thenUpdateDebtRate() {
        //Given
        long idTypeTool = 1L;
        int newDebtRate = 1500;

        when(typeToolRepo.findById(idTypeTool)).thenReturn(Optional.of(typeTool));
        when(typeToolRepo.save(typeTool)).thenReturn(typeTool);

        //When
        TypeToolEntity result = typeToolService.configurationDebtTypeTool(idTypeTool, newDebtRate);

        //Then
        assertThat(result.getDebtRate()).isEqualTo(newDebtRate);
        verify(typeToolRepo, times(1)).save(typeTool);
    }


    @Test
    void whenConfigurationDebtTypeTool_withNonExistentId_thenThrowEntityNotFoundException() {
        //Given
        long idTypeTool = 999L;
        int newDebtRate = 1500;

        when(typeToolRepo.findById(idTypeTool)).thenReturn(Optional.empty());

        //When & Then
        assertThatThrownBy(() -> typeToolService.configurationDebtTypeTool(idTypeTool, newDebtRate))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("No se encontró el tipo de herramienta con id: " + idTypeTool);
    }


    // ==================== Tests for registerReplacementTypeTool ====================

    @Test
    void whenRegisterReplacementTypeTool_thenUpdateReplacementValue() {
        //Given
        long idTypeTool = 1L;
        int newReplacementValue = 75000;

        when(typeToolRepo.findById(idTypeTool)).thenReturn(Optional.of(typeTool));
        when(typeToolRepo.save(typeTool)).thenReturn(typeTool);

        //When
        TypeToolEntity result = typeToolService.registerReplacementTypeTool(idTypeTool, newReplacementValue);

        //Then
        assertThat(result.getReplacementValue()).isEqualTo(newReplacementValue);
        verify(typeToolRepo, times(1)).save(typeTool);
    }


    @Test
    void whenRegisterReplacementTypeTool_withNonExistentId_thenThrowEntityNotFoundException() {
        //Given
        long idTypeTool = 999L;
        int newReplacementValue = 75000;

        when(typeToolRepo.findById(idTypeTool)).thenReturn(Optional.empty());

        //When & Then
        assertThatThrownBy(() -> typeToolService.registerReplacementTypeTool(idTypeTool, newReplacementValue))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("No se encontró el tipo de herramienta con id: " + idTypeTool);
    }


    // ==================== Tests for getTypeToolById ====================

    @Test
    void whenGetTypeToolById_thenReturnTypeTool() {
        //Given
        long idTypeTool = 1L;

        when(typeToolRepo.findById(idTypeTool)).thenReturn(Optional.of(typeTool));

        //When
        TypeToolEntity result = typeToolService.getTypeToolById(idTypeTool);

        //Then
        assertThat(result).isEqualTo(typeTool);
        verify(typeToolRepo, times(1)).findById(idTypeTool);
    }


    @Test
    void whenGetTypeToolById_withNonExistentId_thenThrowEntityNotFoundException() {
        //Given
        long idTypeTool = 999L;

        when(typeToolRepo.findById(idTypeTool)).thenReturn(Optional.empty());

        //When & Then
        assertThatThrownBy(() -> typeToolService.getTypeToolById(idTypeTool))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("No se encontró el tipo de herramienta con id: " + idTypeTool);
    }


    // ==================== Tests for getAllTypeTools ====================

    @Test
    void whenGetAllTypeTools_thenReturnAllTypeTools() {
        //Given
        List<TypeToolEntity> typeTools = new ArrayList<>();
        typeTools.add(typeTool);

        TypeToolEntity typeTool2 = new TypeToolEntity();
        typeTool2.setIdTypeTool(2L);
        typeTool2.setName("Destornillador");
        typeTools.add(typeTool2);

        when(typeToolRepo.findAll()).thenReturn(typeTools);

        //When
        List<TypeToolEntity> result = typeToolService.getAllTypeTools();

        //Then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(typeTool, typeTool2);
        verify(typeToolRepo, times(1)).findAll();
    }


    @Test
    void whenGetAllTypeTools_andRepositoryEmpty_thenReturnEmptyList() {
        //Given
        when(typeToolRepo.findAll()).thenReturn(new ArrayList<>());

        //When
        List<TypeToolEntity> result = typeToolService.getAllTypeTools();

        //Then
        assertThat(result).isEmpty();
        verify(typeToolRepo, times(1)).findAll();
    }


    // ==================== Tests for getAllTypeToolCategory ====================

    @Test
    void whenGetAllTypeToolCategory_thenReturnAllCategories() {
        //Given
        List<String> categories = new ArrayList<>();
        categories.add("Herramientas de Mano");
        categories.add("Herramientas Eléctricas");
        categories.add("Equipo de Seguridad");

        when(typeToolRepo.findDistinctCategory()).thenReturn(categories);

        //When
        List<String> result = typeToolService.getAllTypeToolCategory();

        //Then
        assertThat(result).hasSize(3);
        assertThat(result).contains("Herramientas de Mano", "Herramientas Eléctricas", "Equipo de Seguridad");
        verify(typeToolRepo, times(1)).findDistinctCategory();
    }


    @Test
    void whenGetAllTypeToolCategory_andNoCategoriesExist_thenReturnEmptyList() {
        //Given
        when(typeToolRepo.findDistinctCategory()).thenReturn(new ArrayList<>());

        //When
        List<String> result = typeToolService.getAllTypeToolCategory();

        //Then
        assertThat(result).isEmpty();
        verify(typeToolRepo, times(1)).findDistinctCategory();
    }


}