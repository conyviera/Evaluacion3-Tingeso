package com.example.ProyectoTingeso.Services;

import com.example.ProyectoTingeso.Entities.KardexDetailEntity;
import com.example.ProyectoTingeso.Entities.KardexEntity;
import com.example.ProyectoTingeso.Entities.ToolEntity;
import com.example.ProyectoTingeso.Repositories.KardexDetailRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class KardexDetailServiceTest {

    KardexDetailService kardexDetailService;

    @Mock
    KardexDetailRepository kardexDetailRepo;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        kardexDetailService = new KardexDetailService(kardexDetailRepo);
    }


    @Test
    void whenCreateKardexDetail_thenKardexDetailSaved() {
        //Given
        KardexEntity kardex = new KardexEntity();
        kardex.setTypeMove("LOAN");

        ToolEntity tool = new ToolEntity();
        tool.setIdTool(1L);

        KardexDetailEntity kardexDetailSaved = new KardexDetailEntity();
        kardexDetailSaved.setKardex(kardex);
        kardexDetailSaved.setTool(tool);

        when(kardexDetailRepo.save(any(KardexDetailEntity.class))).thenReturn(kardexDetailSaved);

        //When
        KardexDetailEntity result = kardexDetailService.createKardexDetail(kardex, tool);

        //Then
        assertThat(result).isNotNull();
        assertThat(result.getKardex().getTypeMove()).isEqualTo("LOAN");
        assertThat(result.getTool().getIdTool()).isEqualTo(1L);
        verify(kardexDetailRepo, times(1)).save(any(KardexDetailEntity.class));
    }


    @Test
    void whenCreateKardexDetailWithNullKardex_thenSaveWithoutKardex() {
        //Given
        KardexEntity kardex = null;

        ToolEntity tool = new ToolEntity();
        tool.setIdTool(2L);

        KardexDetailEntity kardexDetailSaved = new KardexDetailEntity();
        kardexDetailSaved.setKardex(kardex);
        kardexDetailSaved.setTool(tool);

        when(kardexDetailRepo.save(any(KardexDetailEntity.class))).thenReturn(kardexDetailSaved);

        //When
        KardexDetailEntity result = kardexDetailService.createKardexDetail(kardex, tool);

        //Then
        assertThat(result).isNotNull();
        assertThat(result.getKardex()).isNull();
        assertThat(result.getTool().getIdTool()).isEqualTo(2L);
        verify(kardexDetailRepo, times(1)).save(any(KardexDetailEntity.class));
    }


    @Test
    void whenCreateKardexDetailWithNullTool_thenSaveWithoutTool() {
        //Given
        KardexEntity kardex = new KardexEntity();
        kardex.setTypeMove("TOOL_RETURN");

        ToolEntity tool = null;

        KardexDetailEntity kardexDetailSaved = new KardexDetailEntity();
        kardexDetailSaved.setKardex(kardex);
        kardexDetailSaved.setTool(tool);

        when(kardexDetailRepo.save(any(KardexDetailEntity.class))).thenReturn(kardexDetailSaved);

        //When
        KardexDetailEntity result = kardexDetailService.createKardexDetail(kardex, tool);

        //Then
        assertThat(result).isNotNull();
        assertThat(result.getKardex().getTypeMove()).isEqualTo("TOOL_RETURN");
        assertThat(result.getTool()).isNull();
        verify(kardexDetailRepo, times(1)).save(any(KardexDetailEntity.class));
    }


    @Test
    void whenFindAllByTool_thenReturnKardexDetailList() {
        //Given
        ToolEntity tool = new ToolEntity();
        tool.setIdTool(1L);

        List<KardexDetailEntity> kardexDetailList = new ArrayList<>();

        KardexDetailEntity detail1 = new KardexDetailEntity();
        KardexEntity kardex1 = new KardexEntity();
        kardex1.setTypeMove("LOAN");
        detail1.setKardex(kardex1);
        detail1.setTool(tool);

        KardexDetailEntity detail2 = new KardexDetailEntity();
        KardexEntity kardex2 = new KardexEntity();
        kardex2.setTypeMove("TOOL_RETURN");
        detail2.setKardex(kardex2);
        detail2.setTool(tool);

        KardexDetailEntity detail3 = new KardexDetailEntity();
        KardexEntity kardex3 = new KardexEntity();
        kardex3.setTypeMove("TOOL_REGISTER");
        detail3.setKardex(kardex3);
        detail3.setTool(tool);

        kardexDetailList.add(detail1);
        kardexDetailList.add(detail2);
        kardexDetailList.add(detail3);

        when(kardexDetailRepo.findAllByTool(tool)).thenReturn(kardexDetailList);

        //When
        List<KardexDetailEntity> result = kardexDetailService.findAllByTool(tool);

        //Then
        assertThat(result).hasSize(3);
        assertThat(result.get(0).getKardex().getTypeMove()).isEqualTo("LOAN");
        assertThat(result.get(1).getKardex().getTypeMove()).isEqualTo("TOOL_RETURN");
        assertThat(result.get(2).getKardex().getTypeMove()).isEqualTo("TOOL_REGISTER");
        verify(kardexDetailRepo, times(1)).findAllByTool(tool);
    }


    @Test
    void whenFindAllByToolNotFound_thenReturnEmptyList() {
        //Given
        ToolEntity tool = new ToolEntity();
        tool.setIdTool(999L);

        when(kardexDetailRepo.findAllByTool(tool)).thenReturn(new ArrayList<>());

        //When
        List<KardexDetailEntity> result = kardexDetailService.findAllByTool(tool);

        //Then
        assertThat(result).isEmpty();
        verify(kardexDetailRepo, times(1)).findAllByTool(tool);
    }


    @Test
    void whenCreateMultipleKardexDetails_thenAllSaved() {
        //Given
        KardexEntity kardex1 = new KardexEntity();
        kardex1.setTypeMove("LOAN");

        KardexEntity kardex2 = new KardexEntity();
        kardex2.setTypeMove("TOOL_RETURN");

        ToolEntity tool1 = new ToolEntity();
        tool1.setIdTool(1L);

        ToolEntity tool2 = new ToolEntity();
        tool2.setIdTool(2L);

        KardexDetailEntity savedDetail1 = new KardexDetailEntity();
        savedDetail1.setKardex(kardex1);
        savedDetail1.setTool(tool1);

        KardexDetailEntity savedDetail2 = new KardexDetailEntity();
        savedDetail2.setKardex(kardex2);
        savedDetail2.setTool(tool2);

        when(kardexDetailRepo.save(any(KardexDetailEntity.class)))
                .thenReturn(savedDetail1)
                .thenReturn(savedDetail2);

        //When
        KardexDetailEntity result1 = kardexDetailService.createKardexDetail(kardex1, tool1);
        KardexDetailEntity result2 = kardexDetailService.createKardexDetail(kardex2, tool2);

        //Then
        assertThat(result1.getKardex().getTypeMove()).isEqualTo("LOAN");
        assertThat(result2.getKardex().getTypeMove()).isEqualTo("TOOL_RETURN");
        verify(kardexDetailRepo, times(2)).save(any(KardexDetailEntity.class));
    }

}
