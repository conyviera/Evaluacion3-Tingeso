import React, { useState, useEffect } from 'react';
import PropTypes from 'prop-types';
import { useParams } from 'react-router-dom';
import toolServices from '../../services/tool.services';
import { Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle, Button } from '@mui/material';

const DeactivateUnusedTool = ({ id, onUpdate }) => {

  const { id: paramId } = useParams();
  const toolId = id ?? paramId;

  const [tool, setTool] = useState(null);
  const [loading, setLoading] = useState(true);
  const [alertConfig, setAlertConfig] = useState({ open: false, message: '', type: 'success' });

  // Custom Confirmation Dialog State
  const [openConfirm, setOpenConfirm] = useState(false);

  useEffect(() => {
    const fetchTool = async () => {
      try {
        const response = await toolServices.getByIdTool(toolId);
        setTool(response.data);
      } catch (error) {
        setAlertConfig({
          open: true,
          message: 'Error al cargar la herramienta.',
          type: 'error'
        });
      } finally {
        setLoading(false);
      }
    };

    if (toolId) {
      fetchTool();
    }
  }, [toolId]);

  const handleSubmit = (e) => {
    e.preventDefault();
    setOpenConfirm(true); // Open the MUI Dialog instead of window.confirm
  };

  const handleConfirm = async () => {
    setOpenConfirm(false);

    try {
      await toolServices.deactivateUnusedTool(toolId);

      setAlertConfig({
        open: true,
        message: 'Éxito: La herramienta ha sido dada de baja correctamente.',
        type: 'success'
      });

      if (onUpdate) onUpdate();

    } catch (error) {
      setAlertConfig({
        open: true,
        message: 'Error: No se pudo dar de baja la herramienta.',
        type: 'error'
      });
    }
  };

  const handleCancel = () => {
    setOpenConfirm(false);
  };


  if (loading) return <p>Cargando información de la herramienta...</p>;

  if (!tool) return <p>No se encontró la herramienta.</p>;

  return (
    <div className="tool-manager">
      <h2>Gestionar Herramienta: {tool.typeTool?.name || ''}</h2>
      <p>Estado actual: <strong>{tool.state}</strong></p>


      {tool.state === "AVAILABLE" ? (
        <form onSubmit={handleSubmit}>
          <p>Esta herramienta está disponible para ser dada de baja.</p>
          <button type="submit" style={{ backgroundColor: 'red', color: 'white' }}>
            Dar de baja herramienta
          </button>
        </form>
      ) : (
        <div className="alert-box">
          {tool.state === "DECOMMISSIONE" && (
            <p>Esta herramienta ya ha sido dada de baja </p>
          )}
          {tool.state === "ON_LOAN" && (
            <p>La herramienta tiene un prestamo activo</p>
          )}
          {tool.state === "UNDER_REPAIR" && (
            <p>La herramienta está en mantenimiento</p>
          )}
        </div>
      )}
      {alertConfig.message && <p className="feedback-message">{alertConfig.message}</p>}

      {/* Confirmation Dialog */}
      <Dialog
        open={openConfirm}
        onClose={handleCancel}
        aria-labelledby="alert-dialog-title"
        aria-describedby="alert-dialog-description"
      >
        <DialogTitle id="alert-dialog-title">
          Confirmar Acción Destructiva
        </DialogTitle>
        <DialogContent>
          <DialogContentText id="alert-dialog-description">
            ¿Estás seguro que quieres dar de baja esta herramienta de manera irreversible? No podrás deshacer esta acción.
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCancel} color="primary" autoFocus>
            Cancelar
          </Button>
          <Button onClick={handleConfirm} color="error" variant="contained">
            Dar de Baja
          </Button>
        </DialogActions>
      </Dialog>
    </div>
  );
};

DeactivateUnusedTool.propTypes = {
  id: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
  onUpdate: PropTypes.func,
};

export default DeactivateUnusedTool;