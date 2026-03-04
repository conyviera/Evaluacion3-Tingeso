package com.example.ProyectoTingeso.Services;

import com.example.ProyectoTingeso.Entities.CustomerEntity;
import com.example.ProyectoTingeso.Entities.DebtsEntity;
import com.example.ProyectoTingeso.Entities.LoanEntity;
import com.example.ProyectoTingeso.Entities.ToolEntity;
import com.example.ProyectoTingeso.Entities.TypeToolEntity;
import com.example.ProyectoTingeso.Repositories.CustomerRepository;
import com.example.ProyectoTingeso.Repositories.DebtsRepository;
import com.example.ProyectoTingeso.Repositories.LoanRepository;
import com.example.ProyectoTingeso.Repositories.ToolRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DebtsService Unit Tests")
class DebtsServiceTest {

    @Mock
    private DebtsRepository debtsRepository;

    @Mock
    private ToolRepository toolRepository;

    @Mock
    private ToolService toolService;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private KardexService kardexService;

    @Mock
    private LoanRepository loanRepository;

    @InjectMocks
    private DebtsService debtsService;

    private CustomerEntity customer;
    private LoanEntity loan;
    private ToolEntity tool;
    private TypeToolEntity typeTool;
    private DebtsEntity debt;

    @BeforeEach
    void setUp() {
        customer = new CustomerEntity();
        customer.setIdCustomer(1L);
        customer.setName("Juan Pérez");
        customer.setRut("12.345.678-9");
        customer.setState("ACTIVE");

        typeTool = new TypeToolEntity();
        typeTool.setIdTypeTool(1L);
        typeTool.setDebtRate(5000);
        typeTool.setReplacementValue(50000);

        tool = new ToolEntity();
        tool.setIdTool(1L);
        tool.setState("AVAILABLE");
        tool.setTypeTool(typeTool);

        loan = new LoanEntity();
        loan.setIdLoan(1L);
        loan.setCustomer(customer);
        loan.setState("ACTIVE");
        loan.setReturnDate(LocalDate.now().minusDays(5));
        loan.setTool(List.of(tool));

        debt = new DebtsEntity();
        debt.setIdDebts(1L);
        debt.setAmount(0);
        debt.setType("ARREARS");
        debt.setCreationDate(LocalDate.now());
        debt.setStatus("PENDING");
        debt.setCustomer(customer);
        debt.setLoan(loan);
    }

    // ==================== RF2.4: calculateDebtForArrears ====================

    @Test
    @DisplayName("Cuando se calcula deuda por atraso con un día de retraso, entonces retorna monto correcto")
    void whenCalculateDebtForArrearsOneDayLate_thenReturnsCorrectAmount() {
        // Given
        loan.setReturnDate(LocalDate.now().minusDays(1));
        LocalDate today = LocalDate.now();
        int expectedDebt = 5000;

        // When
        int result = debtsService.calculateDebtForArrears(loan, today);

        // Then
        assertThat(result).isEqualTo(expectedDebt);
    }

    @Test
    @DisplayName("Cuando se calcula deuda por atraso con múltiples días, entonces retorna suma correcta")
    void whenCalculateDebtForArrearsMultipleDays_thenReturnsSumCorrectly() {
        // Given
        loan.setReturnDate(LocalDate.now().minusDays(5));
        LocalDate today = LocalDate.now();
        int expectedDebt = 25000;

        // When
        int result = debtsService.calculateDebtForArrears(loan, today);

        // Then
        assertThat(result).isEqualTo(expectedDebt);
    }

    @Test
    @DisplayName("Cuando se calcula deuda con múltiples herramientas, entonces suma todas las tasas")
    void whenCalculateDebtMultipleTools_thenSumsAllRates() {
        // Given
        ToolEntity tool2 = new ToolEntity();
        TypeToolEntity typeTool2 = new TypeToolEntity();
        typeTool2.setDebtRate(3000);
        tool2.setTypeTool(typeTool2);

        loan.setTool(List.of(tool, tool2));
        loan.setReturnDate(LocalDate.now().minusDays(2));
        LocalDate today = LocalDate.now();
        int expectedDebt = 16000;

        // When
        int result = debtsService.calculateDebtForArrears(loan, today);

        // Then
        assertThat(result).isEqualTo(expectedDebt);
    }

