<template>
	<view class="page">
		<!-- 顶部导航栏 -->
		<view class="navbar" :style="{ paddingTop: statusBarHeight + 'px' }">
			<view class="nav-back" @click="goBack">
				<text class="icon">‹</text>
			</view>
			<view class="nav-title">
				<text class="title-text">跟TA的互动</text>
			</view>
		</view>

		<!-- 互动分类 tabs -->
		<view class="inter-tabs">
			<view
				v-for="t in interTabs"
				:key="t.key"
				class="inter-tab"
				:class="{ active: activeInterTab === t.key }"
				@click="activeInterTab = t.key"
			>{{ t.label }}</view>
		</view>

		<!-- 互动内容区 -->
		<scroll-view class="inter-content" scroll-y :show-scrollbar="false">
			<view v-if="interLoading" class="inter-loading">加载中...</view>
			<template v-else>
				<!-- 组局列表 -->
				<view v-if="activeInterTab === 'group'" class="inter-section">
					<view v-if="interGroups.length === 0" class="inter-empty">
						<text class="inter-empty-icon">📋</text>
						<text class="inter-empty-text">暂无组局互动</text>
						<text class="inter-empty-desc">你和TA还没有过组局相关的互动哦</text>
					</view>
					<view v-for="g in interGroups" :key="g.id" class="activity-card">
						<!-- 互动类型标签 -->
						<view class="inter-type-badge" :class="g.interType">
							<text>{{ g.interLabel }}</text>
						</view>
						<view class="main-row">
							<view class="poster-stack">
								<image class="poster-shadow poster-back" :src="safeImage(g.posterImg, defaultPoster)" mode="aspectFill"></image>
								<image class="poster" :src="safeImage(g.posterImg, defaultPoster)" mode="aspectFill"></image>
								<view class="poster-watermark">{{ g.tagName || '活动' }}</view>
							</view>

							<view class="content">
								<view class="organizer-row">
									<image :src="safeImage(g.brandAvatar, defaultAvatar)"
										class="brand-avatar" mode="aspectFill"
										@click.stop="openHostHome(g)"
										@error="$event.target.src=defaultAvatar" />
									<text class="brand-name" @click.stop="openHostHome(g)">{{ g.brandName || '主理人' }}</text>
									<view class="times-pill">组局 {{ g.groupCount || 1 }} 次</view>
								</view>

								<text class="title">{{ g.title }}</text>

								<view class="meta-row">
									<view class="meta-item meta-item-time">
										<view class="clock-icon"></view>
										<text>{{ g.activityTime }}</text>
									</view>
									<view class="meta-item meta-item-address">
										<view class="pin-icon"></view>
										<text>{{ g.address }}</text>
									</view>
								</view>

								<view class="info-chip-row">
									<view class="info-chip">{{ g.joinNum }}/{{ g.maxPeople }}人</view>
									<view class="info-chip">{{ g.genderLimit || '不限' }}</view>
									<view class="info-chip">{{ g.feeType || '免费' }}</view>
								</view>

								<view class="deposit-row">
									<text>{{ getDepositText(g) }}</text>
								</view>

								<view class="bottom-row">
									<view class="join-area">
										<view class="avatar-group">
											<image v-for="(avatar, avatarIndex) in (g.avatarList || []).slice(0, 3)" :key="avatarIndex" class="join-avatar"
												:src="safeImage(avatar, defaultAvatar)" mode="aspectFill"
												@error="$event.target.src=defaultAvatar"></image>
										</view>
										<text v-if="g.joinNum" class="join-count">{{ g.joinNum }} 人报名</text>
										<text v-else class="join-count">暂无报名</text>
									</view>

									<view class="btn-group">
										<view v-if="isSelfCreated(g)" class="inter-code-btn" @click="openCheckin(g)">
											<text>组局码</text>
										</view>
										<view v-else-if="hasJoined(g)" class="inter-code-btn" @click="openCheckin(g)">
											<text>组局码</text>
										</view>
										<view v-else class="inter-signup-btn" :class="{ disabled: g.joinNum >= g.maxPeople }" @click="handleSignup(g)">
											<text class="go-text">GO!</text>
											<text class="signup-text">去报名</text>
										</view>
									</view>
								</view>
							</view>
						</view>

						<view class="host-note" v-if="g.hostDesc">
							<text class="host-label">主理人说：</text>
							<text class="host-text">{{ g.hostDesc }}</text>
							<text class="quote-mark">"</text>
						</view>
					</view>
				</view>
				<!-- 帖子列表（后续扩展） -->
				<view v-if="activeInterTab === 'post'" class="inter-section">
					<view v-if="interPosts.length === 0" class="inter-empty">
						<text class="inter-empty-icon">📝</text>
						<text class="inter-empty-text">暂无帖子互动</text>
						<text class="inter-empty-desc">你和TA还没有过帖子相关的互动哦</text>
					</view>
					<view v-for="p in interPosts" :key="p.postNo || p.id" class="inter-post-card" @click="openInterPost(p)">
						<view class="ipc-header">
							<image class="ipc-avatar" :src="p.avatar || otherAvatar" mode="aspectFill"></image>
							<view class="ipc-user-info">
								<text class="ipc-nickname">{{ p.nickname || '用户' }}</text>
								<text class="ipc-time">{{ p.createTime || '' }}</text>
							</view>
							<text class="ipc-anonymous-tag" v-if="p.isAnonymous">匿名</text>
						</view>
						<text class="ipc-content">{{ p.content }}</text>
						<view v-if="p.mediaUrlList && p.mediaUrlList.length" class="ipc-media-row">
							<image v-for="(url, mi) in p.mediaUrlList.slice(0, 3)" :key="mi" :src="url" class="ipc-media-thumb" mode="aspectFill"></image>
							<view v-if="p.mediaUrlList.length > 3" class="ipc-media-more">+{{ p.mediaUrlList.length - 3 }}</view>
						</view>
						<view class="ipc-footer">
							<text class="ipc-stat">👁 {{ p.viewCount || 0 }}</text>
							<text class="ipc-stat">❤ {{ p.likeCount || 0 }}</text>
							<text class="ipc-stat">💬 {{ p.commentCount || 0 }}</text>
						</view>
					</view>
				</view>
			</template>
		</scroll-view>

		<!-- 组局码/报名弹窗 -->
		<view class="qrcode-mask" v-if="showQrcode" @click="closeQrcode">
			<view class="qrcode-modal" @click.stop>
				<view class="qrcode-header">
					<text class="qrcode-title">请扫码入群</text>
					<view class="close-btn" @click="closeQrcode">×</view>
				</view>

				<view class="qrcode-content">
					<image class="qrcode-img" :src="safeImage(qrCodeUrl, '/static/11/1.png')" mode="aspectFit"></image>

					<view class="activity-info">
						<text class="act-name">{{ currentActivity?.title }}</text>

						<view class="stats-row">
							<text class="stats-label">当前统计</text>
							<text class="stats-count">{{ currentActivity?.joinNum }}/{{ currentActivity?.maxPeople }}人</text>
						</view>

						<view class="button-group">
							<view v-if="!hasJoined(currentActivity)" class="confirm-btn" @click="onConfirmSignup">确认报名</view>
							<view v-else class="confirm-btn" @click="closeQrcode">我知道了</view>
							<view class="cancel-btn" @click="closeQrcode">关闭</view>
						</view>
					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import request from '@/utils/request.js'
