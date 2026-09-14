<template>
	<view class="page">
		<!-- 顶部头部 -->
		<view class="header">
			<view class="header-top">
				<text class="title">相亲墙</text>
				<text class="header-sub">找到属于你的那个TA</text>
			</view>
			<!-- 搜索栏 -->
			<view class="search-bar">
				<view class="search-icon">🔍</view>
				<input v-model="keyword" class="search-input" placeholder="搜索昵称、学校、城市..." @confirm="handleSearch" />
			</view>
		</view>

		<!-- 数据概览卡片 -->
		<view class="overview-bar">
			<view class="overview-item" v-for="ov in overviewList" :key="ov.key">
				<text class="ov-num" :style="{ color: ov.color }">{{ ov.value }}</text>
				<text class="ov-label">{{ ov.label }}</text>
			</view>
		</view>

		<!-- 状态筛选 + 性别筛选 -->
		<view class="filter-section">
			<view class="filter-tabs">
				<view
					v-for="tab in tabs"
					:key="tab.value"
					class="tab"
					:class="{ active: activeTab === tab.value }"
					@click="switchTab(tab.value)"
				>
					<text>{{ tab.label }}</text>
				</view>
			</view>
			<view class="gender-filter">
				<view
					v-for="g in genderOptions"
					:key="g.value"
					class="gender-tag"
					:class="{ 'gender-active': genderFilter === g.value }"
					@click="switchGender(g.value)"
				>
					<text>{{ g.label }}</text>
				</view>
			</view>
		</view>

		<!-- 列表 -->
		<scroll-view
			class="list"
			scroll-y
			:show-scrollbar="false"
			@scrolltolower="loadMore"
		>
			<view
				class="card"
				v-for="item in filteredList"
				:key="item.id"
				:class="{ 'card-hot': item.isHot, 'card-new': item.isNew }"
				@click="goDetail(item)"
			>
				<!-- 热门/新人角标 -->
				<view class="corner-badge hot-badge" v-if="item.isHot">
					<text>🔥 热门</text>
				</view>
				<view class="corner-badge new-badge" v-else-if="item.isNew">
					<text>✨ 新人</text>
				</view>

				<!-- 在线状态 -->
				<view class="online-dot" :class="{ 'dot-online': item.isOnline }"></view>

				<view class="card-main">
					<!-- 头像区域 -->
					<view class="avatar-wrap">
						<image
							class="avatar"
							:src="item.avatar || defaultAvatar"
							mode="aspectFill"
						></image>
						<view class="gender-badge" :class="item.gender === 'male' ? 'gender-male' : 'gender-female'">
							<text>{{ item.gender === 'male' ? '♂' : '♀' }}</text>
						</view>
					</view>

					<!-- 信息区域 -->
					<view class="info-wrap">
						<view class="info-header">
							<text class="nickname">{{ item.nickname }}</text>
							<text class="age">{{ item.age }}岁</text>
							<text class="zodiac" v-if="item.zodiac">{{ item.zodiac }}</text>
						</view>

						<view class="info-row">
							<text class="info-tag school-tag">{{ item.school }}</text>
							<text class="info-tag">{{ item.education }}</text>
						</view>

						<view class="info-row">
							<text class="dot-label">身高 {{ item.height }}cm</text>
							<text class="dot-sep">·</text>
							<text class="dot-label">{{ item.occupation }}</text>
							<text class="dot-sep">·</text>
							<text class="dot-label">{{ item.city }}</text>
						</view>

						<!-- 兴趣爱好 -->
						<view class="hobby-row" v-if="item.hobbies && item.hobbies.length">
							<text class="hobby-tag" v-for="(h, i) in item.hobbies.slice(0, 5)" :key="i">{{ h }}</text>
							<text class="hobby-more" v-if="item.hobbies.length > 5">+{{ item.hobbies.length - 5 }}</text>
						</view>

						<!-- 自我介绍 -->
						<view class="intro-wrap">
							<text class="intro-text" :class="{ collapsed: !expandedIds.includes(item.id) }">{{ item.selfIntro }}</text>
							<text class="expand-btn" v-if="item.selfIntro.length > 50" @click.stop="toggleExpand(item.id)">
								{{ expandedIds.includes(item.id) ? ' 收起' : ' 展开' }}
							</text>
						</view>

						<!-- 期望对象 -->
						<view class="requirement-row">
							<text class="req-icon">💌</text>
							<text class="req-text">{{ item.requirements }}</text>
						</view>
					</view>
				</view>

				<!-- 底部操作栏 -->
				<view class="card-footer">
					<view class="footer-meta">
						<text class="meta-item">{{ item.viewCount || 0 }} 次浏览</text>
						<text class="meta-item">{{ item.publishTime }}</text>
						<text class="meta-item online-text" :class="{ 'text-online': item.isOnline }">
							{{ item.isOnline ? '在线' : '离线' }}
						</text>
					</view>
					<view class="footer-actions">
						<view class="like-btn" :class="{ liked: likedIds.includes(item.id) }" @click.stop="toggleLike(item)">
							<text class="like-icon">{{ likedIds.includes(item.id) ? '❤️' : '🤍' }}</text>
							<text class="like-count">{{ item.likeCount }}</text>
						</view>
						<view class="action-btn secondary" v-if="item.status !== 'in_chat'" @click.stop="handleMatch(item)">
							表白
						</view>
						<view class="action-btn chat" v-else @click.stop="goChat(item)">
							聊天
						</view>
					</view>
				</view>
			</view>

			<!-- 加载状态 -->
			<view class="loading" v-if="loading">
				<text>正在为你推荐...</text>
			</view>
			<view class="no-more" v-else-if="!hasMore && list.length > 0">
				<text>— 已经到底啦 —</text>
			</view>
			<view class="empty" v-if="!loading && filteredList.length === 0">
				<image class="empty-img" src="https://picsum.photos/200/200?random=99" mode="aspectFit"></image>
				<text class="empty-title">暂无符合条件的嘉宾</text>
				<text class="empty-sub">试试调整筛选条件吧</text>
			</view>
		</scroll-view>

		<!-- 发布按钮 -->
		<view class="fab" @click="goPublish">
			<text class="fab-icon">+</text>
		</view>

		<!-- 心动确认弹窗 -->
		<view class="modal-overlay" v-if="showMatchModal" @click="showMatchModal = false">
			<view class="match-modal" @click.stop>
				<view class="match-avatar">
					<image :src="matchedItem?.avatar || defaultAvatar" mode="aspectFill"></image>
				</view>
				<text class="match-title">向 {{ matchedItem?.nickname }} 发送心动信号</text>
				<text class="match-sub">对方确认后即可交换联系方式，开启聊天之旅</text>
				<view class="match-btn" @click="confirmMatch">
					<text>💘 确认表白</text>
				</view>
				<view class="match-cancel" @click="showMatchModal = false">
					<text>再考虑一下</text>
				</view>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getCurrentUser } from '@/utils/auth.js'
