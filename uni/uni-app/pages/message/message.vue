<template>
	<view class="page">
		<!-- 顶部固定区域 -->
		<view class="header-fixed" :style="{ height: headerHeight }">
			<!-- 顶部导航栏 -->
			<view class="navbar">
				<view class="nav-back" @click="goBack">
					<text class="icon">‹</text>
				</view>
				<view class="nav-title">
					<text class="title-text">{{ chatTitle }}</text>
					<text class="status-text" :class="{ online: connected }" v-if="!isSystemType">
						{{ connected ? `在线 ${onlineCount} 人` : connectionText }}
					</text>
				</view>
				<view class="interaction-entry" @click="openInteractionPanel" v-if="!isSystemType">互动</view>
			</view>

			<!-- 来源信息卡片 -->
			<view class="source-card" v-if="!isSystemType && sourceInfo" @click="openSource">
				<view class="source-tag">{{ sourceTag }}</view>
				<view class="source-content">
					<view class="source-title">{{ sourceInfo.title }}</view>
					<view class="source-desc" v-if="sourceDesc || sourceInfo.desc">{{ sourceDesc || sourceInfo.desc }}</view>
				</view>
			</view>
		</view>

		<!-- 系统消息：通知卡片列表 -->
		<scroll-view
			v-if="isSystemType"
			class="message-list"
			scroll-y
			:scroll-top="scrollTop"
			:show-scrollbar="false"
			:enhanced="true"
			:style="{ paddingTop: headerHeight, paddingBottom: '24rpx' }"
		>
			<!-- 连接状态提示 -->
			<view class="conn-status" :class="{ offline: !wsConnected }">
				<view class="conn-dot"></view>
				<text>{{ wsConnected ? '实时接收中 ---- 最新消息▼' : '等待连接...' }}</text>
			</view>

			<view v-if="systemNotices.length === 0" class="empty-tip">
				<text class="empty-icon">🔔</text>
				<text class="empty-title">{{ wsConnected ? '暂无系统通知' : '后端连接失败，请检查网络' }}</text>
				<text class="empty-desc" v-if="!wsConnected">消息通知需要后端服务正常运行</text>
			</view>

			<view
				v-for="(notice, idx) in systemNotices"
				:key="notice.id"
				class="notice-card"
				@click="openNoticeTarget(notice)"
			>
 				<!-- <view class="notice-avatar">系</view> -->
				<view class="notice-body">
				<view class="notice-header">
					<text class="notice-title">{{ notice.title }}</text>
					<text class="notice-time">{{ notice.time }}</text>
				</view>
				<text class="notice-desc">{{ notice.lastMsg }}</text>
				<view class="notice-footer" v-if="notice.desc">
					<text class="notice-extra">{{ notice.desc }}</text>
				</view>
				<view class="notice-arrow" v-if="notice.targetUrl || notice.orderNo">
					<text>查看详情</text>
					<text class="arrow">›</text>
				</view>
				</view>
			</view>
		</scroll-view>

		<!-- 聊天消息列表 -->
		<scroll-view
			v-else
			class="message-list"
			scroll-y
			:scroll-top="scrollTop"
			:scroll-into-view="scrollIntoView"
			:scroll-with-animation="scrollWithAnimation"
			:show-scrollbar="false"
			:enhanced="true"
			:style="messageListStyle"
		>
			<!-- 顶部安全间距 -->
			<view class="top-safe-space"></view>
			
			<view v-if="messageList.length === 0" class="empty-tip">
				<text class="empty-title">还没有消息</text>
				<text class="empty-desc">发送第一句话，开始实时聊天</text>
			</view>

			<view
				v-for="msg in messageList"
				:id="'msg-' + msg.id"
				:key="msg.id"
				:class="{
					'message-enter-self': msg.isNew && msg.isSelf,
					'message-enter-other': msg.isNew && !msg.isSelf && msg.type !== 'system'
				}"
			>
				<view
					class="msg-swipe-content"
				>
					<view class="message-item" :class="{ 'is-self': msg.isSelf, 'is-system': msg.type === 'system' }">
						<template v-if="msg.type === 'system'">
							<text class="system-text">{{ msg.content }}</text>
						</template>
						<template v-else>
							<image class="avatar" :src="msg.avatar" mode="aspectFill"></image>
							<view class="message-main">
								<view class="message-meta">
									<text>{{ msg.senderName }}</text>
									<text>{{ msg.time }}</text>
								</view>
								<view class="message-bubble">
									<text class="message-text">{{ msg.content }}</text>
								</view>
							</view>
						</template>
					</view>
				</view>
			</view>
			<view class="message-bottom-space" :style="{ height: messageBottomSpace }"></view>
			<view id="msg-bottom" class="scroll-anchor"></view>
		</scroll-view>

		<!-- 底部固定输入框 -->
		<view class="input-bar-fixed" v-if="!isSystemType" :style="{ bottom: keyboardHeight + 'px' }">
			<view class="input-row">
				<textarea
					class="input-area"
					v-model="inputText"
					:focus="inputFocus"
					placeholder="输入消息..."
					:auto-height="true"
					:maxlength="500"
					:adjust-position="false"
					confirm-type="send"
					@confirm="sendMessage"
					@focus="onInputFocus"
					@keyboardheightchange="onKeyboardHeightChange"
					@linechange="onLineChange"
				></textarea>
				<view class="action-col">
					<view class="expand-btn" v-show="lineCount > 2" @click="openExpand">
						<image class="expand-icon-img" src="/static/message/expand.svg" mode="aspectFit"></image>
					</view>
					<view
						class="send-btn"
						:class="{ disabled: !canSend }"
						@touchstart.stop.prevent="keepInputActive"
						@touchend.stop.prevent="sendMessage"
						@mousedown.stop.prevent="keepInputActive"
						@click.stop="sendMessage"
					>
						发送
					</view>
				</view>
			</view>
		</view>

		<!-- 展开输入框弹窗 -->
		<view class="expand-overlay" v-if="expandVisible" @click.self="confirmExpand">
			<view class="expand-panel">
				<view class="expand-header">
					<view class="expand-collapse" @click="confirmExpand">
						<image class="collapse-icon" src="/static/message/shrink.svg" mode="aspectFit"></image>
					</view>
				</view>
				<view class="expand-body">
					<textarea
						class="expand-textarea"
						v-model="expandText"
						:focus="true"
						:auto-height="false"
						:maxlength="500"
						placeholder="输入消息..."
						:adjust-position="false"
					></textarea>
				</view>
			</view>
		</view>

		<!-- 订单详情弹窗 -->
		<view class="order-dialog-mask" v-if="orderDialogVisible" @click="closeOrderDialog">
			<view class="order-dialog" @click.stop>
				<view class="dialog-header">
					<text class="dialog-title">订单详情</text>
					<text class="dialog-close" @click="closeOrderDialog">✕</text>
				</view>
				<view class="dialog-body" v-if="orderLoading">
					<text class="loading-text">加载中...</text>
				</view>
				<view class="dialog-body" v-else-if="!orderData">
					<text class="loading-text">未找到订单信息</text>
				</view>
				<view class="dialog-body" v-else>
					<view class="order-card">
						<view class="card-header">
							<view class="order-no-row">
								<text class="no-text">订单号：{{ orderData.orderNo }}</text>
								<text class="copy-btn" @click="copyOrderNo(orderData.orderNo)">复制</text>
							</view>
							<view class="status-tag" :style="{
								backgroundColor: getStatusBg(orderData.orderStatus),
								color: getStatusColor(orderData.orderStatus)
							}">
								{{ getStatusText(orderData.orderStatus) }}
							</view>
						</view>
						<view class="category-row">
							<view class="tags">
								<text class="parent-tag" :style="{ background: getParentTypeColor(orderData.parentType) }">
									{{ orderData.parentType }}
								</text>
								<text class="sub-tag">{{ orderData.subType }}</text>
							</view>
							<text class="expect-time" v-if="orderData.expectTime">希望送达时间：{{ orderData.expectTime }}</text>
						</view>
						<view class="address-block">
							<view class="addr-line">
								<view class="addr-icon pickup-icon">取</view>
								<text class="addr-text">{{ orderData.startAddressText }}</text>
							</view>
							<view class="addr-line">
								<view class="addr-icon delivery-icon">送</view>
								<text class="addr-text">{{ orderData.endAddressText }}</text>
							</view>
						</view>
						<view class="extra-info">
							<view class="reject-tip" v-if="orderData.riderCancelReason">
								<text class="reject-title">骑手已拒绝取消</text>
								<text class="reject-reason">理由：{{ orderData.riderCancelReason }}</text>
							</view>
							<view class="fetch-code-row" v-if="orderData.fetchCode">
								<view class="code-tag">
									<text class="code-label">取件码/物品</text>
									<text class="code-value">{{ orderData.fetchCode }}</text>
								</view>
							</view>
							<view class="remark-row" v-if="orderData.remark">
								<text class="remark-text">备注：{{ orderData.remark }}</text>
							</view>
						</view>
						<view class="card-footer">
							<view class="footer-left">
								<text class="time-text">{{ formatOrderTime(orderData.createTime) }}</text>
								<text class="price-tag" :class="orderData.payStatus === 1 ? 'paid' : 'unpaid'">
									{{ orderData.payStatus === 1 ? '实付' : '待支付' }}：¥{{ formatPrice(orderData.orderPrice) }}
								</text>
							</view>
							<view class="footer-actions">
								<view class="action-btn cancel-btn" v-if="orderData.orderStatus === 0"
									@click="cancelOrder(orderData)">取消订单</view>
								<view class="action-btn detail-btn" @click="goToOrderDetail">查看完整详情 →</view>
							</view>
						</view>
					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script setup>
