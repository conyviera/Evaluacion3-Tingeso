import React, { useState } from 'react';
import toolServices from '../../services/tool.services.js';

function AssessToolDamageForm({ tool, onAssessmentComplete }) {
    const [outcome, setOutcome] = useState('');
    const [damageCharge, setDamageCharge] = useState('');
    const [alertConfig, setAlertConfig] = useState({ open: false, message: '', type: 'success' });

    const handleCloseAlert = (event, reason) => {
        if (reason === 'clickaway') {
            return;
        }
        setAlertConfig({ ...alertConfig, open: false });
    };

    if (!tool) {
        return null;
    }

    const handleSubmit = async (e) => {
        e.preventDefault();
        const isConfirmed = window.confirm("¿Confirma la evaluación del daño de la herramienta?");
        if (isConfirmed) {
            try {
                let finalCharge = 0;
                if (damageCharge !== '') {
                    finalCharge = parseInt(damageCharge);
                }
                if (isNaN(finalCharge)) {
                    finalCharge = 0;
                }

                const data = {
                    outcome,
                    damageCharge: finalCharge
                };

                await toolServices.assessToolDamage(tool.idTool, data);
                setAlertConfig({
                    open: true,
                    message: '¡Evaluación de daño registrada con éxito!',
                    type: 'success'
                });
                onAssessmentComplete();
            } catch (error) {
                console.error("Error al registrar la evaluación de daño", error);
                setAlertConfig({
                    open: true,
                    message: 'Error al registrar la evaluación de daño. Por favor, inténtalo de nuevo.',
                    type: 'error'
                });
            }
        }
    };

    return (
        <form onSubmit={handleSubmit}>
            <div>
                <label>Resultado de la evaluación: </label>
                <select className="input-style" value={outcome} onChange={(e) => setOutcome(e.target.value)} required>
                    <option value="">Seleccione una opción</option>
                    <option value="IRREPARABLE">Daño irreparable</option>
                    <option value="MINOR_DAMAGE">Daño menor</option>
                </select>
            </div>
            {outcome !== 'IRREPARABLE' && (
                <div>
                    <label>Cargos por daño (si aplica): </label>
                    <input
                        type="number"
                        value={damageCharge}
                        className="input-style"
                        onChange={(e) => {
                            const value = e.target.value;
                            if (value === '' || Number(value) >= 0) {
                                setDamageCharge(value);
                            }
                        }}
                        min="0"
                        step="1"
                        placeholder="0"
                    />
                </div>
            )}
            <div style={{
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'center'
            }}>
                <button className='button-style' type="submit">Registrar Evaluación</button>
            </div>
        </form>
    );
}
export default AssessToolDamageForm;

