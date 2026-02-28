import React from 'react'
import ReactDOM from 'react-dom/client'
import App from './App.jsx'
import './index.css'
import { ReactKeycloakProvider } from "@react-keycloak/web";
import keycloak from "./services/keycloak";
import { BrowserRouter } from 'react-router-dom';

import { ThemeProvider, createTheme } from '@mui/material/styles';
import CssBaseline from '@mui/material/CssBaseline';


const theme = createTheme({
  palette: {
    mode: 'light',
  },
});


const initOptions = {
  onLoad: 'login-required'
}

ReactDOM.createRoot(document.getElementById('root')).render(
  <BrowserRouter>
    <ReactKeycloakProvider authClient={keycloak} initOptions={initOptions}>

      <ThemeProvider theme={theme}>
        <CssBaseline />
        <App />
      </ThemeProvider>

    </ReactKeycloakProvider>
  </BrowserRouter>
)