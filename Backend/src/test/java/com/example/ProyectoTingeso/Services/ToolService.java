package com.example.ProyectoTingeso.Services;

import com.example.ProyectoTingeso.Entities.ToolEntity;
import com.example.ProyectoTingeso.Entities.TypeToolEntity;
import com.example.ProyectoTingeso.Repositories.ToolRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


class ToolServiceTest {

    @Mock
    private ToolRepository toolRepo;

    @Mock
    private TypeToolService typeToolService;

    @Mock
    private KardexService kardexService;

    @Mock
    private TypeToolRepository typeToolRepository;

    @InjectMocks
    private ToolService toolService;

    private ToolEntity toolEntity;
    private TypeToolEntity typeToolEntity;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        typeToolEntity = new TypeToolEntity();
        typeToolEntity.setIdTypeTool(1L);
        typeToolEntity.setName("Martillo");
        typeToolEntity.setCategory("Herramientas de Mano");
        typeToolEntity.setReplacementValue(50000);
        typeToolEntity.setDailyRate(5000);
        typeToolEntity.setDebtRate(1000);
        typeToolEntity.setStock(10);

        toolEntity = new ToolEntity();
        toolEntity.setIdTool(1L);
        toolEntity.setTypeTool(typeToolEntity);
        toolEntity.setState("AVAILABLE");
    }


    // ==================== Tests for registerTool ====================

    @Test
    void whenRegisterTool_thenCreateToolWithAVAILABLEState() {
        //Given
        String name = "Martillo";
        String category = "Herramientas de Mano";
        int replacementValue = 50000;
        int dailyRate = 5000;
        int debtRate = 1000;

        when(typeToolService.findOrCreateTypeTool(name, category, replacementValue, dailyRate, debtRate))
                .thenReturn(typeToolEntity);
        when(toolRepo.save(any(ToolEntity.class))).thenReturn(toolEntity);

        //When
        ToolEntity result = toolService.registerTool(name, category, replacementValue, dailyRate, debtRate);

        //Then
        assertThat(result.getState()).isEqualTo("AVAILABLE");
        assertThat(result.getTypeTool()).isEqualTo(typeToolEntity);
        verify(typeToolService, times(1)).findOrCreateTypeTool(name, category, replacementValue, dailyRate, debtRate);
        verify(toolRepo, times(1)).save(any(ToolEntity.class));
    }


    @Test
    void whenRegisterTool_thenStockIncrementsbyOne() {
        //Given
        String name = "Martillo";
        String category = "Herramientas de Mano";
        int replacementValue = 50000;
        int dailyRate = 5000;
        int debtRate = 1000;
        int initialStock = typeToolEntity.getStock();

        when(typeToolService.findOrCreateTypeTool(name, category, replacementValue, dailyRate, debtRate))
                .thenReturn(typeToolEntity);
        when(toolRepo.save(any(ToolEntity.class))).thenReturn(toolEntity);

        //When
        toolService.registerTool(name, category, replacementValue, dailyRate, debtRate);

        //Then
        assertThat(typeToolEntity.getStock()).isEqualTo(initialStock + 1);
        verify(typeToolService, times(1)).findOrCreateTypeTool(name, category, replacementValue, dailyRate, debtRate);
    }


    // ==================== Tests for unsubscribeTool ====================

    @Test
    void whenUnsubscribeTool_thenChangeStateToDecommissioned() {
        //Given
        toolEntity.setState("AVAILABLE");

        when(toolRepo.save(toolEntity)).thenReturn(toolEntity);

        //When
        ToolEntity result = toolService.unsubscribeTool(toolEntity);

        //Then
        assertThat(result.getState()).isEqualTo("DECOMMISSIONED");
        verify(kardexService, times(1)).registerDecommissioned(toolEntity);
        verify(toolRepo, times(1)).save(toolEntity);
    }


    // ==================== Tests for deactivateUnusedTool ====================

    @Test
    void whenDeactivateUnusedTool_andToolIsAvailable_thenDecommissionTool() {
        //Given
        Long idTool = 1L;
        toolEntity.setState("AVAILABLE");

        when(toolRepo.findById(idTool)).thenReturn(Optional.of(toolEntity));
        when(toolRepo.save(toolEntity)).thenReturn(toolEntity);

        //When
        ToolEntity result = toolService.deactivateUnusedTool(idTool);

        //Then
        assertThat(result.getState()).isEqualTo("DECOMMISSIONED");
        verify(toolRepo, times(1)).findById(idTool);
        verify(kardexService, times(1)).registerDecommissioned(toolEntity);
    }


    @Test
    void whenDeactivateUnusedTool_andToolNotAvailable_thenThrowIllegalStateException() {
        //Given
        Long idTool = 1L;
        toolEntity.setState("ON_LOAN");

        when(toolRepo.findById(idTool)).thenReturn(Optional.of(toolEntity));

        //When & Then
        assertThatThrownBy(() -> toolService.deactivateUnusedTool(idTool))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("La herramienta esta en uso actualmente " + idTool);

        verify(toolRepo, never()).save(any());
    }


    @Test
    void whenDeactivateUnusedTool_withNonExistentId_thenThrowEntityNotFoundException() {
        //Given
        Long idTool = 999L;

        when(toolRepo.findById(idTool)).thenReturn(Optional.empty());

        //When & Then
        assertThatThrownBy(() -> toolService.deactivateUnusedTool(idTool))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Herramienta no encontrada: " + idTool);

        verify(toolRepo, never()).save(any());
    }


    // ==================== Tests for discardDamagedTools ====================

    @Test
    void whenDiscardDamagedTools_andToolIsUnderRepair_thenDecommissionTool() {
        //Given
        Long idTool = 1L;
        toolEntity.setState("UNDER_REPAIR");

        when(toolRepo.findById(idTool)).thenReturn(Optional.of(toolEntity));
        when(toolRepo.save(toolEntity)).thenReturn(toolEntity);

        //When
        ToolEntity result = toolService.discardDamagedTools(idTool);

        //Then
        assertThat(result.getState()).isEqualTo("DECOMMISSIONED");
        verify(toolRepo, times(1)).findById(idTool);
        verify(kardexService, times(1)).registerDecommissioned(toolEntity);
    }


    @Test
    void whenDiscardDamagedTools_andToolNotUnderRepair_thenThrowIllegalStateException() {
        //Given
        Long idTool = 1L;
        toolEntity.setState("AVAILABLE");

        when(toolRepo.findById(idTool)).thenReturn(Optional.of(toolEntity));

        //When & Then
        assertThatThrownBy(() -> toolService.discardDamagedTools(idTool))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("La herramienta no esta en reparación " + idTool);

        verify(toolRepo, never()).save(any());
    }


    @Test
    void whenDiscardDamagedTools_withNonExistentId_thenThrowEntityNotFoundException() {
        //Given
        Long idTool = 999L;

        when(toolRepo.findById(idTool)).thenReturn(Optional.empty());

        //When & Then
        assertThatThrownBy(() -> toolService.discardDamagedTools(idTool))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Herramienta no encontrada: " + idTool);

        verify(toolRepo, never()).save(any());
    }


    // ==================== Tests for registerLotTool ====================

    @Test
    void whenRegisterLotTool_thenCreateMultipleTools() {
        //Given
        String name = "Martillo";
        String category = "Herramientas de Mano";
        int replacementValue = 50000;
        int dailyRate = 5000;
        int debtRate = 1000;
        int amount = 5;

        when(typeToolService.findOrCreateTypeTool(name, category, replacementValue, dailyRate, debtRate))
                .thenReturn(typeToolEntity);
        when(toolRepo.save(any(ToolEntity.class))).thenReturn(toolEntity);

        //When
        List<ToolEntity> result = toolService.registerLotTool(name, category, replacementValue, dailyRate, debtRate, amount);

        //Then
        assertThat(result).hasSize(amount);
        verify(typeToolService, times(amount)).findOrCreateTypeTool(name, category, replacementValue, dailyRate, debtRate);
        verify(toolRepo, times(amount)).save(any(ToolEntity.class));
        verify(kardexService, times(1)).registerLotMovement(result);
    }


    @Test
    void whenRegisterLotTool_withAmountZero_thenReturnEmptyList() {
        //Given
        String name = "Martillo";
        String category = "Herramientas de Mano";
        int replacementValue = 50000;
        int dailyRate = 5000;
        int debtRate = 1000;
        int amount = 0;

        //When
        List<ToolEntity> result = toolService.registerLotTool(name, category, replacementValue, dailyRate, debtRate, amount);

        //Then
        assertThat(result).isEmpty();
        verify(toolRepo, never()).save(any());
        verify(kardexService, times(1)).registerLotMovement(result);
    }


    // ==================== Tests for findAllByTypeTool ====================

    @Test
    void whenFindAllByTypeTool_thenReturnAllToolsOfType() {
        //Given
        Long idTypeTool = 1L;

        List<ToolEntity> toolList = new ArrayList<>();
        toolList.add(toolEntity);

        ToolEntity tool2 = new ToolEntity();
        tool2.setIdTool(2L);
        tool2.setTypeTool(typeToolEntity);
        tool2.setState("ON_LOAN");
        toolList.add(tool2);

        when(typeToolRepository.findById(idTypeTool)).thenReturn(Optional.of(typeToolEntity));
        when(toolRepo.findAllByTypeTool(typeToolEntity)).thenReturn(toolList);

        //When
        List<ToolEntity> result = toolService.findAllByTypeTool(idTypeTool);

        //Then
        assertThat(result).hasSize(2);
        assertThat(result).contains(toolEntity, tool2);
        verify(typeToolRepository, times(1)).findById(idTypeTool);
        verify(toolRepo, times(1)).findAllByTypeTool(typeToolEntity);
    }


    @Test
    void whenFindAllByTypeTool_withNonExistentTypeId_thenThrowEntityNotFoundException() {
        //Given
        Long idTypeTool = 999L;

        when(typeToolRepository.findById(idTypeTool)).thenReturn(Optional.empty());

        //When & Then
        assertThatThrownBy(() -> toolService.findAllByTypeTool(idTypeTool))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("El Tipo de Herramienta con ID " + idTypeTool + " no existe.");

        verify(toolRepo, never()).findAllByTypeTool(any());
    }


    @Test
    void whenFindAllByTypeTool_andNoToolsExist_thenReturnEmptyList() {
        //Given
        Long idTypeTool = 1L;

        when(typeToolRepository.findById(idTypeTool)).thenReturn(Optional.of(typeToolEntity));
        when(toolRepo.findAllByTypeTool(typeToolEntity)).thenReturn(new ArrayList<>());

        //When
        List<ToolEntity> result = toolService.findAllByTypeTool(idTypeTool);

        //Then
        assertThat(result).isEmpty();
        verify(typeToolRepository, times(1)).findById(idTypeTool);
        verify(toolRepo, times(1)).findAllByTypeTool(typeToolEntity);
    }


    // ==================== Tests for findToolById ====================

    @Test
    void whenFindToolById_thenReturnTool() {
        //Given
        Long idTool = 1L;

        when(toolRepo.findById(idTool)).thenReturn(Optional.of(toolEntity));

        //When
        ToolEntity result = toolService.findToolById(idTool);

        //Then
        assertThat(result).isEqualTo(toolEntity);
        verify(toolRepo, times(1)).findById(idTool);
    }


    @Test
    void whenFindToolById_withNonExistentId_thenThrowEntityNotFoundException() {
        //Given
        Long idTool = 999L;

        when(toolRepo.findById(idTool)).thenReturn(Optional.empty());

        //When & Then
        assertThatThrownBy(() -> toolService.findToolById(idTool))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Herramienta no encontrada: " + idTool);

        verify(toolRepo, times(1)).findById(idTool);
    }

}
