<template>
  <div id="assetPage" class="content-container">
    <h1>资产管理</h1>
    <div class="handle-box">
      <el-input
        v-model="query.name"
        placeholder="输入资产名称"
        class="handle-input mr10"
        @keyup.enter.native="handleSearch"
      ></el-input>
      <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
      <el-button type="primary" :icon="Plus" @click="handleAddWithFetch"> 新增 </el-button>
      <el-button type="warning" :icon="Setting" @click="showColumnSettings">列设置</el-button>
      <div class="toolbar-right" style="margin-left: auto; display: flex; gap: 10px; align-items: center;">
      <!-- 隐藏的文件输入，用于触发“导入” -->
      <input
          ref="fileInput"
          type="file"
          accept=".xls,.xlsx"
          style="display: none"
          @change="onFileChange"
      />
      <!-- 导入按钮 ：调用 triggerImport() -->
      <el-button type="primary" :icon="Upload" @click="triggerImport">导入</el-button>
      <!-- 导出按钮 -->
      <el-button type="primary" :icon="Download" @click="onExport">导出</el-button>
    </div>
    </div>
    <el-table
      :data="tableData"
      border
      class="table"
      ref="multipleTable"
      header-cell-class-name="table-header"
    >
      <el-table-column type="expand">
        <template #default="props">
          <div class="expand-content">
            <div class="expand-grid">
              <template v-for="field in expandFields" :key="field.value">
                <div class="expand-item">
                  <div class="expand-label">{{ field.label }}</div>
                  <div class="expand-value">
                    {{
                      field.value === 'purchasePrice'
                        ? '¥' + props.row[field.value]
                        : props.row[field.value]
                    }}
                  </div>
                </div>
              </template>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column
        type="index"
        label="序号"
        width="80"
        align="center"
        :index="(index:number) => index + 1 + (query.pageIndex - 1) * query.pageSize"
      ></el-table-column>
      <template v-for="field in tableFields" :key="field.value">
        <el-table-column
          :prop="field.value"
          :label="field.label"
          align="center"
          :width="field.value === 'count' ? 80 : undefined"
        ></el-table-column>
      </template>
      <el-table-column label="操作" width="300" align="center">
        <template #default="scope">
          <el-button
            text
            :icon="Edit"
            @click="handleEditWithFetch(scope.$index, scope.row)"
          >
            编辑
          </el-button>
          <el-button
            text
            :icon="Delete"
            class="red"
            @click="handleDelete(scope.$index)"
          >
            删除
          </el-button>
          <el-button
            text
            :icon="Memo"
            type="primary"
            @click="handleBorrow(scope.row)"
            :disabled="scope.row.status !== '正常'"
          >
            领用
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="pagination">
      <el-pagination
        background
        layout="total, sizes, prev, pager, next"
        :current-page="query.pageIndex"
        :page-size="query.pageSize"
        :total="pageTotal"
        :page-sizes="[10, 20, 50, 100]"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      ></el-pagination>
    </div>

    <!-- 使用组合对话框组件 -->
    <AssetDialog
      v-model:visible="dialogVisible"
      :mode="dialogMode"
      :asset-types="assetTypes"
      :departments="departments"
      :asset="currentAsset"
      @success="handleSuccess"
    />

    <!-- 使用领用对话框组件 -->
    <AssetBorrowDialog
      v-model:visible="borrowVisible"
      :departments="departments"
      :asset="currentAsset"
      @success="handleSuccess"
    />

    <!-- 列设置对话框 -->
    <ColumnSettings
      v-model:visible="columnSettingsVisible"
      :visibleColumns="visibleColumns"
      @updateVisibleColumns="updateVisibleColumns"
    />
  </div>
</template>

<script lang="ts" setup>
import { onMounted, ref, computed } from 'vue'
import { useAssetType } from '@/hooks/useAssetType'
import { useDepartment } from '@/hooks/useDepartment'
import { Delete, Edit, Search, Plus, Memo, Setting, Upload, Download} from '@element-plus/icons-vue'
import { useAsset } from '@/hooks/useAsset'
import AssetDialog from '@/components/asset/AssetDialog.vue'
import AssetBorrowDialog from '@/components/asset/AssetBorrowDialog.vue'
import ColumnSettings from '@/components/asset/ColumnSettings.vue'
// 新增导入/导出 API
import { exportAssetList, importAssetList } from '@/api/asset'
import { ElMessage } from 'element-plus'
// import {exportAssets, importAssetList, importAssets} from "@/api/asset";

const { assetTypes, fetchAssetTypes } = useAssetType()
const { departments, fetchDepartments } = useDepartment()
const {
  query,
  tableData,
  pageTotal,
  dialogVisible,
  dialogMode,
  currentAsset,
  borrowVisible,
  getData,
  handleSearch,
  handleSizeChange,
  handleCurrentChange,
  handleAdd,
  handleEdit,
  handleDelete,
  handleBorrow,
  handleSuccess
} = useAsset()
// 用于触发文件选择
const fileInput = ref<HTMLInputElement | null>(null)
// 新增：触发文件选择对话框
function triggerImport() {
  fileInput.value?.click()
}
// 定义所有字段
const allFields = [
  { value: 'name', label: '资产名称' },
  { value: 'code', label: '资产编号' },
  { value: 'sn', label: '资产序列号' },
  { value: 'type', label: '资产类型' },
  { value: 'model', label: '资产型号' },
  { value: 'config', label: '资产配置' },
  { value: 'ip', label: '资产IP' },
  { value: 'description', label: '资产描述' },
  { value: 'provider', label: '资产提供商' },
  { value: 'departmentName', label: '所属部门' },
  { value: 'ownerName', label: '负责人' },
  { value: 'userName', label: '使用人' },
  { value: 'location', label: '资产位置' },
  { value: 'status', label: '状态' },
  { value: 'useStatus', label: '使用状态' },
  { value: 'purchaseDate', label: '购入时间' },
  { value: 'purchasePrice', label: '购买价格' },
  { value: 'count', label: '数量' }
]

