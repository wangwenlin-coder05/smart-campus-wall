<template>
  <view class="page">
    <!-- 顶部渐变背景 + IP属地 -->
    <view class="header">
      <!-- 用户信息 + IP属地元素 -->
      <view class="user-info">
        <image class="avatar" :src="userInfo.avatar || '/static/default-avatar.png'" mode="aspectFill" @click="handleProfileTap"></image>
        <view class="info" @click="handleProfileTap">
          <text class="nickname">{{ userInfo.nickname || '未登录用户' }}</text>
          <view class="id-wrap">
            <text class="id-text">ID: {{ userInfo.uid || '-' }}</text>
            <image class="copy-icon" src="/static/copy.png" mode="aspectFill" @click.stop="copyId"></image>
          </view>
        </view>
        <button class="login-btn" @click="handleLoginTap">{{ loggedIn ? '退出' : '登录' }}</button>
        <!-- 右上角设置按钮 -->
        <view class="edit-icon" @click="goToUserPage">
          <image src="/static/myIcon/settings.png" mode="aspectFill"></image>
        </view>
        <!-- 头像IP属地：服务端渲染标签 -->
        <view class="ip-deco">🍈</view>
      </view>

      <!-- 接单员卡片（渐变色块 + 发光渐变按钮） -->
      <view class="rider-card dark-card">
        <!-- 提示条（纯色背景） -->
        <view class="tip-bar">
          <image class="tip-icon" src="/static/question.png" mode="aspectFill"></image>
          <text class="tip-text">什么是接单员？点击了解</text>
        </view>
        <view class="card-content">
          <view class="text-group">
            <view class="card-title">接单员 · 校园</view>
            <text class="card-desc">课余时间也有收益</text>
          </view>
          <button class="glow-btn" @click="applyRider">立即申请 ›</button>
        </view>
        <image class="decorate-img" src="/static/cute-green.png" mode="aspectFill"></image>
      </view>

      <!-- 推广员卡片（渐变色块 + 发光渐变按钮） -->
      <view class="promoter-card dark-card">
        <view class="text-group">
          <view class="card-title">推广员</view>
          <text class="card-desc">成为推广员，赚取收益</text>
        </view>
        <button class="glow-btn" @click="applyPromoter">立即申请 ›</button>
        <view class="sparkle">✦</view>
      </view>
    </view>

    <!-- 其他功能区（渐变色图标网格） -->
    <view class="function-section">
      <text class="section-title">其他功能</text>
      <view class="grid">
        <view class="grid-item" v-for="(item, index) in functionList" :key="index" @click="goToPage(item.path)">
          <!-- 使用渐变色图标轮廓的图片（可替换为实色资源，此处保留原图但添加渐变统一风格） -->
          <view class="icon-wrap">
            <image class="icon linear-icon" :src="item.icon" mode="aspectFill"></image>
            <view v-if="item.path === '/pages/order/myOrder' && rejectedOrderCount > 0" class="order-badge">
              {{ rejectedOrderCount > 99 ? '99+' : rejectedOrderCount }}
            </view>
          </view>
          <text class="name">{{ item.name }}</text>
        </view>
      </view>
    </view>

    <!-- 联系客服（简单入口） -->
    <view class="contact-item" @click="goToContact">
      <image class="icon" src="/static/service.png" mode="aspectFill"></image>
      <text class="text">联系客服</text>
      <view class="arrow">›</view>
    </view>

    <!-- 版本号 -->
    <view class="version">版本号 v4.0.2</view>

    <!-- 底部安全区占位 -->
    <view class="safe-bottom"></view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getCurrentUser, isLoggedIn, logout } from '@/utils/auth.js'
import { closeGlobalWebSocket } from '@/utils/globalWebSocket.js'
import request from '@/utils/request.js'

const userInfo = ref(getCurrentUser() || {})
const loggedIn = ref(isLoggedIn())
const rejectedOrderCount = ref(0)

// 功能列表（线性图标轮廓资源，原图若为纯色，可通过css渐变叠加统一风格）
const functionList = ref([
  { name: '我的订单', icon: '/static/myIcon/order-icon.png', path: '/pages/order/myOrder' },
  { name: '我的帖子', icon: '/static/myIcon/coupon-icon.png', path: '/pages/wall/myPublishWall' },
  { name: '我的地址', icon: '/static/myIcon/address-icon.png', path: '/pages/address/list' },
  { name: '我的组局', icon: '/static/myIcon/card-icon.png', path: '/pages/organization/myOrganization' },
  { name: '用户指南', icon: '/static/myIcon/guide-icon.png', path: '/pages/guide' },
  { name: '我的消息', icon: '/static/myIcon/message-icon.png', path: '/pages/message/messageList' }
])

