import axios from "axios";
import keycloak from "./keycloak"; 

// Usar URL base completa directamente
const baseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8090/api/v1';

console.log("Conectando a:", baseURL);

const httpClient = axios.create({
  baseURL: baseURL,
  headers: {
    "Content-Type": "application/json"
  }
});

// Interceptor que se ejecuta en cada petición
httpClient.interceptors.request.use(async (config) => {
  console.log("--- INTERCEPTOR DE AXIOS ACTIVADO ---");
  
  if (keycloak.authenticated) {
    console.log("Keycloak está autenticado. Intentando actualizar token...");
    try {
      await keycloak.updateToken(30);
      config.headers.Authorization = `Bearer ${keycloak.token}`;
      console.log("Token añadido a la cabecera:", `Bearer ${keycloak.token.substring(0, 20)}...`); 
    } catch (error) {
      console.error("Failed to refresh token or update auth header", error);
    }
  } else {
    console.log("Keycloak no está autenticado. Petición sin token.");
  }
  
  return config;
}, (error) => {
  return Promise.reject(error);
});

export default httpClient;
