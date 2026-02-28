import React, { useEffect, useState, useMemo } from 'react';
import { usePagination } from '../hooks/usePagination';
import { useSort } from '../hooks/useSort';
import { getErrorMessage } from '../utils/errorHandler.js';
import { Pagination } from '@mui/material';
import Stack from '@mui/material/Stack';
import {
  Box,
  Grid,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Button,
  Typography
} from '@mui/material';
import CreditScoreIcon from '@mui/icons-material/CreditScore';
import ClearIcon from '@mui/icons-material/Clear';

import BasicModal from '../components/Modal.jsx';
import LoanList from '../components/List/LoanList.jsx';
import loanServices from '../services/loan.services.js';
import AddLoanForm from '../components/Form/AddLoanForm';
import ReturnLoanForm from '../components/Form/ReturnLoanForm';
import { buttonPrimary } from '../components/Styles/ButtonStyles.jsx';
import { paginationStyles } from '../components/Styles/PaginationStyles.jsx';

function LoanPage() {

  const [openAdd, setOpenAdd] = useState(false);
  const [openReturn, setOpenReturn] = useState(false);
  const [loan, setLoan] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const [filters, setFilters] = useState({
    idLoan: '',
    customer: '',
    state: ''
  })

  /* Cargamos los prestamos */
  const fetchLoan = async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await loanServices.getAllLoans();
      setLoan(response.data || []);
    } catch (err) {
      console.error("Error al obtener prestamos", err);
      setLoan([]);
      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchLoan(); }, []);

  const handleLoanAdded = () => { fetchLoan(); setOpenAdd(false); }
  const handleReturnLoan = () => { fetchLoan(); setOpenReturn(false); }

  /*--------------- LÓGICA DE FILTRADO ---------------------- */
  const filteredLoans = useMemo(() => {
    const stateFilter = filters.state.toLowerCase();
    const customerFilter = filters.customer.toLowerCase();
    const idFilter = String(filters.idLoan).toLowerCase();

    return (loan ?? []).filter(ln => {
      const lnState = (ln.state ?? '').toString().toLowerCase();
      const lnCustomerName = (ln.customer?.name ?? '').toLowerCase();
      const lnId = String(ln.idLoan ?? '').toLowerCase();

      const matchesState = !stateFilter || lnState === stateFilter;
      const matchesCustomer = !customerFilter || lnCustomerName.includes(customerFilter);
      const matchesId = !idFilter || lnId.includes(idFilter);

      return matchesState && matchesCustomer && matchesId;
    });
  }, [loan, filters]);

  const handleFilterChange = (event) => {
    const { name, value } = event.target;
    setFilters(prev => ({ ...prev, [name]: value }));
    resetPage();
  };

  const handleClearFilters = () => {
    setFilters({
      idLoan: '',
      customer: '',
      state: ''
    });
    resetPage();
  };

  const { sortedItems, requestSort, sortConfig } = useSort(filteredLoans, { key: 'idLoan', direction: 'asc' });

  const { currentPage, totalPages, currentItems: itemsCurrentPage, itemsPerPage, handlePageChange, handleItemsPerPageChange, resetPage } = usePagination(sortedItems);


  return (
    <Box sx={{ padding: { xs: 2, md: 3 } }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
        <Typography variant="h5" component="h1" sx={{ mb: 2, fontWeight: 'bold' }}>
          Gestión de Prestamos
        </Typography>
        <Box sx={{ display: 'flex', justifyContent: 'flex-end', mb: 2 }}>
          <BasicModal
            open={openAdd}
            handleClose={() => setOpenAdd(false)}
            button={
              <Button
                variant="contained"
                color="primary"
                startIcon={<CreditScoreIcon />}
                onClick={() => setOpenAdd(true)}
                sx={buttonPrimary}
              >
                Agregar Nuevo prestamo
              </Button>
            }
          >
            <AddLoanForm onLoanAdded={handleLoanAdded} />
          </BasicModal>
          <Box sx={{ mr: 2 }} />
          <BasicModal
            open={openReturn}
            handleClose={() => setOpenReturn(false)}
            button={
              <Button
                variant="contained"
                color="primary"

                startIcon={<CreditScoreIcon />}
                onClick={() => setOpenReturn(true)}

                sx={buttonPrimary}
              >
                Agregar Devolución
              </Button>
            }
          >
            <ReturnLoanForm onReturnLoan={handleReturnLoan} />
          </BasicModal>
        </Box>
      </Box>

      {/* ---------------- BARRA DE FILTROS ---------------- */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 2, gap: 2 }}>
        <Grid container spacing={1} sx={{ flexGrow: 1 }}>

          <Grid item xs={12} sm={6} md={4}>
            <TextField
              fullWidth
              label="Buscar por Id"
              variant="outlined"
              name="idLoan"
              value={filters.idLoan}
              onChange={handleFilterChange}
              size="small"
              placeholder="Ej: 1"
            />
          </Grid>

          <Grid item xs={12} sm={6} md={4}>
            <FormControl fullWidth size="small">
              <InputLabel>Estado</InputLabel>
              <Select
                name="state"
                value={filters.state}
                label="Estado"
                onChange={handleFilterChange}
              >
                <MenuItem value=""><em>Todos</em></MenuItem>
                <MenuItem value="ACTIVE">Activo</MenuItem>
                <MenuItem value="RETURNED">Devolución</MenuItem>
                <MenuItem value="EXPIRED">Vencido</MenuItem>
              </Select>
            </FormControl>
          </Grid>

          <Grid item xs={12} sm={6} md={4}>
            <FormControl fullWidth size="small">
              <InputLabel>Cliente</InputLabel>
              <Select
                name="customer"
                value={filters.customer}
                label="Cliente"
                onChange={handleFilterChange}
              >
                <MenuItem value=""><em>Todos</em></MenuItem>
                {
                  Array.from(new Set(
                    (loan ?? [])
                      .map(ln => ln.customer?.name)
                      .filter(Boolean)
                  )).map((name, index) => (
                    <MenuItem key={index} value={name}>
                      {name}
                    </MenuItem>
                  ))
                }
              </Select>
            </FormControl>
          </Grid>

        </Grid>

        <Button
          variant="outlined"
          color="secondary"
          startIcon={<ClearIcon />}
          onClick={handleClearFilters}
          sx={{ height: 40, whiteSpace: 'nowrap' }}
        >
          Limpiar Filtros
        </Button>
      </Box>
      <div className='table-container'>
        <LoanList
          loans={itemsCurrentPage ?? []}
          loading={loading}
          error={error}
          requestSort={requestSort}
          sortConfig={sortConfig}
          onRetry={fetchLoan}
        />
      </div>

      <div className="pagination-container">
        <Stack direction="row" spacing={2} alignItems="center" justifyContent="center">
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
      </div>

    </Box>
  );
}
export default LoanPage;