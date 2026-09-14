<template>
	<view class="page">
		<scroll-view class="page-scroll" scroll-y enhanced :show-scrollbar="false">
			<!-- 顶部推荐 -->
			<view class="poster-head">
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
			</view>

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

		<!-- 商品详情弹窗 -->
		<view class="detail-mask" v-if="showProduct" @click="closeProduct">
			<view class="detail-modal" @click.stop>
				<!-- 多图轮播 -->
				<swiper class="modal-swiper" :indicator-dots="true" indicator-color="rgba(255,255,255,0.5)" indicator-active-color="#fff">
					<swiper-item v-for="(img, idx) in currentProduct.images" :key="idx">
						<image class="modal-image" :src="img" mode="aspectFit" @click="openPreview(currentProduct.images, idx)"></image>
					</swiper-item>
				</swiper>
				
				<view class="modal-content">
					<view class="modal-head">
						<view>
							<text class="modal-name">{{ currentProduct.name }}</text>
							<text class="modal-desc">{{ currentProduct.desc }}</text>
						</view>
						<view class="close-btn" @click="closeProduct">✕</view>
					</view>
					<view class="modal-price-row">
						<view class="modal-price">
							<text class="currency">¥</text>
							<text>{{ currentProduct.price }}</text>
						</view>
						<text class="stock">库存 {{ currentProduct.stock }} 件</text>
					</view>
					<view class="modal-actions">
						<view class="contact-btn" @click="contactSeller">
							💬 我想要
						</view>
					</view>
				</view>
			</view>
		</view>

		<!-- 图片预览遮罩（支持缩放） -->
		<view class="preview-mask" v-if="previewVisible" @click.self="closePreview">
			<swiper class="preview-swiper" :current="previewIndex" @change="onPreviewSwiperChange"
				:indicator-dots="true" indicator-color="rgba(255,255,255,0.5)" indicator-active-color="#fff"
				@touchmove.stop @mousedown.stop @mouseup.stop @click.stop>
				<swiper-item v-for="(url, idx) in previewUrls" :key="idx">
					<view class="zoom-container" :style="{ transform: `scale(${scale})` }" @touchstart="onTouchStart"
						@touchmove="onTouchMove" @touchend="onTouchEnd" @wheel="onWheel">
						<image :src="url" mode="aspectFit" class="preview-image" @click.stop="closePreview"></image>
					</view>
				</swiper-item>
			</swiper>
			<view class="close-btn" @click.stop="closePreview">✕</view>
		</view>
	</view>
</template>

<script setup>
import { computed, ref } from 'vue'

const activeCategory = ref('全部')
const showProduct = ref(false)
const currentProduct = ref({})

// 图片预览相关
const previewVisible = ref(false)
const previewUrls = ref([])
const previewIndex = ref(0)
const scale = ref(1)
let lastDist = 0

const categories = ['全部', '数码', '书籍', '宿舍', '服饰', '其他']

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
	if (activeCategory.value === '全部') return products.value
	return products.value.filter(item => item.category === activeCategory.value)
})

const leftProducts = computed(() => filteredProducts.value.filter((_, index) => index % 2 === 0))
const rightProducts = computed(() => filteredProducts.value.filter((_, index) => index % 2 === 1))

const openProduct = product => {
	currentProduct.value = product
	showProduct.value = true
}

const closeProduct = () => {
	showProduct.value = false
	currentProduct.value = {}
}

const contactSeller = () => {
	uni.showToast({
		title: '消息功能即将上线',
		icon: 'none'
	})
	closeProduct()
}

const loadMore = () => {
	uni.showToast({
		title: '更多好物加载中',
		icon: 'none'
	})
}

// 图片预览相关函数
const getDistance = (touches) => {
	if (touches.length < 2) return 0
	const dx = touches[0].pageX - touches[1].pageX
	const dy = touches[0].pageY - touches[1].pageY
	return Math.sqrt(dx * dx + dy * dy)
}

const onTouchStart = (e) => {
	if (e.touches && e.touches.length === 2) {
		e.preventDefault()
		lastDist = getDistance(e.touches)
	}
}

