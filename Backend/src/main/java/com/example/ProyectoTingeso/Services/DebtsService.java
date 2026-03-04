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

    private static final String STATE_RESTRICTED = "RESTRICTED";
    private static final String STATUS_PENDING = "PENDING";
    private static final String TYPE_ARREARS = "ARREARS";
    private static final String STATUS_PENDING_ASSESSMENT = "PENDING_ASSESSMENT";

    private final DebtsRepository debtsRepo;
    private final ToolRepository toolRepo;
    private final ToolService toolService;
    private final CustomerRepository customerRepo;
    private final KardexService kardexService;
    private final LoanRepository loanRepository;

    @Autowired
    public DebtsService(DebtsRepository debtsRepo, ToolRepository toolRepo, ToolService toolService,
            CustomerRepository customerRepo, KardexService kardexService, LoanRepository loanRepository) {
        this.debtsRepo = debtsRepo;
        this.toolRepo = toolRepo;
        this.toolService = toolService;
        this.customerRepo = customerRepo;
        this.kardexService = kardexService;
        this.loanRepository = loanRepository;
    }

    /**
     * Records debts (No controller)
     *
     * @param amount       debt amount
     * @param type         debt type
     * @param creationDate creation date
     * @param status       initial status
     * @param loan         associated loan
     * @param customer     associated customer
     * @param tools        tools involved
     * @return saved DebtsEntity
     */
    @Transactional
    public DebtsEntity registerDebts(int amount, String type, LocalDate creationDate, String status,
            LoanEntity loan, CustomerEntity customer, List<ToolEntity> tools) {

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

        customer.setState(STATE_RESTRICTED);
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
     * @param loan  the loan
     * @param today today's date
     * @return penalty amount
     */
    public int calculateDebtForArrears(LoanEntity loan, LocalDate today) {
        List<ToolEntity> tools = loan.getTool();

        int debtRate = 0;
        for (ToolEntity tool : tools) {
            debtRate = debtRate + tool.getTypeTool().getDebtRate();
        }

        int daysLate = Math.toIntExact(ChronoUnit.DAYS.between(loan.getReturnDate(), today));
        return (int) (daysLate * debtRate);
    }

    /**
     * Creates a debt for arrears
     *
     * @param loan     the loan
     * @param customer associated customer
     * @param today    today's date
     * @return saved DebtsEntity
     */
    @Transactional
    public DebtsEntity debtForArrears(LoanEntity loan, CustomerEntity customer, LocalDate today) {
        Optional<DebtsEntity> debtOptional = debtsRepo.findByLoanAndStatus(loan, "PENDING_RETURN");

        if (debtOptional.isPresent()) {
            DebtsEntity debt = debtOptional.get();
            debt.setStatus(STATUS_PENDING);
            debt.setType(TYPE_ARREARS);
            debt.setAmount(calculateDebtForArrears(loan, today));
            debtsRepo.save(debt);
            return debt;
        }
        if (loan.getState().equals("EXPIRED")) {
            return registerDebts(0, TYPE_ARREARS, today, "PENDING_RETURN", loan, customer,
                    new ArrayList<>(loan.getTool()));
        }

        int amount = calculateDebtForArrears(loan, today);
        return registerDebts(amount, TYPE_ARREARS, today, STATUS_PENDING, loan, customer,
                new ArrayList<>(loan.getTool()));
    }

    /**
     * Issue a fine for damage
     *
     * @param loan     the loan
     * @param tool     damaged tool
     * @param customer associated customer
     * @param today    today's date
     * @return saved DebtsEntity
     */
    @Transactional
    public DebtsEntity debtForToolDamage(LoanEntity loan, ToolEntity tool, CustomerEntity customer, LocalDate today) {
        String type = "DAMAGES";

        tool.setState("UNDER_REPAIR");
        toolRepo.save(tool);

        List<ToolEntity> toolList = new ArrayList<>();
        toolList.add(tool);

        return registerDebts(0, type, today, STATUS_PENDING_ASSESSMENT, loan, customer, toolList);
    }

    /**
     * Deliver the results of the tool repair (unit)
     *
     * @param idTool       tool ID
     * @param outcome      repair outcome
     * @param damageCharge charge amount
     * @return updated ToolEntity
     */
    @Transactional
    public ToolEntity assessToolDamage(Long idTool, String outcome, int damageCharge) {
        if (idTool == null || outcome == null || outcome.isBlank() || damageCharge < 0) {
            throw new IllegalArgumentException("Los datos no son válidos.");
        }

        ToolEntity tool = toolRepo.findById(idTool)
                .orElseThrow(() -> new EntityNotFoundException("La herramienta con ID " + idTool + " no existe."));

        DebtsEntity debt = debtsRepo.findByToolAndStatusAndType(tool, STATUS_PENDING_ASSESSMENT, "DAMAGES")
                .orElseThrow(
                        () -> new IllegalStateException("La herramienta no tiene una deuda pendiente de evaluación."));

        switch (outcome) {
            case "IRREPARABLE":
                toolService.discardDamagedTools(tool.getIdTool());
                debt.setAmount(tool.getTypeTool().getReplacementValue());
                debt.setStatus(STATUS_PENDING);
                debtsRepo.save(debt);
                kardexService.registerDecommissioned(tool);
                break;

            case "MINOR_DAMAGE":
                tool.setState("AVAILABLE");
                toolRepo.save(tool);
                debt.setAmount(damageCharge);
                debt.setStatus(STATUS_PENDING);
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
     *
     * @param idDebts debt ID
     * @return updated DebtsEntity
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
     *
     * @param customer customer entity
     */
    public void refreshCustomerState(CustomerEntity customer) {
        boolean hasPendingDebts = debtsRepo.existsByCustomerAndStatus(customer, STATUS_PENDING);
        boolean hasAssessmentDebts = debtsRepo.existsByCustomerAndStatus(customer, STATUS_PENDING_ASSESSMENT);

        if (hasPendingDebts || hasAssessmentDebts) {
            if (!STATE_RESTRICTED.equals(customer.getState())) {
                customer.setState(STATE_RESTRICTED);
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