    // ==================== RF2.2: registerDebts ====================

    @Test
    @DisplayName("Cuando se registra deuda válida, entonces se guarda exitosamente")
    void whenRegisterValidDebt_thenDebtIsSaved() {
        // Given
        int amount = 5000;
        String type = "ARREARS";
        LocalDate creationDate = LocalDate.now();
        String status = "PENDING";
        List<ToolEntity> tools = List.of(tool);

        when(customerRepository.save(any(CustomerEntity.class))).thenReturn(customer);
        when(debtsRepository.save(any(DebtsEntity.class))).thenReturn(debt);

        // When
        DebtsEntity result = debtsService.registerDebts(amount, type, creationDate, status, loan, customer, tools);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAmount()).isEqualTo(amount);
        assertThat(result.getType()).isEqualTo(type);
        assertThat(result.getStatus()).isEqualTo(status);
        assertThat(customer.getState()).isEqualTo("RESTRICTED");

        verify(customerRepository).save(customer);
        verify(debtsRepository).save(any(DebtsEntity.class));
    }

    @Test
    @DisplayName("Cuando el préstamo es nulo, entonces lanza excepción")
    void whenLoanIsNull_thenThrowsIllegalArgumentException() {
        // Given
        List<ToolEntity> tools = List.of(tool);

        // When & Then
        LocalDate today = LocalDate.now();
        assertThatThrownBy(() -> debtsService.registerDebts(5000, "ARREARS", today, "PENDING", null, customer, tools))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Parámetros requeridos nulos.");

        verify(debtsRepository, never()).save(any());
    }

    @Test
    @DisplayName("Cuando el cliente no corresponde al préstamo, entonces lanza excepción")
    void whenCustomerDoesNotMatchLoan_thenThrowsIllegalArgumentException() {
        // Given
        CustomerEntity differentCustomer = new CustomerEntity();
        differentCustomer.setIdCustomer(2L);
        List<ToolEntity> tools = List.of(tool);

        // When & Then
        LocalDate today = LocalDate.now();
        assertThatThrownBy(
                () -> debtsService.registerDebts(5000, "ARREARS", today, "PENDING", loan, differentCustomer, tools))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El cliente no corresponde al préstamo.");

        verify(debtsRepository, never()).save(any());
    }

    @Test
    @DisplayName("Cuando la fecha de creación es nula, entonces lanza excepción")
    void whenCreationDateIsNull_thenThrowsIllegalArgumentException() {
        // Given
        List<ToolEntity> tools = List.of(tool);

        // When & Then
        assertThatThrownBy(() -> debtsService.registerDebts(5000, "ARREARS", null, "PENDING", loan, customer, tools))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Parámetros requeridos nulos.");
    }

    // ==================== RF2.4: debtForArrears ====================

    @Test
    @DisplayName("Cuando se crea deuda por atraso con estado EXPIRED, entonces crea deuda PENDING_RETURN")
    void whenCreateDebtForArrearsExpiredLoan_thenCreatesPendingReturnDebt() {
        // Given
        loan.setState("EXPIRED");
        loan.setReturnDate(LocalDate.now().minusDays(3));
        LocalDate today = LocalDate.now();

        when(debtsRepository.findByLoanAndStatus(loan, "PENDING_RETURN")).thenReturn(Optional.empty());
        when(customerRepository.save(any(CustomerEntity.class))).thenReturn(customer);
        when(debtsRepository.save(any(DebtsEntity.class))).thenReturn(debt);

        // When
        DebtsEntity result = debtsService.debtForArrears(loan, customer, today);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getType()).isEqualTo("ARREARS");
        verify(debtsRepository).save(any(DebtsEntity.class));
    }

    @Test
    @DisplayName("Cuando ya existe deuda PENDING_RETURN, entonces actualiza la existente")
    void whenDebtPendingReturnExists_thenUpdatesExistingDebt() {
        // Given
        DebtsEntity existingDebt = new DebtsEntity();
        existingDebt.setStatus("PENDING_RETURN");
        existingDebt.setAmount(0);
        loan.setReturnDate(LocalDate.now().minusDays(3));
        LocalDate today = LocalDate.now();

        when(debtsRepository.findByLoanAndStatus(loan, "PENDING_RETURN")).thenReturn(Optional.of(existingDebt));
        when(debtsRepository.save(any(DebtsEntity.class))).thenReturn(existingDebt);

        // When
        DebtsEntity result = debtsService.debtForArrears(loan, customer, today);

        // Then
        assertThat(result.getStatus()).isEqualTo("PENDING");
        assertThat(result.getType()).isEqualTo("ARREARS");
        assertThat(result.getAmount()).isGreaterThan(0);
        verify(debtsRepository).save(any(DebtsEntity.class));
    }

    // ==================== RF2.3: debtForToolDamage ====================

    @Test
    @DisplayName("Cuando se crea deuda por daño de herramienta, entonces cambia estado a UNDER_REPAIR")
    void whenCreateDebtForToolDamage_thenToolStateChangesToUnderRepair() {
        // Given
        LocalDate today = LocalDate.now();

        when(toolRepository.save(any(ToolEntity.class))).thenReturn(tool);
        when(customerRepository.save(any(CustomerEntity.class))).thenReturn(customer);
        when(debtsRepository.save(any(DebtsEntity.class))).thenReturn(debt);

        // When
        DebtsEntity result = debtsService.debtForToolDamage(loan, tool, customer, today);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getType()).isEqualTo("DAMAGES");
        assertThat(result.getStatus()).isEqualTo("PENDING_ASSESSMENT");
        assertThat(tool.getState()).isEqualTo("UNDER_REPAIR");

        verify(toolRepository).save(tool);
        verify(debtsRepository).save(any(DebtsEntity.class));
    }

    // ==================== debtPaid ====================

    @Test
    @DisplayName("Cuando se marca deuda como pagada, entonces actualiza estado y fecha")
    void whenMarkDebtAsPaid_thenUpdatesStatusAndPaymentDate() {
        // Given
        Long debtId = 1L;
        debt.setStatus("PENDING");

        when(debtsRepository.findById(debtId)).thenReturn(Optional.of(debt));
        when(debtsRepository.save(any(DebtsEntity.class))).thenReturn(debt);
        when(debtsRepository.existsByCustomerAndStatus(customer, "PENDING")).thenReturn(false);
        when(debtsRepository.existsByCustomerAndStatus(customer, "PENDING_ASSESSMENT")).thenReturn(false);

        // When
        DebtsEntity result = debtsService.debtPaid(debtId);

        // Then
        assertThat(result.getStatus()).isEqualTo("PAID");
        assertThat(result.getPaymentDate()).isEqualTo(LocalDate.now());
        verify(debtsRepository).findById(debtId);
        verify(debtsRepository).save(any(DebtsEntity.class));
    }

    @Test
    @DisplayName("Cuando se intenta pagar deuda inexistente, entonces lanza excepción")
    void whenDebtNotFound_thenThrowsIllegalArgumentException() {
        // Given
        Long invalidId = 999L;

        when(debtsRepository.findById(invalidId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> debtsService.debtPaid(invalidId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No se encontró la deuda");
    }

    // ==================== getDebtsByIdLoan ====================

    @Test
    @DisplayName("Cuando se obtienen deudas por ID de préstamo, entonces retorna lista correcta")
    void whenGetDebtsByIdLoan_thenReturnsCorrectList() {
        // Given
        Long loanId = 1L;
        List<DebtsEntity> debts = List.of(debt);

        when(debtsRepository.findByLoan_IdLoan(loanId)).thenReturn(debts);

        // When
        List<DebtsEntity> result = debtsService.getDebtsByIdLoan(loanId);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result).contains(debt);
        verify(debtsRepository).findByLoan_IdLoan(loanId);
    }

    @Test
    @DisplayName("Cuando no hay deudas para el préstamo, entonces retorna lista vacía")
    void whenNoDebtsForLoan_thenReturnsEmptyList() {
        // Given
        Long loanId = 999L;

        when(debtsRepository.findByLoan_IdLoan(loanId)).thenReturn(new ArrayList<>());

        // When
        List<DebtsEntity> result = debtsService.getDebtsByIdLoan(loanId);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Cuando evalúa con resultado inválido, entonces lanza excepción")
    void whenAssessToolDamageInvalidOutcome_thenThrowsIllegalArgumentException() {
        // Given
        Long toolId = 1L;
        String outcome = "INVALID";
        int damageCharge = 5000;

        DebtsEntity damageDebt = new DebtsEntity();
        damageDebt.setStatus("PENDING_ASSESSMENT");
        damageDebt.setType("DAMAGES");

        when(toolRepository.findById(toolId)).thenReturn(Optional.of(tool));
        when(debtsRepository.findByToolAndStatusAndType(tool, "PENDING_ASSESSMENT", "DAMAGES"))
                .thenReturn(Optional.of(damageDebt));

        // When & Then
        assertThatThrownBy(() -> debtsService.assessToolDamage(toolId, outcome, damageCharge))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Resultado inválido");
    }

    @Test
    @DisplayName("Cuando tool ID es nulo, entonces lanza excepción")
    void whenToolIdIsNull_thenThrowsIllegalArgumentException() {
        // When & Then
        assertThatThrownBy(() -> debtsService.assessToolDamage(null, "MINOR_DAMAGE", 5000))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Los datos no son válidos.");
    }

    // ==================== refreshCustomerState ====================

    @Test
    @DisplayName("Cuando cliente tiene deudas PENDING, entonces cambia a RESTRICTED")
    void whenCustomerHasPendingDebts_thenChangesToRestricted() {
        // Given
        when(debtsRepository.existsByCustomerAndStatus(customer, "PENDING")).thenReturn(true);
        when(debtsRepository.existsByCustomerAndStatus(customer, "PENDING_ASSESSMENT")).thenReturn(false);
        when(customerRepository.save(any(CustomerEntity.class))).thenReturn(customer);

        // When
        debtsService.refreshCustomerState(customer);

        // Then
        assertThat(customer.getState()).isEqualTo("RESTRICTED");
        verify(customerRepository).save(customer);
    }

    @Test
    @DisplayName("Cuando cliente no tiene deudas pendientes, entonces cambia a ACTIVE")
    void whenCustomerHasNoDebts_thenChangesToActive() {
        // Given
        customer.setState("RESTRICTED");
        when(debtsRepository.existsByCustomerAndStatus(customer, "PENDING")).thenReturn(false);
        when(debtsRepository.existsByCustomerAndStatus(customer, "PENDING_ASSESSMENT")).thenReturn(false);
        when(customerRepository.save(any(CustomerEntity.class))).thenReturn(customer);

        // When
        debtsService.refreshCustomerState(customer);

        // Then
        assertThat(customer.getState()).isEqualTo("ACTIVE");
        verify(customerRepository).save(customer);
    }

    @Test
    @DisplayName("Cuando cliente tiene deudas PENDING_ASSESSMENT, entonces se mantiene RESTRICTED")
    void whenCustomerHasAssessmentDebts_thenStaysRestricted() {
        // Given
        customer.setState("ACTIVE");
        when(debtsRepository.existsByCustomerAndStatus(customer, "PENDING")).thenReturn(false);
        when(debtsRepository.existsByCustomerAndStatus(customer, "PENDING_ASSESSMENT")).thenReturn(true);
        when(customerRepository.save(any(CustomerEntity.class))).thenReturn(customer);

        // When
        debtsService.refreshCustomerState(customer);

        // Then
        assertThat(customer.getState()).isEqualTo("RESTRICTED");
        verify(customerRepository).save(customer);
    }
}
