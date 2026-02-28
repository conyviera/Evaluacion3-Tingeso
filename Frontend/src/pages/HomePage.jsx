import React, { useEffect, useState } from 'react';
import {
  Box,
  Typography,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  CircularProgress,
  styled
} from '@mui/material';
import InfoOutlinedIcon from '@mui/icons-material/InfoOutlined';
import logo from '../image/logo.png';
import loanService from '../services/loan.services';
import { StyledTableHead, StyledHeaderCell, StyledBodyRow } from '../components/Styles/TableStyles';
import Grafic from '../components/grafic';
import customerService from '../services/customer.services';


const HomePage = () => {
  const [topTools, setTopTools] = useState([]);
  const [loading, setLoading] = useState(true);
  const [alertConfig, setAlertConfig] = useState({ open: false, message: '', type: 'success' });
  const [totalCustomers, setTotalCustomers] = useState(0);
  const [activeCustomers, setActiveCustomers] = useState(0);
  const [totalLoans, setTotalLoans] = useState(0);

  const handleCloseAlert = () => {
    setAlertConfig({ open: false, message: '', type: 'success' });
  };

  useEffect(() => {
    const fetchTotalCustomers = async () => {
      try {
        const response = await customerService.countCustomer();
        setTotalCustomers(response.data || 0);
      } catch (error) {
        console.error("Error cargando total de clientes", error);
      }
    };
    fetchTotalCustomers();
  }, []);

  useEffect(() => {
    const fetchActiveCustomers = async () => {
      try {
        const response = await customerService.countActiveCustomers();
        setActiveCustomers(response.data || 0);
      } catch (error) {
        console.error("Error cargando clientes activos", error);
      }
    };
    fetchActiveCustomers();
  }, []);

  useEffect(() => {
    const fetchTotalLoans = async () => {
      try {
        const response = await loanService.countActiveLoans();
        setTotalLoans(response.data || 0);
      } catch (error) {
        console.error("Error cargando préstamos activos", error);
      }
    };
    fetchTotalLoans();
  }, []);

  useEffect(() => {
    const fetchTopTools = async () => {
      try {
        const response = await loanService.getTopTools();
        setTopTools(response.data || []);
      } catch (error) {
        console.error("Error cargando reporte de herramientas", error);
      } finally {
        setLoading(false);
      }
    };
    fetchTopTools();
  }, []);

  const slotStyle = {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    borderRadius: 2,
    bgcolor: '#eeeeeeff',
    color: '#4E7D10',
    fontWeight: 600,
  };

  if (loading) {
    return (
      <Box sx={{ width: '100%', height: '100%', display: 'flex', flexDirection: 'column', justifyContent: 'center', alignItems: 'center', bgcolor: '#ffffff' }}>
        <CircularProgress size={60} sx={{ color: '#4E7D10', mb: 2 }} />
        <Typography variant="h6" color="text.secondary">
          Cargando panel principal...
        </Typography>
      </Box>
    );
  }

  return (
    <Box sx={{ width: '100%', height: '100%', display: 'flex', flexDirection: 'column', bgcolor: '#ffffff', boxSizing: 'border-box', padding: { xs: 2, md: 3 }, gap: 2 }}>

      {/* Fila 1: métricas */}
      <Box sx={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: 2, flexShrink: 0 }}>
        <Box sx={{ ...slotStyle, p: 3, borderRadius: 2 }}>
          Clientes total
          <Typography variant="h6" color="text.secondary">
            {totalCustomers}
          </Typography>
        </Box>
        <Box sx={{ ...slotStyle, p: 3, borderRadius: 2 }}>
          Clientes activos
          <Typography variant="h6" color="text.secondary">
            {activeCustomers}
          </Typography>
        </Box>
        <Box sx={{ ...slotStyle, p: 3, borderRadius: 2 }}>
          Préstamos activos
          <Typography variant="h6" color="text.secondary">
            {totalLoans}
          </Typography>
        </Box>
      </Box>

      {/* Fila 2: tabla + panel lateral — toma el espacio restante */}
      <Box sx={{ display: 'grid', gridTemplateColumns: '2fr 1fr', gap: 2, flexGrow: 1, minHeight: 0 }}>

        {/* Tabla: scroll interno con altura máxima fija */}
        <Box sx={{ minHeight: 0 }}>
          <TableContainer
            component={Paper}
            sx={{
              borderRadius: '10px',
              overflow: 'hidden',
              boxShadow: 'none',
              border: '1px solid #e0e0d8',
            }}
          >
            <Table sx={{ minWidth: 400 }} aria-label="top tools table">
              <StyledTableHead>
                <TableRow>
                  <StyledHeaderCell align="center">Posición</StyledHeaderCell>
                  <StyledHeaderCell align="center">Herramienta</StyledHeaderCell>
                  <StyledHeaderCell align="center">Préstamos</StyledHeaderCell>
                </TableRow>
              </StyledTableHead>
              <TableBody>
                {topTools.length > 0 ? (
                  topTools.map((row, index) => (
                    <StyledBodyRow key={index}>
                      <TableCell align="center">{index + 1}</TableCell>
                      <TableCell align="center">{row.toolName}</TableCell>
                      <TableCell align="center" sx={{ fontWeight: 'bold' }}>
                        {row.usageCount}
                      </TableCell>
                    </StyledBodyRow>
                  ))
                ) : (
                  <TableRow>
                    <TableCell colSpan={3} align="center" sx={{ py: 5 }}>
                      <InfoOutlinedIcon sx={{ fontSize: 40, color: 'text.secondary', mb: 1 }} />
                      <Typography variant="body1" color="text.secondary" sx={{ mb: 1 }}>
                        No se han registrado préstamos de herramientas aún.
                      </Typography>
                      <Typography variant="body2" color="text.secondary">
                        A medida que se realicen préstamos, aquí verás las herramientas más solicitadas.
                      </Typography>
                    </TableCell>
                  </TableRow>
                )}
              </TableBody>
            </Table>
          </TableContainer>
        </Box>

        {/* Panel lateral: gráfico de herramientas */}
        <Box sx={{ borderRadius: 2, minHeight: 0 }}>
          <Grafic data={topTools} />
        </Box>
      </Box>

    </Box>
  );
};

export default HomePage;