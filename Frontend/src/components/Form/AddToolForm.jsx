import React, { useState } from 'react';
import PropTypes from 'prop-types';
import toolService from '../../services/tool.services';
import { Typography } from '@mui/material';

function AddToolForm({ onToolAdded }) {
  const [name, setName] = useState('');
  const [category, setCategory] = useState('');
  const [replacementValue, setReplacementValue] = useState('');
  const [dailyRate, setDailyRate] = useState('');
  const [debtRate, setDebtRate] = useState('');
  const [errors, setErrors] = useState({});

  const [quantity, setQuantity] = useState('');
  const clearForm = () => {
    setName('');
    setCategory('');
    setReplacementValue('');
    setDailyRate('');
    setDebtRate('');
    setQuantity('');
    setErrors({});
  };


  const handleSubmit = async (event) => {
    event.preventDefault();

    const newErrors = {};
    if (!name) newErrors.name = "El nombre es obligatorio.";
    if (!category) newErrors.category = "La categoría es obligatoria.";
    if (!replacementValue) newErrors.replacementValue = "El valor de reposición es obligatorio.";
    if (!dailyRate) newErrors.dailyRate = "El valor de arriendo es obligatorio.";
    if (!debtRate) newErrors.debtRate = "El valor de atraso es obligatorio.";
    if (!quantity) newErrors.quantity = "La cantidad es obligatoria.";

    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors);
      return;
    }

    const toolData = {
      name,
      category,
      replacementValue: Number.parseInt(replacementValue, 10),
      dailyRate: Number.parseInt(dailyRate, 10),
      debtRate: Number.parseInt(debtRate, 10),
      quantity: Number.parseInt(quantity, 10)
    };

    try {
      const response = await toolService.createLotTool(toolData);
      console.log('Herramienta creada:', response.data);
      setAlertConfig({
        open: true,
        message: '¡Herramienta agregada con éxito!',
        type: 'success'
      });
      onToolAdded();
      clearForm();

    } catch (error) {
      console.error('Error al agregar la herramienta:', error);

      const msg = error.response?.data
        ? error.response.data
        : 'Hubo un error de conexión al agregar La herramienta.';
      setAlertConfig({ open: true, message: msg, type: 'error' });
    }
  };

  return (
    <div className="form-container">
      <form className="form-data" onSubmit={handleSubmit}>

        <h3 className='title-input'>Agregar Nueva herramienta</h3>
        <label htmlFor="tool-name">
          Nombre:
        </label>
        <input
          id="tool-name"
          className="input-style"
          type="text"
          value={name}
          onChange={(e) => { setName(e.target.value); setErrors({ ...errors, name: '' }); }}
        />
        {errors.name && <Typography variant="caption" color="error" display="block">{errors.name}</Typography>}
        <label htmlFor="tool-category">
          Categoria:
        </label>
        <input
          id="tool-category"
          className="input-style"
          type="text"
          value={category}
          onChange={(e) => { setCategory(e.target.value); setErrors({ ...errors, category: '' }); }}
        />
        {errors.category && <Typography variant="caption" color="error" display="block">{errors.category}</Typography>}
        <label htmlFor="tool-replacement-value">
          Valor:
        </label>
        <input
          id="tool-replacement-value"
          className="input-style"
          type="number"
          value={replacementValue}
          onChange={(e) => {
            const value = e.target.value;
            if (value === '' || Number(value) >= 0) {
              setReplacementValue(e.target.value);
            }
            setErrors({ ...errors, replacementValue: '' });
          }}
        />
        {errors.replacementValue && <Typography variant="caption" color="error" display="block">{errors.replacementValue}</Typography>}
        <label htmlFor="tool-daily-rate">
          Valor de arriendo por dia:
        </label>
        <input
          id="tool-daily-rate"
          className="input-style"
          type="number"
          value={dailyRate}
          onChange={(e) => {
            const value = e.target.value;
            if (value === '' || Number(value) >= 0) {
              setDailyRate(e.target.value);
            }
            setErrors({ ...errors, dailyRate: '' });
          }}
        />
        {errors.dailyRate && <Typography variant="caption" color="error" display="block">{errors.dailyRate}</Typography>}
        <label htmlFor="tool-debt-rate">
          Valor de arriendo por dia de atraso:
        </label>
        <input
          id="tool-debt-rate"
          className="input-style"
          type="number"
          value={debtRate}
          onChange={(e) => {
            const value = e.target.value;
            if (value === '' || Number(value) >= 0) {
              setDebtRate(e.target.value);
            }
            setErrors({ ...errors, debtRate: '' });
          }}
        />
        {errors.debtRate && <Typography variant="caption" color="error" display="block">{errors.debtRate}</Typography>}
        <label htmlFor="tool-quantity">
          Cantidad a agregar:
        </label>
        <input
          id="tool-quantity"
          className="input-style"
          type="number"
          value={quantity}
          onChange={(e) => {
            const value = e.target.value;
            if (value === '' || Number(value) >= 0) {
              setQuantity(e.target.value);
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
    </div>
  );
}

AddToolForm.propTypes = {
  onToolAdded: PropTypes.func.isRequired,
};

export default AddToolForm;