<template>
	<view class="container">
		<!-- 搜索栏区域 -->
		<view class="search-section">
			<view class="search-box">
				<image class="search-icon" src="/static/icons/search.svg" mode="aspectFit"></image>
				<input type="text" placeholder="搜一搜，帮你跑腿~" placeholder-class="search-placeholder" />
			</view>
		</view>

		<!-- 大功能按钮（3个左对齐，固定宽高） -->
		<view class="big-buttons">
			<view class="big-btn" @click="goOrder(1)"><!-- 图标颜色4b6cbf -->
				<image class="big-btn-icon" src="/static/index/express.png" mode="aspectFit"></image>
				<text class="big-btn-text">代取服务</text>
			</view>
			<view class="big-btn" @click="goOrder(2)">
				<image class="big-btn-icon" src="/static/index/buy.png" mode="aspectFit"></image>
				<text class="big-btn-text">帮买服务</text>
			</view>
			<view class="big-btn" @click="goOrder(3)">
				<image class="big-btn-icon" src="/static/index/run.png" mode="aspectFit"></image>
				<text class="big-btn-text">跑腿代办</text>
			</view>
		</view>

		<!-- 小服务图标网格（5列，最多3行15个） -->
		<view class="service-grid">
			<view class="grid-item" v-for="(item, idx) in gridList" :key="idx" @click="goToPage(item)">
				<image class="grid-icon" :src="item.icon" mode="aspectFit"></image>
				<text class="grid-text">{{ item.name }}</text>
			</view>
		</view>

		<!-- 会员卡横向滚动 -->
		<view class="card-section">
			<view class="section-title">
				<text class="title-text">超值会员卡</text>
				<text class="title-deco">开卡即享优惠</text>
			</view>
			<scroll-view class="card-scroll" scroll-x enable-flex :show-scrollbar="false">
				<view class="card-item" v-for="card in cardList" :key="card.id">
					<text class="card-name">{{ card.name }}</text>
					<text class="card-desc">{{ card.desc }}</text>
					<view class="price-info">
						<text class="now-price">{{ card.price }}</text>
						<text class="old-price">{{ card.oldPrice }}</text>
					</view>
					<text class="count-info">{{ card.countInfo }}</text>
					<view class="card-btn" @click="goToCardBuy(card)">去开卡 →</view>
				</view>
			</scroll-view>
		</view>

		<!-- 底部导航栏 -->
<!-- 		<view class="tab-bar">
			<view class="tab-item" v-for="(tab, idx) in tabs" :key="idx" @click="switchTab(idx)">
				<image class="tab-icon" :src="tab.icon" mode="aspectFit"></image>
				<text class="tab-text">{{ tab.name }}</text>
			</view>
		</view> -->
	</view>
</template>

