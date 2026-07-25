import api from './api'

export const getBanner = () => {
  return api.get('/phim/banner')
}

export const getDangChieu = () => {
  return api.get('/phim/dang-chieu')
}

export const getSapChieu = () => {
  return api.get('/phim/sap-chieu')
}
