package com.example.ProyectoTingeso.Services;

import com.example.ProyectoTingeso.Entities.KardexDetailEntity;
import com.example.ProyectoTingeso.Entities.KardexEntity;
import com.example.ProyectoTingeso.Entities.LoanEntity;
import com.example.ProyectoTingeso.Entities.ToolEntity;
import com.example.ProyectoTingeso.Entities.TypeToolEntity;
import com.example.ProyectoTingeso.Entities.UserEntity;
import com.example.ProyectoTingeso.Repositories.KardexRepository;
import com.example.ProyectoTingeso.Repositories.ToolRepository;
import com.example.ProyectoTingeso.Repositories.TypeToolRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class KardexService {

    private final KardexRepository kardexRepo;
    private final TypeToolRepository typeToolRepository;
    private final KardexDetailService kardexDetailService;
    private final ToolRepository toolRepository;
    private final UserService userService;

    public KardexService(KardexRepository kardexRepo, TypeToolRepository typeToolRepository,
            KardexDetailService kardexDetailService, ToolRepository toolRepository, UserService userService) {
        this.kardexRepo = kardexRepo;
        this.typeToolRepository = typeToolRepository;
        this.kardexDetailService = kardexDetailService;
        this.toolRepository = toolRepository;
        this.userService = userService;
    }

    /**
     * Record movements
     *
     * @param typeMove movement type
     * @param date     movement date
     * @param quantity number of tools
     * @param loan     associated loan (nullable)
     * @param tool     list of tools involved
     * @return saved KardexEntity
     */
    public KardexEntity registerMove(String typeMove, LocalDateTime date, int quantity, LoanEntity loan,
            List<ToolEntity> tool) {
        KardexEntity kardex = new KardexEntity();

        kardex.setTypeMove(typeMove);
        kardex.setDate(date);
        kardex.setQuantity(quantity);
        kardex.setLoan(loan);

        UserEntity user = userService.getUser();
        kardex.setUser(user);

        for (ToolEntity toolEntity : tool) {
            KardexDetailEntity kardexDetail = new KardexDetailEntity();
            kardexDetail.setTool(toolEntity);
            kardex.addDetail(kardexDetail);
        }
        return kardexRepo.save(kardex);
    }

    /**
     * RF 5.1: Register new tools in batches
     *
     * @param tools list of new tools
     */
    @Transactional
    public void registerLotMovement(List<ToolEntity> tools) {
        if (tools == null || tools.isEmpty()) {
            return;
        }

        String typeMove = "TOOL_REGISTER";
        LocalDateTime date = LocalDateTime.now();
        int quality = tools.size();

        registerMove(typeMove, date, quality, null, tools);
    }

    /**
     * RF 5.1: Register loan
     *
     * @param loan the loan to register
     */
    public void registerLoan(LoanEntity loan) {
        String typeMove = "LOAN";
        LocalDateTime today = LocalDateTime.now();
        List<ToolEntity> tools = loan.getTool();
        int quantity = tools.size();

        for (ToolEntity tool : tools) {
            TypeToolEntity typeTool = tool.getTypeTool();
            typeTool.setStock(typeTool.getStock() - 1);
        }

        registerMove(typeMove, today, quantity, loan, tools);
    }

    /**
     * RF 5.1: Register a return
     *
     * @param loan the returned loan
     */
    public void registerToolReturn(LoanEntity loan) {
        String typeMove = "TOOL_RETURN";
        LocalDateTime date = LocalDateTime.now();
        List<ToolEntity> tools = loan.getTool();
        int quantity = tools.size();

        for (ToolEntity tool : tools) {
            TypeToolEntity typeTool = tool.getTypeTool();

            if (tool.getState().equals("AVAILABLE")) {
                typeTool.setStock(typeTool.getStock() + 1);
                typeToolRepository.save(typeTool);
            }
        }

        registerMove(typeMove, date, quantity, loan, tools);
    }

    /**
     * RF 5.1: Register a repaired tool
     *
     * @param tool repaired tool
     */
    public void registerToolRepair(ToolEntity tool) {
        String typeMove = "TOOL_REPAIR";
        LocalDateTime date = LocalDateTime.now();
        int quantity = 1;

        List<ToolEntity> toolList = new ArrayList<>();

        tool.getTypeTool().setStock(tool.getTypeTool().getStock() + 1);
        toolList.add(tool);

        registerMove(typeMove, date, quantity, null, toolList);
    }

    /**
     * RF 5.1: Register removal tool
     *
     * @param tool decommissioned tool
     */
    public void registerDecommissioned(ToolEntity tool) {
        String typeMove = "DECOMMISSIONED";
        LocalDateTime date = LocalDateTime.now();
        int quantity = 1;

        List<ToolEntity> toolList = new ArrayList<>();
        toolList.add(tool);

        registerMove(typeMove, date, quantity, null, toolList);
    }

    /**
     * RF 5.2: View the movement history of each tool
     *
     * @param idtool tool ID
     * @return list of KardexEntity movements
     */
    public List<KardexEntity> getAllMovementsOfATool(long idtool) {
        Optional<ToolEntity> tool = toolRepository.findById(idtool);

        if (tool.isPresent()) {
            List<KardexDetailEntity> listKardexDetail = kardexDetailService.findAllByTool(tool.get());
            List<KardexEntity> listMovement = new ArrayList<>();

            for (KardexDetailEntity kardexDetail : listKardexDetail) {
                KardexEntity kardex = kardexDetail.getKardex();
                listMovement.add(kardex);
            }
            return listMovement;
        }

        return new ArrayList<>();
    }

    /**
     * RF5.3: Generate list of movements by date range
     *
     * @param start start datetime
     * @param end   end datetime
     * @return list of KardexEntity in range
     */
    public List<KardexEntity> listByDateRange(LocalDateTime start, LocalDateTime end) {
        return kardexRepo.findByDateBetween(start, end);
    }

    // ---------------------Utility--------------------------------

    /**
     * Utility: Submit all transactions
     *
     * @return list of all KardexEntity
     */
    public List<KardexEntity> findAllKardex() {
        return kardexRepo.findAll();
    }
}
