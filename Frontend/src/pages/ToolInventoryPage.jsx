import React, { useState, useEffect, useMemo } from 'react';
import { Link } from 'react-router-dom';
import AddToolForm from "../components/Form/AddToolForm";
import BasicModal from '../components/Modal.jsx';
import TypeToolList from '../components/List/TypeToolList.jsx';
import toolService from '../services/tool.services';
import Pagination from '@mui/material/Pagination';
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
import ConstructionIcon from '@mui/icons-material/Construction';
import { paginationStyles } from '../components/Styles/PaginationStyles.jsx';
import { buttonPrimary } from '../components/Styles/ButtonStyles.jsx';

function ToolInventoryPage() {
  const [open, setOpen] = useState(false);
  const [typeTool, setTypeTool] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const [filters, setFilters] = useState({
    idTypeTool: '',
    name: '',
    category: ''
  });

  const [categoryOptions, setCategoryOptions] = useState([]);
  const fetchTool = async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await toolService.getAllTypeTools();
      setTypeTool(response.data || []);
    } catch (err) {
      console.error("Error al obtener tipos de herramientas:", err);
      setTypeTool([]);
      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  const fetchCategories = async () => {
    try {
      if (typeof toolService.getAllCategory === 'function') {
        const { data } = await toolService.getAllCategory();
        const cleaned = (data ?? [])
          .map(x => (x ?? '').toString().trim())
          .filter(Boolean);
        if (cleaned.length) {
          setCategoryOptions(cleaned);
          return;
        }
      }

      const setCat = new Set(
        (typeTool ?? [])
          .map(tt => (tt.category ?? '').toString().trim())
          .filter(Boolean)
      );
      setCategoryOptions(Array.from(setCat).sort((a, b) => a.localeCompare(b)));
    } catch (err) {
      console.error('Error al cargar categorías:', err);
      const setCat = new Set(
        (typeTool ?? [])
          .map(tt => (tt.category ?? '').toString().trim())
          .filter(Boolean)
      );
      setCategoryOptions(Array.from(setCat).sort((a, b) => a.localeCompare(b)));
    }
  };

  useEffect(() => { fetchTool(); }, []);
  useEffect(() => { fetchCategories(); }, [typeTool]);

  const handleToolAdded = () => {
    fetchTool();
    setOpen(false);
  };

  /*------------------------- Filtrado ------------------------ */
  const filteredTypeTool = useMemo(() => {
    const nameFilter = filters.name.toLowerCase();
    const categoryFilter = filters.category.toLowerCase();
    const idFilter = String(filters.idTypeTool).toLowerCase();

    return (typeTool ?? []).filter(tt => {
      const ttName = (tt.name ?? '').toLowerCase();
      const ttCategory = (tt.category ?? '').toString().toLowerCase();
      const ttId = String(tt.idTypeTool ?? '').toLowerCase();

      const matchesName = !nameFilter || ttName.includes(nameFilter);
      const matchesCategory = !categoryFilter || ttCategory.includes(categoryFilter);
      const matchesId = !idFilter || ttId.includes(idFilter);

      return matchesName && matchesCategory && matchesId;
    });
  }, [typeTool, filters]);

  const { sortedItems, requestSort, sortConfig } = useSort(filteredTypeTool, { key: 'name', direction: 'asc' });

  const { currentPage, totalPages, currentItems: itemsCurrentPage, itemsPerPage, handlePageChange, handleItemsPerPageChange, resetPage } = usePagination(sortedItems);

  /*------------------------- Filtros ------------------------ */
  const handleFilterChange = (event) => {
    const { name, value } = event.target;
    setFilters(prev => ({ ...prev, [name]: value }));
    resetPage();
  };

  return (
    <Box sx={{ padding: { xs: 2, md: 3 } }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
        <Typography variant="h5" component="h1" sx={{ mb: 2, fontWeight: 'bold' }}>
          Gestión de Herramientas
        </Typography>

        <BasicModal
          open={open}
          handleClose={() => setOpen(false)}
          button={
            <Button
              variant="contained"
              color="primary"
              startIcon={<ConstructionIcon />}
              aria-label="Agregar herramienta"
              title="Agregar Nueva herramienta"
              onClick={() => setOpen(true)}
              sx={buttonPrimary}
            >
              Agregar Nueva herramienta
            </Button>
          }
        >
          <AddToolForm onToolAdded={handleToolAdded} />
        </BasicModal>
      </Box>

      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 2, gap: 2 }}>
        <Grid container spacing={1} sx={{ flexGrow: 1 }}>

          <Grid item xs={12} sm={6} md={4}>
            <TextField
              fullWidth
              label="Buscar por Id"
              variant="outlined"
              name="idTypeTool"
              value={filters.idTypeTool}
              onChange={handleFilterChange}
              size="small"
              placeholder="Ej: 1"
            />
          </Grid>

          <Grid item xs={12} sm={6} md={4}>
            <TextField
              fullWidth
              label="Buscar por Nombre"
              variant="outlined"
              name="name"
              value={filters.name}
              onChange={handleFilterChange}
              size="small"
              placeholder="Ej: Martillo"
            />
          </Grid>

          <Grid item xs={12} sm={6} md={4}>
            <FormControl fullWidth size="small">
              <InputLabel>Categoría</InputLabel>
              <Select
                name="category"
                value={filters.category}
                label="Categoría"
                onChange={handleFilterChange}
              >
                <MenuItem value=""><em>Todas</em></MenuItem>
                {categoryOptions.map(opt => (
                  <MenuItem key={opt} value={opt}>{opt}</MenuItem>
                ))}
              </Select>
            </FormControl>
          </Grid>

        </Grid>
      </Box>

      <div className='table-container'>
        <TypeToolList
          typeTool={itemsCurrentPage ?? []}
          onRefreshList={handleToolAdded}
          loading={loading}
          error={error}
          requestSort={requestSort}
          sortConfig={sortConfig}
          onRetry={fetchTool}
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

export default ToolInventoryPage;
