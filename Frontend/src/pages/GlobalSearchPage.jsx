import React, { useState, useEffect } from 'react';
import { Box, Typography, CircularProgress, Divider, List, ListItem, ListItemText, ListItemButton, Paper } from '@mui/material';
import { useSearchParams, useNavigate } from 'react-router-dom';
import customerService from '../services/customer.services';
import toolService from '../services/tool.services';
import loanServices from '../services/loan.services';
import SearchOffIcon from '@mui/icons-material/SearchOff';

const GlobalSearchPage = () => {
    const [searchParams] = useSearchParams();
    const query = searchParams.get('q') || '';
    const navigate = useNavigate();

    const [results, setResults] = useState({
        customers: [],
        tools: [],
        loans: []
    });
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    useEffect(() => {
        if (!query.trim()) {
            setResults({ customers: [], tools: [], loans: [] });
            return;
        }

        const fetchResults = async () => {
            setLoading(true);
            setError(null);
            try {
                // Fetch all data in parallel
                const [customersRes, toolsRes, loansRes] = await Promise.all([
                    customerService.getAllCustomers().catch(() => ({ data: [] })),
                    toolService.getAllTypeTools().catch(() => ({ data: [] })),
                    loanServices.getAllLoans().catch(() => ({ data: [] }))
                ]);

                const searchTerm = query.toLowerCase();

                // Filter customers
                const matchedCustomers = (customersRes.data || []).filter(c =>
                    String(c.idCustomer || '').toLowerCase().includes(searchTerm) ||
                    String(c.name || '').toLowerCase().includes(searchTerm) ||
                    String(c.rut || '').toLowerCase().includes(searchTerm)
                );

                // Filter tools
                const matchedTools = (toolsRes.data || []).filter(t =>
                    String(t.idTypeTool || '').toLowerCase().includes(searchTerm) ||
                    String(t.name || '').toLowerCase().includes(searchTerm) ||
                    String(t.category || '').toLowerCase().includes(searchTerm)
                );

                // Filter loans
                const matchedLoans = (loansRes.data || []).filter(l =>
                    String(l.idLoan || '').toLowerCase().includes(searchTerm) ||
                    String(l.customer?.name || '').toLowerCase().includes(searchTerm) ||
                    String(l.state || '').toLowerCase().includes(searchTerm)
                );

                setResults({
                    customers: matchedCustomers,
                    tools: matchedTools,
                    loans: matchedLoans
                });

            } catch (err) {
                console.error("Error during global search:", err);
                setError("Ocurrió un error al realizar la búsqueda. Por favor, inténtalo de nuevo.");
            } finally {
                setLoading(false);
            }
        };

        fetchResults();
    }, [query]);

    const totalResults = results.customers.length + results.tools.length + results.loans.length;

    return (
        <Box sx={{ padding: { xs: 2, md: 3 } }}>
            <Typography variant="h5" component="h1" sx={{ mb: 2, fontWeight: 'bold' }}>
                Resultados de la búsqueda para: "{query}"
            </Typography>

            {loading ? (
                <Box sx={{ display: 'flex', alignItems: 'center', mt: 4 }}>
                    <CircularProgress size={24} sx={{ mr: 2, color: '#4E7D10' }} />
                    <Typography>Buscando...</Typography>
                </Box>
            ) : error ? (
                <Typography color="error" sx={{ mt: 2 }}>{error}</Typography>
            ) : !query.trim() ? (
                <Typography color="text.secondary" sx={{ mt: 2 }}>Por favor, ingresa un término de búsqueda.</Typography>
            ) : totalResults === 0 ? (
                <Paper sx={{ p: 4, display: 'flex', flexDirection: 'column', alignItems: 'center', mt: 2, borderRadius: 2 }}>
                    <SearchOffIcon sx={{ fontSize: 60, color: 'text.secondary', mb: 2 }} />
                    <Typography variant="h6" color="text.secondary">No se encontraron resultados</Typography>
                    <Typography color="text.secondary">Prueba con otro término o verifica la ortografía.</Typography>
                </Paper>
            ) : (
                <Box sx={{ mt: 3, display: 'flex', flexDirection: 'column', gap: 3 }}>

                    {/* Resultados de Clientes */}
                    {results.customers.length > 0 && (
                        <Paper sx={{ p: 2, borderRadius: 2 }}>
                            <Typography variant="h6" sx={{ color: '#4E7D10', mb: 1 }}>
                                Clientes ({results.customers.length})
                            </Typography>
                            <Divider sx={{ mb: 1 }} />
                            <List>
                                {results.customers.map(c => (
                                    <ListItem key={c.idCustomer} disablePadding>
                                        <ListItemButton onClick={() => navigate('/CustomerManagementPage')}>
                                            <ListItemText
                                                primary={`ID: ${c.idCustomer} - ${c.name}`}
                                                secondary={`RUT: ${c.rut} | Estado: ${c.state}`}
                                            />
                                        </ListItemButton>
                                    </ListItem>
                                ))}
                            </List>
                        </Paper>
                    )}

                    {/* Resultados de Herramientas */}
                    {results.tools.length > 0 && (
                        <Paper sx={{ p: 2, borderRadius: 2 }}>
                            <Typography variant="h6" sx={{ color: '#4E7D10', mb: 1 }}>
                                Herramientas ({results.tools.length})
                            </Typography>
                            <Divider sx={{ mb: 1 }} />
                            <List>
                                {results.tools.map(t => (
                                    <ListItem key={t.idTypeTool} disablePadding>
                                        <ListItemButton onClick={() => navigate('/ToolInventoryPage')}>
                                            <ListItemText
                                                primary={`ID: ${t.idTypeTool} - ${t.name}`}
                                                secondary={`Categoría: ${t.category} | Stock: ${t.stock}`}
                                            />
                                        </ListItemButton>
                                    </ListItem>
                                ))}
                            </List>
                        </Paper>
                    )}

                    {/* Resultados de Préstamos */}
                    {results.loans.length > 0 && (
                        <Paper sx={{ p: 2, borderRadius: 2 }}>
                            <Typography variant="h6" sx={{ color: '#4E7D10', mb: 1 }}>
                                Préstamos ({results.loans.length})
                            </Typography>
                            <Divider sx={{ mb: 1 }} />
                            <List>
                                {results.loans.map(l => (
                                    <ListItem key={l.idLoan} disablePadding>
                                        <ListItemButton onClick={() => navigate('/LoanPage')}>
                                            <ListItemText
                                                primary={`ID: ${l.idLoan} - Cliente: ${l.customer?.name || 'N/A'}`}
                                                secondary={`Estado: ${l.state} | Monto: $${l.rentalAmount}`}
                                            />
                                        </ListItemButton>
                                    </ListItem>
                                ))}
                            </List>
                        </Paper>
                    )}

                </Box>
            )}
        </Box>
    );
};

export default GlobalSearchPage;