<script setup>
	import {
		ref
	} from 'vue'

	// 跳转到订单页（带一级分类参数）
	const goOrder = (parentType) => {
		uni.navigateTo({
			url: `/pages/order/orderFetch?parentType=${parentType}`
		})
	}

	// 小服务网格数据（15个，5列x3行）
	const gridList = ref([{
			name: '互助接单',
			icon: '/static/index/small-Icon/组队.png',
			path: '/pages/order/partTimeOrder'
		},
		{
			name: '组局开黑',
			icon: '/static/index/small-Icon/组局.png',
			path: '/pages/organization/organization'
		},
		{
			name: '二手商城',
			icon: '/static/icons/grid/file.png',
			path: '/pages/organization/www'
		},
		{
			name: '失物招领',
			icon: '/static/icons/grid/daily.png',
			path: '/pages/lostFound/lostFound'
		},
		{
			name: '打印文件',
			icon: '/static/icons/grid/other.png',
			path: '/pages/printFile/printFile'
		},
		{
			name: '专业交流',
			icon: '/static/icons/grid/grocery.png',
			path: '/pages/professionalExchange/professionalExchange'
		},
		{
			name: '药品采购',
			icon: '/static/icons/grid/medicine.png',
			path: ''
		},
		{
			name: '零食饮料',
			icon: '/static/icons/grid/snack.png',
			path: ''
		},
		{
			name: '瓜果生鲜',
			icon: '/static/icons/grid/fruit.png',
			path: ''
		},
		{
			name: '其他帮买',
			icon: '/static/icons/grid/other2.png',
			path: ''
		},
		{
			name: '寻找替身',
			icon: '/static/icons/grid/stand.png',
			path: ''
		},
		{
			name: '校外捎带',
			icon: '/static/icons/grid/outside.png',
			path: ''
		},
		{
			name: '校内跑腿',
			icon: '/static/icons/grid/run.png',
			path: ''
		},
		{
			name: '搬运排队',
			icon: '/static/logo.png',
			path: '/pages/organization/ww'
		},
		{
			name: '其他代办',
			icon: '/static/logo.png',
			path: '/pages/organization/www'
		}
	])

	// 会员卡列表
	const cardList = ref([{
			id: 1,
			name: '取物卡',
			desc: '全场景代取',
			price: '2.2元/次',
			oldPrice: '5元/次',
			countInfo: '已售 1.2w+'
		},
		{
			id: 2,
			name: '快递卡',
			desc: '校园快递代取',
			price: '1.5元/次',
			oldPrice: '3元/次',
			countInfo: '限时特惠'
		},
		{
			id: 3,
			name: '帮买卡',
			desc: '商超食堂跑腿',
			price: '2.8元/次',
			oldPrice: '6元/次',
			countInfo: '新人首单半价'
		}
	])

	// 底部导航
	const tabs = ref([{
			name: '首页',
			icon: '/static/icons/tab/home.png'
		},
		{
			name: '订单',
			icon: '/static/icons/tab/order.png'
		},
		{
			name: '消息',
			icon: '/static/icons/tab/msg.png'
		},
		{
			name: '我的',
			icon: '/static/icons/tab/user.png'
		}
	])

	// 网格项点击（暂未绑定具体页面）
	const goToPage = (item) => {
		uni.navigateTo({
			url:item.path
		})
	}

	const goToCardBuy = (card) => {
		uni.showToast({
			title: `开通${card.name}`,
			icon: 'none'
		})
	}

	const switchTab = (idx) => {
		if (idx === 0) return
		uni.showToast({
			title: '功能开发中',
			icon: 'none'
		})
	}
</script>

