<template>
	<view class="page">
		<view class="fixed-top">
			<!-- 搜索栏 -->
			<view class="search-bar">
				<view class="search-input-wrap">
					<text class="search-icon">🔍</text>
					<input
						class="search-input"
						type="text"
						placeholder="搜索商品名称..."
						v-model="searchKeyword"
						@confirm="handleSearch"
						confirm-type="search"
					/>
					<text v-if="searchKeyword" class="clear-btn" @click="clearSearch">✕</text>
				</view>
			</view>

			<!-- 顶部推荐 -->
<!-- 			<view class="poster-head">
				<view class="brand-row">
					<view class="brand-badge">
						<view class="badge-dot"></view>
						<text>LAGOM LAB</text>
					</view>
					<text class="brand-sub">校园闲置好物</text>
				</view>

				<view class="headline-wrap">
					<text class="headline">把闲置</text>
					<text class="headline">变成宝贝</text>
				</view>
				<text class="lead">发现同学手中的宝藏，让每一件物品遇见新主人。</text>

				<view class="hero-product" @click="openProduct(featureProduct)">
					<image class="hero-image" :src="featureProduct.images[0]" mode="aspectFill"></image>
					<view class="hero-info">
						<text class="hero-tag">今日精选</text>
						<text class="hero-name">{{ featureProduct.name }}</text>
						<view class="price-row">
							<text class="currency">¥</text>
							<text class="hero-price">{{ featureProduct.price }}</text>
							<view class="buy-pill">联系TA</view>
						</view>
					</view>
				</view>
			</view> -->

			<!-- 分类栏 -->
			<scroll-view class="category-scroll" scroll-x :show-scrollbar="false">
				<view class="category-strip">
					<view
						v-for="item in categories"
						:key="item"
						class="category-chip"
						:class="{ active: activeCategory === item }"
						@click="activeCategory = item"
					>
						{{ item }}
					</view>
				</view>
			</scroll-view>

			<!-- 瀑布流列表 -->
		</view>

		<scroll-view class="page-scroll" scroll-y enhanced :show-scrollbar="false">
			<view class="waterfall">
				<view class="waterfall-col">
					<view
						v-for="item in leftProducts"
						:key="item.id"
						class="product-card"
						@click="openProduct(item)"
					>
						<view class="image-wrap" :class="item.size">
							<image class="product-image" :src="item.images[0]" mode="aspectFill"></image>
							<view v-if="item.label" class="corner-label">{{ item.label }}</view>
							<view v-if="item.images.length > 1" class="image-count">
								{{ item.images.length }}图
							</view>
						</view>
						<view class="product-info">
							<text class="product-name">{{ item.name }}</text>
							<text class="product-desc">{{ item.desc }}</text>
							<view class="card-bottom">
								<view class="price">
									<text class="currency">¥</text>
									<text>{{ item.price }}</text>
								</view>
								<view class="mini-buy">联系</view>
							</view>
						</view>
					</view>
				</view>

				<view class="waterfall-col">
					<view
						v-for="item in rightProducts"
						:key="item.id"
						class="product-card"
						@click="openProduct(item)"
					>
						<view class="image-wrap" :class="item.size">
							<image class="product-image" :src="item.images[0]" mode="aspectFill"></image>
							<view v-if="item.label" class="corner-label">{{ item.label }}</view>
							<view v-if="item.images.length > 1" class="image-count">
								{{ item.images.length }}图
							</view>
						</view>
						<view class="product-info">
							<text class="product-name">{{ item.name }}</text>
							<text class="product-desc">{{ item.desc }}</text>
							<view class="card-bottom">
								<view class="price">
									<text class="currency">¥</text>
									<text>{{ item.price }}</text>
								</view>
								<view class="mini-buy">联系</view>
							</view>
						</view>
					</view>
				</view>
			</view>

			<!-- 查看更多 -->
			<view class="more-bar" @click="loadMore">
				<text>查看更多</text>
				<view class="arrow-circle"></view>
			</view>
		</scroll-view>


	</view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'

const activeCategory = ref('全部')
const searchKeyword = ref('')

const categories = ['全部', '数码', '书籍', '宿舍', '服饰','电脑','其他']

