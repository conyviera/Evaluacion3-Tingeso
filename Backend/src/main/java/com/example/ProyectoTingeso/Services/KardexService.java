package com.example.ProyectoTingeso.Services;

import com.example.ProyectoTingeso.Entities.*;
import com.example.ProyectoTingeso.Repositories.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class KardexService {

    private final KardexRepository kardexRepo;
    private final TypeToolRepository typeToolRepository;
    private final KardexDetailService kardexDetailService;
    private final ToolRepository toolRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    public KardexService(KardexRepository kardexRepo, TypeToolRepository typeToolRepository, KardexDetailService kardexDetailService, ToolRepository toolRepository, UserRepository userRepository, UserService userService) {
        this.kardexRepo = kardexRepo;
        this.typeToolRepository = typeToolRepository;
        this.kardexDetailService = kardexDetailService;
        this.toolRepository = toolRepository;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    /**
     * Record movements
     * @param typeMove
     * @param date
     * @param quantity
     * @param loan
     * @param tool
     * @return
     */
    public KardexEntity registermove (String typeMove, LocalDateTime date, int quantity, LoanEntity loan, List<ToolEntity> tool) {
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
     * @param tools
     * @return
     */
    @Transactional
    public void registerLotMovement (List<ToolEntity> tools){

        if (tools == null || tools.isEmpty()) {
            return;
        }

        String typeMove= "TOOL_REGISTER";
        LocalDateTime date= LocalDateTime.now();
        int quality= tools.size();

        registermove(typeMove, date, quality, null, tools);

    }

    /**
     * RF 5.1: Register loan
     * @param loan
     * @return
     */
    public void registerLoan(LoanEntity loan){

        String typeMove= "LOAN";
        LocalDateTime today = LocalDateTime.now();
        List<ToolEntity> tools= loan.getTool();
        int quantity= tools.size();


        for(ToolEntity tool: tools){
            TypeToolEntity typeTool = tool.getTypeTool();
            typeTool.setStock(typeTool.getStock()-1);
        }

        registermove(typeMove,  today, quantity, loan, tools);
    }

    /**
     * RF 5.1: Register a return
     * @param loan
     * @return
     */

    public void registerToolReturn ( LoanEntity loan){

        String typeMove= "TOOL_RETURN";
        LocalDateTime date= LocalDateTime.now();
        List<ToolEntity> tools= loan.getTool();
        int quantity= tools.size();


        for(ToolEntity tool: tools){
            TypeToolEntity typeTool= tool.getTypeTool();

            if(tool.getState().equals("AVAILABLE")){
                typeTool.setStock(typeTool.getStock()+1);
                typeToolRepository.save(typeTool);
            }

        }

        registermove(typeMove, date,quantity,loan,tools);
    }

    /**
     * RF 5.1 : Register a repaired tool
     * @param tool
     */

    public void registerToolRepair (ToolEntity tool){
        String typeMove= "TOOL_REPAIR";
        LocalDateTime date= LocalDateTime.now();
        int quantity= 1;

        List<ToolEntity> toolList= new ArrayList<>();

        TypeToolEntity typeTool= tool.getTypeTool();
        typeTool.setStock(typeTool.getStock()+1);

        toolList.add(tool);

        registermove(typeMove,date, quantity, null, toolList);
    }

    /**
     * RF 5.1: Register removal tool
     * @param tool
     */

    public void registerDecommissioned ( ToolEntity tool){
        String typeMove= "DECOMMISSIONED";
        LocalDateTime date= LocalDateTime.now();
        int quantity=1;

        List<ToolEntity> toolList= new ArrayList<>();

        TypeToolEntity typeTool= tool.getTypeTool();

        toolList.add(tool);


        registermove(typeMove,  date, quantity,null, toolList);

    }

    /**
     * RF 5.2: view the movement history of each tool
     * @param idtool
     * @return
     */

    public List<KardexEntity> getAllMovementsOfATool(long idtool){

        Optional<ToolEntity> tool= toolRepository.findById(idtool);

        if (tool.isPresent()){
            List<KardexDetailEntity> listKardexDetail= kardexDetailService.findAllByTool(tool.get());

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
     * @param start
     * @param end
     * @return
     */

    public List<KardexEntity> listByDateRange(LocalDateTime start, LocalDateTime end){
        List<KardexEntity> listKardex= new ArrayList<>();
        listKardex= kardexRepo.findByDateBetween(start, end);

        return listKardex;
    }



    //---------------------Utility--------------------------------

    /**
     * Utility: Submit all transactions
     * @return
     */
    public List<KardexEntity> findAllKardex() {
        List<KardexEntity> kardexList = kardexRepo.findAll();
        return kardexList;
    }









}
