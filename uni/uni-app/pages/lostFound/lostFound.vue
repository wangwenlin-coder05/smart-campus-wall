<template>
	<view class="page">
		<view class="top-panel">
			<view class="header">
				<text class="title">失物招领</text>
				<text class="subtitle">共 {{ stats.total }} 条，找回率 {{ stats.rate }}%</text>
			</view>

			<view class="filter-circles">
				<view
					v-for="filter in filters"
					:key="filter.value"
					class="filter-item"
					:class="{ active: activeFilter === filter.value }"
					@click="switchFilter(filter.value)"
				>
					<view class="circle-wrap">
						<view class="circle" :class="getCircleClass(filter.value)">
							<text class="circle-num">{{ filter.count }}</text>
						</view>
						<view v-if="filter.notifCount > 0" class="notif-dot">
							<text>{{ filter.notifCount > 99 ? '99+' : filter.notifCount }}</text>
						</view>
					</view>
					<text class="filter-label">{{ filter.label }}</text>
				</view>
			</view>

			<view class="rate-track">
				<text class="track-title">找回率</text>
				<view class="track-line">
					<view class="track-fill" :style="{ width: ratePercent }"></view>
					<view class="track-dot" :style="{ left: rateDotPosition }"></view>
				</view>
				<text class="track-value">{{ stats.rate }}%</text>
			</view>

			<view class="search-bar">
				<input
					v-model="keyword"
					class="search-input"
					placeholder="搜索物品、地点、姓名、手机号、学号"
					confirm-type="search"
					@confirm="handleSearch"
				/>
				<view class="search-btn" @click="handleSearch">
					<text>搜索</text>
				</view>
			</view>
		</view>

		<scroll-view
			class="list"
			scroll-y
			refresher-enabled
			:refresher-triggered="refreshing"
			:show-scrollbar="false"
			@refresherrefresh="refreshList"
		>
			<view v-for="item in list" :key="`${item.id}_${item.applicationId || 0}`" class="card">
				<view class="card-media">
					<image v-if="item.image" class="card-image" :src="item.image" mode="aspectFill"></image>
					<view v-else class="image-fallback" :class="item.type === 'lost' ? 'fallback-lost' : 'fallback-found'">
						<text>{{ item.type === 'lost' ? '寻物' : '招领' }}</text>
					</view>
				</view>

				<view class="card-body">
					<view class="card-header">
						<view class="badge-row">
				<text class="card-badge" :class="item.type === 'lost' ? 'badge-lost' : 'badge-found'">
					{{ item.type === 'lost' ? '寻物' : '招领' }}
				</text>
				<text v-if="shouldShowStatus(item)" class="card-status" :class="getStatusClass(item.status)">
					{{ getStatusText(item.status, item) }}
				</text>
			</view>
						<text class="views">{{ item.views || 0 }} 浏览</text>
					</view>

					<text class="card-title">{{ item.title }}</text>
					<text class="card-desc">{{ item.description || '暂无描述' }}</text>

					<view class="card-meta">
						<text class="meta-chip">地点 {{ item.location || '未填写' }}</text>
						<text class="meta-chip">时间 {{ formatTime(item.createTime) }}</text>
					</view>

					<view class="info-panel" v-if="canShowPublisherContact(item)">
						<view class="panel-title">发布方联系方式</view>
						<view class="contact-row">
							<text class="contact-label">姓名</text>
							<text class="contact-value">{{ item.publisherName || '-' }}</text>
						</view>
						<view class="contact-row" v-if="item.publisherPhone">
							<text class="contact-label">手机号</text>
							<text class="contact-value">{{ item.publisherPhone }}</text>
						</view>
						<view class="contact-row" v-if="item.publisherStudentId">
							<text class="contact-label">学号</text>
							<text class="contact-value">{{ item.publisherStudentId }}</text>
						</view>
					</view>

					<view class="info-panel application-panel" v-if="canShowApplication(item)">
						<view class="panel-title">{{ getApplicationTitle(item) }}</view>
						<view class="contact-row">
							<text class="contact-label">姓名</text>
							<text class="contact-value">{{ item.applicantName || '-' }}</text>
						</view>
						<view class="contact-row" v-if="item.applicantPhone">
							<text class="contact-label">手机号</text>
							<text class="contact-value">{{ item.applicantPhone }}</text>
						</view>
						<view class="contact-row" v-if="item.applicantStudentId">
							<text class="contact-label">学号</text>
							<text class="contact-value">{{ item.applicantStudentId }}</text>
						</view>
						<view class="contact-row" v-if="item.applyType === 'claim' && item.applicantIdCard">
							<text class="contact-label">身份证</text>
							<text class="contact-value">{{ item.applicantIdCard }}</text>
						</view>
					</view>

					<view class="complete-info" v-if="item.status === 1 || item.status === 3">
						<text>
							{{ item.status === 1 ? `已由 ${item.returnerName || '同学'} 归还` : `已由 ${item.claimerName || '同学'} 领取` }}
						</text>
						<text>{{ item.status === 1 ? item.returnTime : item.claimTime }}</text>
					</view>

					<view class="card-actions">
						<view class="action-btns" v-if="item.publisherUid === currentUid">
							<view
								v-if="item.applicationStatus === 'pending'"
								class="action-btn action-success"
								@click="handleApprove(item)"
							>
								同意
							</view>
							<view
								v-if="item.applicationStatus === 'pending'"
								class="action-btn action-danger"
								@click="handleReject(item)"
							>
								拒绝
							</view>
							<view
								v-if="item.applicationStatus === 'approved' && item.status === 0"
								class="action-btn action-primary"
								@click="handleStatusChange(item)"
							>
								{{ item.type === 'lost' ? '已归还' : '已领取' }}
							</view>
							<view class="action-btn action-danger-light" @click="handleDelete(item)">
								删除
							</view>
						</view>

						<view class="action-btns" v-else-if="item.applicantUid === currentUid">
							<view
								v-if="item.applicationStatus === 'rejected'"
								class="reject-notice"
							>
								<text class="reject-notice-text">您的申请已被拒绝</text>
							</view>
							<view
								v-if="item.applicationStatus === 'rejected'"
								class="action-btn action-reapply"
								@click="handleReapply(item)"
							>
								再次申请
							</view>
							<view
								v-else-if="item.applicationStatus"
								class="action-btn"
								:class="getApplicationClass(item.applicationStatus)"
							>
								{{ getApplicationText(item.applicationStatus) }}
							</view>
							<view
								v-else-if="item.applicationId"
								class="action-btn action-muted"
							>
								已申请
							</view>
							<view
								v-if="item.applicationStatus === 'approved' && item.status === 0"
								class="action-btn action-primary"
								@click="handleStatusChange(item)"
							>
								{{ item.type === 'lost' ? '已归还' : '已领取' }}
							</view>
						</view>

						<view class="action-btns" v-else>
							<view
								v-if="item.status === 0 && item.type === 'found' && !item.applicationStatus"
								class="action-btn action-claim"
								@click="handleClaim(item, 'claim')"
							>
								申请领取
							</view>
							<view
								v-if="item.status === 0 && item.type === 'lost' && !item.applicationStatus"
								class="action-btn action-return"
								@click="handleClaim(item, 'return')"
							>
								申请归还
							</view>
						</view>
					</view>
				</view>
			</view>

			<view class="loading" v-if="loading">
				<text>加载中...</text>
			</view>
			<view class="empty" v-else-if="list.length === 0">
				<text>暂无相关物品</text>
			</view>
			<view class="no-more" v-else>
				<text>已显示全部</text>
			</view>
		</scroll-view>

		<view class="fab" @click="goToPublish">
			<text class="fab-icon">+</text>
		</view>
	</view>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { onPullDownRefresh, onShow, onLoad } from '@dcloudio/uni-app'