import request from '@/utils/request.js'

const defaultAvatar = 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'

// ==================== 筛选选项 ====================
const tabs = [
	{ label: '全部', value: 'all' },
	{ label: '热聊中', value: 'in_chat' },
	{ label: '待匹配', value: 'pending' },
	{ label: '已成功', value: 'matched' }
]

const genderOptions = [
	{ label: '全部', value: 'all' },
	{ label: '男生', value: 'male' },
	{ label: '女生', value: 'female' }
]

// ==================== 响应式数据 ====================
const activeTab = ref('all')
const genderFilter = ref('all')
const keyword = ref('')
const list = ref([])
const loading = ref(false)
const hasMore = ref(true)
const pageNum = ref(1)
const pageSize = ref(20)
const expandedIds = ref([])
const likedIds = ref([])
const showMatchModal = ref(false)
const matchedItem = ref(null)
const currentUid = computed(() => getCurrentUser()?.uid || '')

// 概览数据
const overviewList = computed(() => [
	{ key: 'total', label: '嘉宾', value: stats.value.total, color: '#333' },
	{ key: 'online', label: '在线', value: onlineCount.value, color: '#00b894' },
	{ key: 'matched', label: '热聊', value: stats.value.matched, color: '#e84393' },
	{ key: 'rate', label: '成功率', value: matchRate.value + '%', color: '#6c5ce7' }
])

const stats = ref({ total: 0, matched: 0, pending: 0 })
const matchRate = ref(0)
const onlineCount = ref(0)

// 前端性别筛选
const filteredList = computed(() => {
	let data = list.value
	if (genderFilter.value !== 'all') {
		data = data.filter(item => item.gender === genderFilter.value)
	}
	return data
})

