import { useState } from 'react';

/**
 * Hook de paginación reutilizable.
 * @param {Array} items - Lista completa de elementos a paginar.
 * @param {number} itemsPerPage - Cantidad de elementos por página (default: 10).
 * @returns currentPage, totalPages, currentItems, handlePageChange, resetPage
 */
export function usePagination(items, defaultItemsPerPage = 10) {
    const [currentPage, setCurrentPage] = useState(1);
    const [itemsPerPage, setItemsPerPage] = useState(defaultItemsPerPage);

    const totalPages = Math.max(1, Math.ceil((items?.length ?? 0) / itemsPerPage));

    const indexOfLast = currentPage * itemsPerPage;
    const indexOfFirst = indexOfLast - itemsPerPage;
    const currentItems = (items ?? []).slice(indexOfFirst, indexOfLast);

    const handlePageChange = (_event, value) => setCurrentPage(value);

    const handleItemsPerPageChange = (event) => {
        setItemsPerPage(Number(event.target.value));
        setCurrentPage(1); // Reset to first page when changing page size
    };

    const resetPage = () => setCurrentPage(1);

    return {
        currentPage,
        totalPages,
        currentItems,
        itemsPerPage,
        handlePageChange,
        handleItemsPerPageChange,
        resetPage
    };
}
