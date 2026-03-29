<template>
  <div class="my-reservations">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>我的预定</span>
          <el-button @click="loadReservations">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </template>
      
      <el-table :data="reservations" stripe>
        <el-table-column prop="roomName" label="会议室" width="120" />
        <el-table-column label="开始时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.startTime) }}
          </template>
        </el-table-column>
        <el-table-column label="结束时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.endTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="purpose" label="预定目的" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button 
              v-if="canCancel(row.status)"
              type="danger" 
              size="small"
              @click="handleCancel(row)"
            >
              取消
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getReservationsByUser, cancelReservation } from '../api/reservation'

const props = defineProps({
  currentUser: Object
})

const reservations = ref([])

const formatDateTime = (datetime) => {
  if (!datetime) return ''
  return datetime.replace('T', ' ')
}

const getStatusTagType = (status) => {
  const map = {
    PENDING: 'warning',
    APPROVED: 'success',
    REJECTED: 'danger',
    CANCELLED: 'info'
  }
  return map[status] || ''
}

const getStatusText = (status) => {
  const map = {
    PENDING: '待审批',
    APPROVED: '已通过',
    REJECTED: '已驳回',
    CANCELLED: '已取消'
  }
  return map[status] || status
}

const canCancel = (status) => {
  return status === 'PENDING' || status === 'APPROVED'
}

const loadReservations = async () => {
  if (!props.currentUser) return
  const res = await getReservationsByUser(props.currentUser.id)
  reservations.value = res.data.sort((a, b) => 
    new Date(b.createTime) - new Date(a.createTime)
  )
}

const handleCancel = async (row) => {
  try {
    await ElMessageBox.confirm('确定要取消该预定吗？', '提示', {
      type: 'warning'
    })
    await cancelReservation(row.id)
    ElMessage.success('取消成功')
    loadReservations()
  } catch (e) {
    if (e !== 'cancel') {
      console.error(e)
    }
  }
}

watch(() => props.currentUser, () => {
  loadReservations()
}, { immediate: true })

onMounted(() => {
  loadReservations()
})
</script>

<style scoped>
.my-reservations {
  height: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