const featureProduct = {
	id: 0,
	name: '光影亚克力吊饰',
	desc: '窗边悬挂，午后会落下一片柔和花影。',
	price: 78,
	images: [
		'/static/organization/feature-card.jpg',
		'/static/order/fruit.png',
		'/static/index/buy.png'
	],
	stock: 26
}

const products = ref([
	{
		id: 1,
		name: '光影亚克力吊饰',
		desc: '适合窗边、床头和书桌侧边。',
		price: 78,
		category: '宿舍',
		images: [
			'/static/organization/feature-card.jpg',
			'/static/order/fruit.png'
		],
		size: 'tall',
		label: 'HOT',
		stock: 26
	},
	{
		id: 2,
		name: '几何艺术陶瓷花瓶',
		desc: '单插一枝花，也能撑起桌面氛围。',
		price: 98,
		category: '宿舍',
		images: [
			'/static/order/fruit.png',
			'/static/index/buy.png',
			'/static/order/service_daily.png'
		],
		size: 'middle',
		label: 'NEW',
		stock: 18
	},
	{
		id: 3,
		name: '漂亮首饰置物镜',
		desc: '镜面、托盘、饰品位三合一。',
		price: 168,
		category: '服饰',
		images: [
			'/static/index/buy.png'
		],
		size: 'short',
		label: '',
		stock: 12
	},
	{
		id: 4,
		name: '改造画框小鱼缸',
		desc: '透明边框设计，放在窗边很出片。',
		price: 286,
		category: '宿舍',
		images: [
			'/static/order/service_daily.png',
			'/static/order/package.png'
		],
		size: 'tall',
		label: 'TOP',
		stock: 6
	},
	{
		id: 5,
		name: '奶油色桌面收纳盒',
		desc: '把口红、耳机、钥匙都藏好。',
		price: 42,
		category: '宿舍',
		images: [
			'/static/order/package.png'
		],
		size: 'middle',
		label: '',
		stock: 39
	},
	{
		id: 6,
		name: '小夜灯香薰托盘',
		desc: '睡前暖光，配合香薰片使用。',
		price: 128,
		category: '宿舍',
		images: [
			'/static/order/snack.png',
			'/static/organization/feature-card.jpg'
		],
		size: 'short',
		label: 'SALE',
		stock: 21
	}
])

const filteredProducts = computed(() => {
	let result = products.value
	// 分类过滤
	if (activeCategory.value !== '全部') {
		result = result.filter(item => item.category === activeCategory.value)
	}
	// 搜索过滤
	if (searchKeyword.value.trim()) {
		const keyword = searchKeyword.value.trim().toLowerCase()
		result = result.filter(item =>
			item.name.toLowerCase().includes(keyword) ||
			item.desc.toLowerCase().includes(keyword)
		)
	}
	return result
})

// 搜索处理
const handleSearch = () => {
	// 搜索时触发重新计算
}

// 清除搜索
const clearSearch = () => {
	searchKeyword.value = ''
}

const leftProducts = computed(() => filteredProducts.value.filter((_, index) => index % 2 === 0))
const rightProducts = computed(() => filteredProducts.value.filter((_, index) => index % 2 === 1))

// 点击商品跳转到详情页
const openProduct = product => {
	uni.setStorageSync('product_detail', product)
	uni.navigateTo({
		url: `/pages/shoppingMall/productDetail?id=${product.id}`
	})
}

const loadMore = () => {
	uni.showToast({
		title: '更多好物加载中',
		icon: 'none'
	})
}

// 页面加载时，如果从详情页搜索跳转过来，则设置搜索关键字
onLoad((options) => {
	if (options && options.keyword) {
		searchKeyword.value = decodeURIComponent(options.keyword)
	}
})
</script>

<style scoped>
page {
	background: #f7f5ef;
	height: 100%;
	overflow: hidden;
}

.page {
	height: 100vh;
	background: #f7f5ef;
	color: #0e0e0e;
	display: flex;
	flex-direction: column;
	overflow: hidden;
}

.fixed-top {
	flex-shrink: 0;
	background: #f7f5ef;
	position: relative;
	z-index: 10;
}

