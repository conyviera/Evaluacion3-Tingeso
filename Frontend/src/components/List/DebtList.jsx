import React, { useState, useEffect } from "react";
import { IconButton, Pagination, Stack, FormControl, InputLabel, Select, MenuItem, TableSortLabel, CircularProgress, Typography, Button } from "@mui/material";
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableContainer from '@mui/material/TableContainer';
import TableRow from '@mui/material/TableRow';
import Paper from '@mui/material/Paper';
import MuiTableCell from '@mui/material/TableCell';
import loanService from '../../services/loan.services.js';
import { useParams } from "react-router-dom";
import BasicModal from '../Modal.jsx';
import AttachMoneyIcon from '@mui/icons-material/AttachMoney';
import DebtPaidForm from '../Form/DebtPaidForm.jsx';
import { StyledTableHead, StyledHeaderCell, StyledBodyRow } from '../Styles/TableStyles';
import { paginationStyles } from '../Styles/PaginationStyles.jsx';
import { usePagination } from '../../hooks/usePagination';
import { useSort } from '../../hooks/useSort';
import { getErrorMessage } from '../../utils/errorHandler.js';
import RefreshIcon from '@mui/icons-material/Refresh';

const DebtList = ({ }) => {

  const { id } = useParams();
  const idLoan = id;

  const [debt, setDebt] = useState([]);
  const [reload, setReload] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchDebt = async () => {
      setLoading(true);
      setError(null);
      try {
        const response = await loanService.getDebtsByLoanId(idLoan);
        setDebt(response.data || []);
      } catch (err) {
        console.error("Error al obtener las deudas", err);
        setDebt([]);
        setError(getErrorMessage(err));
      } finally {
        setLoading(false);
      }
    };
    fetchDebt();
  }, [idLoan, reload]);

  const handleDebtPaid = () => {
    setReload(!reload);
  };

  const { sortedItems, requestSort, sortConfig } = useSort(debt, { key: 'idLoan', direction: 'asc' });

  const { currentPage, totalPages, currentItems: itemsCurrentPage, itemsPerPage, handlePageChange, handleItemsPerPageChange, resetPage } = usePagination(sortedItems);


  return (
    <div className="table-container">
      <div>
        <h4>Deudas Asociadas al Préstamo ID: {idLoan}</h4>
        {debt.length === 0 ?
          (<p>No hay deudas asociadas a este préstamo.</p>) :
          (<p>Total de deudas: {debt.length}</p>)
        }
      </div>
      <TableContainer
        component={Paper}
        sx={{
          borderRadius: '10px',
          overflow: 'hidden'
        }}>
        <Table sx={{ minWidth: 800 }} aria-label="simple table">
          <StyledTableHead>
            <TableRow>
              <StyledHeaderCell align="center">ID</StyledHeaderCell>
              <StyledHeaderCell align="center">Tipo</StyledHeaderCell>
              <StyledHeaderCell align="center">Monto</StyledHeaderCell>
              <StyledHeaderCell align="center">Fecha creación</StyledHeaderCell>
              <StyledHeaderCell align="center">Fecha pago</StyledHeaderCell>
              <StyledHeaderCell align="center">Estado</StyledHeaderCell>
              <StyledHeaderCell align="center">Herramientas</StyledHeaderCell>
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
                  <Button variant="outlined" color="error" size="small" onClick={() => setReload(!reload)} startIcon={<RefreshIcon />}>
                    Reintentar
                  </Button>
                </MuiTableCell>
              </TableRow>
            ) : itemsCurrentPage.length === 0 ? (
              <TableRow>
                <MuiTableCell colSpan={7} align="center" sx={{ py: 4 }}>
                  <Typography variant="body1" color="text.secondary" sx={{ mb: 1 }}>
                    No se encontraron deudas asociadas a este préstamo.
                  </Typography>
                </MuiTableCell>
              </TableRow>
            ) : (
              itemsCurrentPage.map((debtUnitary) => (
                <StyledBodyRow
                  key={debtUnitary.idDebts}
                >
                  <MuiTableCell align="center">{debtUnitary.idDebts}</MuiTableCell>
                  <MuiTableCell align="center">{debtUnitary.type}</MuiTableCell>
                  <MuiTableCell align="center">{debtUnitary.amount}</MuiTableCell>
                  <MuiTableCell align="center">{debtUnitary.creationDate}</MuiTableCell>
                  <MuiTableCell align="center">
                    {debtUnitary.paymentDate ? (
                      debtUnitary.paymentDate
                    ) : (
                      debtUnitary.amount === 0 ? (
                        'N/A'
                      ) : (
                        <BasicModal
                          button={
                            <IconButton
                              sx={{
                                color: '#4E7D10',
                                '&:hover': { filter: 'drop-shadow(0 0 10px #4E7D10)' },
                              }}
                              title="Pagar deuda"
                            >
                              <AttachMoneyIcon />
                            </IconButton>
                          }
                        >
                          <DebtPaidForm debt={debtUnitary} onDebtPaid={handleDebtPaid} />
                        </BasicModal>
                      )
                    )}
                  </MuiTableCell>
                  <MuiTableCell align="center">
                    {debtUnitary.status === 'PAID' ? (
                      <span style={{ color: 'green' }}>Pagada</span>
                    ) : (
                      <span style={{ color: 'red' }}>Pendiente</span>
                    )}
                  </MuiTableCell>
                  <MuiTableCell align="center">
                    {(Array.from(
                      new Set(
                        (debtUnitary.tool || [])
                          .map(h => h?.typeTool?.name)
                          .filter(Boolean)
                      )
                    ).join(', ')
                    )
                      || 'Sin herramientas'
                    }
                  </MuiTableCell>

                </StyledBodyRow>
              ))
            )}
          </TableBody>
        </Table>
      </TableContainer>
      {
        debt.length > 0 && (
          <Stack direction="row" spacing={2} sx={{ mt: 2, alignItems: 'center', justifyContent: 'center' }}>
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
                <MenuItem value={10}>10</MenuItem>
                <MenuItem value={25}>25</MenuItem>
                <MenuItem value={50}>50</MenuItem>
              </Select>
            </FormControl>
          </Stack>
        )
      }
    </div >
  );
}
export default DebtList;