import { computed, nextTick, ref } from 'vue'
import { onLoad, onUnload, onShow } from '@dcloudio/uni-app'
import config from '@/config/env.js'
import RiderWebSocket from '@/utils/websocket.js'
import request from '@/utils/request.js'
import { getCurrentUser as getLoginUser } from '@/utils/auth.js'
import { getBestChatSourceInfo, getLocalSystemMessages, saveChatSourceInfo, saveChatSourceInfoBySource } from '@/utils/messageCenter.js'
import { initGlobalWebSocket, onGlobalMessage, resetLocalChatNotice, updateGlobalBadge, isGlobalWsConnected, onGlobalWsStatus } from '@/utils/globalWebSocket.js'

const DEFAULT_OTHER_AVATAR = 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'
const historyCache = new Map()

const socketClient = ref(null)
const connected = ref(false)
const wsConnected = ref(false)
const connectionText = ref('连接中')
const onlineCount = ref(0)
const inputText = ref('')
const messageList = ref([])
const scrollTop = ref(0)
const scrollIntoView = ref('')
const scrollWithAnimation = ref(false)
const inputFocus = ref(false)
const isNavigating = ref(false)
let scrollTimer = null
let scrollRetryTimer = null
let scrollSeq = 0
let systemTimeRefreshTimer = null
const orderNo = ref('')
const orderData = ref(null)
const orderLoading = ref(false)
const systemNotices = ref([])
const orderDialogVisible = ref(false)

const sourceType = ref('chat')
const conversationId = ref('public')
const sourceId = ref('')
const chatTitle = ref('消息对话')
const otherAvatar = ref(DEFAULT_OTHER_AVATAR)
const currentUser = ref(getCurrentUser())
const sourceTitle = ref('')
const sourceDesc = ref('')
const targetUrl = ref('')
const isSystemType = computed(() => sourceType.value === 'system')

const sourceTag = computed(() => {
	const map = {
		group: '组局',
		goods: '商品',
		system: '系统',
		chat: '私聊'
	}
	return map[sourceType.value] || '私聊'
})

const statusBarHeight = ref(0)
const keyboardHeight = ref(0)
const inputBarHeight = ref(108) // 输入栏高度(rpx)，默认一行高度
const lineCount = ref(1) // 当前输入框行数
const expandVisible = ref(false)
const expandText = ref('')

// 获取实际的状态栏高度（px）
function getStatusBarHeight() {
	try {
		const systemInfo = uni.getSystemInfoSync()
		return systemInfo.statusBarHeight || 0
	} catch (e) {
		return 0
	}
}

// 将 px 转换为 rpx
function pxToRpx(px) {
	try {
		const systemInfo = uni.getSystemInfoSync()
		const windowWidth = systemInfo.windowWidth || 375
		return (px * 750) / windowWidth
	} catch (e) {
		return px
	}
}

const headerHeight = computed(() => {
	const statusBar = statusBarHeight.value
	const navbarHeight = 92
	const sourceCardHeight = (!isSystemType.value && sourceInfo.value) ? 88 : 0
	// 增加安全间距，适配不同屏幕
	const safePadding = 30 // rpx
	return `calc(${statusBar}px + ${navbarHeight}rpx + ${sourceCardHeight}rpx + ${safePadding}rpx)`
})

const messageListStyle = computed(() => {
	return {
		top: headerHeight.value
	}
})

const messageBottomSpace = computed(() => {
	const kb = keyboardHeight.value
	const barH = inputBarHeight.value
	return kb > 0 ? `calc(${kb}px + ${barH}rpx + 24rpx)` : `calc(${barH}rpx + env(safe-area-inset-bottom) + 24rpx)`
})

