import React, { useState, useEffect } from "react";
import { useParams, useNavigate } from 'react-router-dom';
import toolService from '../../services/tool.services';
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableContainer from '@mui/material/TableContainer';
import TableRow from '@mui/material/TableRow';
import Paper from '@mui/material/Paper';
import MuiTableCell from '@mui/material/TableCell';
import Pagination from '@mui/material/Pagination';
import Stack from '@mui/material/Stack';
import { Box, Typography, IconButton, Grid, TextField, FormControl, InputLabel, Select, MenuItem } from '@mui/material';

// ICONS
import HistoryIcon from '@mui/icons-material/History';
import BlockOutlinedIcon from '@mui/icons-material/BlockOutlined';
import CarpenterIcon from '@mui/icons-material/Carpenter';

import DeactivateUnusedTool from '../Form/DeactivateUnusedToolForm';
import BasicModal from '../Modal';
import AssessToolDamageForm from '../Form/AssessToolDamageForm';
import { StyledTableHead, StyledHeaderCell, StyledBodyRow } from '../Styles/TableStyles';

// COMPONENTE PRINCIPAL
const ToolUnitaryList = () => {

  const { id } = useParams();
  const navigate = useNavigate();
  const [tool, setTool] = useState([]);

  const [reload, setReload] = useState(false);

  const [filters, setFilters] = useState({
    idTool: '',
    state: ''
  });

  useEffect(() => {
    const fetchTool = async () => {
      try {
        const response = await toolService.getAllByTypeTool(id);
        setTool(response.data || []);
      } catch (error) {
        console.error("Error al obtener herramientas:", error);
        setTool([]);
      }
    };

    if (id) {
      fetchTool();
    }
  }, [id, reload]);

  const handleToolUpdate = () => {
    setReload(!reload);
  };

  /*-------------------------- Paginación y Filtros -------------------- */
  const ITEM_PER_PAGE = 10;
  const [currentPage, setCurrentPage] = useState(1);
  const handlePageChange = (_event, value) => setCurrentPage(value);

  const handleFilterChange = (event) => {
    const { name, value } = event.target;
    setFilters(prev => ({ ...prev, [name]: value }));
    setCurrentPage(1);
  };

  const filteredTools = tool.filter((t) => {
    const idFilter = String(filters.idTool).toLowerCase();
    const stateFilter = filters.state;

    const toolIdString = String(t.idTool || '').toLowerCase();

    const matchesId = !idFilter || toolIdString.includes(idFilter);

    const matchesState = !stateFilter || (t.state && t.state === stateFilter);

    return matchesId && matchesState;
  });

  const totalItem = Math.max(1, Math.ceil(filteredTools.length / ITEM_PER_PAGE));
  const indexOfLastItem = currentPage * ITEM_PER_PAGE;
  const indexOfFirstItem = indexOfLastItem - ITEM_PER_PAGE;

  const itemsCurrentPage = filteredTools.slice(indexOfFirstItem, indexOfLastItem);

  const headerInfo = tool[0] || {};

  const handleRowClick = (idTool) => {
    navigate(`/MovementsTool/${idTool}`);
  }

  return (
    <Box sx={{ padding: '16px' }}>
      <Typography variant="h5" component="h1" sx={{ mb: 2, fontWeight: 'bold' }}>
        Herramientas Unitarias
      </Typography>

      <Typography sx={{ mb: 1 }}>
        Nombre: {headerInfo.typeTool?.name || ''} |
        Categoria: {headerInfo.typeTool?.category || ''} <br></br>
        Valor de reposición: {headerInfo.typeTool?.replacementValue || ''} |
        Tarifa diaria: {headerInfo.typeTool?.dailyRate || ''} |
        Tarifa de deuda por atraso: {headerInfo.typeTool?.debtRate || ''}
      </Typography>

      <Box sx={{ mb: 2, mt: 3 }}>
        <Grid container spacing={2}>

          <Grid item xs={12} sm={6} md={4}>
            <TextField
              fullWidth
              label="Buscar por ID Herramienta"
              variant="outlined"
              size="small"
              name="idTool"
              value={filters.idTool}
              onChange={handleFilterChange}
              placeholder="Ej: 1"
            />
          </Grid>

          <Grid item xs={12} sm={6} md={4}>
            <FormControl fullWidth size="small">
              <InputLabel>Estado</InputLabel>
              <Select
                label="Estado"
                name="state"
                value={filters.state}
                onChange={handleFilterChange}
              >
                <MenuItem value=""><em>Todos</em></MenuItem>
                <MenuItem value="AVAILABLE">Disponible (Available)</MenuItem>
                <MenuItem value="ON_LOAN">Prestada (On Loan)</MenuItem>
                <MenuItem value="UNDER_REPAIR">En reparación (Under Repair)</MenuItem>
                <MenuItem value="DECOMMISSIONED">Dada de baja (Decommissioned)</MenuItem>
              </Select>
            </FormControl>
          </Grid>

        </Grid>
      </Box>

      <div className="table-container">
        <TableContainer
          component={Paper}
          sx={{
            borderRadius: '10px',
            overflow: 'hidden'
          }}>
          <Table sx={{ minWidth: 800 }} aria-label="simple table">
            <StyledTableHead>
              <TableRow>
                <StyledHeaderCell key="head-id" align="center">ID herramienta</StyledHeaderCell>
                <StyledHeaderCell key="head-state" align="center">Estado</StyledHeaderCell>
                <StyledHeaderCell key="head-boton" align="center"> Ver historial </StyledHeaderCell>
                <StyledHeaderCell key="head-boton2" align="center"> Dar de baja o reparar </StyledHeaderCell>
              </TableRow>
            </StyledTableHead>
            <TableBody>
              {itemsCurrentPage.length > 0 ? (
                itemsCurrentPage.map((toolItem) => (
                  <StyledBodyRow key={toolItem.idTool}>
                    <MuiTableCell align="center">{toolItem.idTool}</MuiTableCell>
                    <MuiTableCell align="center">{toolItem.state}</MuiTableCell>
                    <MuiTableCell align="center">

                      <IconButton
                        onClick={() => handleRowClick(toolItem.idTool)}
                        sx={{ color: '#4E7D10' }}
                        title="Ver Historial"
                      >
                        <HistoryIcon />
                      </IconButton>
                    </MuiTableCell>
                    <MuiTableCell align="center">
                      {
                        toolItem.state === 'AVAILABLE' ? (
                          <BasicModal
                            button={
                              <IconButton
                                sx={{
                                  color: '#d32f2f',
                                  '&:hover': { filter: 'drop-shadow(0 0 5px red)' }
                                }}
                                title="Dar de baja"
                              >
                                <BlockOutlinedIcon />
                              </IconButton>
                            }
                          >
                            <DeactivateUnusedTool id={toolItem.idTool} onUpdate={handleToolUpdate} />
                          </BasicModal>
                        ) : (toolItem.state === 'UNDER_REPAIR' ? (
                          <BasicModal
                            button={
                              <IconButton
                                sx={{
                                  color: '#d32f2f',
                                  '&:hover': { filter: 'drop-shadow(0 0 5px red)' }
                                }}
                                title="Evaluar daño"
                              >
                                <CarpenterIcon />
                              </IconButton>
                            }
                          >
                            <AssessToolDamageForm tool={toolItem} onAssessmentComplete={handleToolUpdate} />
                          </BasicModal>
                        ) : (
                          <Typography variant="body2" color="textSecondary">
                            Reparación no disponible
                          </Typography>
                        )
                        )
                      }

                    </MuiTableCell>
                  </StyledBodyRow>
                ))
              ) : (
                <TableRow>
                  <MuiTableCell colSpan={4} align="center">
                    No se encontraron herramientas con esos filtros.
                  </MuiTableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        </TableContainer>

        <div className="pagination-container" style={{ marginTop: '20px', display: 'flex', justifyContent: 'center' }}>
          <Stack spacing={2}>
            <Pagination
              count={totalItem}
              page={currentPage}
              onChange={handlePageChange}
              color="primary"
              variant="outlined"
            />
          </Stack>
        </div>
      </div>
    </Box>
  );
}

export default ToolUnitaryList;