import { buildUserHomeUrl } from '@/utils/messageCenter.js'
import { getCurrentUser } from '@/utils/auth.js'

const DEFAULT_AVATAR = 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'
const defaultPoster = '/static/organization/feature-card.jpg'
const defaultAvatar = '/static/default-avatar.png'

const statusBarHeight = ref(0)
const otherUserId = ref('')
const otherAvatar = ref(DEFAULT_AVATAR)
const currentUserId = ref('')

const interTabs = [
	{ key: 'group', label: '组局' },
	{ key: 'post', label: '帖子' }
]
const activeInterTab = ref('group')
const interLoading = ref(false)
const interGroups = ref([])
const interPosts = ref([])

const safeImage = (url, fallback) => {
	const value = String(url || '')
	const isBlobUrl = value.startsWith('blob:')
	return value && !isBlobUrl ? value : fallback
}

const getDepositText = (activity) => activity.depositRequired ? `报名押金 ¥${activity.depositAmount}` : '免报名押金'

// ========== 互动类型标签 ==========
const INTER_TYPE_MAP = {
	myCreatedOtherJoined: { label: '你发布 · 对方已报名', cls: 'my-created' },
	otherCreatedMyJoined: { label: '对方发布 · 你已报名', cls: 'other-created' },
	coJoined: { label: '共同报名', cls: 'co-joined' },
	coCollected: { label: '共同收藏', cls: 'co-collected' },
	sourceGreet: { label: '打招呼的组局', cls: 'source-greet' }
}

