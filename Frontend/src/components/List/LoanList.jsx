import React from "react";
import PropTypes from 'prop-types';
import { IconButton, CircularProgress, Typography, TableSortLabel, Button } from "@mui/material";
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableContainer from '@mui/material/TableContainer';
import TableRow from '@mui/material/TableRow';
import Paper from '@mui/material/Paper';
import MuiTableCell from '@mui/material/TableCell';
import ArrowForwardIcon from '@mui/icons-material/ArrowForward';
import { useNavigate } from "react-router-dom";
import { StyledTableHead, StyledHeaderCell, StyledBodyRow } from '../Styles/TableStyles';
import RefreshIcon from '@mui/icons-material/Refresh';

const renderLoanState = (state) => {
  if (state === 'ACTIVE') return 'ACTIVO';
  if (state === 'RETURNED') return 'DEVUELTO';
  if (state === 'EXPIRED') return 'VENCIDO';
  return 'OTRO';
};

const LoanList = ({ loans, loading, error, requestSort, sortConfig, onRetry }) => {

  const navigate = useNavigate();

  const handleRowClick = (id) => {
    navigate(`/DebtList/${id}`);
  }

  const colSpan = 8;

  const createSortHandler = (property) => () => {
    if (requestSort) requestSort(property);
  };

  const renderLoanBody = () => {
    if (loading) {
      return (
        <TableRow>
          <MuiTableCell colSpan={colSpan} align="center" sx={{ py: 4 }}>
            <CircularProgress size={28} sx={{ color: '#4E7D10', mr: 1.5, verticalAlign: 'middle' }} />
            <Typography component="span" variant="body2" color="text.secondary">
              Cargando datos...
            </Typography>
          </MuiTableCell>
        </TableRow>
      );
    }
    if (error) {
      return (
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
      );
    }
    if (loans.length === 0) {
      return (
        <TableRow>
          <MuiTableCell colSpan={colSpan} align="center" sx={{ py: 4 }}>
            <Typography variant="body1" color="text.secondary" sx={{ mb: 1 }}>
              No se encontraron préstamos registrados.
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Puedes crear un nuevo préstamo desde el botón superior &quot;Añadir Préstamo&quot;.
            </Typography>
          </MuiTableCell>
        </TableRow>
      );
    }
    return loans.map((loan) => (
      <StyledBodyRow key={loan.idLoan}>
        <MuiTableCell align="center">{loan.idLoan}</MuiTableCell>
        <MuiTableCell align="center">{loan.customer?.name || 'Sin cliente'}</MuiTableCell>
        <MuiTableCell align="center">{loan.deliveryDate}</MuiTableCell>
        <MuiTableCell align="center">{loan.returnDate}</MuiTableCell>
        <MuiTableCell align="center">$ {loan.rentalAmount}</MuiTableCell>
        <MuiTableCell align="center">{renderLoanState(loan.state)}</MuiTableCell>
        <MuiTableCell align="center">
          {<IconButton
            onClick={() => handleRowClick(loan.idLoan)}
            sx={{
              color: '#4E7D10',
              '&:hover': {
                filter: 'drop-shadow(0 0 10px #4E7D10)',
              },
            }}
            title="Ver detalle de deuda"
          >
            <ArrowForwardIcon />
          </IconButton>}
        </MuiTableCell>
      </StyledBodyRow>
    ));
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
                  active={sortConfig?.key === 'idLoan'}
                  direction={sortConfig?.key === 'idLoan' ? sortConfig.direction : 'asc'}
                  onClick={createSortHandler('idLoan')}
                  sx={{ color: 'inherit !important' }}
                >
                  ID
                </TableSortLabel>
              </StyledHeaderCell>
              <StyledHeaderCell align="center">
                <TableSortLabel
                  active={sortConfig?.key === 'customer.name'}
                  direction={sortConfig?.key === 'customer.name' ? sortConfig.direction : 'asc'}
                  onClick={createSortHandler('customer.name')}
                  sx={{ color: 'inherit !important' }}
                >
                  Cliente
                </TableSortLabel>
              </StyledHeaderCell>
              <StyledHeaderCell align="center">Herramientas</StyledHeaderCell>
              <StyledHeaderCell align="center">
                <TableSortLabel
                  active={sortConfig?.key === 'deliveryDate'}
                  direction={sortConfig?.key === 'deliveryDate' ? sortConfig.direction : 'asc'}
                  onClick={createSortHandler('deliveryDate')}
                  sx={{ color: 'inherit !important' }}
                >
                  Día de arriendo
                </TableSortLabel>
              </StyledHeaderCell>
              <StyledHeaderCell align="center">
                <TableSortLabel
                  active={sortConfig?.key === 'returnDate'}
                  direction={sortConfig?.key === 'returnDate' ? sortConfig.direction : 'asc'}
                  onClick={createSortHandler('returnDate')}
                  sx={{ color: 'inherit !important' }}
                >
                  Día de retorno
                </TableSortLabel>
              </StyledHeaderCell>
              <StyledHeaderCell align="center">
                <TableSortLabel
                  active={sortConfig?.key === 'rentalAmount'}
                  direction={sortConfig?.key === 'rentalAmount' ? sortConfig.direction : 'asc'}
                  onClick={createSortHandler('rentalAmount')}
                  sx={{ color: 'inherit !important' }}
                >
                  Monto
                </TableSortLabel>
              </StyledHeaderCell>
              <StyledHeaderCell align="center">
                <TableSortLabel
                  active={sortConfig?.key === 'state'}
                  direction={sortConfig?.key === 'state' ? sortConfig.direction : 'asc'}
                  onClick={createSortHandler('state')}
                  sx={{ color: 'inherit !important' }}
                >
                  Estado
                </TableSortLabel>
              </StyledHeaderCell>
              <StyledHeaderCell align="center">Deuda Asociada</StyledHeaderCell>
            </TableRow>
          </StyledTableHead>
          <TableBody>
            {renderLoanBody()}
          </TableBody>
        </Table>
      </TableContainer>
    </div >
  );
}

LoanList.propTypes = {
  loans: PropTypes.arrayOf(PropTypes.shape({
    idLoan: PropTypes.number,
    customer: PropTypes.shape({
      idCustomer: PropTypes.number,
      name: PropTypes.string,
    }),
    tool: PropTypes.array,
    deliveryDate: PropTypes.string,
    returnDate: PropTypes.string,
    rentalAmount: PropTypes.number,
    state: PropTypes.string,
  })).isRequired,
  loading: PropTypes.bool,
  error: PropTypes.oneOfType([PropTypes.string, PropTypes.bool]),
  requestSort: PropTypes.func,
  sortConfig: PropTypes.shape({
    key: PropTypes.string,
    direction: PropTypes.string,
  }),
  onRetry: PropTypes.func,
};

export default LoanList;