import request from '@/utils/request.js'
import { getCurrentUser } from '@/utils/auth.js'

const filterLabels = {
	all: '全部',
	found_pending: '待领取',
	lost_pending: '待归还',
	completed: '已完成',
	my_published: '我发布',
	apply: '我申请'
}

const filters = ref(Object.keys(filterLabels).map(value => ({
	label: filterLabels[value],
	value,
	count: 0,
	notifCount: 0
})))

const activeFilter = ref('all')
const keyword = ref('')
const list = ref([])
const allItems = ref([])
const displaySourceItems = ref([])
const loading = ref(false)
const refreshing = ref(false)

const currentUid = computed(() => getCurrentUser()?.uid || '')

const stats = ref({
	total: 0,
	completed: 0,
	pending: 0,
	rate: 0
})

const ratePercent = computed(() => `${Math.min(100, Math.max(0, Number(stats.value.rate) || 0))}%`)
const rateDotPosition = computed(() => {
	const rate = Math.min(100, Math.max(0, Number(stats.value.rate) || 0))
	return `calc(${rate}% - 18rpx)`
})

const mockData = [
	{
		id: 1,
		type: 'lost',
		title: '白色 AirPods Pro 丢失',
		description: '周三下午在图书馆三楼自习室遗失，外壳上有星星贴纸。',
		location: '图书馆三楼',
		createTime: '2026-06-10 16:20',
		status: 0,
		views: 234,
		publisherUid: '1001',
		publisherName: '小陈同学',
		publisherPhone: '13800000001',
		publisherStudentId: '2021001001',
		image: '',
		applicantUid: '',
		applicationStatus: ''
	}
]

