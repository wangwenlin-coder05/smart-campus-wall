<template>
	<view class="page">
		<!-- 顶部标题 -->
		<view class="header">
			<text class="title">交流群</text>
			<text class="subtitle">扫码加入志同道合的圈子</text>
		</view>

		<!-- 搜索栏 -->
		<view class="search-bar">
			<input v-model="keyword" class="search-input" placeholder="搜索群名称、关键词" @confirm="handleSearch" />
			<view class="search-btn" @click="handleSearch">
				<text>搜索</text>
			</view>
		</view>

		<!-- 分类过滤器 -->
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

		<!-- 列表 -->
		<scroll-view
			class="list" scroll-y :show-scrollbar="false" @scrolltolower="loadMore">
			<view class="card" v-for="item in displayList" :key="item.id" :class="{ 'card-hot': item.isHot }">
				<!-- 热门标签 -->
				<view class="hot-badge" v-if="item.isHot">
					<text>🔥 热门</text>
				</view>

				<view class="card-top">
					<!-- 群头像 -->
					<view class="avatar-wrap">
						<image class="avatar" :src="item.logo || defaultLogo" mode="aspectFill"></image>
						<view class="members-count">
							<text>{{ item.memberCount }}</text>
						</view>
					</view>

					<!-- 群信息 -->
					<view class="info-wrap">
						<view class="info-header">
							<text class="group-name">{{ item.name }}</text>
							<view class="category-tag" :class="'cat-' + item.category">
								{{ getCategoryLabel(item.category) }}
							</view>
						</view>
						<text class="group-desc">{{ item.description }}</text>
						<view class="group-meta">
							<text class="meta-item">👥 {{ item.memberCount }}人</text>
							<text class="meta-item">📝 {{ item.postCount }}条讨论</text>
						</view>
						<view class="tag-row">
							<text class="group-tag" v-for="(tag, idx) in item.tags" :key="idx">{{ tag }}</text>
						</view>
					</view>
				</view>

				<!-- 入群按钮 -->
				<view class="join-btn" @click="showQrCode(item)">
					<text>扫码入群</text>
				</view>
			</view>

			<view class="loading" v-if="loading">
				<text>加载中...</text>
			</view>
			<view class="no-more" v-else-if="!hasMore && displayList.length > 0">
				<text>— 已加载全部 —</text>
			</view>
			<view class="empty" v-if="!loading && displayList.length === 0">
				<text>暂无相关群聊</text>
			</view>
		</scroll-view>

		<!-- 二维码弹窗 -->
		<view class="modal-overlay" v-if="showQrModal" @click="showQrModal = false">
			<view class="qr-modal" @click.stop>
				<view class="modal-close" @click="showQrModal = false">
					<text>×</text>
				</view>
				<view class="qr-avatar">
					<image class="avatar" :src="currentGroup?.logo || defaultLogo" mode="aspectFill"></image>
				</view>
				<text class="qr-name">{{ currentGroup?.name }}</text>
				<text class="qr-desc">{{ currentGroup?.description }}</text>
				<view class="qr-code-wrap">
					<image class="qr-code" :src="currentGroup?.qrCode || defaultQrCode" mode="aspectFit"></image>
					<text class="qr-tip">长按识别二维码入群</text>
				</view>
				<view class="qr-info">
					<text class="qr-info-text">{{ currentGroup?.memberCount }}人已加入 · {{ currentGroup?.postCount }}条讨论</text>
				</view>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import request from '@/utils/request.js'

const defaultLogo = 'https://picsum.photos/100/100?random=1'
const defaultQrCode = 'https://picsum.photos/300/300?random=99'

const tabs = [
	{ label: '全部', value: 'all' },
	{ label: '学习', value: 'study' },
	{ label: '兴趣', value: 'hobby' },
	{ label: '兼职', value: 'part-time' },
	{ label: '交友', value: 'friend' },
	{ label: '生活', value: 'life' }
]

const activeTab = ref('all')
const keyword = ref('')
const list = ref([])
const loading = ref(false)
const hasMore = ref(true)
const pageNum = ref(1)
const pageSize = ref(10)
const showQrModal = ref(false)
const currentGroup = ref(null)

