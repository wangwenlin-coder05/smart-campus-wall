<template>
	<view class="page">
		<view class="detail-scroll">
			<view class="list-wrap" v-if="activity">
				<view class="activity-card">
					<view class="main-row">
						<view class="poster-stack">
							<image class="poster-shadow poster-back" :src="safeImage(activity.posterImg, defaultPoster)" mode="aspectFill"></image>
							<image class="poster" :src="safeImage(activity.posterImg, defaultPoster)" mode="aspectFill"></image>
							<view class="poster-watermark">{{ activity.tagName }}</view>
						</view>

						<view class="content">
							<view class="organizer-row">
								<image :src="safeImage(activity.brandAvatar, defaultAvatar)"
									class="brand-avatar" mode="aspectFill"
									@click.stop="openHostHome"
									@error="$event.target.src=defaultAvatar" />
								<text class="brand-name" @click.stop="openHostHome">{{ activity.brandName }}</text>
								<view class="times-pill">组局 {{ activity.groupCount }} 次</view>
								<view class="collect-icon" :class="{ active: activity.collected }"
									@click.stop="toggleCollect">
									{{ activity.collected ? '★' : '☆' }}
								</view>
							</view>

							<text class="title">{{ activity.title }}</text>

							<view class="meta-row">
								<view class="meta-item meta-item-time">
									<view class="clock-icon"></view>
									<text>{{ activity.activityTime }}</text>
								</view>
								<view class="meta-item meta-item-address">
									<view class="pin-icon"></view>
									<text>{{ activity.address }}</text>
								</view>
							</view>

							<view class="info-chip-row">
								<view class="info-chip">{{ activity.joinNum }}/{{ activity.maxPeople }}人</view>
								<view class="info-chip">{{ activity.genderLimit }}</view>
								<view class="info-chip">{{ activity.feeType }}</view>
							</view>

							<view class="deposit-row">
								<text>{{ getDepositText(activity) }}</text>
							</view>

							<view class="bottom-row">
								<view class="join-area">
									<view class="avatar-group">
										<image v-for="(avatar, avatarIndex) in (activity.avatarList || []).slice(0, 3)" :key="`${avatar}-${avatarIndex}`" class="join-avatar"
											:src="safeImage(avatar, defaultAvatar)" mode="aspectFill"
											@error="$event.target.src=defaultAvatar"></image>
									</view>
									<text v-if="activity.joinNum !== 0" class="join-count">{{ activity.joinNum }} 人报名</text>
									<text v-else class="join-count">暂无报名</text>
								</view>

								<view class="btn-group">
									<view class="code-btn" @click="showQrcode">
										<text class="see-text">SEE</text>
										<text class="code-label">组局码</text>
									</view>
								</view>
							</view>
						</view>
					</view>

					<view class="host-note">
						<text class="host-label">主理人说：</text>
						<text class="host-text">{{ activity.hostDesc }}</text>
						<text class="quote-mark">"</text>
					</view>
				</view>
			</view>

			<view class="loading-wrap" v-if="loading">
				<text>加载中...</text>
			</view>

			<view class="error-wrap" v-if="!loading && !activity">
				<text>组局不存在或已失效</text>
			</view>

			<view class="members-section" v-if="activity && allMembers.length > 0">
				<view class="section-title">报名成员 ({{ allMembers.length }})</view>
				<view class="member-list">
					<view class="member-item" v-for="m in allMembers" :key="m.id" @click="openMemberHome(m)">
						<image class="member-avatar" :src="safeImage(m.avatar, defaultAvatar)" mode="aspectFill"
							@error="$event.target.src=defaultAvatar"></image>
						<view class="member-info">
							<text class="member-name">{{ m.nickname }}</text>
							<text class="member-tag" v-if="m.isHost">主理人</text>
						</view>
					</view>
				</view>
			</view>
		</view>
	</view>

	<!-- 组局码弹窗 -->
	<view class="qrcode-overlay" v-if="qrcodeVisible" @click="hideQrcode">
		<view class="qrcode-card" @click.stop>
			<view class="qrcode-close" @click="hideQrcode">✕</view>
			<text class="qrcode-title">扫码入群</text>
			<image class="qrcode-image" :src="activity.qrcodeUrl" mode="aspectFit"></image>
			<text class="qrcode-tip">长按或截图保存二维码</text>
		</view>
	</view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import request from '@/utils/request.js'
