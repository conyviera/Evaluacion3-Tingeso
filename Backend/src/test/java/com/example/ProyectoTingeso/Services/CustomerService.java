package com.example.ProyectoTingeso.Services;

import com.example.ProyectoTingeso.Entities.CustomerEntity;
import com.example.ProyectoTingeso.Entities.DebtsEntity;
import com.example.ProyectoTingeso.Entities.LoanEntity;
import com.example.ProyectoTingeso.Repositories.CustomerRepository;
import com.example.ProyectoTingeso.Repositories.LoanRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomerService Unit Tests")
class CustomerServiceTest {

        @Mock
        private CustomerRepository customerRepository;

        @Mock
        private LoanRepository loanRepository;

        @Mock
        private DebtsService debtsService;

        @InjectMocks
        private CustomerService customerService;

        private CustomerEntity customer;
        private LoanEntity loan;

        @BeforeEach
        void setUp() {
                customer = new CustomerEntity();
                customer.setIdCustomer(1L);
                customer.setName("Juan Pérez");
                customer.setPhoneNumber("987654321");
                customer.setEmail("juan@example.com");
                customer.setRut("12.345.678-9");
                customer.setState("ACTIVE");

                loan = new LoanEntity();
                loan.setIdLoan(1L);
                loan.setCustomer(customer);
                loan.setState("ACTIVE");
                loan.setReturnDate(LocalDate.now().plusDays(10));
        }

        // ==================== RF3.1: registerCustomer ====================

        @Test
        @DisplayName("Cuando se registra un cliente válido, entonces se guarda exitosamente")
        void whenRegisterValidCustomer_thenCustomerIsSaved() {
                // Given
                String name = "Carlos López";
                String phoneNumber = "912345678";
                String email = "carlos@example.com";
                String rut = "13.777.548-2";

                CustomerEntity newCustomer = new CustomerEntity();
                newCustomer.setName(name);
                newCustomer.setPhoneNumber(phoneNumber);
                newCustomer.setEmail(email);
                newCustomer.setRut(rut);
                newCustomer.setState("ACTIVE");

                when(customerRepository.existsByRut(rut)).thenReturn(false);
                when(customerRepository.save(any(CustomerEntity.class))).thenReturn(newCustomer);

                // When
                CustomerEntity result = customerService.registerCustomer(name, phoneNumber, email, rut);

                // Then
                assertThat(result).isNotNull();
                assertThat(result.getName()).isEqualTo(name);
                assertThat(result.getPhoneNumber()).isEqualTo(phoneNumber);
                assertThat(result.getEmail()).isEqualTo(email);
                assertThat(result.getRut()).isEqualTo(rut);
                assertThat(result.getState()).isEqualTo("ACTIVE");

                verify(customerRepository).existsByRut(rut);
                verify(customerRepository).save(any(CustomerEntity.class));
        }

        @Test
        @DisplayName("Cuando el nombre es nulo, entonces lanza excepción")
        void whenNameIsNull_thenThrowsIllegalStateException() {
                // Given
                String name = null;
                String phoneNumber = "912345678";
                String email = "carlos@example.com";
                String rut = "13.777.548-2";

                // When & Then
                assertThatThrownBy(() -> customerService.registerCustomer(name, phoneNumber, email, rut))
                                .isInstanceOf(IllegalStateException.class)
                                .hasMessage("El nombre es obligatorio.");

                verify(customerRepository, never()).save(any());
        }

        @Test
        @DisplayName("Cuando el nombre está vacío, entonces lanza excepción")
        void whenNameIsEmpty_thenThrowsIllegalStateException() {
                // Given
                String name = "";
                String phoneNumber = "912345678";
                String email = "carlos@example.com";
                String rut = "13.777.548-2";

                // When & Then
                assertThatThrownBy(() -> customerService.registerCustomer(name, phoneNumber, email, rut))
                                .isInstanceOf(IllegalStateException.class)
                                .hasMessage("El nombre es obligatorio.");
        }

        @Test
        @DisplayName("Cuando el teléfono es nulo, entonces lanza excepción")
        void whenPhoneNumberIsNull_thenThrowsIllegalStateException() {
                // Given
                String name = "Carlos López";
                String phoneNumber = null;
                String email = "carlos@example.com";
                String rut = "13.777.548-2";

                // When & Then
                assertThatThrownBy(() -> customerService.registerCustomer(name, phoneNumber, email, rut))
                                .isInstanceOf(IllegalStateException.class)
                                .hasMessage("El numero de telefono es obligatorio.");

                verify(customerRepository, never()).save(any());
        }

        @Test
        @DisplayName("Cuando el teléfono está vacío, entonces lanza excepción")
        void whenPhoneNumberIsEmpty_thenThrowsIllegalStateException() {
                // Given
                String name = "Carlos López";
                String phoneNumber = "";
                String email = "carlos@example.com";
                String rut = "13.777.548-2";

                // When & Then
                assertThatThrownBy(() -> customerService.registerCustomer(name, phoneNumber, email, rut))
                                .isInstanceOf(IllegalStateException.class)
                                .hasMessage("El numero de telefono es obligatorio.");
        }