// ==================== 模拟数据 ====================
const mockData = [
	// —— 女生 ——
	{
		id: 'p1',
		nickname: '小雅',
		avatar: 'https://picsum.photos/300/300?random=10',
		gender: 'female',
		age: 22,
		school: '北京大学',
		education: '硕士在读',
		major: '金融学',
		height: 165,
		occupation: '研究生',
		city: '北京',
		zodiac: '天秤座',
		hobbies: ['摄影', '旅行', '钢琴', '跑步', '烘焙', '阅读'],
		selfIntro: '北大金融研二在读，喜欢摄影和旅行，周末经常去探索北京的各大景点。希望找一个志同道合的男生，一起发现生活的美好。性格开朗乐观，善于沟通。',
		requirements: '希望对方170以上，硕士及以上学历，有责任心，喜欢旅行',
		likeCount: 128,
		viewCount: 2356,
		status: 'in_chat',
		isHot: true,
		isNew: false,
		isOnline: true,
		publishTime: '2小时前'
	},
	{
		id: 'p2',
		nickname: '林同学',
		avatar: 'https://picsum.photos/300/300?random=12',
		gender: 'female',
		age: 21,
		school: '复旦大学',
		education: '本科在读',
		major: '新闻传播',
		height: 160,
		occupation: '大三学生',
		city: '上海',
		zodiac: '双子座',
		hobbies: ['跳舞', '唱歌', '美食', '追剧', '穿搭'],
		selfIntro: '新闻专业大三学生，喜欢一切有趣的事物。希望找一个幽默有内涵的男生，能一起吃饭一起看电影就好啦～',
		requirements: '希望对方165以上，喜欢看电影，阳光开朗',
		likeCount: 206,
		viewCount: 4812,
		status: 'matched',
		isHot: true,
		isNew: false,
		isOnline: false,
		publishTime: '1天前'
	},
	{
		id: 'p3',
		nickname: '欣欣',
		avatar: 'https://picsum.photos/300/300?random=14',
		gender: 'female',
		age: 25,
		school: '上海交大',
		education: '硕士',
		major: '法学',
		height: 168,
		occupation: '律师',
		city: '上海',
		zodiac: '摩羯座',
		hobbies: ['瑜伽', '普拉提', '法律科普', '咖啡'],
		selfIntro: '独立女律师，工作认真，生活中是个小女生。希望找一个成熟稳重有上进心的男生，互相支持共同成长。',
		requirements: '希望对方175以上，有稳定工作，人品好，不抽烟',
		likeCount: 159,
		viewCount: 3120,
		status: 'pending',
		isHot: false,
		isNew: false,
		isOnline: true,
		publishTime: '3小时前'
	},
	{
		id: 'p4',
		nickname: '思思',
		avatar: 'https://picsum.photos/300/300?random=20',
		gender: 'female',
		age: 23,
		school: '武汉大学',
		education: '本科',
		major: '临床医学',
		height: 163,
		occupation: '实习医生',
		city: '武汉',
		zodiac: '巨蟹座',
		hobbies: ['看展', '徒步', '养花', '料理'],
		selfIntro: '医学生一枚，正在医院实习。平时比较忙但会抽空享受生活。性格温柔有耐心，希望遇到一个真诚善良的人。',
		requirements: '希望对方170以上，有稳定工作，温柔体贴',
		likeCount: 87,
		viewCount: 1560,
		status: 'pending',
		isHot: false,
		isNew: true,
		isOnline: false,
		publishTime: '刚刚'
	},
	{
		id: 'p5',
		nickname: '悦悦',
		avatar: 'https://picsum.photos/300/300?random=21',
		gender: 'female',
		age: 24,
		school: '中山大学',
		education: '硕士',
		major: '市场营销',
		height: 162,
		occupation: '品牌经理',
		city: '广州',
		zodiac: '射手座',
		hobbies: ['潜水', '冲浪', '摄影', '旅行'],
		selfIntro: '在广州做品牌营销，热爱大海和户外运动。性格独立自信，期待遇到一个同样热爱生活的人。',
		requirements: '希望对方173以上，喜欢运动，有共同话题',
		likeCount: 144,
		viewCount: 2890,
		status: 'pending',
		isHot: false,
		isNew: false,
		isOnline: true,
		publishTime: '5小时前'
	},
	{
		id: 'p6',
		nickname: '晴晴',
		avatar: 'https://picsum.photos/300/300?random=22',
		gender: 'female',
		age: 26,
		school: '四川大学',
		education: '博士在读',
		major: '生物工程',
		height: 166,
		occupation: '博士生',
		city: '成都',
		zodiac: '水瓶座',
		hobbies: ['实验', '养猫', '看动漫', '桌游'],
		selfIntro: '生物博士在读，实验室是我第二个家。家有两只猫主子。希望找一个能理解科研人节奏的男生。',
		requirements: '希望对方170以上，本科学历以上，喜欢小动物',
		likeCount: 66,
		viewCount: 1340,
		status: 'pending',
		isHot: false,
		isNew: true,
		isOnline: false,
		publishTime: '刚刚'
	},
	// —— 男生 ——
	{
		id: 'p7',
		nickname: '阿杰',
		avatar: 'https://picsum.photos/300/300?random=11',
		gender: 'male',
		age: 24,
		school: '清华大学',
		education: '硕士',
		major: '计算机科学',
		height: 178,
		occupation: '后端工程师',
		city: '北京',
		zodiac: '狮子座',
		hobbies: ['篮球', '游戏', '电影', '健身', '编程'],
		selfIntro: '清华硕士毕业，目前在字节跳动做后端开发。喜欢健身和篮球，周末会约朋友打球。希望找一个善良开朗的女生。',
		requirements: '希望对方160以上，本科以上，性格好，能一起看电影',
		likeCount: 95,
		viewCount: 1980,
		status: 'pending',
		isHot: false,
		isNew: false,
		isOnline: true,
		publishTime: '1天前'
	},
	{
		id: 'p8',
		nickname: '小陈',
		avatar: 'https://picsum.photos/300/300?random=13',
		gender: 'male',
		age: 23,
		school: '浙江大学',
		education: '本科',
		major: '机械工程',
		height: 175,
		occupation: '机械设计师',
		city: '杭州',
		zodiac: '金牛座',
		hobbies: ['自驾', '烹饪', '猫', '足球', '露营'],
		selfIntro: '毕业后在杭州工作，是一名机械设计师。工作稳定，生活规律。喜欢做饭和自驾游，家有两只猫猫。',
		requirements: '希望对方155以上，有爱心，喜欢小动物，温柔顾家',
		likeCount: 76,
		viewCount: 1650,
		status: 'pending',
		isHot: false,
		isNew: false,
		isOnline: false,
		publishTime: '2天前'
	},
	{
		id: 'p9',
		nickname: '阿泽',
		avatar: 'https://picsum.photos/300/300?random=15',
		gender: 'male',
		age: 26,
		school: '南京大学',
		education: '硕士',
		major: '建筑学',
		height: 180,
		occupation: '建筑师',
		city: '南京',
		zodiac: '处女座',
		hobbies: ['绘画', '旅行', '摄影', '阅读', '设计'],
		selfIntro: '建筑设计师一枚，平时工作比较忙。希望找一个理解能力强、喜欢建筑的女孩子，一起去看世界各地的建筑。',
		requirements: '希望对方160以上，喜欢艺术或建筑，本科以上学历',
		likeCount: 112,
		viewCount: 2230,
		status: 'in_chat',
		isHot: true,
		isNew: false,
		isOnline: true,
		publishTime: '3小时前'
	},
	{
		id: 'p10',
		nickname: '浩然',
		avatar: 'https://picsum.photos/300/300?random=23',
		gender: 'male',
		age: 25,
		school: '华中科技大学',
		education: '硕士',
		major: '电子信息',
		height: 176,
		occupation: '算法工程师',
		city: '深圳',
		zodiac: '白羊座',
		hobbies: ['跑步', '攀岩', '炒股', '桌球'],
		selfIntro: '在深圳做AI算法，喜欢运动和投资。性格直率，不拐弯抹角。希望遇到一个真诚开朗的女生。',
		requirements: '希望对方158以上，性格开朗，有自己的爱好',
		likeCount: 58,
		viewCount: 1120,
		status: 'pending',
		isHot: false,
		isNew: true,
		isOnline: false,
		publishTime: '刚刚'
	},
	{
		id: 'p11',
		nickname: '文博',
		avatar: 'https://picsum.photos/300/300?random=24',
		gender: 'male',
		age: 27,
		school: '西安交大',
		education: '博士',
		major: '能源动力',
		height: 182,
		occupation: '研究员',
		city: '西安',
		zodiac: '天蝎座',
		hobbies: ['历史', '书法', '登山', '围棋'],
		selfIntro: '在研究所工作，性格沉稳内向但不乏味。喜欢传统文化，周末常去爬山。希望能遇到温柔懂事的女生。',
		requirements: '希望对方160以上，知书达理，能一起安静地看书',
		likeCount: 83,
		viewCount: 1780,
		status: 'matched',
		isHot: false,
		isNew: false,
		isOnline: true,
		publishTime: '2天前'
	},
	{
		id: 'p12',
		nickname: '一帆',
		avatar: 'https://picsum.photos/300/300?random=25',
		gender: 'male',
		age: 23,
		school: '厦门大学',
		education: '本科',
		major: '海洋科学',
		height: 174,
		occupation: '海洋研究员',
		city: '厦门',
		zodiac: '双鱼座',
		hobbies: ['帆船', '游泳', '摄影', '弹吉他'],
		selfIntro: '从小在海边长大，热爱一切与海有关的事物。性格阳光开朗，会弹吉他。希望找一个同样热爱生活的女生。',
		requirements: '希望对方158以上，性格活泼，不介意异地',
		likeCount: 101,
		viewCount: 2010,
		status: 'pending',
		isHot: false,
		isNew: false,
		isOnline: true,
		publishTime: '8小时前'
	}
]

