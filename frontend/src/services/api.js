import axios from "axios";

const api = axios.create({
  /*baseURL: "http://localhost:8080/api/sixteen-queens",*/
  baseURL: "http://localhost:8080/api/snake-ladder",
  
});



export default api;