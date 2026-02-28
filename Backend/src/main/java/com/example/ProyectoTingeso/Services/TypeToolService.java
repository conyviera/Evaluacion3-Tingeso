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

    private final TypeToolRepository typeToolRepo;

    @Autowired
    public TypeToolService(TypeToolRepository typeToolRepo) {
        this.typeToolRepo = typeToolRepo;
    }

    /**
     * Helper 1.1: Searches for a tool type by name. If it does not exist, it creates it. This prevents duplicate types in the database.
     * @param name
     * @param category
     * @param replacementValue
     * @param dailyRate
     * @param debtRate
     * @return
     */
    public TypeToolEntity findOrCreateTypeTool(String name, String category, int replacementValue,  int dailyRate, int debtRate) {

        Optional<TypeToolEntity> existingTypeTool =
                typeToolRepo.findByNameAndCategoryAndReplacementValueAndDailyRate(name, category, replacementValue, dailyRate);

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
     * @param idTypeTool
     * @param dailyRate
     */

    public TypeToolEntity configurationDailyRateTypeTool(long idTypeTool, int  dailyRate) {

        TypeToolEntity typeTool = typeToolRepo.findById(idTypeTool)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró el tipo de herramienta con id: " + idTypeTool));

        if (dailyRate < 0) {
            throw new EntityNotFoundException("La tarifa diaria no puede ser nulo, ni menor a 0");
        }

        typeTool.setDailyRate(dailyRate);
        typeToolRepo.save(typeTool);

        return typeTool;
    }

    /**
     * RF 4.2: Set daily late fee rate
     * @param idTypeTool
     * @param debtRate
     */

    public TypeToolEntity configurationDebtTypeTool(long idTypeTool, int  debtRate) {
        TypeToolEntity typeTool = typeToolRepo.findById(idTypeTool)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró el tipo de herramienta con id: " + idTypeTool));

        typeTool.setDebtRate(debtRate);
        return typeToolRepo.save(typeTool);
    }

    /**
     * RF 4.3: Records replacement value of a tool
     * @param idTypeTool
     * @param replacementValue
     */
    //Solo administrador
    public TypeToolEntity registerReplacementTypeTool(long idTypeTool, int  replacementValue) {

        TypeToolEntity typeTool = typeToolRepo.findById(idTypeTool)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró el tipo de herramienta con id: " + idTypeTool));

        typeTool.setReplacementValue(replacementValue);
        return typeToolRepo.save(typeTool);
    }

    //-------------------------------------------Utility functions---------------------------------------------------------------

    /**
     * Helper 1: Returns the tool type according to the id
     * @param idTypeTool
     * @return
     */
    public TypeToolEntity getTypeToolById(long idTypeTool) {

        TypeToolEntity typeTool = typeToolRepo.findById(idTypeTool)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró el tipo de herramienta con id: " + idTypeTool));

        return typeTool;
    }

    /**
     * Helper 2: Returns all tool types
     */
    public List<TypeToolEntity> getAllTypeTools () {
        List<TypeToolEntity> typeTools = typeToolRepo.findAll();
        return typeTools;
    }


    /**
     * Helper 3: Returns all categories
     * @return
     */
    public List<String> getAllTypeToolCategory (){
        List<String> category= typeToolRepo.findDistinctCategory();

        return category;
    }


}