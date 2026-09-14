import config from '@/config/env.js'
import { getCurrentUser, getCurrentUserId } from '@/utils/auth.js'
import { pushLocalSystemMessage, getLocalSystemMessages, getSystemUnreadCount, saveChatSourceInfo, saveChatSourceInfoBySource } from '@/utils/messageCenter.js'

let ws = null
let reconnectTimer = null
let manualClose = false
let wsConnected = false
const listeners = []
const statusListeners = []
const CHAT_NOTICE_KEY = 'localChatNotices'

/**
 * 构建 WebSocket 连接地址：
 *   - Vite dev 代理模式：使用当前页面的 host，路径保留 /ws 前缀，
 *     由 vite.config.js 的 server.proxy['/ws'] 转发到 ws://localhost:8080/ws/...
 *   - 绝对地址模式：直接用 config.wsBaseUrl + '/ws/...'
 */
function buildWsUrl(path, query) {
  const fullPath = (path.startsWith('/') ? path : '/' + path) + (query ? '?' + query : '')
  if (config.useViteProxy && typeof location !== 'undefined' && location) {
    const wsProto = location.protocol === 'https:' ? 'wss:' : 'ws:'
    return `${wsProto}//${location.host}${fullPath}`
  }
  // 非代理：绝对地址；业务路径已经以 /ws 开头就直接拼接，否则拼 config.wsBaseUrl
  if (fullPath.startsWith('/ws') && config.wsBaseUrl) {
    // config.wsBaseUrl 形如 ws://xxx:8080；把 wsBaseUrl 里 host 提取后拼 /ws/... 也行，
    // 直接依赖后端端口和 ws 根路径一致的约定：wsBaseUrl + path
    return config.wsBaseUrl + fullPath
  }
  return config.wsBaseUrl + fullPath
}

function shouldKeepMessage(content) {
    if (!content) return false
    const skipKeywords = [
        '已进入聊天',
        '已离开聊天',
        '进入群聊',
        '退出群聊',
        '加入群聊',
        '退出了群聊',
        '加入了群聊'
    ]
    return !skipKeywords.some(kw => content.includes(kw))
}

function notifyListeners(type, payload) {
    listeners.forEach(fn => {
        try { fn(type, payload) } catch (e) {}
    })
}

function notifyStatusListeners(connected) {
    statusListeners.forEach(fn => {
        try { fn(connected) } catch (e) {}
    })
}

function getLocalChatNotices() {
    const list = uni.getStorageSync(CHAT_NOTICE_KEY)
    return Array.isArray(list) ? list : []
}

function pushLocalChatNotice(data) {
    const conversationId = data.conversationId
    if (!conversationId) return null

    const now = data.timestamp || Date.now()
    const list = getLocalChatNotices()
    const idx = list.findIndex(item => item.id === conversationId)
    const old = idx > -1 ? list[idx] : {}
    const item = {
        id: conversationId,
        name: data.title || data.senderName || old.name || '消息对话',
        avatar: data.senderAvatar || old.avatar || '',
        lastMsg: data.content || old.lastMsg || '收到一条新消息',
        time: data.time || '刚刚',
        timeSort: now,
        unread: (old.unread || 0) + 1,
        isPinned: false,
        type: 'chat',
        sourceType: data.sourceType || 'chat',
        sourceId: data.sourceId || old.sourceId || '',
        sourceTitle: data.sourceTitle || old.sourceTitle || '',
        sourceDesc: data.sourceDesc || old.sourceDesc || '',
        targetUrl: data.targetUrl || old.targetUrl || ''
    }
    if (item.sourceTitle || item.sourceDesc || item.targetUrl) {
        saveChatSourceInfo(conversationId, item)
        saveChatSourceInfoBySource(item.sourceType, item.sourceId, item)
    }
    if (idx > -1) list.splice(idx, 1)
    list.unshift(item)
    uni.setStorageSync(CHAT_NOTICE_KEY, list.slice(0, 100))
    return item
}

