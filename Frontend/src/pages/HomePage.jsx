import React, { useEffect, useState } from 'react';
import {
  Box,
  Typography,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableRow,
  CircularProgress,
  TextField,
  Button,
  Snackbar,
  Alert,
  IconButton
} from '@mui/material';
import InfoOutlinedIcon from '@mui/icons-material/InfoOutlined';
import FilterAltIcon from '@mui/icons-material/FilterAlt';
import ClearIcon from '@mui/icons-material/Clear';
import { DemoContainer } from '@mui/x-date-pickers/internals/demo';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs';
import { DatePicker } from '@mui/x-date-pickers/DatePicker';
import loanService from '../services/loan.services';
import { StyledTableHead, StyledHeaderCell, StyledBodyRow } from '../components/Styles/TableStyles';
import Grafic from '../components/grafic';
import customerService from '../services/customer.services';

const HomePage = () => {
  const [topTools, setTopTools] = useState([]);
  const [loading, setLoading] = useState(true);
  const [totalCustomers, setTotalCustomers] = useState(0);
  const [activeCustomers, setActiveCustomers] = useState(0); // Mantenemos por si lo necesitas
  const [totalLoans, setTotalLoans] = useState(0);
  const [expiredLoans, setExpiredLoans] = useState(0);
  const [alertConfig, setAlertConfig] = useState({ open: false, message: '', type: 'warning' });
  const [dateRange, setDateRange] = useState([null, null]);

  const fetchData = async (
    start = dateRange[0] ? dateRange[0].format('YYYY-MM-DD') : '',
    end = dateRange[1] ? dateRange[1].format('YYYY-MM-DD') : ''
  ) => {
    setLoading(true);
    try {
      if (start && end) {
        const [resTotal, resActive, resLoans, resExpired, resTop] = await Promise.all([
          customerService.countCustomer(),
          customerService.countActiveCustomers(),
          loanService.countActiveLoansByDeliveryDateBetween(start, end),
          loanService.countExpiredLoansByDeliveryDateBetween(start, end),
          loanService.getTopToolsByDeliveryDateBetween(start, end)
        ]);
        setTotalCustomers(resTotal.data || 0);
        setActiveCustomers(resActive.data || 0);
        setTotalLoans(resLoans.data || 0);
        setExpiredLoans(resExpired.data || 0);
        setTopTools(resTop.data || []);
      } else {
        const [resTotal, resActive, resLoans, resExpired, resTop] = await Promise.all([
          customerService.countCustomer(),
          customerService.countActiveCustomers(),
          loanService.countActiveLoans(),
          loanService.countExpiredLoans(),
          loanService.getTopTools()
        ]);
        setTotalCustomers(resTotal.data || 0);
        setActiveCustomers(resActive.data || 0);
        setTotalLoans(resLoans.data || 0);
        setExpiredLoans(resExpired.data || 0);
        setTopTools(resTop.data || []);
      }
    } catch (error) {
      console.error("Error cargando datos", error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const handleDateChange = (newValue) => {
    setDateRange(newValue);
  };

  const handleDateFilter = () => {
    if (!dateRange[0] || !dateRange[1]) {
      setAlertConfig({ open: true, message: 'Por favor selecciona ambas fechas', type: 'warning' });
      return;
    }
    fetchData(dateRange[0].format('YYYY-MM-DD'), dateRange[1].format('YYYY-MM-DD'));
  };

  const handleClearFilters = () => {
    setDateRange([null, null]);
    fetchData('', '');
  };

  const slotStyle = {
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'center',
    justifyContent: 'center',
    borderRadius: 2,
    bgcolor: '#ffffffff',
    color: '#4E7D10',
    fontWeight: 600,
    textAlign: 'center',
    border: '1px solid #c4e09fff',
    boxShadow: '0px 2px 4px rgba(0, 0, 0, 0.1)'
  };

  if (loading) {
    return (
      <Box sx={{ width: '100%', height: '100%', display: 'flex', justifyContent: 'center', alignItems: 'center' }}>
        <CircularProgress size={60} sx={{ color: '#4E7D10' }} />
      </Box>
    );
  }

  return (
    <Box
      sx={{
        display: 'grid',
        gridTemplateColumns: 'repeat(6, 1fr)',
        gridTemplateRows: '0.5fr repeat(6, 1fr)',
        gap: 2,
        width: '100%',
        height: '100%',
        bgcolor: '#ffffff',
        padding: 3,
        boxSizing: 'border-box'
      }}
    >
      {/* div1: Cabecera (Fila superior completa) */}
      <Box sx={{ gridArea: '1 / 1 / 2 / 7', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <Typography variant="h5" sx={{ fontWeight: 'bold' }}>Reporte de herramientas {dateRange[0] && dateRange[1] ? dateRange[0].format('DD/MM/YY') + ' - ' + dateRange[1].format('DD/MM/YY') : ''}</Typography>

        {/* Filtro */}
        <Box sx={{ display: 'flex', gap: 2, alignItems: 'center' }}>
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
            onClick={handleDateFilter}
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
      </Box>

      {/* Top Stats Area */}
      <Box sx={{ gridArea: '2 / 1 / 3 / 5', display: 'flex', gap: 2 }}>
        {/* Préstamos activos */}
        <Box sx={{ ...slotStyle, flex: 1, gridArea: 'unset' }}>
          <Typography variant="caption">Préstamos activos</Typography>
          <Typography variant="h5">{totalLoans}</Typography>
        </Box>
        {/* Préstamos vencidos */}
        <Box sx={{ ...slotStyle, flex: 1, gridArea: 'unset' }}>
          <Typography variant="caption" sx={{ color: '#d32f2f' }}>Préstamos vencidos</Typography>
          <Typography variant="h5" sx={{ color: '#d32f2f' }}>{expiredLoans}</Typography>
        </Box>
      </Box>

      {/* div4: Ranking (Tabla - Lado derecho alto) */}
      <Box sx={{ gridArea: '2 / 5 / 8 / 7', minHeight: 0 }}>
        <TableContainer
          component={Paper}
          sx={{
            height: '100%',
            borderRadius: '10px',
            border: '1px solid #c4e09fff',
            overflow: 'auto',
            boxShadow: '0px 2px 4px rgba(0, 0, 0, 0.1)'
          }}
        >
          <Table aria-label="simple table">
            <StyledTableHead>
              <TableRow>
                <StyledHeaderCell align="center">Pos.</StyledHeaderCell>
                <StyledHeaderCell align="center">Herramienta</StyledHeaderCell>
                <StyledHeaderCell align="center">Uso</StyledHeaderCell>
              </TableRow>
            </StyledTableHead>
            <TableBody>
              {topTools.length > 0 ? (
                topTools.map((row, index) => (
                  <StyledBodyRow key={index}>
                    <TableCell align="center">{index + 1}</TableCell>
                    <TableCell align="center">{row.toolName}</TableCell>
                    <TableCell align="center" sx={{ fontWeight: 'bold' }}>{row.usageCount}</TableCell>
                  </StyledBodyRow>
                ))
              ) : (
                <TableRow>
                  <TableCell colSpan={3} align="center" sx={{ py: 5 }}>
                    <Typography variant="body2" color="text.secondary">Sin datos</Typography>
                  </TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        </TableContainer>
      </Box>

      {/* div5: Gráfico (Área central/inferior izquierda) */}
      <Box sx={{ gridArea: '3 / 1 / 8 / 5', bgcolor: '#ffffffff', borderRadius: 2, minHeight: 0 }}>
        <Grafic data={topTools} />
      </Box>

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
};

export default HomePage;