<template>
	<view class="page">
		<view class="header">
			<view>
				<text class="eyebrow">MY ACTIVITIES</text>
				<text class="title">我的组局</text>
			</view>
			<view class="create-btn" @click="goCreate">发布</view>
		</view>

		<scroll-view class="main-scroll" scroll-y :show-scrollbar="false" :scroll-into-view="scrollIntoViewId">
			<view class="overview">
				<view class="score-block">
					<text class="score">{{ dashboardScore }}</text>
					<text class="score-label">活跃指数</text>
				</view>
				<view class="metric-grid">
					<view class="metric">
						<text>{{ launchedList.length }}</text>
						<text>我发起</text>
					</view>
					<view class="metric">
						<text>{{ joinedList.length }}</text>
						<text>已报名</text>
					</view>
					<view class="metric">
						<text>{{ pendingCount }}</text>
						<text>待处理</text>
					</view>
				</view>
			</view>

			<view class="brief-card">
				<view class="brief-head">
					<text>今日看板</text>
					<text>{{ todayText }}</text>
				</view>
				<view class="brief-row" v-for="item in briefList" :key="item.label">
					<view class="brief-dot" :class="item.type"></view>
					<text>{{ item.label }}</text>
				</view>
			</view>

			<view class="tabs-wrapper">
				<view class="tabs">
					<view v-for="tab in tabs" :key="tab.value" class="tab" :class="{ active: activeTab === tab.value }"
						@click="activeTab = tab.value">
						{{ tab.label }}
					</view>
				</view>
			</view>

			<view class="activity-list">
				<view v-for="(item, index) in currentList" :key="item.id" class="activity-card"
					:id="'card-' + item.id"
					:class="{ 'flash-once': flashTargetId === item.id && flashCount === 1, 'flash-twice': flashTargetId === item.id && flashCount === 2 }"
					:style="{ animationDelay: Math.min(index, 6) * 0.035 + 's' }">
					<view class="card-top">
						<image class="poster" :src="item.posterImg" mode="aspectFill"></image>
						<view class="activity-main">
							<view class="status-row">
								<text class="category">{{ item.tagName }}</text>
								<text class="status" :class="item.status">{{ statusText(item.status) }}</text>
							</view>
							<text class="activity-title">{{ item.title }}</text>
							<view class="meta">
								<text>{{ item.activityTime }}</text>
								<text>{{ item.address }}</text>
							</view>
							<view class="progress-line">
								<view class="progress-fill" :style="{ width: getProgress(item) + '%' }"></view>
							</view>
							<view class="capacity">
								<text>{{ item.joinNum }}/{{ item.maxPeople }} 人</text>
								<text>{{ item.feeType }}</text>
							</view>
						</view>
					</view>

					<view class="member-row">
						<view class="avatar-stack">
							<image v-for="avatar in item.avatarList" :key="avatar" class="member-avatar" :src="avatar"
								mode="aspectFill"></image>
							<view v-if="!item.avatarList.length" class="empty-avatar">0</view>
						</view>
						<text class="member-text">{{ getMemberText(item) }}</text>
					</view>

					<view class="tool-row" v-if="activeTab === 'launch'">
						<view class="tool ghost" @click="copyInvite(item)">复制邀请</view>
						<view class="tool ghost" @click="openRoster(item)">名单</view>
						<view class="tool dark" @click="toggleActivity(item)">
							{{ item.status === 'closed' ? '重新开放' : '收组' }}
						</view>
					</view>
					<view class="tool-row" v-else>
						<view class="tool ghost" @click="copyInvite(item)">复制信息</view>
						<view class="tool dark" @click="openCheckin(item)">组局码</view>
						<view class="tool danger" @click="onQuitActivity(item)">退出组局</view>
					</view>
				</view>

				<view v-if="currentList.length === 0" class="empty">
					<text class="empty-title">{{ emptyTitle }}</text>
					<text class="empty-desc">去大厅看看，或者自己发起一场新的校园活动。</text>
					<view class="empty-btn" @click="goCreate">我要组局</view>
				</view>
			</view>
		</scroll-view>

		<view class="modal-mask" v-if="showModal" @click="closeModal">
			<view class="modal" @click.stop>
				<view class="modal-head">
					<text>{{ modalTitle }}</text>
					<view class="modal-close" @click="closeModal">×</view>
				</view>
				<view v-if="modalMode === 'roster'" class="roster">
					<view v-for="member in activeActivity.avatarList || []" :key="member" class="roster-item">
						<image :src="member" mode="aspectFill"></image>
						<view>
							<text>报名同学</text>
							<text>等待确认到场</text>
						</view>
						<text class="roster-state">已报名</text>
					</view>
					<view v-if="!(activeActivity.avatarList || []).length" class="modal-empty">暂时还没人报名</view>
				</view>
				<view v-else class="checkin">
					<image class="qrcode" src="/static/11/1.png" mode="aspectFit"></image>
					<text class="check-title">{{ activeActivity.title }}</text>
					<text class="check-copy">到场时出示二维码，组织者扫码确认。</text>
				</view>
			</view>
		</view>
	</view>