const sourceActivityId = ref('')
const sourceActivityType = ref('')

// 当前用户已报名的组局ID集合
const myJoinedIds = ref(new Set())
// 报名弹窗
const showQrcode = ref(false)
const qrCodeUrl = ref('')
const currentActivity = ref(null)

function goBack() {
	uni.navigateBack()
}

// ========== 数据加载 ==========
async function loadInteractionData() {
	if (!otherUserId.value || !currentUserId.value) return

	interLoading.value = true
	try {
		const [myRes, otherRes] = await Promise.all([
			request({ url: `/organization/my?userId=${currentUserId.value}`, method: 'GET' }).catch(() => ({ code: 0, data: {} })),
			request({ url: `/organization/my?userId=${otherUserId.value}`, method: 'GET' }).catch(() => ({ code: 0, data: {} }))
		])

		const myCreated = (myRes.code === 1 && myRes.data?.created) ? myRes.data.created : []
		const myJoined = (myRes.code === 1 && myRes.data?.joined) ? myRes.data.joined : []
		const otherCreated = (otherRes.code === 1 && otherRes.data?.created) ? otherRes.data.created : []
		const otherJoined = (otherRes.code === 1 && otherRes.data?.joined) ? otherRes.data.joined : []

		// 从已加载的数据中查找打招呼的源活动（数据更完整）
		let sourceActivity = null
		if (sourceActivityId.value && sourceActivityType.value === 'group') {
			const allLoaded = [...myCreated, ...myJoined, ...otherCreated, ...otherJoined]
			sourceActivity = allLoaded.find(a => String(a.id) === String(sourceActivityId.value)) || null
		}

		buildInteractions(myCreated, myJoined, otherCreated, otherJoined, sourceActivity)
	} catch (e) {
		console.error('加载互动数据失败', e)
		interGroups.value = []
	} finally {
		interLoading.value = false
	}
}

