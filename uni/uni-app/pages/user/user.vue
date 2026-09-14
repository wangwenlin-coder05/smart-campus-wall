<template>
  <view class="container">
    <view class="card">
      <view class="avatar-wrap" @click="chooseAvatar">
        <image class="avatar" :src="form.avatar || defaultAvatar" mode="aspectFill"></image>
        <text class="avatar-tip">更换头像</text>
      </view>

      <view class="uid">UID：{{ form.uid }}</view>

      <view class="form-list">
        <view class="form-item">
          <text class="label">昵称</text>
          <input class="input" v-model="form.username" placeholder="请输入昵称" />
        </view>
        <view class="form-item">
          <text class="label">姓名</text>
          <input class="input" v-model="form.realName" placeholder="请输入真实姓名" />
        </view>
        <view class="form-item">
          <text class="label">性别</text>
          <picker mode="selector" :range="genderOptions" range-key="label" :value="form.gender || 0" @change="onGenderChange">
            <view class="picker-value">{{ genderText }}</view>
          </picker>
        </view>
        <view class="form-item">
          <text class="label">手机号</text>
          <input class="input" v-model="form.phone" placeholder="手机号" />
        </view>
        <view class="form-item">
          <text class="label">邮箱</text>
          <input class="input" v-model="form.email" placeholder="邮箱" />
        </view>
        <view class="form-item">
          <text class="label">学号</text>
          <input class="input" v-model="form.studentId" placeholder="学号" />
        </view>
        <view class="form-item">
          <text class="label">学院</text>
          <input class="input" v-model="form.college" placeholder="学院" />
        </view>
        <view class="form-item">
          <text class="label">专业</text>
          <input class="input" v-model="form.major" placeholder="专业" />
        </view>
        <view class="form-item">
          <text class="label">班级</text>
          <input class="input" v-model="form.className" placeholder="班级" />
        </view>
        <view class="form-item">
          <text class="label">宿舍</text>
          <input class="input" v-model="form.dormitory" placeholder="宿舍" />
        </view>
        <view class="form-item">
          <text class="label">QQ</text>
          <input class="input" v-model="form.qq" placeholder="QQ" />
        </view>
        <view class="form-item">
          <text class="label">微信</text>
          <input class="input" v-model="form.wechat" placeholder="微信" />
        </view>
        <view class="form-item">
          <text class="label">支付宝</text>
          <input class="input" v-model="form.alipayAccount" placeholder="支付宝账号" />
        </view>
        <view class="form-item">
          <text class="label">爱好</text>
          <input class="input" v-model="form.hobby" placeholder="爱好" />
        </view>
      </view>
    </view>

    <view class="save-btn" :class="{ disabled: saving }" @click="saveProfile">
      {{ saving ? '保存中...' : '保存资料' }}
    </view>
  </view>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import request from '@/utils/request.js'
import { getCurrentUser, saveLogin } from '@/utils/auth.js'
import { reconnectGlobalWebSocket } from '@/utils/globalWebSocket.js'
import { uploadImage } from '@/utils/upload.js'

const defaultAvatar = 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'
const genderOptions = [
  { label: '未知', value: 0 },
  { label: '男', value: 1 },
  { label: '女', value: 2 }
]

const form = ref({})
const saving = ref(false)

const genderText = computed(() => {
  return genderOptions.find(item => item.value === Number(form.value.gender || 0))?.label || '未知'
})

function onGenderChange(e) {
  form.value.gender = Number(e.detail.value)
}

async function loadProfile() {
  const currentUser = getCurrentUser()
  if (!currentUser || !currentUser.uid) {
    uni.navigateTo({ url: '/pages/login/login' })
    return
  }
  const res = await request({
    url: `/user/get/${currentUser.uid}`,
    method: 'GET'
  })
  if (res.code === 1) {
    form.value = res.data || {}
  }
}

function chooseAvatar() {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    success: res => {
      form.value.avatar = res.tempFilePaths[0]
    }
  })
}

async function saveProfile() {
  if (saving.value) return
  if (!form.value.username) {
    uni.showToast({ title: '请填写昵称', icon: 'none' })
    return
  }

  saving.value = true
  let loadingVisible = false
  try {
    let avatarUrl = form.value.avatar
    if (avatarUrl && !/^https?:\/\//.test(avatarUrl)) {
      uni.showLoading({ title: '头像上传中...', mask: true })
      loadingVisible = true
      avatarUrl = await uploadImage(avatarUrl, 'avatar')
      uni.hideLoading()
      loadingVisible = false
    }

    const res = await request({
      url: '/user/updateByUid',
      method: 'PUT',
      data: {
        ...form.value,
        avatar: avatarUrl
      }
    })

    if (res.code === 1) {
      saveLogin({
        token: uni.getStorageSync('token'),
        user: res.data
      })
      reconnectGlobalWebSocket()
      form.value = res.data
      uni.showToast({ title: '保存成功', icon: 'success' })
    } else {
      uni.showToast({ title: res.msg || '保存失败', icon: 'none' })
    }
  } catch (e) {
    uni.showToast({ title: '保存失败', icon: 'none' })
  } finally {
    if (loadingVisible) uni.hideLoading()
    saving.value = false
  }
}

onMounted(loadProfile)
</script>

<style scoped>
.container {
  background-color: #f5f7fa;
  min-height: 100vh;
  padding: 20rpx;
  padding-bottom: 150rpx;
}

.card {
  background: #fff;
  border-radius: 20rpx;
  padding: 34rpx 28rpx;
}

.avatar-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.avatar {
  width: 160rpx;
  height: 160rpx;
  border-radius: 50%;
  background: #eee;
}

.avatar-tip {
  margin-top: 12rpx;
  font-size: 24rpx;
  color: #6c5ce7;
}

.uid {
  margin-top: 18rpx;
  text-align: center;
  color: #888;
  font-size: 24rpx;
}

.form-list {
  margin-top: 32rpx;
}

.form-item {
  display: flex;
  align-items: center;
  min-height: 86rpx;
  border-bottom: 1rpx solid #eee;
}

.label {
  width: 150rpx;
  font-size: 28rpx;
  color: #666;
}

.input,
.picker-value {
  flex: 1;
  font-size: 28rpx;
  color: #333;
}

.save-btn {
  position: fixed;
  left: 32rpx;
  right: 32rpx;
  bottom: 32rpx;
  height: 88rpx;
  line-height: 88rpx;
  border-radius: 44rpx;
  text-align: center;
  color: #fff;
  background: #1f2933;
  font-size: 30rpx;
  font-weight: 700;
}

.save-btn.disabled {
  opacity: 0.55;
}
</style>
