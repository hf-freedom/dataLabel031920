<template>
  <el-container class="layout-container">
    <el-header class="header">
      <div class="logo">会议室预定系统</div>
      <el-dropdown @command="handleUserChange" trigger="click">
        <span class="user-dropdown">
          <el-icon><User /></el-icon>
          {{ currentUser?.name || '选择用户' }}
          <el-icon class="el-icon--right"><ArrowDown /></el-icon>
        </span>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item 
              v-for="user in users" 
              :key="user.id" 
              :command="user.id"
              :class="{ 'is-active': currentUser?.id === user.id }"
            >
              <el-icon v-if="user.role === 'ADMIN'"><Avatar /></el-icon>
              <el-icon v-else><User /></el-icon>
              {{ user.name }}
              <el-tag size="small" :type="user.role === 'ADMIN' ? 'danger' : 'info'" style="margin-left: 8px">
                {{ user.role === 'ADMIN' ? '管理员' : '普通用户' }}
              </el-tag>
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </el-header>
    <el-container>
      <el-aside width="200px" class="aside">
        <el-menu :default-active="activeMenu" router>
          <el-menu-item index="/">
            <el-icon><Calendar /></el-icon>
            <span>日历视图</span>
          </el-menu-item>
          <el-menu-item index="/my-reservations">
            <el-icon><Document /></el-icon>
            <span>我的预定</span>
          </el-menu-item>
          <el-menu-item v-if="isAdmin" index="/approval">
            <el-icon><Checked /></el-icon>
            <span>审批管理</span>
          </el-menu-item>
          <el-menu-item v-if="isAdmin" index="/room-manage">
            <el-icon><Setting /></el-icon>
            <span>会议室管理</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      <el-main class="main">
        <router-view :current-user="currentUser" :is-admin="isAdmin" />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted, provide } from 'vue'
import { useRoute } from 'vue-router'
import { getUsers } from './api/user'

const route = useRoute()
const users = ref([])
const currentUser = ref(null)

const activeMenu = computed(() => route.path)
const isAdmin = computed(() => currentUser.value?.role === 'ADMIN')

provide('currentUser', currentUser)
provide('isAdmin', isAdmin)

const handleUserChange = (userId) => {
  currentUser.value = users.value.find(u => u.id === userId)
  localStorage.setItem('currentUserId', userId)
}

onMounted(async () => {
  const res = await getUsers()
  users.value = res.data
  const savedUserId = localStorage.getItem('currentUserId')
  if (savedUserId) {
    currentUser.value = users.value.find(u => u.id === Number(savedUserId))
  }
})
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body, #app {
  height: 100%;
}

.layout-container {
  height: 100%;
}

.header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
}

.logo {
  color: white;
  font-size: 20px;
  font-weight: bold;
}

.user-dropdown {
  color: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 5px;
}

.aside {
  background: #fff;
  border-right: 1px solid #e6e6e6;
}

.main {
  background: #f5f7fa;
  padding: 20px;
}

.is-active {
  background-color: #ecf5ff;
}
</style>