function buildInteractions(myCreated, myJoined, otherCreated, otherJoined, sourceActivity) {
	const result = []
	const seenIds = new Set()

	// 记录当前用户已报名的组局ID
	const joinedSet = new Set()
	myJoined.forEach(a => joinedSet.add(String(a.id)))
	myCreated.forEach(a => joinedSet.add(String(a.id)))
	myJoinedIds.value = joinedSet

	// 0. 打招呼的组局（聊天来源活动，最高优先级排在最前面）
	if (sourceActivity && !seenIds.has(String(sourceActivity.id))) {
		seenIds.add(String(sourceActivity.id))
		const isJoined = joinedSet.has(String(sourceActivity.id))
		result.push({
			...sourceActivity,
			interType: isJoined ? 'other-created' : 'source-greet',
			interLabel: isJoined ? '打招呼 · 已报名' : INTER_TYPE_MAP.sourceGreet.label
		})
	}

	// 1. 我发布 · 对方报名
	for (const act of myCreated) {
		const match = otherJoined.find(j => String(j.id) === String(act.id))
		if (match && !seenIds.has(String(act.id))) {
			seenIds.add(String(act.id))
			result.push({
				...act,
				interType: 'my-created',
				interLabel: INTER_TYPE_MAP.myCreatedOtherJoined.label
			})
		}
	}

	// 2. 对方发布 · 我报名
	for (const act of otherCreated) {
		const match = myJoined.find(j => String(j.id) === String(act.id))
		if (match && !seenIds.has(String(act.id))) {
			seenIds.add(String(act.id))
			result.push({
				...act,
				interType: 'other-created',
				interLabel: INTER_TYPE_MAP.otherCreatedMyJoined.label
			})
		}
	}

	// 3. 共同报名（都不是发布者，但双方都报名了）
	const myCreatedIds = new Set(myCreated.map(c => String(c.id)))
	const otherCreatedIds = new Set(otherCreated.map(c => String(c.id)))
	for (const act of myJoined) {
		if (seenIds.has(String(act.id))) continue
		if (myCreatedIds.has(String(act.id)) || otherCreatedIds.has(String(act.id))) continue
		const match = otherJoined.find(j => String(j.id) === String(act.id))
		if (match) {
			seenIds.add(String(act.id))
			result.push({
				...act,
				interType: 'co-joined',
				interLabel: INTER_TYPE_MAP.coJoined.label
			})
		}
	}

	// 4. 共同收藏（需要对方用户的收藏数据，当前仅检查我方收藏）
	const myCollected = getCollectedIds()
	const otherCollected = [] // TODO: 需要后端API获取对方用户的收藏列表
	if (myCollected.length && otherCollected.length) {
		const coCollectedIds = myCollected.filter(id => otherCollected.includes(id))
		for (const id of coCollectedIds) {
			if (seenIds.has(String(id))) continue
			seenIds.add(String(id))
			const found = [...myCreated, ...myJoined, ...otherCreated, ...otherJoined]
				.find(a => String(a.id) === String(id))
			if (found) {
				result.push({
					...found,
					interType: 'co-collected',
					interLabel: INTER_TYPE_MAP.coCollected.label
				})
			}
		}
	}

	interGroups.value = result
}

function getCollectedIds() {
	try {
		const stored = uni.getStorageSync('organizationCollectedIds') || []
		return Array.isArray(stored) ? stored : []
	} catch (e) {
		return []
	}
}

// ========== 导航 ==========
function openHostHome(group) {
	const hostId = group.creatorUserId || group.hostUserId || group.userId || group.publisherId
	if (!hostId) {
		uni.showToast({ title: '无法查看该用户主页', icon: 'none' })
		return
	}
	uni.navigateTo({
		url: buildUserHomeUrl({
			userId: hostId,
			name: group.brandName || '主理人',
			avatar: group.brandAvatar || otherAvatar.value
		})
	})
}

function openInterPost(post) {
	uni.navigateTo({ url: `/pages/wall/wall?postNo=${encodeURIComponent(post.postNo)}` })
}

// ========== 报名/组局码 ==========
function hasJoined(activity) {
	if (!activity) return false
	return myJoinedIds.value.has(String(activity.id))
}

function isSelfCreated(activity) {
	if (!activity) return false
	return String(activity.creatorUserId) === String(currentUserId.value)
}

function openCheckin(activity) {
	currentActivity.value = activity
	qrCodeUrl.value = activity.qrcodeUrl || '/static/11/1.png'
	showQrcode.value = true
}

function handleSignup(activity) {
	if (!activity) return
	if (activity.joinNum >= activity.maxPeople) {
		uni.showToast({ title: '该活动已满员', icon: 'none', duration: 1500 })
		return
	}
	if (hasJoined(activity)) {
		uni.showToast({ title: '您已经报名过该活动', icon: 'none', duration: 1500 })
		return
	}
	currentActivity.value = activity
	qrCodeUrl.value = activity.qrcodeUrl || '/static/11/1.png'
	showQrcode.value = true
}

function closeQrcode() {
	showQrcode.value = false
	currentActivity.value = null
	qrCodeUrl.value = ''
}