const normalizeItem = (item) => ({
	...item,
	status: Number(item.status ?? 0),
	views: Number(item.views ?? 0)
})

const requestList = async (kw = '') => {
	const res = await request({
		url: '/lost-found/list',
		method: 'POST',
		data: {
			type: 'all',
			keyword: kw,
			userUid: currentUid.value,
			pageNum: 1,
			pageSize: 200
		}
	})
	return res?.code === 1 && Array.isArray(res.data) ? res.data.map(normalizeItem) : mockData.map(normalizeItem)
}

const dedupeForSummary = (data) => {
	const map = new Map()
	data.forEach(item => {
		if (!map.has(item.id)) {
			map.set(item.id, item)
			return
		}
		const old = map.get(item.id)
		if (applicationPriority(item) > applicationPriority(old)) {
			map.set(item.id, item)
		}
	})
	return Array.from(map.values())
}

const applicationPriority = (item) => {
	if (!item.applicationId) return 0
	if (item.applicationStatus === 'pending') return 4
	if (item.applicationStatus === 'approved') return 3
	if (item.applicationStatus === 'completed') return 2
	return 1
}

const getCircleClass = (value) => {
	if (value === 'found_pending') return 'circle-blue'
	if (value === 'lost_pending') return 'circle-orange'
	if (value === 'completed') return 'circle-green'
	if (value === 'apply') return 'circle-purple'
	if (value === 'my_published') return 'circle-teal'
	return ''
}

const getStatusClass = (status) => {
	return Number(status) === 1 || Number(status) === 3 ? 'status-done' : 'status-pending'
}

const shouldShowStatus = (item) => {
	const status = Number(item.status)
	if (status === 0) {
		return Boolean(item.applicationId || item.applicantUid)
	}
	return true
}

const getStatusText = (status, item) => {
	const map = {
		0: '沟通中',
		1: '已归还',
		2: '待领取',
		3: '已领取'
	}
	return map[Number(status)] || '沟通中'
}

const getApplicationText = (status) => {
	const map = {
		pending: '待对方同意',
		approved: '对方已同意',
		rejected: '已被拒绝',
		completed: '已完成'
	}
	return map[status] || '已申请'
}

