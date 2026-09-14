<template>
  <view class="page">
    <view class="search-row">
      <input class="search-input" v-model="keyword" placeholder="搜索昵称 / UID / 手机号" />
      <view class="search-btn" @click="searchUser">搜索</view>
    </view>

    <view class="section-title">搜索结果</view>
    <view v-if="searchList.length === 0" class="empty">输入关键词查找同学</view>
    <view v-for="user in searchList" :key="user.uid" class="user-card">
      <image class="avatar" :src="user.avatar || defaultAvatar" mode="aspectFill"></image>
      <view class="user-main">
        <view class="name">{{ user.username || '未设置昵称' }}</view>
        <view class="uid">{{ user.uid }}</view>
      </view>
      <view class="add-btn" @click="addFriend(user)">申请</view>
    </view>

    <view class="section-title">收到的申请</view>
    <view v-if="requestList.length === 0" class="empty">暂无好友申请</view>
    <view v-for="item in requestList" :key="item.id" class="user-card">
      <view class="user-main">
        <view class="name">{{ item.userUid }}</view>
        <view class="uid">请求添加你为好友</view>
      </view>
      <view class="add-btn" @click="acceptFriend(item)">同意</view>
    </view>

    <view class="section-title">我的好友</view>
    <view v-if="friendList.length === 0" class="empty">暂无好友</view>
    <view v-for="user in friendList" :key="user.uid" class="user-card" @click="openChat(user)">
      <image class="avatar" :src="user.avatar || defaultAvatar" mode="aspectFill"></image>
      <view class="user-main">
        <view class="name">{{ user.username || '未设置昵称' }}</view>
        <view class="uid">{{ user.uid }}</view>
      </view>
      <view class="chat-btn">聊天</view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request.js'
import { getCurrentUser } from '@/utils/auth.js'
import { buildFriendConversationId, getChatSourceInfo } from '@/utils/messageCenter.js'

const defaultAvatar = 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'
const keyword = ref('')
const searchList = ref([])
const friendList = ref([])
const requestList = ref([])

function currentUid() {
  const user = getCurrentUser()
  if (!user || !user.uid) {
    uni.navigateTo({ url: '/pages/login/login' })
    return ''
  }
  return user.uid
}

async function searchUser() {
  const uid = currentUid()
  if (!uid) return
  if (!keyword.value.trim()) {
    uni.showToast({ title: '请输入搜索内容', icon: 'none' })
    return
  }
  const res = await request({
    url: '/friend/search',
    method: 'GET',
    data: { uid, keyword: keyword.value.trim() }
  })
  if (res.code === 1) searchList.value = res.data || []
}

async function addFriend(user) {
  const uid = currentUid()
  if (!uid) return
  const res = await request({
    url: '/friend/add',
    method: 'POST',
    data: { userUid: uid, friendUid: user.uid }
  })
  uni.showToast({ title: res.msg || '已发送申请', icon: 'none' })
}

async function acceptFriend(item) {
  const uid = currentUid()
  if (!uid) return
  const res = await request({
    url: `/friend/accept?id=${item.id}&uid=${encodeURIComponent(uid)}`,
    method: 'POST'
  })
  uni.showToast({ title: res.msg || '已处理', icon: 'none' })
  await loadAll()
}

async function loadFriends() {
  const uid = currentUid()
  if (!uid) return
  const res = await request({
    url: '/friend/list',
    method: 'GET',
    data: { uid }
  })
  if (res.code === 1) friendList.value = res.data || []
}

async function loadRequests() {
  const uid = currentUid()
  if (!uid) return
  const res = await request({
    url: '/friend/requests',
    method: 'GET',
    data: { uid }
  })
  if (res.code === 1) requestList.value = res.data || []
}

async function loadAll() {
  await Promise.all([loadFriends(), loadRequests()])
}

function openChat(user) {
  const uid = currentUid()
  if (!uid) return
  const convId = buildFriendConversationId(uid, user.uid)
  const sourceInfo = getChatSourceInfo(convId)
  const params = [
    `conversationId=${convId}`,
    'sourceType=friend',
    `sourceId=${user.uid}`,
    `name=${encodeURIComponent(user.username || user.uid)}`,
    `avatar=${encodeURIComponent(user.avatar || defaultAvatar)}`,
    `sourceTitle=${encodeURIComponent(sourceInfo.sourceTitle || '')}`,
    `sourceDesc=${encodeURIComponent(sourceInfo.sourceDesc || '')}`,
    `targetUrl=${encodeURIComponent(sourceInfo.targetUrl || '')}`
  ].join('&')
  uni.navigateTo({ url: `/pages/message/message?${params}` })
}

onMounted(loadAll)
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #f5f6f9;
  padding: 24rpx;
  box-sizing: border-box;
}
.search-row {
  display: flex;
  gap: 16rpx;
}
.search-input {
  flex: 1;
  height: 78rpx;
  padding: 0 22rpx;
  border-radius: 14rpx;
  background: #fff;
}
.search-btn,
.add-btn,
.chat-btn {
  height: 78rpx;
  padding: 0 26rpx;
  border-radius: 14rpx;
  background: #1f2933;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
}
.chat-btn {
  background: #007aff;
}
.section-title {
  margin: 34rpx 0 16rpx;
  font-size: 28rpx;
  font-weight: 700;
  color: #333;
}
.empty {
  color: #9aa3af;
  font-size: 26rpx;
  padding: 10rpx 4rpx 18rpx;
}
.user-card {
  display: flex;
  align-items: center;
  gap: 18rpx;
  padding: 20rpx;
  margin-bottom: 16rpx;
  background: #fff;
  border-radius: 16rpx;
}
.avatar {
  width: 78rpx;
  height: 78rpx;
  border-radius: 50%;
}
.user-main {
  flex: 1;
  min-width: 0;
}
.name {
  font-size: 29rpx;
  font-weight: 700;
  color: #222;
}
.uid {
  margin-top: 6rpx;
  font-size: 23rpx;
  color: #8a94a6;
}
</style>