const inputBarCSS = computed(() => {
	return isSystemType.value ? '0rpx' : 'calc(108rpx + env(safe-area-inset-bottom))'
})

const chatPaddingBottom = computed(() => {
	return `calc(${inputBarCSS.value} + 24rpx)`
})

const sourceInfo = computed(() => {
	if (!(sourceTitle.value || sourceDesc.value || targetUrl.value)) {
		if (sourceType.value === 'group') {
			return { title: '组局信息', desc: '' }
		}
		return null
	}
	return {
		title: sourceTitle.value || sourceTag.value,
		desc: sourceDesc.value || ''
	}
})

function syncSourceInfoCache() {
	const cached = getBestChatSourceInfo({
		conversationId: conversationId.value,
		sourceType: sourceType.value,
		sourceId: sourceId.value
	})
	if (cached.sourceTitle) sourceTitle.value = cached.sourceTitle
	if (cached.sourceDesc) sourceDesc.value = cached.sourceDesc
	if (cached.targetUrl) targetUrl.value = cached.targetUrl

	if (sourceTitle.value || sourceDesc.value || targetUrl.value) {
		const info = {
			sourceTitle: sourceTitle.value,
			sourceDesc: sourceDesc.value,
			targetUrl: targetUrl.value
		}
		saveChatSourceInfo(conversationId.value, info)
		saveChatSourceInfoBySource(sourceType.value, sourceId.value, info)
	}
}

// ========== 互动面板 ==========
function getOtherUserId() {
	const cid = conversationId.value || ''
	if (cid.startsWith('friend_')) {
		const parts = cid.replace('friend_', '').split('_')
		const myId = currentUser.value?.id || ''
		return parts.find(p => p !== myId) || ''
	}
	return sourceId.value || ''
}

const canSend = computed(() => {
	return connected.value && inputText.value.trim().length > 0
})

// 获取当前用户：优先读缓存；没有登录功能时，默认使用你当前测试的账号。
function getCurrentUser() {
	const user = getLoginUser()
	if (!user) {
		return null
	}

	return {
		id: user.uid,
		name: user.username || user.nickname,
		avatar: user.avatar
	}
}

// 连接后端 WebSocket，并把会话ID、用户ID、昵称放到URL参数里。
function connectChatSocket() {
	if (socketClient.value) {
		socketClient.value.close()
		socketClient.value = null
	}
	const params = [
		`conversationId=${encodeURIComponent(conversationId.value)}`,
		`userId=${encodeURIComponent(currentUser.value.id)}`,
		`nickname=${encodeURIComponent(currentUser.value.name)}`,
		`avatar=${encodeURIComponent(currentUser.value.avatar)}`,
		`sourceType=${encodeURIComponent(sourceType.value)}`,
		`sourceId=${encodeURIComponent(sourceId.value || conversationId.value)}`,
		`sourceTitle=${encodeURIComponent(sourceTitle.value || '')}`,
		`sourceDesc=${encodeURIComponent(sourceDesc.value || '')}`,
		`targetUrl=${encodeURIComponent(targetUrl.value || '')}`,
		`title=${encodeURIComponent(chatTitle.value)}`,
		`conversationAvatar=${encodeURIComponent(otherAvatar.value)}`
	].join('&')
	const wsUrl = `${config.wsBaseUrl}/ws/chat?${params}`

	socketClient.value = new RiderWebSocket(wsUrl, {
		reconnectInterval: 3000,
		onOpen: () => {
			connected.value = true
			connectionText.value = '已连接'
		},
		onClose: () => {
			connected.value = false
			connectionText.value = '重连中'
		},
		onError: () => {
			connected.value = false
			connectionText.value = '连接异常'
		},
		onMessage: handleSocketMessage
	})
}

// 处理后端广播：chat 是聊天消息，system 是系统提示，online 只更新在线人数。
function handleSocketMessage(data) {
	if (data.type === 'online') {
		onlineCount.value = data.onlineCount || 0
		return
	}

	onlineCount.value = data.onlineCount || onlineCount.value

	if (data.type === 'system') {
		appendMessage({
			type: 'system',
			content: data.content || '',
			time: data.time || formatTime()
		})
		return
	}

	appendMessage({
		type: 'chat',
		senderId: data.senderId,
		senderName: data.senderName || '匿名用户',
		content: data.content || '',
		time: data.time || formatTime(),
		isSelf: data.senderId === currentUser.value.id,
		avatar: data.senderId === currentUser.value.id ? currentUser.value.avatar : (data.senderAvatar || otherAvatar.value),
		serverId: data.id
	})
}

function appendMessage(message) {
	const id = message.serverId || Date.now() + '_' + Math.floor(Math.random() * 10000)
	const wasEmpty = messageList.value.length === 0
	messageList.value.push({
		id,
		isNew: message.type !== 'system',
		...message
	})
	setTimeout(() => {
		const item = messageList.value.find(msg => msg.id === id)
		if (item) item.isNew = false
	}, 320)
	historyCache.set(conversationId.value, messageList.value)
	const delay = wasEmpty ? 100 : 40
	const retryDelay = wasEmpty ? 150 : (keyboardHeight.value > 0 ? 120 : 0)
	scheduleScrollToBottom(false, delay, retryDelay)
}

function scrollToBottom(animated = true, retryDelay = 0) {
	if (scrollRetryTimer) {
		clearTimeout(scrollRetryTimer)
		scrollRetryTimer = null
	}
	scrollSeq++
	nextTick(() => {
		scrollWithAnimation.value = animated
		scrollIntoView.value = ''
		scrollTop.value = scrollSeq
		nextTick(() => {
			scrollTop.value = 999999
			if (retryDelay > 0) {
				scrollRetryTimer = setTimeout(() => scrollToBottom(false, 0), retryDelay)
			}
		})
	})
}

function scheduleScrollToBottom(animated = false, delay = 40, retryDelay = 0) {
	if (scrollTimer) {
		clearTimeout(scrollTimer)
		scrollTimer = null
	}
	scrollTimer = setTimeout(() => {
		scrollTimer = null
		scrollToBottom(animated && keyboardHeight.value === 0, retryDelay)
	}, delay)
}