const mockGroups = [
	{
		id: 'g1',
		name: '考研交流群',
		category: 'study',
		description: '考研党聚集地，分享备考经验、资料分享、每日打卡',
		memberCount: 1286,
		postCount: 3421,
		logo: 'https://picsum.photos/100/100?random=2',
		qrCode: '',
		tags: ['考研', '24考研', '资料分享', '打卡'],
		isHot: true
	},
	{
		id: 'g2',
		name: 'Python学习交流',
		category: 'study',
		description: '从零开始学Python，答疑解惑，代码分享，项目实战',
		memberCount: 892,
		postCount: 2156,
		logo: 'https://picsum.photos/100/100?random=3',
		qrCode: '',
		tags: ['Python', '编程', '后端', '数据分析']
	},
	{
		id: 'g3',
		name: '校园摄影爱好者',
		category: 'hobby',
		description: '热爱摄影的同学聚集地，作品分享、器材讨论、约拍组队',
		memberCount: 567,
		postCount: 1823,
		logo: 'https://picsum.photos/100/100?random=4',
		qrCode: '',
		tags: ['摄影', '器材', '约拍']
	},
	{
		id: 'g4',
		name: '健身打卡群',
		category: 'hobby',
		description: '运动健身打卡，互相监督鼓励，分享健身计划和经验',
		memberCount: 2043,
		postCount: 5632,
		logo: 'https://picsum.photos/100/100?random=5',
		qrCode: '',
		tags: ['健身', '运动', '打卡']
	},
	{
		id: 'g5',
		name: '校园兼职信息群',
		category: 'part-time',
		description: '正规兼职信息发布，家教、快递、家教、家教、家教',
		memberCount: 3256,
		postCount: 8954,
		logo: 'https://picsum.photos/100/100?random=6',
		qrCode: '',
		tags: ['兼职', '家教', '快递'],
		isHot: true
	},
	{
		id: 'g6',
		name: '周末同城交友',
		category: 'friend',
		description: '周末一起玩，吃饭、电影、桌游、户外活动约起来',
		memberCount: 1567,
		postCount: 4231,
		logo: 'https://picsum.photos/100/100?random=7',
		qrCode: '',
		tags: ['交友', '桌游', '户外']
	},
	{
		id: 'g7',
		name: '校园二手交易',
		category: 'life',
		description: '校园二手闲置物品交易，书籍、电子产品、生活用品',
		memberCount: 4521,
		postCount: 12356,
		logo: 'https://picsum.photos/100/100?random=8',
		qrCode: '',
		tags: ['二手', '闲置', '交易']
	},
	{
		id: 'g8',
		name: '英语四六级交流',
		category: 'study',
		description: '四六级备考资料分享，口语练习，听力技巧',
		memberCount: 1876,
		postCount: 5432,
		logo: 'https://picsum.photos/100/100?random=9',
		qrCode: '',
		tags: ['英语', '四六级', '口语']
	},
	{
		id: 'g9',
		name: '桌游卡牌爱好者',
		category: 'hobby',
		description: '狼人杀、剧本杀、三国杀，周末约局',
		memberCount: 756,
		postCount: 2341,
		logo: 'https://picsum.photos/100/100?random=10',
		qrCode: '',
		tags: ['桌游', '剧本杀', '狼人杀']
	},
	{
		id: 'g10',
		name: '校园美食探店',
		category: 'life',
		description: '校内外美食推荐，食堂评测，一起约饭',
		memberCount: 2876,
		postCount: 6789,
		logo: 'https://picsum.photos/100/100?random=11',
		qrCode: '',
		tags: ['美食', '探店', '约饭']
	}
]

const getCategoryLabel = (category) => {
	const map = {
		study: '学习',
		hobby: '兴趣',
		'part-time': '兼职',
		friend: '交友',
		life: '生活'
	}
	return map[category] || category
}

