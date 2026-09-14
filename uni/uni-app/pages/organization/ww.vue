<template>
	<view class="page">
		<scroll-view class="page-scroll" scroll-y enhanced :show-scrollbar="false">
			<view class="hero">
				<view class="hero-bg">
					<view class="court-line line-a"></view>
					<view class="court-line line-b"></view>
					<view class="court-dot dot-a"></view>
					<view class="court-dot dot-b"></view>
				</view>

				<view class="topbar">
					<view>
						<text class="eyebrow">CAMPUS CREW</text>
						<text class="hero-title">今晚组个局</text>
					</view>
					<view class="city-pill">石家庄</view>
				</view>

				<view class="hero-copy">
					<text>从羽毛球、自习到剧本杀，找到同频的人，一起把校园生活排满。</text>
				</view>

				<view class="hero-action-row">
					<view class="hero-action primary">发起组局</view>
					<view class="hero-action ghost">搜索活动</view>
				</view>

				<view class="hero-card">
					<view class="hero-card-left">
						<text class="mini-label">今日热招</text>
						<text class="mini-title">羽毛球双打练习</text>
						<text class="mini-meta">19:30 体育馆 3/6 人</text>
					</view>
					<view class="mini-avatars">
						<view class="mini-avatar avatar-1"></view>
						<view class="mini-avatar avatar-2"></view>
						<view class="mini-avatar avatar-3"></view>
					</view>
				</view>
			</view>

			<view class="section hot-section">
				<view class="section-head">
					<text class="section-title">热门入口</text>
					<text class="section-more">10 个高频标签</text>
				</view>
				<view class="hot-grid">
					<view
						v-for="item in hotTags"
						:key="item.name"
						class="hot-item"
						:class="item.tone"
					>
						<text class="hot-name">{{ item.name }}</text>
						<text class="hot-desc">{{ item.desc }}</text>
					</view>
				</view>
			</view>

			<view class="section">
				<view class="section-head">
					<text class="section-title">分类找局</text>
					<text class="section-more">5 类 / 100 标签</text>
				</view>
				<scroll-view class="category-scroll" scroll-x :show-scrollbar="false">
					<view class="category-row">
						<view
							v-for="group in categoryGroups"
							:key="group.name"
							class="category-card"
							:class="{ active: activeGroup === group.name }"
							@click="activeGroup = group.name"
						>
							<text class="category-name">{{ group.name }}</text>
							<text class="category-count">20 项</text>
							<view class="category-preview">
								<text v-for="tag in group.tags.slice(0, 3)" :key="tag">{{ tag }}</text>
							</view>
						</view>
					</view>
				</scroll-view>
			</view>

			<view class="section">
				<view class="section-head">
					<text class="section-title">正在招募</text>
					<view class="filter-chip">推荐</view>
				</view>

				<view class="activity-list">
					<view v-for="item in activities" :key="item.id" class="activity-card">
						<view class="activity-cover" :class="item.coverClass">
							<image
								v-if="item.image"
								class="cover-image"
								:src="item.image"
								mode="aspectFill"
							></image>
							<view class="cover-mark">
								<text>{{ item.tag }}</text>
							</view>
						</view>

						<view class="activity-body">
							<view class="activity-top">
								<view>
									<text class="activity-title">{{ item.title }}</text>
									<text class="activity-sub">{{ item.sub }}</text>
								</view>
								<view class="status-pill" :class="item.statusClass">{{ item.status }}</view>
							</view>

							<view class="info-grid">
								<view class="info-item">
									<text class="info-label">时间</text>
									<text class="info-value">{{ item.time }}</text>
								</view>
								<view class="info-item">
									<text class="info-label">地点</text>
									<text class="info-value">{{ item.place }}</text>
								</view>
								<view class="info-item">
									<text class="info-label">费用</text>
									<text class="info-value">{{ item.cost }}</text>
								</view>
								<view class="info-item">
									<text class="info-label">限制</text>
									<text class="info-value">{{ item.limit }}</text>
								</view>
							</view>

							<view class="progress-block">
								<view class="progress-meta">
									<text>{{ item.joined }}/{{ item.total }} 人已报名</text>
									<text>{{ item.views }} 浏览</text>
								</view>
								<view class="progress-track">
									<view class="progress-fill" :style="{ width: item.percent + '%' }"></view>
								</view>
							</view>

							<view class="activity-bottom">
								<view class="organizer">
									<view class="organizer-avatar"></view>
									<text>{{ item.owner }}</text>
								</view>
								<view class="card-actions">
									<view class="collect-btn" :class="{ active: item.collected }" @click="item.collected = !item.collected">
										{{ item.collected ? '已收藏' : '收藏' }}
									</view>
									<view class="code-btn" v-if="item.isJoined" @click="showGroupCode(item)">组局码</view>
										<view class="join-btn" v-else>去报名</view>
								</view>
							</view>
						</view>
					</view>
				</view>
			</view>

			<view class="section publish-section">
				<view class="section-head">
					<text class="section-title">发布组局</text>
					<text class="section-more">静态表单预览</text>
				</view>
				<view class="publish-card">
					<view class="publish-title-row">
						<text class="publish-title">创建一场清晰可报名的活动</text>
						<view class="publish-badge">二维码入群</view>
					</view>
					<view class="field-grid">
						<view v-for="field in publishFields" :key="field" class="field-pill">{{ field }}</view>
					</view>
					<view class="flow-row">
						<view class="flow-step">
							<text class="flow-num">1</text>
							<text>浏览活动</text>
						</view>
						<view class="flow-line"></view>
						<view class="flow-step">
							<text class="flow-num">2</text>
							<text>点击报名</text>
						</view>
						<view class="flow-line"></view>
						<view class="flow-step">
							<text class="flow-num">3</text>
							<text>扫码入群</text>
						</view>
					</view>
				</view>
			</view>

			<view class="section tag-library">
				<view class="section-head">
					<text class="section-title">全部标签库</text>
					<text class="section-more">发布时使用</text>
				</view>
				<view v-for="group in categoryGroups" :key="group.name" class="tag-group">
					<view class="tag-group-title">
						<text>{{ group.name }}</text>
						<text>20</text>
					</view>
					<view class="tag-cloud">
						<view v-for="tag in group.tags" :key="tag" class="tag-chip">{{ tag }}</view>
					</view>
				</view>
			</view>
		</scroll-view>

		<view class="float-create">+</view>
	</view>
