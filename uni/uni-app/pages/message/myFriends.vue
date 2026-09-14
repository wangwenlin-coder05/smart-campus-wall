<template>
	<view class="page">
		<view class="header">
			<view class="header-top">
				<text class="title">我的好友</text>
				<view class="icon-btn" @click="goBack">
					<text class="icon">‹</text>
				</view>
			</view>
			<view class="stats-bar">
				<text class="stats-text">共 {{ friendList.length }} 位好友</text>
			</view>
		</view>

		<view class="search-bar">
			<input v-model="keyword" class="search-input" placeholder="搜索好友" @confirm="handleSearch" />
		</view>

		<scroll-view class="list" scroll-y :refresher-enabled="true" :refresher-triggered="refreshing" @refresherrefresh="refreshList">
			<view v-if="loading" class="state-tip">加载中...</view>
			<view v-else-if="filteredList.length === 0" class="empty">
				<text class="empty-icon">👥</text>
				<text class="empty-text">暂无好友</text>
				<view class="empty-btn" @click="goFindFriends">
					<text>去添加好友</text>
				</view>
			</view>

			<view v-for="item in filteredList" :key="item.id" class="friend-card" @click="openChat(item)">
				<view class="avatar-wrap">
					<image class="avatar" :src="item.avatar" mode="aspectFill"></image>
				</view>
				<view class="info-wrap">
					<view class="name-row">
						<text class="name">{{ item.name }}</text>
						<text v-if="item.remark" class="remark">{{ item.remark }}</text>
					</view>
					<text class="last-msg">{{ item.lastMsg || '打个招呼吧' }}</text>
				</view>
				<view class="action-btn" @click.stop="openChat(item)">
					<text>聊天</text>
				</view>
			</view>
		</scroll-view>
	</view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import request from '@/utils/request.js'
import { getCurrentUser } from '@/utils/auth.js'
import { getChatSourceInfo } from '@/utils/messageCenter.js'

const DEFAULT_AVATAR = 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'

const mockFriends = [
	{
		id: 'friend_001',
		name: '小明同学',
		avatar: 'https://picsum.photos/100/100?random=21',
		lastMsg: '一起去图书馆吗？',
		remark: '室友'
	},
	{
		id: 'friend_002',
		name: '学习搭子',
		avatar: 'https://picsum.photos/100/100?random=22',
		lastMsg: '作业写完了吗？',
		remark: '同班同学'
	},
	{
		id: 'friend_003',
		name: '运动小伙伴',
		avatar: 'https://picsum.photos/100/100?random=23',
		lastMsg: '今晚一起跑步',
		remark: '健身群认识'
	},
	{
		id: 'friend_004',
		name: '摄影师阿文',
		avatar: 'https://picsum.photos/100/100?random=24',
		lastMsg: '周末约拍的照片给你看一下',
		remark: '摄影社'
	},
	{
		id: 'friend_005',
		name: '代码女神',
		avatar: 'https://picsum.photos/100/100?random=25',
		lastMsg: '这段代码有问题，帮忙看看',
		remark: '编程小组'
	},
	{
		id: 'friend_006',
		name: '考研战友',
		avatar: 'https://picsum.photos/100/100?random=26',
		lastMsg: '打卡 Day 30',
		remark: '考研群'
	}
]

const friendList = ref([])
const keyword = ref('')
const loading = ref(true)
const refreshing = ref(false)

const filteredList = computed(() => {
	if (!keyword.value.trim()) {
		return friendList.value
	}
	const kw = keyword.value.trim().toLowerCase()
	return friendList.value.filter(f =>
		f.name.toLowerCase().includes(kw) ||
		(f.remark || '').toLowerCase().includes(kw)
	)
})

const loadFriends = async () => {
	loading.value = true
	try {
		const user = getCurrentUser()
		const userId = user ? user.uid : ''

		if (!userId) {
			friendList.value = [...mockFriends]
			return
		}

		const res = await request({
			url: '/friend/list',
			method: 'GET',
			data: { uid: userId }
		})

		if (res && res.code === 1 && Array.isArray(res.data) && res.data.length > 0) {
			friendList.value = res.data.map(item => ({
				id: 'friend_' + (item.uid || item.id),
				name: item.name || item.nickname || '好友',
				avatar: item.avatar || DEFAULT_AVATAR,
				lastMsg: item.lastMsg || '打个招呼吧',
				remark: item.remark || '',
				sourceId: item.uid || item.id
			}))
		} else {
			friendList.value = [...mockFriends]
		}
	} catch (e) {
		console.warn('加载好友列表失败，使用模拟数据', e)
		friendList.value = [...mockFriends]
	} finally {
		loading.value = false
		refreshing.value = false
	}
}