const getApplicationClass = (status) => {
	const map = {
		pending: 'action-warning',
		approved: 'action-success',
		rejected: 'action-danger',
		completed: 'action-success'
	}
	return map[status] || 'action-muted'
}

const getApplicationTitle = (item) => {
	if (item.publisherUid === currentUid.value) {
		return item.applyType === 'claim' ? '领取申请' : '归还申请'
	}
	return '我的申请'
}

const canShowPublisherContact = (item) => {
	return Boolean(item.publisherPhone || item.publisherStudentId)
}

const canShowApplication = (item) => {
	return Boolean(item.applicationId && (item.publisherUid === currentUid.value || item.applicantUid === currentUid.value))
}

const formatTime = (time) => {
	if (!time) return '未记录'
	return String(time).replace(/:\d{2}$/, '')
}

const matchFilter = (item, filter) => {
	if (filter === 'found_pending') return item.type === 'found' && item.status === 0
	if (filter === 'lost_pending') return item.type === 'lost' && item.status === 0
	if (filter === 'completed') return item.status === 1 || item.status === 3
	if (filter === 'my_published') return item.publisherUid === currentUid.value
	if (filter === 'apply') return item.applicantUid === currentUid.value
	return true
}

const updateFilterCounts = () => {
	const data = allItems.value
	const counts = {
		all: data.length,
		found_pending: data.filter(item => matchFilter(item, 'found_pending')).length,
		lost_pending: data.filter(item => matchFilter(item, 'lost_pending')).length,
		completed: data.filter(item => matchFilter(item, 'completed')).length,
		my_published: data.filter(item => matchFilter(item, 'my_published')).length,
		apply: data.filter(item => matchFilter(item, 'apply')).length
	}

	filters.value = filters.value.map(filter => ({
		...filter,
		count: counts[filter.value] || 0
	}))
}

const updateStatsFromItems = () => {
	const total = allItems.value.length
	const completed = allItems.value.filter(item => item.status === 1 || item.status === 3).length
	stats.value = {
		total,
		completed,
		pending: total - completed,
		rate: total > 0 ? Math.round((completed / total) * 100) : 0
	}
}

const applyLocalFilter = () => {
	const source = keyword.value.trim() ? displaySourceItems.value : allItems.value
	const filtered = source.filter(item => matchFilter(item, activeFilter.value))
	list.value = sortDisplayItems(filtered)
}

const sortDisplayItems = (items) => {
	return [...items].sort((a, b) => {
		const bucketDiff = getSortBucket(a) - getSortBucket(b)
		if (bucketDiff !== 0) return bucketDiff

		const timeDiff = getTimeValue(b.createTime) - getTimeValue(a.createTime)
		if (timeDiff !== 0) return timeDiff

		return Number(b.id || 0) - Number(a.id || 0)
	})
}

const isCompletedItem = (item) => {
	const status = Number(item.status)
	return status === 1 || status === 3 || item.applicationStatus === 'completed'
}

const getSortBucket = (item) => {
	if (isCompletedItem(item)) return 5

	const uid = currentUid.value
	if (uid && item.applicantUid === uid) return 0
	if (uid && item.publisherUid === uid) return 1

	const status = Number(item.status)
	if (status === 0 && !item.applicationStatus && !item.applicationId && !item.applicantUid) return 2
	if (status === 0) return 3
	return 4
}

const getTimeValue = (time) => {
	const value = Date.parse(String(time || '').replace(/-/g, '/'))
	return Number.isNaN(value) ? 0 : value
}

const fetchStats = async () => {
	try {
		const res = await request({
			url: '/lost-found/stats',
			method: 'GET'
		})
		if (res?.code === 1 && res.data) {
			const total = Number(res.data.total || 0)
			const completed = Number(res.data.resolved || 0)
			stats.value = {
				total,
				completed,
				pending: Math.max(0, total - completed),
				rate: total > 0 ? Math.round((completed / total) * 100) : 0
			}
		} else {
			updateStatsFromItems()
		}
	} catch (e) {
		updateStatsFromItems()
	}
}

