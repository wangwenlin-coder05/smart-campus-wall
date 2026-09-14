<template>
	<view class="page">
		<view class="navbar">
			<view class="nav-back" @click="goBack">
				<text class="icon">‹</text>
			</view>
			<text class="nav-title">物品详情</text>
		</view>

		<scroll-view class="content" scroll-y :show-scrollbar="false" v-if="item">
			<!-- 图片/类型占位 -->
			<view class="media-section">
				<image v-if="item.image" class="item-image" :src="item.image" mode="aspectFill"></image>
				<view v-else class="image-fallback" :class="item.type === 'lost' ? 'fallback-lost' : 'fallback-found'">
					<text class="fallback-icon">{{ item.type === 'lost' ? '📦' : '🎁' }}</text>
					<text>{{ item.type === 'lost' ? '寻物启事' : '失物招领' }}</text>
				</view>
			</view>

			<!-- 物品基本信息 -->
			<view class="info-card">
				<view class="badge-row">
					<text class="badge" :class="item.type === 'lost' ? 'badge-lost' : 'badge-found'">
						{{ item.type === 'lost' ? '寻物' : '招领' }}
					</text>
					<text class="status-tag" :class="getStatusClass(item.status)">
						{{ getStatusText(item.status) }}
					</text>
				</view>
				<text class="item-title">{{ item.title }}</text>
				<text class="item-desc">{{ item.description || '暂无描述' }}</text>
				<view class="meta-row">
					<text class="meta-chip">📍 {{ item.location || '未填写' }}</text>
					<text class="meta-chip">🕐 {{ formatTime(item.createTime) }}</text>
					<text class="meta-chip">👁 {{ item.views || 0 }} 浏览</text>
				</view>
			</view>

			<!-- 发布方信息 -->
			<view class="info-card">
				<view class="section-title">我的联系方式</view>
				<view class="publisher-header" @click="openPublisherHome">
					<image class="publisher-avatar" :src="item.publisherAvatar || defaultAvatar" mode="aspectFill"
						@error="$event.target.src=defaultAvatar"></image>
					<view class="publisher-name-wrap">
						<text class="publisher-name">{{ item.publisherName || '同学' }}</text>
						<text class="publisher-uid">ID: {{ item.publisherUid || '-' }}</text>
					</view>
					<text class="publisher-arrow">›</text>
				</view>
				<view class="info-row" v-if="item.publisherPhone">
					<text class="info-label">手机号</text>
					<text class="info-value">{{ item.publisherPhone }}</text>
				</view>
				<view class="info-row" v-if="item.publisherStudentId">
					<text class="info-label">学号</text>
					<text class="info-value">{{ item.publisherStudentId }}</text>
				</view>
			</view>

			<!-- 申请信息 -->
			<view class="info-card" v-if="canShowApplication">
				<view class="section-title">{{ getApplicationTitle }}</view>
				<view class="publisher-header" @click="openApplicantHome" v-if="item.applicantUid">
					<image class="publisher-avatar" :src="item.applicantAvatar || defaultAvatar" mode="aspectFill"
						@error="$event.target.src=defaultAvatar"></image>
					<view class="publisher-name-wrap">
						<text class="publisher-name">{{ item.applicantName || '同学' }}</text>
						<text class="publisher-uid">ID: {{ item.applicantUid }}</text>
					</view>
					<text class="publisher-arrow">›</text>
				</view>
				<view class="info-row" v-if="!item.applicantUid">
					<text class="info-label">姓名</text>
					<text class="info-value">{{ item.applicantName || '-' }}</text>
				</view>
				<view class="info-row" v-if="item.applicantPhone">
					<text class="info-label">手机号</text>
					<text class="info-value">{{ item.applicantPhone }}</text>
				</view>
				<view class="info-row" v-if="item.applicantStudentId">
					<text class="info-label">学号</text>
					<text class="info-value">{{ item.applicantStudentId }}</text>
				</view>
				<view class="info-row" v-if="item.applicantIdCard">
					<text class="info-label">身份证</text>
					<text class="info-value">{{ item.applicantIdCard }}</text>
				</view>
			</view>

			<!-- 完成信息 -->
			<view class="info-card" v-if="item.status === 1 || item.status === 3">
				<view class="section-title">完成信息</view>
				<view class="info-row">
					<text class="info-label">{{ item.status === 1 ? '归还人' : '领取人' }}</text>
					<text class="info-value">{{ item.status === 1 ? (item.returnerName || '-') : (item.claimerName || '-') }}</text>
				</view>
				<view class="info-row">
					<text class="info-label">{{ item.status === 1 ? '归还时间' : '领取时间' }}</text>
					<text class="info-value">{{ item.status === 1 ? formatTime(item.returnTime) : formatTime(item.claimTime) }}</text>
				</view>
			</view>

			<!-- 操作按钮区域 -->
			<view class="actions-section">
				<!-- 我是发布人 -->
				<template v-if="item.publisherUid === currentUid">
					<view class="action-label">操作</view>
					<view class="action-btns">
						<view
							v-if="item.applicationStatus === 'pending'"
							class="action-btn action-success"
							@click="handleApprove"
						>
							同意申请
						</view>
						<view
							v-if="item.applicationStatus === 'pending'"
							class="action-btn action-danger"
							@click="handleReject"
						>
							拒绝申请
						</view>
						<view
							v-if="item.applicationStatus === 'approved' && item.status === 0"
							class="action-btn action-primary"
							@click="handleComplete"
						>
							{{ item.type === 'lost' ? '标记为已归还' : '标记为已领取' }}
						</view>
						<view class="action-btn action-danger-light" @click="handleDelete">
							删除
						</view>
					</view>
				</template>

				<!-- 我是申请人 -->
				<template v-else-if="item.applicantUid === currentUid">
					<view class="action-label">我的申请</view>
					<view class="action-btns">
						<view
							v-if="item.applicationStatus === 'rejected'"
							class="reject-notice"
						>
							<text class="reject-notice-text">您的申请已被拒绝</text>
						</view>
						<view
							v-if="item.applicationStatus === 'rejected'"
							class="action-btn action-reapply"
							@click="handleReapply"
						>
							再次申请
						</view>
						<view
							v-else-if="item.applicationStatus"
							class="status-text"
							:class="getApplicationClass(item.applicationStatus)"
						>
							{{ getApplicationText(item.applicationStatus) }}
						</view>
						<view
							v-if="item.applicationStatus === 'approved' && item.status === 0"
							class="action-btn action-primary"
							@click="handleComplete"
						>
							{{ item.type === 'lost' ? '标记为已归还' : '标记为已领取' }}
						</view>
					</view>
				</template>

				<!-- 非发布人非申请人 -->
				<template v-else>
					<view class="action-btns">
						<view
							v-if="item.status === 0 && item.type === 'found' && !item.applicationStatus"
							class="action-btn action-claim"
							@click="handleClaim('claim')"
						>
							申请领取
						</view>
						<view
							v-if="item.status === 0 && item.type === 'lost' && !item.applicationStatus"
							class="action-btn action-return"
							@click="handleClaim('return')"
						>
							申请归还
						</view>
					</view>
				</template>
			</view>

			<view class="bottom-spacer"></view>
		</scroll-view>

		<view class="loading-wrap" v-else-if="loading">
			<text>加载中...</text>
		</view>
		<view class="error-wrap" v-else>
			<text>物品不存在或已删除</text>
		</view>
	</view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import request from '@/utils/request.js'
