<template>
  <Pie :data="chartData" :options="chartOptions" />
</template>

<script lang="ts" setup>
import { Chart as ChartJS, ArcElement, Tooltip, Legend } from 'chart.js'
import { Pie } from 'vue-chartjs'
import {
  computed,  // 添加缺失的导入
} from 'vue'
ChartJS.register(ArcElement, Tooltip, Legend)

const props = defineProps<{
  data?: {
    title?: string
    labels: string[]
    datasets: {
      backgroundColor: string[]
      data: number[]
    }[]
  }
  options?: {
    responsive?: boolean
    [key: string]: any
  }
}>()

const chartData = computed(() => props.data ?? {
  title: 'Pie Chart',
  labels: ['VueJs', 'EmberJs', 'ReactJs', 'AngularJs'],
  datasets: [
    {
      backgroundColor: ['#41B883', '#E46651', '#00D8FF', '#DD1B16'],
      data: [40, 20, 80, 10]
    }
  ]
})
const chartOptions = computed(() => props.options ?? { responsive: true })

</script>

<style lang="less" scoped>
</style>
