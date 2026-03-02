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

const getTopToolsByDeliveryDateBetween = async (startDate, endDate) => {
    return httpClient.get("/loans/reportDate", { params: { startDate, endDate } });
};

const countActiveLoans = async () => {
    return httpClient.get("/loans/countLoansActive");
};

const countActiveLoansByDeliveryDateBetween = async (startDate, endDate) => {
    return httpClient.get("/loans/countByDeliveryDateBetweenActive", { params: { startDate, endDate } });
};

const countExpiredLoansByDeliveryDateBetween = async (startDate, endDate) => {
    return httpClient.get("/loans/countByDeliveryDateBetweenExpired", { params: { startDate, endDate } });
};

const countExpiredLoans = async () => {
    return httpClient.get("/loans/countLoansExpired");
};

const loanActiveAndExpireFilterDate = async (startDate, endDate) => {
    return httpClient.get("/loans/loanActiveAndExpireFilterDate", { params: { startDate, endDate } });
}

export default {
    createLoan,
    returnLoan,
    getLoanById,
    getAllLoans,
    getDebtsByLoanId,
    payDebt,
    rentalAmount,
    getTopTools,
    getTopToolsByDeliveryDateBetween,
    countActiveLoans,
    countActiveLoansByDeliveryDateBetween,
    countExpiredLoansByDeliveryDateBetween,
    countExpiredLoans,
    loanActiveAndExpireFilterDate
};