</template>

<script setup>
import { computed, ref, nextTick } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import request from '@/utils/request.js'
import { getCurrentUser } from '@/utils/auth.js'

const activeTab = ref('launch')
const showModal = ref(false)
const modalMode = ref('roster')
const activeActivity = ref({})
const loading = ref(false)

const flashTargetId = ref(null)
const flashCount = ref(0)
const scrollIntoViewId = ref('')
const pendingActivityId = ref('')
const pendingFlash = ref(0)

const currentUid = computed(() => getCurrentUser()?.uid || '')
const currentUser = computed(() => getCurrentUser())

const createdList = ref([])
const joinedList = ref([])
const collectedList = ref([])

const tabs = [
	{ label: '我发起', value: 'launch' },
	{ label: '我报名', value: 'join' },
	{ label: '收藏', value: 'collect' }
]

const fallbackActivities = [
	{
		id: 'mine-1',
		title: '周五火锅拼桌缺两位',
		tagName: '火锅',
		activityTime: '周五 18:30',
		address: '松北路 4 号',
		joinNum: 6,
		maxPeople: 8,
		feeType: 'AA',
		status: 'active',
		posterImg: '/static/organization/feature-card.jpg',
		avatarList: ['/static/default-avatar.png', '/static/default-avatar.png']
	},
	{
		id: 'mine-2',
		title: '图书馆番茄钟自习',
		tagName: '自习',
		activityTime: '明天 09:00',
		address: '图书馆 3F',
		joinNum: 11,
		maxPeople: 16,
		feeType: '免费',
		status: 'review',
		posterImg: '/static/organization/feature-card.jpg',
		avatarList: ['/static/default-avatar.png']
	}
]

const joinedMock = [
	{
		id: 'join-1',
		title: '操场夜跑打卡',
		tagName: '跑步',
		activityTime: '今天 20:10',
		address: '东区操场',
		joinNum: 5,
		maxPeople: 12,
		feeType: '免费',
		status: 'active',
		posterImg: '/static/organization/feature-card.jpg',
		avatarList: ['/static/default-avatar.png', '/static/default-avatar.png', '/static/default-avatar.png']
	}
]

const collectMock = [
	{
		id: 'collect-1',
		title: '校园人像互拍',
		tagName: '摄影',
		activityTime: '周六 16:20',
		address: '南湖公园东门',
		joinNum: 5,
		maxPeople: 10,
		feeType: 'AA',
		status: 'active',
		posterImg: '/static/organization/feature-card.jpg',
		avatarList: ['/static/default-avatar.png']
	}
]

const todayText = computed(() => {
	const now = new Date()
	return `${now.getMonth() + 1}/${now.getDate()}`
})

const launchedList = computed(() => createdList.value)
const pendingCount = computed(() => launchedList.value.filter(item => item.status === 'review').length)
const dashboardScore = computed(() => launchedList.value.length * 18 + joinedList.value.length * 9 + collectedList.value.length * 5)