import { getCurrentUser } from '@/utils/auth.js'
import { buildUserHomeUrl } from '@/utils/messageCenter.js'

const defaultAvatar = 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'

const itemId = ref('')
const item = ref(null)
const loading = ref(true)

const currentUid = computed(() => getCurrentUser()?.uid || '')

const goBack = () => uni.navigateBack()

const formatTime = (time) => {
	if (!time) return '未记录'
	return String(time).replace(/:\d{2}$/, '')
}

const getStatusClass = (status) => {
	const s = Number(status)
	if (s === 1 || s === 3) return 'status-done'
	if (s === 0) return 'status-pending'
	return ''
}

const getStatusText = (status) => {
	const map = { 0: '待处理', 1: '已归还', 2: '待领取', 3: '已领取' }
	return map[Number(status)] || '待处理'
}

const canShowApplication = computed(() => {
	if (!item.value) return false
	return Boolean(item.value.applicationId &&
		(item.value.publisherUid === currentUid.value || item.value.applicantUid === currentUid.value))
})

const getApplicationTitle = computed(() => {
	if (!item.value) return ''
	if (item.value.publisherUid === currentUid.value) {
		return item.value.applyType === 'claim' ? '领取申请' : '归还申请'
	}
	return '我的申请'
})

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
		pending: 'status-warning',
		approved: 'status-success',
		rejected: 'status-danger',
		completed: 'status-success'
	}
	return map[status] || ''
}

const openPublisherHome = () => {
	if (!item.value || !item.value.publisherUid) return
	uni.navigateTo({
		url: buildUserHomeUrl({
			userId: item.value.publisherUid,
			name: item.value.publisherName || '同学',
			avatar: item.value.publisherAvatar || defaultAvatar
		})
	})
}

const openApplicantHome = () => {
	if (!item.value || !item.value.applicantUid) return
	uni.navigateTo({
		url: buildUserHomeUrl({
			userId: item.value.applicantUid,
			name: item.value.applicantName || '同学',
			avatar: item.value.applicantAvatar || defaultAvatar
		})
	})
}