        @Test
        @DisplayName("Cuando el RUT es nulo, entonces lanza excepción")
        void whenRutIsNull_thenThrowsIllegalStateException() {
                // Given
                String name = "Carlos López";
                String phoneNumber = "912345678";
                String email = "carlos@example.com";
                String rut = null;

                // When & Then
                assertThatThrownBy(() -> customerService.registerCustomer(name, phoneNumber, email, rut))
                                .isInstanceOf(IllegalStateException.class)
                                .hasMessage("El  rut es obligatorio.");

                verify(customerRepository, never()).save(any());
        }

        @Test
        @DisplayName("Cuando el RUT está vacío, entonces lanza excepción")
        void whenRutIsEmpty_thenThrowsIllegalStateException() {
                // Given
                String name = "Carlos López";
                String phoneNumber = "912345678";
                String email = "carlos@example.com";
                String rut = "";

                // When & Then
                assertThatThrownBy(() -> customerService.registerCustomer(name, phoneNumber, email, rut))
                                .isInstanceOf(IllegalStateException.class)
                                .hasMessage("El  rut es obligatorio.");
        }

        @Test
        @DisplayName("Cuando el RUT ya existe, entonces lanza excepción")
        void whenRutAlreadyExists_thenThrowsIllegalStateException() {
                // Given
                String name = "Carlos López";
                String phoneNumber = "912345678";
                String email = "carlos@example.com";
                String rut = "13.777.548-2";

                when(customerRepository.existsByRut(rut)).thenReturn(true);

                // When & Then
                assertThatThrownBy(() -> customerService.registerCustomer(name, phoneNumber, email, rut))
                                .isInstanceOf(IllegalStateException.class)
                                .hasMessage("El rut ya existe");

                verify(customerRepository).existsByRut(rut);
                verify(customerRepository, never()).save(any());
        }

        @Test
        @DisplayName("Cuando el email es nulo, entonces lanza excepción")
        void whenEmailIsNull_thenThrowsIllegalStateException() {
                // Given
                String name = "Carlos López";
                String phoneNumber = "912345678";
                String email = null;
                String rut = "13.777.548-2";

                when(customerRepository.existsByRut(rut)).thenReturn(false);

                // When & Then
                assertThatThrownBy(() -> customerService.registerCustomer(name, phoneNumber, email, rut))
                                .isInstanceOf(IllegalStateException.class)
                                .hasMessage("El correo es obligatorio.");

                verify(customerRepository, never()).save(any());
        }

        @Test
        @DisplayName("Cuando el email está vacío, entonces lanza excepción")
        void whenEmailIsEmpty_thenThrowsIllegalStateException() {
                // Given
                String name = "Carlos López";
                String phoneNumber = "912345678";
                String email = "";
                String rut = "13.777.548-2";

                when(customerRepository.existsByRut(rut)).thenReturn(false);

                // When & Then
                assertThatThrownBy(() -> customerService.registerCustomer(name, phoneNumber, email, rut))
                                .isInstanceOf(IllegalStateException.class)
                                .hasMessage("El correo es obligatorio.");
        }

        // ==================== RF2.5: customerLoanExpired ====================

        @Test
        @DisplayName("Cuando cliente tiene préstamos expirados, entonces retorna true")
        void whenCustomerHasExpiredLoans_thenReturnTrue() {
                // Given
                String rut = "12.345.678-9";
                LocalDate yesterday = LocalDate.now().minusDays(1);

                CustomerEntity customerWithExpiredLoan = new CustomerEntity();
                customerWithExpiredLoan.setRut(rut);

                LoanEntity expiredLoan = new LoanEntity();
                expiredLoan.setReturnDate(yesterday);
                expiredLoan.setState("RETURNED");

                when(customerRepository.findByRut(rut)).thenReturn(Optional.of(customerWithExpiredLoan));
                when(loanRepository.findByCustomerAndStateIsNot(customerWithExpiredLoan, "RETURNED"))
                                .thenReturn(List.of(expiredLoan));

                // When
                Boolean result = customerService.customerLoanExpired(rut);

                // Then
                assertThat(result).isTrue();
                verify(customerRepository).findByRut(rut);
                verify(loanRepository).findByCustomerAndStateIsNot(customerWithExpiredLoan, "RETURNED");
        }

