import React from 'react';
import { Box, Typography, Button } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import ErrorOutlineIcon from '@mui/icons-material/ErrorOutline';
import { buttonPrimary } from '../components/Styles/ButtonStyles';

const NotFoundPage = () => {
    const navigate = useNavigate();

    return (
        <Box
            sx={{
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'center',
                justifyContent: 'center',
                minHeight: '80vh',
                textAlign: 'center',
                padding: { xs: 2, md: 4 },
                bgcolor: 'background.default',
                borderRadius: '16px',
            }}
        >
            <ErrorOutlineIcon sx={{ fontSize: 100, color: 'text.secondary', mb: 2 }} />
            <Typography variant="h2" component="h1" fontWeight="bold" color="text.primary" gutterBottom>
                404
            </Typography>
            <Typography variant="h5" color="text.secondary" gutterBottom>
                Oops! La página que buscas no se encuentra.
            </Typography>
            <Typography variant="body1" color="text.secondary" sx={{ mb: 4, maxWidth: 'sm' }}>
                Es posible que el enlace sea incorrecto, o que la página haya sido eliminada o movida a otra dirección.
            </Typography>
            <Button
                variant="contained"
                size="large"
                onClick={() => navigate('/')}
                sx={buttonPrimary}
            >
                Volver al Inicio
            </Button>
        </Box>
    );
};

export default NotFoundPage;
