<template>
	<view class="page" @click="closeSwipe">
		<!-- 顶部导航 -->
		<view class="header">
			<view class="header-top">
				<text class="title">消息</text>
				<view class="header-actions">
					<view class="icon-btn" @click="goToFriends">
						<text class="icon">👥</text>
					</view>
					<view class="icon-btn" @click="goAddFriend">
						<text class="icon">+</text>
					</view>
				</view>
			</view>
		</view>

		<!-- 分类 tabs -->
		<view class="tabs">
			<view
				v-for="tab in tabs"
				:key="tab.value"
				class="tab"
				:class="{ active: activeTab === tab.value }"
				@click="switchTab(tab.value)"
			>
				<text>{{ tab.label }}</text>
				<view v-if="tabCount(tab.value) > 0" class="tab-dot">{{ tabCount(tab.value) }}</view>
			</view>
		</view>

		<!-- 消息列表 -->
		<scroll-view
			class="list-scroll"
			scroll-y
			:refresher-enabled="!isSwiping"
			:refresher-triggered="refreshing"
			:refresher-threshold="80"
			@refresherrefresh="onRefresh"
			@scrolltolower="loadMore"
		>
			<view v-if="loading && !chatList.length" class="state-tip">加载中...</view>

			<view v-if="!loading && !filteredList.length" class="empty">
				<text class="empty-icon">📭</text>
				<text class="empty-text">暂无消息</text>
			</view>

			<view
				v-for="(chat, idx) in filteredList"
				:key="chat.id"
				class="swipe-wrap fade-in"
				:style="{ animationDelay: Math.min(idx, 12) * 0.02 + 's' }"
				@touchstart="onSwipeStart($event, chat.id)"
				@touchmove="onSwipeMove($event, chat.id)"
				@touchend="onSwipeEnd($event, chat.id)"
			>
				<view class="swipe-del" @click.stop="delConversation(chat)">
					<image class="del-icon" src="/static/message/delete.png" mode="aspectFit"></image>
					<text>删除</text>
				</view>
				<view
					class="chat-card"
					:class="{ pinned: chat.isPinned }"
					:style="swipeStyle(chat.id)"
					@click.stop="openChat(chat)"
				>
					<view class="avatar-wrap">
						<image class="avatar" :src="chat.avatar" mode="aspectFill"></image>
						<view class="type-tag" :class="'tag-' + chat.type">
							<text>{{ typeLabel(chat.type) }}</text>
						</view>
						<view v-if="chat.unread > 0" class="badge">{{ chat.unread > 99 ? '99+' : chat.unread }}</view>
					</view>
					<view class="chat-body">
						<view class="chat-top">
							<text class="chat-name">{{ chat.name }}</text>
							<text class="chat-time">{{ chat.time }}</text>
						</view>
						<view class="chat-bottom">
							<view class="chat-desc">{{ chat.lastMsg }}</view>
						</view>
					</view>
				</view>
			</view>

			<view v-if="loadingMore" class="state-tip small">加载中...</view>
			<view v-if="!loadingMore && chatList.length >= pageSize" class="state-tip small">— 没有更多了 —</view>
		</scroll-view>
	</view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import config from '@/config/env.js'
import request from '@/utils/request.js'
import { getCurrentUser } from '@/utils/auth.js'
import { getBestChatSourceInfo, getLocalSystemMessages, getSystemUnreadCount, resetSystemNoticeUnread, saveChatSourceInfo, saveChatSourceInfoBySource, SYSTEM_AVATAR } from '@/utils/messageCenter.js'
import { getLocalChatNotices, initGlobalWebSocket, onGlobalMessage, resetLocalChatNotice, getChatUnreadCount, updateGlobalBadge } from '@/utils/globalWebSocket.js'

const DEFAULT_AVATAR = 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'



// 只从本地登录信息读取 uid，前端不使用数据库自增 id。
function getCurrentUserId() {
	const user = getCurrentUser()
	return user ? user.uid : ''
}

// ========== 页面数据 ==========
const pageSize = 20
const chatList = ref([])
const friendList = ref([])
const localSystemMessages = ref([])
const localChatNotices = ref([])
const loading = ref(true)
const refreshing = ref(false)
const loadingMore = ref(false)
const swipedId = ref('')
const swipeDelta = ref({})
const swipeTransition = ref({})
const isSwiping = ref(false)
const isNavigating = ref(false)

