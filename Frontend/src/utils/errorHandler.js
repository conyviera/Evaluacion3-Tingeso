export const getErrorMessage = (err) => {
    if (!err || !err.response) {
        return "Error de conexión con el servidor. Verifica tu conexión a internet o contacta con soporte.";
    }
    if (err.response.status === 401 || err.response.status === 403) {
        return "Error de autenticación o permisos insuficientes. Por favor, recarga o inicie sesión nuevamente.";
    }
    if (err.response.status === 404) {
        return "No se encontraron datos.";
    }
    if (err.response.data && err.response.data.message) {
        return err.response.data.message;
    }
    return "Error al procesar la solicitud. Por favor, intente nuevamente.";
};
