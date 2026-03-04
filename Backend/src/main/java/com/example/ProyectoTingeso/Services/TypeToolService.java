package com.example.ProyectoTingeso.Services;

import com.example.ProyectoTingeso.Entities.TypeToolEntity;
import com.example.ProyectoTingeso.Repositories.TypeToolRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TypeToolService {

    private static final String TYPE_TOOL_NOT_FOUND = "No se encontró el tipo de herramienta con id: ";

    private final TypeToolRepository typeToolRepo;

    @Autowired
    public TypeToolService(TypeToolRepository typeToolRepo) {
        this.typeToolRepo = typeToolRepo;
    }

    /**
     * Helper 1.1: Searches for a tool type by name. If it does not exist, it
     * creates it.
     *
     * @param name             tool name
     * @param category         category
     * @param replacementValue replacement value
     * @param dailyRate        daily rate
     * @param debtRate         debt rate
     * @return existing or new TypeToolEntity
     */
    public TypeToolEntity findOrCreateTypeTool(String name, String category, int replacementValue,
            int dailyRate, int debtRate) {
        Optional<TypeToolEntity> existingTypeTool = typeToolRepo.findByNameAndCategoryAndReplacementValueAndDailyRate(
                name, category, replacementValue,
                dailyRate);

        if (existingTypeTool.isPresent()) {
            return existingTypeTool.get();
        } else {
            if ((name == null) || (name.isEmpty())) {
                throw new IllegalStateException("El nombre de la herramienta es obligatoria.");
            }
            if ((category == null) || (category.isEmpty())) {
                throw new IllegalStateException("La categoría de la herramienta es obligatoria.");
            }
            if (replacementValue <= 0) {
                throw new IllegalStateException("El valor de reposición debe ser un valor positivo.");
            }
            if (dailyRate < 0) {
                throw new EntityNotFoundException("La daily rate no puede ser nulo");
            }
            if (debtRate < 0) {
                throw new EntityNotFoundException("La debt rate no puede ser nulo");
            }

            TypeToolEntity newTypeTool = new TypeToolEntity();
            newTypeTool.setName(name);
            newTypeTool.setCategory(category);
            newTypeTool.setReplacementValue(replacementValue);
            newTypeTool.setDailyRate(dailyRate);
            newTypeTool.setDebtRate(debtRate);

            return typeToolRepo.save(newTypeTool);
        }
    }

    /**
     * RF 4.1: Set daily rental rate
     *
     * @param idTypeTool type tool ID
     * @param dailyRate  new daily rate
     * @return updated TypeToolEntity
     */
    public TypeToolEntity configurationDailyRateTypeTool(long idTypeTool, int dailyRate) {
        TypeToolEntity typeTool = typeToolRepo.findById(idTypeTool)
                .orElseThrow(() -> new EntityNotFoundException(TYPE_TOOL_NOT_FOUND + idTypeTool));

        if (dailyRate < 0) {
            throw new EntityNotFoundException("La tarifa diaria no puede ser nulo, ni menor a 0");
        }

        typeTool.setDailyRate(dailyRate);
        typeToolRepo.save(typeTool);
        return typeTool;
    }

    /**
     * RF 4.2: Set daily late fee rate
     *
     * @param idTypeTool type tool ID
     * @param debtRate   new debt rate
     * @return updated TypeToolEntity
     */
    public TypeToolEntity configurationDebtTypeTool(long idTypeTool, int debtRate) {
        TypeToolEntity typeTool = typeToolRepo.findById(idTypeTool)
                .orElseThrow(() -> new EntityNotFoundException(TYPE_TOOL_NOT_FOUND + idTypeTool));

        typeTool.setDebtRate(debtRate);
        return typeToolRepo.save(typeTool);
    }

    /**
     * RF 4.3: Records replacement value of a tool
     *
     * @param idTypeTool       type tool ID
     * @param replacementValue new replacement value
     * @return updated TypeToolEntity
     */
    public TypeToolEntity registerReplacementTypeTool(long idTypeTool, int replacementValue) {
        TypeToolEntity typeTool = typeToolRepo.findById(idTypeTool)
                .orElseThrow(() -> new EntityNotFoundException(TYPE_TOOL_NOT_FOUND + idTypeTool));

        typeTool.setReplacementValue(replacementValue);
        return typeToolRepo.save(typeTool);
    }

    // -------------------------------------------Utility
    // functions---------------------------------------------------------------

    /**
     * Helper 1: Returns the tool type according to the id
     *
     * @param idTypeTool type tool ID
     * @return TypeToolEntity
     */
    public TypeToolEntity getTypeToolById(long idTypeTool) {
        return typeToolRepo.findById(idTypeTool)
                .orElseThrow(() -> new EntityNotFoundException(TYPE_TOOL_NOT_FOUND + idTypeTool));
    }

    /**
     * Helper 2: Returns all tool types
     *
     * @return list of TypeToolEntity
     */
    public List<TypeToolEntity> getAllTypeTools() {
        return typeToolRepo.findAll();
    }

    /**
     * Helper 3: Returns all categories
     *
     * @return list of category names
     */
    public List<String> getAllTypeToolCategory() {
        return typeToolRepo.findDistinctCategory();
    }
}