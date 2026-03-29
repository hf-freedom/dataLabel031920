import request from '../utils/request'

export function getUsers() {
  return request.get('/users')
}

export function getUserById(id) {
  return request.get(`/users/${id}`)
}
