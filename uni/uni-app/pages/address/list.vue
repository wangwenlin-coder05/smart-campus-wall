<template>
  <view class="page">
    <scroll-view class="address-list" scroll-y>
      <view
        class="address-item-wrapper"
        v-for="addr in addressList"
        :key="addr.addressNo"
      >
        <!-- 滑动卡片主体 -->
        <view
          class="address-item"
          :class="{
            'select-mode': isSelectMode,
            'show-delete': activeDeleteId === addr.addressNo
          }"
          @touchstart="onTouchStart($event, addr.addressNo)"
          @touchmove="onTouchMove($event, addr.addressNo)"
          @touchend="onTouchEnd($event, addr.addressNo)"
          @click="handleAddressClick(addr)"
        >
          <view class="addr-info">
            <view class="addr-text">{{ addr.address }}</view>
            <view class="contact">
              <text class="name">{{ addr.receiverName }}</text>
              <text class="divider">｜</text>
              <text class="phone">{{ addr.receiverPhone }}</text>
            </view>
          </view>

          <view class="addr-right">
            <view
              class="type-tag"
              :class="addr.addressType === 1 ? 'school' : 'outer'"
            >
              {{ addr.addressType === 1 ? '校内' : '校外' }}
            </view>
            <view v-if="addr.isDefault === 1" class="default-tag">默认</view>
            <view v-else class="default-btn" @click.stop="setDefault(addr)">设默认</view>
            <!-- 非选择模式下显示编辑图标 -->
            <view
              class="edit-icon"
              @click.stop="goToEdit(addr)"
            ></view>
          </view>
        </view>

        <!-- 删除按钮（隐藏在右侧） -->
        <view
          class="delete-btn"
          @click.stop="confirmDelete(addr)"
        >删除</view>
      </view>

      <view class="empty" v-if="addressList.length === 0"
        >暂无地址，点击下方添加</view
      >
      <view class="bottom-placeholder"></view>
    </scroll-view>

    <!-- 底部添加按钮 -->
    <view class="add-btn" @click="goToAdd">＋ 添加新地址</view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import request from '@/utils/request.js'
import { getCurrentUser } from '@/utils/auth.js'

const isSelectMode = ref(false)
const addressList = ref([])
const currentUid = ref('')
const isNavigating = ref(false)

const syncCurrentUser = () => {
  const user = getCurrentUser()
  if (!user || !user.uid) {
    if (!isNavigating.value) {
      isNavigating.value = true
      uni.navigateTo({ url: '/pages/login/login' })
    }
    return false
  }
  currentUid.value = user.uid
  return true
}

// ---------- 滑动删除相关 ----------
const activeDeleteId = ref(null)     // 当前展开删除按钮的地址ID
const touchStartX = ref(0)
const touchMoveX = ref(0)
const swipeThreshold = 60           // 触发删除按钮显示的滑动距离（rpx）

// 触摸开始
const onTouchStart = (e, id) => {
  touchStartX.value = e.touches[0].clientX
  touchMoveX.value = 0
}

// 触摸移动
const onTouchMove = (e, id) => {
  const moveX = e.touches[0].clientX - touchStartX.value
  touchMoveX.value = moveX
  // 向左滑动超过阈值，显示删除按钮
  if (moveX < -swipeThreshold) {
    activeDeleteId.value = id
  } else if (moveX > swipeThreshold) {
    // 向右滑动则关闭
    activeDeleteId.value = null
  }
}

// 触摸结束
const onTouchEnd = (e, id) => {
  // 如果滑动距离不够，且之前没有展开，则不做操作
  if (Math.abs(touchMoveX.value) < swipeThreshold) {
    // 如果没有滑动，保持原状态（点击卡片逻辑由@click处理）
  }
}

// 获取地址列表
const fetchAddressList = async () => {
  if (!syncCurrentUser()) return
  try {
    const res = await request({
      url: `/address/listByUid/${currentUid.value}`,
      method: 'GET'
    })
    if (res.code === 1) {
      addressList.value = res.data || []
    } else {
      uni.showToast({ title: res.msg || '加载失败', icon: 'none' })
    }
  } catch (err) {
    console.error('获取地址列表异常', err)
    uni.showToast({ title: '网络异常，请稍后重试', icon: 'none' })
  }
}

// 确认删除
const confirmDelete = (addr) => {
  uni.showModal({
    title: '删除地址',
    content: `确定删除地址“${addr.address}”吗？`,
    success: async (res) => {
      if (res.confirm) {
        try {
          const res = await request({
            url: `/address/deleteByNo/${currentUid.value}/${addr.addressNo}`,
            method: 'DELETE'
          })
          if (res.code === 1) {
            uni.showToast({ title: '删除成功', icon: 'success' })
            activeDeleteId.value = null
            fetchAddressList() // 刷新列表
          } else {
            uni.showToast({ title: res.msg || '删除失败', icon: 'none' })
          }
        } catch (e) {
          uni.showToast({ title: '网络异常', icon: 'none' })
        }
      }
    }
  })
}

