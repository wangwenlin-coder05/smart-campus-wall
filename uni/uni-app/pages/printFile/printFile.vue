<template>
	<view class="page">
		<scroll-view class="page-scroll" scroll-y enhanced :show-scrollbar="false">
			<view class="hero">
				<view class="paper-glow glow-a"></view>
				<view class="paper-glow glow-b"></view>
				<view class="hero-top">
					<view>
						<text class="kicker">CAMPUS PRINT</text>
						<text class="hero-title">校园打印</text>
					</view>
					<view class="soft-badge">文件 · 照片 · 论文</view>
				</view>
				<text class="hero-copy">把文件交给附近的人或官方打印点，扫码进入小程序，也可以添加好友预约打印。</text>
				<view class="hero-stats">
					<view class="stat-item">
						<text class="stat-num">12</text>
						<text class="stat-label">附近点位</text>
					</view>
					<view class="stat-line"></view>
					<view class="stat-item">
						<text class="stat-num">10min</text>
						<text class="stat-label">平均取件</text>
					</view>
					<view class="stat-line"></view>
					<view class="stat-item">
						<text class="stat-num">24h</text>
						<text class="stat-label">部分可约</text>
					</view>
				</view>
			</view>

			<view class="tabs">
				<view
					v-for="tab in tabs"
					:key="tab.value"
					class="tab"
					:class="{ active: activeType === tab.value }"
					@click="activeType = tab.value"
				>
					{{ tab.label }}
				</view>
			</view>

			<view class="notice-card">
				<view class="notice-mark"></view>
				<view>
					<text class="notice-title">今日推荐</text>
					<text class="notice-text">优先展示校内认证点位和响应较快的个人打印服务。</text>
				</view>
			</view>

			<view class="service-list">
				<view
					v-for="item in filteredServices"
					:key="item.id"
					class="service-card"
				>
					<view class="card-head">
						<view class="avatar" :class="item.type">
							<text>{{ item.shortName }}</text>
						</view>
						<view class="title-block">
							<view class="name-row">
								<text class="service-name">{{ item.name }}</text>
								<view class="type-tag" :class="item.type">{{ item.typeLabel }}</view>
							</view>
							<text class="service-place">{{ item.place }}</text>
						</view>
					</view>

					<view class="desc-panel">
						<text>{{ item.desc }}</text>
					</view>

					<view class="meta-grid">
						<view class="meta-item">
							<text class="meta-label">黑白</text>
							<text class="meta-value">{{ item.priceBw }}</text>
						</view>
						<view class="meta-item">
							<text class="meta-label">彩印</text>
							<text class="meta-value">{{ item.priceColor }}</text>
						</view>
						<view class="meta-item">
							<text class="meta-label">装订</text>
							<text class="meta-value">{{ item.bind }}</text>
						</view>
					</view>

					<view class="card-foot">
						<view class="rating">
							<text class="rating-score">{{ item.score }}</text>
							<text class="rating-text">评分 · {{ item.orders }} 单</text>
						</view>
						<view class="action-btn" @click="openQr(item)">
							{{ item.actionText }}
						</view>
					</view>
				</view>
			</view>
		</scroll-view>

		<view v-if="showQr" class="qr-mask" @click="closeQr">
			<view class="qr-modal" @click.stop>
				<view class="modal-light"></view>
				<view class="modal-head">
					<view>
						<text class="modal-kicker">{{ currentService.typeLabel }}</text>
						<text class="modal-title">{{ currentService.name }}</text>
					</view>
					<view class="close" @click="closeQr">×</view>
				</view>
				<view class="qr-frame">
					<image class="qr-img" :src="currentService.qrCode" mode="aspectFit"></image>
				</view>
				<text class="modal-desc">{{ currentService.qrText }}</text>
				<view class="modal-btn" @click="confirmScan">完成</view>
			</view>
		</view>
	</view>
</template>

<script setup>
import { computed, ref } from 'vue'

const activeType = ref('all')
const showQr = ref(false)
const currentService = ref({})

const tabs = [
	{ label: '全部', value: 'all' },
	{ label: '校园官方', value: 'official' },
	{ label: '个人服务', value: 'personal' }
]

