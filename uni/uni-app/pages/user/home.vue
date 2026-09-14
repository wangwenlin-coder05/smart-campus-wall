<template>
	<view class="page">
		<!-- 用户信息卡片 -->
		<view class="profile-card">
			<image class="avatar" :src="profile.avatar || defaultAvatar" mode="aspectFill"></image>
			<view class="profile-main">
				<text class="name">{{ profile.name }}</text>
				<text class="sub">公开主页</text>
			</view>
			<view class="action-btn" @click="handlePrimaryAction">{{ isFollowing ? '打招呼' : '+ 关注' }}</view>
		</view>

		<!-- tabs -->
		<view class="tabs">
			<view class="tab" :class="{ active: activeTab === 'posts' }" @click="activeTab = 'posts'">帖子</view>
			<view class="tab" :class="{ active: activeTab === 'groups' }" @click="activeTab = 'groups'">组局</view>
		</view>

		<!-- 内容滚动区 -->
		<scroll-view class="content-scroll" scroll-y :show-scrollbar="false" :refresher-enabled="true"
			:refresher-triggered="refreshing" @refresherrefresh="onRefresh">
			<!-- 帖子列表 -->
			<view v-if="activeTab === 'posts'">
				<view v-for="post in visiblePosts" :key="post.postNo || post.id" class="post-card">
					<view class="card-header">
						<image class="card-avatar" :src="post.avatar || profile.avatar" mode="aspectFill"></image>
						<view class="user-info">
							<text class="nickname">{{ post.nickname || profile.name }}</text>
							<text class="create-time">{{ formatTime(post.createTime) }}</text>
						</view>
					</view>
					<view class="content-text">{{ post.content }}</view>
					<view v-if="post.mediaUrlList && post.mediaUrlList.length" class="media-grid">
						<image v-for="(url, idx) in post.mediaUrlList.slice(0, 9)" :key="idx" :src="url"
							mode="aspectFill" class="grid-image" @click.stop="previewMedia(post.mediaUrlList, idx)"></image>
					</view>
					<view class="card-footer">
						<view class="footer-left">
							<text class="footer-item" v-if="post.viewCount > 0">浏览 {{ post.viewCount }}</text>
						</view>
						<view class="footer-right">
							<text class="footer-item" v-if="post.likeCount > 0">赞 {{ post.likeCount }}</text>
							<text class="footer-item" v-if="post.collectCount > 0">收藏 {{ post.collectCount }}</text>
						</view>
					</view>
				</view>
				<view v-if="!visiblePosts.length && !loading" class="empty">暂无公开帖子</view>
				<view v-if="loading" class="loading">加载中...</view>
			</view>

			<!-- 组局列表 -->
			<view v-else>
				<view v-for="group in groups" :key="group.id" class="group-card" @click="openGroup(group)">
					<view class="gc-main">
						<view class="gc-left">
							<image class="gc-poster" :src="group.posterImg || defaultPoster" mode="aspectFill"></image>
						</view>
						<view class="gc-right">
							<text class="gc-title">{{ group.title }}</text>
							<view class="gc-meta">
								<text>{{ group.activityTime }}</text>
								<text>{{ group.address }}</text>
							</view>
							<view class="gc-bottom">
								<text class="gc-count">{{ group.joinNum }}/{{ group.maxPeople }}人</text>
								<text class="gc-fee">{{ group.feeType || '免费' }}</text>
							</view>
						</view>
					</view>
					<view class="gc-action" @click.stop="openGroup(group)">去报名</view>
				</view>
				<view v-if="!groups.length && !loading" class="empty">暂无发布的组局</view>
				<view v-if="loading" class="loading">加载中...</view>
			</view>
		</scroll-view>
	</view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import request from '@/utils/request.js'
import { buildChatUrl } from '@/utils/messageCenter.js'
import { getCurrentUser } from '@/utils/auth.js'

const defaultAvatar = 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'
const defaultPoster = '/static/organization/feature-card.jpg'

const profile = ref({
	userId: '',
	name: '用户',
	avatar: defaultAvatar
})
const isFollowing = ref(false)
const activeTab = ref('posts')
const posts = ref([])
const groups = ref([])
const loading = ref(true)
const refreshing = ref(false)