const currentList = computed(() => {
	if (activeTab.value === 'join') return joinedList.value
	if (activeTab.value === 'collect') return collectedList.value
	return launchedList.value
})

const emptyTitle = computed(() => {
	if (activeTab.value === 'join') return '还没有报名活动'
	if (activeTab.value === 'collect') return '还没有收藏活动'
	return '还没有发起组局'
})

const briefList = computed(() => [
	{ type: 'hot', label: launchedList.value.length ? `最近一场：${launchedList.value[0].title}` : '你还没有发布组局' },
	{ type: 'warn', label: pendingCount.value ? `${pendingCount.value} 场组局等待审核` : '暂无待审核组局' },
	{ type: 'ok', label: joinedList.value.length ? `你报名了 ${joinedList.value.length} 场活动` : '今天可以去大厅找个搭子' }
])

const getProgress = item => {
	if (!item.maxPeople) return 0
	return Math.min(100, Math.round((item.joinNum / item.maxPeople) * 100))
}

const statusText = status => {
	const map = { active: '进行中', review: '待审核', closed: '已收组' }
	return map[status] || '进行中'
}

const getMemberText = item => {
	if (!item.joinNum) return '暂时还没人报名'
	if (item.joinNum >= item.maxPeople) return '人数已满，准备出发'
	return `还差 ${item.maxPeople - item.joinNum} 人成局`
}

onLoad((options) => {
	if (options.activityId) {
		pendingActivityId.value = options.activityId
		pendingFlash.value = Number(options.flash) || 0
	}
})

const triggerFlash = async (activityId, flashCountValue) => {
	const targetTab = launchedList.value.some(item => String(item.id) === String(activityId))
		? 'launch'
		: joinedList.value.some(item => String(item.id) === String(activityId))
			? 'join'
			: collectedList.value.some(item => String(item.id) === String(activityId))
				? 'collect'
				: activeTab.value
	if (targetTab !== activeTab.value) {
		activeTab.value = targetTab
	}
	await nextTick()
	scrollIntoViewId.value = ''
	await nextTick()
	scrollIntoViewId.value = 'card-' + activityId
	await nextTick()
	flashTargetId.value = activityId
	flashCount.value = flashCountValue
	setTimeout(() => {
		flashTargetId.value = null
		flashCount.value = 0
	}, flashCountValue * 700)
}

const fetchMyData = async () => {
	if (!currentUid.value) {
		loadFallback()
		return
	}
	loading.value = true
	try {
		const res = await request({
			url: `/organization/my?userId=${currentUid.value}`,
			method: 'GET'
		})
		if (res && res.code === 1 && res.data) {
			createdList.value = res.data.created || []
			joinedList.value = res.data.joined || []
			loadCollected()
		} else {
			loadFallback()
		}
	} catch (e) {
		console.error('获取我的组局失败', e)
		loadFallback()
	} finally {
		loading.value = false
		if (pendingActivityId.value) {
			const id = pendingActivityId.value
			const fc = pendingFlash.value
			pendingActivityId.value = ''
			pendingFlash.value = 0
			nextTick(() => {
				triggerFlash(id, fc)
			})
		}
	}
}

const loadFallback = () => {
	createdList.value = fallbackActivities
	joinedList.value = joinedMock
	loadCollected()
}

const loadCollected = () => {
	const stored = uni.getStorageSync('mockCollectedActivities') || []
	collectedList.value = Array.isArray(stored) && stored.length ? stored : collectMock
}

const goCreate = () => {
	uni.navigateTo({ url: '/pages/organization/publishOrganization' })
}

const copyInvite = item => {
	uni.setClipboardData({
		data: `${item.title}｜${item.activityTime}｜${item.address}`,
		success: () => uni.showToast({ title: '已复制', icon: 'none' })
	})
}

