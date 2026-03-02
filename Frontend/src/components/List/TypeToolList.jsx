import React, { useState } from 'react';
import { CircularProgress, Typography, Box, IconButton, TableSortLabel, Button } from '@mui/material';
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableContainer from '@mui/material/TableContainer';
import TableRow from '@mui/material/TableRow';
import Paper from '@mui/material/Paper';
import MuiTableCell from '@mui/material/TableCell';
import { useNavigate } from "react-router-dom";
import EditIcon from '@mui/icons-material/Edit';
import BuildIcon from '@mui/icons-material/Build';
import AddIcon from '@mui/icons-material/Add';

import BasicModal from '../Modal.jsx';
import AddExistingToolForm from "../Form/AddExistingToolForm.jsx";
import UpdateTypeToolForm from '../Form/UpdateTypeToolForm.jsx';
import { StyledTableHead, StyledHeaderCell, StyledBodyRow } from '../Styles/TableStyles';
import RefreshIcon from '@mui/icons-material/Refresh';

const TypeToolList = ({ typeTool, onRefreshList, loading, error, requestSort, sortConfig, onRetry }) => {

  const navigate = useNavigate();

  const [addModalOpen, setAddModalOpen] = useState(false);
  const [editModalOpen, setEditModalOpen] = useState(false);
  const [selectedTool, setSelectedTool] = useState(null);

  const handleRowClick = (id) => {
    navigate(`/typeTool/${id}`);
  };

  const handleOpenModal = (tool) => {
    setSelectedTool(tool);
    setAddModalOpen(true);
  };

  const handleFormSubmitSuccess = () => {
    onRefreshList();
  };

  const colSpan = 8;

  const createSortHandler = (property) => () => {
    if (requestSort) requestSort(property);
  };

  return (
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
              <StyledHeaderCell key="head-id" align="center">
                <TableSortLabel
                  active={sortConfig?.key === 'idTypeTool'}
                  direction={sortConfig?.key === 'idTypeTool' ? sortConfig.direction : 'asc'}
                  onClick={createSortHandler('idTypeTool')}
                  sx={{ color: 'inherit !important' }}
                >
                  ID
                </TableSortLabel>
              </StyledHeaderCell>
              <StyledHeaderCell key="head-name" align="center">
                <TableSortLabel
                  active={sortConfig?.key === 'name'}
                  direction={sortConfig?.key === 'name' ? sortConfig.direction : 'asc'}
                  onClick={createSortHandler('name')}
                  sx={{ color: 'inherit !important' }}
                >
                  Nombre
                </TableSortLabel>
              </StyledHeaderCell>
              <StyledHeaderCell key="head-category" align="center">
                <TableSortLabel
                  active={sortConfig?.key === 'category'}
                  direction={sortConfig?.key === 'category' ? sortConfig.direction : 'asc'}
                  onClick={createSortHandler('category')}
                  sx={{ color: 'inherit !important' }}
                >
                  Categoria
                </TableSortLabel>
              </StyledHeaderCell>
              <StyledHeaderCell key="head-replacementValue" align="center">
                <TableSortLabel
                  active={sortConfig?.key === 'replacementValue'}
                  direction={sortConfig?.key === 'replacementValue' ? sortConfig.direction : 'asc'}
                  onClick={createSortHandler('replacementValue')}
                  sx={{ color: 'inherit !important' }}
                >
                  Valor de reposición
                </TableSortLabel>
              </StyledHeaderCell>
              <StyledHeaderCell key="head-dailyRate" align="center">
                <TableSortLabel
                  active={sortConfig?.key === 'dailyRate'}
                  direction={sortConfig?.key === 'dailyRate' ? sortConfig.direction : 'asc'}
                  onClick={createSortHandler('dailyRate')}
                  sx={{ color: 'inherit !important' }}
                >
                  Tarifa diaria
                </TableSortLabel>
              </StyledHeaderCell>
              <StyledHeaderCell key="head-debtRate" align="center">
                <TableSortLabel
                  active={sortConfig?.key === 'debtRate'}
                  direction={sortConfig?.key === 'debtRate' ? sortConfig.direction : 'asc'}
                  onClick={createSortHandler('debtRate')}
                  sx={{ color: 'inherit !important' }}
                >
                  Tarifa por día de atraso
                </TableSortLabel>
              </StyledHeaderCell>
              <StyledHeaderCell key="head-stock" align="center">
                <TableSortLabel
                  active={sortConfig?.key === 'stock'}
                  direction={sortConfig?.key === 'stock' ? sortConfig.direction : 'asc'}
                  onClick={createSortHandler('stock')}
                  sx={{ color: 'inherit !important' }}
                >
                  Disponibles
                </TableSortLabel>
              </StyledHeaderCell>
              <StyledHeaderCell key="head-button" align="center"> Acción</StyledHeaderCell>
            </TableRow>
          </StyledTableHead>
          <TableBody>
            {loading ? (
              <TableRow>
                <MuiTableCell colSpan={colSpan} align="center" sx={{ py: 4 }}>
                  <CircularProgress size={28} sx={{ color: '#4E7D10', mr: 1.5, verticalAlign: 'middle' }} />
                  <Typography component="span" variant="body2" color="text.secondary">
                    Cargando datos...
                  </Typography>
                </MuiTableCell>
              </TableRow>
            ) : error ? (
              <TableRow>
                <MuiTableCell colSpan={colSpan} align="center" sx={{ py: 4 }}>
                  <Typography variant="body2" color="error" sx={{ mb: 1 }}>
                    {typeof error === 'string' ? error : "Error al mostrar datos"}
                  </Typography>
                  {onRetry && (
                    <Button variant="outlined" color="error" size="small" onClick={onRetry} startIcon={<RefreshIcon />}>
                      Reintentar
                    </Button>
                  )}
                </MuiTableCell>
              </TableRow>
            ) : typeTool.length === 0 ? (
              <TableRow>
                <MuiTableCell colSpan={colSpan} align="center" sx={{ py: 4 }}>
                  <Typography variant="body1" color="text.secondary" sx={{ mb: 1 }}>
                    No se encontraron herramientas registradas en el inventario.
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    Puedes agregar una nueva categoría utilizando el botón superior "Añadir herramienta".
                  </Typography>
                </MuiTableCell>
              </TableRow>
            ) : (
              typeTool.map((tool) => (
                <StyledBodyRow key={tool.idTypeTool}>
                  <MuiTableCell align="center">{tool.idTypeTool}</MuiTableCell>
                  <MuiTableCell align="center">{tool.name}</MuiTableCell>
                  <MuiTableCell align="center">{tool.category}</MuiTableCell>
                  <MuiTableCell align="center">$ {tool.replacementValue}</MuiTableCell>
                  <MuiTableCell align="center">$ {tool.dailyRate}</MuiTableCell>
                  <MuiTableCell align="center">$ {tool.debtRate}</MuiTableCell>
                  <MuiTableCell align="center">{tool.stock}</MuiTableCell>
                  <MuiTableCell align="center">
                    <Box display="flex" justifyContent="center" alignItems="center">
                      <BasicModal
                        open={addModalOpen && selectedTool?.idTypeTool === tool.idTypeTool}
                        handleClose={() => setAddModalOpen(false)}
                        button={
                          <IconButton
                            onClick={() => handleOpenModal(tool)}
                            sx={{
                              color: '#4E7D10',
                              '&:hover': {
                                filter: 'drop-shadow(0 0 10px #4E7D10)',
                              },
                            }}
                            title="Agregar stock a la herramienta"
                          >
                            <AddIcon />
                          </IconButton>
                        }
                      >
                        <AddExistingToolForm
                          typeTool={tool}
                          onToolAdded={handleFormSubmitSuccess} />
                      </BasicModal>
                      <BasicModal
                        open={editModalOpen && selectedTool?.idTypeTool === tool.idTypeTool}
                        handleClose={() => setEditModalOpen(false)}
                        button={
                          <IconButton
                            onClick={() => { setSelectedTool(tool); setEditModalOpen(true); }}
                            sx={{
                              color: '#4E7D10',
                              '&:hover': {
                                filter: 'drop-shadow(0 0 10px #4E7D10)',
                              },
                            }}
                            title="Editar tipo de herramienta"
                          >
                            <EditIcon />
                          </IconButton>
                        }
                      >
                        <UpdateTypeToolForm idTypeTool={tool.idTypeTool} onUpdate={onRefreshList} />
                      </BasicModal>
                      <IconButton
                        onClick={() => handleRowClick(tool.idTypeTool)}
                        sx={{
                          color: '#4E7D10',
                          '&:hover': {
                            filter: 'drop-shadow(0 0 10px #4E7D10)',
                          }
                        }}
                        title="Ver herramientas unitarias"
                      >
                        <BuildIcon />
                      </IconButton>
                    </Box>
                  </MuiTableCell>
                </StyledBodyRow>
              ))
            )}
          </TableBody>
        </Table>
      </TableContainer>
    </div>
  );
}
export default TypeToolList;
