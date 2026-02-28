package com.example.ProyectoTingeso.Services;

import com.example.ProyectoTingeso.Entities.ToolEntity;
import com.example.ProyectoTingeso.Entities.TypeToolEntity;
import com.example.ProyectoTingeso.Repositories.ToolRepository;
import com.example.ProyectoTingeso.Repositories.TypeToolRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ToolService {
    private final ToolRepository toolRepo;
    private final TypeToolService typeToolService;
    private final KardexService kardexService;
    private final TypeToolRepository typeToolRepository;


    @Autowired
    public ToolService(ToolRepository toolRepo, TypeToolService typeToolService, KardexService kardexService, TypeToolRepository typeToolRepository) {
        this.toolRepo = toolRepo;
        this.typeToolService = typeToolService;
        this.kardexService = kardexService;
        this.typeToolRepository = typeToolRepository;
    }

    /**
     * RF1.1: Register a new tool instance. Search for the tool type by name, and if it does not exist, create it.
     * @param name
     * @param category
     * @param replacementValue
     * @param dailyRate
     * @return
     */
    public ToolEntity registerTool(String name, String category, int replacementValue, int dailyRate, int debtRate) {

        TypeToolEntity typeToolEntity = typeToolService.findOrCreateTypeTool(name, category, replacementValue, dailyRate, debtRate);

        // update stock
        typeToolEntity.setStock(typeToolEntity.getStock() + 1);

        //------------Create Tool--------------------------
        ToolEntity tool = new ToolEntity();
        tool.setTypeTool(typeToolEntity);
        tool.setState("AVAILABLE");
        return toolRepo.save(tool);
    }


    /**
     * RF 1.2: Changes the status of a tool to “DECOMMISSIONED.”
     * @param tool
     * @return
     */
    //state: AVAILABLE (DISPONIBLE), ON_LOAN(PRESTADA), UNDER_REPAIR(EN REPARACIÓN), DECOMMISSIONED(DADA DE BAJA)
    public ToolEntity unsubscribeTool(ToolEntity tool) {

        tool.setState("DECOMMISSIONED");
        kardexService.registerDecommissioned(tool);
        return toolRepo.save(tool);
    }

    /**
     * Helper 1.2: Discard unused tools
     * @param idTool
     * @return
     */

    public ToolEntity deactivateUnusedTool (Long idTool) {
        ToolEntity tool = toolRepo.findById(idTool)
                .orElseThrow(() -> new EntityNotFoundException("Herramienta no encontrada: " + idTool));

        if (tool.getState().equals("AVAILABLE")) {
            unsubscribeTool(tool);
            return tool;
        }

        throw new IllegalStateException("La herramienta esta en uso actualmente " + idTool);
    }

    /**
     * Helper 1.2: Discard damaged tools
     * @param idTool
     * @return
     */

    public ToolEntity discardDamagedTools (Long idTool) {
        ToolEntity tool = toolRepo.findById(idTool)
                .orElseThrow(() -> new EntityNotFoundException("Herramienta no encontrada: " + idTool));

        if (tool.getState().equals("UNDER_REPAIR")) {
            unsubscribeTool(tool);
            return tool;
        }

        throw new IllegalStateException("La herramienta no esta en reparación " + idTool);
    }

    //-------------------------------------------Utility functions---------------------------------------------------------------

    /**
     * Utility 1 (RF 1.1): Register a batch of tools
     *
     * @param name
     * @param category
     * @param replacementValue
     * @param dailyRate
     * @param debtRate
     * @param amount
     * @return
     */
    public List<ToolEntity> registerLotTool(String name, String category, int replacementValue, int dailyRate, int debtRate, int amount) {
        List<ToolEntity> tools = new ArrayList<>();

        for (int i = 0; i < amount; i++) {
            ToolEntity tool = registerTool(name, category, replacementValue, dailyRate, debtRate);
            tools.add(tool);
        }

        kardexService.registerLotMovement(tools);

        return tools;
    }

    /**
     * Utility 2: Search for all tools of the same type
     * @param idTypeTool
     * @return
     */

    public List<ToolEntity> findAllByTypeTool(Long idTypeTool) {

        Optional<TypeToolEntity> typeToolOptional = typeToolRepository.findById(idTypeTool);

        if (typeToolOptional.isEmpty()) {
            throw new EntityNotFoundException("El Tipo de Herramienta con ID " + idTypeTool + " no existe.");
        }

        TypeToolEntity typeTool = typeToolOptional.get();

        List<ToolEntity> tools = toolRepo.findAllByTypeTool(typeTool);

        return tools;
    }

    /**
     * Utility 3: Search for a tool by ID
     * @param idTool
     * @return
     */

    public ToolEntity findToolById(Long idTool) {

        ToolEntity tool = toolRepo.findById(idTool).orElseThrow(() -> new EntityNotFoundException("Herramienta no encontrada: " + idTool));

        return tool;
    }
}