const tabs = [
	{ label: '全部', value: 'all' },
	{ label: '组局', value: 'group' },
	{ label: '商品', value: 'goods' },
	{ label: '系统', value: 'system' },
	{ label: '好友', value: 'chat' }
]
const activeTab = ref('all')

// 类型角标，保持短文字，避免挤压头像区域。
const typeLabel = (type) => {
	const map = {
		group: '局',
		goods: '物',
		chat: '友',
		system: '系'
	}
	return map[type] || '聊'
}

// 系统通知（聚合所有本地系统消息为一条，小红点显示总未读数）
// 格式化消息时间：3分钟以内显示"刚刚"，否则显示日期时间
const formatMessageTime = (timeSort) => {
	if (!timeSort) return ''
	const now = Date.now()
	const diff = now - timeSort
	if (diff < 1 * 60 * 1000) {
		return '刚刚'
	}
	const date = new Date(timeSort)
	const month = (date.getMonth() + 1).toString().padStart(2, '0')
	const day = date.getDate().toString().padStart(2, '0')
	const hour = date.getHours().toString().padStart(2, '0')
	const minute = date.getMinutes().toString().padStart(2, '0')
	return `${month}-${day} ${hour}:${minute}`
}

const systemNotification = computed(() => {
	const messages = localSystemMessages.value
	if (!messages || messages.length === 0) {
		return {
			id: 'system_notice',
			name: '系统通知',
			avatar: SYSTEM_AVATAR,
			lastMsg: '您有新的活动提醒和订单消息',
			time: '今天',
			timeSort: Date.now(),
			unread: 0,
			isPinned: true,
			type: 'system',
			sourceType: 'system',
			sourceId: ''
		}
	}
	const totalUnread = messages.reduce((s, m) => s + (m.unread || 0), 0)
	const latest = messages[0]
	return {
		id: 'system_notice',
		name: '系统通知',
		avatar: SYSTEM_AVATAR,
		lastMsg: latest.lastMsg || latest.title || '您有新的系统消息',
		time: formatMessageTime(latest.timeSort),
		timeSort: latest.timeSort || Date.now(),
		unread: totalUnread,
		isPinned: true,
		type: 'system',
		sourceType: 'system',
		sourceId: '',
		title: latest.title || '系统通知',
		desc: latest.desc || ''
	}
})

const allMessages = computed(() => {
	let list = [...chatList.value].filter(item => !String(item.id || '').startsWith('notice_'))
	let friends = [...friendList.value]
	let localChats = localChatNotices.value
		.filter(item => (item.unread || 0) > 0)
		.map(item => ({
			...item,
			type: item.id === 'system_notice' ? 'system' : (item.type === 'system' ? 'chat' : item.type)
		}))

	const seenIds = new Set()
	const merged = []
	for (const item of list) {
		if (!seenIds.has(item.id)) {
			merged.push(item)
			seenIds.add(item.id)
		}
	}
	for (const f of friends) {
		if (!seenIds.has(f.id)) {
			merged.push(f)
			seenIds.add(f.id)
		}
	}
	for (const local of localChats) {
		if (!seenIds.has(local.id)) {
			merged.push(local)
			seenIds.add(local.id)
		}
	}

	const sysNotice = systemNotification.value
	if (sysNotice) {
		merged.unshift(sysNotice)
	}

	return merged
})

const filteredList = computed(() => {
	const merged = [...allMessages.value]

	if (activeTab.value !== 'all') {
		return merged.filter(item => item.type === activeTab.value)
	}

	return merged.sort((a, b) => {
		if (a.isPinned !== b.isPinned) return (b.isPinned ? 1 : 0) - (a.isPinned ? 1 : 0)
		return (b.timeSort || 0) - (a.timeSort || 0)
	})
})

const tabCount = (value) => {
	if (value === 'all') {
		return allMessages.value.reduce((sum, item) => sum + (item.unread || 0), 0)
	}
	return allMessages.value
		.filter(item => item.type === value)
		.reduce((sum, item) => sum + (item.unread || 0), 0)
}

let globalMsgCleanup = null