const toggleActivity = async item => {
	if (!currentUid.value) return
	const newStatus = item.status === 'closed' ? 'active' : 'closed'
	try {
		const res = await request({
			url: '/organization/status',
			method: 'POST',
			data: { id: item.id, status: newStatus }
		})
		if (res && res.code === 1) {
			item.status = newStatus
			uni.showToast({ title: newStatus === 'closed' ? '已收组' : '已重新开放', icon: 'none' })
		} else {
			uni.showToast({ title: res?.msg || '操作失败', icon: 'none' })
		}
	} catch (e) {
		uni.showToast({ title: '操作失败', icon: 'none' })
	}
}

const openRoster = item => {
	activeActivity.value = item
	modalMode.value = 'roster'
	showModal.value = true
}

const openCheckin = item => {
	activeActivity.value = item
	modalMode.value = 'checkin'
	showModal.value = true
}

const onQuitActivity = (item) => {
	uni.showModal({
		title: '退出组局',
		content: '确定要退出该组局吗？',
		success: async (res) => {
			if (res.confirm) {
				try {
					const apiRes = await request({
						url: '/organization/leave',
						method: 'POST',
						data: {
							activityId: item.id,
							userId: currentUid.value
						}
					})
					if (apiRes.code === 1) {
						uni.showToast({ title: '已退出组局', icon: 'success' })
						fetchMyData()
					} else {
						uni.showToast({ title: apiRes.msg || '退出失败', icon: 'none' })
					}
				} catch (e) {
					uni.showToast({ title: '退出失败，请稍后再试', icon: 'none' })
				}
			}
		}
	})
}

const closeModal = () => {
	showModal.value = false
}

const modalTitle = computed(() => modalMode.value === 'roster' ? '报名名单' : '签到二维码')

onShow(() => {
	fetchMyData()
})
</script>

<style>
page {
	background: #f4f6f8;
}
</style>

<style scoped>
.page {
	height: 100vh;
	padding-top: var(--status-bar-height);
	background: #f4f6f8;
	color: #20242a;
	overflow: hidden;
}

.header {
	padding: 26rpx 28rpx 18rpx;
	display: flex;
	align-items: center;
	justify-content: space-between;
	background: #f4f6f8;
}

.eyebrow {
	display: block;
	font-size: 20rpx;
	color: #9aa3af;
	font-weight: 800;
	letter-spacing: 0;
	margin-bottom: 8rpx;
}

.title {
	display: block;
	font-size: 46rpx;
	line-height: 1;
	font-weight: 900;
}

.create-btn {
	height: 64rpx;
	padding: 0 28rpx;
	border-radius: 16rpx;
	background: #20242a;
	color: #fff;
	font-size: 25rpx;
	font-weight: 900;
	display: flex;
	align-items: center;
	justify-content: center;
}

.main-scroll {
	height: calc(100vh - 140rpx);
}

.overview {
	margin: 0 28rpx 22rpx;
	padding: 28rpx;
	border-radius: 18rpx;
	background: #ffffff;
	display: flex;
	align-items: center;
	box-shadow: 0 18rpx 46rpx rgba(31, 40, 51, 0.05);
}

.score-block {
	width: 190rpx;
	border-right: 1rpx solid #edf0f4;
}

.score {
	display: block;
	font-size: 58rpx;
	font-weight: 900;
	line-height: 1;
}

.score-label {
	display: block;
	font-size: 22rpx;
	color: #8b95a1;
	font-weight: 800;
	margin-top: 8rpx;
}

.metric-grid {
	flex: 1;
	display: flex;
	justify-content: space-around;
}

.metric {
	display: flex;
	flex-direction: column;
	align-items: center;
	gap: 8rpx;
}

.metric text:first-child {
	font-size: 34rpx;
	font-weight: 900;
}

.metric text:last-child {
	font-size: 21rpx;
	color: #8b95a1;
	font-weight: 800;
}

.brief-card {
	margin: 0 28rpx 22rpx;
	padding: 24rpx;
	border-radius: 18rpx;
	background: #20242a;
	color: #ffffff;
}

