<template>
  <div class="room-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>会议室管理</span>
          <el-button type="primary" @click="openAddDialog">
            <el-icon><Plus /></el-icon>
            添加会议室
          </el-button>
        </div>
      </template>
      
      <el-table :data="rooms" stripe>
        <el-table-column prop="name" label="名称" width="150" />
        <el-table-column prop="location" label="位置" width="150" />
        <el-table-column label="开放时间" width="150">
          <template #default="{ row }">
            {{ row.openTimeStart }} - {{ row.openTimeEnd }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.disabled ? 'danger' : 'success'">
              {{ row.disabled ? '已禁用' : '可用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250">
          <template #default="{ row }">
            <el-button size="small" @click="openEditDialog(row)">编辑</el-button>
            <el-button 
              :type="row.disabled ? 'success' : 'warning'" 
              size="small"
              @click="handleToggle(row)"
            >
              {{ row.disabled ? '启用' : '禁用' }}
            </el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑会议室' : '添加会议室'" width="500px">
      <el-form :model="roomForm" label-width="100px">
        <el-form-item label="名称">
          <el-input v-model="roomForm.name" placeholder="请输入会议室名称" />
        </el-form-item>
        <el-form-item label="位置">
          <el-input v-model="roomForm.location" placeholder="请输入位置" />
        </el-form-item>
        <el-form-item label="开放时间">
          <el-col :span="11">
            <el-time-select
              v-model="roomForm.openTimeStart"
              placeholder="开始时间"
              start="06:00"
              step="00:30"
              end="22:00"
            />
          </el-col>
          <el-col :span="2" style="text-align: center">-</el-col>
          <el-col :span="11">
            <el-time-select
              v-model="roomForm.openTimeEnd"
              placeholder="结束时间"
              start="06:00"
              step="00:30"
              end="22:00"
              :min-time="roomForm.openTimeStart"
            />
          </el-col>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRooms, createRoom, updateRoom, toggleRoom, deleteRoom } from '../api/room'

const rooms = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const roomForm = ref({
  id: null,
  name: '',
  location: '',
  openTimeStart: '08:00',
  openTimeEnd: '18:00',
  disabled: false
})

const loadRooms = async () => {
  const res = await getRooms()
  rooms.value = res.data
}

const openAddDialog = () => {
  isEdit.value = false
  roomForm.value = {
    id: null,
    name: '',
    location: '',
    openTimeStart: '08:00',
    openTimeEnd: '18:00',
    disabled: false
  }
  dialogVisible.value = true
}

const openEditDialog = (room) => {
  isEdit.value = true
  roomForm.value = { ...room }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!roomForm.value.name) {
    ElMessage.warning('请输入会议室名称')
    return
  }
  if (!roomForm.value.location) {
    ElMessage.warning('请输入位置')
    return
  }
  
  try {
    if (isEdit.value) {
      await updateRoom(roomForm.value.id, roomForm.value)
      ElMessage.success('修改成功')
    } else {
      await createRoom(roomForm.value)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    loadRooms()
  } catch (e) {
    console.error(e)
  }
}

const handleToggle = async (room) => {
  try {
    await toggleRoom(room.id)
    ElMessage.success(room.disabled ? '已启用' : '已禁用')
    loadRooms()
  } catch (e) {
    console.error(e)
  }
}

const handleDelete = async (room) => {
  try {
    await ElMessageBox.confirm(`确定删除会议室"${room.name}"吗？`, '提示', {
      type: 'warning'
    })
    await deleteRoom(room.id)
    ElMessage.success('删除成功')
    loadRooms()
  } catch (e) {
    if (e !== 'cancel') {
      console.error(e)
    }
  }
}

onMounted(() => {
  loadRooms()
})
</script>

<style scoped>
.room-manage {
  height: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