</template>

<script setup>
import { ref } from 'vue'

const hotTags = [
	{ name: '交友', desc: '同频扩列', tone: 'tone-green' },
	{ name: '羽毛球', desc: '约场双打', tone: 'tone-mint' },
	{ name: '篮球', desc: '半场开黑', tone: 'tone-orange' },
	{ name: '自习', desc: '安静搭子', tone: 'tone-blue' },
	{ name: '考研', desc: '互相监督', tone: 'tone-ink' },
	{ name: '干饭', desc: '拼桌约饭', tone: 'tone-yellow' },
	{ name: '奶茶', desc: '顺路拼单', tone: 'tone-pink' },
	{ name: '剧本杀', desc: '缺人补位', tone: 'tone-red' },
	{ name: '电竞开黑', desc: '今晚上分', tone: 'tone-cyan' },
	{ name: '逛街', desc: '周末出门', tone: 'tone-lime' }
]

const categoryGroups = [
	{
		name: '运动健身',
		tags: ['篮球', '足球', '羽毛球', '乒乓球', '排球', '网球', '台球', '飞盘', '游泳', '跑步', '骑行', '徒步', '露营', '健身', '瑜伽', '跳舞', '武术搏击', '滑板轮滑', '体育训练', '运动搭子']
	},
	{
		name: '学习提升',
		tags: ['自习', '考研', '考公', '英语学习', '考证备考', '期末复习', '专业课学习', '小组作业', '论文互助', '毕设组队', '编程开发', '设计剪辑', '办公软件', '口语练习', '阅读分享', '技能学习', '学科竞赛', '创业交流', '学习监督', '学习搭子']
	},
	{
		name: '美食聚餐',
		tags: ['火锅', '烤肉', '烧烤', '聚餐干饭', '探店', '夜宵', '小吃', '早餐', '午饭', '晚饭', '奶茶', '咖啡', '甜品', '自助餐', '食堂搭子', '轻食减脂餐', '野餐', '节日聚餐', '拼桌约饭', '美食分享']
	},
	{
		name: '娱乐游戏',
		tags: ['桌游', '剧本杀', '密室逃脱', '狼人杀', '麻将', '扑克', '棋牌游戏', '电竞开黑', '手游组队', '端游组队', '联机游戏', '网吧开黑', 'KTV', '看电影', 'DIY手作', '漫展', '音乐节', '演唱会', '宠物互动', '娱乐聚会']
	},
	{
		name: '校园社交',
		tags: ['逛街', '散步', '城市漫步', '网红打卡', '书店', '图书馆', '看展', '摄影', '志愿活动', '社团活动', '球赛观战', '校园活动', '新生交友', '同专业交流', '兴趣交友', '脱单交友', '闲聊唠嗑', '周边出游', '临时组局', '搭子扩列']
	}
]