// 页面打开时先读取数据库里的历史聊天记录。
async function loadHistory() {
	const cid = conversationId.value
	if (historyCache.has(cid)) {
		messageList.value = historyCache.get(cid)
		nextTick(() => scrollToBottom(false))
		return
	}
	try {
		const res = await request({
			url: '/chat/history',
			method: 'GET',
			data: {
				conversationId: cid,
				limit: 50
			}
		})

		if (res.code !== 1) {
			return
		}

		messageList.value = (res.data || []).map(item => {
			const isSelf = String(item.senderId) === currentUser.value.id
			return {
				id: item.id,
				type: item.messageType || 'chat',
				senderId: item.senderId,
				senderName: item.senderName || '匿名用户',
				content: item.content || '',
				time: toShortTime(item.createTime),
				isSelf,
				avatar: isSelf ? currentUser.value.avatar : (item.senderAvatar || otherAvatar.value)
			}
		})

		historyCache.set(cid, messageList.value)
		nextTick(() => {
			scrollToBottom(false)
		})
	} catch (e) {
		uni.showToast({ title: '历史消息加载失败', icon: 'none' })
	}
}

async function markConversationRead() {
    if (!conversationId.value || !currentUser.value?.id) return
    resetLocalChatNotice(conversationId.value)
    updateGlobalBadge()
    try {
        await request({
            url: `/chat/read?conversationId=${encodeURIComponent(conversationId.value)}&userId=${encodeURIComponent(currentUser.value.id)}`,
            method: 'POST'
        })
    } catch (e) {
        console.warn('mark read failed', e)
    }
}

async function loadOrderData(orderNumber) {
	if (!orderNumber || !currentUser.value) return
	orderLoading.value = true
	try {
		const res = await request({
			url: `/order/userUid/${currentUser.value.id}/page`,
			method: 'GET',
			data: { pageNum: 1, pageSize: 50 }
		})
		if (res.code === 1 && Array.isArray(res.data.list)) {
			const found = res.data.list.find(o => o.orderNo === orderNumber)
			if (found) orderData.value = found
		}
	} catch (e) {
		// 静默处理
	} finally {
		orderLoading.value = false
	}
}

function loadSystemMessages() {
	const messages = getLocalSystemMessages()
	systemNotices.value = messages.map(msg => ({
		id: msg.id,
		title: msg.title || msg.name || '系统通知',
		lastMsg: msg.lastMsg || msg.title || '',
		desc: msg.desc || '',
		time: formatSystemNoticeTime(msg.timeSort || msg.timestamp || msg.createTime || msg.time),
		timeSort: normalizeTimeValue(msg.timeSort || msg.timestamp || msg.createTime || msg.time),
		orderNo: msg.orderNo || msg.sourceId || '',
		targetUrl: msg.targetUrl || (msg.orderNo ? `/pages/order/orderDetail?orderNo=${encodeURIComponent(msg.orderNo)}` : ''),
		unread: msg.unread || 0
	}))
}

function normalizeTimeValue(value) {
	if (!value) return 0
	if (typeof value === 'number') return value
	if (/^\d+$/.test(String(value))) return Number(value)
	const time = new Date(String(value).replace(/-/g, '/')).getTime()
	return Number.isNaN(time) ? 0 : time
}

function formatSystemNoticeTime(value) {
	const time = normalizeTimeValue(value)
	if (!time) return ''

	const now = Date.now()
	const diff = now - time
	if (diff >= 0 && diff < 60 * 1000) {
		return '刚刚'
	}

	const date = new Date(time)
	const nowDate = new Date(now)
	const month = String(date.getMonth() + 1).padStart(2, '0')
	const day = String(date.getDate()).padStart(2, '0')
	const hour = String(date.getHours()).padStart(2, '0')
	const minute = String(date.getMinutes()).padStart(2, '0')
	const isToday = date.toDateString() === nowDate.toDateString()

	const yesterday = new Date(nowDate)
	yesterday.setDate(nowDate.getDate() - 1)
	const isYesterday = date.toDateString() === yesterday.toDateString()

	if (isToday) return `${hour}:${minute}`
	if (isYesterday) return `昨天 ${hour}:${minute}`
	if (date.getFullYear() === nowDate.getFullYear()) return `${month}-${day} ${hour}:${minute}`
	return `${date.getFullYear()}-${month}-${day} ${hour}:${minute}`
}

function startSystemTimeRefresh() {
	if (systemTimeRefreshTimer) return
	systemTimeRefreshTimer = setInterval(() => {
		if (isSystemType.value) loadSystemMessages()
	}, 30 * 1000)
}

function stopSystemTimeRefresh() {
	if (!systemTimeRefreshTimer) return
	clearInterval(systemTimeRefreshTimer)
	systemTimeRefreshTimer = null
}

function scrollSystemToBottom() {
	nextTick(() => {
		scrollTop.value = scrollTop.value > 0 ? scrollTop.value + 1 : 99999
	})
}

function openNoticeTarget(notice) {
        if (notice.targetUrl) {
                uni.navigateTo({ url: notice.targetUrl })
                return
        }
        const lostFoundKeywords = ["申请领取", "申请归还", "领取申请", "归还申请", "失物", "招领"]
        const text = `${notice.title || ""}${notice.desc || ""}${notice.lastMsg || ""}`
        const isLostFound = lostFoundKeywords.some(kw => text.includes(kw))
        if (isLostFound && notice.orderNo) {
                uni.navigateTo({ url: `/pages/lostFound/lostFoundDetail?id=${encodeURIComponent(notice.orderNo)}` })
                return
        }

        const postKeywords = ["赞了你的帖子", "收藏了你的帖子", "关注了你", "评论了你的帖子"]
        const isPostNotice = postKeywords.some(kw => (notice.lastMsg || "").includes(kw))
        if (isPostNotice && notice.orderNo) {
                uni.navigateTo({ url: `/pages/wall/wall?postNo=${encodeURIComponent(notice.orderNo)}` })
                return
        }

        const activityKeywords = ["报名了你的组局"]
        const isActivityNotice = activityKeywords.some(kw => (notice.lastMsg || "").includes(kw))
        if (isActivityNotice && notice.orderNo) {
                uni.navigateTo({ url: `/pages/organization/organizationDetail?activityId=${encodeURIComponent(notice.orderNo)}` })
                return
        }

        if (notice.orderNo) {
                uni.navigateTo({ url: `/pages/order/orderDetail?orderNo=${encodeURIComponent(notice.orderNo)}` })
        }
}

async function loadOrderForPopup(orderNumber) {
	if (!orderNumber || !currentUser.value) return
	orderData.value = null
	orderLoading.value = true
	orderDialogVisible.value = true
	try {
		const res = await request({
			url: `/order/userUid/${currentUser.value.id}/page`,
			method: 'GET',
			data: { pageNum: 1, pageSize: 50 }
		})
		if (res.code === 1 && Array.isArray(res.data.list)) {
			const found = res.data.list.find(o => o.orderNo === orderNumber)
			if (found) orderData.value = found
		}
	} catch (e) { /* */ }
	finally { orderLoading.value = false }
}

function closeOrderDialog() {
	orderDialogVisible.value = false
	orderData.value = null
	orderLoading.value = false
}

