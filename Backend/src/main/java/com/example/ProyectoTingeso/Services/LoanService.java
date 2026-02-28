package com.example.ProyectoTingeso.Services;

import com.example.ProyectoTingeso.Entities.*;
import com.example.ProyectoTingeso.Repositories.DebtsRepository;
import com.example.ProyectoTingeso.Repositories.LoanRepository;
import com.example.ProyectoTingeso.Repositories.ToolRepository;
import com.example.ProyectoTingeso.Repositories.TypeToolRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class LoanService {
    private final ToolRepository toolRepo;
    private final LoanRepository loanRepo;
    private final DebtsRepository debtsRepo;
    private final DebtsService debtsService;
    private final KardexService kardexService;
    private final TypeToolRepository typeToolRepo;
    private final CustomerService customerService;

    @Autowired
    public LoanService(ToolRepository toolRepo, LoanRepository loanRepo, DebtsRepository debtsRepo,  DebtsService debtsService, KardexService kardexService,  TypeToolRepository typeToolRepo, CustomerService customerService) {
        this.toolRepo = toolRepo;
        this.loanRepo = loanRepo;
        this.debtsRepo = debtsRepo;
        this.typeToolRepo = typeToolRepo;
        this.debtsService = debtsService;
        this.kardexService = kardexService;
        this.customerService = customerService;
    }

    /**
     *  RF2.1: Record a loan by associating the customer and tool,
     *  with the delivery date and agreed return date.
     * @param typeToolId
     * @param customer
     * @param deliveryDate
     * @param returnDate
     * @return
     */

    @Transactional
    public LoanEntity createLoan(List<Long> typeToolId, CustomerEntity customer, LocalDate deliveryDate, LocalDate returnDate) {

        LocalDate today = LocalDate.now();
        LoanEntity loan = new LoanEntity();
        List<ToolEntity> tools = new ArrayList<>();
        List<TypeToolEntity> typeToolList = new ArrayList<>();

        validateLoanAvailability(customer, deliveryDate, returnDate);

        for (Long typetoolId : typeToolId) {
            TypeToolEntity typetool = typeToolRepo.findById(typetoolId)
                    .orElseThrow(() -> new IllegalArgumentException("No existe tipo de herramienta con ese ID: " + typetoolId));

            typeToolList.add(typetool);
        }

        loan.setDeliveryDate(deliveryDate);
        loan.setReturnDate(returnDate);
        loan.setCustomer(customer);
        loan.setState("ACTIVE");


        loan.setRentalAmount(RentalAmount(typeToolId, deliveryDate, returnDate));


        for (TypeToolEntity typeTool : typeToolList) {

            ToolEntity tool = validateToolAvailability(typeTool, customer);

            tool.setState("ON_LOAN");
            toolRepo.save(tool);

            tools.add(tool);
        }

        loan.setTool(tools);
        LoanEntity save = loanRepo.save(loan);
        kardexService.registerLoan(save);

        //Check if a date that has already expired is entered.
        customerService.restrictClient(customer);

        return save;
    }

    /**
     * RF2.2: Validate availability before authorizing the loan
     * @param customer
     * @param deliveryDate
     * @param returnDate
     * @return
     */

    public void validateLoanAvailability ( CustomerEntity customer, LocalDate deliveryDate, LocalDate returnDate){

        if(!(customerActive(customer))){
            throw new IllegalArgumentException("El cliente esta restringido");
        }

        if(expiredLoan(customer)){
            throw new IllegalArgumentException("El cliente tiene prestamos vencidos");
        }

        if(customerService.customerLoanExpired( customer.getRut())){
            customerService.restrictClient(customer);
            throw new IllegalArgumentException("El cliente tiene atrasos no regularizados");
        }

        if(!(customerLoanSize(customer))){
            throw new IllegalArgumentException("El cliente tiene 5 prestamos activos");
        }

        if(returnDateIsBeforeDeliveryDate(deliveryDate, returnDate)){
            throw new IllegalArgumentException("La fecha de retorno debe ser posterior a la fecha de entrega de la herramienta");
        }

        if (!(customerHasNoPendingDebts(customer))){
            throw new IllegalArgumentException("El cliente tiene deudas pendientes");
        }

    }

    /**
     * Helper 2.2: Verify that the tool is in stock and that there are no current loans already in place for the same tool.
     * @param typeTool
     * @param customer
     * @return
     */
    public ToolEntity validateToolAvailability(TypeToolEntity typeTool, CustomerEntity customer){
        if(customerToolActive(typeTool,  customer)){
            throw new IllegalArgumentException("El cliente prestamos vigentes con esta herramienta");
        }

        return findAvailableTool(typeTool)
                .orElseThrow(() -> new EntityNotFoundException("No hay stock disponible para esta herramienta"));
    }

    /**
     * Helper 2.2: A customer may not borrow more than one unit of the same tool at a time.
     * @param typeTool
     * @param customer
     * @return
     */
    private boolean customerToolActive (TypeToolEntity typeTool, CustomerEntity customer){
        List<LoanEntity> activeLoans = loanRepo.findByCustomerAndState(customer,"ACTIVE");

        for (LoanEntity loan : activeLoans) {
            List<ToolEntity> tools = loan.getTool();
            for (ToolEntity tool : tools) {
                if (tool.getTypeTool().equals(typeTool)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Helper 2.2: The customer must be in active status (not restricted).
     * @param customer
     * @return
     */
    private boolean customerActive (CustomerEntity customer)
    {
        return "ACTIVE".equalsIgnoreCase(customer.getState());
    }

    /**
     * Helper 2.2: The customer must not have any overdue loans.
     * @param customer
     * @return
     */
    private boolean expiredLoan(CustomerEntity customer){
        List<LoanEntity> expiredLoans= loanRepo.findByCustomerAndState(customer, "EXPIRED");

        return !expiredLoans.isEmpty();
    }

    /**
     * Helper 2.2: The customer must not have any unpaid debts. Returns true if they have no debts, returns false if they have debts.
     * @param customer
     * @return
     */
    private boolean customerHasNoPendingDebts( CustomerEntity customer){
        return debtsRepo.findByCustomerAndStatus(customer, "PENDING").isEmpty();
    }


    /**
     * Helper: The tool must be available and have stock >=1. If it exists, return it; if not, return an optional.
     * @param typeTool
     * @return
     */
    private   Optional<ToolEntity> findAvailableTool(TypeToolEntity typeTool){
        return toolRepo.findFirstByStateAndTypeTool("AVAILABLE", typeTool);

    }

    /**
     * Helper 2.2: The system must verify that the return date is not earlier than the delivery date.
     * @param deliveryDate
     * @param returnDate
     * @return
     */
    private boolean returnDateIsBeforeDeliveryDate (LocalDate deliveryDate, LocalDate returnDate){
        return returnDate.isBefore(deliveryDate);
    }

    /**
     * Helper 2.2: A customer can have a maximum of 5 active loans simultaneously.
     * @param customer
     * @return
     */
    private boolean customerLoanSize (CustomerEntity customer){
        List<LoanEntity> activeLoans = loanRepo.findByCustomerAndState(customer,"ACTIVE");
        return activeLoans.size() < 5;
    }



    /**
     * RF2.3: Record tool returns, updating status and stock.
     * @param loanId
     * @param toolStates
     */

    //DAMAGED --> GOOD
    @Transactional
    public LoanEntity toolReturn(Long loanId, Map<Long, String> toolStates) {

        LoanEntity loan = loanRepo.findById(loanId)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró el préstamo con id: " + loanId));

        if (loan.getState().equals("RETURNED")) {
            throw new IllegalArgumentException("El prestamo ya fue devuelto");
        }


        List<ToolEntity> toolsInLoan = loan.getTool();

        for (Map.Entry<Long, String> entry : toolStates.entrySet()) {
            Long toolId = entry.getKey();
            String newState = entry.getValue();

            ToolEntity toolToUpdate = toolsInLoan.stream()
                    .filter(t -> t.getIdTool().equals(toolId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("La herramienta con ID " + toolId + " no pertenece a este préstamo."));

            toolToUpdate.setState(newState);
        }

        loan.setState("RETURNED");


        LocalDate today = LocalDate.now();
        CustomerEntity customer = loan.getCustomer();

        if (today.isAfter(loan.getReturnDate())) {
            debtsService.debtForArrears(loan, customer, today);
        }


        for (ToolEntity tool : toolsInLoan) {
            String currentState = tool.getState();
            if ("DAMAGED".equalsIgnoreCase(currentState)) {
                debtsService.debtForToolDamage(loan, tool, customer, today);
            } else if ("GOOD".equalsIgnoreCase(currentState)) {
                // Si la herramienta está en buen estado, ahora pasa a estar disponible.
                tool.setState("AVAILABLE");
            }
        }

        LoanEntity save= loanRepo.save(loan);

        kardexService.registerToolReturn(save);

        return save;
    }

    /**
     * RF2.4 Automatically calculate late fees (daily rate)
     * @param typeToolsId
     * @param deliveryDate
     * @param returnDate
     * @return
     */
    public int RentalAmount(List<Long> typeToolsId, LocalDate deliveryDate, LocalDate returnDate){

        List<TypeToolEntity> typeToolList= new ArrayList<>();

        for(Long typeToolId: typeToolsId ){
            TypeToolEntity typeTool= typeToolRepo.findById(typeToolId)
                    .orElseThrow(() -> new EntityNotFoundException("No se encontró el id de la herramienta: " + typeToolId));

            typeToolList.add(typeTool);

        }

        long daysRental = ChronoUnit.DAYS.between(deliveryDate, returnDate) + 1; //calculate the rental days

        int dailyRate= 0;

        for (TypeToolEntity typeTool : typeToolList) {
            dailyRate += typeTool.getDailyRate();
        }

        return Math.toIntExact(dailyRate * daysRental);
    }

    /**
     * RF6.1: List active loans and their status (ACTIVE, EXPIRED)
     * @return List
     */
    public List loanActiveAndExpire(){
        return loanRepo.findByStateIsNot("RETURNED");
    }

    /**
     * RF 6.3: Report on the most frequently borrowed tools
     * @return
     */
    public List<Map<String, Object>> getTopToolsReport() {

        List<Object[]> results = loanRepo.countLoansByType();

        List<Map<String, Object>> report = new ArrayList<>();

        for (Object[] row : results) {
            Map<String, Object> item = new HashMap<>();

            item.put("toolName", row[0]);

            item.put("usageCount", row[1]);

            report.add(item);
        }

        return report;
    }


    //-------------------------Utility-----------------------------------------------


    /**
     * Utility 1: Return all loans
     * @return
     */
    @Transactional
    public List<LoanEntity> findByAll(){
        return loanRepo.findAll();
    }

    public int countLoansByState(){
        return loanRepo.countLoansByState("ACTIVE");
    }


}