const activeGroup = ref('运动健身')

const activities = ref([
	{
		id: 1,
		tag: '羽毛球',
		title: '周四晚羽毛球双打，缺 3 人',
		sub: '新手友好，场地已订，球拍可借',
		time: '周四 19:30',
		place: '东区体育馆',
		cost: 'AA 18元',
		limit: '不限',
		joined: 3,
		total: 6,
		percent: 50,
		views: 128,
		owner: '林夕',
		status: '招募中',
		statusClass: 'recruit',
		coverClass: 'cover-sport',
		collected: false,
		isJoined: false
	},
	{
		id: 2,
		tag: '自习',
		title: '图书馆三楼考研监督局',
		sub: '番茄钟学习，晚饭后复盘今日进度',
		time: '今天 18:40',
		place: '图书馆 3F',
		cost: '免费',
		limit: '限 8 人',
		joined: 6,
		total: 8,
		percent: 75,
		views: 96,
		owner: '小何',
		status: '快满员',
		statusClass: 'almost',
		coverClass: 'cover-study',
		collected: true,
		isJoined: true,
		groupCode: 'ZK2025'
	},
	{
		id: 3,
		tag: '赛车',
		title: '周末卡丁车体验组局',
		sub: '大学城出发，适合第一次玩车的同学',
		time: '周六 14:00',
		place: '近郊卡丁车馆',
		cost: '固定 79元',
		limit: '不限',
		joined: 9,
		total: 12,
		percent: 75,
		views: 241,
		owner: '车友会',
		status: '招募中',
		statusClass: 'recruit',
		coverClass: 'cover-race',
		image: '/static/organization/feature-card.png',
		collected: false,
		isJoined: false
	}
])

const publishFields = [
	'活动标题',
	'一级分类',
	'二级标签',
	'活动时间',
	'报名截止',
	'活动地点',
	'人数上限',
	'性别限制',
	'费用类型',
	'报名押金',
	'活动简介',
	'活动配图'
]

function showGroupCode(item) {
	uni.showModal({
		title: '组局码',
		content: '组局码：' + (item.groupCode || '暂无') + '\n请凭此码联系主理人入群',
		showCancel: false,
		confirmText: '知道了'
	})
}

</script>

<style scoped>
page {
	background: #eef3ef;
}