function goToOrderDetail() {
	if (!orderData.value) return
	closeOrderDialog()
	uni.navigateTo({ url: `/pages/order/orderDetail?orderNo=${encodeURIComponent(orderData.value.orderNo)}` })
}

async function cancelOrder(order) {
	const res = await uni.showModal({ title: '提示', content: '确认取消该订单吗？' })
	if (!res.confirm) return
	try {
		const result = await request({
			url: '/order/cancelOrder',
			method: 'POST',
			data: { orderNo: order.orderNo }
		})
		if (result.code === 1) {
			uni.showToast({ title: '取消成功', icon: 'success' })
			order.orderStatus = 6
		} else {
			uni.showToast({ title: result.msg || '取消失败', icon: 'none' })
		}
	} catch (e) {
		uni.showToast({ title: '操作失败', icon: 'none' })
	}
}

function sendMessage() {
	const content = inputText.value.trim()
	if (!content) return

	if (!connected.value || !socketClient.value?.send({ content })) {
		uni.showToast({ title: '连接中，请稍后再发', icon: 'none' })
		return
	}

	inputText.value = ''
	keepInputActive()
}

function keepInputActive() {
	inputFocus.value = true
}

function onInputFocus() {
	inputFocus.value = true
	scheduleScrollToBottom(false, 40, 350)
}

function onKeyboardHeightChange(e) {
	const h = e.detail.height || 0
	keyboardHeight.value = h
	scheduleScrollToBottom(false, 40, h > 0 ? 300 : 120)
}

// 输入框行数变化，动态更新输入栏高度（用于消息列表底部留白）
function onLineChange(e) {
	const lc = e.detail.lineCount || 1
	lineCount.value = lc
	const h = Math.min(280, 76 + lc * 40)
	inputBarHeight.value = h
	nextTick(() => scheduleScrollToBottom(false, 40, 120))
}

// 展开输入框
function openExpand() {
	expandText.value = inputText.value
	expandVisible.value = true
}

// 取消展开（不保存）
function closeExpand() {
	expandVisible.value = false
}

// 确认展开内容，同步回主输入框
function confirmExpand() {
	inputText.value = expandText.value
	expandVisible.value = false
	nextTick(() => {
		inputFocus.value = true
	})
}

function formatTime() {
	const now = new Date()
	const hour = String(now.getHours()).padStart(2, '0')
	const minute = String(now.getMinutes()).padStart(2, '0')
	return `${hour}:${minute}`
}

function toShortTime(value) {
	if (!value) return formatTime()
	const normalized = String(value).replace(/-/g, '/')
	const date = new Date(normalized)
	if (Number.isNaN(date.getTime())) return formatTime()
	const hour = String(date.getHours()).padStart(2, '0')
	const minute = String(date.getMinutes()).padStart(2, '0')
	return `${hour}:${minute}`
}

function goBack() {
	uni.navigateBack()
}

function openSource() {
	if (!targetUrl.value) return

	if (sourceType.value === 'group') {
		const activityId = sourceId.value || getQueryParam(targetUrl.value, 'activityId')
		const hostId = getQueryParam(targetUrl.value, 'hostId')
		if (activityId) {
			const isHost = hostId && currentUser.value && String(currentUser.value.id) === String(hostId)
			const flash = isHost ? '1' : '2'
			const url = isHost
				? `/pages/organization/myOrganization?activityId=${encodeURIComponent(activityId)}&flash=${flash}`
				: `/pages/organization/organizationDetail?activityId=${encodeURIComponent(activityId)}`
			uni.navigateTo({ url })
			return
		}
	}

	uni.navigateTo({ url: targetUrl.value })
}

function getQueryParam(url, name) {
	const match = (url || '').match(new RegExp('[?&]' + name + '=([^&]*)'))
	return match ? decodeURIComponent(match[1]) : ''
}

function openInteractionPanel() {
	const otherId = getOtherUserId()
	if (!otherId) {
		uni.showToast({ title: '无法获取用户信息', icon: 'none' })
		return
	}
	const params = [
		`otherUserId=${encodeURIComponent(otherId)}`,
		`otherAvatar=${encodeURIComponent(otherAvatar.value)}`,
		`sourceType=${encodeURIComponent(sourceType.value)}`,
		`sourceId=${encodeURIComponent(sourceId.value)}`
	].join('&')
	uni.navigateTo({
		url: `/pages/message/interaction?${params}`
	})
}

function copyOrderNo(no) {
	uni.setClipboardData({ data: no })
	uni.showToast({ title: '已复制', icon: 'none' })
}

function formatPrice(val) { return (val != null ? Number(val).toFixed(2) : '0.00') }
function formatOrderTime(timeStr) { return (timeStr || '').replace('T', ' ') }

function getStatusText(status) {
	const map = { 0: '待接单', 1: '已接单', 2: '配送中', 3: '已送达', 4: '纠纷中', 5: '已完成', 6: '已取消' }
	return map[status] || '未知'
}
function getStatusColor(status) {
	const map = { 0: '#ff9800', 1: '#2196f3', 2: '#4caf50', 3: '#9e9e9e', 4: '#f44336', 5: '#4caf50', 6: '#9e9e9e' }
	return map[status] || '#999'
}
function getStatusBg(status) {
	const map = { 0: '#fff3e0', 1: '#e3f2fd', 2: '#e8f5e9', 3: '#f5f5f5', 4: '#ffebee', 5: '#e8f5e9', 6: '#f5f5f5' }
	return map[status] || '#f5f5f5'
}
function getParentTypeColor(type) {
	const map = { '快递': '#e3f2fd', '文件': '#fff3e0', '外卖': '#fce4ec', '代买': '#e8f5e9', '跑腿': '#f3e5f5', '搬运': '#e1f5fe', '其他': '#f5f5f5' }
	return map[type] || '#f5f5f5'
}

onLoad(async (options) => {
    if (!currentUser.value) {
        if (!isNavigating.value) {
            isNavigating.value = true
            uni.navigateTo({ url: '/pages/login/login' })
        }
        return
    }

    pageOptions.value = { ...options }

    statusBarHeight.value = uni.getSystemInfoSync().statusBarHeight || 0

    sourceType.value = options.sourceType || 'chat'
    conversationId.value = options.conversationId || options.sourceId || 'public'
    sourceId.value = options.sourceId || conversationId.value
    chatTitle.value = options.name ? decodeURIComponent(options.name) : '消息对话'
    otherAvatar.value = options.avatar ? decodeURIComponent(options.avatar) : DEFAULT_OTHER_AVATAR
    sourceTitle.value = options.sourceTitle ? decodeURIComponent(options.sourceTitle) : ''
    sourceDesc.value = options.sourceDesc ? decodeURIComponent(options.sourceDesc) : ''
    targetUrl.value = options.targetUrl ? decodeURIComponent(options.targetUrl) : ''
    orderNo.value = options.orderNo ? decodeURIComponent(options.orderNo) : ''
    syncSourceInfoCache()

    if (isSystemType.value) {
        connected.value = false
        connectionText.value = ''
        wsConnected.value = isGlobalWsConnected()
        loadSystemMessages()
        startSystemTimeRefresh()
        return
    }

    await loadHistory()
    await markConversationRead()
    connectChatSocket()
    inputFocus.value = false
    scrollToBottom(false, 120)
})

