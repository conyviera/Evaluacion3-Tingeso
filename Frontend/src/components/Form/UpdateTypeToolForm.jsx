import React, { useState } from 'react';
import { Button, Checkbox, FormControlLabel, TextField, Box, Typography } from '@mui/material';
import toolServices from '../../services/tool.services';
import ButtonAlert from '../Styles/ButtonAlert';

function UpdateTypeToolForm({ idTypeTool, onUpdate }) {
  const [alertConfig, setAlertConfig] = useState({ open: false, message: '', type: 'success' });

  const handleCloseAlert = (event, reason) => {
    if (reason === 'clickaway') {
      return;
    }
    setAlertConfig({ ...alertConfig, open: false });
  };
  const [values, setValues] = useState({
    dailyRate: '',
    debtRate: '',
    replacementRate: ''
  });

  const [selected, setSelected] = useState({
    dailyRate: false,
    debtRate: false,
    replacementRate: false
  });

  const handleInputChange = (e) => {
    setValues({ ...values, [e.target.name]: e.target.value });
  };

  const handleCheckChange = (e) => {
    setSelected({ ...selected, [e.target.name]: e.target.checked });
  };

  const handleSubmit = async (e) => {
    if (e) e.preventDefault();

    let hayCambios = false;

    try {

      if (selected.dailyRate && values.dailyRate) {
        const rate = parseInt(values.dailyRate, 10);

        await toolServices.configurationDailyRateTypeTool(idTypeTool, rate);
        hayCambios = true;
      }


      if (selected.debtRate && values.debtRate) {
        const rate = parseInt(values.debtRate, 10);
        await toolServices.configurationDebtTypeTool(idTypeTool, rate);
        hayCambios = true;
      }

      if (selected.replacementRate && values.replacementRate) {
        const rate = parseInt(values.replacementRate, 10);
        await toolServices.registerReplacementTypeTool(idTypeTool, rate);
        hayCambios = true;
      }

      if (!hayCambios) {
        setAlertConfig({
          open: true,
          message: 'Por favor, selecciona al menos una opción válida para actualizar.',
          type: 'error'
        });
        return;
      }

      setAlertConfig({
        open: true,
        message: '¡Datos actualizados correctamente!',
        type: 'success'
      });
      onUpdate();

    } catch (error) {
      console.error("Error al actualizar:", error);
      if (error.response && error.response.data) {
        setAlertConfig({
          open: true,
          message: error.response.data,
          type: 'error'
        });
      } else {
        setAlertConfig({
          open: true,
          message: 'Hubo un error al actualizar uno o más campos.',
          type: 'error'
        });
      }
    }
  };

  return (
    <div className="form-container" style={{ overflowY: 'auto', maxHeight: '100vh', padding: '20px' }} >
      <h3 className='title-input'>Configuración de tarifas</h3>

      <div className="form-data">
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
          <Checkbox
            checked={selected.dailyRate}
            onChange={handleCheckChange}
            name="dailyRate"
          />
          <label style={{ margin: 0 }}>Editar Tarifa Diaria:</label>
        </div>

        <TextField
          label="Precio Diario"
          name="dailyRate"
          type="number"
          value={values.dailyRate}
          onChange={handleInputChange}
          disabled={!selected.dailyRate}
          size="small"
          fullWidth
          sx={{ '& .MuiOutlinedInput-root': { backgroundColor: '#FFFFFF', borderRadius: '10px' } }}
        />
      </div>

      <Box className="form-data">
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
          <Checkbox
            checked={selected.debtRate}
            onChange={handleCheckChange}
            name="debtRate"
          />
          <label style={{ margin: 0 }}>Editar Multa Atraso:</label>
        </div>
        <TextField
          label="Costo por Atraso"
          name="debtRate"
          type="number"
          value={values.debtRate}
          onChange={handleInputChange}
          disabled={!selected.debtRate}
          size="small"
          fullWidth
          sx={{ '& .MuiOutlinedInput-root': { backgroundColor: '#FFFFFF', borderRadius: '10px' } }}
        />
      </Box>

      <div className="form-data">
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
          <Checkbox
            checked={selected.replacementRate}
            onChange={handleCheckChange}
            name="replacementRate"
          />
          <label style={{ margin: 0 }}>Editar Reposición:</label>
        </div>
        <TextField
          label="Valor Reposición"
          name="replacementRate"
          type="number"
          value={values.replacementRate}
          onChange={handleInputChange}
          disabled={!selected.replacementRate}
          size="small"
          fullWidth
          sx={{ '& .MuiOutlinedInput-root': { backgroundColor: '#FFFFFF', borderRadius: '10px' } }}
        />
      </div>

      <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', marginTop: '20px' }}>
        <button className='button-style' type="button" onClick={handleSubmit}>Guardar Cambios</button>
      </div>
      <ButtonAlert
        open={alertConfig.open}
        handleClose={handleCloseAlert}
        message={alertConfig.message}
        type={alertConfig.type}
      />
    </div>
  );
}
export default UpdateTypeToolForm;