<template>
  <footer class="footer">
    <div class="footer-content">
      <div class="footer-links">
        <a href="https://github.com/geekhall" target="_blank" class="footer-link">
          <el-icon
            ><svg
              t="1675595403992"
              class="icon"
              viewBox="0 0 1024 1024"
              version="1.1"
              xmlns="http://www.w3.org/2000/svg"
              p-id="2674"
              width="20"
              height="20"
            >
              <path
                d="M511.957333 21.333333C241.024 21.333333 21.333333 240.981333 21.333333 512c0 216.832 140.544 400.725333 335.573334 465.664 24.490667 4.394667 32.256-10.069333 32.256-23.082667 0-11.690667 0.256-44.245333 0-85.205333-136.448 29.610667-164.736-64.64-164.736-64.64-22.314667-56.704-54.4-71.765333-54.4-71.765333-44.586667-30.464 3.285333-29.824 3.285333-29.824 49.194667 3.413333 75.178667 50.517333 75.178667 50.517333 43.776 75.008 114.816 53.333333 142.762666 40.789333 4.522667-31.658667 17.152-53.376 31.189334-65.536-108.970667-12.458667-223.488-54.485333-223.488-242.602666 0-53.546667 19.114667-97.322667 50.517333-131.669334-5.034667-12.330667-21.930667-62.293333 4.778667-129.834666 0 0 41.258667-13.184 134.912 50.346666a469.802667 469.802667 0 0 1 122.88-16.554666c41.642667 0.213333 83.626667 5.632 122.88 16.554666 93.653333-63.488 134.784-50.346667 134.784-50.346666 26.752 67.541333 9.898667 117.504 4.864 129.834666 31.402667 34.346667 50.474667 78.122667 50.474666 131.669334 0 188.586667-114.730667 230.016-224.042666 242.090666 17.578667 15.232 33.578667 44.672 33.578666 90.453334v135.850666c0 13.141333 7.936 27.605333 32.853334 22.869334C862.250667 912.597333 1002.666667 728.746667 1002.666667 512 1002.666667 240.981333 783.018667 21.333333 511.957333 21.333333z"
                p-id="2675"
              ></path></svg
          ></el-icon>
          <span>GitHub</span>
        </a>
        <button class="theme-toggle" @click="toggleDark()">
          <el-icon v-if="isDark"><Moon /></el-icon>
          <el-icon v-else><Sunny /></el-icon>
        </button>
      </div>
      <div class="footer-copyright">
        <span>© 2024 AMS资产管理系统</span>
        <span class="divider">|</span>
        <span>Powered by GeekHall</span>
      </div>
    </div>
  </footer>
</template>

<script lang="ts" setup>
import { isDark, toggleDark } from '~/composables'
import { Moon, Sunny } from '@element-plus/icons-vue'
</script>

<style lang="less" scoped>
.footer {
  padding: 20px;
  background: #fff;
  border-top: 1px solid #ebeef5;
  margin-top: auto;
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.footer-links {
  display: flex;
  align-items: center;
  gap: 20px;
}

.footer-link {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #606266;
  text-decoration: none;
  transition: all 0.3s;

  &:hover {
    color: #409eff;
    transform: translateY(-2px);
  }

  .icon {
    width: 20px;
    height: 20px;
  }
}

.theme-toggle {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  border: 1px solid #dcdfe6;
  background: transparent;
  cursor: pointer;
  transition: all 0.3s;

  &:hover {
    background: #f5f7fa;
    border-color: #409eff;
    color: #409eff;
  }

  .el-icon {
    font-size: 18px;
  }
}

.footer-copyright {
  color: #909399;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 8px;

  .divider {
    color: #dcdfe6;
  }
}
</style>