let globalMsgCleanup = null
let wsStatusCleanup = null
const pageOptions = ref({})

onShow(() => {
	initGlobalWebSocket()
	const opts = pageOptions.value && Object.keys(pageOptions.value).length > 0
		? pageOptions.value
		: (() => {
			const pages = getCurrentPages()
			const cp = pages[pages.length - 1]
			return cp?.$page?.options || cp?.options || {}
		})()
	if (opts.sourceType) {
		conversationId.value = opts.conversationId || opts.sourceId || 'public'
		sourceType.value = opts.sourceType || 'chat'
		sourceId.value = opts.sourceId || conversationId.value
		chatTitle.value = opts.name ? decodeURIComponent(opts.name) : chatTitle.value
		sourceTitle.value = opts.sourceTitle ? decodeURIComponent(opts.sourceTitle) : sourceTitle.value
		sourceDesc.value = opts.sourceDesc ? decodeURIComponent(opts.sourceDesc) : sourceDesc.value
		targetUrl.value = opts.targetUrl ? decodeURIComponent(opts.targetUrl) : targetUrl.value
		syncSourceInfoCache()
	}
	if (isSystemType.value) {
		wsConnected.value = isGlobalWsConnected()
		loadSystemMessages()
		startSystemTimeRefresh()
		if (!globalMsgCleanup) {
			globalMsgCleanup = onGlobalMessage((type) => {
				if (type === 'system') loadSystemMessages()
			})
		}
		if (!wsStatusCleanup) {
			wsStatusCleanup = onGlobalWsStatus((connected) => {
				wsConnected.value = connected
			})
		}
	}
})

onUnload(() => {
	markConversationRead()
	stopSystemTimeRefresh()
	if (scrollTimer) {
		clearTimeout(scrollTimer)
		scrollTimer = null
	}
	if (scrollRetryTimer) {
		clearTimeout(scrollRetryTimer)
		scrollRetryTimer = null
	}
	if (socketClient.value) {
		socketClient.value.close()
	}
	if (globalMsgCleanup) {
		globalMsgCleanup()
		globalMsgCleanup = null
	}
	if (wsStatusCleanup) {
		wsStatusCleanup()
		wsStatusCleanup = null
	}
})
</script>

<style scoped>
.page {
	height: 100vh;
	overflow: hidden;
	position: relative;
	background: #f4f6f8;
}

.header-fixed {
	position: absolute;
	top: 0;
	left: 0;
	right: 0;
	z-index: 100;
	background: #f4f6f8;
}

.navbar {
	display: flex;
	align-items: center;
	justify-content: space-between;
	min-height: 92rpx;
	padding: 0 22rpx;
	padding-top: var(--status-bar-height);
	background: #ffffff;
	border-bottom: 1rpx solid #edf0f3;
}

.nav-back,
.nav-placeholder {
	width: 64rpx;
	height: 64rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	flex-shrink: 0;
}

.interaction-entry {
	width: 92rpx;
	height: 56rpx;
	border-radius: 28rpx;
	background: #1f2933;
	color: #ffffff;
	font-size: 24rpx;
	font-weight: 700;
	display: flex;
	align-items: center;
	justify-content: center;
	flex-shrink: 0;
}

.nav-back .icon {
	font-size: 52rpx;
	color: #1f2933;
	font-weight: 500;
	line-height: 1;
}

.nav-title {
	flex: 1;
	min-width: 0;
	display: flex;
	flex-direction: column;
	align-items: center;
}

.title-text {
	max-width: 480rpx;
	font-size: 32rpx;
	font-weight: 700;
	color: #1f2933;
	line-height: 1.25;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}

.status-text {
	margin-top: 4rpx;
	font-size: 22rpx;
	color: #8a94a6;
}

.status-text.online {
	color: #16a34a;
}

.source-card {
	display: flex;
	align-items: center;
	padding: 20rpx 24rpx;
	background: #ffffff;
	border-bottom: 1rpx solid #e7ebf0;
}

.source-card:active {
	background: #f8fafc;
}

.source-tag {
	padding: 6rpx 16rpx;
	background: #1f2933;
	color: #ffffff;
	font-size: 22rpx;
	font-weight: 700;
	border-radius: 8rpx;
	margin-right: 16rpx;
	flex-shrink: 0;
}

.source-content {
	flex: 1;
	min-width: 0;
}

.source-title {
	font-size: 28rpx;
	font-weight: 700;
	color: #222832;
	line-height: 1.4;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}

.source-desc {
	font-size: 23rpx;
	color: #8a94a6;
	line-height: 1.4;
	margin-top: 4rpx;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}

.message-list {
	position: absolute;
	top: 0;
	left: 0;
	right: 0;
	bottom: 0;
	padding: 0 5rpx;
	box-sizing: border-box;
	will-change: scroll-position;
	transition: top 0.25s ease;
	overflow-y: auto;
}

.message-bottom-space {
	width: 100%;
	flex-shrink: 0;
}

.message-enter-self {
	animation: selfMessageSlideIn 0.26s cubic-bezier(0.2, 0.85, 0.25, 1) both;
	will-change: transform, opacity;
}

.message-enter-other {
	animation: otherMessageSlideIn 0.26s cubic-bezier(0.2, 0.85, 0.25, 1) both;
	will-change: transform, opacity;
}

@keyframes selfMessageSlideIn {
	from {
		opacity: 0;
		transform: translateX(72rpx);
	}
	to {
		opacity: 1;
		transform: translateX(0);
	}
}

@keyframes otherMessageSlideIn {
	from {
		opacity: 0;
		transform: translateX(-72rpx);
	}
	to {
		opacity: 1;
		transform: translateX(0);
	}
}

.scroll-anchor {
	height: 1rpx;
	width: 100%;
}

.top-safe-space {
	height: 10rpx;
	flex-shrink: 0;
}

/* ===== 连接状态 ===== */
.conn-status {
	display: flex;
	align-items: center;
	justify-content: center;
	gap: 8rpx;
	padding: 16rpx 24rpx;
	margin: 16rpx 22rpx 0;
	border-radius: 20rpx;
	background: #e8f5e9;
	font-size: 24rpx;
	color: #16a34a;
}

