import request from '../utils/request'

export function getRooms() {
  return request.get('/rooms')
}

export function getRoomById(id) {
  return request.get(`/rooms/${id}`)
}

export function createRoom(data) {
  return request.post('/rooms', data)
}

export function updateRoom(id, data) {
  return request.put(`/rooms/${id}`, data)
}

export function toggleRoom(id) {
  return request.put(`/rooms/${id}/toggle`)
}

export function deleteRoom(id) {
  return request.delete(`/rooms/${id}`)
}
