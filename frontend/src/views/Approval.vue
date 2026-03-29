<template>
  <div class="approval-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>待审批列表</span>
          <el-button @click="loadPending">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </template>
      
      <el-table :data="pendingList" stripe>
        <el-table-column prop="userName" label="申请人" width="100" />
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
        <el-table-column label="申请时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button type="success" size="small" @click="handleApprove(row)">
              通过
            </el-button>
            <el-button type="danger" size="small" @click="handleReject(row)">
              驳回
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <el-empty v-if="pendingList.length === 0" description="暂无待审批的预定" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPendingReservations, approveReservation, rejectReservation } from '../api/reservation'

const pendingList = ref([])

const formatDateTime = (datetime) => {
  if (!datetime) return ''
  return datetime.replace('T', ' ')
}

const loadPending = async () => {
  const res = await getPendingReservations()
  pendingList.value = res.data.sort((a, b) => 
    new Date(a.startTime) - new Date(b.startTime)
  )
}

const handleApprove = async (row) => {
  try {
    await ElMessageBox.confirm('确定通过该预定申请吗？', '提示', {
      type: 'success'
    })
    await approveReservation(row.id)
    ElMessage.success('审批通过')
    loadPending()
  } catch (e) {
    if (e !== 'cancel') {
      console.error(e)
    }
  }
}

const handleReject = async (row) => {
  try {
    await ElMessageBox.confirm('确定驳回该预定申请吗？', '提示', {
      type: 'warning'
    })
    await rejectReservation(row.id)
    ElMessage.success('已驳回')
    loadPending()
  } catch (e) {
    if (e !== 'cancel') {
      console.error(e)
    }
  }
}

onMounted(() => {
  loadPending()
})
</script>

<style scoped>
.approval-page {
  height: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