.page {
	min-height: 100vh;
	background:
		linear-gradient(180deg, #e8fff4 0%, #f4f6ef 34%, #eef2f4 100%);
	color: #17211d;
	position: relative;
}

.page-scroll {
	height: 100vh;
}

.hero {
	position: relative;
	margin: 0 0 34rpx;
	padding: 72rpx 30rpx 32rpx;
	min-height: 520rpx;
	overflow: hidden;
	background: linear-gradient(138deg, #101a17 0%, #164f43 48%, #caff67 118%);
	border-radius: 0 0 44rpx 44rpx;
}

.hero-bg {
	position: absolute;
	inset: 0;
	opacity: 0.8;
}

.court-line {
	position: absolute;
	border: 2rpx solid rgba(255, 255, 255, 0.18);
	border-radius: 999rpx;
	transform: rotate(-18deg);
}

.line-a {
	width: 620rpx;
	height: 260rpx;
	right: -210rpx;
	top: 60rpx;
}

.line-b {
	width: 520rpx;
	height: 520rpx;
	left: -240rpx;
	bottom: -180rpx;
}

.court-dot {
	position: absolute;
	border-radius: 999rpx;
	filter: blur(2rpx);
}

.dot-a {
	width: 170rpx;
	height: 170rpx;
	right: 58rpx;
	top: 200rpx;
	background: linear-gradient(180deg, #ff734d, #78e36f 64%, rgba(120, 227, 111, 0));
	opacity: 0.72;
}

.dot-b {
	width: 92rpx;
	height: 92rpx;
	left: 82rpx;
	top: 274rpx;
	background: #66ff9b;
	opacity: 0.26;
}

.topbar,
.hero-copy,
.hero-action-row,
.hero-card {
	position: relative;
	z-index: 1;
}

.topbar {
	display: flex;
	justify-content: space-between;
	align-items: flex-start;
}

.eyebrow {
	display: block;
	font-size: 22rpx;
	color: rgba(210, 255, 202, 0.78);
	letter-spacing: 4rpx;
	font-weight: 700;
	margin-bottom: 14rpx;
}

.hero-title {
	display: block;
	font-size: 66rpx;
	line-height: 1.05;
	color: #ffffff;
	font-weight: 900;
}

.city-pill {
	padding: 14rpx 22rpx;
	background: rgba(255, 255, 255, 0.14);
	color: #ffffff;
	font-size: 24rpx;
	border: 1rpx solid rgba(255, 255, 255, 0.28);
	border-radius: 999rpx;
}

.hero-copy {
	width: 520rpx;
	margin-top: 26rpx;
	color: rgba(255, 255, 255, 0.78);
	font-size: 29rpx;
	line-height: 1.55;
}

.hero-action-row {
	display: flex;
	margin-top: 32rpx;
}

.hero-action {
	height: 74rpx;
	padding: 0 30rpx;
	border-radius: 24rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 28rpx;
	font-weight: 800;
	margin-right: 18rpx;
}

.hero-action.primary {
	background: #caff67;
	color: #101a17;
	box-shadow: 0 18rpx 44rpx rgba(202, 255, 103, 0.28);
}

.hero-action.ghost {
	background: rgba(255, 255, 255, 0.12);
	color: #ffffff;
	border: 1rpx solid rgba(255, 255, 255, 0.24);
}

.hero-card {
	margin-top: 34rpx;
	padding: 26rpx;
	border-radius: 30rpx;
	background: rgba(255, 255, 255, 0.13);
	border: 1rpx solid rgba(255, 255, 255, 0.26);
	backdrop-filter: blur(18rpx);
	display: flex;
	justify-content: space-between;
	align-items: center;
}

.hero-card-left text {
	display: block;
}

.mini-label {
	font-size: 22rpx;
	color: #caff67;
	margin-bottom: 8rpx;
}

.mini-title {
	font-size: 32rpx;
	color: #ffffff;
	font-weight: 800;
	margin-bottom: 8rpx;
}

.mini-meta {
	font-size: 24rpx;
	color: rgba(255, 255, 255, 0.7);
}

.mini-avatars {
	display: flex;
}

.mini-avatar {
	width: 54rpx;
	height: 54rpx;
	border-radius: 999rpx;
	border: 4rpx solid rgba(255, 255, 255, 0.82);
	margin-left: -16rpx;
}

.avatar-1 {
	background: #f5a46a;
}

.avatar-2 {
	background: #7ed7ff;
}

.avatar-3 {
	background: #caff67;
}

.section {
	padding: 0 26rpx;
	margin-bottom: 38rpx;
}

.section-head {
	display: flex;
	align-items: center;
	justify-content: space-between;
	margin-bottom: 22rpx;
}

.section-title {
	font-size: 38rpx;
	font-weight: 900;
	color: #17211d;
}

.section-more {
	font-size: 24rpx;
	color: #7d8a82;
}

.hot-grid {
	display: grid;
	grid-template-columns: repeat(2, 1fr);
	gap: 18rpx;
}

.hot-item {
	height: 132rpx;
	border-radius: 24rpx;
	padding: 22rpx;
	box-sizing: border-box;
	position: relative;
	overflow: hidden;
	box-shadow: 0 16rpx 34rpx rgba(34, 48, 42, 0.08);
}

.hot-item::after {
	content: '';
	position: absolute;
	right: -22rpx;
	bottom: -28rpx;
	width: 100rpx;
	height: 100rpx;
	border-radius: 999rpx;
	background: rgba(255, 255, 255, 0.35);
}

.hot-name,
.hot-desc {
	display: block;
	position: relative;
	z-index: 1;
}

.hot-name {
	font-size: 31rpx;
	font-weight: 900;
	color: #102019;
	margin-bottom: 10rpx;
}

.hot-desc {
	font-size: 23rpx;
	color: rgba(16, 32, 25, 0.62);
}

.tone-green { background: linear-gradient(135deg, #caff67, #8cf5a1); }
.tone-mint { background: linear-gradient(135deg, #9ff7dc, #e8ffe6); }
.tone-orange { background: linear-gradient(135deg, #ffb35c, #ffe08a); }
.tone-blue { background: linear-gradient(135deg, #a9d7ff, #e7f5ff); }
.tone-ink { background: linear-gradient(135deg, #25322d, #6a7f75); }
.tone-ink .hot-name,
.tone-ink .hot-desc { color: #ffffff; }
.tone-yellow { background: linear-gradient(135deg, #ffe66d, #fff8c8); }
.tone-pink { background: linear-gradient(135deg, #ffafcf, #ffe2ee); }
.tone-red { background: linear-gradient(135deg, #ff7a6b, #ffd0bd); }
.tone-cyan { background: linear-gradient(135deg, #6ee7f5, #cafbff); }
.tone-lime { background: linear-gradient(135deg, #d8ff84, #f6ffd8); }

.category-scroll {
	white-space: nowrap;
}

.category-row {
	display: flex;
	padding-bottom: 4rpx;
}

.category-card {
	width: 238rpx;
	min-height: 186rpx;
	margin-right: 18rpx;
	padding: 24rpx;
	border-radius: 28rpx;
	background: rgba(255, 255, 255, 0.78);
	border: 1rpx solid rgba(255, 255, 255, 0.84);
	box-shadow: 0 14rpx 32rpx rgba(34, 48, 42, 0.07);
	box-sizing: border-box;
	display: inline-flex;
	flex-direction: column;
}

.category-card.active {
	background: #17211d;
}

.category-card.active .category-name,
.category-card.active .category-count {
	color: #ffffff;
}

.category-card.active .category-preview text {
	background: rgba(202, 255, 103, 0.16);
	color: #caff67;
}

.category-name {
	font-size: 30rpx;
	font-weight: 900;
	color: #17211d;
}

.category-count {
	font-size: 22rpx;
	color: #7d8a82;
	margin-top: 8rpx;
}

.category-preview {
	display: flex;
	flex-wrap: wrap;
	margin-top: 18rpx;
}

.category-preview text {
	font-size: 21rpx;
	color: #5c6a62;
	background: #eff5ed;
	border-radius: 999rpx;
	padding: 7rpx 12rpx;
	margin: 0 8rpx 8rpx 0;
}

.filter-chip {
	padding: 10rpx 22rpx;
	border-radius: 999rpx;
	background: #17211d;
	color: #ffffff;
	font-size: 24rpx;
	font-weight: 700;
}

.activity-list {
	display: flex;
	flex-direction: column;
}

.activity-card {
	margin-bottom: 24rpx;
	border-radius: 34rpx;
	overflow: hidden;
	background: rgba(255, 255, 255, 0.9);
	box-shadow: 0 22rpx 44rpx rgba(31, 49, 41, 0.1);
	border: 1rpx solid rgba(255, 255, 255, 0.95);
}

.activity-cover {
	height: 188rpx;
	position: relative;
	overflow: hidden;
}

.cover-sport {
	background:
		linear-gradient(135deg, rgba(202, 255, 103, 0.86), rgba(64, 191, 134, 0.92)),
		repeating-linear-gradient(45deg, transparent 0 28rpx, rgba(255,255,255,0.18) 28rpx 31rpx);
}

.cover-study {
	background:
		linear-gradient(135deg, rgba(36, 49, 95, 0.96), rgba(109, 139, 255, 0.82)),
		repeating-linear-gradient(-20deg, transparent 0 22rpx, rgba(255,255,255,0.12) 22rpx 24rpx);
}

.cover-race {
	background: #101316;
}

.cover-image {
	width: 100%;
	height: 100%;
	opacity: 0.92;
}

.cover-mark {
	position: absolute;
	left: 24rpx;
	bottom: 22rpx;
	padding: 10rpx 18rpx;
	border-radius: 999rpx;
	background: rgba(255, 255, 255, 0.82);
	color: #101a17;
	font-size: 24rpx;
	font-weight: 900;
}

.activity-body {
	padding: 26rpx;
}

.activity-top {
	display: flex;
	justify-content: space-between;
	align-items: flex-start;
	margin-bottom: 22rpx;
}

.activity-title {
	display: block;
	font-size: 34rpx;
	font-weight: 900;
	color: #17211d;
	line-height: 1.35;
	max-width: 520rpx;
}

.activity-sub {
	display: block;
	font-size: 24rpx;
	color: #718078;
	line-height: 1.45;
	margin-top: 8rpx;
	max-width: 520rpx;
}

.status-pill {
	flex-shrink: 0;
	padding: 9rpx 16rpx;
	border-radius: 999rpx;
	font-size: 22rpx;
	font-weight: 800;
}

.status-pill.recruit {
	background: #e8ffd0;
	color: #2d7b38;
}

.status-pill.almost {
	background: #fff1cf;
	color: #a36710;
}

.info-grid {
	display: grid;
	grid-template-columns: repeat(2, 1fr);
	gap: 14rpx;
	margin-bottom: 22rpx;
}

.info-item {
	background: #f4f7f2;
	border-radius: 18rpx;
	padding: 16rpx;
}

.info-label {
	display: block;
	font-size: 21rpx;
	color: #849087;
	margin-bottom: 8rpx;
}

.info-value {
	display: block;
	font-size: 25rpx;
	color: #1d2923;
	font-weight: 800;
}

.progress-block {
	margin-bottom: 24rpx;
}

.progress-meta {
	display: flex;
	justify-content: space-between;
	font-size: 23rpx;
	color: #68766e;
	margin-bottom: 10rpx;
}

.progress-track {
	height: 12rpx;
	border-radius: 999rpx;
	background: #e4ebe4;
	overflow: hidden;
}

.progress-fill {
	height: 100%;
	border-radius: 999rpx;
	background: linear-gradient(90deg, #17211d, #caff67);
}

.activity-bottom {
	display: flex;
	align-items: center;
	justify-content: space-between;
}

.organizer {
	display: flex;
	align-items: center;
	font-size: 24rpx;
	color: #5f6e66;
}

.organizer-avatar {
	width: 46rpx;
	height: 46rpx;
	border-radius: 999rpx;
	background: linear-gradient(135deg, #caff67, #7ed7ff);
	margin-right: 10rpx;
}

.card-actions {
	display: flex;
	align-items: center;
}

.collect-btn,
.join-btn {
	height: 62rpx;
	padding: 0 22rpx;
	border-radius: 999rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 24rpx;
	font-weight: 800;
	margin-left: 12rpx;
}

.collect-btn {
	color: #53625a;
	background: #eef3ef;
}

.collect-btn.active {
	color: #17211d;
	background: #d8ff84;
}

.join-btn {
	color: #ffffff;
	background: #17211d;
}

.code-btn {
	height: 62rpx;
	padding: 0 22rpx;
	border-radius: 999rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 24rpx;
	font-weight: 800;
	margin-left: 12rpx;
	color: #17211d;
	background: #d8ff84;
}

.publish-card {
	border-radius: 34rpx;
	padding: 28rpx;
	background: linear-gradient(135deg, #17211d, #2e4c3e);
	box-shadow: 0 22rpx 44rpx rgba(31, 49, 41, 0.12);
}

.publish-title-row {
	display: flex;
	justify-content: space-between;
	align-items: flex-start;
	margin-bottom: 22rpx;
}

.publish-title {
	max-width: 430rpx;
	font-size: 34rpx;
	color: #ffffff;
	font-weight: 900;
	line-height: 1.35;
}

.publish-badge {
	padding: 10rpx 16rpx;
	border-radius: 999rpx;
	background: #caff67;
	color: #17211d;
	font-size: 22rpx;
	font-weight: 900;
}

.field-grid {
	display: flex;
	flex-wrap: wrap;
	margin-bottom: 26rpx;
}

.field-pill {
	padding: 12rpx 16rpx;
	border-radius: 16rpx;
	background: rgba(255, 255, 255, 0.12);
	color: rgba(255, 255, 255, 0.86);
	font-size: 23rpx;
	margin: 0 10rpx 10rpx 0;
}

.flow-row {
	display: flex;
	align-items: center;
	justify-content: space-between;
	background: rgba(255, 255, 255, 0.1);
	border-radius: 24rpx;
	padding: 18rpx;
}

.flow-step {
	display: flex;
	flex-direction: column;
	align-items: center;
	color: #ffffff;
	font-size: 22rpx;
}

.flow-num {
	width: 40rpx;
	height: 40rpx;
	border-radius: 999rpx;
	background: #caff67;
	color: #17211d;
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 24rpx;
	font-weight: 900;
	margin-bottom: 8rpx;
}

.flow-line {
	flex: 1;
	height: 2rpx;
	background: rgba(255, 255, 255, 0.22);
	margin: 0 14rpx;
}

.tag-group {
	margin-bottom: 26rpx;
	padding: 24rpx;
	border-radius: 28rpx;
	background: rgba(255, 255, 255, 0.78);
	box-shadow: 0 14rpx 32rpx rgba(34, 48, 42, 0.06);
}

.tag-group-title {
	display: flex;
	justify-content: space-between;
	align-items: center;
	font-size: 30rpx;
	font-weight: 900;
	color: #17211d;
	margin-bottom: 18rpx;
}

.tag-cloud {
	display: flex;
	flex-wrap: wrap;
}

.tag-chip {
	padding: 12rpx 18rpx;
	border-radius: 999rpx;
	background: #eff5ed;
	color: #45534c;
	font-size: 24rpx;
	margin: 0 10rpx 10rpx 0;
}

.float-create {
	position: fixed;
	right: 34rpx;
	bottom: 112rpx;
	width: 104rpx;
	height: 104rpx;
	border-radius: 999rpx;
	background: #17211d;
	color: #caff67;
	font-size: 58rpx;
	font-weight: 300;
	display: flex;
	align-items: center;
	justify-content: center;
	box-shadow: 0 20rpx 44rpx rgba(23, 33, 29, 0.28);
	z-index: 20;
}
</style>
