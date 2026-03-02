import React from 'react';
import { Box, Typography } from '@mui/material';
import {
    BarChart,
    Bar,
    XAxis,
    YAxis,
    CartesianGrid,
    Tooltip,
    ResponsiveContainer,
    Cell,
} from 'recharts';

const BAR_COLORS = ['#4E7D10', '#6fa31a', '#90c930', '#b5e254', '#d4f07b', '#eaf7b0'];

const CustomTooltip = ({ active, payload, label }) => {
    if (active && payload && payload.length) {
        return (
            <Box
                sx={{
                    bgcolor: '#fff',
                    border: '1px solid #e0e0d8',
                    borderRadius: 2,
                    px: 1.5,
                    py: 1,
                    boxShadow: 2
                }}
            >
                <Typography variant="body2" sx={{ fontWeight: 700, color: '#4E7D10' }}>
                    {label}
                </Typography>
                <Typography variant="body2" sx={{ color: '#555' }}>
                    Préstamos: <strong>{payload[0].value}</strong>
                </Typography>
            </Box>
        );
    }
    return null;
};

const Grafic = ({ data = [] }) => {
    const chartData = data.map((item) => ({
        name: item.toolName,
        prestamos: item.usageCount,
    }));

    return (
        <Box
            sx={{
                width: '100%',
                height: '100%',
                display: 'flex',
                flexDirection: 'column',
                gap: 1,
                p: 2,
                bgcolor: '#ffffffff',
                borderRadius: 2,
                border: '1px solid #c4e09fff',
                boxSizing: 'border-box',
                boxShadow: '0px 2px 4px rgba(0, 0, 0, 0.1)'
            }}
        >
            <Typography
                variant="subtitle1"
                sx={{ fontWeight: 700, color: '#4E7D10', textAlign: 'center', flexShrink: 0 }}
            >
                Herramientas más prestadas
            </Typography>

            {chartData.length === 0 ? (
                <Box sx={{ flex: 1, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                    <Typography variant="body2" sx={{ color: '#888' }}>
                        Sin datos disponibles
                    </Typography>
                </Box>
            ) : (
                <Box sx={{ flex: 1, minHeight: 0 }}>
                    <ResponsiveContainer width="100%" height="100%">
                        <BarChart
                            data={chartData}
                            margin={{ top: 8, right: 8, left: -24, bottom: 40 }}
                            barSize={28}
                        >
                            <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#d4d4b8" />
                            <XAxis
                                dataKey="name"
                                tick={{ fontSize: 10, fill: '#555' }}
                                tickLine={false}
                                axisLine={false}
                                angle={-35}
                                textAnchor="end"
                                interval={0}
                            />
                            <YAxis
                                allowDecimals={false}
                                tick={{ fontSize: 10, fill: '#555' }}
                                tickLine={false}
                                axisLine={false}
                            />
                            <Tooltip content={<CustomTooltip />} cursor={{ fill: 'rgba(78,125,16,0.08)' }} />
                            <Bar dataKey="prestamos" radius={[4, 4, 0, 0]}>
                                {chartData.map((_, index) => (
                                    <Cell
                                        key={`cell-${index}`}
                                        fill={BAR_COLORS[index % BAR_COLORS.length]}
                                    />
                                ))}
                            </Bar>
                        </BarChart>
                    </ResponsiveContainer>
                </Box>
            )}
        </Box>
    );
};

export default Grafic;