.page-scroll {
	flex: 1;
	height: 0;
	min-height: 0;
	box-sizing: border-box;
}

/* 搜索栏 */
.search-bar {
	padding: 24rpx 24rpx 0;
}

.search-input-wrap {
	display: flex;
	align-items: center;
	height: 72rpx;
	padding: 0 20rpx;
	border: 4rpx solid #0e0e0e;
	border-radius: 999rpx;
	background: #ffffff;
	box-shadow: 4rpx 4rpx 0 #0e0e0e;
}

.search-icon {
	font-size: 28rpx;
	margin-right: 12rpx;
}

.search-input {
	flex: 1;
	height: 100%;
	font-size: 28rpx;
	color: #0e0e0e;
}

.search-input::placeholder {
	color: #999;
}

.clear-btn {
	font-size: 28rpx;
	color: #999;
	padding: 8rpx;
}

.poster-head {
	margin: 24rpx;
	padding: 30rpx;
	border: 6rpx solid #0e0e0e;
	border-radius: 16rpx;
	background: #fffdf7;
	box-shadow: 10rpx 10rpx 0 #0e0e0e;
	position: relative;
	overflow: hidden;
}

.poster-head::before {
	content: "";
	position: absolute;
	right: -74rpx;
	top: -86rpx;
	width: 240rpx;
	height: 240rpx;
	border: 2rpx solid rgba(14, 14, 14, 0.1);
	border-radius: 50%;
	background:
		linear-gradient(90deg, transparent 49%, rgba(14, 14, 14, 0.08) 50%, transparent 51%),
		linear-gradient(0deg, transparent 49%, rgba(14, 14, 14, 0.08) 50%, transparent 51%);
	background-size: 34rpx 34rpx;
}

.brand-row {
	position: relative;
	z-index: 1;
	display: flex;
	align-items: center;
	gap: 14rpx;
	margin-bottom: 18rpx;
}

.brand-badge {
	height: 42rpx;
	padding: 0 16rpx;
	border-radius: 999rpx;
	background: #0e0e0e;
	color: #ffffff;
	font-size: 22rpx;
	font-weight: 800;
	display: flex;
	align-items: center;
	gap: 8rpx;
	letter-spacing: 0;
}

.badge-dot {
	width: 16rpx;
	height: 16rpx;
	border-radius: 50%;
	border: 4rpx solid #ffffff;
	box-sizing: border-box;
}

.brand-sub {
	font-size: 22rpx;
	font-weight: 800;
	color: #2a2a2a;
}

.headline-wrap {
	position: relative;
	z-index: 1;
	display: flex;
	flex-direction: column;
	margin-bottom: 12rpx;
}

.headline {
	font-size: 58rpx;
	line-height: 1.02;
	font-weight: 900;
	letter-spacing: 0;
}

.lead {
	position: relative;
	z-index: 1;
	display: block;
	width: 560rpx;
	max-width: 100%;
	font-size: 24rpx;
	line-height: 1.6;
	color: #66645f;
	margin-bottom: 28rpx;
}

.hero-product {
	position: relative;
	z-index: 1;
	border: 5rpx solid #0e0e0e;
	border-radius: 16rpx;
	background: #ffffff;
	overflow: hidden;
}

.hero-image {
	width: 100%;
	height: 330rpx;
	display: block;
	background: #efece2;
}

.hero-info {
	padding: 22rpx 24rpx 24rpx;
}

.hero-tag {
	display: block;
	font-size: 22rpx;
	font-weight: 800;
	color: #76716a;
	margin-bottom: 6rpx;
}

.hero-name {
	display: block;
	font-size: 34rpx;
	font-weight: 900;
	margin-bottom: 12rpx;
}

.price-row,
.card-bottom {
	display: flex;
	align-items: center;
	justify-content: space-between;
}

.currency {
	font-size: 24rpx;
	font-weight: 900;
	margin-right: 6rpx;
}

.hero-price {
	font-size: 48rpx;
	font-weight: 900;
}

.buy-pill,
.mini-buy {
	height: 44rpx;
	padding: 0 18rpx;
	border-radius: 999rpx;
	background: #0e0e0e;
	color: #ffffff;
	font-size: 18rpx;
	font-weight: 900;
	display: flex;
	align-items: center;
	justify-content: center;
}