const loadItem = async () => {
	if (!itemId.value) {
		loading.value = false
		return
	}
	loading.value = true
	try {
		const res = await request({
			url: `/lost-found/get/${itemId.value}`,
			method: 'GET',
			data: { userUid: currentUid.value }
		})
		if (res.code === 1 && res.data) {
			item.value = {
				...res.data,
				status: Number(res.data.status ?? 0),
				views: Number(res.data.views ?? 0)
			}
		} else {
			item.value = null
		}
	} catch (e) {
		item.value = null
	} finally {
		loading.value = false
	}
}

const handleClaim = (type) => {
	uni.navigateTo({
		url: `/pages/lostFound/claim?itemId=${itemId.value}&applyType=${type}`
	})
}

const handleReapply = () => {
	const applyType = item.value?.applyType || (item.value?.type === 'found' ? 'claim' : 'return')
	uni.navigateTo({
		url: `/pages/lostFound/claim?itemId=${itemId.value}&applyType=${applyType}`
	})
}

const handleApprove = () => {
	handleApplicationOperate('/lost-found/approve', '同意这条申请吗？', '已同意')
}

const handleReject = () => {
	handleApplicationOperate('/lost-found/reject', '拒绝这条申请吗？', '已拒绝')
}

const handleApplicationOperate = (url, content, successTitle) => {
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
						applicationId: item.value.applicationId,
						userUid: currentUid.value
					}
				})
				if (applyRes?.code === 1) {
					uni.showToast({ title: successTitle, icon: 'success' })
					loadItem()
				} else {
					uni.showToast({ title: applyRes?.msg || '操作失败', icon: 'none' })
				}
			} catch (e) {
				uni.showToast({ title: '操作失败', icon: 'none' })
			}
		}
	})
}

const handleComplete = () => {
	if (!item.value?.applicationId) {
		uni.showToast({ title: '请先同意一条申请', icon: 'none' })
		return
	}
	const statusText = item.value.type === 'lost' ? '已归还' : '已领取'
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
						applicationId: item.value.applicationId,
						userUid: currentUid.value
					}
				})
				if (applyRes?.code === 1) {
					uni.showToast({ title: '状态已更新', icon: 'success' })
					loadItem()
				} else {
					uni.showToast({ title: applyRes?.msg || '操作失败', icon: 'none' })
				}
			} catch (e) {
				uni.showToast({ title: '操作失败', icon: 'none' })
			}
		}
	})
}

const handleDelete = () => {
	uni.showModal({
		title: '确认删除',
		content: '确定删除这条失物招领吗？',
		success: async (res) => {
			if (!res.confirm) return
			try {
				const delRes = await request({
					url: '/lost-found/delete',
					method: 'POST',
					data: { itemId: Number(itemId.value), userUid: currentUid.value }
				})
				if (delRes?.code === 1) {
					uni.showToast({ title: '已删除', icon: 'success' })
					setTimeout(() => uni.navigateBack(), 800)
				} else {
					uni.showToast({ title: delRes?.msg || '删除失败', icon: 'none' })
				}
			} catch (e) {
				uni.showToast({ title: '操作失败', icon: 'none' })
			}
		}
	})
}

onLoad((query) => {
	itemId.value = query.id || ''
	loadItem()
})
</script>

<style scoped>
.page {
	min-height: 100vh;
	background: #f5f6f9;
	display: flex;
	flex-direction: column;
}

.navbar {
	display: flex;
	align-items: center;
	padding: 24rpx 28rpx;
	background: #ffffff;
	flex-shrink: 0;
}

.nav-back {
	width: 64rpx;
	height: 64rpx;
	display: flex;
	align-items: center;
	justify-content: center;
}

.icon {
	font-size: 48rpx;
	color: #333;
}

.nav-title {
	flex: 1;
	text-align: center;
	font-size: 32rpx;
	font-weight: 700;
	color: #1a1d21;
	margin-right: 64rpx;
}

.content {
	flex: 1;
	padding: 24rpx 28rpx;
}

.media-section {
	width: 100%;
	height: 400rpx;
	border-radius: 16rpx;
	overflow: hidden;
	margin-bottom: 24rpx;
}

.item-image {
	width: 100%;
	height: 100%;
}

.image-fallback {
	width: 100%;
	height: 100%;
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	gap: 12rpx;
	font-size: 28rpx;
	color: #ffffff;
}

