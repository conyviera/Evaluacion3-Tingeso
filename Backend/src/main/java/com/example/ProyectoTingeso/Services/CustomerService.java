package com.example.ProyectoTingeso.Services;

import com.example.ProyectoTingeso.Entities.CustomerEntity;
import com.example.ProyectoTingeso.Entities.DebtsEntity;
import com.example.ProyectoTingeso.Entities.LoanEntity;
import com.example.ProyectoTingeso.Entities.ToolEntity;
import com.example.ProyectoTingeso.Repositories.CustomerRepository;
import com.example.ProyectoTingeso.Repositories.DebtsRepository;
import com.example.ProyectoTingeso.Repositories.LoanRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDate;
import java.util.List;

@Service
public class CustomerService {

    private final DebtsService debtsService;
    CustomerRepository customerRepo;
    LoanRepository loanRepo;

    @Autowired
    public CustomerService(CustomerRepository customerRepo, LoanRepository loanRepo, DebtsService debtsService){
        this.customerRepo = customerRepo;
        this.loanRepo = loanRepo;
        this.debtsService = debtsService;
    }

    /**
     * RF3.1: Register a new customer validating that all attributes are correct
     * @param name
     * @param phoneNumber
     * @param email
     * @param rut
     * @return
     */
    public CustomerEntity registerCustomer(String name, String phoneNumber, String email, String rut) {
        if ((name == null) || (name.isEmpty())) {
            throw new IllegalStateException("El nombre es obligatorio.");
        }
        if ((phoneNumber == null) || (phoneNumber.isEmpty())){
            throw new IllegalStateException("El numero de telefono es obligatorio.");
        }

        if ((rut == null) || (rut.isEmpty())){
            throw new IllegalStateException("El  rut es obligatorio.");
        }

        if(customerRepo.existsByRut(rut)){
            throw new IllegalStateException("El rut ya existe");
        }

        if ((email == null) || (email.isEmpty())){
            throw new IllegalStateException("El correo es obligatorio.");
        }


        CustomerEntity customer = new CustomerEntity();
        customer.setName(name);
        customer.setPhoneNumber(phoneNumber);
        customer.setEmail(email);
        customer.setRut(rut);

        //save the initial state of the customer
        customer.setState("ACTIVE");


        return customerRepo.save(customer);

    }

    /**
     * RF 2.5: Blocks new loans to customers with outstanding arrears
     * @param rut
     * @return
     */
    public Boolean customerLoanExpired(String rut){
        CustomerEntity customer = customerRepo.findByRut(rut)
                .orElseThrow(() -> new EntityNotFoundException("No existe el cliente con RUT " + rut));


        List<LoanEntity> loanCustomer= loanRepo.findByCustomerAndStateIsNot(customer, "RETURNED");

        LocalDate today = LocalDate.now();

        for(LoanEntity loan : loanCustomer){
            LocalDate returnDate= loan.getReturnDate();

            if(returnDate.isBefore(today)){
                return true;
            }
        }

        return false;
    }

    /**
     * RF3.2: Change customer status to “restricted” in case of late payments
     * @return
     */
    @Transactional
    public void restrictClient(CustomerEntity customer){
        List<LoanEntity> loans= loanRepo.findByCustomerAndState(customer, "ACTIVE");

        LocalDate today = LocalDate.from(LocalDateTime.now());

        for (LoanEntity loan : loans) {
            if (loan.getReturnDate().isBefore(today)){
                loan.setState("EXPIRED");

                customer.setState("RESTRICTED");
                customerRepo.save(customer);

                LoanEntity save= loanRepo.save(loan);

                debtsService.debtForArrears(save, customer, today);
            }
        }
    }

    //---------------------------------Utility---------------------------------------

    /**
     * Utility 1: Delivery to all customers
     * @return
     */
    @Transactional
    public List<CustomerEntity> getAllCustomer() {
        return customerRepo.findAll();
    }

    /**
     * Utility: If the customer is active, it returns TRUE, and if restricted, it returns FALSE.
     * @param id
     * @return
     */
    public Boolean getStateCustomer(Long id){
        CustomerEntity customer= customerRepo.findById(id).orElseThrow(() -> new EntityNotFoundException("No existe el cliente con ID " + id));
        return customer.getState().equals("ACTIVE");
    }

    public int countAllByActive(){
        int number= customerRepo.countAllByState("ACTIVE");
        return number;
    }



    public long countAllCustomer(){
        long  number= customerRepo.count();
        return number;
    }
}