const visiblePosts = computed(() => posts.value.filter(item => !item.anonymous && !item.isAnonymous))

const currentUser = computed(() => {
	const user = getCurrentUser()
	return user ? user.uid : ''
})

async function loadHome() {
	loading.value = true
	try {
		const params = { userId: profile.value.userId }
		if (currentUser.value) params.currentUid = currentUser.value
		const res = await request({
			url: '/user/home',
			method: 'GET',
			data: params
		})
		if (res.code === 1 && res.data) {
			profile.value = {
				...profile.value,
				name: res.data.name || profile.value.name,
				avatar: res.data.avatar || profile.value.avatar,
				userId: res.data.uid || profile.value.userId
			}
			groups.value = Array.isArray(res.data.groups) ? res.data.groups : []
			isFollowing.value = !!res.data.isFollowing
			await loadPostsFromWall()
			return
		}
		throw new Error('fallback')
	} catch (e) {
		await loadPostsFromWall()
		await loadGroupsFromOrg()
	} finally {
		loading.value = false
		refreshing.value = false
	}
}

async function loadPostsFromWall() {
	try {
		const res = await request({
			url: '/wall/post/listByUserId',
			method: 'GET',
			data: { UID: profile.value.userId, pageNum: 1, pageSize: 20 }
		})
		if (res.code === 1 && Array.isArray(res.data)) {
			posts.value = res.data
		}
	} catch (e) { /* */ }
}

async function loadGroupsFromOrg() {
	try {
		const uid = profile.value.userId
		const res = await request({
			url: '/organization/list',
			method: 'POST',
			data: { userId: uid, keyword: '', category: '', status: 'active' }
		})
		if (res.code === 1 && Array.isArray(res.data)) {
			groups.value = res.data.filter(g =>
				String(g.creatorUserId || g.hostUserId || g.userId) === uid
			)
		}
	} catch (e) { /* */ }
}

function handlePrimaryAction() {
	if (!isFollowing.value) {
		followUser()
		return
	}
	greetUser()
}

async function followUser() {
	const myUid = currentUser.value
	if (!myUid) {
		uni.showToast({ title: '请先登录', icon: 'none' })
		return
	}
	if (myUid === profile.value.userId) {
		uni.showToast({ title: '不能关注自己', icon: 'none' })
		return
	}
	try {
		const res = await request({
			url: '/friend/add',
			method: 'POST',
			data: {
				userUid: myUid,
				friendUid: profile.value.userId
			}
		})
		if (res.code === 1) {
			isFollowing.value = true
			uni.showToast({ title: '已发送好友申请', icon: 'success' })
		} else {
			uni.showToast({ title: res.msg || '操作失败', icon: 'none' })
		}
	} catch (e) {
		uni.showToast({ title: '网络异常，请重试', icon: 'none' })
	}
}

function greetUser() {
	if (!currentUser.value) {
		uni.showToast({ title: '请先登录', icon: 'none' })
		return
	}
	const targetId = profile.value.userId
	const myId = currentUser.value
	const parts = [myId, targetId].sort()
	const convId = `friend_${parts[0]}_${parts[1]}`
	uni.navigateTo({
		url: buildChatUrl({
			conversationId: convId,
			sourceType: 'chat',
			sourceId: targetId,
			name: profile.value.name,
			avatar: profile.value.avatar,
			sourceTitle: `${profile.value.name}的主页`,
			sourceDesc: '来自用户主页的打招呼',
			targetUrl: `/pages/user/home?userId=${encodeURIComponent(targetId)}&name=${encodeURIComponent(profile.value.name)}&avatar=${encodeURIComponent(profile.value.avatar)}`
		})
	})
}

function openGroup(group) {
	uni.navigateTo({
		url: `/pages/organization/organization?activityId=${encodeURIComponent(group.id)}`
	})
}