// 选中的列（表格显示字段）
const selectedColumns = ref<string[]>([])

// 计算表格显示的字段
const tableFields = computed(() => {
  return allFields.filter((field) => selectedColumns.value.includes(field.value))
})

// 计算展开行显示的字段
const expandFields = computed(() => {
  return allFields.filter((field) => !selectedColumns.value.includes(field.value))
})

// 列显示控制
const visibleColumns = ref({
  name: true,
  code: true,
  sn: true,
  type: true,
  model: true,
  config: false,
  ip: false,
  description: false,
  provider: false,
  departmentName: true,
  ownerName: true,
  userName: true,
  location: false,
  status: true,
  useStatus: true,
  purchaseDate: false,
  purchasePrice: false,
  count: true
})

// 列设置对话框
const columnSettingsVisible = ref(false)

const showColumnSettings = () => {
  // 初始化选中的列（表格中显示的字段）
  selectedColumns.value = Object.entries(visibleColumns.value)
    .filter(([_, value]) => value)
    .map(([key]) => key)
  columnSettingsVisible.value = true
}

// 更新列显示控制
const updateVisibleColumns = (newVisibleColumns: any) => {
  visibleColumns.value = newVisibleColumns
  // 更新选中的列（表格中显示的字段）
  selectedColumns.value = Object.entries(newVisibleColumns)
    .filter(([_, value]) => value)
    .map(([key]) => key)
}

onMounted(() => {
  const savedPageIndex = localStorage.getItem('AMSCurrentAssetPageIndex')
  if (savedPageIndex) {
    query.pageIndex = parseInt(savedPageIndex, 10)
  }
  // 初始化选中的列
  selectedColumns.value = Object.entries(visibleColumns.value)
    .filter(([_, value]) => value)
    .map(([key]) => key)
  getData()
  fetchDepartments()
})

// 新增操作
const handleAddWithFetch = async () => {
  await fetchAssetTypes()
  await fetchDepartments()
  await handleAdd()
}

// 编辑操作
const handleEditWithFetch = async (index: number, row: any) => {
  await fetchAssetTypes()
  await fetchDepartments()
  await handleEdit(index, row)
}

// —— 导出资产 ——
const onExport = async () => {
  try {
    const blob = await exportAssetList({
      pageIndex: query.pageIndex,
      pageSize: query.pageSize,
      name: query.name,
    });
    // console.log('收到 Blob：', blob, '大小：', blob.size, '类型：', blob.type);
    // 检查 blob 是否有效
    if (!blob) {
      ElMessage.error("导出失败：未获取到数据");
      return;
    }
    const url = window.URL.createObjectURL(blob);
    // const url = window.URL.createObjectURL(new Blob([blob]));
    const a = document.createElement('a');
    a.href = url;
    a.download = 'assets.xlsx';
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    window.URL.revokeObjectURL(url);
  } catch (err) {
    // 1) 在控制台输出整个错误对象
    console.error('导出时发生错误:', err);
    // 2) 继续给用户反馈
    ElMessage.error('导出失败，详情请看控制台');
  }
};

// —— 导入资产 ——
const onFileChange = async (e: Event) => {
  const files = (e.target as HTMLInputElement).files
  if (!files?.length) return
  try {
    const result = await importAssetList(files[0])
    // const result = res.data
    const { total, successCount, failureCount, errors } = result
    let msg = `共 ${total} 条，成功 ${successCount} 条`
    if (result.failureCount > 0) {
      msg += `，失败 ${failureCount} 条（详情见控制台）`
      ElMessage.warning(msg)
      console.table(errors)
    } else {
      ElMessage.success(msg)
    }
    // 导入完成后刷新列表
    await getData()
  } catch {
    ElMessage.error('导入失败')
  } finally {
    // 重置文件输入，使得同一文件可再次选择
    fileInput.value!.value = ''
  }

}




</script>

<style scoped>
.handle-box {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
}

.handle-select {
  width: 120px;
}

.handle-input {
  width: 300px;
}

.table {
  width: 100%;
  font-size: 14px;
}

.red {
  color: #f56c6c;
}

.mr10 {
  margin-right: 10px;
}

.table-td-avatar {
  display: block;
  margin: auto;
  width: 40px;
  height: 40px;
}

.expand-content {
  padding: 20px;
  background-color: #fafafa;
}

.expand-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 1px;
  background-color: #ebeef5;
  border: 1px solid #ebeef5;
  border-radius: 4px;
}

.expand-item {
  display: flex;
  width: calc(33.33% - 1px);
  background-color: #fff;
}

.expand-label {
  width: 120px;
  padding: 12px 15px;
  background-color: #f5f7fa;
  font-weight: bold;
  color: #606266;
  border-right: 1px solid #ebeef5;
}

.expand-value {
  flex: 1;
  padding: 12px 15px;
  color: #606266;
}

@media screen and (max-width: 1200px) {
  .expand-item {
    width: calc(50% - 1px);
  }
}

@media screen and (max-width: 768px) {
  .expand-item {
    width: 100%;
  }
}

</style>