.conn-status.offline {
	background: #fff3e0;
	color: #e65100;
}

.conn-dot {
	width: 12rpx;
	height: 12rpx;
	border-radius: 50%;
	background: #16a34a;
}

.conn-status.offline .conn-dot {
	background: #e65100;
}

/* ===== 空状态 ===== */
.empty-tip {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	min-height: 50vh;
}

.empty-icon {
	font-size: 80rpx;
	margin-bottom: 24rpx;
}

.empty-title {
	font-size: 30rpx;
	font-weight: 700;
	color: #4b5563;
}

.empty-desc {
	margin-top: 10rpx;
	font-size: 25rpx;
	color: #9aa3af;
}

.message-item {
	display: flex;
	margin-bottom: 10rpx;
	align-items: flex-start;
	padding: 0 22rpx;
	box-sizing: border-box;
}

.msg-swipe-content {
}

.message-item.is-self {
	flex-direction: row-reverse;
}

.message-item.is-system {
	justify-content: center;
	margin: 14rpx 0 14rpx;
}

.system-text {
	max-width: 560rpx;
	padding: 8rpx 18rpx;
	border-radius: 999rpx;
	background: #e9edf2;
	color: #7b8494;
	font-size: 22rpx;
	line-height: 1.4;
	text-align: center;
}

.avatar {
	width: 78rpx;
	height: 78rpx;
	border-radius: 18rpx;
	flex-shrink: 0;
	background: #e9edf2;
}

.message-item.is-self .avatar {
	margin-left: 18rpx;
}

.message-item:not(.is-self) .avatar {
	margin-right: 18rpx;
}

.message-main {
	max-width: 66%;
	display: flex;
	flex-direction: column;
	align-items: flex-start;
}

.message-item.is-self .message-main {
	align-items: flex-end;
}

.message-meta {
	display: flex;
	gap: 12rpx;
	margin-bottom: 8rpx;
	font-size: 21rpx;
	color: #9aa3af;
	line-height: 1;
}

.message-bubble {
	padding: 18rpx 22rpx;
	background: #ffffff;
	border-radius: 18rpx;
	border-top-left-radius: 6rpx;
	/* box-shadow: 0 4rpx 14rpx rgba(20, 30, 50, 0.01); */
}

.message-item.is-self .message-bubble {
	background: #d8f8c6;
	border-top-left-radius: 18rpx;
	border-top-right-radius: 6rpx;
}

.message-text {
	font-size: 28rpx;
	line-height: 1.55;
	color: #1f2933;
	word-break: break-word;
	white-space: pre-wrap;
}

.input-bar-fixed {
	position: absolute;
	left: 0;
	right: 0;
	bottom: 0;
	z-index: 100;
	background: #ffffff;
	border-top: 1rpx solid #e7ebf0;
	box-sizing: border-box;
	transition: bottom 0.2s ease;
	will-change: bottom;
	padding: 14rpx 16rpx;
	padding-bottom: calc(14rpx + env(safe-area-inset-bottom));
}

.input-row {
	display: flex;
	align-items: flex-end;
	gap: 12rpx;
}

.action-col {
	display: flex;
	flex-direction: column;
	align-items: center;
	gap: 8rpx;
	flex-shrink: 0;
}

.input-area {
	flex: 1;
	min-height: 40rpx;
	max-height: 200rpx;
	padding: 16rpx 22rpx;
	background: #f4f6f8;
	border-radius: 20rpx;
	font-size: 28rpx;
	line-height: 1.45;
	border: 1rpx solid #e4e8ee;
	box-sizing: border-box;
	vertical-align: middle;
	overflow-y: auto;
	word-break: break-word;
}

.expand-btn {
	width: 56rpx;
	height: 56rpx;
	border-radius: 14rpx;
	background: #f4f6f8;
	display: flex;
	align-items: center;
	justify-content: center;
	flex-shrink: 0;
	border: 1rpx solid #e4e8ee;
}

.expand-btn:active {
	background: #e8eaef;
}

.expand-icon-img {
	width: 32rpx;
	height: 32rpx;
}

.send-btn {
	width: 120rpx;
	height: 75rpx;
	border-radius: 16rpx;
	background: #1f2933;
	color: #ffffff;
	font-size: 26rpx;
	font-weight: 700;
	display: flex;
	align-items: center;
	justify-content: center;
	flex-shrink: 0;
}

.send-btn.disabled {
	background: #c9d0d8;
	color: #ffffff;
}

/* ===== 展开输入弹窗 ===== */
.expand-overlay {
	position: fixed;
	left: 0;
	right: 0;
	top: 0;
	bottom: 0;
	z-index: 500;
	background: rgba(15, 23, 42, 0.5);
	display: flex;
	align-items: flex-end;
}

.expand-panel {
	width: 100%;
	height: 90%;
	background: #ffffff;
	display: flex;
	flex-direction: column;
	overflow: hidden;
	animation: expandSlideUp 0.25s ease;
}

@keyframes expandSlideUp {
	from { transform: translateY(100%); }
	to { transform: translateY(0); }
}

.expand-header {
	display: flex;
	align-items: center;
	padding: 10rpx 24rpx;
	flex-shrink: 0;
}

.expand-collapse {
	width: 56rpx;
	height: 56rpx;
	border-radius: 50%;
	background: #f2f4f7;
	display: flex;
	align-items: center;
	justify-content: center;
}

.expand-collapse:active {
	background: #e4e6eb;
}

.collapse-icon {
	font-size: 36rpx;
	color: #6a7484;
	line-height: 1;
}

.expand-body {
	flex: 1;
	overflow: hidden;
	padding: 0 24rpx 24rpx 24rpx;
	padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
}

.expand-textarea {
	width: 100%;
	height: 100%;
	min-height: 400rpx;
	padding: 16rpx 20rpx;
	background: #f4f6f8;
	border-radius: 16rpx;
	font-size: 28rpx;
	line-height: 1.6;
	border: 1rpx solid #e4e8ee;
	box-sizing: border-box;
	word-break: break-word;
}

/* ===== 系统通知卡片列表 ===== */
.notice-card {
	background: #ffffff;
	border-radius: 12rpx;
	padding: 20rpx 20rpx;
	margin: 20rpx 10rpx;
	border: 1rpx solid #eef0f3;
	position: relative;
	display: flex;
	align-items: flex-start;
	gap: 18rpx;
}

.notice-avatar {
	width: 80rpx;
	height: 80rpx;
	border-radius: 10rpx;
	background: #1a1d21;
	color: #ffffff;
	font-size: 30rpx;
	font-weight: 800;
	display: flex;
	align-items: center;
	justify-content: center;
	flex-shrink: 0;
}

