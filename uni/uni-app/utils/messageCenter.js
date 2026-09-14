const SYSTEM_MESSAGES_KEY = 'localSystemMessages'

export const DEFAULT_AVATAR = 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'
export const SYSTEM_AVATAR = 'https://cube.elemecdn.com/0/f/e/0f0e0d00f6c23a3ef8e9e2e3f1a00jpeg.png'

export function getLocalSystemMessages() {
	const list = uni.getStorageSync(SYSTEM_MESSAGES_KEY)
	return Array.isArray(list) ? list : []
}

export function getSystemUnreadCount() {
	return getLocalSystemMessages().reduce((sum, m) => sum + (m.unread || 0), 0)
}

export function buildUserHomeUrl(user = {}) {
	const params = [
		`userId=${encodeURIComponent(user.userId || user.uid || user.id || '')}`,
		`name=${encodeURIComponent(user.name || user.nickname || user.username || '用户')}`,
		`avatar=${encodeURIComponent(user.avatar || DEFAULT_AVATAR)}`
	].join('&')
	return `/pages/user/home?${params}`
}

export function pushLocalSystemMessage(message) {
	const now = Date.now()
	const item = {
		id: message.id || `sys_${now}_${Math.floor(Math.random() * 10000)}`,
		name: message.name || '系统通知',
		avatar: message.avatar || SYSTEM_AVATAR,
		lastMsg: message.lastMsg || message.title || '你有一条新的系统消息',
		time: message.time || '刚刚',
		timeSort: message.timeSort || now,
		unread: message.unread ?? 1,
		isPinned: false,
		type: 'system',
		sourceType: message.sourceType || 'system',
		sourceId: message.sourceId || '',
		targetUrl: message.targetUrl || '',
		title: message.title || message.lastMsg || '系统通知',
		desc: message.desc || '',
		orderNo: message.orderNo || message.sourceId || ''
	}
	const list = getLocalSystemMessages()
	list.unshift(item)
	uni.setStorageSync(SYSTEM_MESSAGES_KEY, list.slice(0, 50))
	return item
}

export function resetSystemNoticeUnread() {
	const list = getLocalSystemMessages()
	const updated = list.map(msg => ({ ...msg, unread: 0 }))
	uni.setStorageSync(SYSTEM_MESSAGES_KEY, updated)
}

export function buildFriendConversationId(userA, userB) {
	const ids = [String(userA || ''), String(userB || '')].filter(Boolean).sort()
	return `friend_${ids.join('_')}`
}

const CHAT_SOURCE_KEY = 'chatSourceInfoMap'

export function saveChatSourceInfo(conversationId, sourceInfo) {
	if (!conversationId) return
	const map = uni.getStorageSync(CHAT_SOURCE_KEY) || {}
	map[conversationId] = {
		sourceTitle: sourceInfo.sourceTitle || '',
		sourceDesc: sourceInfo.sourceDesc || '',
		targetUrl: sourceInfo.targetUrl || ''
	}
	uni.setStorageSync(CHAT_SOURCE_KEY, map)
}

export function getChatSourceKey(sourceType, sourceId) {
	if (!sourceType || !sourceId) return ''
	return `${sourceType}:${sourceId}`
}

export function saveChatSourceInfoBySource(sourceType, sourceId, sourceInfo) {
	const key = getChatSourceKey(sourceType, sourceId)
	if (!key) return
	saveChatSourceInfo(key, sourceInfo)
}

export function getChatSourceInfo(conversationId) {
	if (!conversationId) return {}
	const map = uni.getStorageSync(CHAT_SOURCE_KEY) || {}
	return map[conversationId] || {}
}

function hasChatSourceInfo(sourceInfo) {
	return !!(sourceInfo?.sourceTitle || sourceInfo?.sourceDesc || sourceInfo?.targetUrl)
}

export function getBestChatSourceInfo({ conversationId = '', sourceType = '', sourceId = '' } = {}) {
	const bySource = getChatSourceInfo(getChatSourceKey(sourceType, sourceId))
	if (hasChatSourceInfo(bySource)) return bySource
	const byConversation = getChatSourceInfo(conversationId)
	if (hasChatSourceInfo(byConversation)) return byConversation
	return {}
}

export function buildChatUrl({ conversationId, sourceType = 'chat', sourceId = '', name = '消息对话', avatar = DEFAULT_AVATAR, sourceTitle = '', sourceDesc = '', targetUrl = '' }) {
	if (conversationId && (sourceTitle || sourceDesc || targetUrl)) {
		saveChatSourceInfo(conversationId, { sourceTitle, sourceDesc, targetUrl })
	}
	if (sourceType && sourceId && (sourceTitle || sourceDesc || targetUrl)) {
		saveChatSourceInfoBySource(sourceType, sourceId, { sourceTitle, sourceDesc, targetUrl })
	}
	const params = [
		`conversationId=${encodeURIComponent(conversationId || sourceId || 'chat')}`,
		`sourceType=${encodeURIComponent(sourceType)}`,
		`sourceId=${encodeURIComponent(sourceId || conversationId || '')}`,
		`name=${encodeURIComponent(name)}`,
		`avatar=${encodeURIComponent(avatar || DEFAULT_AVATAR)}`,
		`sourceTitle=${encodeURIComponent(sourceTitle || '')}`,
		`sourceDesc=${encodeURIComponent(sourceDesc || '')}`,
		`targetUrl=${encodeURIComponent(targetUrl || '')}`
	].join('&')
	return `/pages/message/message?${params}`
}