onShow(() => {
  userInfo.value = getCurrentUser() || {}
  loggedIn.value = isLoggedIn()
  fetchRejectedOrderCount()
})

const fetchRejectedOrderCount = async () => {
  rejectedOrderCount.value = 0
  if (!loggedIn.value || !userInfo.value.uid) return
  try {
    const res = await request({
      url: `/order/userUid/${userInfo.value.uid}/page`,
      method: 'GET',
      data: {
        pageNum: 1,
        pageSize: 100
      }
    })
    if (res.code === 1) {
      const list = res.data.list || []
      rejectedOrderCount.value = list.filter(order => (
        order &&
        order.cancelReason &&
        order.riderCancelReason &&
        order.orderStatus !== 4 &&
        order.orderStatus !== 6
      )).length
    }
  } catch (e) {
    rejectedOrderCount.value = 0
  }
}

// 复制ID
const copyId = () => {
  if (!loggedIn.value || !userInfo.value.uid) {
    uni.navigateTo({ url: '/pages/login/login' })
    return
  }

  uni.setClipboardData({
    data: userInfo.value.uid,
    success: () => {
      uni.showToast({ title: 'ID已复制', icon: 'none' })
    }
  })
}

const goToUserPage = () => {
  if (!loggedIn.value) {
    uni.navigateTo({ url: '/pages/login/login' })
    return
  }
  uni.navigateTo({ url: '/pages/user/user' })
}

const handleProfileTap = () => {
  if (!loggedIn.value) {
    uni.navigateTo({ url: '/pages/login/login' })
    return
  }
  uni.navigateTo({ url: '/pages/user/user' })
}

const handleLoginTap = () => {
  if (!loggedIn.value) {
    uni.navigateTo({ url: '/pages/login/login' })
    return
  }

  logout()
  closeGlobalWebSocket()
  userInfo.value = {}
  loggedIn.value = false
  uni.showToast({ title: '已退出登录', icon: 'none' })
}

const applyRider = () => {
  uni.navigateTo({ url: '/pages/apply/rider' })
}

const applyPromoter = () => {
  uni.navigateTo({ url: '/pages/apply/promoter' })
}

// 通用跳转
const goToPage = (path) => {
  if (path) {
    uni.navigateTo({ url: path })
  } else {
    uni.showToast({ title: '功能开发中', icon: 'none' })
  }
}
// 联系客服
const goToContact = () => {
  uni.navigateTo({ url: '/pages/contact/service' })
}
</script>

<style scoped>
/* 全局清新背景 */
page {
  background-color: #f5f9f2;
}
.page {
  min-height: 100vh;
  position: relative;
  padding-top: var(--status-bar-height);
}

/* 顶部渐变 + 卡片底部弧度 */
.header {
  background: linear-gradient(135deg, #e6f9e6 0%, #f0fff0 100%);
  padding: 32rpx 32rpx 48rpx;
  border-bottom-left-radius: 40rpx;
  border-bottom-right-radius: 40rpx;
  position: relative;
  overflow: hidden;
}
.header::before {
  content: "";
  position: absolute;
  top: -20%;
  right: -20%;
  width: 200rpx;
  height: 200rpx;
  background: rgba(136, 232, 85, 0.1);
  border-radius: 50%;
  pointer-events: none;
}

/* 用户信息区 */
.user-info {
  display: flex;
  align-items: center;
  position: relative;
  margin-bottom: 32rpx;
}
.avatar {
  width: 112rpx;
  height: 112rpx;
  border-radius: 50%;
  margin-right: 24rpx;
  border: 3rpx solid #ffffff;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
}
.info {
  flex: 1;
}
.nickname {
  font-size: 36rpx;
  font-weight: 600;
  color: #1f2e1c;
  letter-spacing: 0.5rpx;
}
.id-wrap {
  display: flex;
  align-items: center;
  margin-top: 8rpx;
}
.id-text {
  font-size: 24rpx;
  color: #6f826a;
}
.copy-icon {
  width: 32rpx;
  height: 32rpx;
  margin-left: 12rpx;
  opacity: 0.7;
}
.edit-icon {
  position: absolute;
  top: -6rpx;
  right: 0;
  width: 48rpx;
  height: 48rpx;
}

.login-btn {
  position: absolute;
  top: 56rpx;
  right: 0;
  min-width: 96rpx;
  height: 52rpx;
  line-height: 52rpx;
  padding: 0 18rpx;
  border-radius: 26rpx;
  background: #1f2933;
  color: #ffffff;
  font-size: 24rpx;
}
.edit-icon image {
  width: 100%;
  height: 100%;
}
/* IP装饰小元素 */
.ip-deco {
  position: absolute;
  bottom: -16rpx;
  right: 32rpx;
  font-size: 52rpx;
  opacity: 0.6;
  transform: rotate(8deg);
}

.dark-card {
  background-color: #2c3e2a;
  border-radius: 32rpx;
  padding: 28rpx 24rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 6rpx 18rpx rgba(0, 0, 0, 0.08), inset 0 1rpx 0 rgba(255, 255, 255, 0.05);
  position: relative;
  overflow: hidden;
}
.card-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.text-group {
  flex: 1;
}
.card-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #f4fff0;
  margin-bottom: 8rpx;
}
.card-desc {
  font-size: 24rpx;
  color: #cfe6c9;
  line-height: 1.4;
}

