import httpClient from './http-common.js'; 


//Función para crear un lote de herramientas
const createLotTool = async (tool) => {
  return httpClient.post("/tool/register", tool);
};

//Función que trae todos los tipos de herramienta 
const getAllTypeTools = async () => {
  return httpClient.get("/type-tools/getAllTypeTools");
};

//Función que trae las categorias de herramientas disponibles 
const getAllcategory = async () => {
  return httpClient.get("/type-tools/getAllCategory");
}

//Función que trae todas las herramientas que comparten el mismo tipo
const getAllByTypeTool = async (idTypeTool) => {
  return httpClient.get( `/tool/findAllbyTypeTool/${idTypeTool}` );
}

//Función que trae una herramienta segun su id 
const getByIdTool = async (id) => {
  return httpClient.get(`/tool/findById/${id}`); 
}

//Función que cambia la tarifa diaria de la herramienta 
const configurationDailyRateTypeTool = async (idTypeTool, data) => {
  return httpClient.put(`/type-tools/configurationDailyRateTypeTool/${idTypeTool}`, {
    dailyRate: parseInt(data)
  })
}

//Función que cambia la multa por atraso de la herramienta
const configurationDebtTypeTool = async (idTypeTool, data) => {
  return httpClient.put(`/type-tools/configurationDebtTypeTool/${idTypeTool}`, {
    debtRate: parseInt(data)
  });
}

// Función que registra el valor de reposición de la herramienta
const registerReplacementTypeTool = async(idTypeTool, data) =>{
  return httpClient.put(`/type-tools/registerReplacementTypeTool/${idTypeTool}`, {
    replacementValue: parseInt(data)
  });
}

//Función que da de baja una herramienta en desuso
const deactivateUnusedTool = async (id)=> {
  return httpClient.put(`/tool/deactivateUnusedTool/${id}`)
}

//Función que obtiene un tipo de herramienta por su id
const getTypeToolById = async (id) => {
  return httpClient.get(`/type-tools/getById/${id}`);
}

const assessToolDamage = async (idDebts, data) => {
    return httpClient.put(`/debts/assessToolDamage/${idDebts}`, data);
};



export default {
  createLotTool,
  getAllTypeTools,
  getAllcategory,
  getAllByTypeTool,
  getTypeToolById,
  getByIdTool,
  configurationDailyRateTypeTool,
  configurationDebtTypeTool,
  registerReplacementTypeTool,
  deactivateUnusedTool,
  assessToolDamage
};

