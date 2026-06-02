import axios from 'axios'

export const API_BASE = 'http://localhost:8080/api'

export const setupAuthInterceptor = (getToken) => {
  axios.interceptors.request.use((config) => {
    const token = getToken()

    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }

    return config
  })
}

export const getUserProfile = (token) => {
  return axios.get(`${API_BASE}/user/profile`, {
    headers: { Authorization: `Bearer ${token}` }
  })
}

export const getBanner = () => {
  return axios.get(`${API_BASE}/phim/banner`)
}

export const getDangChieu = () => {
  return axios.get(`${API_BASE}/phim/dang-chieu`)
}

export const getSapChieu = () => {
  return axios.get(`${API_BASE}/phim/sap-chieu`)
}