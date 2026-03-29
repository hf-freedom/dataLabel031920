import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { 
    path: '/', 
    name: 'Home', 
    component: () => import('../views/Home.vue') 
  },
  { 
    path: '/approval', 
    name: 'Approval', 
    component: () => import('../views/Approval.vue') 
  },
  { 
    path: '/my-reservations', 
    name: 'MyReservations', 
    component: () => import('../views/MyReservations.vue') 
  },
  { 
    path: '/room-manage', 
    name: 'RoomManage', 
    component: () => import('../views/RoomManage.vue') 
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