const onTouchMove = (e) => {
	if (e.touches && e.touches.length === 2) {
		e.preventDefault()
		const currentDist = getDistance(e.touches)
		if (lastDist > 0) {
			const delta = currentDist / lastDist
			scale.value = Math.min(Math.max(scale.value * delta, 0.5), 4)
		}
		lastDist = currentDist
	}
}

const onTouchEnd = () => {
	lastDist = 0
}

const onWheel = (e) => {
	e.preventDefault()
	const delta = e.deltaY > 0 ? 0.95 : 1.05
	scale.value = Math.min(Math.max(scale.value * delta, 0.5), 4)
}

const openPreview = (urls, current) => {
	previewUrls.value = urls
	previewIndex.value = current
	previewVisible.value = true
	scale.value = 1
}

const closePreview = () => {
	previewVisible.value = false
	previewUrls.value = []
	previewIndex.value = 0
	scale.value = 1
}

const onPreviewSwiperChange = (e) => {
	previewIndex.value = e.detail.current
}
</script>

<style scoped>
page {
	background: #f7f5ef;
}

.page {
	min-height: 100vh;
	background: #f7f5ef;
	color: #0e0e0e;
}

.page-scroll {
	height: 100vh;
	box-sizing: border-box;
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
.card-bottom,
.modal-price-row,
.modal-actions {
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
	padding: 10rpx 24rpx 24rpx;
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

/* 详情弹窗 */
.detail-mask {
	position: fixed;
	left: 0;
	right: 0;
	top: 0;
	bottom:0;
	background: rgba(0, 0, 0, 0.56);
	z-index: 99;
	display: flex;
	align-items: flex-end;
	justify-content: center;
	padding: 28rpx;
	box-sizing: border-box;
}

.detail-modal {
	width: 100%;
	border: 5rpx solid #0e0e0e;
	border-radius: 16rpx;
	background: #ffffff;
	overflow: hidden;
	box-shadow: 8rpx 8rpx 0 #0e0e0e;
}

.modal-swiper {
	width: 100%;
	height: 1000rpx;
	background: #ece8dc;
}

.modal-image {
	width: 100%;
	height: 100%;
	display: block;
}

.modal-content {
	padding: 26rpx;
}

.modal-head {
	display: flex;
	justify-content: space-between;
	gap: 18rpx;
	margin-bottom: 18rpx;
}

.modal-name {
	display: block;
	font-size: 34rpx;
	font-weight: 900;
	line-height: 1.25;
	margin-bottom: 8rpx;
}

.modal-desc {
	display: block;
	font-size: 24rpx;
	line-height: 1.5;
	color: #706d67;
}

.close-btn {
	flex: 0 0 auto;
	width: 60rpx;
	height: 60rpx;
	border-radius: 50%;
	background: #0e0e0e;
	color: #ffffff;
	font-size: 36rpx;
	display: flex;
	align-items: center;
	justify-content: center;
}

.modal-price-row {
	margin-bottom: 24rpx;
}

.modal-price {
	display: flex;
	align-items: baseline;
	font-size: 48rpx;
	font-weight: 900;
}

.stock {
	font-size: 23rpx;
	color: #77736d;
	font-weight: 700;
}

.modal-actions {
	gap: 16rpx;
}

.contact-btn {
	flex: 1;
	height: 84rpx;
	border: 4rpx solid #0e0e0e;
	border-radius: 16rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 28rpx;
	font-weight: 900;
	background: #f7cb45;
}

/* 图片预览遮罩 */
.preview-mask {
	position: fixed;
	top: 0;
	left: 0;
	width: 100%;
	height: 100%;
	background: rgba(0, 0, 0, 0.9);
	z-index: 9999;
	display: flex;
	align-items: center;
	justify-content: center;
}

.preview-swiper {
	width: 100%;
	height: 100%;
	max-width: 800px;
	max-height: 90vh;
	margin: 0 auto;
}

.zoom-container {
	width: 100%;
	height: 100%;
	display: flex;
	align-items: center;
	justify-content: center;
	transition: transform 0.1s ease-out;
}

.preview-image {
	width: 100%;
	height: 100%;
	object-fit: contain;
	display: block;
	pointer-events: auto;
}
</style>
