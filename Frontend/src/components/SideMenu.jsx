import * as React from "react";
import { styled, useTheme } from "@mui/material/styles";
import { Box, Toolbar, AppBar as MuiAppBar, List, IconButton, ListItemButton, ListItemIcon, ListItemText, Typography, Breadcrumbs, InputBase, alpha } from '@mui/material';
import MuiDrawer from "@mui/material/Drawer";
import { useNavigate, Outlet, useLocation } from "react-router-dom";
import Menu from '@mui/material/Menu';
import MenuItem from '@mui/material/MenuItem';
import { useKeycloak } from '@react-keycloak/web';


import ArrowBackIcon from "@mui/icons-material/ArrowBack";
import PeopleAltIcon from "@mui/icons-material/PeopleAlt";
import ConstructionOutlinedIcon from "@mui/icons-material/ConstructionOutlined";
import HomeIcon from "@mui/icons-material/Home";
import CreditScoreIcon from "@mui/icons-material/CreditScore";
import AccountCircleIcon from "@mui/icons-material/AccountCircle";
import InventoryIcon from '@mui/icons-material/Inventory';
import NavigateNextIcon from '@mui/icons-material/NavigateNext';
import SearchIcon from '@mui/icons-material/Search';
import logo from '../image/logo.png';


const drawerWidth = 180;

const DrawerHeader = styled("div")(({ theme }) => ({
  display: "flex",
  alignItems: "center",
  justifyContent: "center",
  padding: theme.spacing(1, 1),
  ...theme.mixins.toolbar,
}));

const Drawer = styled(MuiDrawer)(({ theme }) => ({
  width: drawerWidth,
  flexShrink: 0,
  whiteSpace: "nowrap",
  boxSizing: "border-box",
  "& .MuiDrawer-paper": {
    width: drawerWidth,
    backgroundColor: '#EAEAD1',
    overflow: 'hidden',
    backdropFilter: "none",
    WebkitBackdropFilter: "none",
    boxShadow: "none",
    border: "none",
  },
}));

const selectedStyle = (theme) => ({
  backgroundColor: '#4E7D10',
  borderRadius: '17px',
  width: 'auto',
  '&, &:hover': {
    backgroundColor: '#4E7D10',
    filter: 'drop-shadow(0 0 10px #4E7D10)',
  },
  '& .MuiListItemIcon-root': {
    color: 'white',
  },
  '& .MuiListItemText-root': {
    color: 'white',
  },
});

const Search = styled('div')(({ theme }) => ({
  position: 'relative',
  borderRadius: theme.shape.borderRadius,
  backgroundColor: alpha(theme.palette.common.black, 0.05),
  '&:hover': {
    backgroundColor: alpha(theme.palette.common.black, 0.10),
  },
  marginRight: theme.spacing(2),
  marginLeft: 0,
  width: '100%',
  [theme.breakpoints.up('sm')]: {
    marginLeft: theme.spacing(3),
    width: 'auto',
  },
}));

const SearchIconWrapper = styled('div')(({ theme }) => ({
  padding: theme.spacing(0, 2),
  height: '100%',
  position: 'absolute',
  pointerEvents: 'none',
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'center',
  color: theme.palette.text.secondary,
}));

const StyledInputBase = styled(InputBase)(({ theme }) => ({
  color: 'inherit',
  width: '100%',
  '& .MuiInputBase-input': {
    padding: theme.spacing(1, 1, 1, 0),
    // vertical padding + font size from searchIcon
    paddingLeft: `calc(1em + ${theme.spacing(4)})`,
    transition: theme.transitions.create('width'),
    [theme.breakpoints.up('md')]: {
      width: '20ch',
      '&:focus': {
        width: '30ch',
      },
    },
    color: theme.palette.text.primary,
  },
}));

