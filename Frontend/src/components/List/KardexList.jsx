import React from "react";
import { CircularProgress, Typography, TableSortLabel, Button } from '@mui/material';
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableContainer from '@mui/material/TableContainer';
import TableRow from '@mui/material/TableRow';
import Paper from '@mui/material/Paper';
import MuiTableCell from '@mui/material/TableCell';
import { StyledTableHead, StyledHeaderCell, StyledBodyRow } from '../Styles/TableStyles';
import RefreshIcon from '@mui/icons-material/Refresh';

const KardexList = ({ kardex, loading, error, requestSort, sortConfig, onRetry }) => {

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
              <StyledHeaderCell align="center">
                <TableSortLabel
                  active={sortConfig?.key === 'idKardex'}
                  direction={sortConfig?.key === 'idKardex' ? sortConfig.direction : 'asc'}
                  onClick={createSortHandler('idKardex')}
                  sx={{ color: 'inherit !important' }}
                >
                  ID
                </TableSortLabel>
              </StyledHeaderCell>
              <StyledHeaderCell align="center">
                <TableSortLabel
                  active={sortConfig?.key === 'date'}
                  direction={sortConfig?.key === 'date' ? sortConfig.direction : 'asc'}
                  onClick={createSortHandler('date')}
                  sx={{ color: 'inherit !important' }}
                >
                  Fecha
                </TableSortLabel>
              </StyledHeaderCell>
              <StyledHeaderCell align="center">
                <TableSortLabel
                  active={sortConfig?.key === 'typeMove'}
                  direction={sortConfig?.key === 'typeMove' ? sortConfig.direction : 'asc'}
                  onClick={createSortHandler('typeMove')}
                  sx={{ color: 'inherit !important' }}
                >
                  Tipo de movimiento
                </TableSortLabel>
              </StyledHeaderCell>
              <StyledHeaderCell align="center">
                <TableSortLabel
                  active={sortConfig?.key === 'quantity'}
                  direction={sortConfig?.key === 'quantity' ? sortConfig.direction : 'asc'}
                  onClick={createSortHandler('quantity')}
                  sx={{ color: 'inherit !important' }}
                >
                  Cantidad
                </TableSortLabel>
              </StyledHeaderCell>
              <StyledHeaderCell align="center">Nombre herramientas</StyledHeaderCell>
              <StyledHeaderCell align="center">
                <TableSortLabel
                  active={sortConfig?.key === 'loan.idLoan'}
                  direction={sortConfig?.key === 'loan.idLoan' ? sortConfig.direction : 'asc'}
                  onClick={createSortHandler('loan.idLoan')}
                  sx={{ color: 'inherit !important' }}
                >
                  ID prestamo
                </TableSortLabel>
              </StyledHeaderCell>
              <StyledHeaderCell align="center">
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
                <MuiTableCell colSpan={7} align="center" sx={{ py: 4 }}>
                  <CircularProgress size={28} sx={{ color: '#4E7D10', mr: 1.5, verticalAlign: 'middle' }} />
                  <Typography component="span" variant="body2" color="text.secondary">
                    Cargando datos...
                  </Typography>
                </MuiTableCell>
              </TableRow>
            ) : error ? (
              <TableRow>
                <MuiTableCell colSpan={7} align="center" sx={{ py: 4 }}>
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
            ) : kardex.length === 0 ? (
              <TableRow>
                <MuiTableCell colSpan={7} align="center" sx={{ py: 4 }}>
                  <Typography variant="body1" color="text.secondary" sx={{ mb: 1 }}>
                    No se encontraron movimientos registrados en el historial.
                  </Typography>
                </MuiTableCell>
              </TableRow>
            ) : (
              kardex.map((move) => (
                <StyledBodyRow
                  key={move.idKardex}
                >
                  <MuiTableCell align="center">{move.idKardex}</MuiTableCell>
                  <MuiTableCell align="center">{formatDateTime(move.date)}</MuiTableCell>
                  <MuiTableCell align="center">{move.typeMove === 'LOAN' ? 'PRÉSTAMO'
                    : move.typeMove === 'TOOL_RETURN' ? 'DEVOLUCIÓN'
                      : move.typeMove === 'TOOL_REPAIR' ? 'REPARACIÓN'
                        : move.typeMove === 'TOOL_REGISTER' ? 'REGISTRO DE HERRAMIENTA'
                          : move.typeMove === 'TOOL_REMOVE' ? 'ELIMINACIÓN DE HERRAMIENTA'
                            : move.typeMove === 'DECOMMISSIONED' ? 'DADA DE BAJA'
                              : 'OTRO'}</MuiTableCell>
                  <MuiTableCell align="center">{move.quantity}</MuiTableCell>
                  <MuiTableCell align="center">
                    {(Array.from(
                      new Set(
                        (move.kardexDetail || [])
                          .map(h => h?.tool?.typeTool?.name)
                          .filter(Boolean)
                      )
                    ).join(', ')
                    )
                      || 'Sin herramientas'
                    }
                  </MuiTableCell>
                  <MuiTableCell align="center">{move.loan?.idLoan || 'Sin prestamo asociado'}</MuiTableCell>
                  <MuiTableCell align="center">{move.user?.username || 'usuario sin nombre'}</MuiTableCell>
                </StyledBodyRow>
              ))
            )}
          </TableBody>
        </Table>
      </TableContainer>
    </div>
  );
}
export default KardexList;
