import axios from "axios";

const apiServer = axios.create({
    baseURL: "localhost:8080",
    timeout: 5000,
})