function formatTime(timeStr) {
	if (!timeStr) return ''
	const normalized = String(timeStr).replace(/-/g, '/')
	const date = new Date(normalized)
	if (Number.isNaN(date.getTime())) return timeStr
	const now = new Date()
	const diff = now - date
	if (diff < 60000) return '刚刚'
	if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
	if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
	const month = String(date.getMonth() + 1).padStart(2, '0')
	const day = String(date.getDate()).padStart(2, '0')
	return `${month}-${day}`
}

function previewMedia(urls, idx) {
	uni.previewImage({ urls, current: idx })
}

function onRefresh() {
	refreshing.value = true
	loadHome()
}

onLoad((options) => {
	profile.value = {
		userId: options.userId || '',
		name: options.name ? decodeURIComponent(options.name) : '用户',
		avatar: options.avatar ? decodeURIComponent(options.avatar) : defaultAvatar
	}
	loadHome()
})
</script>

<style scoped>
.page {
	height: 100vh;
	background: #f2f3f5;
	display: flex;
	flex-direction: column;
	overflow: hidden;
}

/* ===== 用户信息卡片 ===== */
.profile-card {
	margin: 24rpx;
	padding: 28rpx;
	border-radius: 24rpx;
	background: #ffffff;
	display: flex;
	align-items: center;
	box-shadow: 0 4rpx 20rpx rgba(20, 30, 50, 0.04);
	flex-shrink: 0;
}

.avatar {
	width: 116rpx;
	height: 116rpx;
	border-radius: 28rpx;
	background: #e9edf2;
	margin-right: 22rpx;
	flex-shrink: 0;
}

.profile-main {
	flex: 1;
	min-width: 0;
	display: flex;
	flex-direction: column;
}

.name {
	font-size: 36rpx;
	font-weight: 800;
	color: #1a1d21;
}

.sub {
	margin-top: 8rpx;
	font-size: 24rpx;
	color: #8a94a6;
}

.action-btn {
	height: 64rpx;
	padding: 0 28rpx;
	border-radius: 32rpx;
	background: #1f2933;
	color: #ffffff;
	font-size: 26rpx;
	font-weight: 700;
	display: flex;
	align-items: center;
	justify-content: center;
	flex-shrink: 0;
}

/* ===== tabs ===== */
.tabs {
	display: flex;
	gap: 16rpx;
	padding: 0 24rpx 18rpx;
	flex-shrink: 0;
}

.tab {
	height: 60rpx;
	padding: 0 30rpx;
	border-radius: 30rpx;
	background: #ffffff;
	color: #6a7484;
	font-size: 26rpx;
	font-weight: 700;
	display: flex;
	align-items: center;
}

.tab.active {
	background: #1f2933;
	color: #ffffff;
}

/* ===== 内容滚动区 ===== */
.content-scroll {
	flex: 1;
	min-height: 0;
	padding: 0 24rpx 36rpx;
	box-sizing: border-box;
}

/* ===== 帖子卡片（对齐 wall.vue 原样式） ===== */
.post-card {
	width: 100%;
	background: #fff;
	border-radius: 20rpx;
	padding: 24rpx;
	box-sizing: border-box;
	margin: 0 auto 20rpx auto;
	display: block;
	border-top: 2rpx solid #e0e0e0;
	border-left: 4rpx solid #e0e0e0;
	border-right: 4rpx solid #e0e0e0;
	border-bottom: 2rpx solid #e0e0e0;
	opacity: 0;
	transform: scale(0.95);
	animation: cardEnter 0.4s cubic-bezier(0.25, 0.46, 0.45, 0.94) forwards;
}

.post-card:last-child {
	margin-bottom: 0;
}

@keyframes cardEnter {
	0% { opacity: 0; transform: scale(0.95); }
	60% { transform: scale(1.02); }
	100% { opacity: 1; transform: scale(1); }
}

.card-header {
	display: flex;
	align-items: center;
	margin-bottom: 16rpx;
}

.card-avatar {
	width: 72rpx;
	height: 72rpx;
	border-radius: 50%;
	background: #e9edf2;
	margin-right: 16rpx;
	flex-shrink: 0;
}

.user-info {
	flex: 1;
	display: flex;
	flex-direction: column;
}

