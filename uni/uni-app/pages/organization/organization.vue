<template>
	<view class="page">
		<view class="top-panel">
			<scroll-view class="category-scroll" scroll-x :show-scrollbar="false">
				<view class="category-row">
					<view v-for="item in topCategories" :key="item.name" class="top-category"
						:class="{ active: activeCategory === item.name }" @click="selectTopCategory(item.name)">
						<text class="category-icon">{{ item.icon }}</text>
						<text>{{ item.name }}</text>
					</view>
				</view>
			</scroll-view>

			<view class="search-action-row" :class="{ focused: searchFocused }">
				<view class="search-box" :class="{ focused: searchFocused }">
					<input class="search-input" v-model.trim="keyword" confirm-type="search" placeholder="搜索感兴趣的内容"
						placeholder-class="search-placeholder" @focus="searchFocused = true"
						@blur="searchFocused = false" />
					<view class="search-btn" @click="refreshList">⌕</view>
				</view>
				<view class="inline-create-btn" @click="handleCreate">我要组局</view>
			</view>

			<view class="sort-row">
				<view class="sort-tabs">
					<view v-for="tab in sortTabs" :key="tab" class="sort-tab" :class="{ active: activeSort === tab }"
						@click="activeSort = tab">
						{{ tab }}
					</view>
				</view>
				<view class="filter-entry" @click="openFilter">筛选 ⌯</view>
			</view>
		</view>

		<scroll-view
			class="activity-scroll"
			scroll-y
			refresher-enabled
			:refresher-triggered="refreshing"
			:show-scrollbar="false"
			:scroll-into-view="scrollIntoViewId"
			@refresherrefresh="onPullRefresh"
		>
			<view class="list-wrap" v-for="item in activityList" :key="item.id">
				<view class="activity-card"
					:id="'card-' + item.id"
					:class="{ 'flash-once': flashTargetId === item.id && flashCount === 1, 'flash-twice': flashTargetId === item.id && flashCount === 2 }">
					<view class="main-row">
						<view class="poster-stack">
							<image class="poster-shadow poster-back" :src="safeImage(item.posterImg, defaultPoster)" mode="aspectFill"></image>
							<image class="poster" :src="safeImage(item.posterImg, defaultPoster)" mode="aspectFill"></image>
							<view class="poster-watermark">{{ item.tagName }}</view>
						</view>

						<view class="content">
							<view class="organizer-row">
								<image :src="safeImage(item.brandAvatar, defaultAvatar)"
									class="brand-avatar" mode="aspectFill"
									@click.stop="openHostHome(item)"
									@error="$event.target.src=defaultAvatar" />
								<text class="brand-name" @click.stop="openHostHome(item)">{{ item.brandName }}</text>
								<view class="times-pill">组局 {{ item.groupCount }} 次</view>
								<view class="collect-icon" :class="{ active: item.collected }"
									@click.stop="toggleCollect(item)">
									{{ item.collected ? '★' : '☆' }}
								</view>
							</view>

							<text class="title">{{ item.title }}</text>

							<view class="meta-row">
								<view class="meta-item meta-item-time">
									<view class="clock-icon"></view>
									<text>{{ item.activityTime }}</text>
								</view>
								<view class="meta-item meta-item-address">
									<view class="pin-icon"></view>
									<text>{{ item.address }}</text>
								</view>
							</view>

							<view class="info-chip-row">
								<view class="info-chip">{{ item.joinNum }}/{{ item.maxPeople }}人</view>
								<view class="info-chip">{{ item.genderLimit }}</view>
								<view class="info-chip">{{ item.feeType }}</view>
							</view>

							<view class="deposit-row">
								<text>{{ getDepositText(item) }}</text>
							</view>

							<view class="bottom-row">
								<view class="join-area">
									<view class="avatar-group">
										<image v-for="(avatar, avatarIndex) in item.avatarList.slice(0, 3)" :key="`${avatar}-${avatarIndex}`" class="join-avatar"
											:src="safeImage(avatar, defaultAvatar)" mode="aspectFill"
											@error="$event.target.src=defaultAvatar"></image>
									</view>
									<!-- 这里判断 -->
									<text v-if="item.joinNum !== 0" class="join-count">{{ item.joinNum }} 人报名</text>
									<text v-else class="join-count">暂无报名</text>
								</view>

								<view class="btn-group">
									<view class="hello-btn" @click="greetHost(item)">
										<text>打招呼</text>
									</view>
									<view class="signup-btn" v-if="signedActivityIds.has(item.id)" @click="showGroupCode(item)">
										<text class="go-text see-text">SEE！</text>
										<text class="signup-text">组局码</text>
									</view>
									<view class="signup-btn" v-else :class="{ disabled: item.joinNum >= item.maxPeople }"
										@click="handleSignup(item)">
										<text class="go-text">GO!</text>
										<text class="signup-text">去报名</text>
									</view>
								</view>
							</view>
						</view>
					</view>

					<view class="host-note">
						<text class="host-label">主理人说：</text>
						<text class="host-text">{{ item.hostDesc }}</text>
						<text class="quote-mark">"</text>
					</view>
				</view>
			</view>

			<view class="empty-text" v-if="activityList.length === 0">没有找到合适的组局，换个分类试试～</view>
			<view class="end-text" v-else>- 到底啦 -</view>
		</scroll-view>

		<view class="filter-mask" v-if="showFilter" @click="closeFilter"></view>
		<view class="filter-drawer" :class="{ show: showFilter }">
			<view class="drawer-header">
				<view>
					<text class="drawer-title">筛选分类</text>
				</view>
				<view class="drawer-close" @click="closeFilter">×</view>
			</view>

			<scroll-view class="drawer-content" scroll-y :show-scrollbar="false" @touchmove.stop>
				<view class="filter-group" v-for="group in categoryGroups" :key="group.name">
					<view class="filter-group-title">
						<text>{{ group.name }}</text>
						<text>{{ group.start }}～{{ group.end }}</text>
					</view>
					<view class="tag-grid">
						<view v-for="tag in group.tags" :key="tag.id" class="tag-item"
							:class="{ checked: selectedTagIds.includes(tag.id) }" @click="toggleTag(tag.id)">
							<text class="tag-no">{{ tag.id }}</text>
							<text>{{ tag.name }}</text>
						</view>
					</view>
				</view>
			</scroll-view>

			<view class="drawer-footer">
				<view class="reset-btn" @click="resetFilter">重置</view>
				<view class="confirm-filter-btn" @click="confirmFilter">确定</view>
			</view>
		</view>

		<view class="qrcode-mask" v-if="showQrcode" @click="closeQrcode">
			<view class="qrcode-modal" @click.stop>
				<view class="qrcode-header">
					<text class="qrcode-title">请扫码入群</text>
					<view class="close-btn" @click="closeQrcode">×</view>
				</view>

				<view class="qrcode-content">
					<image class="qrcode-img" :src="safeImage(qrCodeUrl, defaultQrcode)" mode="aspectFit"></image>

					<view class="activity-info">
						<text class="act-name">{{ currentActivity?.title }}</text>
						
						<view class="stats-row">
							<text class="stats-label">当前统计</text>
							<text class="stats-count">{{ currentActivity?.joinNum }}/{{ currentActivity?.maxPeople }}
								人</text>
						</view>

						<view class="button-group" v-if="isJoinedView">
							<view class="confirm-btn cancel-join-btn" @click="onCancelJoin">我要取消报名</view>
						</view>
						<view class="button-group" v-else>
							<view class="confirm-btn" @click="onConfirmSignup">确认报名</view>
							<view class="cancel-btn" @click="onCancelSignup">取消报名</view>
						</view>
					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script setup>
	import {
		computed,
		nextTick,
		onMounted,
		ref
	} from 'vue'
	import { onLoad, onShow } from '@dcloudio/uni-app'
	import request from '@/utils/request.js'
	import { getCurrentUser } from '@/utils/auth.js'
	import { buildChatUrl, buildUserHomeUrl } from '@/utils/messageCenter.js'

	const defaultPoster = '/static/organization/feature-card.jpg'
	const defaultAvatar = '/static/default-avatar.png'
	const defaultQrcode = '/static/11/1.png'

	const isBlobUrl = url => String(url || '').startsWith('blob:')
	const safeImage = (url, fallback) => {
		const value = String(url || '')
		return value && !isBlobUrl(value) ? value : fallback
	}

	const currentUserUid = computed(() => {
		const u = getCurrentUser()
		return u ? u.uid : ''
	})

	// 防抖工具函数 - 防止按钮重复点击
	const debounce = (fn, delay = 300) => {
		let timer = null
		return (...args) => {
			if (timer) clearTimeout(timer)
			timer = setTimeout(() => fn.apply(this, args), delay)
		}
	}

	const flashTargetId = ref(null)
	const flashCount = ref(0)
	const scrollIntoViewId = ref('')
	const pendingActivityId = ref('')
	const pendingFlash = ref(0)

	// 分类标签数据
	const categoryGroups = [{
			name: '体育运动',
			start: 1,
			end: 20,
			icon: '♨',
			tags: ['篮球', '足球', '羽毛球', '乒乓球', '排球', '网球', '台球', '飞盘', '游泳', '跑步', '骑行', '徒步', '露营', '健身', '瑜伽', '跳舞',
				'武术搏击', '滑板轮滑', '体育训练', '运动搭子'
			]
		},
		{
			name: '学业备考',
			start: 21,
			end: 40,
			icon: '◉',
			tags: ['自习', '考研', '考公', '英语学习', '考证备考', '期末复习', '专业课学习', '小组作业', '论文互助', '毕设组队', '编程开发', '设计剪辑', '办公软件',
				'口语练习', '阅读分享', '技能学习', '学科竞赛', '创业交流', '学习监督', '学习搭子'
			]
		},
		{
			name: '美食觅食',
			start: 41,
			end: 60,
			icon: '◌',
			tags: ['火锅', '烤肉', '烧烤', '聚餐干饭', '探店', '夜宵', '小吃', '早餐', '午饭', '晚饭', '奶茶', '咖啡', '甜品', '自助餐', '食堂搭子',
				'轻食减脂餐', '野餐', '节日聚餐', '拼桌约饭', '美食分享'
			]
		},
		{
			name: '休闲玩乐',
			start: 61,
			end: 80,
			icon: '◎',
			tags: ['桌游', '剧本杀', '密室逃脱', '狼人杀', '麻将', '扑克', '线下牌局', '电竞开黑', '手游组队', '端游组队', '联机游戏', '网吧开黑', 'KTV',
				'看电影', 'DIY手作', '漫展', '音乐节', '演唱会', '宠物互动', '娱乐聚会'
			]
		},
		{
			name: '社交搭伴',
			start: 81,
			end: 100,
			icon: '☻',
			tags: ['逛街', '散步', '城市漫步', '网红打卡', '书店', '图书馆', '看展', '摄影', '志愿活动', '社团活动', '球赛观战', '校园活动', '新生交友',
				'同专业交流', '兴趣交友', '脱单交友', '闲聊唠嗑', '周边出游', '临时组局', '找搭子'
			]
		}
	].map(group => ({
		...group,
		tags: group.tags.map((name, index) => ({
			id: group.start + index,
			name,
			groupName: group.name
		}))
	}))

	const topCategories = computed(() => [{
		name: '全部',
		icon: '⌾'
	},
	...categoryGroups.slice(0, 5).map(item => ({
		name: item.name,
		icon: item.icon
	}))
])
	const allTags = computed(() => categoryGroups.flatMap(group => group.tags))
	const sortTabs = ['推荐', '新发', '近期','收藏']

	// 模拟后端返回活动数据（每个活动独立头像、主办方头像，对接后端直接替换此数组）
	const baseActivities = [{
			id: 1,
			tagId: 10,
			brandName: '操场夜跑团',
			groupCount: 9,
			title: '晚饭后一起跑步打卡',
			activityTime: '今天 20:10',
			address: '吉林农业科技学院操场吉林农业科技学院操场场吉林农业科技学',
			joinNum: 1,
			maxPeople: 12,
			genderLimit: '不限',
			feeType: '免费',
			depositRequired: false,
			depositAmount: 0,
			hostDesc: '配速不卷，跑完一起拉伸，新手也能轻松跟。',
			avatarList: ["/static/default-avatar.png", "/static/default-avatar.png", "/static/default-avatar.png"],
			brandAvatar: "/static/organization/feature-card.jpg"
		},
		{
			id: 2,
			tagId: 21,
			brandName: '图书馆自习局',
			groupCount: 14,
			title: '图书馆三楼番茄钟自习',
			activityTime: '明天 09:00',
			address: '图书馆 3F',
			joinNum: 1,
			maxPeople: 16,
			genderLimit: '不限',
			feeType: '免费',
			depositRequired: false,
			depositAmount: 0,
			hostDesc: '互相监督不闲聊，午饭和晚饭时间统一休息。',
			avatarList: ["/static/default-avatar.png"],
			brandAvatar: "/static/organization/feature-card.jpg"
		},
		{
			id: 3,
			tagId: 41,
			brandName: '火锅搭子集合',
			groupCount: 6,
			title: '周五火锅拼桌缺人',
			activityTime: '周五 18:30',
			address: '松北路74号',
			joinNum: 0,
			maxPeople: 8,
			genderLimit: '不限',
			feeType: 'AA',
			depositRequired: true,
			depositAmount: 10,
			hostDesc: '提前占位，口味可商量，AA 透明。',
			avatarList: [],
			brandAvatar: "/static/organization/feature-card.jpg"
		},
		{
			id: 4,
			tagId: 68,
			brandName: '峡谷开黑队',
			groupCount: 21,
			title: '今晚五排开黑补两位',
			activityTime: '今天 22:00',
			address: '线上语音房',
			joinNum: 3,
			maxPeople: 5,
			genderLimit: '不限',
			feeType: '免费',
			depositRequired: false,
			depositAmount: 0,
			hostDesc: '快乐上分，拒绝压力队友，会开麦就行。',
			avatarList: ["/static/default-avatar.png", "/static/default-avatar.png", "/static/default-avatar.png"],
			brandAvatar: "/static/organization/feature-card.jpg"
		},
		{
			id: 5,
			tagId: 88,
			brandName: '校园摄影社',
			groupCount: 12,
			title: '傍晚校园人像互拍',
			activityTime: '周六 16:20',
			address: '南湖公园东门',
			joinNum: 5,
			maxPeople: 10,
			genderLimit: '限女',
			feeType: 'AA',
			depositRequired: true,
			depositAmount: 20,
			hostDesc: '会带反光板和补光灯，适合想练人像的同学。',
			avatarList: ["/static/default-avatar.png", "/static/default-avatar.png"],
			brandAvatar: "/static/organization/feature-card.jpg"
		},
		{
			id: 6,
			tagId: 3,
			brandName: '羽毛球搭子',
			groupCount: 18,
			title: '体育馆双打缺 3 人',
			activityTime: '周四 19:30',
			address: '东区体育馆',
			joinNum: 3,
			maxPeople: 6,
			genderLimit: '不限',
			feeType: '固定人均',
			depositRequired: true,
			depositAmount: 15,
			hostDesc: '场地已订，球拍可借，强度中等。',
			avatarList: ["/static/default-avatar.png"],
			brandAvatar: "/static/organization/feature-card.jpg"
		},
		{
			id: 7,
			tagId: 22,
			brandName: '考研监督小队',
			groupCount: 7,
			title: '数学刷题互相监督',
			activityTime: '每天 19:00',
			address: '教学楼 A206',
			joinNum: 11,
			maxPeople: 20,
			genderLimit: '不限',
			feeType: '免费',
			depositRequired: false,
			depositAmount: 0,
			hostDesc: '每天打卡，错题复盘，适合需要学习氛围的同学。',
			avatarList: ["/static/default-avatar.png", "/static/default-avatar.png"],
			brandAvatar: "/static/organization/feature-card.jpg"
		},
		{
			id: 8,
			tagId: 73,
			brandName: '周末 K 歌局',
			groupCount: 4,
			title: '周六 KTV 轻松唱歌',
			activityTime: '周六 20:00',
			address: '大学城星光KTV',
			joinNum: 9,
			maxPeople: 14,
			genderLimit: '不限',
			feeType: 'AA',
			depositRequired: true,
			depositAmount: 30,
			hostDesc: '不麦霸，大家轮流唱，现场结算。',
			avatarList: ["/static/default-avatar.png"],
			brandAvatar: "/static/organization/feature-card.jpg"
		}
	]

	// 模拟接口处理
	const mockFetchActivities = () => baseActivities.map(item => {
		const tag = allTags.value.find(tagItem => tagItem.id === item.tagId)
		return {
			...item,
			category: tag?.groupName || '社交搭伴',
			tagName: tag?.name || '搭子扩列',
			posterImg: defaultPoster,
			collected: collectedActivityIds.value.has(item.id),
			qrcodeUrl: defaultQrcode
		}
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
		qrcodeUrl: safeImage(item.qrcodeUrl, defaultQrcode),
		collected: collectedActivityIds.value.has(item.id)
	})

	const fetchActivities = async () => {
		try {
			const res = await request({
				url: '/organization/list',
				method: 'POST',
				data: {
					keyword: keyword.value,
					category: activeCategory.value === '全部' ? '' : activeCategory.value,
					tagId: appliedTagIds.value.length === 1 ? appliedTagIds.value[0] : null,
					status: 'active'
				}
			})
			if (res.code !== 1) {
				throw new Error(res.msg || '获取组局失败')
			}
			const list = Array.isArray(res.data) ? res.data : []
			allActivityList.value = list.map(normalizeActivity)
		} catch (error) {
			allActivityList.value = mockFetchActivities()
		}

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

	const triggerFlash = async (activityId, flashCountValue) => {
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

	onLoad((options) => {
		if (options.activityId) {
			pendingActivityId.value = options.activityId
			pendingFlash.value = Number(options.flash) || 0
		}
	})

	// 页面状态
	const activeCategory = ref('全部')
	const activeSort = ref('推荐')
	const keyword = ref('')
	const refreshing = ref(false)
	const selectedTagIds = ref([])
	const appliedTagIds = ref([])
	const allActivityList = ref([])
	const showFilter = ref(false)
	const searchFocused = ref(false)
	const collectedActivityIds = ref(new Set(uni.getStorageSync('organizationCollectedIds') || []))
	// 报名状态持久化到本地缓存
	const signedActivityIds = ref(new Set(uni.getStorageSync('signedIds') || []))
	const showQrcode = ref(false)
	const currentActivity = ref(null)
	const qrCodeUrl = ref('')
	const isJoinedView = ref(false)

	// 筛选后的活动列表
	const activityList = computed(() => {
		const searchText = keyword.value.toLowerCase()
		let list = allActivityList.value.filter(item => {
			const matchCategory = activeCategory.value === '全部' || item.category === activeCategory.value
			const matchTags = appliedTagIds.value.length === 0 || appliedTagIds.value.includes(item.tagId)
			const matchKeyword = !searchText || [item.title, item.brandName, item.address, item.category,
					item.tagName
				]
				.some(text => String(text).toLowerCase().includes(searchText))
			return matchCategory && matchTags && matchKeyword
		})

		if (activeSort.value === '收藏') list = list.filter(item => item.collected)
		if (activeSort.value === '新发') list = [...list].sort((a, b) => b.id - a.id)
		if (activeSort.value === '近期') list = [...list].sort((a, b) => a.activityTime.localeCompare(b.activityTime,
			'zh-Hans-CN'))
		return list
	})

	// 切换顶部分类
	const selectTopCategory = (name) => {
		activeCategory.value = name
	}

	// 刷新列表
	const refreshList = () => {
		fetchActivities()
	}

	const onPullRefresh = async () => {
		refreshing.value = true
		try {
			await fetchActivities()
		} finally {
			refreshing.value = false
		}
	}

	// 打开筛选
	const openFilter = () => {
		selectedTagIds.value = [...appliedTagIds.value]
		showFilter.value = true
	}

	// 关闭筛选
	const closeFilter = () => {
		showFilter.value = false
	}

	// 切换标签选中
	const toggleTag = (id) => {
		const index = selectedTagIds.value.indexOf(id)
		if (index === -1) selectedTagIds.value.push(id)
		else selectedTagIds.value.splice(index, 1)
	}

	// 重置筛选
	const resetFilter = () => {
		selectedTagIds.value = []
		appliedTagIds.value = []
		
		activeCategory.value = '全部'
		showFilter.value = false
	}

	// 确认筛选
	const confirmFilter = () => {
		appliedTagIds.value = [...selectedTagIds.value]
		showFilter.value = false
		fetchActivities()
	}

	// 收藏 - 增加防抖
	const toggleCollect = debounce((activity) => {
		activity.collected = !activity.collected
		if (activity.collected) {
			collectedActivityIds.value.add(activity.id)
		} else {
			collectedActivityIds.value.delete(activity.id)
		}
		uni.setStorageSync('organizationCollectedIds', [...collectedActivityIds.value])
		uni.showToast({
			title: activity.collected ? '已收藏' : '已取消收藏',
			icon: 'none'
		})
	})

	// 拼接押金文案
	const getDepositText = (activity) => activity.depositRequired ? `报名押金 ¥${activity.depositAmount}` : '免报名押金'

	// 我要组局
	const buildOrganizationTargetUrl = (activity) => {
		const hostId = activity.creatorUserId || activity.hostUserId || activity.userId || activity.publisherId
		return `/pages/organization/organization?activityId=${encodeURIComponent(activity.id)}&hostId=${encodeURIComponent(hostId || '')}`
	}

	const greetHost = (activity) => {
		const hostId = activity.creatorUserId || activity.hostUserId || activity.userId || activity.publisherId
		const myId = currentUserUid.value
		const parts = [myId, hostId].filter(Boolean).sort()
		const convId = parts.length === 2 ? `friend_${parts[0]}_${parts[1]}` : `group_${activity.id}`
		uni.navigateTo({
			url: buildChatUrl({
				conversationId: convId,
				sourceType: 'group',
				sourceId: activity.id,
				name: activity.brandName || '主理人',
				avatar: safeImage(activity.brandAvatar, defaultAvatar),
				sourceTitle: activity.title || '组局活动',
				sourceDesc: `${activity.activityTime || ''}  ${activity.address || ''}`,
				targetUrl: buildOrganizationTargetUrl(activity)
			})
		})
	}

	const openHostHome = (activity) => {
		const hostId = activity.creatorUserId || activity.hostUserId || activity.userId || activity.publisherId
		if (!hostId) {
			uni.showToast({ title: '无法查看该用户主页', icon: 'none' })
			return
		}
		uni.navigateTo({
			url: buildUserHomeUrl({
				userId: hostId,
				name: activity.brandName || '主理人',
				avatar: safeImage(activity.brandAvatar, defaultAvatar)
			})
		})
	}

	const handleCreate = () => {
		uni.navigateTo({
			url: '/pages/organization/publishOrganization'
		})
	}

	// 打开报名弹窗 - 增加防抖
	function showGroupCode(item) {
		if (showQrcode.value) closeQrcode()
		currentActivity.value = item
		qrCodeUrl.value = item.qrcodeUrl || '/static/11/1.png'
		isJoinedView.value = true
		showQrcode.value = true
	}

	const handleSignup = debounce((activity) => {
		if (showQrcode.value) closeQrcode()
		currentActivity.value = activity
		qrCodeUrl.value = activity.qrcodeUrl || '/static/11/1.png'
		isJoinedView.value = false
		showQrcode.value = true
	})

	// 确认报名
	const onConfirmSignup = async () => {
		const activity = currentActivity.value
		if (!activity) return

		const user = getCurrentUser()
		if (!user || !user.uid) {
			uni.showToast({
				title: '请先登录',
				icon: 'none'
			})
			return
		}

		if (signedActivityIds.value.has(activity.id)) {
			uni.showToast({
				title: '您已经报名过该活动',
				icon: 'none',
				duration: 1500
			})
			return
		}

		if (activity.joinNum >= activity.maxPeople) {
			uni.showToast({
				title: '该活动已满员',
				icon: 'none',
				duration: 1500
			})
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
			const updatedActivity = normalizeActivity(res.data || {})
			const index = allActivityList.value.findIndex(item => item.id === activity.id)
			if (index !== -1 && updatedActivity.id) {
				allActivityList.value.splice(index, 1, {
					...allActivityList.value[index],
					...updatedActivity
				})
			}
			signedActivityIds.value.add(activity.id)
			uni.setStorageSync('signedIds', [...signedActivityIds.value])
			uni.showToast({
				title: '报名成功',
				icon: 'success',
				duration: 1500
			})

			const imgUrl = activity.posterImg || ''
			if (imgUrl && imgUrl.trim() !== '' && imgUrl !== defaultPoster) {
				try {
					uni.showLoading({ title: '正在保存海报...' })
					const downloadRes = await new Promise((resolve, reject) => {
						uni.downloadFile({
							url: imgUrl,
							success: (dRes) => {
								if (dRes.statusCode === 200) {
									resolve(dRes)
								} else {
									reject(new Error('下载失败'))
								}
							},
							fail: reject
						})
					})
					await new Promise((resolve, reject) => {
						uni.saveImageToPhotosAlbum({
							filePath: downloadRes.tempFilePath,
							success: resolve,
							fail: reject
						})
					})
					uni.hideLoading()
					uni.showToast({
						title: '海报已保存到相册',
						icon: 'success',
						duration: 1500
					})
				} catch (err) {
					uni.hideLoading()
				}
			}

			closeQrcode()
		} catch (error) {
			uni.showToast({
				title: '报名失败，请稍后再试',
				icon: 'none'
			})
		}
	}

	// 取消已报名的活动
	const onCancelJoin = async () => {
		const activity = currentActivity.value
		if (!activity) return

		const user = getCurrentUser()
		if (!user || !user.uid) {
			uni.showToast({ title: '请先登录', icon: 'none' })
			return
		}

		uni.showModal({
			title: '确认取消',
			content: '确定要取消报名吗？',
			success: async (res) => {
				if (res.confirm) {
					try {
						const apiRes = await request({
							url: '/organization/leave',
							method: 'POST',
							data: {
								activityId: activity.id,
								userId: user.uid
							}
						})
						if (apiRes.code === 1) {
							signedActivityIds.value.delete(activity.id)
							uni.setStorageSync('signedIds', [...signedActivityIds.value])
							uni.showToast({ title: '已取消报名', icon: 'success' })
							closeQrcode()
							fetchActivities()
						} else {
							uni.showToast({ title: apiRes.msg || '取消失败', icon: 'none' })
						}
					} catch (e) {
						uni.showToast({ title: '取消失败，请稍后再试', icon: 'none' })
					}
				}
			}
		})
	}

	// 取消报名
	const onCancelSignup = () => {
		closeQrcode()
	}

	// 关闭二维码弹窗
	const closeQrcode = () => {
		showQrcode.value = false
		currentActivity.value = null
		qrCodeUrl.value = ''
		isJoinedView.value = false
	}

	onMounted(() => {
		fetchActivities()
	})

	onShow(() => {
		fetchActivities()
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
		padding: 18rpx 18rpx 0;
		padding-top: var(--status-bar-height);
		box-sizing: border-box;
		background: linear-gradient(180deg, #eaf9fc 0%, #f7f7f7 28%, #f5f5f5 100%);
		color: #1f1a2b;
		position: relative;
		overflow: hidden;
		display: flex;
		flex-direction: column;
	}

	.top-panel {
		flex-shrink: 0;
		padding: 18rpx 18rpx 18rpx;
		background: linear-gradient(180deg, #eaf9fc 0%, rgba(247, 247, 247, 0.94) 100%);
	}

	.category-scroll {
		width: 100%;
		white-space: nowrap;
	}

	.category-row {
	display: flex;
	gap: 12rpx;
	padding: 4rpx 0 18rpx;
}

.top-category {
	height: 68rpx;
	padding: 0 20rpx;
	border-radius: 999rpx;
	background: rgba(255, 255, 255, 0.88);
	color: #252333;
	font-size: 25rpx;
	font-weight: 800;
	display: flex;
	align-items: center;
	gap: 8rpx;
	box-shadow: 0 8rpx 22rpx rgba(20, 18, 36, 0.06);
	flex-shrink: 0;
}

	.top-category.active {
		background: #3d0639;
		color: #ffffff;
	}

	.category-icon {
		font-size: 28rpx;
	}

	.search-action-row {
		position: relative;
		display: flex;
		align-items: center;
		gap: 16rpx;
	}

	.search-box {
		position: relative;
		z-index: 2;
		flex: 1;
		height: 86rpx;
		border: 4rpx solid #c9edf7;
		border-radius: 28rpx;
		background: #ffffff;
		display: flex;
		align-items: center;
		padding: 0 12rpx 0 22rpx;
		box-sizing: border-box;
		box-shadow: 0 16rpx 34rpx rgba(92, 203, 224, 0.12);
		transition: margin-right 0.34s cubic-bezier(0.2, 0.72, 0.18, 1), border-color 0.28s ease, box-shadow 0.28s ease;
	}

	.search-box.focused {
		margin-right: -176rpx;
		border-color: #9bd9ef;
		box-shadow: 0 22rpx 42rpx rgba(92, 203, 224, 0.18);
	}

	.inline-create-btn {
		flex: 0 0 160rpx;
		height: 86rpx;
		border-radius: 28rpx;
		background: #3d0639;
		color: #ffffff;
		font-size: 27rpx;
		font-weight: 900;
		display: flex;
		align-items: center;
		justify-content: center;
		box-shadow: 0 16rpx 34rpx rgba(61, 6, 57, 0.16);
		transition: opacity 0.24s ease, transform 0.34s cubic-bezier(0.2, 0.72, 0.18, 1);
	}

	.search-action-row.focused .inline-create-btn {
		opacity: 0;
		transform: translateX(18rpx) scale(0.96);
	}

	.search-input {
		flex: 1;
		height: 76rpx;
		font-size: 28rpx;
		color: #1f1a2b;
	}

	.search-placeholder {
		color: #888991;
	}

	.search-btn {
		width: 116rpx;
		height: 66rpx;
		border-radius: 26rpx;
		background: #bce5f2;
		color: #1e2530;
		font-size: 46rpx;
		font-weight: 900;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.sort-row {
		height: 78rpx;
		display: flex;
		align-items: center;
		justify-content: space-between;
	}

	.sort-tabs {
		display: flex;
		gap: 38rpx;
		align-items: center;
	}

	.sort-tab {
		position: relative;
		color: #3a3442;
		font-size: 29rpx;
		font-weight: 700;
		padding: 14rpx 0;
	}

	.sort-tab.active {
		color: #3d0639;
		font-weight: 900;
	}

	.sort-tab.active::after {
		content: '';
		position: absolute;
		left: 50%;
		bottom: 6rpx;
		width: 42rpx;
		height: 8rpx;
		border-radius: 99rpx;
		background: #bae4f4;
		transform: translateX(-50%);
	}

	.filter-entry {
		color: #211625;
		font-size: 27rpx;
		font-weight: 900;
	}

	.activity-scroll {
		flex: 1;
		height: auto;
		box-sizing: border-box;
		overflow: hidden;
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

	.signup-btn {
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

	.hello-btn {
		width: 112rpx;
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

	.hello-btn:active {
		transform: scale(0.96);
		opacity: 0.9;
	}

	.signup-btn:active {
		transform: rotate(-1deg) scale(0.96);
		opacity: 0.9;
	}

	.signup-btn.disabled {
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

	.see-text {
		color: #78e08f;
	}

	.signup-text {
		font-size: 25rpx;
		line-height: 1.1;
		font-weight: 900;
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
		color: #384c5f;
		font-weight: 800;
	}

	.quote-mark {
		position: absolute;
		right: 24rpx;
		bottom: -30rpx;
		color: rgba(173, 204, 231, 0.72);
		font-size: 72rpx;
		font-weight: 900;
		line-height: 1;
	}

	.end-text,
	.empty-text {
		text-align: center;
		color: #a4a4ad;
		font-size: 24rpx;
		width: 100%;
	}

	.filter-mask {
		position: fixed;
		top: 0;
		left: 0;
		right: 0;
		bottom: 0;
		background: rgba(0, 0, 0, 0.42);
		z-index: 80;
	}

	.filter-drawer {
		position: fixed;
		right: 0;
		top: 0;
		bottom: 0;
		width: 650rpx;
		max-width: 90vw;
		background: #ffffff;
		z-index: 81;
		transform: translateX(100%);
		transition: transform 0.24s ease;
		display: flex;
		flex-direction: column;
		border-radius: 30rpx 0 0 30rpx;
		overflow: hidden;
	}

	.filter-drawer.show {
		transform: translateX(0);
	}

	.drawer-header {
		padding: 34rpx 28rpx 22rpx;
		display: flex;
		align-items: center;
		justify-content: space-between;
		border-bottom: 1rpx solid #edf0f4;
		flex-shrink: 0;
	}

	.drawer-title {
		display: block;
		font-size: 34rpx;
		font-weight: 900;
		color: #1f1a2b;
	}

	.drawer-close {
		width: 58rpx;
		height: 58rpx;
		border-radius: 50%;
		background: #f2f5f8;
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 42rpx;
		color: #57616d;
	}

	.drawer-content {
		height: calc(100vh - 178rpx);
		min-height: 0;
		padding: 24rpx 24rpx 126rpx;
		box-sizing: border-box;
		overflow: hidden;
	}

	.filter-group {
		margin-bottom: 30rpx;
	}

	.filter-group-title {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-bottom: 14rpx;
		color: #202435;
		font-size: 27rpx;
		font-weight: 900;
	}

	.filter-group-title text:last-child {
		color: #a1a8b0;
		font-size: 22rpx;
	}

	.tag-grid {
		display: flex;
		flex-wrap: wrap;
		gap: 14rpx 12rpx;
	}

	.tag-item {
		min-width: 176rpx;
		max-width: 100%;
		height: 58rpx;
		padding: 0 14rpx;
		box-sizing: border-box;
		border-radius: 16rpx;
		background: #f4f7fa;
		color: #4c5664;
		font-size: 24rpx;
		font-weight: 800;
		display: flex;
		align-items: center;
		gap: 8rpx;
	}

	.tag-item text:last-child {
		min-width: 0;
		white-space: nowrap;
		overflow: hidden;
		text-overflow: ellipsis;
	}

	.tag-item.checked {
		background: #3d0639;
		color: #ffffff;
	}

	.tag-no {
		font-size: 20rpx;
		opacity: 0.7;
	}

	.drawer-footer {
		position: absolute;
		left: 0;
		right: 0;
		bottom: 0;
		padding: 18rpx 24rpx 28rpx;
		background: #ffffff;
		box-shadow: 0 -8rpx 26rpx rgba(31, 38, 50, 0.06);
		display: flex;
		gap: 18rpx;
	}

	.reset-btn,
	.confirm-filter-btn {
		flex: 1;
		height: 78rpx;
		border-radius: 999rpx;
		font-size: 28rpx;
		font-weight: 900;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.reset-btn {
		background: #f2f5f8;
		color: #54606d;
	}

	.confirm-filter-btn {
		background: #3d0639;
		color: #ffffff;
	}

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

	.cancel-join-btn {
		background: linear-gradient(135deg, #e74c3c 0%, #c0392b 100%);
		color: #ffffff;
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