import axios from "axios";

const minimumCostApi = axios.create({
  baseURL: "http://localhost:8080/api/minimum-cost",
});

export default minimumCostApi;