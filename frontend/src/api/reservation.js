import request from '../utils/request'

export function getReservations() {
  return request.get('/reservations')
}

export function getReservationById(id) {
  return request.get(`/reservations/${id}`)
}

export function getReservationsByRoom(roomId) {
  return request.get(`/reservations/room/${roomId}`)
}

export function getReservationsByUser(userId) {
  return request.get(`/reservations/user/${userId}`)
}

export function getPendingReservations() {
  return request.get('/reservations/pending')
}

export function createReservation(data) {
  return request.post('/reservations', data)
}

export function approveReservation(id) {
  return request.put(`/reservations/${id}/approve`)
}

export function rejectReservation(id) {
  return request.put(`/reservations/${id}/reject`)
}

export function cancelReservation(id) {
  return request.put(`/reservations/${id}/cancel`)
}

export function deleteReservation(id) {
  return request.delete(`/reservations/${id}`)
}
