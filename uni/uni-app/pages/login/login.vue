<template>
  <view class="page">
    <view class="panel">
      <view class="title">{{ isRegister ? '注册账号' : '登录账号' }}</view>
      <view class="sub-title">验证码暂用 123456 模拟，开发阶段登录一次后本地记住身份</view>

      <view class="mode-row">
        <view
          v-for="item in loginTypes"
          :key="item.value"
          class="mode-item"
          :class="{ active: activeType === item.value }"
          @click="activeType = item.value"
        >
          {{ item.label }}
        </view>
      </view>

      <view v-if="isRegister" class="avatar-box" @click="chooseAvatar">
        <image class="avatar" :src="form.avatar || defaultAvatar" mode="aspectFill"></image>
        <text class="avatar-text">上传头像</text>
      </view>

      <view class="form">
        <input class="input" v-model="form.account" :placeholder="accountPlaceholder" />
        <input class="input" v-model="form.password" password placeholder="密码，至少6位" />

        <view class="code-row">
          <input class="code-input" v-model="form.code" placeholder="验证码：123456" />
          <button class="code-btn" @click="fillMockCode">获取验证码</button>
        </view>

        <input v-if="isRegister" class="input" v-model="form.username" placeholder="用户昵称" />
        <view v-if="isRegister" class="gender-row">
          <view class="gender-item" :class="{ active: form.gender === 0 }" @click="form.gender = 0">未知</view>
          <view class="gender-item" :class="{ active: form.gender === 1 }" @click="form.gender = 1">男</view>
          <view class="gender-item" :class="{ active: form.gender === 2 }" @click="form.gender = 2">女</view>
        </view>
      </view>

      <button class="primary-btn" :loading="loading" @click="submit">
        {{ isRegister ? '注册并登录' : '登录' }}
      </button>

      <view class="switch-line" @click="toggleMode">
        {{ isRegister ? '已有账号，去登录' : '没有账号，去注册' }}
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import request from '@/utils/request.js'
import { saveLogin } from '@/utils/auth.js'
import { reconnectGlobalWebSocket } from '@/utils/globalWebSocket.js'
import { uploadImage } from '@/utils/upload.js'

const defaultAvatar = 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'
const loginTypes = [
  { label: '手机', value: 'phone', placeholder: '请输入手机号' },
  { label: '邮箱', value: 'email', placeholder: '请输入邮箱' },
  { label: '微信', value: 'wechat', placeholder: '请输入微信号' },
  { label: '支付宝', value: 'alipay', placeholder: '请输入支付宝账号' }
]

const isRegister = ref(false)
const loading = ref(false)
const activeType = ref('phone')
const form = ref({
  account: '',
  password: '',
  code: '',
  username: '',
  avatar: '',
  gender: 0
})

const accountPlaceholder = computed(() => {
  return loginTypes.find(item => item.value === activeType.value)?.placeholder || '请输入账号'
})

function fillMockCode() {
  form.value.code = '123456'
  uni.showToast({ title: '验证码已模拟填入', icon: 'none' })
}

function toggleMode() {
  isRegister.value = !isRegister.value
}

function validateForm() {
  const account = form.value.account.trim()
  if (!account || !form.value.password) {
    uni.showToast({ title: '请填写账号和密码', icon: 'none' })
    return false
  }
  if (activeType.value === 'phone' && !/^1\d{10}$/.test(account)) {
    uni.showToast({ title: '手机号格式不正确', icon: 'none' })
    return false
  }
  if (activeType.value === 'email' && !/^[^@\s]+@[^@\s]+\.[^@\s]+$/.test(account)) {
    uni.showToast({ title: '邮箱格式不正确', icon: 'none' })
    return false
  }
  if ((activeType.value === 'wechat' || activeType.value === 'alipay') && account.length < 3) {
    uni.showToast({ title: '账号至少3个字符', icon: 'none' })
    return false
  }
  if (form.value.password.length < 6) {
    uni.showToast({ title: '密码至少6位', icon: 'none' })
    return false
  }
  if (isRegister.value && !form.value.username.trim()) {
    uni.showToast({ title: '请填写用户昵称', icon: 'none' })
    return false
  }
  if (isRegister.value && form.value.code !== '123456') {
    uni.showToast({ title: '验证码为123456', icon: 'none' })
    return false
  }
  return true
}

