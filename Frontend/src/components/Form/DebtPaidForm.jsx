import React from 'react';
import PropTypes from 'prop-types';
import loanService from '../../services/loan.services.js';

function DebtPaidForm({ debt, onDebtPaid }) {

    if (!debt) {
        return null;
    }

    const handleSubmit = async (e) => {
        e.preventDefault();
        const isConfirmed = window.confirm("Confirma que deseas pagar esta deuda?");

        if (isConfirmed) {
            try {
                await loanService.payDebt(debt.idDebts);
                onDebtPaid();
            } catch (error) {
                console.error("Error al pagar la deuda", error);
            }
        }
    };
    return (
        <form onSubmit={handleSubmit}>
            <div>
                <label htmlFor="debt-amount">Monto de la deuda: </label>
                <span id="debt-amount">{debt.amount}</span>
            </div>
            <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', marginTop: '20px' }}>
                <button className='button-style' type="submit">Pagar Deuda</button>
            </div>
        </form>
    );
}

DebtPaidForm.propTypes = {
    debt: PropTypes.shape({
        idDebts: PropTypes.number,
        amount: PropTypes.number,
    }),
    onDebtPaid: PropTypes.func.isRequired,
};

export default DebtPaidForm;