<style scoped>
	/* 全局极简风格 */
	page {
		background-color: #f5f6f8;
	}

	.container {
		min-height: 100vh;
		padding: 20rpx 28rpx 160rpx;
	}

	/* 搜索栏 */
	.search-section {
		margin-bottom: 30rpx;
	}

	.search-box {
		display: flex;
		align-items: center;
		background: rgba(255, 255, 255, 0.8);
		backdrop-filter: blur(20rpx);
		border-radius: 48rpx;
		padding: 16rpx 24rpx;
		box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
	}

	.search-icon {
		width: 40rpx;
		height: 40rpx;
		margin-right: 16rpx;
		opacity: 0.6;
	}

	.search-box input {
		flex: 1;
		font-size: 28rpx;
		color: #333;
	}

	.search-placeholder {
		color: #b0b0b0;
		font-size: 26rpx;
	}

	/* 大功能按钮（左对齐，固定宽高） */
	.big-buttons {
		display: flex;
		justify-content: flex-start;
		justify-content: space-around;

		gap: 24rpx;
		margin-bottom: 40rpx;
	}

	.big-btn {
		width: 210rpx;
		height: 180rpx;
		background: rgba(255, 255, 255, 0.7);
		backdrop-filter: blur(20rpx);
		border-radius: 28rpx;
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		gap: 12rpx;
		box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.03);
		transition: all 0.2s;
	}

	.big-btn:active {
		transform: scale(0.96);
		background: rgba(255, 255, 255, 0.9);
	}

	.big-btn-icon {
		width: 75rpx;
		height: 75rpx;
	}

	.big-btn-text {
		font-size: 26rpx;
		/* font-weight: 600; */
		color:  #555;
	}

	/* 小服务网格（5列，自适应宽度） */
	.service-grid {
		background: rgba(255, 255, 255, 0.7);
		backdrop-filter: blur(20rpx);
		border-radius: 32rpx;
		padding: 30rpx 16rpx 20rpx;
		display: grid;
		grid-template-columns: repeat(5, 1fr);
		row-gap: 28rpx;
		justify-items: center;
		box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.03);
		margin-bottom: 40rpx;
	}

	.grid-item {
		display: flex;
		flex-direction: column;
		align-items: center;
		width: 100%;
	}

	.grid-icon {
		width: 64rpx;
		height: 64rpx;
		margin-bottom: 10rpx;
	}

	.grid-text {
		font-size: 22rpx;
		color: #555;
	}

	/* 会员卡区域 */
	.card-section {
		margin-bottom: 40rpx;
	}

	.section-title {
		display: flex;
		align-items: baseline;
		justify-content: space-between;
		margin-bottom: 24rpx;
		padding: 0 8rpx;
	}

	.title-text {
		font-size: 32rpx;
		font-weight: 700;
		color: #1a1a1a;
	}

	.title-deco {
		font-size: 22rpx;
		color: #7c8ba0;
		background: #f0f2f5;
		padding: 6rpx 16rpx;
		border-radius: 30rpx;
	}

	.card-scroll {
		white-space: nowrap;
		display: flex;
	}

	.card-item {
		width: 320rpx;
		border-radius: 28rpx;
		padding: 28rpx 20rpx;
		margin-right: 20rpx;
		display: inline-flex;
		flex-direction: column;
		background: rgba(255, 255, 255, 0.8);
		backdrop-filter: blur(20rpx);
		box-shadow: 0 8rpx 20rpx rgba(0, 0, 0, 0.04);
		transition: 0.2s;
	}

	.card-name {
		font-size: 34rpx;
		font-weight: 700;
		color: #1a1a1a;
	}

	.card-desc {
		font-size: 22rpx;
		color: #7c8ba0;
		margin: 8rpx 0 16rpx;
	}

	.price-info {
		display: flex;
		align-items: baseline;
		gap: 10rpx;
		margin-bottom: 12rpx;
	}

	.now-price {
		font-size: 32rpx;
		font-weight: 800;
		color: #e74c3c;
	}

	.old-price {
		font-size: 22rpx;
		color: #a0a8b0;
		text-decoration: line-through;
	}

	.count-info {
		font-size: 22rpx;
		color: #5470a6;
		background: rgba(84, 112, 166, 0.08);
		align-self: flex-start;
		padding: 6rpx 14rpx;
		border-radius: 40rpx;
		margin-bottom: 24rpx;
	}

	.card-btn {
		background-color: #2e2e2e;
		color: #ffffff;
		border-radius: 60rpx;
		font-size: 24rpx;
		padding: 14rpx 0;
		width: 100%;
		text-align: center;
		font-weight: 500;
		transition: background 0.2s;
	}

	.card-btn:active {
		background-color: #1a1a1a;
	}

	/* 底部导航栏 */
	.tab-bar {
		position: fixed;
		bottom: 0;
		left: 0;
		right: 0;
		background: rgba(255, 255, 255, 0.9);
		backdrop-filter: blur(20rpx);
		display: flex;
		justify-content: space-around;
		align-items: center;
		padding: 16rpx 24rpx 28rpx;
		padding-bottom: calc(28rpx + env(safe-area-inset-bottom));
		border-radius: 32rpx 32rpx 0 0;
		box-shadow: 0 -4rpx 20rpx rgba(0, 0, 0, 0.03);
	}

	.tab-item {
		display: flex;
		flex-direction: column;
		align-items: center;
		gap: 6rpx;
	}

	.tab-icon {
		width: 44rpx;
		height: 44rpx;
		opacity: 0.7;
	}

	.tab-text {
		font-size: 20rpx;
		color: #6c7a8d;
	}
</style>