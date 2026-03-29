<template>
  <div class="home-container">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="room-card">
          <template #header>
            <span>会议室列表</span>
          </template>
          <div 
            v-for="room in rooms" 
            :key="room.id" 
            class="room-item"
            :class="{ 
              active: selectedRoom?.id === room.id,
              disabled: room.disabled 
            }"
            @click="selectRoom(room)"
          >
            <div class="room-name">{{ room.name }}</div>
            <div class="room-info">
              <span>{{ room.location }}</span>
              <el-tag v-if="room.disabled" type="danger" size="small">已禁用</el-tag>
            </div>
            <div class="room-time">{{ room.openTimeStart }} - {{ room.openTimeEnd }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="18">
        <el-card>
          <template #header>
            <div class="calendar-header">
              <span>{{ selectedRoom?.name || '请选择会议室' }} - 日历视图</span>
              <div class="header-actions">
                <el-date-picker
                  v-model="selectedDate"
                  type="date"
                  placeholder="选择日期"
                  format="YYYY-MM-DD"
                  value-format="YYYY-MM-DD"
                  @change="loadReservations"
                />
                <el-button 
                  type="primary" 
                  @click="openReserveDialog"
                  :disabled="!selectedRoom || selectedRoom?.disabled || !currentUser"
                >
                  <el-icon><Plus /></el-icon>
                  预定
                </el-button>
              </div>
            </div>
          </template>
          
          <div v-if="!selectedRoom" class="empty-tip">
            <el-empty description="请从左侧选择一个会议室" />
          </div>
          
          <div v-else class="calendar-view">
            <div class="time-grid">
              <div class="time-label" v-for="hour in timeSlots" :key="hour">
                {{ hour }}:00
              </div>
            </div>
            <div class="reservation-grid">
              <div 
                v-for="hour in timeSlots" 
                :key="hour" 
                class="time-cell"
                :class="{ 
                  'in-open-time': isInOpenTime(hour),
                  'has-reservation': hasReservationAt(hour)
                }"
              >
                <div 
                  v-for="(res, index) in getReservationsAt(hour)" 
                  :key="res.id"
                  class="reservation-block"
                  :class="getStatusClass(res.status)"
                  :style="getBlockStyle(res, index)"
                  @click="showReservationDetail(res)"
                >
                  <div class="res-title">{{ res.userName }}</div>
                  <div class="res-time">{{ formatTime(res.startTime) }} - {{ formatTime(res.endTime) }}</div>
                </div>
              </div>
            </div>
          </div>
          
          <div class="legend">
            <span class="legend-item">
              <span class="legend-color pending"></span>待审批
            </span>
            <span class="legend-item">
              <span class="legend-color approved"></span>已通过
            </span>
            <span class="legend-item">
              <span class="legend-color rejected"></span>已驳回
            </span>
            <span class="legend-item">
              <span class="legend-color cancelled"></span>已取消
            </span>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="reserveDialogVisible" title="预定会议室" width="500px">
      <el-form :model="reserveForm" label-width="100px">
        <el-form-item label="会议室">
          <el-input :value="selectedRoom?.name" disabled />
        </el-form-item>
        <el-form-item label="预定日期">
          <el-input :value="selectedDate" disabled />
        </el-form-item>
        <el-form-item label="开始时间">
          <el-select v-model="reserveForm.startTime" placeholder="选择开始时间">
            <el-option 
              v-for="time in availableStartTimes" 
              :key="time" 
              :label="time" 
              :value="time"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="结束时间">
          <el-select v-model="reserveForm.endTime" placeholder="选择结束时间">
            <el-option 
              v-for="time in availableEndTimes" 
              :key="time" 
              :label="time" 
              :value="time"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="预定目的">
          <el-input v-model="reserveForm.purpose" type="textarea" rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reserveDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReservation">提交预定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailDialogVisible" title="预定详情" width="400px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="预定人">{{ currentDetail?.userName }}</el-descriptions-item>
        <el-descriptions-item label="会议室">{{ currentDetail?.roomName }}</el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ formatDateTime(currentDetail?.startTime) }}</el-descriptions-item>
        <el-descriptions-item label="结束时间">{{ formatDateTime(currentDetail?.endTime) }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusTagType(currentDetail?.status)">
            {{ getStatusText(currentDetail?.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="预定目的">{{ currentDetail?.purpose || '无' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, inject } from 'vue'
import { ElMessage } from 'element-plus'
import { getRooms } from '../api/room'
import { getReservationsByRoom, createReservation } from '../api/reservation'

const props = defineProps({
  currentUser: Object,
  isAdmin: Boolean
})

const rooms = ref([])
const selectedRoom = ref(null)
const selectedDate = ref(new Date().toISOString().split('T')[0])
const reservations = ref([])
const reserveDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const currentDetail = ref(null)

const reserveForm = ref({
  startTime: '',
  endTime: '',
  purpose: ''
})

const timeSlots = Array.from({ length: 24 }, (_, i) => i)

const isInOpenTime = (hour) => {
  if (!selectedRoom.value) return false
  const start = parseInt(selectedRoom.value.openTimeStart.split(':')[0])
  const end = parseInt(selectedRoom.value.openTimeEnd.split(':')[0])
  return hour >= start && hour < end
}

const hasReservationAt = (hour) => {
  return reservations.value.some(r => {
    const startHour = new Date(r.startTime).getHours()
    const endHour = new Date(r.endTime).getHours()
    const endMin = new Date(r.endTime).getMinutes()
    const realEndHour = endMin > 0 ? endHour : endHour - 1
    return hour >= startHour && hour <= realEndHour
  })
}

const getReservationsAt = (hour) => {
  return reservations.value.filter(r => {
    const startHour = new Date(r.startTime).getHours()
    const endHour = new Date(r.endTime).getHours()
    const endMin = new Date(r.endTime).getMinutes()
    const realEndHour = endMin > 0 ? endHour : endHour - 1
    return hour >= startHour && hour <= realEndHour
  })
}

const getBlockStyle = (res, index) => {
  const startHour = new Date(res.startTime).getHours()
  const endHour = new Date(res.endTime).getHours()
  const endMin = new Date(res.endTime).getMinutes()
  const realEndHour = endMin > 0 ? endHour : endHour - 1
  const span = realEndHour - startHour + 1
  
  return {
    gridColumn: `${startHour + 2} / span ${span}`,
    marginTop: index > 0 ? '5px' : '0'
  }
}

const getStatusClass = (status) => {
  const map = {
    PENDING: 'pending',
    APPROVED: 'approved',
    REJECTED: 'rejected',
    CANCELLED: 'cancelled'
  }
  return map[status] || ''
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

const formatTime = (datetime) => {
  if (!datetime) return ''
  const d = new Date(datetime)
  return `${d.getHours().toString().padStart(2, '0')}:${d.getMinutes().toString().padStart(2, '0')}`
}

const formatDateTime = (datetime) => {
  if (!datetime) return ''
  return datetime.replace('T', ' ')
}

const selectRoom = (room) => {
  if (room.disabled) {
    ElMessage.warning('该会议室已被禁用')
    return
  }
  selectedRoom.value = room
  loadReservations()
}

const loadReservations = async () => {
  if (!selectedRoom.value) return
  const res = await getReservationsByRoom(selectedRoom.value.id)
  const dateStr = selectedDate.value
  reservations.value = res.data.filter(r => 
    r.startTime.startsWith(dateStr) && r.status !== 'CANCELLED'
  )
}

const availableStartTimes = computed(() => {
  if (!selectedRoom.value) return []
  const times = []
  const start = parseInt(selectedRoom.value.openTimeStart.split(':')[0])
  const end = parseInt(selectedRoom.value.openTimeEnd.split(':')[0])
  for (let i = start; i < end; i++) {
    times.push(`${i.toString().padStart(2, '0')}:00`)
  }
  return times
})

const availableEndTimes = computed(() => {
  if (!selectedRoom.value || !reserveForm.value.startTime) return []
  const times = []
  const startHour = parseInt(reserveForm.value.startTime.split(':')[0])
  const end = parseInt(selectedRoom.value.openTimeEnd.split(':')[0])
  for (let i = startHour + 1; i <= end; i++) {
    times.push(`${i.toString().padStart(2, '0')}:00`)
  }
  return times
})

const openReserveDialog = () => {
  if (!props.currentUser) {
    ElMessage.warning('请先选择用户')
    return
  }
  reserveForm.value = {
    startTime: '',
    endTime: '',
    purpose: ''
  }
  reserveDialogVisible.value = true
}

const submitReservation = async () => {
  if (!reserveForm.value.startTime || !reserveForm.value.endTime) {
    ElMessage.warning('请选择开始和结束时间')
    return
  }
  
  const startTime = `${selectedDate.value}T${reserveForm.value.startTime}:00`
  const endTime = `${selectedDate.value}T${reserveForm.value.endTime}:00`
  
  try {
    await createReservation({
      roomId: selectedRoom.value.id,
      userId: props.currentUser.id,
      startTime,
      endTime,
      purpose: reserveForm.value.purpose
    })
    ElMessage.success('预定提交成功')
    reserveDialogVisible.value = false
    loadReservations()
  } catch (e) {
    console.error(e)
  }
}

const showReservationDetail = (res) => {
  currentDetail.value = res
  detailDialogVisible.value = true
}

onMounted(async () => {
  const res = await getRooms()
  rooms.value = res.data
})
</script>

<style scoped>
.home-container {
  height: 100%;
}

.room-card {
  height: calc(100vh - 140px);
  overflow-y: auto;
}

.room-item {
  padding: 12px;
  border-radius: 8px;
  cursor: pointer;
  margin-bottom: 10px;
  border: 1px solid #e6e6e6;
  transition: all 0.3s;
}

.room-item:hover {
  border-color: #409eff;
}

.room-item.active {
  border-color: #409eff;
  background: #ecf5ff;
}

.room-item.disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.room-name {
  font-weight: bold;
  margin-bottom: 5px;
}

.room-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #666;
}

.room-time {
  font-size: 12px;
  color: #999;
  margin-top: 5px;
}

.calendar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.empty-tip {
  padding: 50px 0;
}

.calendar-view {
  display: flex;
  min-height: 600px;
}

.time-grid {
  width: 80px;
  flex-shrink: 0;
}

.time-label {
  height: 60px;
  line-height: 60px;
  text-align: center;
  border-bottom: 1px solid #e6e6e6;
  font-size: 12px;
  color: #666;
}

.reservation-grid {
  flex: 1;
  position: relative;
}

.time-cell {
  height: 60px;
  border-bottom: 1px solid #e6e6e6;
  border-left: 1px solid #e6e6e6;
  background: #fafafa;
}

.time-cell.in-open-time {
  background: #fff;
}

.reservation-block {
  padding: 5px 10px;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
  margin: 5px;
}

.reservation-block.pending {
  background: #fdf6ec;
  border-left: 3px solid #e6a23c;
}

.reservation-block.approved {
  background: #f0f9eb;
  border-left: 3px solid #67c23a;
}

.reservation-block.rejected {
  background: #fef0f0;
  border-left: 3px solid #f56c6c;
}

.reservation-block.cancelled {
  background: #f4f4f5;
  border-left: 3px solid #909399;
}

.res-title {
  font-weight: bold;
}

.res-time {
  color: #666;
}

.legend {
  display: flex;
  gap: 20px;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #e6e6e6;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
}

.legend-color {
  width: 20px;
  height: 12px;
  border-radius: 2px;
}

.legend-color.pending {
  background: #e6a23c;
}

.legend-color.approved {
  background: #67c23a;
}

.legend-color.rejected {
  background: #f56c6c;
}

.legend-color.cancelled {
  background: #909399;
}
</style>