.notice-body {
	flex: 1;
	min-width: 0;
}

.notice-card:active {
	background: #f8fafc;
}

.notice-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 6rpx;
}

.notice-title {
	font-size: 27rpx;
	font-weight: 600;
	color: #1a1d21;
	max-width: 420rpx;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}

.notice-time {
	font-size: 22rpx;
	color: #9aa3af;
	flex-shrink: 0;
}

.notice-desc {
	font-size: 24rpx;
	color: #5a6470;
	line-height: 1.45;
	margin-bottom: 6rpx;
}

.notice-footer {
	background: #fef8ee;
	padding: 6rpx 12rpx;
	border-radius: 8rpx;
	margin-bottom: 6rpx;
}

.notice-extra {
	font-size: 22rpx;
	color: #e09820;
}

.notice-arrow {
	display: flex;
	align-items: center;
	justify-content: flex-end;
	gap: 4rpx;
	font-size: 22rpx;
	color: #3677ff;
	font-weight: 500;
	padding-top: 8rpx;
	border-top: 1rpx solid #eef0f3;
	margin-top: 4rpx;
}

.notice-arrow .arrow {
	font-size: 28rpx;
	line-height: 1;
}

/* ===== 订单详情弹窗 ===== */
.order-dialog-mask {
	position: fixed;
	left: 0;
	right: 0;
	top: 0;
	bottom: 0;
	z-index: 300;
	background: rgba(15, 23, 42, 0.5);
	display: flex;
	align-items: center;
	justify-content: center;
	backdrop-filter: blur(6rpx);
}

.order-dialog {
	width: 660rpx;
	max-height: 85vh;
	background: #ffffff;
	border-radius: 28rpx;
	overflow: hidden;
	box-shadow: 0 24rpx 60rpx rgba(0, 0, 0, 0.18);
	display: flex;
	flex-direction: column;
}

.dialog-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 28rpx 28rpx 16rpx;
	border-bottom: 1rpx solid #eef1f5;
	flex-shrink: 0;
}

.dialog-title {
	font-size: 32rpx;
	font-weight: 800;
	color: #1a1d21;
}

.dialog-close {
	width: 54rpx;
	height: 54rpx;
	border-radius: 50%;
	background: #f2f4f7;
	color: #6b7280;
	font-size: 36rpx;
	display: flex;
	align-items: center;
	justify-content: center;
}

.dialog-body {
	flex: 1;
	overflow-y: auto;
	padding: 24rpx;
}

.loading-text {
	display: block;
	text-align: center;
	padding: 80rpx 0;
	color: #9aa3af;
	font-size: 28rpx;
}

.order-card {
	background: #ffffff;
	border-radius: 20rpx;
	overflow: hidden;
}

.card-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 16rpx 0 12rpx;
}

.order-no-row {
	display: flex;
	align-items: center;
	gap: 10rpx;
}

.no-text {
	font-size: 24rpx;
	color: #6a7484;
	font-weight: 600;
}

.copy-btn {
	font-size: 22rpx;
	color: #3677ff;
	padding: 4rpx 12rpx;
	background: #eef3ff;
	border-radius: 8rpx;
}

.status-tag {
	padding: 6rpx 16rpx;
	border-radius: 10rpx;
	font-size: 22rpx;
	font-weight: 700;
}

.category-row {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 10rpx 0 16rpx;
	border-bottom: 1rpx solid #f0f2f5;
}

.tags {
	display: flex;
	gap: 8rpx;
	align-items: center;
}

.parent-tag {
	padding: 4rpx 12rpx;
	border-radius: 8rpx;
	font-size: 22rpx;
	font-weight: 700;
	color: #314253;
}

.sub-tag {
	font-size: 22rpx;
	color: #6a7484;
}

.expect-time {
	font-size: 21rpx;
	color: #8a94a6;
}

.address-block {
	padding: 18rpx 0 14rpx;
	border-bottom: 1rpx solid #f0f2f5;
}

.addr-line {
	display: flex;
	align-items: center;
	margin-bottom: 10rpx;
}

.addr-line:last-child {
	margin-bottom: 0;
}

.addr-icon {
	width: 36rpx;
	height: 36rpx;
	border-radius: 8rpx;
	font-size: 22rpx;
	font-weight: 800;
	color: #ffffff;
	display: flex;
	align-items: center;
	justify-content: center;
	margin-right: 12rpx;
	flex-shrink: 0;
}

.pickup-icon {
	background: #4caf50;
}

.delivery-icon {
	background: #ff9800;
}

.addr-text {
	font-size: 26rpx;
	color: #1a1d21;
	line-height: 1.4;
}

.extra-info {
	padding: 14rpx 0;
}

.reject-tip {
	background: #fff3e0;
	padding: 14rpx 16rpx;
	border-radius: 12rpx;
	margin-bottom: 12rpx;
}

.reject-title {
	font-size: 24rpx;
	font-weight: 700;
	color: #e65100;
	display: block;
}

.reject-reason {
	font-size: 22rpx;
	color: #bf360c;
	margin-top: 6rpx;
	display: block;
}

.fetch-code-row {
	margin-bottom: 10rpx;
}

.code-tag {
	display: flex;
	align-items: center;
	gap: 12rpx;
	padding: 10rpx 16rpx;
	background: #f5f6f8;
	border-radius: 10rpx;
}

.code-label {
	font-size: 22rpx;
	color: #8a94a6;
}

.code-value {
	font-size: 26rpx;
	font-weight: 700;
	color: #1a1d21;
}

.remark-row {
	margin-top: 8rpx;
}

.remark-text {
	font-size: 23rpx;
	color: #6a7484;
}

.card-footer {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 16rpx 0 0;
	border-top: 1rpx solid #f0f2f5;
}

.footer-left {
	display: flex;
	flex-direction: column;
	gap: 6rpx;
}

.time-text {
	font-size: 22rpx;
	color: #9aa3af;
}

.price-tag {
	font-size: 24rpx;
	font-weight: 700;
}

.price-tag.paid {
	color: #16a34a;
}

.price-tag.unpaid {
	color: #ff6b3d;
}

.footer-actions {
	display: flex;
	gap: 12rpx;
	align-items: center;
}

.action-btn {
	height: 56rpx;
	padding: 0 20rpx;
	border-radius: 28rpx;
	font-size: 24rpx;
	font-weight: 700;
	display: flex;
	align-items: center;
	justify-content: center;
}

.cancel-btn {
	background: #fef0f0;
	color: #e53935;
}

.detail-btn {
	background: #1f2933;
	color: #ffffff;
}
</style>