const setDefault = async (addr) => {
  if (!syncCurrentUser()) return
  const res = await request({
    url: `/address/defaultByNo/${currentUid.value}/${addr.addressNo}`,
    method: 'PUT'
  })
  if (res.code === 1) {
    uni.showToast({ title: '已设为默认', icon: 'none' })
    fetchAddressList()
  } else {
    uni.showToast({ title: res.msg || '设置失败', icon: 'none' })
  }
}

// 点击卡片
const handleAddressClick = (addr) => {
  if (activeDeleteId.value === addr.addressNo) {
    // 如果删除按钮已展开，则收起
    activeDeleteId.value = null
    return
  }
  if (isSelectMode.value) {
    uni.$emit('addressSelected', addr)
    uni.navigateBack()
  } else {
    goToEdit(addr)
  }
}

const goToAdd = () => {
  uni.navigateTo({ url: '/pages/address/edit' })
}

const goToEdit = (addr) => {
  uni.navigateTo({ url: `/pages/address/edit?addressNo=${addr.addressNo}` })
}

onShow(() => {
  fetchAddressList()
})

onMounted(() => {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  if (currentPage?.options?.mode === 'select') {
    isSelectMode.value = true
  }
  fetchAddressList()
})
</script>

<style scoped>
page {
  background-color: #f5f5f5;
}
.page {
  min-height: 100vh;
  position: relative;
}

/* 列表容器 */
.address-list {
  padding: 20rpx 24rpx;
  box-sizing: border-box;
}

/* 滑动外层包裹 */
.address-item-wrapper {
  position: relative;
  margin-bottom: 20rpx;
  border-radius: 20rpx;
}

/* 地址卡片主体 */
.address-item {
  background: #fff;
  border-radius: 24rpx;
  padding: 22rpx 24rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: relative;
  z-index: 2;
  transition: transform 0.2s ease;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);
}

/* 删除按钮显示时卡片向左平移 */
.address-item.show-delete {
  transform: translateX(-140rpx); /* 删除按钮宽度 */
}

/* 选择模式 */
.address-item.select-mode {
  border: 2rpx solid #333;
  background-color: #fafafa;
}

/* 左侧信息区 */
.addr-info {
  flex: 1;
  min-width: 0;
}
.addr-text {
  font-size: 28rpx;
  color: #1a1a1a;
  font-weight: 400;
  line-height: 1.5;
  word-break: break-all;
}
.contact {
  display: flex;
  align-items: center;
  gap: 8rpx;
  margin-top: 12rpx;
  font-size: 26rpx;
  color: #666;
}
.divider {
  color: #ccc;
  margin: 0 4rpx;
}

/* 右侧区域 */
.addr-right {
  display: flex;
  align-items: center;
  gap: 16rpx;
  flex-shrink: 0;
}

/* 地址类型标签 */
.type-tag {
  padding: 4rpx 16rpx;
  border-radius: 8rpx;
  font-size: 22rpx;
  font-weight: 500;
}
.type-tag.school {
  background: #e6f9e6;
  color: #4caf50;
}
.type-tag.outer {
  background: #eef5ff;
  color: #3498db;
}

.default-tag,
.default-btn {
  padding: 4rpx 14rpx;
  border-radius: 8rpx;
  font-size: 22rpx;
}

.default-tag {
  background: #fff3e0;
  color: #f59e0b;
}

.default-btn {
  background: #f4f6f8;
  color: #666;
}

/* 编辑图标 */
.edit-icon {
  width: 48rpx;
  height: 48rpx;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='none' stroke='%23999' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpath d='M17 3l4 4-7 7H10v-4l7-7z'/%3E%3Cpath d='M4 20l4-4'/%3E%3C/svg%3E");
  background-size: contain;
  background-repeat: no-repeat;
  opacity: 0.6;
}

/* 删除按钮 */
.delete-btn {
  position: absolute;
  right: 0;
  top: 0;
  bottom: 0;
  width: 180rpx;
  background: #e74c3c;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
  font-weight: 500;
  border-radius: 0 24rpx 24rpx 0;
  z-index: 1;
  opacity: 0;
  transition: opacity 0.2s ease;
}

/* 删除按钮显示时的样式 */
.address-item.show-delete + .delete-btn,
.address-item-wrapper:has(.show-delete) .delete-btn {
  opacity: 1;
}

/* 空状态 */
.empty {
  text-align: center;
  padding: 200rpx 0;
  font-size: 28rpx;
  color: #aaa;
}
.bottom-placeholder {
  height: 140rpx;
}

/* 底部添加按钮 */
.add-btn {
  position: fixed;
  bottom: 32rpx;
  left: 32rpx;
  right: 32rpx;
  background: #333;
  color: #fff;
  text-align: center;
  padding: 24rpx;
  border-radius: 40rpx;
  font-size: 30rpx;
  font-weight: 500;
  letter-spacing: 2rpx;
}
.add-btn:active {
  opacity: 0.85;
}
</style>
