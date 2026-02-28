// Archivo: MiPaginacion.js
import * as React from 'react';
import Pagination from '@mui/material/Pagination';
import Stack from '@mui/material/Stack';

export default function MiPaginacion({ count, page, onChange }) {

  
  return (
    <Stack spacing={2}>
        
      <Pagination 
        count={count}         // Total de páginas
        page={page}           // Página actual
        onChange={onChange}   // Función que se llama al cambiar
        color="primary"
        variant="outlined" 
      />
    </Stack>
  );
}