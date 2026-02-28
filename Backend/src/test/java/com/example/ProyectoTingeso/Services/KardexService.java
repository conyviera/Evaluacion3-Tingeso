package com.example.ProyectoTingeso.Services;

import com.example.ProyectoTingeso.Entities.*;
import com.example.ProyectoTingeso.Repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class KardexServiceTest {

    KardexService kardexService;

    @Mock
    KardexRepository kardexRepo;

    @Mock
    TypeToolRepository typeToolRepository;

    @Mock
    KardexDetailService kardexDetailService;

    @Mock
    ToolRepository toolRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    UserService userService;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        kardexService = new KardexService(kardexRepo, typeToolRepository, kardexDetailService, toolRepository, userRepository, userService);
    }


    @Test
    void whenRegisterLotMovement_thenKardexCreated() {
        //Given
        List<ToolEntity> tools = new ArrayList<>();
        ToolEntity tool1 = new ToolEntity();
        ToolEntity tool2 = new ToolEntity();
        ToolEntity tool3 = new ToolEntity();
        tools.add(tool1);
        tools.add(tool2);
        tools.add(tool3);

        UserEntity user = new UserEntity();
        when(userService.getUser()).thenReturn(user);

        KardexEntity kardexSaved = new KardexEntity();
        kardexSaved.setTypeMove("TOOL_REGISTER");
        kardexSaved.setQuantity(3);
        when(kardexRepo.save(any(KardexEntity.class))).thenReturn(kardexSaved);

        //When
        kardexService.registerLotMovement(tools);

        //Then
        verify(kardexRepo, times(1)).save(any(KardexEntity.class));
        verify(userService, times(1)).getUser();
    }


    @Test
    void whenRegisterLotMovementEmpty_thenNothingHappens() {
        //Given
        List<ToolEntity> tools = new ArrayList<>();

        //When
        kardexService.registerLotMovement(tools);

        //Then
        verify(kardexRepo, never()).save(any(KardexEntity.class));
    }


    @Test
    void whenRegisterLoan_thenKardexAndStockUpdated() {
        //Given
        LoanEntity loan = new LoanEntity();
        List<ToolEntity> tools = new ArrayList<>();

        ToolEntity tool1 = new ToolEntity();
        TypeToolEntity typeTool1 = new TypeToolEntity();
        typeTool1.setStock(5);
        tool1.setTypeTool(typeTool1);

        ToolEntity tool2 = new ToolEntity();
        TypeToolEntity typeTool2 = new TypeToolEntity();
        typeTool2.setStock(5);
        tool2.setTypeTool(typeTool2);

        tools.add(tool1);
        tools.add(tool2);
        loan.setTool(tools);

        UserEntity user = new UserEntity();
        when(userService.getUser()).thenReturn(user);

        KardexEntity kardexSaved = new KardexEntity();
        kardexSaved.setTypeMove("LOAN");
        kardexSaved.setQuantity(2);
        when(kardexRepo.save(any(KardexEntity.class))).thenReturn(kardexSaved);

        //When
        kardexService.registerLoan(loan);

        //Then
        assertThat(typeTool1.getStock()).isEqualTo(4);
        assertThat(typeTool2.getStock()).isEqualTo(4);
        verify(kardexRepo, times(1)).save(any(KardexEntity.class));
    }


    @Test
    void whenRegisterLoanNoTools_thenKardexCreatedWithZeroQuantity() {
        //Given
        LoanEntity loan = new LoanEntity();
        loan.setTool(new ArrayList<>());

        UserEntity user = new UserEntity();
        when(userService.getUser()).thenReturn(user);

        KardexEntity kardexSaved = new KardexEntity();
        kardexSaved.setTypeMove("LOAN");
        kardexSaved.setQuantity(0);
        when(kardexRepo.save(any(KardexEntity.class))).thenReturn(kardexSaved);

        //When
        kardexService.registerLoan(loan);

        //Then
        verify(kardexRepo, times(1)).save(any(KardexEntity.class));
    }


    @Test
    void whenRegisterToolReturn_thenStockIncremented() {
        //Given
        LoanEntity loan = new LoanEntity();
        List<ToolEntity> tools = new ArrayList<>();

        ToolEntity tool = new ToolEntity();
        tool.setState("AVAILABLE");
        TypeToolEntity typeTool = new TypeToolEntity();
        typeTool.setStock(3);
        tool.setTypeTool(typeTool);

        tools.add(tool);
        loan.setTool(tools);

        UserEntity user = new UserEntity();
        when(userService.getUser()).thenReturn(user);

        when(typeToolRepository.save(any(TypeToolEntity.class))).thenReturn(typeTool);

        KardexEntity kardexSaved = new KardexEntity();
        kardexSaved.setTypeMove("TOOL_RETURN");
        when(kardexRepo.save(any(KardexEntity.class))).thenReturn(kardexSaved);

        //When
        kardexService.registerToolReturn(loan);

        //Then
        assertThat(typeTool.getStock()).isEqualTo(4);
        verify(typeToolRepository, times(1)).save(any(TypeToolEntity.class));
    }


    @Test
    void whenRegisterToolReturnNotAvailable_thenStockNotUpdated() {
        //Given
        LoanEntity loan = new LoanEntity();
        List<ToolEntity> tools = new ArrayList<>();

        ToolEntity tool = new ToolEntity();
        tool.setState("DAMAGED");
        TypeToolEntity typeTool = new TypeToolEntity();
        typeTool.setStock(3);
        tool.setTypeTool(typeTool);

        tools.add(tool);
        loan.setTool(tools);

        UserEntity user = new UserEntity();
        when(userService.getUser()).thenReturn(user);

        KardexEntity kardexSaved = new KardexEntity();
        kardexSaved.setTypeMove("TOOL_RETURN");
        when(kardexRepo.save(any(KardexEntity.class))).thenReturn(kardexSaved);

        //When
        kardexService.registerToolReturn(loan);

        //Then
        assertThat(typeTool.getStock()).isEqualTo(3);
        verify(typeToolRepository, never()).save(any(TypeToolEntity.class));
    }


    @Test
    void whenRegisterToolRepair_thenStockIncrementedAndKardexCreated() {
        //Given
        ToolEntity tool = new ToolEntity();
        tool.setState("DAMAGED");
        TypeToolEntity typeTool = new TypeToolEntity();
        typeTool.setStock(2);
        tool.setTypeTool(typeTool);

        UserEntity user = new UserEntity();
        when(userService.getUser()).thenReturn(user);

        KardexEntity kardexSaved = new KardexEntity();
        kardexSaved.setTypeMove("TOOL_REPAIR");
        kardexSaved.setQuantity(1);
        when(kardexRepo.save(any(KardexEntity.class))).thenReturn(kardexSaved);

        //When
        kardexService.registerToolRepair(tool);

        //Then
        assertThat(typeTool.getStock()).isEqualTo(3);
        verify(kardexRepo, times(1)).save(any(KardexEntity.class));
    }


    @Test
    void whenRegisterDecommissioned_thenStockDecrementedAndKardexCreated() {
        //Given
        ToolEntity tool = new ToolEntity();
        TypeToolEntity typeTool = new TypeToolEntity();
        typeTool.setStock(5);
        tool.setTypeTool(typeTool);

        UserEntity user = new UserEntity();
        when(userService.getUser()).thenReturn(user);

        KardexEntity kardexSaved = new KardexEntity();
        kardexSaved.setTypeMove("DECOMMISSIONED");
        kardexSaved.setQuantity(1);
        when(kardexRepo.save(any(KardexEntity.class))).thenReturn(kardexSaved);

        //When
        kardexService.registerDecommissioned(tool);

        //Then
        assertThat(typeTool.getStock()).isEqualTo(4);
        verify(kardexRepo, times(1)).save(any(KardexEntity.class));
    }


    @Test
    void whenGetAllMovementsOfNonExistentTool_thenReturnEmptyList() {
        //Given
        Long toolId = 999L;

        when(toolRepository.findById(toolId)).thenReturn(Optional.empty());

        //When
        List<KardexEntity> movements = kardexService.getAllMovementsOfATool(toolId);

        //Then
        assertThat(movements).isEmpty();
    }


    @Test
    void whenListByDateRange_thenReturnMovementsInRange() {
        //Given
        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 0, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 31, 23, 59, 59);

        List<KardexEntity> kardexList = new ArrayList<>();
        KardexEntity kardex1 = new KardexEntity();
        kardex1.setTypeMove("LOAN");
        kardex1.setDate(LocalDateTime.of(2024, 1, 15, 10, 30, 0));

        KardexEntity kardex2 = new KardexEntity();
        kardex2.setTypeMove("TOOL_RETURN");
        kardex2.setDate(LocalDateTime.of(2024, 1, 20, 14, 45, 0));

        kardexList.add(kardex1);
        kardexList.add(kardex2);

        when(kardexRepo.findByDateBetween(start, end)).thenReturn(kardexList);

        //When
        List<KardexEntity> movements = kardexService.listByDateRange(start, end);

        //Then
        assertThat(movements).hasSize(2);
        assertThat(movements.get(0).getTypeMove()).isEqualTo("LOAN");
        assertThat(movements.get(1).getTypeMove()).isEqualTo("TOOL_RETURN");
    }


    @Test
    void whenListByDateRangeNoData_thenReturnEmptyList() {
        //Given
        LocalDateTime start = LocalDateTime.of(2020, 1, 1, 0, 0, 0);
        LocalDateTime end = LocalDateTime.of(2020, 1, 31, 23, 59, 59);

        when(kardexRepo.findByDateBetween(start, end)).thenReturn(new ArrayList<>());

        //When
        List<KardexEntity> movements = kardexService.listByDateRange(start, end);

        //Then
        assertThat(movements).isEmpty();
    }


    @Test
    void whenFindAllKardex_thenReturnAllMovements() {
        //Given
        List<KardexEntity> kardexList = new ArrayList<>();

        KardexEntity kardex1 = new KardexEntity();
        kardex1.setTypeMove("LOAN");

        KardexEntity kardex2 = new KardexEntity();
        kardex2.setTypeMove("TOOL_RETURN");

        KardexEntity kardex3 = new KardexEntity();
        kardex3.setTypeMove("TOOL_REGISTER");

        KardexEntity kardex4 = new KardexEntity();
        kardex4.setTypeMove("TOOL_REPAIR");

        KardexEntity kardex5 = new KardexEntity();
        kardex5.setTypeMove("DECOMMISSIONED");

        kardexList.add(kardex1);
        kardexList.add(kardex2);
        kardexList.add(kardex3);
        kardexList.add(kardex4);
        kardexList.add(kardex5);

        when(kardexRepo.findAll()).thenReturn(kardexList);

        //When
        List<KardexEntity> allMovements = kardexService.findAllKardex();

        //Then
        assertThat(allMovements).hasSize(5);
    }


    @Test
    void whenFindAllKardexEmpty_thenReturnEmptyList() {
        //Given
        when(kardexRepo.findAll()).thenReturn(new ArrayList<>());

        //When
        List<KardexEntity> allMovements = kardexService.findAllKardex();

        //Then
        assertThat(allMovements).isEmpty();
    }

}