async function chooseAvatar() {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    success: async (res) => {
      // 这里只做本地预览，真正上传放到点击注册时执行。
      form.value.avatar = res.tempFilePaths[0]
    }
  })
}

async function submit() {
  if (!validateForm()) return

  loading.value = true
  try {
    let avatarUrl = form.value.avatar
    const shouldUploadAvatar = isRegister.value && avatarUrl && !/^https?:\/\//.test(avatarUrl)
    if (shouldUploadAvatar) {
      let avatarUploadFailed = false
      uni.showLoading({ title: '头像上传中...', mask: true })
      try {
        avatarUrl = await uploadImage(avatarUrl, 'avatar')
      } catch (e) {
        avatarUploadFailed = true
      } finally {
        uni.hideLoading()
      }
      if (avatarUploadFailed) {
        uni.showToast({ title: '头像上传失败，请稍后重试', icon: 'none' })
        return
      }
    }

    const data = {
      loginType: activeType.value,
      registerType: activeType.value,
      account: form.value.account,
      password: form.value.password,
      code: form.value.code,
      username: form.value.username,
      avatar: avatarUrl,
      gender: form.value.gender
    }

    const res = await request({
      url: isRegister.value ? '/auth/register' : '/auth/login',
      method: 'POST',
      data
    })

    if (res.code !== 1) {
      uni.showToast({ title: res.msg || '操作失败', icon: 'none' })
      return
    }

    saveLogin(res.data)
    reconnectGlobalWebSocket()
    uni.showToast({ title: '登录成功', icon: 'none' })
    setTimeout(() => {
      uni.switchTab({ url: '/pages/my/my' })
    }, 300)
  } catch (e) {
    // 捕获所有异常，避免 Vue 警告: Unhandled error during execution of native event handler
    console.error('[Login submit error]', e)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40rpx;
  background: #f5f6f9;
  box-sizing: border-box;
}

.panel {
  width: 100%;
  padding: 42rpx 34rpx;
  background: #ffffff;
  border-radius: 24rpx;
  box-shadow: 0 12rpx 34rpx rgba(20, 30, 50, 0.08);
  box-sizing: border-box;
}

.title {
  font-size: 42rpx;
  font-weight: 800;
  color: #1f2933;
}

.sub-title {
  margin-top: 10rpx;
  font-size: 25rpx;
  color: #8a94a6;
  line-height: 1.5;
}

.mode-row,
.gender-row {
  display: flex;
  gap: 12rpx;
  margin-top: 28rpx;
}

.mode-item,
.gender-item {
  flex: 1;
  height: 62rpx;
  border-radius: 14rpx;
  background: #f4f6f8;
  color: #697386;
  font-size: 25rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.mode-item.active,
.gender-item.active {
  background: #1f2933;
  color: #ffffff;
}

.avatar-box {
  margin: 32rpx auto 0;
  width: 160rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.avatar {
  width: 128rpx;
  height: 128rpx;
  border-radius: 50%;
  background: #eef1f5;
}

.avatar-text {
  margin-top: 12rpx;
  font-size: 24rpx;
  color: #6c5ce7;
}

.form {
  margin-top: 32rpx;
}

.input {
  height: 88rpx;
  padding: 0 24rpx;
  margin-bottom: 20rpx;
  background: #f4f6f8;
  border: 1rpx solid #e5e9ef;
  border-radius: 16rpx;
  font-size: 28rpx;
  box-sizing: border-box;
}

.code-row {
  display: flex;
  gap: 14rpx;
  margin-bottom: 20rpx;
}

.code-input {
  flex: 1;
  height: 88rpx;
  padding: 0 24rpx;
  background: #f4f6f8;
  border: 1rpx solid #e5e9ef;
  border-radius: 16rpx;
  font-size: 28rpx;
  box-sizing: border-box;
}

.code-btn {
  width: 190rpx;
  height: 88rpx;
  line-height: 88rpx;
  border-radius: 16rpx;
  background: #eef0ff;
  color: #5146d8;
  font-size: 25rpx;
}

.primary-btn {
  height: 88rpx;
  margin-top: 16rpx;
  border-radius: 16rpx;
  background: #1f2933;
  color: #ffffff;
  font-size: 30rpx;
  font-weight: 700;
}

.switch-line {
  margin-top: 28rpx;
  text-align: center;
  font-size: 26rpx;
  color: #6c5ce7;
}
</style>