// ==================== 方法 ====================
const updateStats = (data) => {
	const total = data.length
	const matched = data.filter(item => item.status === 'in_chat').length
	const pending = data.filter(item => item.status === 'pending').length
	const online = data.filter(item => item.isOnline).length
	stats.value = { total, matched, pending }
	matchRate.value = total > 0 ? Math.round((matched / total) * 100) : 0
	onlineCount.value = online
}

const switchTab = (value) => {
	activeTab.value = value
	keyword.value = ''
	list.value = []
	pageNum.value = 1
	hasMore.value = true
	fetchList()
}

const switchGender = (value) => {
	genderFilter.value = value
}

const handleSearch = () => {
	list.value = []
	pageNum.value = 1
	hasMore.value = true
	fetchList()
}

const fetchList = async () => {
	if (loading.value) return
	loading.value = true

	try {
		const res = await request({
			url: '/partner/list',
			method: 'POST',
			data: {
				status: activeTab.value === 'all' ? null : activeTab.value,
				keyword: keyword.value || '',
				pageNum: pageNum.value,
				pageSize: pageSize.value
			}
		})

		let data = []
		if (res && res.code === 1 && res.data && Array.isArray(res.data)) {
			data = res.data
		} else {
			// 使用本地 mock 数据
			data = [...mockData]
			if (activeTab.value === 'in_chat') {
				data = data.filter(item => item.status === 'in_chat')
			} else if (activeTab.value === 'pending') {
				data = data.filter(item => item.status === 'pending')
			} else if (activeTab.value === 'matched') {
				data = data.filter(item => item.status === 'matched')
			}
			if (keyword.value) {
				const kw = keyword.value.toLowerCase()
				data = data.filter(item =>
					item.nickname.toLowerCase().includes(kw) ||
					item.school.toLowerCase().includes(kw) ||
					(item.city && item.city.toLowerCase().includes(kw))
				)
			}
		}

		list.value = data
		updateStats(data)
	} catch (e) {
		console.error('获取相亲墙列表失败', e)
	} finally {
		loading.value = false
		hasMore.value = false
	}
}

