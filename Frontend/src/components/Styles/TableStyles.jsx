import { styled } from '@mui/material/styles';
import MuiTableCell from '@mui/material/TableCell';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';

// Estilo para el Encabezado
const StyledTableHead = styled(TableHead)(({ theme }) => ({
    backgroundColor: '#4E7D10', // Un gris claro del tema

}));

// Estilo para las Celdas del Encabezado
const StyledHeaderCell = styled(MuiTableCell)(({ theme }) => ({
    fontWeight: 'bold',
    color: '#ffffffff', // Color de texto primario del tema
    textAlign: 'center',
}));

// Estilo para las Filas del Cuerpo
const StyledBodyRow = styled(TableRow)(({ theme }) => ({
    '&:hover': {
        backgroundColor: theme.palette.action.hover, // Color hover del tema
        cursor: 'pointer',
    },
    // Ocultar el borde de la última fila
    '&:last-child td, &:last-child th': {
        border: 0,
    },
}));

export { StyledTableHead, StyledHeaderCell, StyledBodyRow };