const services = ref([
	{
		id: 1,
		type: 'official',
		typeLabel: '校园官方',
		shortName: '校',
		name: '图文中心自助打印',
		place: '图书馆一层 · 西门入口',
		desc: '支持 PDF、Word、PPT 上传打印，取件码自助领取，适合论文、材料和批量文件。',
		priceBw: '¥0.15/页',
		priceColor: '¥0.80/页',
		bind: '可胶装',
		score: '4.9',
		orders: 2380,
		actionText: '扫码打印',
		qrText: '扫码进入校园打印小程序，上传文件后到图书馆取件。',
		qrCode: '/static/11/1.png'
	},
	{
		id: 2,
		type: 'personal',
		typeLabel: '个人',
		shortName: '晨',
		name: '晨光宿舍打印',
		place: '6 号宿舍楼 · 晚间可取',
		desc: '晚间文件应急打印，支持黑白、彩印和资料整理，适合课前讲义与报名表。',
		priceBw: '¥0.18/页',
		priceColor: '¥1.00/页',
		bind: '夹条',
		score: '4.8',
		orders: 642,
		actionText: '添加好友',
		qrText: '扫码添加好友，发送文件并确认取件时间。',
		qrCode: '/static/11/1.png'
	},
	{
		id: 3,
		type: 'official',
		typeLabel: '校园官方',
		shortName: '印',
		name: '教学楼云打印站',
		place: '第三教学楼 · A 区大厅',
		desc: '靠近教室，课间取件方便，支持证件照、课件、考试材料和社团海报打印。',
		priceBw: '¥0.16/页',
		priceColor: '¥0.90/页',
		bind: '订书',
		score: '4.7',
		orders: 1696,
		actionText: '扫码打印',
		qrText: '扫码进入校园打印小程序，选择第三教学楼点位。',
		qrCode: '/static/11/1.png'
	},
	{
		id: 4,
		type: 'personal',
		typeLabel: '个人',
		shortName: '南',
		name: '南苑资料小站',
		place: '南苑食堂旁 · 可送楼下',
		desc: '主打少量快印和资料装订，支持提前预约，取件时间比较灵活。',
		priceBw: '¥0.20/页',
		priceColor: '¥1.20/页',
		bind: '可骑马钉',
		score: '4.9',
		orders: 318,
		actionText: '添加好友',
		qrText: '扫码添加好友，沟通打印规格与配送位置。',
		qrCode: '/static/11/1.png'
	}
])

const filteredServices = computed(() => {
	if (activeType.value === 'all') return services.value
	return services.value.filter(item => item.type === activeType.value)
})

const openQr = item => {
	currentService.value = item
	showQr.value = true
}

const closeQr = () => {
	showQr.value = false
	currentService.value = {}
}

const confirmScan = () => {
	uni.showToast({
		title: '已为你保留入口',
		icon: 'success'
	})
	closeQr()
}
</script>

<style scoped>
page {
	background: #f5f4f1;
}