/* 独立提示条样式 */
.tip-bar {
  background-color: #1e2a1c;
  border-radius: 40rpx;
  padding: 12rpx 20rpx;
  display: inline-flex;
  align-items: center;
  margin-bottom: 24rpx;
}
.tip-icon {
  width: 32rpx;
  height: 32rpx;
  margin-right: 12rpx;
  filter: brightness(0.9);
}
.tip-text {
  font-size: 24rpx;
  color: #bddbb5;
}

.glow-btn {
  background: linear-gradient(95deg, #aaff88 0%, #88e855 100%);
  border: none;
  padding: 16rpx 32rpx;
  border-radius: 48rpx;
  font-size: 28rpx;
  font-weight: 600;
  color: #1a3a12;
  box-shadow: 0 4rpx 12rpx rgba(136, 232, 85, 0.25);
  transition: all 0.2s ease;
}
.glow-btn:active {
  transform: scale(0.96);
  opacity: 0.9;
}

.decorate-img {
  position: absolute;
  bottom: 12rpx;
  right: 12rpx;
  width: 100rpx;
  height: 100rpx;
  opacity: 0.3;
  pointer-events: none;
}
.sparkle {
  position: absolute;
  top: 16rpx;
  right: 24rpx;
  font-size: 32rpx;
  opacity: 0.4;
}

.promoter-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.promoter-card .text-group {
  flex: 1;
}

.function-section {
  background: #fefef7;
  margin: 32rpx 32rpx 24rpx;
  border-radius: 32rpx;
  padding: 28rpx 20rpx 12rpx;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.02), 0 1rpx 0 #ffffff inset;
}
.section-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #2e3b2b;
  padding-left: 8rpx;
  margin-bottom: 24rpx;
  display: block;
}
.grid {
  display: flex;
  flex-wrap: wrap;
}
.grid-item {
  width: 25%;
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 36rpx;
}
/* 绾挎€у浘鏍囬鏍肩粺涓€锛氳缃楂樸€佹护闀滃幓鑹叉彁绾跨鎰?*/
.linear-icon {
  width: 48rpx;
  height: 48rpx;
  opacity: 0.85;
  filter: grayscale(0.1) brightness(0.98);
  margin-bottom: 12rpx;
}
.icon-wrap {
  position: relative;
  width: 56rpx;
  height: 60rpx;
  display: flex;
  justify-content: center;
}
.order-badge {
  position: absolute;
  top: -12rpx;
  right: -18rpx;
  min-width: 32rpx;
  height: 32rpx;
  padding: 0 8rpx;
  border-radius: 18rpx;
  background: #e74c3c;
  color: #fff;
  font-size: 20rpx;
  font-weight: 700;
  line-height: 32rpx;
  text-align: center;
  border: 3rpx solid #fefef7;
  box-sizing: border-box;
}
.name {
  font-size: 26rpx;
  color: #4d5f47;
  font-weight: 450;
}

/* 鑱旂郴瀹㈡湇琛?(绠€绾﹂鏍? */
.contact-item {
  display: flex;
  align-items: center;
  background: #ffffffdd;
  backdrop-filter: blur(2px);
  margin: 16rpx 32rpx;
  border-radius: 60rpx;
  padding: 24rpx 28rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.02);
  border: 0.5rpx solid #e2f0da;
}
.contact-item .icon {
  width: 44rpx;
  height: 44rpx;
  margin-right: 20rpx;
  opacity: 0.8;
}
.contact-item .text {
  flex: 1;
  font-size: 28rpx;
  color: #3a5534;
  font-weight: 500;
}
.contact-item .arrow {
  font-size: 36rpx;
  color: #aec9a2;
}

.version {
  text-align: center;
  font-size: 24rpx;
  color: #b1c9a8;
  margin: 48rpx 0 32rpx;
  letter-spacing: 1rpx;
}

/* 搴曢儴瀹夊叏鍖?*/
.safe-bottom {
  height: calc(40rpx + env(safe-area-inset-bottom));
}
</style>