const fetchList = async () => {
	if (loading.value) return
	loading.value = true

	try {
		const baseData = await requestList('')
		allItems.value = dedupeForSummary(baseData)
		updateFilterCounts()
		updateStatsFromItems()
		await fetchStats()
		await fetchNotifications()

		if (keyword.value.trim()) {
			displaySourceItems.value = await requestList(keyword.value.trim())
		} else {
			displaySourceItems.value = allItems.value
		}
		applyLocalFilter()
	} catch (e) {
		console.error('获取失物招领列表失败', e)
		allItems.value = mockData.map(normalizeItem)
		displaySourceItems.value = allItems.value
		updateFilterCounts()
		updateStatsFromItems()
		applyLocalFilter()
	} finally {
		loading.value = false
		refreshing.value = false
		uni.stopPullDownRefresh()
	}
}

const refreshList = () => {
	refreshing.value = true
	fetchList()
}

const fetchNotifications = async () => {
	if (!currentUid.value) return
	try {
		const res = await request({
			url: '/lost-found/notifications',
			method: 'POST',
			data: { userUid: currentUid.value }
		})
		if (res?.code === 1 && res.data) {
			const pub = Number(res.data.publishedPendingCount) || 0
			const app = Number(res.data.appliedChangedCount) || 0
			filters.value = filters.value.map(f => {
				if (f.value === 'my_published') return { ...f, notifCount: pub }
				if (f.value === 'apply') return { ...f, notifCount: app }
				return f
			})
		}
	} catch (e) {
		console.error('获取通知数量失败', e)
	}
}

const switchFilter = (value) => {
	activeFilter.value = value
	applyLocalFilter()
}

const handleSearch = () => {
	fetchList()
}

const goToPublish = () => {
	uni.navigateTo({ url: '/pages/lostFound/publishLostFound' })
}

const handleClaim = (item, type) => {
	uni.navigateTo({
		url: `/pages/lostFound/claim?itemId=${item.id}&applyType=${type}`
	})
}

const handleReapply = (item) => {
	const applyType = item.applyType || (item.type === 'found' ? 'claim' : 'return')
	uni.navigateTo({
		url: `/pages/lostFound/claim?itemId=${item.id}&applyType=${applyType}`
	})
}

const handleApprove = (item) => {
	handleApplicationOperate(item, '/lost-found/approve', '同意这条申请吗？', '已同意')
}

const handleReject = (item) => {
	handleApplicationOperate(item, '/lost-found/reject', '拒绝这条申请吗？', '已拒绝')
}

const handleApplicationOperate = (item, url, content, successTitle) => {
	uni.showModal({
		title: '确认操作',
		content,
		success: async (res) => {
			if (!res.confirm) return
			try {
				const applyRes = await request({
					url,
					method: 'POST',
					data: {
						applicationId: item.applicationId,
						userUid: currentUid.value
					}
				})
				if (applyRes?.code === 1) {
					uni.showToast({ title: successTitle, icon: 'success' })
					fetchList()
				} else {
					uni.showToast({ title: applyRes?.msg || '操作失败', icon: 'none' })
				}
			} catch (e) {
				uni.showToast({ title: '操作失败', icon: 'none' })
			}
		}
	})
}

const handleStatusChange = (item) => {
	if (!item.applicationId) {
		uni.showToast({ title: '请先同意一条申请', icon: 'none' })
		return
	}

	const statusText = item.type === 'lost' ? '已归还' : '已领取'
	uni.showModal({
		title: '确认操作',
		content: `确定将这条信息标记为${statusText}吗？`,
		success: async (res) => {
			if (!res.confirm) return
			try {
				const applyRes = await request({
					url: '/lost-found/complete',
					method: 'POST',
					data: {
						applicationId: item.applicationId,
						userUid: currentUid.value
					}
				})
				if (applyRes?.code === 1) {
					uni.showToast({ title: '状态已更新', icon: 'success' })
					fetchList()
				} else {
					uni.showToast({ title: applyRes?.msg || '操作失败', icon: 'none' })
				}
			} catch (e) {
				uni.showToast({ title: '操作失败', icon: 'none' })
			}
		}
	})
}