.page {
	background:
		linear-gradient(180deg, #fbfaf7 0%, #f5f4f1 44%, #eeeeeb 100%);
	color: #1d1d1f;
}

.page-scroll {
	box-sizing: border-box;
}

.hero {
	position: relative;
	margin: 28rpx 28rpx 20rpx;
	padding: 34rpx;
	min-height: 420rpx;
	border: 1rpx solid rgba(40, 40, 42, 0.08);
	border-radius: 16rpx;
	background:
		linear-gradient(135deg, rgba(255, 255, 255, 0.88), rgba(246, 244, 239, 0.72)),
		radial-gradient(circle at 18% 18%, rgba(198, 184, 255, 0.22), transparent 34%),
		radial-gradient(circle at 82% 8%, rgba(255, 255, 255, 0.86), transparent 36%);
	box-shadow: 0 28rpx 70rpx rgba(24, 24, 27, 0.08);
	overflow: hidden;
	box-sizing: border-box;
}

.hero::after {
	content: "";
	position: absolute;
	left: 38rpx;
	right: 38rpx;
	bottom: 118rpx;
	height: 1rpx;
	background: linear-gradient(90deg, transparent, rgba(29, 29, 31, 0.14), transparent);
}

.paper-glow {
	position: absolute;
	border-radius: 50%;
	filter: blur(8rpx);
	opacity: 0.74;
}

.glow-a {
	width: 260rpx;
	height: 260rpx;
	right: -88rpx;
	top: -68rpx;
	background: rgba(213, 204, 255, 0.42);
}

.glow-b {
	width: 210rpx;
	height: 210rpx;
	left: -90rpx;
	bottom: 32rpx;
	background: rgba(255, 255, 255, 0.9);
}

.hero-top,
.card-head,
.name-row,
.card-foot,
.modal-head {
	display: flex;
	align-items: center;
	justify-content: space-between;
}

.hero-top {
	position: relative;
	z-index: 1;
	margin-bottom: 42rpx;
}

.kicker,
.modal-kicker {
	display: block;
	font-size: 20rpx;
	font-weight: 800;
	color: #7c7785;
	letter-spacing: 0;
	margin-bottom: 10rpx;
}

.hero-title {
	display: block;
	font-size: 66rpx;
	line-height: 1;
	font-weight: 900;
	letter-spacing: 0;
}

.soft-badge {
	height: 54rpx;
	padding: 0 22rpx;
	border: 1rpx solid rgba(29, 29, 31, 0.1);
	border-radius: 999rpx;
	background: rgba(255, 255, 255, 0.7);
	backdrop-filter: blur(12rpx);
	font-size: 22rpx;
	font-weight: 700;
	color: #4d4a52;
	display: flex;
	align-items: center;
}

.hero-copy {
	position: relative;
	z-index: 1;
	display: block;
	width: 560rpx;
	max-width: 100%;
	font-size: 28rpx;
	line-height: 1.72;
	color: #55525b;
	margin-bottom: 58rpx;
}

.hero-stats {
	position: relative;
	z-index: 1;
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding: 24rpx 22rpx 0;
}

.stat-item {
	display: flex;
	flex-direction: column;
	align-items: center;
	min-width: 140rpx;
}

.stat-num {
	font-size: 34rpx;
	font-weight: 900;
	margin-bottom: 6rpx;
}

.stat-label {
	font-size: 21rpx;
	color: #77737d;
	font-weight: 700;
}

.stat-line {
	width: 1rpx;
	height: 42rpx;
	background: rgba(29, 29, 31, 0.12);
}

.tabs {
	display: flex;
	gap: 14rpx;
	padding: 6rpx 28rpx 22rpx;
}

.tab {
	flex: 1;
	height: 70rpx;
	border-radius: 16rpx;
	background: rgba(255, 255, 255, 0.66);
	border: 1rpx solid rgba(29, 29, 31, 0.08);
	color: #77737d;
	font-size: 25rpx;
	font-weight: 800;
	display: flex;
	align-items: center;
	justify-content: center;
}

.tab.active {
	background: #1d1d1f;
	color: #ffffff;
	box-shadow: 0 16rpx 36rpx rgba(29, 29, 31, 0.18);
}

.notice-card {
	margin: 0 28rpx 22rpx;
	padding: 22rpx;
	border-radius: 16rpx;
	background: rgba(255, 255, 255, 0.62);
	border: 1rpx solid rgba(29, 29, 31, 0.08);
	display: flex;
	gap: 18rpx;
	align-items: center;
}

.notice-mark {
	width: 54rpx;
	height: 54rpx;
	border-radius: 16rpx;
	background:
		linear-gradient(135deg, #1d1d1f 0 48%, transparent 49%),
		linear-gradient(135deg, transparent 0 52%, rgba(190, 178, 248, 0.92) 53%);
}

.notice-title {
	display: block;
	font-size: 25rpx;
	font-weight: 900;
	margin-bottom: 6rpx;
}

.notice-text {
	display: block;
	font-size: 22rpx;
	line-height: 1.45;
	color: #77737d;
}

.service-list {
	display: flex;
	flex-direction: column;
	gap: 22rpx;
	padding: 0 28rpx 48rpx;
}

.service-card {
	position: relative;
	padding: 26rpx;
	border-radius: 16rpx;
	background: rgba(255, 255, 255, 0.78);
	border: 1rpx solid rgba(29, 29, 31, 0.08);
	box-shadow: 0 22rpx 54rpx rgba(29, 29, 31, 0.07);
	overflow: hidden;
}

.service-card::before {
	content: "";
	position: absolute;
	right: -80rpx;
	top: -130rpx;
	width: 260rpx;
	height: 260rpx;
	border-radius: 50%;
	background: rgba(205, 196, 248, 0.18);
}

.card-head {
	position: relative;
	z-index: 1;
	gap: 18rpx;
	margin-bottom: 22rpx;
}

.avatar {
	width: 84rpx;
	height: 84rpx;
	border-radius: 16rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 34rpx;
	font-weight: 900;
	color: #ffffff;
	box-shadow: inset 0 0 0 1rpx rgba(255, 255, 255, 0.42);
}

.avatar.official {
	background: #1d1d1f;
}

.avatar.personal {
	background: #7f768f;
}

.title-block {
	flex: 1;
	min-width: 0;
}

.name-row {
	justify-content: flex-start;
	gap: 12rpx;
	margin-bottom: 8rpx;
}

.service-name {
	font-size: 31rpx;
	font-weight: 900;
	max-width: 370rpx;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}

.type-tag {
	flex: 0 0 auto;
	height: 38rpx;
	padding: 0 14rpx;
	border-radius: 999rpx;
	font-size: 19rpx;
	font-weight: 800;
	display: flex;
	align-items: center;
}

.type-tag.official {
	background: rgba(29, 29, 31, 0.08);
	color: #1d1d1f;
}

.type-tag.personal {
	background: rgba(127, 118, 143, 0.12);
	color: #62586f;
}

.service-place {
	display: block;
	font-size: 23rpx;
	color: #77737d;
	font-weight: 700;
}

.desc-panel {
	position: relative;
	z-index: 1;
	margin-bottom: 22rpx;
	padding: 22rpx;
	border-radius: 16rpx;
	background: rgba(245, 244, 241, 0.74);
	font-size: 25rpx;
	line-height: 1.62;
	color: #55525b;
}

.meta-grid {
	position: relative;
	z-index: 1;
	display: flex;
	gap: 12rpx;
	margin-bottom: 24rpx;
}

.meta-item {
	flex: 1;
	min-height: 92rpx;
	border-radius: 16rpx;
	background: rgba(255, 255, 255, 0.68);
	border: 1rpx solid rgba(29, 29, 31, 0.06);
	display: flex;
	flex-direction: column;
	justify-content: center;
	align-items: center;
}

.meta-label {
	font-size: 20rpx;
	color: #8a858e;
	margin-bottom: 8rpx;
}

.meta-value {
	font-size: 23rpx;
	font-weight: 900;
	color: #1d1d1f;
}

.card-foot {
	position: relative;
	z-index: 1;
}

.rating {
	display: flex;
	flex-direction: column;
}

.rating-score {
	font-size: 34rpx;
	font-weight: 900;
}

.rating-text {
	font-size: 21rpx;
	color: #77737d;
	font-weight: 700;
}

.action-btn,
.modal-btn {
	height: 70rpx;
	padding: 0 28rpx;
	border-radius: 16rpx;
	background: #1d1d1f;
	color: #ffffff;
	font-size: 25rpx;
	font-weight: 900;
	display: flex;
	align-items: center;
	justify-content: center;
	box-shadow: 0 14rpx 30rpx rgba(29, 29, 31, 0.16);
}

.qr-mask {
	position: fixed;
	left: 0;
	right: 0;
	top: 0;
	bottom: 0;
	background: rgba(22, 22, 24, 0.52);
	z-index: 99;
	display: flex;
	align-items: flex-end;
	padding: 28rpx;
	box-sizing: border-box;
}

.qr-modal {
	position: relative;
	width: 100%;
	padding: 30rpx;
	border-radius: 16rpx;
	background:
		linear-gradient(180deg, rgba(255, 255, 255, 0.96), rgba(247, 246, 243, 0.98));
	box-shadow: 0 30rpx 90rpx rgba(0, 0, 0, 0.26);
	overflow: hidden;
	box-sizing: border-box;
}

.modal-light {
	position: absolute;
	right: -80rpx;
	top: -80rpx;
	width: 240rpx;
	height: 240rpx;
	border-radius: 50%;
	background: rgba(205, 196, 248, 0.3);
}

.modal-head {
	position: relative;
	z-index: 1;
	margin-bottom: 24rpx;
}

.modal-title {
	display: block;
	font-size: 34rpx;
	font-weight: 900;
}

.close {
	width: 58rpx;
	height: 58rpx;
	border-radius: 50%;
	background: #1d1d1f;
	color: #ffffff;
	font-size: 40rpx;
	line-height: 54rpx;
	text-align: center;
	font-weight: 700;
}

.qr-frame {
	position: relative;
	z-index: 1;
	width: 360rpx;
	height: 360rpx;
	margin: 0 auto 24rpx;
	padding: 22rpx;
	border-radius: 16rpx;
	background: #ffffff;
	border: 1rpx solid rgba(29, 29, 31, 0.08);
	box-shadow: 0 18rpx 46rpx rgba(29, 29, 31, 0.08);
	box-sizing: border-box;
}

.qr-img {
	width: 100%;
	height: 100%;
	display: block;
}

.modal-desc {
	position: relative;
	z-index: 1;
	display: block;
	text-align: center;
	font-size: 25rpx;
	line-height: 1.6;
	color: #55525b;
	margin: 0 24rpx 26rpx;
}

.modal-btn {
	position: relative;
	z-index: 1;
	width: 100%;
	height: 82rpx;
	padding: 0;
}
</style>