const displayList = computed(() => {
	let filtered = [...list.value]
	if (activeTab.value !== 'all') {
		filtered = filtered.filter(item => item.category === activeTab.value)
	}
	if (keyword.value.trim()) {
		const kw = keyword.value.trim().toLowerCase()
		filtered = filtered.filter(item => {
			return item.name.toLowerCase().includes(kw) ||
				item.description.toLowerCase().includes(kw) ||
				(item.tags || []).some(t => t.toLowerCase().includes(kw))
		})
	}
	return filtered
})

const switchTab = (value) => {
	activeTab.value = value
	keyword.value = ''
	list.value = []
	pageNum.value = 1
	hasMore.value = true
	fetchList()
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
			url: '/discussion-group/list',
			method: 'POST',
			data: {
				category: activeTab.value === 'all' ? null : activeTab.value,
				keyword: keyword.value || '',
				pageNum: pageNum.value,
				pageSize: pageSize.value
			}
		})

		if (res && res.code === 1 && res.data && Array.isArray(res.data)) {
			list.value = res.data
		} else {
			list.value = [...mockGroups]
		}
	} catch (e) {
		console.error('获取交流群列表失败', e)
		list.value = [...mockGroups]
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

const showQrCode = (item) => {
	currentGroup.value = item
	showQrModal.value = true
}

onMounted(() => {
	fetchList()
})
</script>

<style scoped>
.page {
	background: #f5f6f9;
	min-height: 100vh;
	box-sizing: border-box;
}

.header {
	position: fixed;
	top: 0;
	left: 0;
	right: 0;
	z-index: 100;
	padding: 24rpx 32rpx;
	padding-top: calc(var(--status-bar-height) + 12rpx);
	background: linear-gradient(135deg, #6c5ce7, #a29bfe);
}

.title {
	display: block;
	font-size: 40rpx;
	font-weight: 700;
	color: #fff;
}

.subtitle {
	display: block;
	font-size: 24rpx;
	color: rgba(255, 255, 255, 0.85);
	margin-top: 8rpx;
}

.search-bar {
	position: fixed;
	top: calc(130rpx + var(--status-bar-height));
	left: 0;
	right: 0;
	z-index: 99;
	display: flex;
	align-items: center;
	gap: 16rpx;
	padding: 16rpx 24rpx;
	background: #fff;
}

.search-input {
	flex: 1;
	height: 72rpx;
	padding: 0 24rpx;
	background: #f5f6f9;
	border-radius: 36rpx;
	font-size: 28rpx;
}

.search-btn {
	padding: 16rpx 28rpx;
	background: #6c5ce7;
	color: #fff;
	font-size: 26rpx;
	border-radius: 36rpx;
}

.filter-tabs {
	position: fixed;
	top: calc(130rpx + var(--status-bar-height) + 104rpx);
	left: 0;
	right: 0;
	z-index: 98;
	display: flex;
	justify-content: space-around;
	padding: 20rpx 0;
	background: #fff;
	border-bottom: 1rpx solid #f0f0f0;
}

.tab {
	padding: 12rpx 20rpx;
	font-size: 26rpx;
	color: #999;
	position: relative;
	font-weight: 500;
}

.tab.active {
	color: #6c5ce7;
	font-weight: 700;
}

.tab.active::after {
	content: '';
	position: absolute;
	bottom: -4rpx;
	left: 50%;
	transform: translateX(-50%);
	width: 40rpx;
	height: 6rpx;
	background: #6c5ce7;
	border-radius: 3rpx;
}

.list {
	padding-top: calc(140rpx + var(--status-bar-height) + 104rpx + 100rpx);
	padding-bottom: 80rpx;
	box-sizing: border-box;
}

.card {
	margin: 24rpx;
	padding: 28rpx;
	background: #fff;
	border-radius: 20rpx;
	position: relative;
	overflow: hidden;
}

.card-hot {
	border: 2rpx solid #ff9a76;
}

.hot-badge {
	position: absolute;
	top: 20rpx;
	right: 20rpx;
	padding: 6rpx 14rpx;
	background: linear-gradient(135deg, #ff9a76, #ff6b6b);
	border-radius: 20rpx;
	font-size: 20rpx;
	color: #fff;
}

.card-top {
	display: flex;
	gap: 20rpx;
	margin-bottom: 20rpx;
}

.avatar-wrap {
	position: relative;
	flex-shrink: 0;
}

.avatar {
	width: 120rpx;
	height: 120rpx;
	border-radius: 20rpx;
}

.members-count {
	position: absolute;
	bottom: -4rpx;
	left: 50%;
	transform: translateX(-50%);
	padding: 4rpx 12rpx;
	background: #6c5ce7;
	border-radius: 16rpx;
	white-space: nowrap;
}

.members-count text {
	font-size: 20rpx;
	color: #fff;
}

.info-wrap {
	flex: 1;
	min-width: 0;
}

.info-header {
	display: flex;
	align-items: center;
	gap: 12rpx;
	margin-bottom: 10rpx;
}

.group-name {
	font-size: 32rpx;
	font-weight: 700;
	color: #333;
}

.category-tag {
	font-size: 20rpx;
	padding: 4rpx 12rpx;
	border-radius: 8rpx;
	background: #f5f6f9;
	color: #666;
}

.category-tag.cat-study { background: #e3f2fd; color: #1976d2; }
.category-tag.cat-hobby { background: #fff3e0; color: #f57c00; }
.category-tag.cat-part-time { background: #e8f5e9; color: #388e3c; }
.category-tag.cat-friend { background: #fce4ec; color: #c2185b; }
.category-tag.cat-life { background: #f3e5f5; color: #7b1fa2; }

.group-desc {
	display: -webkit-box;
	-webkit-box-orient: vertical;
	-webkit-line-clamp: 2;
	overflow: hidden;
	font-size: 24rpx;
	color: #666;
	line-height: 1.5;
	margin-bottom: 12rpx;
}

.group-meta {
	display: flex;
	gap: 16rpx;
	margin-bottom: 12rpx;
}

.meta-item {
	font-size: 22rpx;
	color: #999;
}

.tag-row {
	display: flex;
	gap: 10rpx;
}

.group-tag {
	font-size: 22rpx;
	color: #6c5ce7;
	background: #f3e5f5;
	padding: 4rpx 14rpx;
	border-radius: 16rpx;
}

.join-btn {
	padding: 20rpx;
	background: linear-gradient(135deg, #6c5ce7, #a29bfe);
	color: #fff;
	text-align: center;
	border-radius: 40rpx;
	font-size: 28rpx;
	font-weight: 600;
}

.loading, .no-more, .empty {
	text-align: center;
	padding: 40rpx;
	font-size: 26rpx;
	color: #999;
}

.modal-overlay {
	position: fixed;
	inset: 0;
	z-index: 200;
	background: rgba(0, 0, 0, 0.6);
	display: flex;
	align-items: center;
	justify-content: center;
}

.qr-modal {
	width: 560rpx;
	padding: 40rpx 32rpx;
	background: #fff;
	border-radius: 24rpx;
	display: flex;
	flex-direction: column;
	align-items: center;
	position: relative;
}

.modal-close {
	position: absolute;
	top: 16rpx;
	right: 24rpx;
	width: 48rpx;
	height: 48rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 48rpx;
	color: #999;
	line-height: 1;
}

.qr-avatar {
	margin-bottom: 16rpx;
}

.qr-avatar .avatar {
	width: 100rpx;
	height: 100rpx;
	border-radius: 24rpx;
}

.qr-name {
	font-size: 34rpx;
	font-weight: 700;
	color: #333;
	margin-bottom: 8rpx;
}

.qr-desc {
	font-size: 24rpx;
	color: #666;
	text-align: center;
	padding: 0 20rpx;
	margin-bottom: 24rpx;
}

.qr-code-wrap {
	display: flex;
	flex-direction: column;
	align-items: center;
	padding: 24rpx;
	background: #f5f6f9;
	border-radius: 16rpx;
	margin-bottom: 20rpx;
}

.qr-code {
	width: 400rpx;
	height: 400rpx;
}

.qr-tip {
	font-size: 24rpx;
	color: #6c5ce7;
	margin-top: 16rpx;
}

.qr-info {
	text-align: center;
}

.qr-info-text {
	font-size: 24rpx;
	color: #999;
}
</style>
