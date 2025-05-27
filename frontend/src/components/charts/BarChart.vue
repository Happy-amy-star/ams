<template>
  <div class="chart-container">
    <!-- 数据加载状态提示 -->
    <div v-if="loading" class="loading-overlay">
      <el-icon class="is-loading" :size="30">
        <Loading />
      </el-icon>
      <span class="loading-text">数据加载中...</span>
    </div>

    <!-- 空状态提示 -->
    <el-empty
        v-else-if="!hasData"
        description="暂无预算数据"
        :image-size="100"
        class="empty-tip"
    />

    <!-- 正常图表展示 -->
    <Bar v-else
         :data="chartData" :options="chartOptions" />
  </div>
</template>

<script lang="ts" setup>
import {
  computed,  // 添加缺失的导入
  ref
} from 'vue'
import {
  Chart as ChartJS,
  Title,
  Tooltip,
  Legend,
  BarElement,
  CategoryScale,
  PointElement,
  LinearScale
} from 'chart.js'
import { Loading } from '@element-plus/icons-vue'
import { Bar } from 'vue-chartjs'
ChartJS.register(CategoryScale, LinearScale, BarElement, Title, Tooltip, PointElement, Legend)

const props = defineProps<{
  data?: {
    labels: string[]
    datasets: {
      label: string
      backgroundColor: string
      data: number[]
    }[]
  }
  options?: {
    responsive?: boolean
    [key: string]: any
  }
  // 1. 添加 loading 类型声明
  loading?: boolean
}>()
// 2. 正确定义计算属性
const hasData = computed(() => {
  const targetData = props.data || chartData.value
  return targetData?.datasets?.some(dataset =>
      dataset?.data?.length > 0 &&
      dataset.data.some(value => value > 0)
  ) ?? false
})
// const chartData = props.data || {
const chartData = computed(() => props.data ?? {
  labels: [
    'January',
    'February',
    'March',
    'April',
    'May',
    'June',
    'July',
    'August',
    'September',
    'October',
    'November',
    'December'
  ],
  datasets: [
    {
      label: 'Sales 2022 (M)',
      backgroundColor: '#f87979',
      data: [40, 20, 12, 39, 10, 40, 39, 80, 40, 20, 12, 11]
    }
  ]
})

// const chartOptions = props.options || {
//   responsive: true
// }
const chartOptions = computed(() => props.options ?? { responsive: true })
</script>

<style lang="less" scoped>
.chart-container {
  position: relative;
  min-height: 400px;

  .loading-overlay {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 10px;
    z-index: 10;

    .loading-text {
      color: #666;
      font-size: 14px;
    }
  }

  .empty-tip {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    width: 100%;
  }

  // 保证图表容器有固定高度
  canvas {
    max-height: 600px;
    min-height: 400px;
  }
}
</style>