const handleDelete = (item) => {
	uni.showModal({
		title: '确认删除',
		content: '确定删除这条失物招领吗？',
		success: async (res) => {
			if (!res.confirm) return
			try {
				const delRes = await request({
					url: '/lost-found/delete',
					method: 'POST',
					data: {
						itemId: item.id,
						userUid: currentUid.value
					}
				})
				if (delRes?.code === 1) {
					uni.showToast({ title: '已删除', icon: 'success' })
					fetchList()
				} else {
					uni.showToast({ title: delRes?.msg || '删除失败', icon: 'none' })
				}
			} catch (e) {
				uni.showToast({ title: '删除失败', icon: 'none' })
			}
		}
	})
}

onMounted(fetchList)
onLoad((options) => {
	if (options?.filter) {
		activeFilter.value = options.filter
	}
})

onShow(fetchList)
onPullDownRefresh(refreshList)
</script>

<style scoped>
.page {
	height: 100vh;
	overflow: hidden;
	background: #f4f6f8;
}

.top-panel {
	position: fixed;
	top: 0;
	left: 0;
	right: 0;
	z-index: 100;
	padding-top: var(--status-bar-height);
	background: #ffffff;
	box-shadow: 0 8rpx 9rpx rgba(31, 38, 46, 0.08);
}

.header {
	display: flex;
	align-items: center;
	justify-content: space-between;
	height: 92rpx;
	padding: 0 28rpx;
	background: #3d7a72;
}

.title {
	font-size: 36rpx;
	font-weight: 700;
	color: #ffffff;
}

.subtitle {
	font-size: 24rpx;
	color: rgba(255, 255, 255, 0.86);
}

.filter-circles {
	display: grid;
	grid-template-columns: repeat(6, 1fr);
	gap: 6rpx;
	padding: 24rpx 18rpx 12rpx;
	background: #ffffff;
}

.filter-item {
	display: flex;
	flex-direction: column;
	align-items: center;
	gap: 8rpx;
	min-width: 0;
}

.circle {
	width: 74rpx;
	height: 74rpx;
	border-radius: 50%;
	background: #eef1f4;
	display: flex;
	align-items: center;
	justify-content: center;
	border: 3rpx solid transparent;
}

.circle-num {
	font-size: 26rpx;
	font-weight: 700;
	color: #48515a;
}

.circle-blue {
	background: #e5f1ff;
}

.circle-blue .circle-num {
	color: #1f78d1;
}

.circle-orange {
	background: #fff0df;
}

.circle-orange .circle-num {
	color: #c76a13;
}

.circle-green {
	background: #e7f5ed;
}

.circle-green .circle-num {
	color: #218a55;
}

.circle-purple {
	background: #f1e9ff;
}

.circle-purple .circle-num {
	color: #7b4acb;
}

.circle-teal {
	background: #e3f4f1;
}

.circle-teal .circle-num {
	color: #237d72;
}

.filter-item.active .circle {
	background: #3d7a72;
	border-color: #b9ded8;
}

.filter-item.active .circle-num {
	color: #ffffff;
}

.filter-item.active .filter-label {
	color: #2f665f;
	font-weight: 700;
}

.filter-label {
	max-width: 100%;
	font-size: 21rpx;
	color: #6f7984;
	white-space: nowrap;
}

.circle-wrap {
	position: relative;
	display: flex;
	align-items: center;
	justify-content: center;
}