const loadMore = () => {
	if (!hasMore.value || loading.value) return
	pageNum.value++
	fetchList()
}

const toggleExpand = (id) => {
	const idx = expandedIds.value.indexOf(id)
	if (idx > -1) {
		expandedIds.value.splice(idx, 1)
	} else {
		expandedIds.value.push(id)
	}
}

const toggleLike = (item) => {
	if (!currentUid.value) {
		uni.navigateTo({ url: '/pages/login/login' })
		return
	}
	const idx = likedIds.value.indexOf(item.id)
	if (idx > -1) {
		likedIds.value.splice(idx, 1)
		item.likeCount = Math.max(0, item.likeCount - 1)
	} else {
		likedIds.value.push(item.id)
		item.likeCount++
	}
}

const handleMatch = (item) => {
	if (!currentUid.value) {
		uni.navigateTo({ url: '/pages/login/login' })
		return
	}
	matchedItem.value = item
	showMatchModal.value = true
}

const confirmMatch = async () => {
	if (!matchedItem.value) return
	try {
		const res = await request({
			url: '/partner/like',
			method: 'POST',
			data: {
				targetId: matchedItem.value.id,
				userId: currentUid.value
			}
		})
		if (res && res.code === 1) {
			uni.showToast({ title: '表白成功！', icon: 'success' })
			showMatchModal.value = false
			fetchList()
		} else {
			uni.showToast({ title: res?.msg || '表白失败', icon: 'none' })
		}
	} catch (e) {
		uni.showToast({ title: '表白失败', icon: 'none' })
	}
}