async function onConfirmSignup() {
	const activity = currentActivity.value
	if (!activity) return

	const user = getCurrentUser()
	if (!user || !user.uid) {
		uni.showToast({ title: '请先登录', icon: 'none' })
		return
	}

	if (hasJoined(activity)) {
		uni.showToast({ title: '您已经报名过该活动', icon: 'none', duration: 1500 })
		return
	}

	if (activity.joinNum >= activity.maxPeople) {
		uni.showToast({ title: '该活动已满员', icon: 'none', duration: 1500 })
		return
	}

	try {
		const res = await request({
			url: '/organization/join',
			method: 'POST',
			data: {
				activityId: activity.id,
				userId: user.uid,
				nickname: user.nickname || user.username || '报名同学',
				avatar: safeImage(user.avatar, defaultAvatar)
			}
		})
		if (res.code !== 1 || !res.data) {
			throw new Error(res.msg || '报名失败')
		}
		myJoinedIds.value.add(String(activity.id))
		const updated = res.data
		if (updated) {
			activity.joinNum = updated.joinNum ?? activity.joinNum
			activity.avatarList = updated.avatarList ?? activity.avatarList
		}
		uni.showToast({ title: '报名成功', icon: 'success', duration: 1500 })
		closeQrcode()
	} catch (e) {
		uni.showToast({ title: e.message || '报名失败', icon: 'none', duration: 1500 })
	}
}

onLoad((options) => {
	statusBarHeight.value = uni.getSystemInfoSync().statusBarHeight || 0
	otherUserId.value = options.otherUserId ? decodeURIComponent(options.otherUserId) : ''
	otherAvatar.value = options.otherAvatar ? decodeURIComponent(options.otherAvatar) : DEFAULT_AVATAR
	sourceActivityId.value = options.sourceId ? decodeURIComponent(options.sourceId) : ''
	sourceActivityType.value = options.sourceType ? decodeURIComponent(options.sourceType) : ''

	const user = getCurrentUser()
	currentUserId.value = user?.id || user?.uid || ''

	if (otherUserId.value && currentUserId.value) {
		loadInteractionData()
	}
})
</script>

<style scoped>
.page {
	width: 100%;
	min-height: 100vh;
	padding-top: var(--status-bar-height);
	background: #f5f6f9;
	display: flex;
	flex-direction: column;
}

/* ===== 导航栏 ===== */
.navbar {
	display: flex;
	align-items: center;
	padding: 12rpx 20rpx;
	background: #ffffff;
	flex-shrink: 0;
}

.nav-back {
	width: 60rpx;
	height: 60rpx;
	display: flex;
	align-items: center;
	justify-content: center;
}

.nav-back .icon {
	font-size: 48rpx;
	color: #1f2933;
	font-weight: 300;
	line-height: 1;
}

.nav-title {
	flex: 1;
	text-align: center;
	padding-right: 60rpx;
}

.title-text {
	font-size: 34rpx;
	font-weight: 800;
	color: #1f2933;
}

/* ===== 互动 tabs ===== */
.inter-tabs {
	display: flex;
	gap: 14rpx;
	padding: 20rpx 28rpx 18rpx;
	background: #ffffff;
	flex-shrink: 0;
}

.inter-tab {
	height: 56rpx;
	padding: 0 24rpx;
	border-radius: 28rpx;
	background: #f5f6f8;
	color: #6a7484;
	font-size: 26rpx;
	font-weight: 700;
	display: flex;
	align-items: center;
}

.inter-tab.active {
	background: #1f2933;
	color: #ffffff;
}

/* ===== 互动内容滚动区 ===== */
.inter-content {
	flex: 1;
	min-height: 0;
	padding: 20rpx 28rpx 28rpx;
	box-sizing: border-box;
}

.inter-loading {
	padding: 80rpx 0;
	text-align: center;
	color: #9aa3af;
	font-size: 26rpx;
}

.inter-empty {
	padding: 120rpx 0;
	display: flex;
	flex-direction: column;
	align-items: center;
	text-align: center;
}

.inter-empty-icon {
	font-size: 72rpx;
	margin-bottom: 24rpx;
}

.inter-empty-text {
	font-size: 30rpx;
	font-weight: 800;
	color: #1f2933;
	margin-bottom: 12rpx;
}

.inter-empty-desc {
	font-size: 24rpx;
	color: #9aa3af;
}