const bindGlobalMessages = () => {
    if (globalMsgCleanup) return
    globalMsgCleanup = onGlobalMessage(() => {
        localSystemMessages.value = getLocalSystemMessages()
        localChatNotices.value = getLocalChatNotices()
        chatList.value = chatList.value.map(item => mergeLocalChatNotice(item))
        updateTabBarBadge()
    })
}

function mergeLocalChatNotice(item) {
    const local = localChatNotices.value.find(notice => notice.id === item.id)
    if (!local) {
        if (item.sourceTitle || item.sourceDesc || item.targetUrl) {
            saveChatSourceInfo(item.id, item)
            saveChatSourceInfoBySource(item.sourceType, item.sourceId, item)
        }
        return item
    }
    const isGenericName = !item.name || item.name === '消息对话'
    const merged = {
        ...item,
        name: isGenericName ? (local.name || item.name) : item.name,
		avatar: item.avatar || local.avatar || '',
		lastMsg: local.lastMsg || item.lastMsg,
		time: local.time || item.time,
		timeSort: Math.max(item.timeSort || 0, local.timeSort || 0),
		unread: local.unread > 0 ? local.unread : Math.min(item.unread || 0, local.unread || 0),
		sourceTitle: local.sourceTitle || item.sourceTitle || '',
		sourceDesc: local.sourceDesc || item.sourceDesc || '',
		targetUrl: local.targetUrl || item.targetUrl || ''
    }
    if (merged.sourceTitle || merged.sourceDesc || merged.targetUrl) {
        saveChatSourceInfo(merged.id, merged)
        saveChatSourceInfoBySource(merged.sourceType, merged.sourceId, merged)
    }
    return merged
}

// ========== 数据加载 ==========
const loadList = async (isRefresh = false) => {
	localSystemMessages.value = getLocalSystemMessages()
	localChatNotices.value = getLocalChatNotices()

	if (!getCurrentUserId()) {
		chatList.value = []
		friendList.value = []
		loading.value = false
		refreshing.value = false
		if (!isNavigating.value) {
			isNavigating.value = true
			uni.navigateTo({ url: '/pages/login/login' })
		}
		return
	}

	if (isRefresh) refreshing.value = true
	else loading.value = true

	try {
		const res = await request({
			url: '/chat/conversations',
			method: 'GET',
			data: {
				userId: getCurrentUserId()
			}
		})
		if (res.code === 1 && Array.isArray(res.data)) {
			chatList.value = res.data.map(item => mergeLocalChatNotice({
				id: item.id,
				name: item.name || '消息对话',
				avatar: item.avatar || DEFAULT_AVATAR,
				lastMsg: item.lastMsg || '暂无消息',
				time: item.time || '',
				timeSort: new Date(String(item.lastTime || '').replace(/-/g, '/')).getTime() || Date.now(),
				unread: item.unread || 0,
				isPinned: !!item.isPinned,
				type: item.type || 'chat',
				sourceType: item.sourceType || item.type || 'chat',
				sourceId: item.sourceId || item.id,
				sourceTitle: item.sourceTitle || '',
				sourceDesc: item.sourceDesc || '',
				targetUrl: item.targetUrl || ''
			}))
		} else {
			chatList.value = []
		}

		friendList.value = []
	} catch (e) {
		friendList.value = []
	} finally {
		loading.value = false
		refreshing.value = false
	}

	// 静默清理重复会话（仅首次加载时执行一次）
	if (!isRefresh && !_cleanupDone) {
		_cleanupDone = true
		request({
			url: '/chat/conversations/cleanup',
			method: 'POST'
		}).then(res => {
			if (res.code === 1 && res.data > 0) {
				// 有清理则重新加载列表
				setTimeout(() => loadList(true), 300)
			}
		}).catch(() => {})
	}
}

let _cleanupDone = false

const loadMore = async () => {
	if (loadingMore.value) return
	// 当前接口一次返回所有最近会话，后续会话很多时可在這里接分页。
}

// ========== 事件 ==========
const onRefresh = () => {
	loadList(true)
}

const switchTab = (value) => {
	activeTab.value = value
	closeSwipe()
}

// ========== 滑动删除 ==========
const SWIPE_THRESHOLD = 60
const swipeStartPos = ref({})

