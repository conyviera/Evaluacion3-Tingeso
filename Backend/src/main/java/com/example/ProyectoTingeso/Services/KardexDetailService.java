package com.example.ProyectoTingeso.Services;

import com.example.ProyectoTingeso.Entities.KardexDetailEntity;
import com.example.ProyectoTingeso.Entities.KardexEntity;
import com.example.ProyectoTingeso.Entities.ToolEntity;
import com.example.ProyectoTingeso.Repositories.KardexDetailRepository;
import com.example.ProyectoTingeso.Repositories.KardexRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KardexDetailService {

    private final KardexDetailRepository kardexDetailRepo;

    public KardexDetailService(KardexDetailRepository kardexDetailRepo) {
        this.kardexDetailRepo = kardexDetailRepo;
    }

    public KardexDetailEntity createKardexDetail(KardexEntity kardex, ToolEntity tool){

        KardexDetailEntity kardexDetail = new KardexDetailEntity();

        kardexDetail.setKardex(kardex);
        kardexDetail.setTool(tool);

        KardexDetailEntity  save = kardexDetailRepo.save(kardexDetail);

        return save;
    }

    /**
     * Returns the idKardex of the movements of each tool
     * @param tool
     * @return
     */

    public List<KardexDetailEntity> findAllByTool(ToolEntity tool){

        List<KardexDetailEntity> KardexDetailList= kardexDetailRepo.findAllByTool(tool);
        return KardexDetailList;
    }
}