.notif-dot {
	position: absolute;
	top: -6rpx;
	right: -10rpx;
	min-width: 32rpx;
	height: 32rpx;
	padding: 0 8rpx;
	border-radius: 20rpx;
	background: linear-gradient(135deg, #ff5252, #ff1744);
	display: flex;
	align-items: center;
	justify-content: center;
	box-shadow: 0 4rpx 12rpx rgba(255, 23, 68, 0.45);
	z-index: 10;
}

.notif-dot text {
	font-size: 18rpx;
	color: #ffffff;
	font-weight: 700;
	line-height: 1;
}

.rate-track {
	display: flex;
	align-items: center;
	gap: 16rpx;
	padding: 12rpx 34rpx 20rpx;
	background: #ffffff;
}

.track-title,
.track-value {
	flex-shrink: 0;
	font-size: 23rpx;
	color: #65717c;
}

.track-line {
	position: relative;
	flex: 1;
	height: 8rpx;
	border-radius: 8rpx;
	background: #e0e5e8;
}

.track-fill {
	height: 100%;
	border-radius: 8rpx;
	background: #42a878;
}

.track-dot {
	position: absolute;
	top: 50%;
	width: 36rpx;
	height: 36rpx;
	border-radius: 50%;
	background: #42a878;
	transform: translateY(-50%);
	box-shadow: 0 4rpx 14rpx rgba(66, 168, 120, 0.36);
}

.search-bar {
	display: flex;
	align-items: center;
	gap: 14rpx;
	padding: 14rpx 24rpx 20rpx;
	background: #ffffff;
}

.search-input {
	flex: 1;
	height: 72rpx;
	padding: 0 24rpx;
	background: #f1f4f6;
	border-radius: 10rpx;
	font-size: 27rpx;
	color: #1f2933;
}

.search-btn {
	height: 72rpx;
	min-width: 110rpx;
	padding: 0 24rpx;
	border-radius: 10rpx;
	background: #3d7a72;
	display: flex;
	align-items: center;
	justify-content: center;
	color: #ffffff;
	font-size: 27rpx;
	font-weight: 600;
}

.list {
	position: fixed;
	left: 0;
	right: 0;
	top: calc(var(--status-bar-height) + 410rpx);
	bottom: 0;
	height: auto;
	padding-top: 0;
	padding-bottom: 0rpx;
	box-sizing: border-box;
}

.card {
	display: flex;
	gap: 20rpx;
	margin: 20rpx 22rpx;
	padding: 20rpx;
	border-radius: 8rpx;
	background: #ffffff;
	box-shadow: 0 6rpx 20rpx rgba(31, 38, 46, 0.06);
}

.card-media {
	flex: 0 0 146rpx;
	width: 146rpx;
	height: 146rpx;
}

.card-image,
.image-fallback {
	width: 146rpx;
	height: 146rpx;
	border-radius: 8rpx;
}

.image-fallback {
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 28rpx;
	font-weight: 700;
}

.fallback-lost {
	background: #fff0df;
	color: #c76a13;
}

.fallback-found {
	background: #e5f1ff;
	color: #1f78d1;
}

.card-body {
	flex: 1;
	min-width: 0;
}

.card-header {
	display: flex;
	align-items: center;
	justify-content: space-between;
	gap: 12rpx;
	margin-bottom: 10rpx;
}

.badge-row {
	display: flex;
	align-items: center;
	gap: 8rpx;
	min-width: 0;
}

.card-badge,
.card-status {
	padding: 5rpx 12rpx;
	border-radius: 6rpx;
	font-size: 21rpx;
	font-weight: 600;
	white-space: nowrap;
}

.badge-lost {
	background: #fff0df;
	color: #c76a13;
}

.badge-found {
	background: #e5f1ff;
	color: #1f78d1;
}

.status-pending {
	background: #edf0f2;
	color: #63707b;
}

.status-done {
	background: #e7f5ed;
	color: #218a55;
}

.views {
	flex-shrink: 0;
	font-size: 21rpx;
	color: #89939e;
}

.card-title {
	display: block;
	margin-bottom: 8rpx;
	font-size: 30rpx;
	font-weight: 700;
	color: #1f2933;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}

.card-desc {
	display: -webkit-box;
	margin-bottom: 14rpx;
	overflow: hidden;
	color: #5f6b75;
	font-size: 24rpx;
	line-height: 1.45;
	-webkit-box-orient: vertical;
	-webkit-line-clamp: 2;
}

.card-meta {
	display: flex;
	flex-wrap: wrap;
	gap: 10rpx;
	margin-bottom: 14rpx;
}

.meta-chip {
	max-width: 100%;
	padding: 6rpx 12rpx;
	border-radius: 6rpx;
	background: #f2f5f7;
	color: #66727d;
	font-size: 21rpx;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}

.info-panel,
.complete-info {
	margin-bottom: 14rpx;
	padding: 14rpx;
	border-radius: 8rpx;
	background: #f7faf9;
	border: 1rpx solid #e0ebe8;
}

.application-panel {
	background: #fffaf2;
	border-color: #f1dfc4;
}

.panel-title {
	margin-bottom: 10rpx;
	font-size: 23rpx;
	font-weight: 700;
	color: #2d3842;
}

.contact-row,
.complete-info {
	display: flex;
	justify-content: space-between;
	gap: 12rpx;
}

.contact-row + .contact-row {
	margin-top: 8rpx;
}

.contact-label {
	flex: 0 0 90rpx;
	font-size: 23rpx;
	color: #6c7781;
}

.contact-value,
.complete-info text {
	flex: 1;
	font-size: 23rpx;
	color: #2d3842;
	text-align: right;
	word-break: break-all;
}

.card-actions {
	display: flex;
	justify-content: flex-end;
	padding-top: 14rpx;
	border-top: 1rpx solid #eef1f2;
}

.action-btns {
	display: flex;
	flex-wrap: wrap;
	justify-content: flex-end;
	gap: 10rpx;
}

.action-btn {
	min-height: 54rpx;
	padding: 0 22rpx;
	border-radius: 8rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	background: #eef1f2;
	color: #596670;
	font-size: 24rpx;
	font-weight: 600;
}

.action-primary {
	background: #3d7a72;
	color: #ffffff;
}

.action-claim {
	background: #1f78d1;
	color: #ffffff;
}

.action-return {
	background: #c76a13;
	color: #ffffff;
}

.action-success {
	background: #e7f5ed;
	color: #218a55;
}

.action-warning {
	background: #fff0df;
	color: #c76a13;
}

.action-danger,
.action-danger-light {
	background: #fbe8e8;
	color: #c93d3d;
}

.action-muted {
	background: #eef1f2;
	color: #65717c;
}

.reject-notice {
	padding: 10rpx 18rpx;
	border-radius: 8rpx;
	background: #fef0f0;
	border: 1rpx solid #fcd7d7;
	margin-bottom: 8rpx;
}

.reject-notice-text {
	font-size: 23rpx;
	color: #c93d3d;
	font-weight: 600;
}

.action-reapply {
	background: #3d7a72;
	color: #ffffff;
}

.loading,
.no-more,
.empty {
	padding: 42rpx 24rpx;
	text-align: center;
	font-size: 25rpx;
	color: #87919b;
}

.fab {
	position: fixed;
	right: 32rpx;
	bottom: 54rpx;
	z-index: 110;
	width: 96rpx;
	height: 96rpx;
	border-radius: 50%;
	background: #3d7a72;
	display: flex;
	align-items: center;
	justify-content: center;
	box-shadow: 0 10rpx 24rpx rgba(61, 122, 114, 0.32);
}

.fab-icon {
	font-size: 56rpx;
	color: #ffffff;
	font-weight: 300;
	line-height: 1;
}
</style>