import { getCurrentUser } from '@/utils/auth.js'
import { buildChatUrl, buildUserHomeUrl } from '@/utils/messageCenter.js'

const defaultPoster = '/static/organization/feature-card.jpg'
const defaultAvatar = '/static/default-avatar.png'

const isBlobUrl = url => String(url || '').startsWith('blob:')
const safeImage = (url, fallback) => {
	const value = String(url || '')
	return value && !isBlobUrl(value) ? value : fallback
}

const currentUserUid = computed(() => {
	const u = getCurrentUser()
	return u ? u.uid : ''
})

const activity = ref(null)
const members = ref([])
const loading = ref(true)
const activityId = ref('')
const isJoined = ref(false)
const collectedActivityIds = ref(new Set(uni.getStorageSync('organizationCollectedIds') || []))
const signedActivityIds = ref(new Set(uni.getStorageSync('signedIds') || []))
const qrcodeVisible = ref(false)

const allMembers = computed(() => {
	const list = [...members.value]
	if (activity.value) {
		const hostId = activity.value.creatorUserId || activity.value.hostUserId || activity.value.userId || activity.value.publisherId
		const hostName = activity.value.brandName || '主理人'
		const hostAvatar = safeImage(activity.value.brandAvatar, defaultAvatar)
		const alreadyHasHost = list.some(m => m.userId === hostId)
		if (!alreadyHasHost && hostId) {
			list.unshift({
				id: 'host_' + hostId,
				userId: hostId,
				nickname: hostName,
				avatar: hostAvatar,
				isHost: true,
				isSelf: hostId === currentUserUid.value
			})
		}
	}
	return list.map(m => ({
		...m,
		isSelf: m.userId === currentUserUid.value
	}))
})

const normalizeActivity = (item) => ({
	...item,
	groupCount: Math.max(1, Number(item.groupCount || 1)),
	joinNum: Number(item.joinNum || 0),
	maxPeople: Number(item.maxPeople || 2),
	depositRequired: item.depositRequired === true || item.depositRequired === 1,
	depositAmount: Number(item.depositAmount || 0),
	avatarList: item.avatarList && item.avatarList.length
		? item.avatarList.map(avatar => safeImage(avatar, defaultAvatar))
		: [],
	posterImg: safeImage(item.posterImg, defaultPoster),
	brandAvatar: safeImage(item.brandAvatar, defaultAvatar),
	qrcodeUrl: safeImage(item.qrcodeUrl, ''),
	collected: collectedActivityIds.value.has(item.id)
})

const getDepositText = (item) => item.depositRequired ? `报名押金 ¥${item.depositAmount}` : '免报名押金'

const loadDetail = async () => {
	if (!activityId.value) {
		loading.value = false
		return
	}
	loading.value = true
	try {
		const res = await request({
			url: `/organization/get/${activityId.value}`,
			method: 'GET'
		})
		if (res.code === 1 && res.data) {
			activity.value = normalizeActivity(res.data)
			isJoined.value = signedActivityIds.value.has(activity.value.id)
		} else {
			activity.value = null
		}
	} catch (e) {
		activity.value = null
	} finally {
		loading.value = false
	}
}

const loadMembers = async () => {
	try {
		const res = await request({
			url: `/organization/members/${activityId.value}`,
			method: 'GET'
		})
		if (res.code === 1 && res.data) {
			members.value = res.data
		}
	} catch (e) {
		members.value = []
	}
}