        @Test
        @DisplayName("Cuando cliente no tiene préstamos expirados, entonces retorna false")
        void whenCustomerHasNoExpiredLoans_thenReturnFalse() {
                // Given
                String rut = "12.345.678-9";

                CustomerEntity activeCustomer = new CustomerEntity();
                activeCustomer.setRut(rut);

                LoanEntity activeLoan = new LoanEntity();
                activeLoan.setReturnDate(LocalDate.now().plusDays(10));
                activeLoan.setState("ACTIVE");

                when(customerRepository.findByRut(rut)).thenReturn(Optional.of(activeCustomer));
                when(loanRepository.findByCustomerAndStateIsNot(activeCustomer, "RETURNED"))
                                .thenReturn(List.of(activeLoan));

                // When
                Boolean result = customerService.customerLoanExpired(rut);

                // Then
                assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Cuando cliente no existe, entonces lanza EntityNotFoundException")
        void whenCustomerDoesNotExist_thenThrowsEntityNotFoundException() {
                // Given
                String rut = "99.999.999-9";

                when(customerRepository.findByRut(rut)).thenReturn(Optional.empty());

                // When & Then
                assertThatThrownBy(() -> customerService.customerLoanExpired(rut))
                                .isInstanceOf(EntityNotFoundException.class)
                                .hasMessageContaining("No existe el cliente con RUT");
        }

        @Test
        @DisplayName("Cuando cliente tiene múltiples préstamos, uno vencido, entonces retorna true")
        void whenCustomerHasMultipleLoansOneExpired_thenReturnTrue() {
                // Given
                String rut = "12.345.678-9";
                CustomerEntity customerWithMultipleLoans = new CustomerEntity();
                customerWithMultipleLoans.setRut(rut);

                LoanEntity activeLoan = new LoanEntity();
                activeLoan.setReturnDate(LocalDate.now().plusDays(5));
                activeLoan.setState("ACTIVE");

                LoanEntity expiredLoan = new LoanEntity();
                expiredLoan.setReturnDate(LocalDate.now().minusDays(1));
                expiredLoan.setState("ACTIVE");

                when(customerRepository.findByRut(rut)).thenReturn(Optional.of(customerWithMultipleLoans));
                when(loanRepository.findByCustomerAndStateIsNot(customerWithMultipleLoans, "RETURNED"))
                                .thenReturn(List.of(activeLoan, expiredLoan));

                // When
                Boolean result = customerService.customerLoanExpired(rut);

                // Then
                assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Cuando cliente no tiene préstamos vencidos, entonces el estado sigue ACTIVE")
        void whenCustomerHasNoExpiredLoans_thenStateStaysActive() {
                // Given
                customer.setState("ACTIVE");
                loan.setReturnDate(LocalDate.now().plusDays(10));

                when(loanRepository.findByCustomerAndState(customer, "ACTIVE"))
                                .thenReturn(List.of(loan));

                // When
                customerService.restrictClient(customer);

                // Then
                assertThat(customer.getState()).isEqualTo("ACTIVE");
                verify(loanRepository).findByCustomerAndState(customer, "ACTIVE");
        }

        // ==================== getAllCustomer ====================

        @Test
        @DisplayName("Cuando se obtienen todos los clientes, entonces retorna lista de clientes")
        void whenGetAllCustomers_thenReturnsCustomerList() {
                // Given
                CustomerEntity customer1 = new CustomerEntity();
                customer1.setRut("12.345.678-9");
                customer1.setName("Cliente 1");

                CustomerEntity customer2 = new CustomerEntity();
                customer2.setRut("98.765.432-1");
                customer2.setName("Cliente 2");

                List<CustomerEntity> customers = List.of(customer1, customer2);

                when(customerRepository.findAll()).thenReturn(customers);

                // When
                List<CustomerEntity> result = customerService.getAllCustomer();

                // Then
                assertThat(result).hasSize(2);
                assertThat(result).contains(customer1, customer2);
                verify(customerRepository).findAll();
        }

        @Test
        @DisplayName("Cuando no hay clientes, entonces retorna lista vacía")
        void whenNoCustomers_thenReturnsEmptyList() {
                // Given
                when(customerRepository.findAll()).thenReturn(new ArrayList<>());

                // When
                List<CustomerEntity> result = customerService.getAllCustomer();

                // Then
                assertThat(result).isEmpty();
                verify(customerRepository).findAll();
        }

        // ==================== getStateCustomer ====================

        @Test
        @DisplayName("Cuando cliente está ACTIVE, entonces retorna true")
        void whenCustomerIsActive_thenReturnTrue() {
                // Given
                Long id = 1L;
                customer.setIdCustomer(id);
                customer.setState("ACTIVE");

                when(customerRepository.findById(id)).thenReturn(Optional.of(customer));

                // When
                Boolean result = customerService.getStateCustomer(id);

                // Then
                assertThat(result).isTrue();
                verify(customerRepository).findById(id);
        }

        @Test
        @DisplayName("Cuando cliente está RESTRICTED, entonces retorna false")
        void whenCustomerIsRestricted_thenReturnFalse() {
                // Given
                Long id = 1L;
                customer.setIdCustomer(id);
                customer.setState("RESTRICTED");

                when(customerRepository.findById(id)).thenReturn(Optional.of(customer));

                // When
                Boolean result = customerService.getStateCustomer(id);

                // Then
                assertThat(result).isFalse();
                verify(customerRepository).findById(id);
        }

        @Test
        @DisplayName("Cuando cliente no existe, entonces lanza EntityNotFoundException")
        void whenCustomerNotFound_thenThrowsEntityNotFoundException() {
                // Given
                Long id = 999L;

                when(customerRepository.findById(id)).thenReturn(Optional.empty());

                // When & Then
                assertThatThrownBy(() -> customerService.getStateCustomer(id))
                                .isInstanceOf(EntityNotFoundException.class)
                                .hasMessageContaining("No existe el cliente con ID");

                verify(customerRepository).findById(id);
        }
}
