import React, { useState } from 'react';
import toolService from '../../services/tool.services';
import Logo from '../../image/logo.png';
import { Typography, Snackbar, Alert } from '@mui/material';

function AddExistingToolForm({ typeTool, onToolAdded }) {

  const [quantity, setQuantity] = useState('');
  const [errors, setErrors] = useState({});
  const [alertConfig, setAlertConfig] = useState({ open: false, message: '', type: 'success' });

  if (!typeTool) {
    return null;
  }

  const clearForm = () => {
    setQuantity('');
    setErrors({});
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    const numQuantity = parseInt(quantity, 10);

    let newErrors = {};
    if (isNaN(numQuantity) || numQuantity <= 0) {
      newErrors.quantity = "Por favor, ingresa una cantidad válida y mayor a 0.";
    }

    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors);
      return;
    }

    const toolData =
    {
      name: typeTool.name,
      category: typeTool.category,
      replacementValue: typeTool.replacementValue,
      dailyRate: typeTool.dailyRate,
      debtRate: typeTool.debtRate,
      quantity: parseInt(quantity, 10)
    };

    try {
      await toolService.createLotTool(toolData);
      setAlertConfig({ open: true, message: '¡Herramienta agregada con éxito!', type: 'success' });
      clearForm();
      onToolAdded();

    } catch (error) {
      if (error.response && error.response.data) {
        setAlertConfig({ open: true, message: error.response.data, type: 'error' });
      } else {
        setAlertConfig({ open: true, message: 'Hubo un error de conexión al agregar la herramienta.', type: 'error' });
      }
    }
  };

  return (
    <div className="form-container">
      <form className="form-data" onSubmit={handleSubmit}>

        <h3 className='title-input'>Agregar a: <br /> {typeTool.name}</h3>
        <label>
          Cantidad a agregar:
          <input
            className="input-style"
            type="number"
            value={quantity}
            onChange={(e) => {
              const value = e.target.value;
              if (value === '' || Number(value) >= 0) {
                setQuantity(e.target.value)
              }
              setErrors({ ...errors, quantity: '' });
            }}
          />
          {errors.quantity && <Typography variant="caption" color="error" display="block">{errors.quantity}</Typography>}
        </label>

        <div style={{
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center'
        }}>

          <button className='button-style' type="submit">Agregar Herramienta</button>

        </div>
      </form>

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
    </div>
  );


}

export default AddExistingToolForm;