.brief-head {
	display: flex;
	justify-content: space-between;
	font-size: 26rpx;
	font-weight: 900;
	margin-bottom: 18rpx;
}

.brief-row {
	display: flex;
	align-items: center;
	gap: 12rpx;
	font-size: 24rpx;
	color: rgba(255, 255, 255, 0.78);
	margin-top: 12rpx;
}

.brief-dot {
	width: 14rpx;
	height: 14rpx;
	border-radius: 50%;
	background: #58d68d;
}

.brief-dot.warn {
	background: #f6c453;
}

.brief-dot.hot {
	background: #7dd3fc;
}

.tabs-wrapper {
	position: sticky;
	top: 0;
	z-index: 20;
	background: #f4f6f8;
	padding: 0 28rpx 18rpx;
}

.tabs {
	display: flex;
	gap: 14rpx;
}

.tab {
	flex: 1;
	height: 66rpx;
	border-radius: 16rpx;
	background: #ffffff;
	color: #7d8792;
	font-size: 24rpx;
	font-weight: 900;
	display: flex;
	align-items: center;
	justify-content: center;
	box-shadow: 0 10rpx 24rpx rgba(31, 40, 51, 0.04);
}

.tab.active {
	background: #20242a;
	color: #ffffff;
}

.activity-list {
	padding: 0 28rpx 48rpx;
}

.activity-card {
	padding: 24rpx;
	margin-bottom: 22rpx;
	border-radius: 18rpx;
	background: #ffffff;
	box-shadow: 0 18rpx 46rpx rgba(31, 40, 51, 0.05);
	opacity: 0;
	transform: scale(0.88);
	animation: cardFadeIn 0.35s cubic-bezier(0.25, 0.46, 0.45, 0.94) forwards;
}

@keyframes cardFadeIn {
	to {
		opacity: 1;
		transform: scale(1);
	}
}

.card-top {
	display: flex;
	gap: 20rpx;
}

.poster {
	width: 144rpx;
	height: 176rpx;
	border-radius: 16rpx;
	background: #eef1f5;
	flex-shrink: 0;
}

.activity-main {
	flex: 1;
	min-width: 0;
}

.status-row,
.capacity,
.member-row,
.tool-row {
	display: flex;
	align-items: center;
}

.status-row {
	justify-content: space-between;
	margin-bottom: 10rpx;
}

.category {
	height: 36rpx;
	padding: 0 14rpx;
	border-radius: 999rpx;
	background: #eef3f8;
	color: #596675;
	font-size: 20rpx;
	font-weight: 900;
	display: flex;
	align-items: center;
}

.status {
	height: 38rpx;
	padding: 0 14rpx;
	border-radius: 999rpx;
	font-size: 20rpx;
	font-weight: 900;
	display: flex;
	align-items: center;
	background: #edf8f2;
	color: #3f9466;
}

.status.review {
	background: #f8f1df;
	color: #9a711e;
}

.status.closed {
	background: #eef1f5;
	color: #8b95a1;
}

.activity-title {
	display: block;
	font-size: 30rpx;
	font-weight: 900;
	line-height: 1.35;
	margin-bottom: 10rpx;
	white-space: nowrap;
	overflow: hidden;
	text-overflow: ellipsis;
}

.meta {
	display: flex;
	flex-direction: column;
	gap: 6rpx;
	color: #8b95a1;
	font-size: 22rpx;
	font-weight: 700;
}

.progress-line {
	height: 12rpx;
	border-radius: 999rpx;
	background: #eef1f5;
	overflow: hidden;
	margin: 16rpx 0 10rpx;
}

.progress-fill {
	height: 100%;
	border-radius: 999rpx;
	background: #20242a;
}

.capacity {
	justify-content: space-between;
	color: #596675;
	font-size: 22rpx;
	font-weight: 800;
}

.member-row {
	margin-top: 22rpx;
	gap: 14rpx;
}

.avatar-stack {
	display: flex;
	align-items: center;
}

