import React, { useState } from 'react';
import loanService from '../../services/loan.services.js';

function DebtPaidForm({ debt, onDebtPaid }) {
    const [amountPaid, setAmountPaid] = useState('');
    const [alertConfig, setAlertConfig] = useState({ open: false, message: '', type: 'success' });

    const handleCloseAlert = (event, reason) => {
        if (reason === 'clickaway') {
            return;
        }
        setAlertConfig({ ...alertConfig, open: false });
    };

    if (!debt) {
        return null;
    }

    const handleSubmit = async (e) => {
        e.preventDefault();
        const isConfirmed = window.confirm("Confirma que deseas pagar esta deuda?");

        if (isConfirmed) {
            try {
                await loanService.payDebt(debt.idDebts);

                setAlertConfig({
                    open: true,
                    message: '¡Deuda pagada con éxito!',
                    type: 'success'
                });
                onDebtPaid();
            } catch (error) {
                console.error("Error al pagar la deuda", error);
                setAlertConfig({
                    open: true,
                    message: 'Error al pagar la deuda. Por favor, inténtalo de nuevo.',
                    type: 'error'
                });
            }
        }
    };
    return (
        <form onSubmit={handleSubmit}>
            <div>
                <label>Monto de la deuda: </label>
                <span>{debt.amount}</span>
            </div>
            <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', marginTop: '20px' }}>
                <button className='button-style' type="submit">Pagar Deuda</button>
            </div>
        </form>
    );
}
export default DebtPaidForm;