import React, { useState, useEffect } from 'react';
import PropTypes from 'prop-types';
import loanService from '../../services/loan.services';
import toolServices from '../../services/tool.services';

// Importa los componentes de MUI
import Box from '@mui/material/Box';
import OutlinedInput from '@mui/material/OutlinedInput';
import InputLabel from '@mui/material/InputLabel';
import MenuItem from '@mui/material/MenuItem';
import FormControl from '@mui/material/FormControl';
import Select from '@mui/material/Select';
import Chip from '@mui/material/Chip';
import Button from '@mui/material/Button';
import { Typography, Tooltip, Snackbar, Alert } from '@mui/material';
import InfoIcon from '@mui/icons-material/Info';

const parseErrorMessage = (error) => {
  if (!error.response?.data) return error.message || 'Error de conexión al crear el préstamo.';
  const { data } = error.response;
  if (typeof data === 'string') return data;
  if (data.message) return data.message;
  if (data.error) return `Error del servidor: ${data.error}`;
  return 'Error de conexión al crear el préstamo.';
};

const getLoanErrorField = (lowerMsg) => {
  if (lowerMsg.includes('rut') || lowerMsg.includes('cliente')) return 'customerId';
  if (lowerMsg.includes('herramienta')) return 'typeToolIds';
  if (lowerMsg.includes('fecha')) return 'returnDate';
  return null;
};

const ITEM_HEIGHT = 48;
const ITEM_PADDING_TOP = 8;
const MenuProps = {
  PaperProps: {
    style: {
      maxHeight: ITEM_HEIGHT * 4.5 + ITEM_PADDING_TOP,
      width: 250,
    },
  },
};

