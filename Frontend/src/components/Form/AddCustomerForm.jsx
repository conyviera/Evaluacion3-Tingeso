import React, { useState } from 'react';
import PropTypes from 'prop-types';
import { Typography, Tooltip, Box } from '@mui/material';
import InfoIcon from '@mui/icons-material/Info';
import customerService from '../../services/customer.services';
import '../../App.css';
import ButtonAlert from '../Styles/ButtonAlert';

const validateCustomerForm = ({ rut, name, email, phoneNumber }) => {
    const errs = {};
    if (!rut) {
        errs.rut = 'El RUT es obligatorio.';
    } else if (!/\d{7,8}-\d/.test(rut)) {
        errs.rut = 'El formato del RUT es inválido.';
    }
    if (!name) errs.name = 'El nombre es obligatorio.';
    if (!email) {
        errs.email = 'El correo es obligatorio.';
    } else if (!/\S+@\S+\.\S+/.test(email)) {
        errs.email = 'El formato del correo es inválido.';
    }
    if (!phoneNumber) errs.phoneNumber = 'El teléfono es obligatorio.';
    return errs;
};

const getApiErrorField = (lowerMsg) => {
    if (lowerMsg.includes('rut')) return 'rut';
    if (lowerMsg.includes('correo') || lowerMsg.includes('email')) return 'email';
    if (lowerMsg.includes('teléfono') || lowerMsg.includes('telefono')) return 'phoneNumber';
    return null;
};

function AddCustomerForm({ onCustomerAdded }) {
    const [alertConfig, setAlertConfig] = useState({ open: false, message: '', type: 'success' });

    const [name, setName] = useState('');
    const [email, setEmail] = useState('');
    const [phoneNumber, setPhoneNumber] = useState('');
    const [rut, setRut] = useState('');
    const [errors, setErrors] = useState({});

    const clearForm = () => {
        setName('');
        setEmail('');
        setPhoneNumber('');
        setRut('');
        setErrors({});
    };

    const handleCloseAlert = (event, reason) => {
        if (reason === 'clickaway') {
            return;
        }
        setAlertConfig({ ...alertConfig, open: false });
    };

    const handleSubmit = async (event) => {
        event.preventDefault();

        const newErrors = validateCustomerForm({ rut, name, email, phoneNumber });
        if (Object.keys(newErrors).length > 0) {
            setErrors(newErrors);
            return;
        }

        const customerData = { name, email, phoneNumber, rut };

        try {
            await customerService.createCustomer(customerData);
            setAlertConfig({ open: true, message: 'Cliente agregado con éxito', type: 'success' });
            clearForm();
            onCustomerAdded();
        } catch (error) {
            console.error('Error al registrar el cliente', error);
            const errorMsg = error.response?.data?.message ||
                (typeof error.response?.data === 'string' ? error.response.data : '');

            if (errorMsg) {
                const field = getApiErrorField(errorMsg.toLowerCase());
                if (field) {
                    setErrors({ [field]: errorMsg });
                } else {
                    setAlertConfig({ open: true, message: errorMsg, type: 'error' });
                }
            } else {
                setAlertConfig({ open: true, message: 'Hubo un error de conexión al agregar al cliente.', type: 'error' });
            }
        }
    };

    return (
        <div className="form-container">
            <form className="form-data" onSubmit={handleSubmit}>
                <h3 className='title-input'>Agregar Nuevo Cliente</h3>

                <label htmlFor="cust-rut">
                    <Box component="span" sx={{ display: 'flex', alignItems: 'center' }}>
                        Rut:
                        <Tooltip title="Formato esperado: 12345678-9 (con guión).">
                            <InfoIcon fontSize="small" sx={{ ml: 1, color: 'text.secondary' }} />
                        </Tooltip>
                    </Box>
                    <input
                        id="cust-rut"
                        className="input-style"
                        placeholder='12345678-9'
                        type="text"
                        value={rut}
                        onChange={(e) => { setRut(e.target.value); setErrors({ ...errors, rut: '' }) }}
                    />
                    {errors.rut && <Typography variant="caption" color="error" display="block">{errors.rut}</Typography>}
                </label>
                <label htmlFor="cust-name">
                    Nombre:
                </label>
                <input
                    id="cust-name"
                    className="input-style"
                    type="text"
                    placeholder="Nombre"
                    value={name}
                    onChange={(e) => { setName(e.target.value); setErrors({ ...errors, name: '' }) }}
                />
                {errors.name && <Typography variant="caption" color="error" display="block">{errors.name}</Typography>}
                <label htmlFor="cust-email">
                    Email:
                </label>
                <input
                    id="cust-email"
                    className="input-style"
                    placeholder='ejemplo@gmail.com'
                    type="email"
                    value={email}
                    onChange={(e) => { setEmail(e.target.value); setErrors({ ...errors, email: '' }) }}
                />
                {errors.email && <Typography variant="caption" color="error" display="block">{errors.email}</Typography>}
                <label htmlFor="cust-phone">
                    Teléfono:
                </label>
                <input
                    id="cust-phone"
                    className="input-style"
                    placeholder='+56912345678'
                    type="text"
                    value={phoneNumber}
                    onChange={(e) => { setPhoneNumber(e.target.value); setErrors({ ...errors, phoneNumber: '' }) }}
                />
                {errors.phoneNumber && <Typography variant="caption" color="error" display="block">{errors.phoneNumber}</Typography>}
                <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', marginTop: '20px' }}>
                    <button className='button-style' type="submit">Agregar Cliente</button>
                </div>
            </form>
            <ButtonAlert
                open={alertConfig.open}
                handleClose={handleCloseAlert}
                message={alertConfig.message}
                type={alertConfig.type}
            />
        </div>
    );
}

AddCustomerForm.propTypes = {
    onCustomerAdded: PropTypes.func.isRequired,
};

export default AddCustomerForm;