import React, { useState } from 'react';
import toolService from '../../services/tool.services';
import Logo from '../../image/logo.png';
import { Typography } from '@mui/material';

function AddToolForm({ onToolAdded }) {
  const [name, setName] = useState('');
  const [category, setCategory] = useState('');
  const [replacementValue, setReplacementValue] = useState('');
  const [dailyRate, setDailyRate] = useState('');
  const [debtRate, setDebtRate] = useState('');
  const [alertConfig, setAlertConfig] = useState({ open: false, message: '', type: 'success' });
  const [errors, setErrors] = useState({});

  const handleCloseAlert = (event, reason) => {
    if (reason === 'clickaway') {
      return;
    }
    setAlertConfig({ ...alertConfig, open: false });
  };

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

    let newErrors = {};
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

    const toolData =
    {
      name: name,
      category: category,
      replacementValue: parseInt(replacementValue, 10),
      dailyRate: parseInt(dailyRate, 10),
      debtRate: parseInt(debtRate, 10),
      quantity: parseInt(quantity, 10)
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

      if (error.response && error.response.data) {
        setAlertConfig({
          open: true,
          message: error.response.data,
          type: 'error'
        });
      } else {
        setAlertConfig({
          open: true,
          message: 'Hubo un error de conexión al agregar La herramienta.',
          type: 'error'
        });
      }
    }
  };

  return (
    <div className="form-container">
      <form className="form-data" onSubmit={handleSubmit}>

        <h3 className='title-input'>Agregar Nueva herramienta</h3>
        <label>
          Nombre:
          <input
            className="input-style"
            type="text"
            value={name}
            onChange={(e) => { setName(e.target.value); setErrors({ ...errors, name: '' }); }}
          />
          {errors.name && <Typography variant="caption" color="error" display="block">{errors.name}</Typography>}
        </label>
        <label>
          Categoria:
          <input
            className="input-style"
            type="text"
            value={category}
            onChange={(e) => { setCategory(e.target.value); setErrors({ ...errors, category: '' }); }}
          />
          {errors.category && <Typography variant="caption" color="error" display="block">{errors.category}</Typography>}
        </label>
        <label>
          Valor:
          <input
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
        </label>
        <label>
          Valor de arriendo por dia:
          <input
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
        </label>
        <label>
          Valor de arriendo por dia de atraso:
          <input
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
        </label>
        <label>
          Cantidad a agregar:
          <input
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
        </label>

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

export default AddToolForm;