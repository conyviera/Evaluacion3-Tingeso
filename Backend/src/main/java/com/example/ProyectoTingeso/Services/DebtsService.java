package com.example.ProyectoTingeso.Services;

import com.example.ProyectoTingeso.Entities.CustomerEntity;
import com.example.ProyectoTingeso.Entities.DebtsEntity;
import com.example.ProyectoTingeso.Entities.LoanEntity;
import com.example.ProyectoTingeso.Entities.ToolEntity;
import com.example.ProyectoTingeso.Repositories.CustomerRepository;
import com.example.ProyectoTingeso.Repositories.DebtsRepository;
import com.example.ProyectoTingeso.Repositories.LoanRepository;
import com.example.ProyectoTingeso.Repositories.ToolRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class DebtsService {

    private final DebtsRepository debtsRepo;
    private final ToolRepository toolRepo;
    private final ToolService toolService;
    private final CustomerRepository customerRepo;
    private final KardexService kardexService;
    private final LoanRepository loanRepository;

    @Autowired
    public DebtsService(DebtsRepository debtsRepo, ToolRepository toolRepo, ToolService toolService, CustomerRepository customerRepo, KardexService kardexService, LoanRepository loanRepository) {
        this.debtsRepo = debtsRepo;
        this.toolRepo = toolRepo;
        this.toolService = toolService;
        this.customerRepo = customerRepo;
        this.kardexService= kardexService;
        this.loanRepository = loanRepository;
    }

    /**
     * Records debts (No controller)
     *
     * @param amount
     * @param type
     * @param creationDate
     * @param status
     * @param loan
     * @param customer
     * @return
     */

    @Transactional
    public DebtsEntity registerDebts(int amount, String type, LocalDate creationDate, String status, LoanEntity loan, CustomerEntity customer,List<ToolEntity> tools) {

        if (loan == null || creationDate == null || type == null || status == null) {
            throw new IllegalArgumentException("Parámetros requeridos nulos.");
        }
        if (!loan.getCustomer().getIdCustomer().equals(customer.getIdCustomer())) {
            throw new IllegalArgumentException("El cliente no corresponde al préstamo.");
        }

        DebtsEntity lateFeeDebt = new DebtsEntity();

        lateFeeDebt.setAmount(amount);
        lateFeeDebt.setType(type);
        lateFeeDebt.setCreationDate(creationDate);
        lateFeeDebt.setPaymentDate(null);
        lateFeeDebt.setStatus(status);

        customer.setState("RESTRICTED");
        customerRepo.save(customer);
        lateFeeDebt.setCustomer(customer);

        lateFeeDebt.setLoan(loan);
        lateFeeDebt.setTool(tools);

        debtsRepo.save(lateFeeDebt);


        return lateFeeDebt;

    }

    /**
     * RF2.4: Calculate late payment penalty
     *
     * @param loan
     * @param today
     * @return
     */
    public int calculateDebtForArrears(LoanEntity loan, LocalDate today) {

        List<ToolEntity> tools=loan.getTool();

        int debtRate=0;
        for (ToolEntity tool:tools){
            debtRate= debtRate + tool.getTypeTool().getDebtRate();
        }


        int daysLate = Math.toIntExact(ChronoUnit.DAYS.between(loan.getReturnDate(), today));


        return (int) (daysLate * debtRate);
    }

    /**
     * Creates a debt for arrears
     *
     * @param loan
     * @param customer
     * @param today
     * @return
     */

    @Transactional
    public DebtsEntity debtForArrears(LoanEntity loan, CustomerEntity customer, LocalDate today) {
        Optional<DebtsEntity> debtOptional= debtsRepo.findByLoanAndStatus(loan, "PENDING_RETURN");

        if(debtOptional.isPresent()){
            DebtsEntity debt = debtOptional.get();
            debt.setStatus("PENDING");
            debt.setType("ARREARS");
            debt.setAmount(calculateDebtForArrears(loan, today));
            debtsRepo.save(debt);

            return debt;
        }
        if (loan.getState().equals("EXPIRED")) {
            String status = "PENDING_RETURN";
            int amount= 0;
            String type = "ARREARS";
            return registerDebts(amount, type, today, status, loan, customer, new ArrayList<> (loan.getTool()));
        }

        String status = "PENDING";
        String type = "ARREARS";
        int amount = calculateDebtForArrears(loan, today);

        return registerDebts(amount, type, today, status, loan, customer, new ArrayList<> (loan.getTool()));

    }

    /**
     *  Issue a fine for damage
     *
     * @param loan
     * @param customer
     * @param today
     * @return
     */

    @Transactional
    public DebtsEntity debtForToolDamage(LoanEntity loan, ToolEntity tool, CustomerEntity customer, LocalDate today) {
        int amount = 0;

        String type = "DAMAGES";
        String status = "PENDING_ASSESSMENT";

        tool.setState("UNDER_REPAIR");
        toolRepo.save(tool);

        List<ToolEntity> toolList= new ArrayList<>();
        toolList.add(tool);

        return registerDebts(amount, type, today, status, loan, customer, toolList);
    }

    /**
     * Deliver the results of the tool repair (unit)
     * @param idTool
     * @param outcome
     * @param damageCharge
     * @return
     */
    @Transactional
    public ToolEntity assessToolDamage(Long idTool, String outcome, int damageCharge) {

        if (idTool == null || outcome == null || outcome.isBlank() || damageCharge < 0) {
            throw new IllegalArgumentException("Los datos no son válidos.");
        }

        ToolEntity tool = toolRepo.findById(idTool)
                .orElseThrow(() -> new EntityNotFoundException("La herramienta con ID " + idTool + " no existe."));

        DebtsEntity debt = debtsRepo.findByToolAndStatusAndType(tool, "PENDING_ASSESSMENT", "DAMAGES")
                .orElseThrow(() -> new IllegalStateException("La herramienta no tiene una deuda pendiente de evaluación."));

        switch (outcome) {
            case "IRREPARABLE":
                toolService.discardDamagedTools (tool.getIdTool());

                debt.setAmount(tool.getTypeTool().getReplacementValue());
                debt.setStatus("PENDING");
                debtsRepo.save(debt);

                kardexService.registerDecommissioned(tool);
                break;

            case "MINOR_DAMAGE":
                tool.setState("AVAILABLE");
                toolRepo.save(tool);

                debt.setAmount(damageCharge);
                debt.setStatus("PENDING");
                debtsRepo.save(debt);
                kardexService.registerToolRepair(tool);
                break;

            default:
                throw new IllegalArgumentException("Resultado inválido: use IRREPARABLE o MINOR_DAMAGE.");
        }
        return tool;
    }


    /**
     * Mark the debt as paid
     * @param idDebts
     * @return
     */
    public DebtsEntity debtPaid(Long idDebts) {
        DebtsEntity debt = debtsRepo.findById(idDebts)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la deuda " + idDebts));

        debt.setStatus("PAID");
        debt.setPaymentDate(LocalDate.now());
        debtsRepo.save(debt);

        refreshCustomerState(debt.getCustomer());

        return debt;
    }

    /**
     * Search for debts by loan ID
     */

    public List<DebtsEntity> getDebtsByIdLoan(Long idLoan) {
        return debtsRepo.findByLoan_IdLoan(idLoan);
    }

    /**
     * Change the status if you have debts
     * @param customer
     */
    public void refreshCustomerState(CustomerEntity customer) {

        boolean hasPendingDebts = debtsRepo.existsByCustomerAndStatus(customer, "PENDING");
        boolean hasAssessmentDebts = debtsRepo.existsByCustomerAndStatus(customer, "PENDING_ASSESSMENT");

        if (hasPendingDebts || hasAssessmentDebts) {
            if (!"RESTRICTED".equals(customer.getState())) {
                customer.setState("RESTRICTED");
                customerRepo.save(customer);
            }
        } else {
            if (!"ACTIVE".equals(customer.getState())) {
                customer.setState("ACTIVE");
                customerRepo.save(customer);
            }
        }
    }


}