function AddLoanForm({ onLoanAdded }) {
  const [typeToolIds, setTypeToolIds] = useState([]);
  const [typeTool, setTypeTool] = useState([]);

  const [customerId, setCustomerId] = useState('');
  const [deliveryDate, setDeliveryDate] = useState('');
  const [returnDate, setReturnDate] = useState('');

  const [calculatedAmount, setCalculatedAmount] = useState(null);
  const [isCalculating, setIsCalculating] = useState(false);

  const [alertConfig, setAlertConfig] = useState({ open: false, message: '', type: 'success' });
  const [errors, setErrors] = useState({});

  const handleCloseAlert = (event, reason) => {
    if (reason === 'clickaway') {
      return;
    }
    setAlertConfig({ ...alertConfig, open: false });
  };


  useEffect(() => {
    const fetchTypeTool = async () => {
      try {
        const response = await toolServices.getAllTypeTools();
        setTypeTool(response.data || []);
      } catch (error) {
        console.error("Error al obtener los tipos de herramientas", error);
        setTypeTool([]);
      }
    };
    fetchTypeTool();
  }, []);

  useEffect(() => {
    setCalculatedAmount(null);
  }, [typeToolIds, deliveryDate, returnDate]);

  const clearForm = () => {
    setTypeToolIds([]);
    setCustomerId('');
    setDeliveryDate('');
    setReturnDate('');
    setCalculatedAmount(null);
    setErrors({});
  };

  // Funcion que calcula el monto del prestamo 
  const handleCalculateTotal = async () => {
    if (typeToolIds.length === 0 || !deliveryDate || !returnDate) {
      setAlertConfig({
        open: true,
        message: "Selecciona herramientas y fechas para calcular.",
        type: 'error'
      });
      return;
    }

    setIsCalculating(true);
    const payload = {
      typeToolIds: typeToolIds,
      deliveryDate: deliveryDate,
      returnDate: returnDate
    };

    try {
      const response = await loanService.rentalAmount(payload);
      setCalculatedAmount(response.data);
    } catch (error) {
      setAlertConfig({
        open: true,
        message: error.response?.data || "Error al calcular el monto.",
        type: 'error'
      });
      console.error("Error calculando monto:", error);
    } finally {
      setIsCalculating(false);
    }
  };

  const handleSelectChange = (event) => {
    const { target: { value } } = event;
    setTypeToolIds(typeof value === 'string' ? value.split(',') : value);
  };

  const handleSubmit = async (event) => {
    event.preventDefault();

    let newErrors = {};
    if (typeToolIds.length === 0) newErrors.typeToolIds = "Debes seleccionar al menos una herramienta.";
    if (!customerId) newErrors.customerId = "El ID del cliente es obligatorio.";
    if (!deliveryDate) newErrors.deliveryDate = "La fecha de préstamo es obligatoria.";
    if (!returnDate) newErrors.returnDate = "La fecha de devolución es obligatoria.";

    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors);
      return;
    }

    const loanData = {
      typeToolIds: typeToolIds,
      customerId: Number.parseInt(customerId, 10),
      deliveryDate: deliveryDate,
      returnDate: returnDate
    };

    try {
      await loanService.createLoan(loanData);
      setAlertConfig({
        open: true,
        message: '¡Préstamo agregado con éxito!',
        type: 'success'
      });

      if (onLoanAdded) onLoanAdded();
      clearForm();

    } catch (error) {
      console.error('Error al crear el préstamo:', error);
      const errorMsg = parseErrorMessage(error);

      setAlertConfig({ open: true, message: errorMsg, type: 'error' });

      const field = getLoanErrorField(String(errorMsg).toLowerCase());
      if (field) {
        setErrors(prev => ({ ...prev, [field]: errorMsg }));
      }
    }
  };

  // Helper para obtener el nombre de la herramienta por su ID
  const getToolNameById = (id) => {
    const tool = typeTool.find(t => t.idTypeTool === id);
    return tool ? tool.name : id;
  };

  // Helper para formatear dinero (CLP o USD según tu preferencia)
  const formatMoney = (amount) => {
    return new Intl.NumberFormat('es-CL', { style: 'currency', currency: 'CLP' }).format(amount);
  };

  return (
    // CORRECCIÓN SCROLL: Aseguramos que el contenedor permita scroll si es muy alto
    <div className="form-container" style={{ overflowY: 'auto', maxHeight: '100vh', padding: '20px' }}>
      <form className="form-data" onSubmit={handleSubmit}>

        <h3 className='title-input'>Agregar Nuevo Préstamo</h3>

        <label>
          <Box component="span" sx={{ display: 'flex', alignItems: 'center' }}>
            Herramientas:
            <Tooltip title="Selecciona una o más herramientas y asegúrate de especificar las cantidades.">
              <InfoIcon fontSize="small" sx={{ ml: 1, color: 'text.secondary' }} />
            </Tooltip>
          </Box>
          <FormControl sx={{ m: 1, width: 300 }}>
            <InputLabel id="demo-multiple-chip-label">Herramientas</InputLabel>
            <Select
              labelId="demo-multiple-chip-label"
              id="demo-multiple-chip"
              multiple
              value={typeToolIds}
              onChange={handleSelectChange}
              input={<OutlinedInput id="select-multiple-chip" label="Herramientas" />}


              renderValue={(selected) => (
                <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
                  {selected.map((value) => (
                    <Chip key={value} label={getToolNameById(value)} />
                  ))}
                </Box>
              )}
              MenuProps={MenuProps}
            >
              {typeTool.map((type) => (
                <MenuItem
                  key={type.idTypeTool}
                  value={type.idTypeTool}
                >
                  {`${type.idTypeTool} - ${type.name}`}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
          {errors.typeToolIds && <Typography variant="caption" color="error" display="block" sx={{ mt: 1 }}>{errors.typeToolIds}</Typography>}
        </label>

        <br />

        <label>
          <Box component="span" sx={{ display: 'flex', alignItems: 'center' }}>
            ID del Cliente:
            <Tooltip title="El ID numérico del cliente en el sistema. Puedes visualizarlo en la sección de Clientes.">
              <InfoIcon fontSize="small" sx={{ ml: 1, color: 'text.secondary' }} />
            </Tooltip>
          </Box>
          <input
            className="input-style"
            placeholder="Ej: 1"
            type="number"
            value={customerId}
            onChange={(e) => {
              const value = e.target.value;
              if (value === '' || Number(value) >= 0) {
                setCustomerId(value);
              }
              setErrors({ ...errors, customerId: '' });
            }}
          />
          {errors.customerId && <Typography variant="caption" color="error" display="block">{errors.customerId}</Typography>}
        </label>

        <label htmlFor="loan-delivery-date">
          Fecha de Préstamo:
        </label>
        <input
          id="loan-delivery-date"
          className="input-style"
          type="date"
          value={deliveryDate}
          onChange={(e) => { setDeliveryDate(e.target.value); setErrors({ ...errors, deliveryDate: '' }); }}
        />
        {errors.deliveryDate && <Typography variant="caption" color="error" display="block">{errors.deliveryDate}</Typography>}

        <label htmlFor="loan-return-date">
          Fecha de Devolución:
        </label>
        <input
          id="loan-return-date"
          className="input-style"
          type="date"
          value={returnDate}
          onChange={(e) => { setReturnDate(e.target.value); setErrors({ ...errors, returnDate: '' }); }}
        />
        {errors.returnDate && <Typography variant="caption" color="error" display="block">{errors.returnDate}</Typography>}

        <div>
          {calculatedAmount !== null && (
            <div style={{ textAlign: 'center', marginBottom: '15px', marginTop: '20px' }}>
              <strong style={{ fontSize: '1.2em', color: '#000102ff' }}>
                Total Estimado: {formatMoney(calculatedAmount)}
              </strong>
            </div>
          )}

          <div style={{
            display: 'flex',
            flexDirection: 'row',
            justifyContent: 'center',
            gap: '20px',
            marginTop: '20px'
          }}>

            <button
              className='button-style'
              type="button"
              onClick={handleCalculateTotal}
              disabled={isCalculating || typeToolIds.length === 0 || !deliveryDate || !returnDate}
            >
              {isCalculating ? 'Calculando...' : 'Cotizar Valor'}
            </button>

            <button className='button-style' type="submit">
              Agregar Préstamo
            </button>

          </div>
        </div>
      </form>
      <Snackbar open={alertConfig.open} autoHideDuration={6000} onClose={handleCloseAlert}>
        <Alert onClose={handleCloseAlert} severity={alertConfig.type} sx={{ width: '100%' }}>
          {alertConfig.message}
        </Alert>
      </Snackbar>
    </div>
  );
}

AddLoanForm.propTypes = {
  onLoanAdded: PropTypes.func,
};

export default AddLoanForm;