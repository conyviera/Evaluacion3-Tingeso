import React from 'react';
import { CircularProgress, Typography, TableSortLabel, Box, Button } from '@mui/material';
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableContainer from '@mui/material/TableContainer';
import TableRow from '@mui/material/TableRow';
import Paper from '@mui/material/Paper';
import MuiTableCell from '@mui/material/TableCell';
import { StyledTableHead, StyledHeaderCell, StyledBodyRow } from '../Styles/TableStyles';
import RefreshIcon from '@mui/icons-material/Refresh';

const CustomerList = ({ customers, loading, error, requestSort, sortConfig, onRetry }) => {

  const colSpan = 6;

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
                  active={sortConfig?.key === 'idCustomer'}
                  direction={sortConfig?.key === 'idCustomer' ? sortConfig.direction : 'asc'}
                  onClick={createSortHandler('idCustomer')}
                  sx={{ color: 'inherit !important' }}
                >
                  ID Cliente
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
              <StyledHeaderCell key="head-rut" align="center">
                <TableSortLabel
                  active={sortConfig?.key === 'rut'}
                  direction={sortConfig?.key === 'rut' ? sortConfig.direction : 'asc'}
                  onClick={createSortHandler('rut')}
                  sx={{ color: 'inherit !important' }}
                >
                  RUT
                </TableSortLabel>
              </StyledHeaderCell>
              <StyledHeaderCell key="head-phoneNumber" align="center">
                <TableSortLabel
                  active={sortConfig?.key === 'phoneNumber'}
                  direction={sortConfig?.key === 'phoneNumber' ? sortConfig.direction : 'asc'}
                  onClick={createSortHandler('phoneNumber')}
                  sx={{ color: 'inherit !important' }}
                >
                  Teléfono
                </TableSortLabel>
              </StyledHeaderCell>
              <StyledHeaderCell key="head-email" align="center">
                <TableSortLabel
                  active={sortConfig?.key === 'email'}
                  direction={sortConfig?.key === 'email' ? sortConfig.direction : 'asc'}
                  onClick={createSortHandler('email')}
                  sx={{ color: 'inherit !important' }}
                >
                  Email
                </TableSortLabel>
              </StyledHeaderCell>
              <StyledHeaderCell key="head-state" align="center">
                <TableSortLabel
                  active={sortConfig?.key === 'state'}
                  direction={sortConfig?.key === 'state' ? sortConfig.direction : 'asc'}
                  onClick={createSortHandler('state')}
                  sx={{ color: 'inherit !important' }}
                >
                  Estado
                </TableSortLabel>
              </StyledHeaderCell>
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
            ) : customers.length === 0 ? (
              <TableRow>
                <MuiTableCell colSpan={colSpan} align="center" sx={{ py: 4 }}>
                  <Typography variant="body1" color="text.secondary" sx={{ mb: 1 }}>
                    No se encontraron clientes registrados.
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    Puedes agregar uno nuevo utilizando el botón superior "Añadir Cliente".
                  </Typography>
                </MuiTableCell>
              </TableRow>
            ) : (
              customers.map((cust) => (
                <StyledBodyRow key={cust.idCustomer}>
                  <MuiTableCell align="center">{cust.idCustomer}</MuiTableCell>
                  <MuiTableCell align="center">{cust.name}</MuiTableCell>
                  <MuiTableCell align="center">{cust.rut}</MuiTableCell>
                  <MuiTableCell align="center">{cust.phoneNumber}</MuiTableCell>
                  <MuiTableCell align="center">{cust.email}</MuiTableCell>
                  <MuiTableCell align="center">
                    {cust.state === 'ACTIVE' ? (
                      <span style={{ color: 'green' }}>Activo</span>
                    ) : (
                      <span style={{ color: 'red' }}>Restringido</span>
                    )}
                  </MuiTableCell>
                </StyledBodyRow>
              ))
            )}
          </TableBody>
        </Table>
      </TableContainer >
    </div >
  );
}
export default CustomerList;