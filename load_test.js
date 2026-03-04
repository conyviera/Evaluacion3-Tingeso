import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  stages: [
    { duration: '30s', target: 20 }, 
    { duration: '1m', target: 20 },  
    { duration: '30s', target: 50 }, 
    { duration: '1m', target: 50 },  
    { duration: '20s', target: 0 },  
  ],
  thresholds: {
    // Aumentamos a 0.60 (60%) porque esperamos muchos errores 400 de negocio
    http_req_failed: ['rate<0.60'], 
    http_req_duration: ['p(95)<500'], 
  },
};

function getAccessToken() {
  const url = 'http://localhost:8080/realms/sisgr-realm/protocol/openid-connect/token';
  const payload = {
    grant_type: 'password',
    client_id: 'sisgr-backend',
    username: 'admin',
    password: '12345',
  };

  const res = http.post(url, payload);
  if (res.status !== 200) return null;
  return res.json().access_token;
}

export default function () {
  const token = getAccessToken();
  if (!token) return;

  const url = 'http://localhost:8090/api/v1/loans/createLoan';
  
  // Aleatorización total de IDs del 1 al 50
  const randomToolId = Math.floor(Math.random() * 50) + 1;
  const randomCustomerId = Math.floor(Math.random() * 50) + 1;

  const loanData = JSON.stringify({
    "typeToolIds": [randomToolId],
    "customerId": randomCustomerId,
    "deliveryDate": "2026-06-01", // Fecha futura para evitar validaciones de tiempo
    "returnDate": "2026-06-05"
  });

  const params = {
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`,
    },
  };

  const res = http.post(url, loanData, params);

  // Consideramos "éxito de rendimiento" tanto el 201 como el 400, 
  // porque el servidor respondió rápido a una regla de negocio.
  check(res, {
    'Servidor respondio': (r) => r.status === 201 || r.status === 200 || r.status === 400,
  });

  sleep(1); 
}