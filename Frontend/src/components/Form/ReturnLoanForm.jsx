import React, { useState } from 'react';
import PropTypes from 'prop-types';
import loanService from '../../services/loan.services';
import '../../App.css';
import { Typography } from '@mui/material';
import ButtonAlert from '../Styles/ButtonAlert';

function ReturnLoanForm({ onReturnLoan }) {
    const [loanId, setLoanId] = useState('');
    const [loanDetails, setLoanDetails] = useState(null);
    const [toolStates, setToolStates] = useState({});
    const [isLoading, setIsLoading] = useState(false);
    const [alertConfig, setAlertConfig] = useState({ open: false, message: '', type: 'success' });
    const [errors, setErrors] = useState({});

    const handleCloseAlert = (event, reason) => {
        if (reason === 'clickaway') {
            return;
        }
        setAlertConfig({ ...alertConfig, open: false });
    };


    const handleFetchLoan = async () => {
        if (!loanId) {
            setErrors({ loanId: "El ID del préstamo es obligatorio para buscar." });
            return;
        }
        setIsLoading(true);
        setLoanDetails(null);
        try {
            const response = await loanService.getLoanById(loanId);
            const data = response.data;

            if (data.state === 'RETURNED') {
                setAlertConfig({
                    open: true,
                    message: 'El préstamo ya ha sido devuelto.',
                    type: 'warning'
                });
                return;
            }

            if (!data) {
                setAlertConfig({
                    open: true,
                    message: 'El préstamo no existe.',
                    type: 'warning'
                });
                return;
            }

            setLoanDetails(data);

            const initialStates = {};
            data.tool.forEach(tool => {
                initialStates[tool.idTool] = 'GOOD';
            });
            setToolStates(initialStates);

        } catch (err) {
            let errorMsg = 'Error al buscar el préstamo. Por favor, inténtalo de nuevo.';

            if (err.response?.status === 404) {
                errorMsg = 'El préstamo no existe.';
            } else if (err.response?.data) {
                if (typeof err.response.data === 'string') {
                    errorMsg = err.response.data;
                } else if (err.response.data.message) {
                    errorMsg = err.response.data.message;
                } else if (err.response.data.error) {
                    errorMsg = `Error del servidor: ${err.response.data.error}`;
                }
            } else if (err.message) {
                errorMsg = err.message;
            }

            setAlertConfig({
                open: true,
                message: errorMsg,
                type: 'error'
            });
            console.error("Error al buscar el préstamo:", err);
        }
        setIsLoading(false);
    };

    const handleStateChange = (toolId, newState) => {
        setToolStates(prevStates => ({
            ...prevStates,
            [toolId]: newState
        }));
    };
    const handleSubmitReturn = async (event) => {
        event.preventDefault();
        setIsLoading(true);

        const toolStatesArray = Object.keys(toolStates).map(toolId => ({
            toolId: Number.parseInt(toolId, 10),
            state: toolStates[toolId]
        }));

        const payload = {
            toolStates: toolStatesArray
        };

        try {
            await loanService.returnLoan(loanId, payload);
            setAlertConfig({
                open: true,
                message: '¡Préstamo devuelto con éxito!',
                type: 'success'
            });
            setLoanId('');
            setLoanDetails(null);
            setToolStates({});
            onReturnLoan();
        } catch (err) {
            let errorMsg = 'Hubo un error al procesar la devolución. Por favor, inténtalo de nuevo.';
            if (err.response?.data) {
                if (typeof err.response.data === 'string') {
                    errorMsg = err.response.data;
                } else if (err.response.data.message) {
                    errorMsg = err.response.data.message;
                } else if (err.response.data.error) {
                    errorMsg = `Error del servidor: ${err.response.data.error}`;
                }
            } else if (err.message) {
                errorMsg = err.message;
            }

            setAlertConfig({
                open: true,
                message: errorMsg,
                type: 'error'
            });
            console.error("Error al devolver el préstamo:", err);

        }
        setIsLoading(false);
    };

    return (
        <div style={{ overflowY: 'auto', maxHeight: '100vh', padding: '20px' }}>
            <h2>Registrar Devolución de Préstamo</h2>

            {!loanDetails && (
                <div>
                    <label htmlFor="return-loan-id">
                        ID del Préstamo:
                    </label>
                    <input
                        id="return-loan-id"
                        className="input-style"
                        type="number"
                        value={loanId}
                        onChange={(e) => { setLoanId(e.target.value); setErrors({ ...errors, loanId: '' }); }}
                        placeholder="Ingresa ID del préstamo"
                    />
                    {errors.loanId && <Typography variant="caption" color="error" display="block">{errors.loanId}</Typography>}

                    <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', marginTop: '20px' }}>
                        <br />
                        <button className='button-style' onClick={handleFetchLoan} disabled={isLoading}>
                            {isLoading ? 'Buscando...' : 'Buscar Préstamo'}
                        </button>
                    </div>

                </div>
            )}

            {loanDetails && (
                <form onSubmit={handleSubmitReturn}>
                    <h3>Detalles del Préstamo #{loanDetails.idLoan}</h3>

                    {loanDetails.customer && <p>Cliente: {loanDetails.customer.name}</p>}

                    <h4>Estado de Herramientas Devueltas:</h4>

                    {loanDetails.tool.map(tool => (
                        <div key={tool.idTool}>
                            <label htmlFor={`tool-state-${tool.idTool}`}>{tool.typeTool.name} (ID: {tool.idTool})</label>
                            <select
                                id={`tool-state-${tool.idTool}`}
                                className="input-style"
                                value={toolStates[tool.idTool] || ''}
                                onChange={(e) => handleStateChange(tool.idTool, e.target.value)}
                            >
                                <option value="GOOD">Buen Estado</option>
                                <option value="DAMAGED">Dañado</option>
                            </select>
                        </div>
                    ))}
                    <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', marginTop: '20px' }}>
                        <button className='button-style' type="submit" disabled={isLoading}>
                            {isLoading ? 'Procesando...' : 'Confirmar Devolución'}
                        </button>
                    </div>

                </form>
            )}
            <ButtonAlert
                open={alertConfig.open}
                handleClose={handleCloseAlert}
                message={alertConfig.message}
                type={alertConfig.type}
            />
        </div>
    );
}

ReturnLoanForm.propTypes = {
    onReturnLoan: PropTypes.func.isRequired,
};

export default ReturnLoanForm;