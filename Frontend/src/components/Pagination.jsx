// Archivo: MiPaginacion.js
import * as React from 'react';
import PropTypes from 'prop-types';
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

MiPaginacion.propTypes = {
  count: PropTypes.number.isRequired,
  page: PropTypes.number.isRequired,
  onChange: PropTypes.func.isRequired,
};

