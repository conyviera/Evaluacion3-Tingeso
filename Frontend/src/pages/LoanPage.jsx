import React, { useEffect, useState, useMemo } from 'react';
import { usePagination } from '../hooks/usePagination';
import { useSort } from '../hooks/useSort';
import { getErrorMessage } from '../utils/errorHandler.js';
import { Pagination } from '@mui/material';
import Stack from '@mui/material/Stack';
import { DemoContainer } from '@mui/x-date-pickers/internals/demo';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs';
import { DatePicker } from '@mui/x-date-pickers/DatePicker';
import dayjs from 'dayjs';
import {
  Box,
  Grid,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Button,
  Typography,
  Snackbar,
  Alert
} from '@mui/material';
import CreditScoreIcon from '@mui/icons-material/CreditScore';
import ClearIcon from '@mui/icons-material/Clear';
import FilterAltIcon from '@mui/icons-material/FilterAlt';
import SearchIcon from '@mui/icons-material/Search';
import IconButton from '@mui/material/IconButton';

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
  const [alertConfig, setAlertConfig] = useState({ open: false, message: '', type: 'warning' });

  const [dateRange, setDateRange] = useState([null, null]);

  const [filters, setFilters] = useState({
    idLoan: '',
    customer: '',
    state: ''
  });

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

  const handleDateChange = (newValue) => {
    setDateRange(newValue);
  };

  const handleDateFilter = async () => {
    const startDate = dateRange[0] ? dateRange[0].format('YYYY-MM-DD') : '';
    const endDate = dateRange[1] ? dateRange[1].format('YYYY-MM-DD') : '';

    if (!startDate || !endDate) {
      setAlertConfig({ open: true, message: 'Por favor selecciona ambas fechas', type: 'warning' });
      return;
    }
    setLoading(true);
    try {
      const response = await loanServices.loanActiveAndExpireFilterDate(startDate, endDate);
      setLoan(response.data || []);
      resetPage();
    } catch (err) {
      console.error("Error al filtrar préstamos por fecha", err);
      setError("Error al aplicar el filtro de fechas.");
      setLoan([]);
    } finally {
      setLoading(false);
    }
  };

  const handleClearFilters = () => {
    setFilters({
      idLoan: '',
      customer: '',
      state: ''
    });
    setDateRange([null, null]);
    fetchLoan();
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
      <Box
        sx={{
          display: 'flex',
          flexDirection: 'row',
          flexWrap: 'nowrap',   // Evita que los elementos bajen a la siguiente línea
          alignItems: 'center',
          gap: 1.5,             // Espaciado entre componentes
          mb: 3,
          backgroundColor: '#ffffffff',
          padding: 1.5,
          borderRadius: 2,
          boxShadow: '0px 2px 4px rgba(0,0,0,0.05)',
          width: '100%',
          overflowX: 'auto'     // Permite scroll lateral si la pantalla es muy pequeña
        }}
      >
        {/* Filtro ID */}
        <TextField
          label="ID"
          variant="outlined"
          name="idLoan"
          value={filters.idLoan}
          onChange={handleFilterChange}
          size="small"
          sx={{ flex: '0 1 80px' }}
        />

        {/* Filtro Estado */}
        <FormControl size="small" sx={{ flex: '0 1 140px' }}>
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

        {/* Filtro Cliente */}
        <FormControl size="small" sx={{ flex: '1 1 180px', minWidth: '150px' }}>
          <InputLabel>Cliente</InputLabel>
          <Select
            name="customer"
            value={filters.customer}
            label="Cliente"
            onChange={handleFilterChange}
          >
            <MenuItem value=""><em>Todos</em></MenuItem>
            {Array.from(new Set((loan ?? []).map(ln => ln.customer?.name).filter(Boolean))).map((name, index) => (
              <MenuItem key={index} value={name}>{name}</MenuItem>
            ))}
          </Select>
        </FormControl>

        {/* Filtro de Fechas */}
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, flexShrink: 0 }}>
          <LocalizationProvider dateAdapter={AdapterDayjs}>
            <DatePicker
              label="Desde"
              value={dateRange[0]}
              onChange={(newValue) => handleDateChange([newValue, dateRange[1]])}
              slotProps={{ textField: { size: 'small', sx: { width: '135px', backgroundColor: 'white' } } }}
            />
            <DatePicker
              label="Hasta"
              value={dateRange[1]}
              onChange={(newValue) => handleDateChange([dateRange[0], newValue])}
              slotProps={{ textField: { size: 'small', sx: { width: '135px', backgroundColor: 'white' } } }}
            />
          </LocalizationProvider>
        </Box>

        {/* Iconos de Acción */}
        <Box sx={{ display: 'flex', flexShrink: 0 }}>
          <IconButton
            onClick={handleDateFilter}
            sx={{
              color: '#4E7D10',
              '&:hover': { filter: 'drop-shadow(0 0 8px #4E7D10)' },
            }}
            title="Filtrar por fecha"
          >
            <FilterAltIcon />
          </IconButton>

          <IconButton
            onClick={handleClearFilters}
            sx={{
              color: '#4E7D10', // Color distinto para limpiar para evitar confusiones
              '&:hover': { filter: 'drop-shadow(0 0 8px #4E7D10)' },
            }}
            title="Limpiar filtros"
          >
            <ClearIcon />
          </IconButton>
        </Box>
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
              <MenuItem value={5}>5</MenuItem>
              <MenuItem value={10}>10</MenuItem>
              <MenuItem value={15}>15</MenuItem>
            </Select>
          </FormControl>
        </Stack>
      </div>

      <Snackbar
        open={alertConfig.open}
        autoHideDuration={4000}
        onClose={() => setAlertConfig({ ...alertConfig, open: false })}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
      >
        <Alert
          onClose={() => setAlertConfig({ ...alertConfig, open: false })}
          severity={alertConfig.type}
          sx={{ width: '100%' }}
        >
          {alertConfig.message}
        </Alert>
      </Snackbar>
    </Box>
  );
}
export default LoanPage;