.category-scroll {
	width: 100%;
}

.category-strip {
	display: flex;
	gap: 14rpx;
	padding: 14rpx 24rpx 14rpx;
	white-space: nowrap;
	box-sizing: border-box;
}

.category-chip {
	flex: 0 0 auto;
	height: 58rpx;
	padding: 0 26rpx;
	border: 4rpx solid #0e0e0e;
	border-radius: 999rpx;
	background: #ffffff;
	font-size: 24rpx;
	font-weight: 800;
	display: flex;
	align-items: center;
	justify-content: center;
}

.category-chip.active {
	background: #f7cb45;
}

.waterfall {
	display: flex;
	align-items: flex-start;
	gap: 18rpx;
	padding: 0 24rpx;
	box-sizing: border-box;
}

.waterfall-col {
	width: 0;
	flex: 1;
	display: flex;
	flex-direction: column;
	gap: 18rpx;
}

.product-card {
	border: 5rpx solid #0e0e0e;
	border-radius: 16rpx;
	background: #ffffff;
	overflow: hidden;
	box-shadow: 1rpx 2rpx 0 #0e0e0e;
}

.image-wrap {
	position: relative;
	background: #ece8dc;
}

.image-wrap.short {
	height: 260rpx;
}

.image-wrap.middle {
	height: 310rpx;
}

.image-wrap.tall {
	height: 380rpx;
}

.product-image {
	width: 100%;
	height: 100%;
	display: block;
}

.corner-label {
	position: absolute;
	left: 14rpx;
	top: 14rpx;
	height: 40rpx;
	padding: 0 14rpx;
	border: 3rpx solid #0e0e0e;
	border-radius: 999rpx;
	background: #ffffff;
	font-size: 18rpx;
	font-weight: 900;
	display: flex;
	align-items: center;
}

.product-info {
	padding: 18rpx 18rpx 20rpx;
}

.product-name {
	display: block;
	font-size: 27rpx;
	line-height: 1.25;
	font-weight: 900;
	margin-bottom: 8rpx;
}

.product-desc {
	display: -webkit-box;
	-webkit-line-clamp: 2;
	-webkit-box-orient: vertical;
	overflow: hidden;
	min-height: 58rpx;
	font-size: 21rpx;
	line-height: 1.4;
	color: #706d67;
	margin-bottom: 14rpx;
}

.price {
	display: flex;
	align-items: baseline;
	font-size: 40rpx;
	font-weight: 900;
}

.mini-buy {
	width: 74rpx;
	height: 38rpx;
	padding: 0;
	font-size: 17rpx;
}

.image-count {
	position: absolute;
	right: 14rpx;
	top: 14rpx;
	height: 40rpx;
	padding: 0 14rpx;
	border-radius: 999rpx;
	background: rgba(0,0,0,0.6);
	color: #fff;
	font-size: 18rpx;
	font-weight: 600;
	display: flex;
	align-items: center;
}

.more-bar {
	margin: 26rpx 24rpx 52rpx;
	height: 76rpx;
	border: 5rpx solid #0e0e0e;
	border-radius: 999rpx;
	background: #ffffff;
	box-shadow: 6rpx 6rpx 0 #0e0e0e;
	font-size: 34rpx;
	font-weight: 900;
	display: flex;
	align-items: center;
	justify-content: center;
	gap: 18rpx;
}

.arrow-circle {
	width: 58rpx;
	height: 38rpx;
	border-radius: 999rpx;
	background: #0e0e0e;
	position: relative;
}

.arrow-circle::before,
.arrow-circle::after {
	content: "";
	position: absolute;
	background: #ffffff;
}

.arrow-circle::before {
	width: 24rpx;
	height: 5rpx;
	top: 17rpx;
	left: 15rpx;
}

.arrow-circle::after {
	width: 14rpx;
	height: 14rpx;
	right: 15rpx;
	top: 11rpx;
	border-top: 5rpx solid #ffffff;
	border-right: 5rpx solid #ffffff;
	transform: rotate(45deg);
	background: transparent;
}
</style>