function swipeStyle(id) {
	const delta = swipeDelta.value[id] || 0
	const isOpen = swipedId.value === id
	const offset = isOpen ? -70 : delta
	const hasTransition = swipeTransition.value[id] !== false
	return {
		transform: `translateX(${offset}px)`,
		transition: hasTransition ? 'transform 0.2s ease' : 'none'
	}
}

function onSwipeStart(e, id) {
	const touch = (e.touches || e.changedTouches || [])[0]
	if (!touch) return
	isSwiping.value = false
	swipeStartPos.value = { ...swipeStartPos.value, [id]: { x: touch.clientX, y: touch.clientY } }
	swipeDelta.value = { ...swipeDelta.value, [id]: swipedId.value === id ? -80 : 0 }
	swipeTransition.value = { ...swipeTransition.value, [id]: false }
	if (swipedId.value && swipedId.value !== id) {
		closeSwipe()
	}
}

function onSwipeMove(e, id) {
	const touch = (e.touches || e.changedTouches || [])[0]
	const start = swipeStartPos.value[id]
	if (!touch || !start) return
	const dx = touch.clientX - start.x
	const dy = touch.clientY - start.y
	if (Math.abs(dx) < 8 && Math.abs(dy) < 8) return
	if (Math.abs(dy) > Math.abs(dx)) return
	if (swipedId.value !== id && dx > 10) return
	isSwiping.value = true
	const baseOffset = swipedId.value === id ? -80 : 0
	const offset = Math.max(-80, Math.min(0, baseOffset + dx))
	swipeDelta.value = { ...swipeDelta.value, [id]: offset }
}

function onSwipeEnd(e, id) {
	const offset = swipeDelta.value[id] || 0
	swipeTransition.value = { ...swipeTransition.value, [id]: true }
	if (swipedId.value === id) {
		if (offset > -100) {
			closeSwipe()
		}
	} else {
		if (offset < -SWIPE_THRESHOLD) {
			swipedId.value = id
			swipeDelta.value = { ...swipeDelta.value, [id]: -80 }
		} else {
			swipeDelta.value = { ...swipeDelta.value, [id]: 0 }
		}
	}
	setTimeout(() => { isSwiping.value = false }, 300)
}

function closeSwipe() {
	const id = swipedId.value
	if (id) {
		swipeDelta.value = { ...swipeDelta.value, [id]: 0 }
	}
	swipedId.value = ''
	isSwiping.value = false
}

async function delConversation(chat) {
	const confirm = await new Promise(resolve => {
		uni.showModal({
			title: '删除会话',
			content: `确定删除「${chat.name}」的会话记录吗？`,
			success: (res) => resolve(res.confirm)
		})
	})
	if (!confirm) return

	try {
		await request({
			url: '/chat/conversation/delete',
			method: 'DELETE',
			data: { conversationId: chat.id }
		})
	} catch (e) {}

	chatList.value = chatList.value.filter(c => c.id !== chat.id)
	friendList.value = friendList.value.filter(c => c.id !== chat.id)
	closeSwipe()
	updateTabBarBadge()
	uni.showToast({ title: '已删除', icon: 'none', duration: 1200 })
}

// 点击空白区域关闭滑动已在 page 上通过 @click="closeSwipe" 处理

const goToFriends = () => {
	uni.navigateTo({ url: '/pages/message/myFriends' })
}

const goAddFriend = () => {
	if (!getCurrentUserId()) {
		if (!isNavigating.value) {
			isNavigating.value = true
			uni.navigateTo({ url: '/pages/login/login' })
		}
		return
	}
	uni.navigateTo({ url: '/pages/friend/friend' })
}