/* ===== 互动类型标签 ===== */
.inter-type-badge {
	position: absolute;
	top: 12rpx;
	right: 12rpx;
	z-index: 10;
	padding: 6rpx 16rpx;
	border-radius: 10rpx;
	font-size: 20rpx;
	font-weight: 800;
	white-space: nowrap;
}

.inter-type-badge.my-created {
	background: #e3f2fd;
	color: #1565c0;
}

.inter-type-badge.other-created {
	background: #fff3e0;
	color: #e65100;
}

.inter-type-badge.co-joined {
	background: #e8f5e9;
	color: #2e7d32;
}

.inter-type-badge.co-collected {
	background: #f3e5f5;
	color: #7b1fa2;
}

.inter-type-badge.source-greet {
	background: #e8f0fe;
	color: #1a56db;
}

/* ===== 组局卡片（搬运组局页样式） ===== */
.activity-card {
	width: 100%;
	border-radius: 24rpx;
	background: linear-gradient(180deg, #ffffff 0%, #f1f8ff 100%);
	overflow: hidden;
	margin-bottom: 20rpx;
	position: relative;
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

/* ===== 互动页按钮组 ===== */
.btn-group {
	display: flex;
	align-items: center;
	gap: 16rpx;
	flex-shrink: 0;
}

.inter-code-btn {
	width: 114rpx;
	height: 60rpx;
	border-radius: 13rpx;
	background: #ffffff;
	color: #222933;
	border: 2rpx solid #222933;
	font-size: 24rpx;
	font-weight: 900;
	display: flex;
	align-items: center;
	justify-content: center;
	flex-shrink: 0;
	box-sizing: border-box;
}

.inter-code-btn:active {
	transform: scale(0.96);
	opacity: 0.9;
}

.inter-signup-btn {
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
	transform: rotate(-1deg);
}

.inter-signup-btn:active {
	transform: rotate(-1deg) scale(0.96);
	opacity: 0.9;
}

.inter-signup-btn.disabled {
	opacity: 0.5;
	pointer-events: none;
}

.go-text {
	font-family: "Arial Black", Gadget, sans-serif;
	font-style: italic;
	font-size: 35rpx;
	line-height: 1;
	color: #ffd63c;
	-webkit-text-stroke: 3rpx #101418;
	font-weight: 1000;
	text-shadow: -2rpx 2rpx 0 #101418;
	transform: rotate(-10deg);
	position: absolute;
	left: -15rpx;
	top: -19rpx;
}

.signup-text {
	font-size: 25rpx;
	line-height: 1.1;
	font-weight: 900;
}

/* ===== 组局码/报名弹窗 ===== */
.qrcode-mask {
	position: fixed;
	top: 0;
	left: 0;
	right: 0;
	bottom: 0;
	background: rgba(0, 0, 0, 0.6);
	display: flex;
	align-items: center;
	justify-content: center;
	z-index: 999;
	backdrop-filter: blur(8rpx);
}

.qrcode-modal {
	width: 560rpx;
	background: linear-gradient(135deg, #ffffff 0%, #f8fbff 100%);
	border-radius: 32rpx;
	overflow: hidden;
	box-shadow: 0 32rpx 64rpx rgba(0, 0, 0, 0.2);
	animation: modalFadeIn 0.3s ease;
}

@keyframes modalFadeIn {
	from {
		opacity: 0;
		transform: scale(0.9);
	}
	to {
		opacity: 1;
		transform: scale(1);
	}
}

.qrcode-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 32rpx 32rpx 20rpx;
	border-bottom: 1rpx solid #eef2f8;
}

.qrcode-title {
	font-size: 34rpx;
	font-weight: 900;
	color: #17202a;
}

.close-btn {
	width: 48rpx;
	height: 48rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 48rpx;
	color: #9aa7b5;
}

.qrcode-content {
	padding: 40rpx 32rpx 48rpx;
	display: flex;
	flex-direction: column;
	align-items: center;
}

.qrcode-img {
	width: 300rpx;
	height: 300rpx;
	border-radius: 16rpx;
	background: #ffffff;
	box-shadow: 0 8rpx 20rpx rgba(0, 0, 0, 0.1);
}

.activity-info {
	margin-top: 32rpx;
	width: 100%;
	text-align: center;
}

.act-name {
	font-size: 28rpx;
	font-weight: 800;
	color: #1c2834;
	display: block;
	margin-bottom: 20rpx;
	line-height: 1.4;
}

.stats-row {
	background: #eef5fb;
	padding: 16rpx;
	border-radius: 16rpx;
	margin-bottom: 24rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	gap: 12rpx;
}

.stats-label {
	font-size: 26rpx;
	color: #314253;
	font-weight: 700;
}

.stats-count {
	font-size: 32rpx;
	color: #ff6b3d;
	font-weight: 900;
}

.button-group {
	display: flex;
	gap: 24rpx;
	margin-top: 16rpx;
}

.confirm-btn,
.cancel-btn {
	flex: 1;
	height: 80rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	border-radius: 44rpx;
	font-size: 28rpx;
	font-weight: 700;
}

.confirm-btn {
	background: linear-gradient(135deg, #222933 0%, #1a2129 100%);
	color: #ffffff;
}

.cancel-btn {
	background: #f2f6fc;
	color: #5a6e82;
	border: 1rpx solid #dce5ef;
}

.host-note {
	position: relative;
	padding: 22rpx 28rpx 24rpx;
	background: #e4f2ff;
	color: #6a7e91;
	font-size: 23rpx;
	line-height: 1.55;
}

.host-label {
	font-weight: 800;
	margin-right: 6rpx;
	color: #4a5c6e;
}

.host-text {
	word-break: break-all;
}

.quote-mark {
	position: absolute;
	right: 18rpx;
	top: 12rpx;
	font-size: 52rpx;
	color: #b8d6f0;
	line-height: 1;
	font-weight: 900;
}

/* ===== 帖子卡片（复用表白墙样式） ===== */
.inter-post-card {
	background: #fff;
	border-radius: 20rpx;
	padding: 24rpx;
	margin-bottom: 20rpx;
	border-top: 2rpx solid #e0e0e0;
	border-left: 4rpx solid #e0e0e0;
	border-right: 4rpx solid #e0e0e0;
	border-bottom: 2rpx solid #e0e0e0;
}

.inter-post-card:active {
	opacity: 0.95;
}

.ipc-header {
	display: flex;
	align-items: center;
	margin-bottom: 16rpx;
}

.ipc-avatar {
	width: 64rpx;
	height: 64rpx;
	border-radius: 50%;
	background: #e9edf2;
	margin-right: 14rpx;
	flex-shrink: 0;
}

.ipc-user-info {
	flex: 1;
	display: flex;
	flex-direction: column;
}

.ipc-nickname {
	font-size: 26rpx;
	font-weight: 700;
	color: #1a1d21;
}

.ipc-time {
	font-size: 22rpx;
	color: #9aa3af;
	margin-top: 4rpx;
}

.ipc-anonymous-tag {
	background: #d1d5db;
	color: #fff;
	font-size: 20rpx;
	padding: 2rpx 12rpx;
	border-radius: 8rpx;
	flex-shrink: 0;
}

.ipc-content {
	font-size: 26rpx;
	color: #333;
	line-height: 1.55;
	margin-bottom: 14rpx;
	display: -webkit-box;
	-webkit-line-clamp: 3;
	-webkit-box-orient: vertical;
	overflow: hidden;
}

.ipc-media-row {
	display: flex;
	gap: 8rpx;
	margin-bottom: 14rpx;
}

.ipc-media-thumb {
	width: 160rpx;
	height: 160rpx;
	border-radius: 10rpx;
	background: #e9edf2;
	flex-shrink: 0;
}

.ipc-media-more {
	width: 160rpx;
	height: 160rpx;
	border-radius: 10rpx;
	background: rgba(0, 0, 0, 0.35);
	color: #fff;
	font-size: 28rpx;
	font-weight: 700;
	display: flex;
	align-items: center;
	justify-content: center;
	flex-shrink: 0;
}

.ipc-footer {
	display: flex;
	gap: 28rpx;
}

.ipc-stat {
	font-size: 22rpx;
	color: #9aa3af;
}
</style>