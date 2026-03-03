import React, { useState, useEffect } from 'react';
import AddCustomerForm from '../components/Form/AddCustomerForm.jsx';
import BasicModal from '../components/Modal.jsx';
import CustomerList from '../components/List/CustomerList.jsx';
import customerService from '../services/customer.services.js';
import { Pagination } from '@mui/material';
import Stack from '@mui/material/Stack';
import { usePagination } from '../hooks/usePagination';
import { useSort } from '../hooks/useSort';
import { getErrorMessage } from '../utils/errorHandler.js';
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
import PersonAddIcon from '@mui/icons-material/PersonAdd';
import { buttonPrimary } from '../components/Styles/ButtonStyles.jsx';
import ClearIcon from '@mui/icons-material/Clear';
import IconButton from '@mui/material/IconButton';
import { paginationStyles } from '../components/Styles/PaginationStyles.jsx';

function CustomerManagementPage() {
  const [open, setOpen] = useState(false);
  const [customers, setCustomers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // --- 1. ESTADO DE FILTROS ---
  const [filters, setFilters] = useState({
    idCustomer: '',
    name: '',
    rut: '',
    state: ''
  });

  // Lógica para cargar clientes 
  const fetchCustomers = async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await customerService.getAllCustomers();
      setCustomers(response.data);
    } catch (err) {
      console.error("Error al obtener clientes:", err);
      setCustomers([]);
      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCustomers();
  }, []);

  const handleCustomerAdded = () => {
    fetchCustomers();
    setOpen(false);
  };

  /*------------------------- Filtrado ------------------------ */
  const handleFilterChange = (event) => {
    const { name, value } = event.target;
    setFilters(prevFilters => ({
      ...prevFilters,
      [name]: value
    }));
    resetPage();
  };

  const handleClearFilters = () => {
    setFilters({
      idCustomer: '',
      name: '',
      rut: '',
      state: ''
    });
    resetPage();
  };

  const filteredCustomers = customers.filter((cust) => {
    const nameFilter = filters.name.toLowerCase();
    const rutFilter = filters.rut.toLowerCase();
    const idFilter = String(filters.idCustomer).toLowerCase();

    const stateFilter = filters.state;

    const matchesName = !nameFilter || (cust.name || '').toLowerCase().includes(nameFilter);
    const matchesRut = !rutFilter || (cust.rut || '').toLowerCase().includes(rutFilter);
    const matchesId = !idFilter || String(cust.idCustomer || '').toLowerCase().includes(idFilter);

    const matchesState = !stateFilter || (cust.state && String(cust.state).toUpperCase() === stateFilter);

    return matchesName && matchesRut && matchesId && matchesState;
  });

  const { sortedItems, requestSort, sortConfig } = useSort(filteredCustomers, { key: 'name', direction: 'asc' });

  const { currentPage, totalPages, currentItems: itemsCurrentPage, itemsPerPage, handlePageChange, handleItemsPerPageChange, resetPage } = usePagination(sortedItems);

  return (
    <Box sx={{ padding: { xs: 2, md: 3 } }}>

      <Box sx=
        {{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
        <Typography variant="h5" component="h1" sx={{ mb: 2, fontWeight: 'bold' }}>
          Gestión de Clientes
        </Typography>

        <BasicModal
          open={open}
          handleClose={() => setOpen(false)}
          button={
            <Button
              variant="contained"
              color="primary"
              startIcon={<PersonAddIcon />}
              aria-label="Agregar Cliente"
              title="Agregar Nuevo Cliente"
              onClick={() => setOpen(true)}
              sx={buttonPrimary}
            >
              Agregar cliente
            </Button>
          }
        >
          <AddCustomerForm onCustomerAdded={handleCustomerAdded} />
        </BasicModal>
      </Box>

      {/* --- 4. NUEVA BARRA DE FILTROS Y BOTÓN "AGREGAR" --- */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 2, gap: 2 }}>

        {/* Contenedor de Filtros */}
        <Grid container spacing={1} sx={{ flexGrow: 1 }}>
          <Grid item xs={12} sm={6} md={3}>
            <TextField
              fullWidth
              label="Buscar por Id"
              variant="outlined"
              name="idCustomer" // IMPORTANTE: el 'name' debe coincidir con la clave del estado 'filters'
              value={filters.idCustomer}
              onChange={handleFilterChange}
              size="small" // Para un look más compacto como en la imagen
              placeholder="Ej: 1"
            />
          </Grid>

          <Grid item xs={12} sm={6} md={3}>
            <TextField
              fullWidth
              label="Buscar por Nombre"
              variant="outlined"
              name="name" // IMPORTANTE: el 'name' debe coincidir con la clave del estado 'filters'
              value={filters.name}
              onChange={handleFilterChange}
              size="small" // Para un look más compacto como en la imagen
              placeholder="Ej: Juan"
            />
          </Grid>

          <Grid item xs={12} sm={6} md={3}>
            <TextField
              fullWidth
              label="Buscar por RUT"
              variant="outlined"
              name="rut" // IMPORTANTE
              value={filters.rut}
              onChange={handleFilterChange}
              size="small"
              placeholder="Ej: 12345678-9"
            />
          </Grid>

          <Grid item xs={12} sm={6} md={3}>
            <FormControl fullWidth size="small">
              <InputLabel>Estado</InputLabel>
              <Select
                name="state"
                value={filters.state}
                label="Estado"
                onChange={handleFilterChange}
                size="small"
              >
                <MenuItem value=""><em>Todos</em></MenuItem>
                <MenuItem value="ACTIVE">Activo</MenuItem>
                <MenuItem value="RESTRICTED">Restringido</MenuItem>
              </Select>
            </FormControl>
          </Grid>
        </Grid>
        <IconButton
          onClick={handleClearFilters}
          sx={{
            color: '#4E7D10',
            '&:hover': {
              filter: 'drop-shadow(0 0 10px #4E7D10)',
            },
          }}
          title="Limpiar"
        >
          <ClearIcon />
        </IconButton>
      </Box>

      {/* --- FIN DE LA NUEVA BARRA --- */}

      <div className='table-container'>
        <CustomerList
          customers={itemsCurrentPage}
          loading={loading}
          error={error}
          requestSort={requestSort}
          sortConfig={sortConfig}
          onRetry={fetchCustomers}
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
    </Box>
  );
}

export default CustomerManagementPage;