const goBack = () => {
	uni.navigateBack()
}

const goFindFriends = () => {
	uni.navigateTo({ url: '/pages/friend/friend' })
}

const handleSearch = () => {
	// 已在 computed 中处理
}

const openChat = (friend) => {
	const sourceInfo = getChatSourceInfo(friend.id)
	const params = [
		`conversationId=${friend.id}`,
		`sourceType=chat`,
		`sourceId=${friend.sourceId || friend.id}`,
		`name=${encodeURIComponent(friend.name)}`,
		`avatar=${encodeURIComponent(friend.avatar)}`,
		`sourceTitle=${encodeURIComponent(sourceInfo.sourceTitle || '')}`,
		`sourceDesc=${encodeURIComponent(sourceInfo.sourceDesc || '')}`,
		`targetUrl=${encodeURIComponent(sourceInfo.targetUrl || '')}`
	].join('&')

	uni.navigateTo({
		url: `/pages/message/message?${params}`
	})
}

const refreshList = () => {
	refreshing.value = true
	loadFriends()
}

onMounted(() => {
	loadFriends()
})

onShow(() => {
	if (friendList.value.length === 0) {
		loadFriends()
	}
})
</script>

<style scoped>
.page {
	position: fixed;
	left: 0;
	right: 0;
	top: 0;
	bottom: 0;
	background: #f5f6f9;
	display: flex;
	flex-direction: column;
}

.header {
	background: #ffffff;
	padding: 24rpx 28rpx 10rpx;
	flex-shrink: 0;
	box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.03);
}

.header-top {
	display: flex;
	align-items: center;
	justify-content: space-between;
	margin-bottom: 20rpx;
}

.title {
	font-size: 40rpx;
	font-weight: 800;
	color: #1a1d21;
}

.icon-btn {
	width: 68rpx;
	height: 68rpx;
	border-radius: 20rpx;
	background: #f5f6f8;
	display: flex;
	align-items: center;
	justify-content: center;
}

.icon {
	font-size: 40rpx;
	font-weight: 500;
	color: #1f2933;
}

.stats-bar {
	display: flex;
	align-items: center;
	padding: 10rpx 0 16rpx;
}

.stats-text {
	font-size: 24rpx;
	color: #8a94a6;
}

.search-bar {
	padding: 16rpx 28rpx;
	background: #ffffff;
	flex-shrink: 0;
	border-bottom: 1rpx solid #eef0f3;
}

.search-input {
	width: 100%;
	height: 72rpx;
	padding: 0 24rpx;
	background: #f1f3f5;
	border-radius: 36rpx;
	font-size: 28rpx;
}

.list {
	flex: 1;
	min-height: 0;
	box-sizing: border-box;
}

.state-tip {
	padding: 60rpx 0;
	text-align: center;
	font-size: 26rpx;
	color: #8a94a6;
}

.empty {
	display: flex;
	flex-direction: column;
	align-items: center;
	padding: 120rpx 0;
}

.empty-icon {
	font-size: 80rpx;
	margin-bottom: 20rpx;
}

.empty-text {
	font-size: 28rpx;
	color: #8a94a6;
}

.empty-btn {
	margin-top: 40rpx;
	padding: 20rpx 48rpx;
	background: #1f78d1;
	color: #ffffff;
	border-radius: 40rpx;
	font-size: 28rpx;
}

.friend-card {
	display: flex;
	align-items: center;
	margin: 20rpx 28rpx;
	padding: 24rpx;
	background: #ffffff;
	border-radius: 24rpx;
	box-shadow: 0 4rpx 16rpx rgba(20, 30, 50, 0.04);
}

.avatar-wrap {
	margin-right: 20rpx;
	flex-shrink: 0;
}

.avatar {
	width: 96rpx;
	height: 96rpx;
	border-radius: 48rpx;
	background: #eef1f5;
}

.info-wrap {
	flex: 1;
	min-width: 0;
}

.name-row {
	display: flex;
	align-items: center;
	gap: 12rpx;
	margin-bottom: 12rpx;
}

.name {
	font-size: 30rpx;
	font-weight: 700;
	color: #1f2933;
}

.remark {
	font-size: 20rpx;
	color: #8a94a6;
	background: #f1f3f5;
	padding: 4rpx 14rpx;
	border-radius: 10rpx;
}

.last-msg {
	font-size: 24rpx;
	color: #8a94a6;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}

.action-btn {
	padding: 14rpx 32rpx;
	background: #e8f4ff;
	color: #1f78d1;
	border-radius: 32rpx;
	font-size: 24rpx;
	font-weight: 600;
	flex-shrink: 0;
}
</style>