function connect() {
    if (ws) return
    const userId = getCurrentUserId()
    if (!userId) return

    manualClose = false
    const user = getCurrentUser()
    const token = user && user.token ? user.token : ''
    const params = [
        `conversationId=${encodeURIComponent(`notice_${userId}`)}`,
        `userId=${encodeURIComponent(userId)}`,
        `nickname=${encodeURIComponent(user?.nickname || user?.username || '用户')}`,
        `avatar=${encodeURIComponent(user?.avatar || '')}`,
        `sourceType=system`,
        `sourceId=${encodeURIComponent(userId)}`,
        `title=${encodeURIComponent('系统通知')}`,
        `conversationAvatar=${encodeURIComponent('/static/default-avatar.png')}`,
        `token=${encodeURIComponent(token)}`
    ].join('&')
    // 使用统一地址构造：Vite dev 走 ws 代理，其他场景走绝对地址
    const wsUrl = buildWsUrl('/ws/chat', params)

    try {
        ws = uni.connectSocket({
            url: wsUrl,
            complete: () => {}
        })

        ws.onOpen(() => {
            wsConnected = true
            notifyStatusListeners(true)
        })

        ws.onMessage((res) => {
            try {
                const data = typeof res.data === 'string' ? JSON.parse(res.data) : res.data
                const content = data.content || data.message || ''

                if (!shouldKeepMessage(content)) {
                    return
                }

                if (data.type === 'chat_notice') {
                    const notice = pushLocalChatNotice(data)
                    updateGlobalBadge()
                    notifyListeners('chat', notice)
                    return
                }

                const validTypes = ['order_status', 'system', 'order', 'notification', 'notice']
                const isSystemMsg = validTypes.includes(data.type) || !data.type
                if (!isSystemMsg && !content && !data.title) {
                    return
                }

                const orderNo = data.orderNo || data.orderId || ''
				let targetUrl = data.targetUrl || ''
				const sourceId = data.sourceId || ''

				// 失物招领相关通知：跳转到失物招领详情页
				if (!targetUrl && !orderNo) {
					const lostFoundKeywords = ['申请领取', '申请归还', '领取申请', '归还申请', '失物', '招领']
					const text = `${data.title || ''}${data.content || ''}${data.desc || ''}`
					const isLostFound = lostFoundKeywords.some(kw => text.includes(kw))
					if (isLostFound && sourceId) {
						targetUrl = `/pages/lostFound/lostFoundDetail?id=${encodeURIComponent(sourceId)}`
					}
				}

				// 帖子互动通知：跳转到帖子详情页
				if (!targetUrl && !orderNo) {
					const postKeywords = ['赞了你的帖子', '收藏了你的帖子', '关注了你', '评论了你的帖子']
					const isPostNotice = postKeywords.some(kw => (data.content || '').includes(kw))
					if (isPostNotice && sourceId) {
						targetUrl = `/pages/wall/wall?postNo=${encodeURIComponent(sourceId)}`
					}
				}

				// 组局报名通知：跳转到组局详情页
				if (!targetUrl && !orderNo) {
					const activityKeywords = ['报名了你的组局']
					const isActivityNotice = activityKeywords.some(kw => (data.content || '').includes(kw))
					if (isActivityNotice && sourceId) {
						targetUrl = `/pages/organization/organizationDetail?activityId=${encodeURIComponent(sourceId)}`
					}
				}

				// 订单通知：默认跳转订单页
				if (!targetUrl && orderNo) {
					targetUrl = `/pages/order/myOrder?orderNo=${encodeURIComponent(orderNo)}`
				}
                const title = data.title || data.typeName || (orderNo ? '订单通知' : '系统通知')

                pushLocalSystemMessage({
                    title,
                    lastMsg: content || data.lastMsg || '您有一条新的系统消息',
                    desc: data.desc || '',
                    sourceId: orderNo || sourceId,
                    targetUrl,
                    orderNo: orderNo || sourceId,
                    timeSort: data.timestamp || Date.now()
                })

                notifyListeners('system', getLocalSystemMessages())

                updateGlobalBadge()
            } catch (e) {
            }
        })

        ws.onError(() => {
            wsConnected = false
            notifyStatusListeners(false)
            scheduleReconnect()
        })

        ws.onClose(() => {
            ws = null
            wsConnected = false
            notifyStatusListeners(false)
            if (!manualClose) {
                scheduleReconnect()
            }
        })
    } catch (err) {
        scheduleReconnect()
    }
}

function scheduleReconnect() {
    if (manualClose || reconnectTimer) return
    reconnectTimer = setTimeout(() => {
        reconnectTimer = null
        connect()
    }, 3000)
}

export function initGlobalWebSocket() {
    connect()
}

export function reconnectGlobalWebSocket() {
    closeGlobalWebSocket()
    manualClose = false
    connect()
}

export function closeGlobalWebSocket() {
    manualClose = true
    if (reconnectTimer) {
        clearTimeout(reconnectTimer)
        reconnectTimer = null
    }
    if (ws) {
        try { ws.close({}) } catch (e) {}
        ws = null
    }
}

export function resetLocalChatNotice(conversationId) {
    if (!conversationId) return
    const list = getLocalChatNotices()
    const updated = list.map(item => item.id === conversationId ? { ...item, unread: 0 } : item)
    uni.setStorageSync(CHAT_NOTICE_KEY, updated)
}

export function onGlobalMessage(fn) {
    listeners.push(fn)
    return () => {
        const idx = listeners.indexOf(fn)
        if (idx > -1) listeners.splice(idx, 1)
    }
}

function getChatUnreadCount() {
    return getLocalChatNotices().reduce((sum, m) => sum + (m.unread || 0), 0)
}

function updateGlobalBadge() {
    const chatUnread = getChatUnreadCount()
    const systemUnread = getSystemUnreadCount()
    const total = chatUnread + systemUnread
    if (total > 0) {
        uni.setTabBarBadge({ index: 2, text: total > 99 ? '99+' : String(total) }).catch(() => {})
    } else {
        uni.removeTabBarBadge({ index: 2 }).catch(() => {})
    }
}

export { getLocalChatNotices, getChatUnreadCount, updateGlobalBadge }

export function isGlobalWsConnected() {
    return wsConnected
}

export function onGlobalWsStatus(fn) {
    statusListeners.push(fn)
    return () => {
        const idx = statusListeners.indexOf(fn)
        if (idx > -1) statusListeners.splice(idx, 1)
    }
}