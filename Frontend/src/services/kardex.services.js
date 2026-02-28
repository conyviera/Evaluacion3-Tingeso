import httpClient from './http-common.js'; 

// Función que obtiene todos los movimientos del kardex
const getAllMove = async() => {
    return httpClient.get(`/kardex/getAllMove`)
}

//Función que obtiene todos los movimientos de una herramienta específica
const getAllMovementsOfTool = async(id) =>{
    return httpClient.get(`/kardex/getAllMovementsOfTool/${id}`)
}

const getAllKardexByDate = async(dateRange) => {
    return httpClient.get(`/kardex/getAllKardexByDate`, { params: dateRange });
}

export default{
    getAllMove,
    getAllMovementsOfTool,
    getAllKardexByDate
}