// Mapa de rutas a etiquetas para el breadcrumb
const routeLabels = {
  '/': 'Inicio',
  '/CustomerManagementPage': 'Clientes',
  '/ToolInventoryPage': 'Inventario',
  '/LoanPage': 'Préstamos',
  '/Kardex': 'Historial de Movimientos',
};

// Rutas que tienen padre (para el breadcrumb encadenado)
const routeParents = {
  '/typeTool': '/ToolInventoryPage',
  '/DeacticateUnusedTool': '/ToolInventoryPage',
  '/MovementsTool': '/ToolInventoryPage',
  '/UpdateTypeTool': '/ToolInventoryPage',
  '/DebtList': '/LoanPage',
};

function getBreadcrumbs(pathname) {
  // Ruta exacta
  if (routeLabels[pathname]) {
    if (pathname === '/') return [{ label: 'Inicio', path: '/' }];
    return [
      { label: 'Inicio', path: '/' },
      { label: routeLabels[pathname], path: pathname },
    ];
  }

  // Rutas dinámicas (con parámetros)
  for (const [segment, parentPath] of Object.entries(routeParents)) {
    if (pathname.startsWith(segment)) {
      const dynamicLabels = {
        '/typeTool': 'Herramientas Unitarias',
        '/DeacticateUnusedTool': 'Dar de Baja',
        '/MovementsTool': 'Movimientos',
        '/UpdateTypeTool': 'Editar Tipo',
        '/DebtList': 'Deudas',
      };
      return [
        { label: 'Inicio', path: '/' },
        { label: routeLabels[parentPath], path: parentPath },
        { label: dynamicLabels[segment], path: pathname },
      ];
    }
  }

  return [{ label: 'Inicio', path: '/' }];
}