const handleJoin = async () => {
	if (!activity.value) return
	const user = getCurrentUser()
	if (!user || !user.uid) {
		uni.showToast({ title: '请先登录', icon: 'none' })
		return
	}
	if (isJoined.value) {
		uni.showToast({ title: '您已经报名过该活动', icon: 'none' })
		return
	}
	if (activity.value.joinNum >= activity.value.maxPeople) {
		uni.showToast({ title: '该活动已满员', icon: 'none' })
		return
	}
	try {
		const res = await request({
			url: '/organization/join',
			method: 'POST',
			data: {
				activityId: activity.value.id,
				userId: user.uid,
				nickname: user.nickname || user.username || '报名同学',
				avatar: safeImage(user.avatar, defaultAvatar)
			}
		})
		if (res.code !== 1 || !res.data) {
			throw new Error(res.msg || '报名失败')
		}
		activity.value = normalizeActivity(res.data)
		isJoined.value = true
		signedActivityIds.value.add(activity.value.id)
		uni.setStorageSync('signedIds', [...signedActivityIds.value])
		loadMembers()
		uni.showToast({ title: '报名成功', icon: 'success' })
	} catch (e) {
		uni.showToast({ title: '报名失败', icon: 'none' })
	}
}

const handleQuit = async () => {
	if (!activity.value) return
	const user = getCurrentUser()
	if (!user || !user.uid) return
	uni.showModal({
		title: '确认取消',
		content: '确定要取消报名吗？',
		success: async (res) => {
			if (!res.confirm) return
			try {
				const quitRes = await request({
					url: '/organization/quit',
					method: 'POST',
					data: {
						activityId: activity.value.id,
						userId: user.uid
					}
				})
				if (quitRes.code !== 1) {
					throw new Error(quitRes.msg || '取消失败')
				}
				isJoined.value = false
				signedActivityIds.value.delete(activity.value.id)
				uni.setStorageSync('signedIds', [...signedActivityIds.value])
				loadDetail()
				loadMembers()
				uni.showToast({ title: '已取消报名', icon: 'success' })
			} catch (e) {
				uni.showToast({ title: '取消失败', icon: 'none' })
			}
		}
	})
}

const toggleCollect = () => {
	if (!activity.value) return
	activity.value.collected = !activity.value.collected
	if (activity.value.collected) {
		collectedActivityIds.value.add(activity.value.id)
	} else {
		collectedActivityIds.value.delete(activity.value.id)
	}
	uni.setStorageSync('organizationCollectedIds', [...collectedActivityIds.value])
	uni.showToast({
		title: activity.value.collected ? '已收藏' : '已取消收藏',
		icon: 'none'
	})
}

const showQrcode = () => {
	if (!activity.value || !activity.value.qrcodeUrl) {
		uni.showToast({ title: '组局码暂未上传', icon: 'none' })
		return
	}
	qrcodeVisible.value = true
}

const hideQrcode = () => {
	qrcodeVisible.value = false
}

const greetHost = () => {
	if (!activity.value) return
	const hostId = activity.value.creatorUserId || activity.value.hostUserId || activity.value.userId || activity.value.publisherId
	const myId = currentUserUid.value
	const parts = [myId, hostId].filter(Boolean).sort()
	const convId = parts.length === 2 ? `friend_${parts[0]}_${parts[1]}` : `group_${activity.value.id}`
	uni.navigateTo({
		url: buildChatUrl({
			conversationId: convId,
			sourceType: 'group',
			sourceId: activity.value.id,
			name: activity.value.brandName || '主理人',
			avatar: safeImage(activity.value.brandAvatar, defaultAvatar),
			sourceTitle: activity.value.title || '组局活动',
			sourceDesc: `${activity.value.activityTime || ''}  ${activity.value.address || ''}`,
			targetUrl: `/pages/organization/organizationDetail?activityId=${encodeURIComponent(activity.value.id)}`
		})
	})
}

