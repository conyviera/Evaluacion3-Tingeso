import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { Pagination } from '@mui/material';
import Stack from '@mui/material/Stack';
import { Box, Grid, TextField, FormControl, InputLabel, Select, MenuItem, Button, Typography, Snackbar, Alert } from '@mui/material';
import CreditScoreIcon from '@mui/icons-material/CreditScore';
import SearchIcon from '@mui/icons-material/Search';
import ClearIcon from '@mui/icons-material/Clear';

import BasicModal from '../components/Modal.jsx';
import kardexServices from '../services/kardex.services.js'
import KardexList from '../components/List/KardexList.jsx'
import { usePagination } from '../hooks/usePagination';
import { useSort } from '../hooks/useSort';
import { getErrorMessage } from '../utils/errorHandler.js';
import { buttonPrimary } from '../components/Styles/ButtonStyles.jsx';
import { paginationStyles } from '../components/Styles/PaginationStyles.jsx';

function KardexPage() {

    const [open, setOpen] = useState(false);
    const [kardex, setKardex] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [alertConfig, setAlertConfig] = useState({ open: false, message: '', type: 'warning' });

    const [dateRange, setDateRange] = useState({
        start: '',
        end: ''
    });

    const fetchKardex = async () => {
        setLoading(true);
        setError(null);
        try {
            const response = await kardexServices.getAllMove();
            setKardex(response.data || []);
            setKardex(response.data || []);
        } catch (err) {
            console.error("Error al obtener los movimientos", err);
            setKardex([]);
            setError(getErrorMessage(err));
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => { fetchKardex(); }, []);

    const handleDateChange = (e) => {
        const { name, value } = e.target;
        setDateRange({ ...dateRange, [name]: value });
    };


    const handleFilter = async () => {
        if (!dateRange.start || !dateRange.end) {
            setAlertConfig({ open: true, message: 'Por favor selecciona ambas fechas', type: 'warning' });
            return;
        }

        try {
            const response = await kardexServices.getAllKardexByDate(dateRange);
            setKardex(response.data || []);
            setKardex(response.data || []);
        } catch (error) {
            console.error("Error al filtrar movimientos", error);
            setError("Error al aplicar el filtro de fechas. Verifica que las fechas sean correctas e intenta nuevamente.");
        }
    };


    const handleClear = () => {
        setDateRange({ start: '', end: '' });
        fetchKardex();
    };


    const { sortedItems, requestSort, sortConfig } = useSort(kardex, { key: 'idKardex', direction: 'asc' });

    const { currentPage, totalPages, currentItems: itemsCurrentPage, itemsPerPage, handlePageChange, handleItemsPerPageChange } = usePagination(sortedItems);

    return (
        <Box sx={{ padding: { xs: 2, md: 3 } }}>

            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
                <Typography variant="h5" component="h1" sx={{ mb: 2, fontWeight: 'bold' }}>
                    Historial de Movimientos
                </Typography>
            </Box>

            <Box
                sx={{
                    display: 'flex',
                    gap: 2,
                    mb: 3,
                    backgroundColor: '#ffffffff',
                    padding: 2,
                    borderRadius: 2,
                    alignItems: 'center',
                    flexWrap: 'wrap'
                }}
            >
                <TextField
                    label="Desde"
                    type="date"
                    name="start"
                    value={dateRange.start}
                    onChange={handleDateChange}
                    size="small"
                    InputLabelProps={{ shrink: true }}
                    sx={{ backgroundColor: 'white' }}
                />

                <TextField
                    label="Hasta"
                    type="date"
                    name="end"
                    value={dateRange.end}
                    onChange={handleDateChange}
                    size="small"
                    InputLabelProps={{ shrink: true }}
                    sx={{ backgroundColor: 'white' }}
                />

                <Button
                    variant="contained"
                    startIcon={<SearchIcon />}
                    onClick={handleFilter}
                    disabled={!dateRange.start || !dateRange.end}
                    sx={buttonPrimary}
                >
                    Filtrar
                </Button>

                <Button
                    variant="outlined"
                    startIcon={<ClearIcon />}
                    onClick={handleClear}
                    sx={buttonPrimary}
                >
                    Limpiar
                </Button>
            </Box>

            {error && (
                <Box sx={{ color: 'error.main', mb: 2, p: 2, bgcolor: '#ffebee', borderRadius: 1 }}>
                    {error}
                </Box>
            )}

            <div className='table-container'>
                <KardexList
                    kardex={itemsCurrentPage ?? []}
                    loading={loading}
                    error={error}
                    requestSort={requestSort}
                    sortConfig={sortConfig}
                    onRetry={fetchKardex}
                />
            </div>

            <div className="pagination-container">
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

export default KardexPage;