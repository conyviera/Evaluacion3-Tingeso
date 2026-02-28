import React, { useState } from 'react';
import { Typography, Tooltip, IconButton, Box } from '@mui/material';
import InfoIcon from '@mui/icons-material/Info';
import customerService from '../../services/customer.services';
import Logo from '../../image/logo.png';
import '../../App.css';
import ButtonAlert from '../Styles/ButtonAlert';

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

        // Validation
        let newErrors = {};
        if (!rut)
            newErrors.rut = "El RUT es obligatorio.";
        else if (!/\d{7,8}-\d/.test(rut)) {
            newErrors.rut = "El formato del RUT es inválido.";
        }
        if (!name) newErrors.name = "El nombre es obligatorio.";
        if (!email) {
            newErrors.email = "El correo es obligatorio.";
        } else if (!/\S+@\S+\.\S+/.test(email)) {
            newErrors.email = "El formato del correo es inválido.";
        }
        if (!phoneNumber) newErrors.phoneNumber = "El teléfono es obligatorio.";

        if (Object.keys(newErrors).length > 0) {
            setErrors(newErrors);
            return;
        }

        const customerData = {
            name: name,
            email: email,
            phoneNumber: phoneNumber,
            rut: rut
        };

        try {
            const response = await customerService.createCustomer(customerData);
            setAlertConfig({ open: true, message: 'Cliente agregado con éxito', type: 'success' });
            clearForm();
            onCustomerAdded();

        } catch (error) {
            console.error("Error al registrar el cliente", error);
            const errorMsg = error.response?.data?.message || (typeof error.response?.data === 'string' ? error.response.data : '');

            if (errorMsg) {
                const lowerMsg = errorMsg.toLowerCase();
                if (lowerMsg.includes('rut')) {
                    setErrors({ rut: errorMsg });
                } else if (lowerMsg.includes('correo') || lowerMsg.includes('email')) {
                    setErrors({ email: errorMsg });
                } else if (lowerMsg.includes('teléfono') || lowerMsg.includes('telefono')) {
                    setErrors({ phoneNumber: errorMsg });
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
                {/* ... (Tu logo está bien) ... */}
                <h3 className='title-input'>Agregar Nuevo Cliente</h3>

                <label>
                    <Box component="span" sx={{ display: 'flex', alignItems: 'center' }}>
                        Rut:
                        <Tooltip title="Formato esperado: 12345678-9 (con guión).">
                            <InfoIcon fontSize="small" sx={{ ml: 1, color: 'text.secondary' }} />
                        </Tooltip>
                    </Box>
                    <input
                        className="input-style"
                        placeholder='12345678-9'
                        type="text"
                        value={rut}
                        onChange={(e) => { setRut(e.target.value); setErrors({ ...errors, rut: '' }) }}
                    />
                    {errors.rut && <Typography variant="caption" color="error" display="block">{errors.rut}</Typography>}
                </label>
                <label>
                    Nombre:
                    <input
                        className="input-style"
                        type="text"
                        placeholder="Nombre"
                        value={name}
                        onChange={(e) => { setName(e.target.value); setErrors({ ...errors, name: '' }) }}
                    />
                    {errors.name && <Typography variant="caption" color="error" display="block">{errors.name}</Typography>}
                </label> <br />
                <label>
                    Email:
                    <input
                        className="input-style"
                        placeholder='ejemplo@gmail.com'
                        type="email"
                        value={email}
                        onChange={(e) => { setEmail(e.target.value); setErrors({ ...errors, email: '' }) }}
                    />
                    {errors.email && <Typography variant="caption" color="error" display="block">{errors.email}</Typography>}
                </label><br />
                <label>
                    Teléfono:
                    <input
                        className="input-style"
                        placeholder='+56912345678'
                        type="text"
                        value={phoneNumber}
                        onChange={(e) => { setPhoneNumber(e.target.value); setErrors({ ...errors, phoneNumber: '' }) }}
                    />
                    {errors.phoneNumber && <Typography variant="caption" color="error" display="block">{errors.phoneNumber}</Typography>}
                </label><br />
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

export default AddCustomerForm;