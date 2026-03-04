import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { Pagination } from '@mui/material';
import Stack from '@mui/material/Stack';
import { Box, Grid, TextField, FormControl, InputLabel, Select, MenuItem, Button, Typography, Snackbar, Alert } from '@mui/material';
import CreditScoreIcon from '@mui/icons-material/CreditScore';
import SearchIcon from '@mui/icons-material/Search';

import BasicModal from '../components/Modal.jsx';
import kardexServices from '../services/kardex.services.js'
import KardexList from '../components/List/KardexList.jsx'
import { usePagination } from '../hooks/usePagination';
import { useSort } from '../hooks/useSort';
import { getErrorMessage } from '../utils/errorHandler.js';
import { buttonPrimary } from '../components/Styles/ButtonStyles.jsx';
import { paginationStyles } from '../components/Styles/PaginationStyles.jsx';
import FilterAltIcon from '@mui/icons-material/FilterAlt';
import ClearIcon from '@mui/icons-material/Clear';
import IconButton from '@mui/material/IconButton';
import { DemoContainer } from '@mui/x-date-pickers/internals/demo';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs';
import { DatePicker } from '@mui/x-date-pickers/DatePicker';
import dayjs from 'dayjs';

function KardexPage() {

    const [kardex, setKardex] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [alertConfig, setAlertConfig] = useState({ open: false, message: '', type: 'warning' });

    const [dateRange, setDateRange] = useState([null, null]);

    const fetchKardex = async () => {
        setLoading(true);
        setError(null);
        try {
            const response = await kardexServices.getAllMove();
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

    const handleDateChange = (newValue) => {
        setDateRange(newValue);
    };


    const handleFilter = async () => {
        const startDate = dateRange[0] ? dateRange[0].format('YYYY-MM-DD') : '';
        const endDate = dateRange[1] ? dateRange[1].format('YYYY-MM-DD') : '';

        if (!startDate || !endDate) {
            setAlertConfig({ open: true, message: 'Por favor selecciona ambas fechas', type: 'warning' });
            return;
        }

        setLoading(true);
        try {
            const response = await kardexServices.getAllKardexByDate({ start: startDate, end: endDate });
            setKardex(response.data || []);
            if (resetPage) resetPage();
        } catch (error) {
            console.error("Error al filtrar movimientos", error);
            setError("Error al aplicar el filtro de fechas. Verifica que las fechas sean correctas e intenta nuevamente.");
            setKardex([]);
        } finally {
            setLoading(false);
        }
    };


    const handleClear = () => {
        setDateRange([null, null]);
        fetchKardex();
        if (resetPage) resetPage();
    };


    const { sortedItems, requestSort, sortConfig } = useSort(kardex, { key: 'idKardex', direction: 'asc' });

    const { currentPage, totalPages, currentItems: itemsCurrentPage, itemsPerPage, handlePageChange, handleItemsPerPageChange, resetPage } = usePagination(sortedItems);

    return (
        <Box sx={{ padding: { xs: 2, md: 3 }, pt: { xs: 1, md: 1 } }}>

            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
                <Typography variant="h5" component="h1" sx={{ fontWeight: 'bold' }}>
                    Historial de Movimientos
                </Typography>

                <Box
                    sx={{
                        display: 'flex',
                        gap: 2,
                        backgroundColor: '#ffffffff',
                        padding: 2,
                        borderRadius: 2,
                        alignItems: 'center',
                        flexWrap: 'wrap'
                    }}
                >
                    <LocalizationProvider dateAdapter={AdapterDayjs}>
                        <DemoContainer components={['DateRangePicker']} sx={{ pt: 0, overflow: 'hidden' }}>
                            <Box sx={{ display: 'flex', gap: 1 }}>
                                <DatePicker
                                    label="Desde"
                                    value={dateRange[0]}
                                    onChange={(newValue) => handleDateChange([newValue, dateRange[1]])}
                                    slotProps={{ textField: { size: 'small', sx: { backgroundColor: 'white' } } }}
                                />
                                <DatePicker
                                    label="Hasta"
                                    value={dateRange[1]}
                                    onChange={(newValue) => handleDateChange([dateRange[0], newValue])}
                                    slotProps={{ textField: { size: 'small', sx: { backgroundColor: 'white' } } }}
                                />
                            </Box>
                        </DemoContainer>
                    </LocalizationProvider>

                    <IconButton
                        onClick={handleFilter}
                        sx={{
                            color: '#4E7D10',
                            '&:hover': {
                                filter: 'drop-shadow(0 0 10px #4E7D10)',
                            },
                        }}
                        title="Filtrar"
                    >
                        <FilterAltIcon />
                    </IconButton>
                    <IconButton
                        onClick={handleClear}
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

export default KardexPage;