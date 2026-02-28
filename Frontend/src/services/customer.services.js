import httpClient from './http-common.js';

const createCustomer = async (customer) => {
    return httpClient.post("/customers/registerCustomer", customer);
};

const getAllCustomers = async () => {
    return httpClient.get("/customers/getAll");
};

const countActiveCustomers = async () => {
    return httpClient.get("/customers/countActive");
};

const countCustomer = async () => {
    return httpClient.get("/customers/count");
};

export default {
    createCustomer, getAllCustomers, countActiveCustomers, countCustomer
};
