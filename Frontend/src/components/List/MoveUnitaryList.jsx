import React, { useState, useEffect } from "react";
import { useParams } from 'react-router-dom';

import kardexService from '../../services/kardex.services';
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableContainer from '@mui/material/TableContainer';
import TableRow from '@mui/material/TableRow';
import Paper from '@mui/material/Paper';
import MuiTableCell from '@mui/material/TableCell';
import Pagination from '@mui/material/Pagination';
import Stack from '@mui/material/Stack';
import { Box, Typography, Button, FormControl, InputLabel, Select, MenuItem, TableSortLabel, CircularProgress } from '@mui/material';
import { StyledTableHead, StyledHeaderCell, StyledBodyRow } from '../Styles/TableStyles';
import { paginationStyles } from '../Styles/PaginationStyles.jsx';
import { usePagination } from '../../hooks/usePagination';
import { useSort } from '../../hooks/useSort';
import { getErrorMessage } from '../../utils/errorHandler.js';
import RefreshIcon from '@mui/icons-material/Refresh';


const MoveUnitaryList = () => {

  const { id } = useParams();

  const formatDateTime = (isoString) => {
    const d = new Date(isoString);
    const fecha = d.toLocaleDateString('es-CL', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric'
    });
    const hora = d.toLocaleTimeString('es-CL', {
      hour: '2-digit',
      minute: '2-digit',
    });
    return `${hora} ${fecha}`;
  };

  const [movement, setMovement] = useState([]);
  const [reload, setReload] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchMovements = async () => {
      setLoading(true);
      setError(null);
      try {
        const response = await kardexService.getAllMovementsOfTool(id);
        setMovement(response.data || []);
      } catch (err) {
        console.error("Error al obtener movimientos", err);
        setMovement([]);
        setError(getErrorMessage(err));
      } finally {
        setLoading(false);
      }
    };

    if (id) {
      fetchMovements();
    }
  }, [id, reload]);

  const { sortedItems, requestSort, sortConfig } = useSort(movement, { key: 'idKardex', direction: 'asc' });

  /*--------------------------Paginación-------------------- */
  const { currentPage, totalPages, currentItems: itemsCurrentPage, itemsPerPage, handlePageChange, handleItemsPerPageChange, resetPage } = usePagination(sortedItems);


  const createSortHandler = (property) => () => {
    requestSort(property);
  };

  return (
    <Box sx={{ padding: '16px' }}>
      <Typography variant="h5" component="h1" sx={{ mb: 2, fontWeight: 'bold' }}>
        Movimientos <br></br>
      </Typography>
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
                    active={sortConfig?.key === 'idKardex'}
                    direction={sortConfig?.key === 'idKardex' ? sortConfig.direction : 'asc'}
                    onClick={createSortHandler('idKardex')}
                    sx={{ color: 'inherit !important' }}
                  >
                    ID Mov.
                  </TableSortLabel>
                </StyledHeaderCell>
                <StyledHeaderCell key="head-type" align="center">
                  <TableSortLabel
                    active={sortConfig?.key === 'typeMove'}
                    direction={sortConfig?.key === 'typeMove' ? sortConfig.direction : 'asc'}
                    onClick={createSortHandler('typeMove')}
                    sx={{ color: 'inherit !important' }}
                  >
                    Tipo
                  </TableSortLabel>
                </StyledHeaderCell>
                <StyledHeaderCell key="head-date" align="center">
                  <TableSortLabel
                    active={sortConfig?.key === 'date'}
                    direction={sortConfig?.key === 'date' ? sortConfig.direction : 'asc'}
                    onClick={createSortHandler('date')}
                    sx={{ color: 'inherit !important' }}
                  >
                    Fecha
                  </TableSortLabel>
                </StyledHeaderCell>
                <StyledHeaderCell key="head-loan" align="center">
                  <TableSortLabel
                    active={sortConfig?.key === 'loan.idLoan'}
                    direction={sortConfig?.key === 'loan.idLoan' ? sortConfig.direction : 'asc'}
                    onClick={createSortHandler('loan.idLoan')}
                    sx={{ color: 'inherit !important' }}
                  >
                    Prestamo
                  </TableSortLabel>
                </StyledHeaderCell>
                <StyledHeaderCell key="head-user" align="center">
                  <TableSortLabel
                    active={sortConfig?.key === 'user.username'}
                    direction={sortConfig?.key === 'user.username' ? sortConfig.direction : 'asc'}
                    onClick={createSortHandler('user.username')}
                    sx={{ color: 'inherit !important' }}
                  >
                    Usuario
                  </TableSortLabel>
                </StyledHeaderCell>
              </TableRow>
            </StyledTableHead>
            <TableBody>
              {loading ? (
                <TableRow>
                  <MuiTableCell colSpan={5} align="center" sx={{ py: 4 }}>
                    <CircularProgress size={28} sx={{ color: '#4E7D10', mr: 1.5, verticalAlign: 'middle' }} />
                    <Typography component="span" variant="body2" color="text.secondary">
                      Cargando datos...
                    </Typography>
                  </MuiTableCell>
                </TableRow>
              ) : error ? (
                <TableRow>
                  <MuiTableCell colSpan={5} align="center" sx={{ py: 4 }}>
                    <Typography variant="body2" color="error" sx={{ mb: 1 }}>
                      {typeof error === 'string' ? error : "Error al mostrar datos"}
                    </Typography>
                    <Button variant="outlined" color="error" size="small" onClick={() => setReload(!reload)} startIcon={<RefreshIcon />}>
                      Reintentar
                    </Button>
                  </MuiTableCell>
                </TableRow>
              ) : itemsCurrentPage.length === 0 ? (
                <TableRow>
                  <MuiTableCell colSpan={5} align="center" sx={{ py: 4 }}>
                    <Typography variant="body1" color="text.secondary" sx={{ mb: 1 }}>
                      No se encontraron movimientos para esta herramienta.
                    </Typography>
                  </MuiTableCell>
                </TableRow>
              ) : (
                itemsCurrentPage.map((move) => (
                  <StyledBodyRow key={move.idKardex}>
                    <MuiTableCell align="center">{move.idKardex}</MuiTableCell>
                    <MuiTableCell align="center">{move.typeMove === 'LOAN' ? 'PRÉSTAMO'
                      : move.typeMove === 'TOOL_RETURN' ? 'DEVOLUCIÓN'
                        : move.typeMove === 'TOOL_REPAIR' ? 'REPARACIÓN'
                          : move.typeMove === 'TOOL_REGISTER' ? 'REGISTRO DE HERRAMIENTA'
                            : move.typeMove === 'TOOL_REMOVE' ? 'ELIMINACIÓN DE HERRAMIENTA'
                              : move.typeMove === 'DECOMMISSIONED' ? 'DADA DE BAJA'
                                : 'OTRO'}</MuiTableCell>
                    <MuiTableCell align="center">{formatDateTime(move.date)}</MuiTableCell>
                    <MuiTableCell align="center">{move.loan?.idLoan || 'Sin prestamo asociado'}</MuiTableCell>
                    <MuiTableCell align="center">{move.user?.username || 'Sin usuario'}</MuiTableCell>
                  </StyledBodyRow>
                ))
              )}
            </TableBody>
          </Table>
        </TableContainer>

        <div className="pagination-container" style={{ marginTop: '20px', display: 'flex', justifyContent: 'center' }}>
          <Stack direction="row" spacing={2} sx={{ alignItems: 'center', justifyContent: 'center' }}>
            <Pagination
              count={totalPages}
              page={currentPage}
              onChange={handlePageChange}
              color="primary"
              variant="outlined"
              sx={paginationStyles}
            />
            <FormControl size="small" sx={{ minWidth: 100 }}>
              <InputLabel id="per-page-label">Filas</InputLabel>
              <Select
                labelId="per-page-label"
                value={itemsPerPage}
                label="Filas"
                onChange={handleItemsPerPageChange}
              >
                <MenuItem value={5}>5</MenuItem>
                <MenuItem value={10}>10</MenuItem>
                <MenuItem value={15}>15</MenuItem>
              </Select>
            </FormControl>
          </Stack>
        </div>

      </div>
    </Box>
  );
}

export default MoveUnitaryList;