const goPublish = () => {
	if (!currentUid.value) {
		uni.navigateTo({ url: '/pages/login/login' })
		return
	}
	uni.navigateTo({ url: '/pages/partner/publishPartner' })
}

const goDetail = (item) => {
	uni.navigateTo({ url: `/pages/partner/partnerDetail?id=${item.id}` })
}

const goChat = (item) => {
	uni.navigateTo({ url: `/pages/message/message?uid=${item.id}&nickname=${item.nickname}` })
}

onMounted(() => {
	fetchList()
})
</script>

<style scoped>
.page {
	background: #f5f6f9;
	min-height: 100vh;
	padding-bottom: 120rpx;
}

/* ==================== 头部 ==================== */
.header {
	position: fixed;
	top: 0;
	left: 0;
	right: 0;
	z-index: 100;
	padding: calc(var(--status-bar-height) + 16rpx) 32rpx 20rpx 32rpx;
	background: linear-gradient(135deg, #e84393 0%, #fd79a8 50%, #fab1a0 100%);
}

.header-top {
	display: flex;
	align-items: baseline;
	justify-content: space-between;
	margin-bottom: 20rpx;
}

.title {
	font-size: 44rpx;
	font-weight: 800;
	color: #fff;
	letter-spacing: 2rpx;
}

.header-sub {
	font-size: 24rpx;
	color: rgba(255, 255, 255, 0.8);
}

.search-bar {
	display: flex;
	align-items: center;
	height: 72rpx;
	padding: 0 20rpx;
	background: rgba(255, 255, 255, 0.25);
	border-radius: 36rpx;
	backdrop-filter: blur(10rpx);
}

.search-icon {
	font-size: 28rpx;
	margin-right: 12rpx;
}

.search-input {
	flex: 1;
	height: 72rpx;
	font-size: 28rpx;
	color: #fff;
}

.search-input::placeholder {
	color: rgba(255, 255, 255, 0.65);
}

/* ==================== 概览栏 ==================== */
.overview-bar {
	display: flex;
	justify-content: space-around;
	align-items: center;
	margin: 24rpx;
	padding: 28rpx 16rpx;
	background: #fff;
	border-radius: 20rpx;
	box-shadow: 0 2rpx 16rpx rgba(0, 0, 0, 0.04);
	margin-top: calc(var(--status-bar-height) + 140rpx);
}

.overview-item {
	display: flex;
	flex-direction: column;
	align-items: center;
	gap: 6rpx;
}

.ov-num {
	font-size: 36rpx;
	font-weight: 800;
}

.ov-label {
	font-size: 22rpx;
	color: #999;
}

/* ==================== 筛选区 ==================== */
.filter-section {
	position: sticky;
	top: calc(var(--status-bar-height) + 128rpx);
	z-index: 99;
	background: #f5f6f9;
	padding: 0 24rpx 16rpx 24rpx;
}

.filter-tabs {
	display: flex;
	justify-content: space-around;
	background: #fff;
	border-radius: 16rpx;
	padding: 8rpx;
	margin-bottom: 16rpx;
}

.tab {
	flex: 1;
	text-align: center;
	padding: 16rpx 0;
	font-size: 26rpx;
	color: #999;
	border-radius: 12rpx;
	transition: all 0.25s;
}

.tab.active {
	background: linear-gradient(135deg, #e84393, #fd79a8);
	color: #fff;
	font-weight: 600;
	box-shadow: 0 2rpx 12rpx rgba(232, 67, 147, 0.3);
}

.gender-filter {
	display: flex;
	gap: 16rpx;
}

.gender-tag {
	padding: 10rpx 28rpx;
	font-size: 24rpx;
	color: #666;
	background: #fff;
	border-radius: 24rpx;
	border: 1rpx solid #eee;
	transition: all 0.2s;
}

.gender-tag.gender-active {
	background: #fff0f5;
	color: #e84393;
	border-color: #e84393;
	font-weight: 600;
}

/* ==================== 列表 ==================== */
.list {
	height: 100vh;
	padding: 0 24rpx;
	box-sizing: border-box;
}

.card {
	margin-bottom: 20rpx;
	padding: 24rpx;
	background: #fff;
	border-radius: 20rpx;
	position: relative;
	overflow: hidden;
	transition: transform 0.2s;
}

.card:active {
	transform: scale(0.985);
}

.card-hot {
	border: 2rpx solid rgba(232, 67, 147, 0.15);
}

.card-new {
	border: 2rpx solid rgba(0, 184, 148, 0.15);
}

/* 角标 */
.corner-badge {
	position: absolute;
	top: 0;
	left: 0;
	padding: 6rpx 20rpx 6rpx 16rpx;
	font-size: 20rpx;
	font-weight: 600;
	color: #fff;
	border-radius: 0 0 16rpx 0;
}

.hot-badge {
	background: linear-gradient(135deg, #e84393, #fd79a8);
}

.new-badge {
	background: linear-gradient(135deg, #00b894, #55efc4);
}

/* 在线状态点 */
.online-dot {
	position: absolute;
	top: 20rpx;
	right: 20rpx;
	width: 16rpx;
	height: 16rpx;
	border-radius: 50%;
	background: #ddd;
	border: 2rpx solid #fff;
	box-shadow: 0 0 0 2rpx #ddd;
}

.dot-online {
	background: #00b894;
	box-shadow: 0 0 0 2rpx #00b894;
}

.card-main {
	display: flex;
	gap: 20rpx;
}

.avatar-wrap {
	flex-shrink: 0;
	position: relative;
}

.avatar {
	width: 130rpx;
	height: 130rpx;
	border-radius: 50%;
	border: 3rpx solid #f0f0f0;
}

.gender-badge {
	position: absolute;
	bottom: 2rpx;
	right: 2rpx;
	width: 38rpx;
	height: 38rpx;
	border-radius: 50%;
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 24rpx;
	color: #fff;
	border: 2rpx solid #fff;
}

.gender-male {
	background: #74b9ff;
}

.gender-female {
	background: #fd79a8;
}

.info-wrap {
	flex: 1;
	min-width: 0;
}

.info-header {
	display: flex;
	align-items: center;
	gap: 10rpx;
	margin-bottom: 8rpx;
}

.nickname {
	font-size: 34rpx;
	font-weight: 700;
	color: #333;
}

.age {
	font-size: 24rpx;
	color: #666;
}

.zodiac {
	font-size: 20rpx;
	color: #fff;
	background: linear-gradient(135deg, #a29bfe, #6c5ce7);
	padding: 2rpx 10rpx;
	border-radius: 16rpx;
}

.info-row {
	display: flex;
	align-items: center;
	gap: 8rpx;
	margin-bottom: 8rpx;
	flex-wrap: wrap;
}

.info-tag {
	font-size: 22rpx;
	color: #666;
	background: #f5f6f9;
	padding: 4rpx 12rpx;
	border-radius: 6rpx;
}

.school-tag {
	color: #fff;
	background: linear-gradient(135deg, #a29bfe, #6c5ce7);
}

.dot-label {
	font-size: 22rpx;
	color: #888;
}

.dot-sep {
	font-size: 22rpx;
	color: #ddd;
}

.hobby-row {
	display: flex;
	gap: 8rpx;
	margin-bottom: 10rpx;
	flex-wrap: wrap;
}

.hobby-tag {
	font-size: 20rpx;
	color: #e84393;
	background: #fff0f5;
	padding: 3rpx 12rpx;
	border-radius: 14rpx;
}

.hobby-more {
	font-size: 20rpx;
	color: #999;
	padding: 3rpx 8rpx;
}

.intro-wrap {
	margin-bottom: 10rpx;
}

.intro-text {
	font-size: 26rpx;
	color: #555;
	line-height: 1.6;
}

.intro-text.collapsed {
	display: -webkit-box;
	-webkit-box-orient: vertical;
	-webkit-line-clamp: 2;
	overflow: hidden;
}

.expand-btn {
	font-size: 24rpx;
	color: #e84393;
}

.requirement-row {
	display: flex;
	align-items: flex-start;
	gap: 8rpx;
	background: linear-gradient(135deg, #f9f0ff, #fff5f9);
	padding: 10rpx 14rpx;
	border-radius: 10rpx;
}

.req-icon {
	font-size: 24rpx;
	flex-shrink: 0;
}

.req-text {
	font-size: 24rpx;
	color: #6c5ce7;
	line-height: 1.5;
}

/* ==================== 底部操作栏 ==================== */
.card-footer {
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding-top: 16rpx;
	margin-top: 12rpx;
	border-top: 1rpx solid #f5f5f5;
}

.footer-meta {
	display: flex;
	gap: 16rpx;
}

.meta-item {
	font-size: 20rpx;
	color: #bbb;
}

.online-text {
	color: #bbb;
}

.text-online {
	color: #00b894;
}

.footer-actions {
	display: flex;
	align-items: center;
	gap: 12rpx;
}

.like-btn {
	display: flex;
	align-items: center;
	gap: 6rpx;
	padding: 8rpx 16rpx;
	border-radius: 20rpx;
	background: #fff5f7;
}

.like-icon {
	font-size: 26rpx;
}

.like-count {
	font-size: 22rpx;
	color: #e84393;
	font-weight: 600;
}

.action-btn {
	padding: 10rpx 24rpx;
	font-size: 24rpx;
	font-weight: 600;
	border-radius: 24rpx;
	color: #fff;
}

.action-btn.secondary {
	background: linear-gradient(135deg, #e84393, #fd79a8);
}

.action-btn.chat {
	background: linear-gradient(135deg, #a29bfe, #6c5ce7);
}

/* ==================== 加载/空状态 ==================== */
.loading, .no-more {
	text-align: center;
	padding: 32rpx;
	font-size: 24rpx;
	color: #bbb;
}

.empty {
	display: flex;
	flex-direction: column;
	align-items: center;
	padding: 80rpx 40rpx;
}

.empty-img {
	width: 200rpx;
	height: 200rpx;
	border-radius: 50%;
	margin-bottom: 24rpx;
	opacity: 0.6;
}

.empty-title {
	font-size: 30rpx;
	color: #999;
	margin-bottom: 8rpx;
}

.empty-sub {
	font-size: 24rpx;
	color: #bbb;
}

/* ==================== 发布按钮 ==================== */
.fab {
	position: fixed;
	right: 32rpx;
	bottom: 80rpx;
	z-index: 100;
	width: 100rpx;
	height: 100rpx;
	border-radius: 50%;
	background: linear-gradient(135deg, #e84393, #fd79a8);
	display: flex;
	align-items: center;
	justify-content: center;
	box-shadow: 0 6rpx 24rpx rgba(232, 67, 147, 0.45);
	transition: transform 0.2s;
}

.fab:active {
	transform: scale(0.9);
}

.fab-icon {
	font-size: 56rpx;
	color: #fff;
	font-weight: 300;
	line-height: 1;
}

/* ==================== 弹窗 ==================== */
.modal-overlay {
	position: fixed;
	inset: 0;
	z-index: 200;
	background: rgba(0, 0, 0, 0.55);
	display: flex;
	align-items: center;
	justify-content: center;
}

.match-modal {
	width: 580rpx;
	padding: 48rpx 40rpx 36rpx;
	background: #fff;
	border-radius: 28rpx;
	display: flex;
	flex-direction: column;
	align-items: center;
	gap: 14rpx;
}

.match-avatar {
	width: 120rpx;
	height: 120rpx;
	border-radius: 50%;
	overflow: hidden;
	border: 4rpx solid #fd79a8;
	margin-bottom: 8rpx;
}

.match-avatar image {
	width: 100%;
	height: 100%;
}

.match-title {
	font-size: 34rpx;
	font-weight: 700;
	color: #333;
	text-align: center;
}

.match-sub {
	font-size: 26rpx;
	color: #999;
	text-align: center;
	line-height: 1.5;
}

.match-btn {
	width: 100%;
	padding: 24rpx;
	background: linear-gradient(135deg, #e84393, #fd79a8);
	color: #fff;
	font-size: 32rpx;
	font-weight: 600;
	border-radius: 48rpx;
	text-align: center;
	margin-top: 12rpx;
}

.match-cancel {
	font-size: 26rpx;
	color: #bbb;
	padding: 8rpx;
}
</style>