const openHostHome = () => {
	if (!activity.value) return
	const hostId = activity.value.creatorUserId || activity.value.hostUserId || activity.value.userId || activity.value.publisherId
	if (!hostId) {
		uni.showToast({ title: '无法查看该用户主页', icon: 'none' })
		return
	}
	uni.navigateTo({
		url: buildUserHomeUrl({
			userId: hostId,
			name: activity.value.brandName || '主理人',
			avatar: safeImage(activity.value.brandAvatar, defaultAvatar)
		})
	})
}

const openMemberHome = (member) => {
	if (!member || !member.userId) return
	uni.navigateTo({
		url: buildUserHomeUrl({
			userId: member.userId,
			name: member.nickname || '同学',
			avatar: safeImage(member.avatar, defaultAvatar)
		})
	})
}

const greetMember = (member) => {
	if (!member || !member.userId) return
	const myId = currentUserUid.value
	if (!myId) {
		uni.showToast({ title: '请先登录', icon: 'none' })
		return
	}
	const parts = [myId, member.userId].sort()
	const convId = `friend_${parts[0]}_${parts[1]}`
	uni.navigateTo({
		url: buildChatUrl({
			conversationId: convId,
			sourceType: 'chat',
			sourceId: member.userId,
			name: member.nickname || '同学',
			avatar: safeImage(member.avatar, defaultAvatar),
			sourceTitle: `${member.nickname || '同学'}的主页`,
			sourceDesc: `来自组局"${activity.value?.title || ''}"的打招呼`,
			targetUrl: `/pages/user/home?userId=${encodeURIComponent(member.userId)}&name=${encodeURIComponent(member.nickname || '')}&avatar=${encodeURIComponent(safeImage(member.avatar, defaultAvatar))}`
		})
	})
}

onLoad((options) => {
	if (options.activityId) {
		activityId.value = options.activityId
	}
})

onMounted(() => {
	loadDetail()
	loadMembers()
})
</script>

<style>
	page {
		background: #f5f5f5;
	}
</style>

