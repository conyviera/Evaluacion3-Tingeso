import React, { useState } from 'react';
import PropTypes from 'prop-types';
import toolService from '../../services/tool.services';
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
    const numQuantity = Number.parseInt(quantity, 10);

    const newErrors = {};
    if (Number.isNaN(numQuantity) || numQuantity <= 0) {
      newErrors.quantity = "Por favor, ingresa una cantidad válida y mayor a 0.";
    }

    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors);
      return;
    }

    const toolData = {
      name: typeTool.name,
      category: typeTool.category,
      replacementValue: typeTool.replacementValue,
      dailyRate: typeTool.dailyRate,
      debtRate: typeTool.debtRate,
      quantity: Number.parseInt(quantity, 10)
    };

    try {
      await toolService.createLotTool(toolData);
      setAlertConfig({ open: true, message: '¡Herramienta agregada con éxito!', type: 'success' });
      clearForm();
      onToolAdded();

    } catch (error) {
      const msg = error.response?.data
        ? error.response.data
        : 'Hubo un error de conexión al agregar la herramienta.';
      setAlertConfig({ open: true, message: msg, type: 'error' });
    }
  };

  return (
    <div className="form-container">
      <form className="form-data" onSubmit={handleSubmit}>

        <h3 className='title-input'>Agregar a: <br /> {typeTool.name}</h3>
        <label htmlFor="qty-existing">
          Cantidad a agregar:
        </label>
        <input
          id="qty-existing"
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

AddExistingToolForm.propTypes = {
  typeTool: PropTypes.shape({
    name: PropTypes.string,
    category: PropTypes.string,
    replacementValue: PropTypes.number,
    dailyRate: PropTypes.number,
    debtRate: PropTypes.number,
  }),
  onToolAdded: PropTypes.func.isRequired,
};

export default AddExistingToolForm;