const openChat = (chat) => {
    if (swipedId.value) {
        closeSwipe()
        return
    }
    if (chat.id === 'system_notice') {
        resetSystemNoticeUnread()
        localSystemMessages.value = getLocalSystemMessages()
        updateTabBarBadge()
        const params = [
            `conversationId=system_notice`,
            `sourceType=system`,
            `name=${encodeURIComponent('系统通知')}`
        ].join('&')
        uni.navigateTo({ url: `/pages/message/message?${params}` })
        return
    }

    if (chat.targetUrl && chat.type === 'system') {
        chat.unread = 0
        uni.navigateTo({ url: chat.targetUrl })
        return
    }
    const item = chatList.value.find(c => c.id === chat.id)
    if (item) item.unread = 0
    const friend = friendList.value.find(f => f.id === chat.id)
    if (friend) friend.unread = 0
    resetLocalChatNotice(chat.id)
    localChatNotices.value = getLocalChatNotices()
    updateGlobalBadge()

    const userId = getCurrentUserId()
    if (userId) {
        request({
            url: `/chat/read?conversationId=${encodeURIComponent(chat.id)}&userId=${encodeURIComponent(userId)}`,
            method: 'POST'
        }).catch(() => {})
    }

    const sourceInfo = getBestChatSourceInfo({
        conversationId: chat.id,
        sourceType: chat.sourceType || 'chat',
        sourceId: chat.sourceId || ''
    })
    const chatSourceTitle = chat.sourceTitle || sourceInfo.sourceTitle || ''
    const chatSourceDesc = chat.sourceDesc || sourceInfo.sourceDesc || ''
    const chatTargetUrl = chat.targetUrl || sourceInfo.targetUrl || ''

    const params = [
        `conversationId=${chat.id}`,
        `sourceType=${chat.sourceType || 'chat'}`,
        `sourceId=${chat.sourceId || ''}`,
        `name=${encodeURIComponent(chat.name)}`,
        `avatar=${encodeURIComponent(chat.avatar)}`,
        `sourceTitle=${encodeURIComponent(chatSourceTitle)}`,
        `sourceDesc=${encodeURIComponent(chatSourceDesc)}`,
        `targetUrl=${encodeURIComponent(chatTargetUrl)}`
    ].join('&')

    uni.navigateTo({
        url: `/pages/message/message?${params}`
    })
}

function updateTabBarBadge() {
    const count = getSystemUnreadCount() + getChatUnreadCount()
    if (count > 0) {
        uni.setTabBarBadge({ index: 2, text: count > 99 ? '99+' : String(count) })
    } else {
        uni.removeTabBarBadge({ index: 2 })
    }
}

// ========== 生命周期 ==========
onMounted(() => {
	initGlobalWebSocket()
	bindGlobalMessages()
	loadList(false).then(() => updateTabBarBadge())
})

onShow(() => {
    initGlobalWebSocket()
    bindGlobalMessages()
    localSystemMessages.value = getLocalSystemMessages()
    localChatNotices.value = getLocalChatNotices()
    updateTabBarBadge()
    loadList(false)
})
</script>

<style scoped>
.page {
	position: fixed;
	left: 0;
	right: 0;
	top: var(--window-top, 0);
	bottom: var(--window-bottom, 0);
	min-height: 0;
	overflow: hidden;
	background: #f2f3f5;
	display: flex;
	flex-direction: column;
}

/* ===== 顶部导航 ===== */
.header {
	background: #ffffff;
	padding: 24rpx 28rpx 10rpx;
	flex-shrink: 0;
}

.header-top {
	display: flex;
	align-items: center;
	justify-content: space-between;
}

.header-actions {
	display: flex;
	align-items: center;
	gap: 16rpx;
}

.title {
	font-size: 48rpx;
	font-weight: 900;
	color: #1a1d21;
	letter-spacing: 0;
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
	font-size: 32rpx;
}

/* ===== 分类 tabs ===== */
.tabs {
	display: flex;
	gap: 14rpx;
	padding: 20rpx 28rpx 22rpx;
	background: #ffffff;
	border-bottom: 1rpx solid #eef0f3;
	flex-shrink: 0;
}

.tab {
	position: relative;
	height: 60rpx;
	padding: 0 26rpx;
	border-radius: 30rpx;
	background: #f5f6f8;
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 26rpx;
	color: #6a7484;
	font-weight: 600;
	transition: all 0.2s ease;
}

.tab.active {
	background: #1a1d21;
	color: #ffffff;
}

.tab-dot {
	position: absolute;
	top: -6rpx;
	right: -2rpx;
	min-width: 32rpx;
	height: 32rpx;
	padding: 0 8rpx;
	background: #ff4757;
	color: #ffffff;
	font-size: 18rpx;
	font-weight: 700;
	border-radius: 16rpx;
	line-height: 32rpx;
	text-align: center;
	border: 2rpx solid #ffffff;
}

