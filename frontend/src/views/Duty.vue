<template>
  <div id="dutyPage" class="content-container">
    <h1>值班表</h1>
    <!-- <el-calendar ref="calendar">
      <template #header="{ date }">
        <span>值班表</span>
        <span>{{ date }}</span>
        <el-button-group>
          <el-button size="small" @click="selectDate('prev-year')"> 去年 </el-button>
          <el-button size="small" @click="selectDate('prev-month')"> 上月 </el-button>
          <el-button size="small" @click="selectDate('today')">今天</el-button>
          <el-button size="small" @click="selectDate('next-month')"> 下月 </el-button>
          <el-button size="small" @click="selectDate('next-year')"> 来年 </el-button>
        </el-button-group>
      </template>
    </el-calendar> -->
    <el-calendar>
      <template #date-cell="{ data }">
        <p :class="data.isSelected ? 'is-selected' : ''">
          {{ data.day.split('-').slice(1).join('/') }}
          <br />
          {{ getDutyInfo(data.day)?.day || '' }}
          <br />
          {{ getDutyInfo(data.day)?.core || '无值班信息' }}
          {{ getDutyInfo(data.day)?.tel || '无值班信息' }}
        </p>
      </template>
    </el-calendar>
  </div>
</template>

<script lang="ts" setup>
import { ref, computed } from 'vue'
import type { CalendarDateType, CalendarInstance } from 'element-plus'
const dutyData = ref([
  { date: '2025-04-01', day: '国庆节', core: '张三', tel: '123456789' },
  { date: '2025-04-02', day: '国庆节', core: '李四', tel: '987654321' },
  { date: '2025-04-03', day: '国庆节', core: '王五', tel: '456789123' },
  { date: '2025-04-04', day: '国庆节', core: '赵六', tel: '321654987' },
  { date: '2025-04-05', day: '国庆节', core: '钱七', tel: '654321789' },
  { date: '2025-04-06', day: '国庆节', core: '孙八', tel: '789123456' },
  { date: '2025-04-07', day: '国庆节', core: '周九', tel: '159753486' }
])
const getDutyInfo = (date: string) => {
  return dutyData.value.find((item) => item.date === date)
}

const selectedDate = ref(new Date())
const isSpecialDate = (date: Date) => {
  const specialDates = [3, 15, 20] // 特定日期数组
  return specialDates.includes(date.getDate())
}
const isWeekend = (date: Date) => {
  const day = date.getDay()
  return day === 0 || day === 6 // 周末
}
const isToday = (date: Date) => {
  const today = new Date()
  return (
    date.getDate() === today.getDate() &&
    date.getMonth() === today.getMonth() &&
    date.getFullYear() === today.getFullYear()
  )
}

const calendar = ref<CalendarInstance>()
const selectDate = (val: CalendarDateType) => {
  if (!calendar.value) return
  calendar.value.selectDate(val)
}
</script>
<style lang="less" scoped>
.is-selected {
  color: #1989fa;
}
</style>