.fallback-lost {
	background: linear-gradient(135deg, #f5a623, #f76b1c);
}

.fallback-found {
	background: linear-gradient(135deg, #3d7a72, #2b5f56);
}

.fallback-icon {
	font-size: 72rpx;
}

.info-card {
	background: #ffffff;
	border-radius: 16rpx;
	padding: 28rpx;
	margin-bottom: 24rpx;
}

.badge-row {
	display: flex;
	align-items: center;
	gap: 16rpx;
	margin-bottom: 16rpx;
}

.badge {
	font-size: 22rpx;
	padding: 6rpx 16rpx;
	border-radius: 8rpx;
}

.badge-lost {
	background: #fff3e0;
	color: #e65100;
}

.badge-found {
	background: #e0f2f1;
	color: #00695c;
}

.status-tag {
	font-size: 22rpx;
	padding: 6rpx 16rpx;
	border-radius: 8rpx;
}

.status-pending {
	background: #fff8e1;
	color: #f57f17;
}

.status-done {
	background: #e8f5e9;
	color: #2e7d32;
}

.item-title {
	display: block;
	font-size: 34rpx;
	font-weight: 700;
	color: #1f2933;
	margin-bottom: 12rpx;
	line-height: 1.4;
}

.item-desc {
	display: block;
	font-size: 28rpx;
	color: #667085;
	line-height: 1.6;
	margin-bottom: 16rpx;
}

.meta-row {
	display: flex;
	flex-wrap: wrap;
	gap: 12rpx;
}

.meta-chip {
	font-size: 24rpx;
	color: #98a2b3;
	background: #f5f6f8;
	padding: 8rpx 16rpx;
	border-radius: 8rpx;
}

.section-title {
	font-size: 28rpx;
	font-weight: 700;
	color: #1f2933;
	margin-bottom: 20rpx;
	padding-bottom: 16rpx;
	border-bottom: 1rpx solid #f0f0f0;
}

.publisher-header {
	display: flex;
	align-items: center;
	gap: 16rpx;
	padding: 8rpx 0 16rpx;
}

.publisher-avatar {
	width: 72rpx;
	height: 72rpx;
	border-radius: 50%;
	background: #e9edf2;
	flex-shrink: 0;
}

.publisher-name-wrap {
	flex: 1;
	min-width: 0;
}

.publisher-name {
	font-size: 28rpx;
	font-weight: 700;
	color: #1f2933;
	display: block;
}

.publisher-uid {
	font-size: 22rpx;
	color: #999;
	display: block;
	margin-top: 4rpx;
}

.publisher-arrow {
	font-size: 36rpx;
	color: #c0c4cc;
	flex-shrink: 0;
}

.info-row {
	display: flex;
	align-items: center;
	padding: 12rpx 0;
}

.info-label {
	font-size: 26rpx;
	color: #98a2b3;
	width: 120rpx;
	flex-shrink: 0;
}

.info-value {
	font-size: 28rpx;
	color: #1f2933;
	flex: 1;
}

.actions-section {
	background: #ffffff;
	border-radius: 16rpx;
	padding: 28rpx;
	margin-bottom: 24rpx;
}

.action-label {
	font-size: 28rpx;
	font-weight: 700;
	color: #1f2933;
	margin-bottom: 20rpx;
}

.action-btns {
	display: flex;
	flex-wrap: wrap;
	gap: 16rpx;
}

.action-btn {
	padding: 20rpx 36rpx;
	border-radius: 12rpx;
	font-size: 28rpx;
	font-weight: 600;
	text-align: center;
}

.action-success {
	background: #e8f5e9;
	color: #2e7d32;
}

.action-danger {
	background: #fce4ec;
	color: #c62828;
}

.action-danger-light {
	background: #f5f5f5;
	color: #999;
}

.action-primary {
	background: #e3f2fd;
	color: #1565c0;
}

.action-claim {
	background: #3d7a72;
	color: #ffffff;
	flex: 1;
}

.action-return {
	background: #f5a623;
	color: #ffffff;
	flex: 1;
}

.action-reapply {
	background: #3d7a72;
	color: #ffffff;
}

.reject-notice {
	width: 100%;
	padding: 16rpx 24rpx;
	background: #fff3e0;
	border-radius: 12rpx;
	margin-bottom: 12rpx;
}

.reject-notice-text {
	font-size: 26rpx;
	color: #e65100;
}

.status-text {
	padding: 20rpx 36rpx;
	border-radius: 12rpx;
	font-size: 28rpx;
	font-weight: 600;
}

.status-warning {
	background: #fff8e1;
	color: #f57f17;
}

.status-success {
	background: #e8f5e9;
	color: #2e7d32;
}

.status-danger {
	background: #fce4ec;
	color: #c62828;
}

.bottom-spacer {
	height: 40rpx;
}

.loading-wrap,
.error-wrap {
	flex: 1;
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 28rpx;
	color: #98a2b3;
}
</style>