.nickname {
	font-size: 28rpx;
	font-weight: 600;
	color: #333;
	display: block;
}

.create-time {
	font-size: 22rpx;
	color: #999;
	margin-top: 4rpx;
}

.content-text {
	font-size: 28rpx;
	color: #333;
	line-height: 1.6;
	margin-bottom: 16rpx;
	word-break: break-all;
	display: -webkit-box;
	-webkit-line-clamp: 5;
	-webkit-box-orient: vertical;
	overflow: hidden;
	text-overflow: ellipsis;
}

/* ===== 媒体网格 ===== */
.media-grid {
	display: flex;
	flex-wrap: wrap;
	gap: 8rpx;
	margin-bottom: 16rpx;
}

.grid-image {
	width: calc((100% - 16rpx) / 3);
	aspect-ratio: 1;
	border-radius: 8rpx;
	background: #f0f0f0;
	overflow: hidden;
}

/* ===== 帖子底部 ===== */
.card-footer {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding-top: 12rpx;
	border-top: 1rpx solid #f0f2f5;
}

.footer-item {
	font-size: 22rpx;
	color: #999;
}

/* ===== 组局卡片（对齐 organization.vue activity-card 原样式） ===== */
.group-card {
	width: 100%;
	border-radius: 24rpx;
	background: linear-gradient(180deg, #ffffff 0%, #f1f8ff 100%);
	overflow: hidden;
	margin-bottom: 20rpx;
	box-shadow: 0 18rpx 42rpx rgba(70, 83, 104, 0.12), inset 0 1rpx 0 rgba(255, 255, 255, 0.96);
	border: 1rpx solid rgba(255, 255, 255, 0.9);
}

.gc-main {
	min-height: 220rpx;
	padding: 24rpx 22rpx 18rpx;
	box-sizing: border-box;
	display: flex;
	position: relative;
	background:
		linear-gradient(90deg, rgba(255, 255, 255, 0.72), rgba(244, 250, 255, 0.28)),
		repeating-linear-gradient(135deg, rgba(222, 235, 246, 0.32) 0 20rpx, rgba(255, 255, 255, 0) 20rpx 42rpx);
}

.gc-left {
	width: 170rpx;
	height: 224rpx;
	margin: 6rpx 18rpx 0 0;
	position: relative;
	flex-shrink: 0;
}

.gc-poster {
	position: absolute;
	left: 12rpx;
	top: 8rpx;
	width: 160rpx;
	height: 200rpx;
	border-radius: 18rpx;
	border: 4rpx solid #ffffff;
	transform: rotate(-5deg) translate(-12rpx, -6rpx);
	box-shadow: 0 10rpx 18rpx rgba(18, 35, 52, 0.18);
	background: #e9edf2;
}

.gc-right {
	flex: 1;
	min-width: 0;
	position: relative;
	z-index: 2;
	display: flex;
	flex-direction: column;
	justify-content: space-between;
}

.gc-title {
	display: block;
	font-size: 32rpx;
	line-height: 1.28;
	color: #17202a;
	font-weight: 900;
	margin-bottom: 12rpx;
	white-space: nowrap;
	overflow: hidden;
	text-overflow: ellipsis;
}

.gc-meta {
	display: flex;
	flex-direction: column;
	gap: 4rpx;
	font-size: 22rpx;
	color: #7d8996;
	font-weight: 700;
}

.gc-bottom {
	display: flex;
	justify-content: space-between;
	align-items: center;
}

.gc-count {
	font-size: 22rpx;
	color: #7d8996;
	font-weight: 700;
}

.gc-fee {
	font-size: 22rpx;
	color: #16a34a;
	font-weight: 700;
}

.gc-action {
	height: 56rpx;
	border-radius: 28rpx;
	background: #1f2933;
	color: #ffffff;
	font-size: 24rpx;
	font-weight: 700;
	display: flex;
	align-items: center;
	justify-content: center;
}

/* ===== 空状态 / 加载 ===== */
.empty,
.loading {
	padding: 160rpx 0;
	text-align: center;
	color: #9aa3af;
	font-size: 28rpx;
}
</style>