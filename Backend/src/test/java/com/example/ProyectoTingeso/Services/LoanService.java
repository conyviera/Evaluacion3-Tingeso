package com.example.ProyectoTingeso.Services;

import com.example.ProyectoTingeso.Entities.*;
import com.example.ProyectoTingeso.Repositories.DebtsRepository;
import com.example.ProyectoTingeso.Repositories.LoanRepository;
import com.example.ProyectoTingeso.Repositories.ToolRepository;
import com.example.ProyectoTingeso.Repositories.TypeToolRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    LoanService loanService;

    @Mock
    ToolRepository toolRepo;

    @Mock
    LoanRepository loanRepo;

    @Mock
    DebtsRepository debtsRepo;

    @Mock
    DebtsService debtsService;

    @Mock
    KardexService kardexService;

    @Mock
    TypeToolRepository typeToolRepo;

    @Mock
    CustomerService customerService;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        loanService = new LoanService(toolRepo, loanRepo, debtsRepo, debtsService, kardexService, typeToolRepo, customerService);
    }


    @Test
    void whenCreateLoanWithValidData_thenLoanCreated() {
        //Given
        CustomerEntity customer = new CustomerEntity();
        customer.setRut("12.345.678-9");
        customer.setState("ACTIVE");

        LocalDate deliveryDate = LocalDate.of(2024, 1, 15);
        LocalDate returnDate = LocalDate.of(2024, 1, 22);

        List<Long> typeToolId = List.of(1L, 2L);

        TypeToolEntity typeTool1 = new TypeToolEntity();
        typeTool1.setIdTypeTool(1L);
        typeTool1.setDailyRate(5000);
        typeTool1.setStock(10);

        TypeToolEntity typeTool2 = new TypeToolEntity();
        typeTool2.setIdTypeTool(2L);
        typeTool2.setDailyRate(3000);
        typeTool2.setStock(5);

        ToolEntity tool1 = new ToolEntity();
        tool1.setIdTool(1L);
        tool1.setState("AVAILABLE");
        tool1.setTypeTool(typeTool1);

        ToolEntity tool2 = new ToolEntity();
        tool2.setIdTool(2L);
        tool2.setState("AVAILABLE");
        tool2.setTypeTool(typeTool2);

        LoanEntity loanSaved = new LoanEntity();
        loanSaved.setIdLoan(1L);
        loanSaved.setDeliveryDate(deliveryDate);
        loanSaved.setReturnDate(returnDate);
        loanSaved.setCustomer(customer);
        loanSaved.setState("ACTIVE");
        loanSaved.setRentalAmount(56000);
        loanSaved.setTool(List.of(tool1, tool2));

        when(typeToolRepo.findById(1L)).thenReturn(Optional.of(typeTool1));
        when(typeToolRepo.findById(2L)).thenReturn(Optional.of(typeTool2));
        when(toolRepo.findFirstByStateAndTypeTool("AVAILABLE", typeTool1)).thenReturn(Optional.of(tool1));
        when(toolRepo.findFirstByStateAndTypeTool("AVAILABLE", typeTool2)).thenReturn(Optional.of(tool2));
        when(loanRepo.save(any(LoanEntity.class))).thenReturn(loanSaved);
        when(loanRepo.findByCustomerAndState(customer, "EXPIRED")).thenReturn(new ArrayList<>());
        when(loanRepo.findByCustomerAndState(customer, "ACTIVE")).thenReturn(new ArrayList<>());

        //When
        LoanEntity result = loanService.createLoan(typeToolId, customer, deliveryDate, returnDate);

        //Then
        assertThat(result).isNotNull();
        assertThat(result.getState()).isEqualTo("ACTIVE");
        assertThat(result.getTool()).hasSize(2);
        verify(loanRepo, times(1)).save(any(LoanEntity.class));
        verify(kardexService, times(1)).registerLoan(any(LoanEntity.class));
    }


    @Test
    void whenCreateLoanWithRestrictedCustomer_thenThrowException() {
        //Given
        CustomerEntity customer = new CustomerEntity();
        customer.setRut("98.765.432-1");
        customer.setState("RESTRICTED");

        LocalDate deliveryDate = LocalDate.of(2024, 1, 15);
        LocalDate returnDate = LocalDate.of(2024, 1, 22);

        List<Long> typeToolId = List.of(1L);

        //When & Then
        assertThatThrownBy(() -> loanService.createLoan(typeToolId, customer, deliveryDate, returnDate))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("El cliente esta restringido");
    }


    @Test
    void whenCreateLoanWithReturnDateBeforeDeliveryDate_thenThrowException() {
        //Given
        CustomerEntity customer = new CustomerEntity();
        customer.setRut("12.345.678-9");
        customer.setState("ACTIVE");

        LocalDate deliveryDate = LocalDate.of(2024, 1, 22);
        LocalDate returnDate = LocalDate.of(2024, 1, 15);

        List<Long> typeToolId = List.of(1L);

        //When & Then
        assertThatThrownBy(() -> loanService.createLoan(typeToolId, customer, deliveryDate, returnDate))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("La fecha de retorno debe ser posterior");
    }


    @Test
    void whenToolReturnWithGoodState_thenLoanReturned() {
        //Given
        LoanEntity loan = new LoanEntity();
        loan.setIdLoan(1L);
        loan.setState("ACTIVE");
        loan.setReturnDate(LocalDate.of(2024, 1, 22));

        ToolEntity tool = new ToolEntity();
        tool.setIdTool(1L);
        tool.setState("ON_LOAN");

        loan.setTool(List.of(tool));

        CustomerEntity customer = new CustomerEntity();
        customer.setRut("12.345.678-9");
        loan.setCustomer(customer);

        Map<Long, String> toolStates = new HashMap<>();
        toolStates.put(1L, "GOOD");

        LoanEntity loanSaved = new LoanEntity();
        loanSaved.setIdLoan(1L);
        loanSaved.setState("RETURNED");
        loanSaved.setTool(List.of(tool));

        when(loanRepo.findById(1L)).thenReturn(Optional.of(loan));
        when(loanRepo.save(any(LoanEntity.class))).thenReturn(loanSaved);

        //When
        LoanEntity result = loanService.toolReturn(1L, toolStates);

        //Then
        assertThat(result.getState()).isEqualTo("RETURNED");
        verify(loanRepo, times(1)).save(any(LoanEntity.class));
        verify(kardexService, times(1)).registerToolReturn(any(LoanEntity.class));
    }


    @Test
    void whenToolReturnWithDamagedState_thenDebtCreated() {
        //Given
        LoanEntity loan = new LoanEntity();
        loan.setIdLoan(1L);
        loan.setState("ACTIVE");
        loan.setReturnDate(LocalDate.of(2024, 1, 22));

        ToolEntity tool = new ToolEntity();
        tool.setIdTool(1L);
        tool.setState("ON_LOAN");

        loan.setTool(List.of(tool));

        CustomerEntity customer = new CustomerEntity();
        customer.setRut("12.345.678-9");
        loan.setCustomer(customer);

        Map<Long, String> toolStates = new HashMap<>();
        toolStates.put(1L, "DAMAGED");

        LoanEntity loanSaved = new LoanEntity();
        loanSaved.setIdLoan(1L);
        loanSaved.setState("RETURNED");
        loanSaved.setTool(List.of(tool));

        when(loanRepo.findById(1L)).thenReturn(Optional.of(loan));
        when(loanRepo.save(any(LoanEntity.class))).thenReturn(loanSaved);

        //When
        LoanEntity result = loanService.toolReturn(1L, toolStates);

        //Then
        assertThat(result.getState()).isEqualTo("RETURNED");
        verify(debtsService, times(1)).debtForToolDamage(any(LoanEntity.class), any(ToolEntity.class), any(CustomerEntity.class), any(LocalDate.class));
    }


    @Test
    void whenToolReturnAlreadyReturned_thenThrowException() {
        //Given
        LoanEntity loan = new LoanEntity();
        loan.setIdLoan(1L);
        loan.setState("RETURNED");

        Map<Long, String> toolStates = new HashMap<>();
        toolStates.put(1L, "GOOD");

        when(loanRepo.findById(1L)).thenReturn(Optional.of(loan));

        //When & Then
        assertThatThrownBy(() -> loanService.toolReturn(1L, toolStates))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("El prestamo ya fue devuelto");
    }


    @Test
    void whenRentalAmountCalculation_thenCorrectAmountReturned() {
        //Given
        List<Long> typeToolId = List.of(1L, 2L);
        LocalDate deliveryDate = LocalDate.of(2024, 1, 1);
        LocalDate returnDate = LocalDate.of(2024, 1, 8);

        TypeToolEntity typeTool1 = new TypeToolEntity();
        typeTool1.setIdTypeTool(1L);
        typeTool1.setDailyRate(5000);

        TypeToolEntity typeTool2 = new TypeToolEntity();
        typeTool2.setIdTypeTool(2L);
        typeTool2.setDailyRate(3000);

        when(typeToolRepo.findById(1L)).thenReturn(Optional.of(typeTool1));
        when(typeToolRepo.findById(2L)).thenReturn(Optional.of(typeTool2));

        //When
        int result = loanService.RentalAmount(typeToolId, deliveryDate, returnDate);

        //Then
        int expectedDays = 8;
        int expectedAmount = (5000 + 3000) * expectedDays;
        assertThat(result).isEqualTo(expectedAmount);
    }


    @Test
    void whenLoanActiveAndExpire_thenReturnActiveLoanList() {
        //Given
        LoanEntity loan1 = new LoanEntity();
        loan1.setIdLoan(1L);
        loan1.setState("ACTIVE");

        LoanEntity loan2 = new LoanEntity();
        loan2.setIdLoan(2L);
        loan2.setState("EXPIRED");

        List<LoanEntity> loanList = List.of(loan1, loan2);

        when(loanRepo.findByStateIsNot("RETURNED")).thenReturn(loanList);

        //When
        List result = loanService.loanActiveAndExpire();

        //Then
        assertThat(result).hasSize(2);
        verify(loanRepo, times(1)).findByStateIsNot("RETURNED");
    }


    @Test
    void whenGetTopToolsReport_thenReturnToolsReport() {
        //Given
        Object[] row1 = {"Taladro", 15L};
        Object[] row2 = {"Martillo", 12L};
        Object[] row3 = {"Sierra", 8L};

        List<Object[]> results = List.of(row1, row2, row3);

        when(loanRepo.countLoansByType()).thenReturn(results);

        //When
        List<Map<String, Object>> report = loanService.getTopToolsReport();

        //Then
        assertThat(report).hasSize(3);
        assertThat(report.get(0).get("toolName")).isEqualTo("Taladro");
        assertThat(report.get(0).get("usageCount")).isEqualTo(15L);
        assertThat(report.get(1).get("toolName")).isEqualTo("Martillo");
        verify(loanRepo, times(1)).countLoansByType();
    }


    @Test
    void whenFindByAll_thenReturnAllLoans() {
        //Given
        LoanEntity loan1 = new LoanEntity();
        loan1.setIdLoan(1L);

        LoanEntity loan2 = new LoanEntity();
        loan2.setIdLoan(2L);

        List<LoanEntity> loanList = List.of(loan1, loan2);

        when(loanRepo.findAll()).thenReturn(loanList);

        //When
        List<LoanEntity> result = loanService.findByAll();

        //Then
        assertThat(result).hasSize(2);
        verify(loanRepo, times(1)).findAll();
    }

}