.tab.active .tab-dot {
	background: #ffffff;
	color: #ff4757;
	border-color: #1a1d21;
}

/* ===== 列表 ===== */
.list-scroll {
	flex: 1;
	min-height: 0;
	background: #f2f3f5;
	box-sizing: border-box;
	overflow: hidden;
}

/* ===== 聊天卡片 ===== */
.swipe-wrap {
	position: relative;
	overflow: hidden;
	margin: 10rpx 20rpx;
	border-radius: 24rpx;
	background: #f2f3f5;
}

.swipe-wrap.fade-in {
	opacity: 0;
	transform: translateY(10rpx);
	animation: fadeInUp 0.4s cubic-bezier(0.25, 0.46, 0.45, 0.94) forwards;
}

@keyframes fadeInUp {
	to {
		opacity: 1;
		transform: translateY(0);
	}
}

.swipe-del {
	position: absolute;
	right: 0;
	top: 0;
	bottom: 0;
	width: 180rpx;
	background: #ff4757;
	border-radius: 0 27rpx 27rpx 0;
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	gap: 8rpx;
	font-size: 22rpx;
	color: #ffffff;
	font-weight: 600;
}

.del-icon {
	width: 40rpx;
	height: 40rpx;
}

.chat-card {
	display: flex;
	align-items: center;
	padding: 20rpx 20rpx;
	background: #ffffff;
	border-radius: 24rpx;
	box-shadow: 0 4rpx 20rpx rgba(20, 30, 50, 0.04);
	position: relative;
	z-index: 1;
}

.chat-card:active {
	background: #f5f6f8;
}

.chat-card.pinned {
	background: linear-gradient(135deg, #ffffff 0%, #fafbfd 100%);
	border: 1rpx solid #e8ecf1;
}

/* ===== 头像区 ===== */
.avatar-wrap {
	position: relative;
	margin-right: 20rpx;
	flex-shrink: 0;
}

.avatar {
	width: 80rpx;
	height: 80rpx;
	border-radius: 15rpx;
	background: #eef1f5;
}

.type-tag {
	position: absolute;
	bottom: -3rpx;
	right: -7rpx;
	width: 35rpx;
	height: 35rpx;
	border-radius: 12rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 22rpx;
	font-weight: 800;
	color: #ffffff;
	border: 3rpx solid #ffffff;
}

.type-tag.tag-group {
	background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.type-tag.tag-goods {
	background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.type-tag.tag-chat {
	background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.type-tag.tag-system {
	background: linear-gradient(135deg, #434343 0%, #000000 100%);
}

.badge {
	position: absolute;
	top: -10rpx;
	left: -10rpx;
	min-width: 20rpx;
	height: 30rpx;
	padding: 0 10rpx;
	background: #ff4757;
	color: #ffffff;
	font-size: 22rpx;
	font-weight: 800;
	border-radius: 20rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	line-height: 1;
	border: 3rpx solid #ffffff;
	box-shadow: 0 4rpx 12rpx rgba(255, 71, 87, 0.4);
}

/* ===== 正文区 ===== */
.chat-body {
	flex: 1;
	min-width: 0;
}

.chat-top {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 12rpx;
}

.chat-name {
	font-size: 30rpx;
	font-weight: 700;
	color: #1a1d21;
	max-width: 380rpx;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}

.chat-time {
	font-size: 22rpx;
	color: #9aa3af;
	flex-shrink: 0;
	margin-left: 16rpx;
	font-weight: 500;
}

.chat-bottom {
	display: flex;
	align-items: center;
}

.chat-desc {
	font-size: 26rpx;
	color: #6a7484;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
	flex: 1;
	line-height: 1.4;
}

/* ===== 空状态 / 加载 ===== */
.empty {
	display: flex;
	flex-direction: column;
	align-items: center;
	padding: 200rpx 0;
}

.empty-icon {
	font-size: 100rpx;
	margin-bottom: 20rpx;
	opacity: 0.5;
}

.empty-text {
	font-size: 28rpx;
	color: #9aa3af;
}

.state-tip {
	text-align: center;
	padding: 60rpx 0;
	color: #9aa3af;
	font-size: 28rpx;
}

.state-tip.small {
	padding: 30rpx 0;
	font-size: 24rpx;
}
</style>