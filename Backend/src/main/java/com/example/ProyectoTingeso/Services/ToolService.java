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

    private static final String TOOL_NOT_FOUND = "Herramienta no encontrada: ";

    private final ToolRepository toolRepo;
    private final TypeToolService typeToolService;
    private final KardexService kardexService;
    private final TypeToolRepository typeToolRepository;

    @Autowired
    public ToolService(ToolRepository toolRepo, TypeToolService typeToolService, KardexService kardexService,
            TypeToolRepository typeToolRepository) {
        this.toolRepo = toolRepo;
        this.typeToolService = typeToolService;
        this.kardexService = kardexService;
        this.typeToolRepository = typeToolRepository;
    }

    /**
     * RF1.1: Register a new tool instance.
     *
     * @param name             tool name
     * @param category         tool category
     * @param replacementValue replacement value
     * @param dailyRate        daily rental rate
     * @param debtRate         daily late fee rate
     * @return saved ToolEntity
     */
    public ToolEntity registerTool(String name, String category, int replacementValue, int dailyRate, int debtRate) {
        TypeToolEntity typeToolEntity = typeToolService.findOrCreateTypeTool(name, category, replacementValue,
                dailyRate, debtRate);
        typeToolEntity.setStock(typeToolEntity.getStock() + 1);

        ToolEntity tool = new ToolEntity();
        tool.setTypeTool(typeToolEntity);
        tool.setState("AVAILABLE");
        return toolRepo.save(tool);
    }

    /**
     * RF 1.2: Changes the status of a tool to "DECOMMISSIONED."
     *
     * @param tool tool entity
     * @return updated ToolEntity
     */
    public ToolEntity unsubscribeTool(ToolEntity tool) {
        tool.setState("DECOMMISSIONED");
        kardexService.registerDecommissioned(tool);
        return toolRepo.save(tool);
    }

    /**
     * Helper 1.2: Discard unused tools
     *
     * @param idTool tool ID
     * @return updated ToolEntity
     */
    public ToolEntity deactivateUnusedTool(Long idTool) {
        ToolEntity tool = toolRepo.findById(idTool)
                .orElseThrow(() -> new EntityNotFoundException(TOOL_NOT_FOUND + idTool));

        if (tool.getState().equals("AVAILABLE")) {
            unsubscribeTool(tool);
            return tool;
        }
        throw new IllegalStateException("La herramienta esta en uso actualmente " + idTool);
    }

    /**
     * Helper 1.2: Discard damaged tools
     *
     * @param idTool tool ID
     * @return updated ToolEntity
     */
    public ToolEntity discardDamagedTools(Long idTool) {
        ToolEntity tool = toolRepo.findById(idTool)
                .orElseThrow(() -> new EntityNotFoundException(TOOL_NOT_FOUND + idTool));

        if (tool.getState().equals("UNDER_REPAIR")) {
            unsubscribeTool(tool);
            return tool;
        }
        throw new IllegalStateException("La herramienta no esta en reparación " + idTool);
    }

    // -------------------------------------------Utility
    // functions---------------------------------------------------------------

    /**
     * Utility 1 (RF 1.1): Register a batch of tools
     *
     * @param name             tool name
     * @param category         category
     * @param replacementValue replacement cost
     * @param dailyRate        daily rate
     * @param debtRate         debt rate
     * @param amount           quantity to register
     * @return list of created ToolEntity
     */
    public List<ToolEntity> registerLotTool(String name, String category, int replacementValue, int dailyRate,
            int debtRate, int amount) {
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
     *
     * @param idTypeTool type tool ID
     * @return list of ToolEntity
     */
    public List<ToolEntity> findAllByTypeTool(Long idTypeTool) {
        Optional<TypeToolEntity> typeToolOptional = typeToolRepository.findById(idTypeTool);

        if (typeToolOptional.isEmpty()) {
            throw new EntityNotFoundException("El Tipo de Herramienta con ID " + idTypeTool + " no existe.");
        }

        TypeToolEntity typeTool = typeToolOptional.get();
        return toolRepo.findAllByTypeTool(typeTool);
    }

    /**
     * Utility 3: Search for a tool by ID
     *
     * @param idTool tool ID
     * @return ToolEntity
     */
    public ToolEntity findToolById(Long idTool) {
        return toolRepo.findById(idTool)
                .orElseThrow(() -> new EntityNotFoundException(TOOL_NOT_FOUND + idTool));
    }
}