<style scoped>
	.page {
		height: 100vh;
		box-sizing: border-box;
		background: linear-gradient(180deg, #eaf9fc 0%, #f7f7f7 28%, #f5f5f5 100%);
		color: #1f1a2b;
		display: flex;
		flex-direction: column;
		overflow: hidden;
	}

	.detail-scroll {
		flex: 1;
		height: auto;
		box-sizing: border-box;
		overflow-y: auto;
		padding: 18rpx 18rpx 0;
	}

	.list-wrap {
		width: 100%;
		margin-bottom: 30rpx;
	}

	.activity-card {
		width: 100%;
		border-radius: 24rpx;
		background: linear-gradient(180deg, #ffffff 0%, #f1f8ff 100%);
		overflow: hidden;
		box-shadow: 0 18rpx 42rpx rgba(70, 83, 104, 0.12), inset 0 1rpx 0 rgba(255, 255, 255, 0.96);
		border: 1rpx solid rgba(255, 255, 255, 0.9);
	}

	.main-row {
		min-height: 286rpx;
		padding: 24rpx 22rpx 18rpx;
		box-sizing: border-box;
		display: flex;
		position: relative;
		background:
			linear-gradient(90deg, rgba(255, 255, 255, 0.72), rgba(244, 250, 255, 0.28)),
			repeating-linear-gradient(135deg, rgba(222, 235, 246, 0.32) 0 20rpx, rgba(255, 255, 255, 0) 20rpx 42rpx);
	}

	.main-row::after {
		content: '';
		position: absolute;
		right: 16rpx;
		top: 18rpx;
		width: 150rpx;
		height: 58rpx;
		opacity: 0.42;
		background:
			linear-gradient(45deg, transparent 0 18rpx, rgba(213, 229, 242, 0.92) 18rpx 23rpx, transparent 23rpx 39rpx),
			linear-gradient(45deg, transparent 0 56rpx, rgba(213, 229, 242, 0.78) 56rpx 61rpx, transparent 61rpx 76rpx);
	}

	.poster-stack {
		width: 170rpx;
		height: 224rpx;
		margin: 6rpx 18rpx 0 0;
		position: relative;
		flex-shrink: 0;
	}

	.poster-shadow,
	.poster {
		position: absolute;
		left: 12rpx;
		top: 8rpx;
		width: 160rpx;
		height: 200rpx;
		border-radius: 18rpx;
		border: 4rpx solid #ffffff;
		transform: rotate(-5deg) translate(-12rpx, -6rpx);
	}

	.poster-back {
		transform: rotate(6deg) translate(-12rpx, -6rpx);
		opacity: 0.96;
	}

	.poster {
		z-index: 3;
		box-shadow: 0 10rpx 18rpx rgba(18, 35, 52, 0.18);
	}

	.poster-watermark {
		position: absolute;
		z-index: 5;
		left: 0;
		right: 0;
		bottom: -70rpx;
		height: 42rpx;
		color: rgba(222, 235, 246, 1);
		font-size: 38rpx;
		font-weight: 900;
		display: flex;
		align-items: center;
		justify-content: center;
		white-space: nowrap;
		overflow: hidden;
		text-overflow: ellipsis;
		letter-spacing: 0;
		pointer-events: none;
	}

	.content {
		flex: 1;
		min-width: 0;
		position: relative;
		z-index: 2;
	}

	.organizer-row {
		display: flex;
		align-items: center;
		margin-bottom: 14rpx;
		min-width: 0;
	}

	.brand-avatar {
		width: 34rpx;
		height: 34rpx;
		border-radius: 50%;
		background: #f6c23e;
		margin-right: 9rpx;
		flex-shrink: 0;
	}

	.brand-name {
		font-size: 25rpx;
		color: #1c2834;
		font-weight: 800;
		margin-right: 10rpx;
		white-space: nowrap;
		overflow: hidden;
		text-overflow: ellipsis;
		max-width: 190rpx;
	}

	.times-pill {
		height: 32rpx;
		padding: 0 12rpx;
		border-radius: 8rpx;
		background: #dfe9f5;
		color: #314253;
		font-size: 21rpx;
		font-weight: 800;
		display: flex;
		align-items: center;
		white-space: nowrap;
	}

	.collect-icon {
		margin-left: auto;
		width: 48rpx;
		height: 48rpx;
		border-radius: 50%;
		background: #ffffff;
		color: #9ba4b0;
		font-size: 34rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		box-shadow: 0 6rpx 18rpx rgba(38, 49, 66, 0.08);
	}

	.collect-icon.active {
		color: #ffb02e;
	}

	.title {
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

	.meta-row {
		display: flex;
		align-items: center;
		margin-bottom: 14rpx;
	}

	.meta-item {
		display: flex;
		align-items: center;
		margin-right: 16rpx;
		color: #7d8996;
		font-size: 22rpx;
		font-weight: 700;
		min-width: 0;
	}

	.meta-item text {
		white-space: nowrap;
		overflow: hidden;
		text-overflow: ellipsis;
		max-width: 180rpx;
	}

	.meta-item-time {
		flex-shrink: 0;
	}

	.meta-item-address {
		flex: 1;
		margin-right: 0;
		min-width: 0;
	}

	.meta-item-address text {
		max-width: 100%;
		white-space: normal;
		word-break: break-all;
		overflow: visible;
		text-overflow: clip;
		line-height: 1.45;
	}

	.clock-icon {
		width: 18rpx;
		height: 18rpx;
		border-radius: 50%;
		border: 4rpx solid #c3ccd6;
		margin-right: 5rpx;
		position: relative;
		box-sizing: border-box;
		flex-shrink: 0;
	}

	.clock-icon::after {
		content: '';
		position: absolute;
		left: 5rpx;
		top: 2rpx;
		width: 4rpx;
		height: 8rpx;
		background: #c3ccd6;
		border-radius: 99rpx;
	}

	.pin-icon {
		width: 16rpx;
		height: 16rpx;
		border-radius: 50% 50% 50% 0;
		background: #c3ccd6;
		transform: rotate(-45deg);
		margin-right: 5rpx;
		position: relative;
		flex-shrink: 0;
	}

	.pin-icon::after {
		content: '';
		position: absolute;
		width: 6rpx;
		height: 6rpx;
		border-radius: 50%;
		background: #eef7ff;
		left: 5rpx;
		top: 5rpx;
	}

	.info-chip-row {
		display: flex;
		gap: 8rpx;
		margin-bottom: 10rpx;
		flex-wrap: wrap;
	}

	.info-chip {
		height: 34rpx;
		padding: 0 12rpx;
		border-radius: 999rpx;
		background: #edf4fb;
		color: #43566b;
		font-size: 21rpx;
		font-weight: 800;
		display: flex;
		align-items: center;
	}

	.deposit-row {
		display: flex;
		justify-content: space-between;
		color: #91a0af;
		font-size: 21rpx;
		font-weight: 800;
		margin-bottom: 16rpx;
	}

	.bottom-row {
		display: flex;
		align-items: center;
		justify-content: space-between;
	}

	.btn-group {
		display: flex;
		align-items: center;
		gap: 16rpx;
		flex-shrink: 0;
	}

	.join-area {
		display: flex;
		align-items: center;
		min-width: 0;
	}

	.avatar-group {
		display: flex;
		align-items: center;
		margin-right: 12rpx;
	}

	.join-avatar {
		width: 38rpx;
		height: 38rpx;
		border-radius: 50%;
		border: 3rpx solid #ffffff;
		margin-left: -10rpx;
		background: #d7e0e8;
		box-shadow: 0 3rpx 8rpx rgba(27, 42, 57, 0.12);
	}

	.join-avatar:first-child {
		margin-left: 0;
	}

	.join-count {
		color: #6f7f8e;
		font-size: 23rpx;
		font-weight: 700;
		white-space: nowrap;
	}

	.code-btn {
		width: 114rpx;
		height: 60rpx;
		background: #222933;
		border-radius: 13rpx;
		color: #ffffff;
		position: relative;
		display: flex;
		align-items: center;
		justify-content: center;
		box-shadow: 0 8rpx 16rpx rgba(20, 30, 40, 0.2);
		flex-shrink: 0;
	}

	.code-btn:active {
		transform: scale(0.96);
		opacity: 0.9;
	}

	.see-text {
		font-family: "Arial Black", Gadget, sans-serif;
		font-style: italic;
		font-size: 35rpx;
		line-height: 1;
		color: #78e08f;
		-webkit-text-stroke: 3rpx #101418;
		font-weight: 1000;
		text-shadow: -2rpx 2rpx 0 #101418;
		transform: rotate(-10deg);
		position: absolute;
		left: -10rpx;
		top: -16rpx;
	}

	.code-label {
		font-size: 25rpx;
		line-height: 1.1;
		font-weight: 900;
	}

	.host-note {
		position: relative;
		padding: 20rpx 24rpx 22rpx;
		color: #405265;
		font-size: 24rpx;
		line-height: 1.42;
		background: linear-gradient(180deg, #f8fbfe 0%, #eef4fa 100%);
	}

	.host-label {
		font-weight: 900;
		color: #222933;
		margin-right: 6rpx;
		font-size: 24rpx;
	}

	.host-text {
		color: #475b6e;
		line-height: 1.5;
	}

	.quote-mark {
		position: absolute;
		right: 22rpx;
		bottom: 8rpx;
		font-size: 76rpx;
		color: rgba(55, 72, 92, 0.08);
		font-weight: 900;
		line-height: 1;
		pointer-events: none;
	}

	.flash-once {
		animation: flashCard 0.7s ease-in-out;
	}

	.flash-twice {
		animation: flashCard 0.7s ease-in-out 2;
	}

	@keyframes flashCard {
		0%, 100% { box-shadow: 0 18rpx 42rpx rgba(70, 83, 104, 0.12); }
		50% { box-shadow: 0 0 0 8rpx rgba(255, 183, 77, 0.42), 0 18rpx 42rpx rgba(70, 83, 104, 0.12); }
	}

	.empty-text,
	.end-text {
		text-align: center;
		color: #98a2b3;
		font-size: 24rpx;
		padding: 18rpx 0 28rpx;
	}

	.loading-wrap,
	.error-wrap {
		flex: 1;
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 28rpx;
		color: #98a2b3;
		padding: 80rpx 0;
	}

	.members-section {
		width: 100%;
		border-radius: 24rpx;
		background: #ffffff;
		padding: 24rpx;
		margin-bottom: 30rpx;
		box-shadow: 0 8rpx 24rpx rgba(70, 83, 104, 0.08);
	}

	.section-title {
		font-size: 28rpx;
		font-weight: 800;
		color: #1f2933;
		margin-bottom: 20rpx;
	}

	.member-list {
		display: flex;
		flex-wrap: wrap;
		gap: 20rpx;
	}

	.member-item {
		display: flex;
		align-items: center;
		gap: 12rpx;
	}

	.member-avatar {
		width: 56rpx;
		height: 56rpx;
		border-radius: 50%;
		background: #e0e0e0;
	}

	.member-info {
		display: flex;
		align-items: center;
		gap: 8rpx;
		flex: 1;
		min-width: 0;
	}

	.member-name {
		font-size: 26rpx;
		color: #314253;
		font-weight: 600;
	}

	.member-tag {
		height: 32rpx;
		padding: 0 10rpx;
		border-radius: 6rpx;
		background: #3d7a72;
		color: #ffffff;
		font-size: 20rpx;
		font-weight: 700;
		display: flex;
		align-items: center;
		white-space: nowrap;
	}

	.member-name {
		font-size: 26rpx;
		color: #314253;
		font-weight: 600;
	}

	.member-tag {
		height: 32rpx;
		padding: 0 10rpx;
		border-radius: 6rpx;
		background: #3d7a72;
		color: #ffffff;
		font-size: 20rpx;
		font-weight: 700;
		display: flex;
		align-items: center;
		white-space: nowrap;
	}

	/* ===== 组局码弹窗 ===== */
	.qrcode-overlay {
		position: fixed;
		inset: 0;
		background: rgba(0, 0, 0, 0.6);
		display: flex;
		align-items: center;
		justify-content: center;
		z-index: 999;
		backdrop-filter: blur(8rpx);
	}

	.qrcode-card {
		width: 560rpx;
		background: #ffffff;
		border-radius: 28rpx;
		padding: 40rpx 36rpx 32rpx;
		display: flex;
		flex-direction: column;
		align-items: center;
		position: relative;
		box-shadow: 0 16rpx 48rpx rgba(0, 0, 0, 0.15);
		animation: qrcodePop 0.3s ease;
	}

	@keyframes qrcodePop {
		from { transform: scale(0.85); opacity: 0; }
		to { transform: scale(1); opacity: 1; }
	}

	.qrcode-close {
		position: absolute;
		top: 20rpx;
		right: 24rpx;
		width: 48rpx;
		height: 48rpx;
		border-radius: 50%;
		background: #f2f3f5;
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 24rpx;
		color: #6a7484;
	}

	.qrcode-title {
		font-size: 34rpx;
		color: #1a1d21;
		font-weight: 700;
		margin-bottom: 28rpx;
	}

	.qrcode-image {
		width: 420rpx;
		height: 420rpx;
		border-radius: 16rpx;
		background: #f8f9fb;
	}

	.qrcode-tip {
		font-size: 24rpx;
		color: #8a94a6;
		margin-top: 20rpx;
	}
</style>