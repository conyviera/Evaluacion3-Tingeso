import httpClient from './http-common.js';

const createLoan = async (loan) => {
    return httpClient.post("/loans/createLoan", loan);
};

const returnLoan = async (id, data) => {
    return httpClient.put(`/loans/${id}/return`, data);

};

const getLoanById = (id) => {
    return httpClient.get(`/loans/${id}`);
};

const getAllLoans = async () => {
    return httpClient.get(`/loans/getAllLoans`);
}

const getDebtsByLoanId = async (idLoan) => {
    return httpClient.get(`/debts/loan/${idLoan}`);
};

const payDebt = async (idDebt) => {
    return httpClient.put(`/debts/pay/${idDebt}`, idDebt);
};

const rentalAmount = async (data) => {
    return await httpClient.post('/loans/RentalAmount', data);
}

const getTopTools = async () => {
    return httpClient.get("/loans/report");
};

const countActiveLoans = async () => {
    return httpClient.get("/loans/countLoans");
};

export default {
    createLoan,
    returnLoan,
    getLoanById,
    getAllLoans,
    getDebtsByLoanId,
    payDebt,
    rentalAmount,
    getTopTools,
    countActiveLoans
};