.member-avatar,
.empty-avatar {
	width: 42rpx;
	height: 42rpx;
	border-radius: 50%;
	border: 3rpx solid #ffffff;
	margin-left: -10rpx;
	background: #dfe6ee;
}

.member-avatar:first-child {
	margin-left: 0;
}

.empty-avatar {
	margin-left: 0;
	display: flex;
	align-items: center;
	justify-content: center;
	color: #8b95a1;
	font-size: 20rpx;
	font-weight: 900;
}

.member-text {
	color: #6f7b88;
	font-size: 23rpx;
	font-weight: 800;
}

.tool-row {
	gap: 14rpx;
	margin-top: 22rpx;
}

.tool {
	flex: 1;
	height: 66rpx;
	border-radius: 16rpx;
	font-size: 24rpx;
	font-weight: 900;
	display: flex;
	align-items: center;
	justify-content: center;
}

.tool.ghost {
	background: #f4f6f8;
	color: #596675;
}

.tool.dark {
	background: #20242a;
	color: #ffffff;
}

.tool.danger {
	background: #fff0f0;
	color: #e74c3c;
}

.empty {
	margin-top: 80rpx;
	display: flex;
	flex-direction: column;
	align-items: center;
	text-align: center;
}

.empty-title {
	font-size: 30rpx;
	font-weight: 900;
}

.empty-desc {
	margin-top: 12rpx;
	font-size: 24rpx;
	color: #8b95a1;
}

.empty-btn {
	margin-top: 28rpx;
	height: 72rpx;
	padding: 0 32rpx;
	border-radius: 999rpx;
	background: #20242a;
	color: #ffffff;
	font-size: 26rpx;
	font-weight: 900;
	display: flex;
	align-items: center;
	justify-content: center;
}

.modal-mask {
	position: fixed;
	inset: 0;
	background: rgba(0, 0, 0, 0.48);
	display: flex;
	align-items: flex-end;
	z-index: 99;
}

.modal {
	width: 100%;
	max-height: 72vh;
	border-radius: 28rpx 28rpx 0 0;
	background: #ffffff;
	padding: 28rpx;
	box-sizing: border-box;
}

.modal-head {
	display: flex;
	justify-content: space-between;
	align-items: center;
	font-size: 32rpx;
	font-weight: 900;
	margin-bottom: 24rpx;
}

.modal-close {
	width: 56rpx;
	height: 56rpx;
	border-radius: 50%;
	background: #f4f6f8;
	display: flex;
	align-items: center;
	justify-content: center;
	color: #7d8792;
}

.roster-item {
	height: 92rpx;
	display: flex;
	align-items: center;
	border-bottom: 1rpx solid #eef1f5;
}

.roster-item image {
	width: 56rpx;
	height: 56rpx;
	border-radius: 50%;
	margin-right: 16rpx;
}

.roster-item view {
	flex: 1;
	display: flex;
	flex-direction: column;
}

.roster-item view text:first-child {
	font-size: 26rpx;
	font-weight: 900;
}

.roster-item view text:last-child,
.roster-state,
.modal-empty,
.check-copy {
	font-size: 22rpx;
	color: #8b95a1;
	font-weight: 700;
}

.checkin {
	display: flex;
	flex-direction: column;
	align-items: center;
	text-align: center;
	padding-bottom: 20rpx;
}

.qrcode {
	width: 280rpx;
	height: 280rpx;
	border-radius: 18rpx;
	background: #f4f6f8;
}

.check-title {
	margin-top: 24rpx;
	font-size: 28rpx;
	font-weight: 900;
}

.check-copy {
	margin-top: 10rpx;
}

@keyframes flashBg {
	0%, 100% { background-color: #ffffff; }
	50% { background-color: #fff3cd; }
}

.activity-card.flash-once {
	animation: flashBg 0.6s ease-in-out 1;
}

.activity-card.flash-twice {
	animation: flashBg 0.6s ease-in-out 2;
}
</style>