package com.example.ProyectoTingeso.Services;

import com.example.ProyectoTingeso.Entities.CustomerEntity;
import com.example.ProyectoTingeso.Entities.LoanEntity;
import com.example.ProyectoTingeso.Repositories.CustomerRepository;
import com.example.ProyectoTingeso.Repositories.LoanRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class CustomerService {

    private static final String STATE_ACTIVE = "ACTIVE";

    private final DebtsService debtsService;
    private final CustomerRepository customerRepo;
    private final LoanRepository loanRepo;

    @Autowired
    public CustomerService(CustomerRepository customerRepo, LoanRepository loanRepo, DebtsService debtsService) {
        this.customerRepo = customerRepo;
        this.loanRepo = loanRepo;
        this.debtsService = debtsService;
    }

    /**
     * RF3.1: Register a new customer validating that all attributes are correct
     *
     * @param name        customer name
     * @param phoneNumber phone number
     * @param email       email address
     * @param rut         unique tax ID
     * @return saved CustomerEntity
     */
    public CustomerEntity registerCustomer(String name, String phoneNumber, String email, String rut) {
        if ((name == null) || (name.isEmpty())) {
            throw new IllegalStateException("El nombre es obligatorio.");
        }
        if ((phoneNumber == null) || (phoneNumber.isEmpty())) {
            throw new IllegalStateException("El numero de telefono es obligatorio.");
        }
        if ((rut == null) || (rut.isEmpty())) {
            throw new IllegalStateException("El  rut es obligatorio.");
        }
        if (customerRepo.existsByRut(rut)) {
            throw new IllegalStateException("El rut ya existe");
        }
        if ((email == null) || (email.isEmpty())) {
            throw new IllegalStateException("El correo es obligatorio.");
        }

        CustomerEntity customer = new CustomerEntity();
        customer.setName(name);
        customer.setPhoneNumber(phoneNumber);
        customer.setEmail(email);
        customer.setRut(rut);
        customer.setState(STATE_ACTIVE);

        return customerRepo.save(customer);
    }

    /**
     * RF 2.5: Blocks new loans to customers with outstanding arrears
     *
     * @param rut customer RUT
     * @return true if customer has expired loans
     */
    public boolean customerLoanExpired(String rut) {
        CustomerEntity customer = customerRepo.findByRut(rut)
                .orElseThrow(() -> new EntityNotFoundException("No existe el cliente con RUT " + rut));

        List<LoanEntity> loanCustomer = loanRepo.findByCustomerAndStateIsNot(customer, "RETURNED");
        LocalDate today = LocalDate.now();

        for (LoanEntity loan : loanCustomer) {
            LocalDate returnDate = loan.getReturnDate();
            if (returnDate.isBefore(today)) {
                return true;
            }
        }
        return false;
    }

    /**
     * RF3.2: Change customer status to "restricted" in case of late payments
     *
     * @param customer customer entity
     */
    @Transactional
    public void restrictClient(CustomerEntity customer) {
        List<LoanEntity> loans = loanRepo.findByCustomerAndState(customer, STATE_ACTIVE);
        LocalDate today = LocalDate.now();

        for (LoanEntity loan : loans) {
            if (loan.getReturnDate().isBefore(today)) {
                loan.setState("EXPIRED");
                customer.setState("RESTRICTED");
                customerRepo.save(customer);
                LoanEntity save = loanRepo.save(loan);
                debtsService.debtForArrears(save, customer, today);
            }
        }
    }

    // ---------------------------------Utility---------------------------------------

    /**
     * Utility 1: Delivery to all customers
     *
     * @return list of all customers
     */
    @Transactional
    public List<CustomerEntity> getAllCustomer() {
        return customerRepo.findAll();
    }

    /**
     * Utility: If the customer is active, it returns TRUE, and if restricted, it
     * returns FALSE.
     *
     * @param id customer ID
     * @return true if active
     */
    public boolean getStateCustomer(Long id) {
        CustomerEntity customer = customerRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No existe el cliente con ID " + id));
        return customer.getState().equals(STATE_ACTIVE);
    }

    /**
     * Count all active customers.
     *
     * @return count of active customers
     */
    public int countAllByActive() {
        return customerRepo.countAllByState(STATE_ACTIVE);
    }

    /**
     * Count all customers.
     *
     * @return total customer count
     */
    public long countAllCustomer() {
        return customerRepo.count();
    }
}