export default function MainLayout() {
  const theme = useTheme();
  const navigate = useNavigate();
  const location = useLocation();
  const [searchQuery, setSearchQuery] = React.useState('');
  const { keycloak } = useKeycloak();

  const breadcrumbs = getBreadcrumbs(location.pathname);
  const isHome = location.pathname === '/';

  const userName = keycloak?.tokenParsed?.preferred_username || keycloak?.tokenParsed?.name || "Usuario";

  const [anchorEl, setAnchorEl] = React.useState(null);
  const open = Boolean(anchorEl);

  const handleMenuClick = (event) => {
    setAnchorEl(event.currentTarget);
  };

  const handleMenuClose = () => {
    setAnchorEl(null);
  };

  const handleLogout = () => {
    handleMenuClose();
    if (keycloak) {
      keycloak.logout();
    }
  };

  return (

    <Box sx={{ display: "flex", height: '100%', bgcolor: '#EAEAD1' }}>

      <Drawer variant="permanent">
        <DrawerHeader>
          <Box
            component="img"
            sx={{ height: 100, my: 1 }}
            alt="Logo"
            src={logo}
          />
        </DrawerHeader>

        <List sx={{ p: 1.5 }}>
          <ListItemButton
            onClick={() => navigate("/")}
            sx={location.pathname === "/" ? selectedStyle(theme) : {}}
          >
            <ListItemIcon><HomeIcon /></ListItemIcon>
            <ListItemText primary="Inicio" />
          </ListItemButton>

          <ListItemButton
            onClick={() => navigate("/CustomerManagementPage")}
            sx={location.pathname === "/CustomerManagementPage" ? selectedStyle(theme) : {}}
          >
            <ListItemIcon><PeopleAltIcon /></ListItemIcon>
            <ListItemText primary="Clientes" />
          </ListItemButton>

          <ListItemButton
            onClick={() => navigate("/ToolInventoryPage")}
            sx={location.pathname === "/ToolInventoryPage" ? selectedStyle(theme) : {}}
          >
            <ListItemIcon><ConstructionOutlinedIcon /></ListItemIcon>
            <ListItemText primary="Inventario" />
          </ListItemButton>

          <ListItemButton
            onClick={() => navigate("/LoanPage")}
            sx={location.pathname === "/LoanPage" ? selectedStyle(theme) : {}}
          >
            <ListItemIcon><CreditScoreIcon /></ListItemIcon>
            <ListItemText primary="Prestamos" />
          </ListItemButton>

          <ListItemButton
            onClick={() => navigate("/Kardex")}
            sx={location.pathname === "/Kardex" ? selectedStyle(theme) : {}}
          >
            <ListItemIcon><InventoryIcon /></ListItemIcon>
            <ListItemText primary="Historial" />
          </ListItemButton>
        </List>
      </Drawer>

      <Box
        component="main"
        sx={{
          flexGrow: 1,
          p: 2,
          boxSizing: 'border-box',
          backgroundColor: "#EAEAD1",
          minWidth: 0
        }}
      >

        <Box sx={{
          width: '100%',
          height: '100%',
          bgcolor: '#FFFFFF',
          borderRadius: '16px',
          overflow: 'hidden',
          display: 'flex',
          flexDirection: 'column',
          boxShadow: '0 2px 12px rgba(0,0,0,0.08)'
        }}>

          <MuiAppBar
            position="static"
            elevation={0}
            sx={{
              backgroundColor: "#FFFFFF",
              borderBottom: "1px solid #E0E0D8",
              color: "#000"
            }}
          >
            <Toolbar>
              {/* Botón retroceder: solo visible si no estamos en Home */}
              <IconButton
                color="inherit"
                aria-label="go back"
                onClick={() => navigate(-1)}
                edge="start"
                sx={{
                  marginRight: 2,
                  visibility: isHome ? 'hidden' : 'visible',
                }}
              >
                <ArrowBackIcon />
              </IconButton>

              {/* Breadcrumb de navegación */}
              <Breadcrumbs
                separator={<NavigateNextIcon fontSize="small" />}
                aria-label="breadcrumb"
                sx={{ flexGrow: 1 }}
              >
                {breadcrumbs.map((crumb, index) => {
                  const isLast = index === breadcrumbs.length - 1;
                  return (
                    <Typography
                      key={crumb.path}
                      variant="body2"
                      sx={{
                        cursor: isLast ? 'default' : 'pointer',
                        color: isLast ? '#4E7D10' : 'text.secondary',
                        fontWeight: isLast ? 700 : 400,
                        '&:hover': isLast ? {} : { textDecoration: 'underline' },
                      }}
                      onClick={() => !isLast && navigate(crumb.path)}
                    >
                      {crumb.label}
                    </Typography>
                  );
                })}
              </Breadcrumbs>

              <IconButton
                color="inherit"
                onClick={handleMenuClick}
                aria-controls={open ? 'account-menu' : undefined}
                aria-haspopup="true"
                aria-expanded={open ? 'true' : undefined}
                sx={{ display: 'flex', alignItems: 'center', gap: 1, borderRadius: 2 }}
              >
                <Typography variant="body1" sx={{ fontWeight: 500, fontSize: '1rem', textTransform: 'capitalize' }}>
                  {userName}
                </Typography>
                <AccountCircleIcon />
              </IconButton>
              <Menu
                id="account-menu"
                anchorEl={anchorEl}
                open={open}
                onClose={handleMenuClose}
                PaperProps={{
                  elevation: 0,
                  sx: {
                    overflow: 'visible',
                    filter: 'drop-shadow(0px 2px 8px rgba(0,0,0,0.15))',
                    mt: 1.5,
                  },
                }}
                anchorOrigin={{
                  vertical: 'bottom',
                  horizontal: 'right',
                }}
                transformOrigin={{
                  vertical: 'top',
                  horizontal: 'right',
                }}
              >
                <MenuItem onClick={handleLogout}>Cerrar sesión</MenuItem>
              </Menu>
            </Toolbar>
          </MuiAppBar>

          <Box
            sx={{
              flexGrow: 1,
              p: 3,
              overflowY: 'auto'
            }}
          >
            <Outlet />
          </Box>
        </Box>
      </Box>
    </Box>
  );
}