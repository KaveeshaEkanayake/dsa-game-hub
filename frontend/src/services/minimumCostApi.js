import axios from "axios";

const API_URL = import.meta.env.VITE_API_URL || "http://localhost:8080";

const minimumCostApi = axios.create({
  baseURL: `${API_URL}/api/minimum-cost`,
});

export default minimumCostApi;