<template>
	<view class="page" v-if="product">
		<!-- 顶部自定义导航栏 -->
		<view class="nav-bar" :style="{ paddingTop: statusBarHeight + 'px' }">
			<view class="nav-inner">
				<view class="nav-left" @click="goBack">
					<text class="nav-back">‹</text>
				</view>
				<view class="nav-tabs">
					<text class="nav-tab active">商品</text>
					<text class="nav-tab">详情</text>
					<text class="nav-tab">评价</text>
				</view>
				<view class="nav-actions">
					<text class="nav-icon" @click="handleShare">↗</text>
				</view>
			</view>
		</view>

		<scroll-view class="page-scroll" scroll-y :show-scrollbar="false" :enhanced="true">
			<!-- 图片轮播 -->
			<view class="swiper-wrap">
				<swiper class="product-swiper" indicator-dots :indicator-color="'rgba(255,255,255,0.4)'" :indicator-active-color="'#fff'" circular autoplay :interval="4000">
					<swiper-item v-for="(img, idx) in product.images" :key="idx">
						<image class="swiper-image" :src="img" mode="aspectFill" @click="previewImage(idx)"></image>
					</swiper-item>
				</swiper>
				<view class="swiper-counter" v-if="product.images.length > 1">
					<text>{{ previewIndex + 1 }}/{{ product.images.length }}</text>
				</view>
			</view>

			<!-- 价格信息卡片 -->
			<view class="price-card">
				<view class="price-top">
					<view class="price-main">
						<text class="currency">¥</text>
						<text class="price-value">{{ product.price }}</text>
						<view v-if="product.originalPrice" class="original-price">
							<text>¥{{ product.originalPrice }}</text>
						</view>
					</view>
					<view class="price-tags">
						<text class="tag-hot" v-if="product.wantCount > 100">热门</text>
						<text class="tag-free" v-if="product.freeShipping">包邮</text>
					</view>
				</view>
				<view class="price-divider"></view>
				<view class="price-meta">
					<view class="meta-item">
						<text class="meta-val">{{ product.wantCount || 0 }}</text>
						<text class="meta-label">想要</text>
					</view>
					<view class="meta-item">
						<text class="meta-val">{{ formatViewCount(product.viewCount || 0) }}</text>
						<text class="meta-label">浏览</text>
					</view>
					<view class="meta-item">
						<text class="meta-val">{{ product.stock || 1 }}</text>
						<text class="meta-label">库存</text>
					</view>
				</view>
			</view>

			<!-- 商品信息 -->
			<view class="info-card">
				<view class="info-header">
					<text class="product-title">{{ product.name }}</text>
					<view v-if="product.category" class="category-tag">
						<text>{{ product.category }}</text>
					</view>
				</view>
				<text class="product-desc" v-if="product.desc">{{ product.desc }}</text>
				<view v-if="product.specs && product.specs.length" class="spec-list">
					<view class="spec-item" v-for="(spec, idx) in product.specs" :key="idx">
						<text class="spec-label">{{ spec.label }}</text>
						<text class="spec-value">{{ spec.value }}</text>
					</view>
				</view>
			</view>

			<!-- 卖家信息 -->
			<view class="seller-card" v-if="seller">
				<view class="seller-top">
					<image class="seller-avatar" :src="seller.avatar" mode="aspectFill"></image>
					<view class="seller-info">
						<view class="seller-name-row">
							<text class="seller-name">{{ seller.nickname }}</text>
							<view v-if="seller.creditTag" class="credit-badge">
								<text>{{ seller.creditTag }}</text>
							</view>
						</view>
						<text class="seller-meta">{{ seller.lastSeen }} · {{ seller.location }}</text>
					</view>
					<view class="follow-btn" :class="{ followed: isFollowed }" @click="toggleFollow">
						<text>{{ isFollowed ? '已关注' : '+ 关注' }}</text>
					</view>
				</view>
				<view class="seller-stats">
					<view class="stat-item">
						<text class="stat-value">{{ seller.soldCount }}<text class="stat-unit">件</text></text>
						<text class="stat-label">共卖出</text>
					</view>
					<view class="stat-item">
						<text class="stat-value">{{ seller.goodRate }}<text class="stat-unit">%</text></text>
						<text class="stat-label">好评率</text>
					</view>
					<view class="stat-item">
						<text class="stat-value">{{ seller.joinTime }}</text>
						<text class="stat-label">来校园</text>
					</view>
					<view class="stat-item" @click="goChat">
						<text class="stat-contact">联系卖家</text>
					</view>
				</view>
			</view>

			<!-- 商品图片展示 -->
			<view class="gallery-card" v-if="product.images.length > 1">
				<view class="gallery-head">
					<text class="gallery-title">商品实拍</text>
					<text class="gallery-count">{{ product.images.length }}张</text>
				</view>
				<view class="gallery-grid">
					<image
						v-for="(img, idx) in product.images"
						:key="idx"
						class="gallery-image"
						:src="img"
						mode="aspectFill"
						@click="previewImage(idx)"
					></image>
				</view>
			</view>

			<!-- 交易须知 -->
			<view class="notice-card">
				<view class="notice-icon-wrap">
					<text class="notice-icon">✓</text>
				</view>
				<view class="notice-content">
					<text class="notice-title">校园交易保障</text>
					<text class="notice-text">本校实名认证用户交易 · 平台担保 · 买前沟通细节</text>
				</view>
			</view>

			<!-- 底部占位 -->
			<view class="bottom-space"></view>
		</scroll-view>

		<!-- 底部操作栏 -->
		<view class="action-bar">
			<view class="action-left">
				<view class="action-icon" @click="goChat">
					<text class="action-ico">💬</text>
					<text class="action-label">消息</text>
				</view>
				<view class="action-icon" @click="toggleFavor">
					<text class="action-ico">{{ isFavored ? '❤️' : '🤍' }}</text>
					<text class="action-label">收藏</text>
				</view>
			</view>
			<view class="action-btns">
				<view class="chat-btn" @click="goChat">
					<text>聊一聊</text>
				</view>
				<view class="buy-btn" @click="handleBuy">
					<text>立即购买</text>
				</view>
			</view>
		</view>

		<!-- 图片预览 -->
		<view class="preview-mask" v-if="previewVisible" @click="closePreview">
			<swiper class="preview-swiper" :current="previewIndex" @change="onPreviewChange" indicator-dots indicator-color="rgba(255,255,255,0.3)" indicator-active-color="#fff">
				<swiper-item v-for="(url, idx) in product.images" :key="idx">
					<image :src="url" mode="aspectFit" class="preview-image" @click.stop></image>
				</swiper-item>
			</swiper>
			<view class="preview-close" @click="closePreview">✕</view>
			<view class="preview-counter">{{ previewIndex + 1 }} / {{ product.images.length }}</view>
		</view>
	</view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'

const product = ref(null)
const seller = ref(null)
const isFollowed = ref(false)
const isFavored = ref(false)
const chatCount = ref(226)
const favorCount = ref(61)
const previewVisible = ref(false)
const previewIndex = ref(0)
const navKeyword = ref('')
const statusBarHeight = ref(0)

// 默认卖家数据
const defaultSeller = {
	nickname: '校园好物君',
	avatar: 'https://picsum.photos/200/200?random=5',
	creditTag: '信用极好',
	lastSeen: '刚刚来过',
	location: '本校',
	joinTime: '2年',
	soldCount: 201,
	goodRate: 100
}

const formatViewCount = (count) => {
	if (count >= 10000) return (count / 10000).toFixed(1) + 'w'
	if (count >= 1000) return (count / 1000).toFixed(1) + 'k'
	return count
}

onLoad((options) => {
	statusBarHeight.value = uni.getSystemInfoSync().statusBarHeight || 0
	const id = options?.id
	const cached = uni.getStorageSync('product_detail')
	if (cached) {
		product.value = cached
		uni.removeStorageSync('product_detail')
	}
	if (!product.value && options) {
		product.value = {
			id: id || 0,
			name: decodeURIComponent(options.name || '二手商品'),
			desc: decodeURIComponent(options.desc || '校园闲置好物'),
			price: options.price || '0',
			category: decodeURIComponent(options.category || '其他'),
			images: options.image ? [decodeURIComponent(options.image)] : ['https://picsum.photos/800/800?random=1'],
			stock: options.stock || 1,
			freeShipping: true,
			wantCount: 344,
			viewCount: 148000,
			specs: []
		}
	}
	seller.value = { ...defaultSeller }
	if (product.value && product.value.seller) {
		seller.value = { ...defaultSeller, ...product.value.seller }
	}
})

const goBack = () => {
	uni.navigateBack({
		fail: () => {
			uni.navigateTo({ url: '/pages/shoppingMall/shoppingMall' })
		}
	})
}

const handleShare = () => {
	uni.showToast({ title: '分享功能开发中', icon: 'none' })
}

const previewImage = (index) => {
	previewIndex.value = index
	previewVisible.value = true
}

const closePreview = () => {
	previewVisible.value = false
}

const onPreviewChange = (e) => {
	previewIndex.value = e.detail.current
}

const toggleFollow = () => {
	isFollowed.value = !isFollowed.value
	uni.showToast({ title: isFollowed.value ? '已关注' : '取消关注', icon: 'none' })
}

const toggleFavor = () => {
	isFavored.value = !isFavored.value
	uni.showToast({ title: isFavored.value ? '已收藏' : '取消收藏', icon: 'none' })
}

const handleBuy = () => {
	uni.showToast({ title: '购买功能开发中', icon: 'none' })
}

const goChat = () => {
	if (seller.value) {
		uni.navigateTo({
			url: `/pages/message/message?uid=${product.value.id}&nickname=${encodeURIComponent(seller.value.nickname)}`
		})
	}
}
</script>

<script>
export default {
	onShareAppMessage() {
		const p = this.$scope && this.$scope.product
		return {
			title: p?.name || '校园闲置好物',
			path: p ? `/pages/shoppingMall/productDetail?id=${p.id}` : '/pages/shoppingMall/shoppingMall'
		}
	}
}
</script>

<style scoped>
.page {
	min-height: 100vh;
	background: #f2f3f5;
}

/* ===== 导航栏 ===== */
.nav-bar {
	position: fixed;
	top: 0;
	left: 0;
	right: 0;
	z-index: 100;
	background: rgba(255, 255, 255, 0.95);
	backdrop-filter: blur(20rpx);
	border-bottom: 1rpx solid transparent;
}

.nav-inner {
	display: flex;
	align-items: center;
	justify-content: space-between;
	height: 88rpx;
	padding: 0 20rpx;
}

.nav-left {
	width: 64rpx;
	height: 64rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	flex-shrink: 0;
}

.nav-back {
	font-size: 52rpx;
	color: #1a1d21;
	font-weight: 400;
	line-height: 1;
}

.nav-tabs {
	display: flex;
	gap: 40rpx;
}

.nav-tab {
	font-size: 30rpx;
	color: #8a94a6;
	font-weight: 500;
	position: relative;
	padding-bottom: 8rpx;
	transition: color 0.2s;
}

.nav-tab.active {
	color: #1a1d21;
	font-weight: 700;
}

.nav-tab.active::after {
	content: '';
	position: absolute;
	bottom: 0;
	left: 50%;
	transform: translateX(-50%);
	width: 32rpx;
	height: 5rpx;
	border-radius: 3rpx;
	background: #1a1d21;
}

.nav-actions {
	display: flex;
	align-items: center;
}

.nav-icon {
	width: 64rpx;
	height: 64rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 36rpx;
	color: #1a1d21;
}

/* ===== 滚动区域 ===== */
.page-scroll {
	height: 100vh;
	box-sizing: border-box;
	padding-bottom: 160rpx;
}

/* ===== 图片轮播 ===== */
.swiper-wrap {
	position: relative;
	width: 100%;
	height: 750rpx;
	background: #1a1d21;
}

.product-swiper {
	width: 100%;
	height: 100%;
}

.swiper-image {
	width: 100%;
	height: 100%;
}

.swiper-counter {
	position: absolute;
	bottom: 40rpx;
	right: 28rpx;
	background: rgba(0, 0, 0, 0.45);
	padding: 6rpx 18rpx;
	border-radius: 20rpx;
	backdrop-filter: blur(10rpx);
}

.swiper-counter text {
	font-size: 22rpx;
	color: #fff;
	font-weight: 500;
}

/* ===== 价格卡片 ===== */
.price-card {
	margin: -30rpx 20rpx 16rpx;
	background: #fff;
	border-radius: 24rpx;
	padding: 28rpx 28rpx 20rpx;
	position: relative;
	z-index: 2;
	box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.06);
}

.price-top {
	display: flex;
	justify-content: space-between;
	align-items: flex-start;
}

.price-main {
	display: flex;
	align-items: baseline;
	gap: 4rpx;
}

.currency {
	font-size: 36rpx;
	color: #ff4757;
	font-weight: 800;
}

.price-value {
	font-size: 68rpx;
	color: #ff4757;
	font-weight: 800;
	line-height: 1;
	letter-spacing: -2rpx;
}

.original-price {
	font-size: 26rpx;
	color: #b0b8c1;
	text-decoration: line-through;
	margin-left: 12rpx;
}

.price-tags {
	display: flex;
	gap: 10rpx;
}

.tag-hot {
	font-size: 20rpx;
	color: #fff;
	background: linear-gradient(135deg, #ff6b6b, #ff4757);
	padding: 4rpx 14rpx;
	border-radius: 8rpx;
	font-weight: 700;
}

.tag-free {
	font-size: 20rpx;
	color: #ff4757;
	background: #fff1f0;
	padding: 4rpx 14rpx;
	border-radius: 8rpx;
	font-weight: 600;
}

.price-divider {
	height: 1rpx;
	background: #f0f1f3;
	margin: 20rpx 0;
}

.price-meta {
	display: flex;
	gap: 40rpx;
}

.meta-item {
	display: flex;
	flex-direction: column;
	gap: 4rpx;
}

.meta-val {
	font-size: 28rpx;
	color: #1a1d21;
	font-weight: 700;
}

.meta-label {
	font-size: 22rpx;
	color: #8a94a6;
}

/* ===== 商品信息 ===== */
.info-card {
	margin: 0 20rpx 16rpx;
	background: #fff;
	border-radius: 24rpx;
	padding: 28rpx;
}

.info-header {
	display: flex;
	justify-content: space-between;
	align-items: flex-start;
	gap: 16rpx;
	margin-bottom: 16rpx;
}

.product-title {
	flex: 1;
	font-size: 34rpx;
	color: #1a1d21;
	font-weight: 700;
	line-height: 1.45;
}

.category-tag {
	flex-shrink: 0;
	padding: 6rpx 16rpx;
	background: #f2f3f5;
	border-radius: 20rpx;
}

.category-tag text {
	font-size: 22rpx;
	color: #6a7484;
	font-weight: 500;
}

.product-desc {
	display: block;
	font-size: 27rpx;
	color: #6a7484;
	line-height: 1.65;
}

.spec-list {
	margin-top: 24rpx;
	padding: 20rpx 24rpx;
	background: #f8f9fb;
	border-radius: 16rpx;
}

.spec-item {
	display: flex;
	padding: 8rpx 0;
}

.spec-item + .spec-item {
	border-top: 1rpx solid #edf0f3;
}

.spec-label {
	font-size: 26rpx;
	color: #8a94a6;
	width: 140rpx;
	flex-shrink: 0;
}

.spec-value {
	font-size: 26rpx;
	color: #1a1d21;
	font-weight: 500;
}

/* ===== 卖家卡片 ===== */
.seller-card {
	margin: 0 20rpx 16rpx;
	background: #fff;
	border-radius: 24rpx;
	padding: 28rpx;
}

.seller-top {
	display: flex;
	align-items: center;
}

.seller-avatar {
	width: 88rpx;
	height: 88rpx;
	border-radius: 50%;
	background: #f2f3f5;
	flex-shrink: 0;
}

.seller-info {
	flex: 1;
	margin-left: 18rpx;
	min-width: 0;
}

.seller-name-row {
	display: flex;
	align-items: center;
	gap: 10rpx;
	margin-bottom: 6rpx;
}

.seller-name {
	font-size: 30rpx;
	color: #1a1d21;
	font-weight: 700;
}

.credit-badge {
	padding: 2rpx 12rpx;
	background: linear-gradient(135deg, #ffd700, #ffb800);
	border-radius: 8rpx;
}

.credit-badge text {
	font-size: 18rpx;
	color: #7a6200;
	font-weight: 700;
}

.seller-meta {
	font-size: 24rpx;
	color: #8a94a6;
}

.follow-btn {
	padding: 12rpx 24rpx;
	border-radius: 28rpx;
	font-size: 24rpx;
	font-weight: 600;
	color: #ff7a00;
	background: #fff7ed;
	border: 1rpx solid #ffe0c0;
	flex-shrink: 0;
	transition: all 0.2s;
}

.follow-btn.followed {
	color: #8a94a6;
	background: #f2f3f5;
	border-color: #e4e6eb;
}

.follow-btn:active {
	opacity: 0.75;
}

.seller-stats {
	display: flex;
	align-items: center;
	margin-top: 24rpx;
	padding: 20rpx 0 0;
	border-top: 1rpx solid #f0f1f3;
}

.stat-item {
	flex: 1;
	display: flex;
	flex-direction: column;
	align-items: center;
	gap: 4rpx;
}

.stat-value {
	font-size: 30rpx;
	color: #1a1d21;
	font-weight: 700;
}

.stat-unit {
	font-size: 20rpx;
	font-weight: 500;
	color: #6a7484;
}

.stat-label {
	font-size: 22rpx;
	color: #8a94a6;
}

.stat-contact {
	font-size: 24rpx;
	color: #3677ff;
	font-weight: 600;
	padding: 8rpx 20rpx;
	background: #eef3ff;
	border-radius: 20rpx;
}

/* ===== 商品实拍 ===== */
.gallery-card {
	margin: 0 20rpx 16rpx;
	background: #fff;
	border-radius: 24rpx;
	padding: 28rpx;
}

.gallery-head {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 20rpx;
}

.gallery-title {
	font-size: 30rpx;
	color: #1a1d21;
	font-weight: 700;
}

.gallery-count {
	font-size: 24rpx;
	color: #8a94a6;
}

.gallery-grid {
	display: grid;
	grid-template-columns: repeat(2, 1fr);
	gap: 12rpx;
}

.gallery-image {
	width: 100%;
	height: 340rpx;
	border-radius: 14rpx;
	background: #f2f3f5;
}

.gallery-image:last-child:nth-child(odd) {
	grid-column: span 2;
	height: 300rpx;
}

/* ===== 交易保障 ===== */
.notice-card {
	margin: 0 20rpx 16rpx;
	background: #f8fdf6;
	border-radius: 24rpx;
	padding: 24rpx 28rpx;
	display: flex;
	align-items: center;
	gap: 18rpx;
	border: 1rpx solid #e6f5e0;
}

.notice-icon-wrap {
	width: 52rpx;
	height: 52rpx;
	border-radius: 50%;
	background: #16a34a;
	display: flex;
	align-items: center;
	justify-content: center;
	flex-shrink: 0;
}

.notice-icon {
	font-size: 28rpx;
	color: #fff;
	font-weight: 700;
}

.notice-content {
	flex: 1;
	min-width: 0;
}

.notice-title {
	display: block;
	font-size: 27rpx;
	color: #1a1d21;
	font-weight: 700;
	margin-bottom: 4rpx;
}

.notice-text {
	font-size: 23rpx;
	color: #6a7484;
	line-height: 1.45;
}

/* ===== 底部占位 ===== */
.bottom-space {
	height: 40rpx;
}

/* ===== 底部操作栏 ===== */
.action-bar {
	position: fixed;
	bottom: 0;
	left: 0;
	right: 0;
	display: flex;
	align-items: center;
	padding: 14rpx 20rpx;
	padding-bottom: calc(14rpx + env(safe-area-inset-bottom));
	background: #fff;
	box-shadow: 0 -4rpx 24rpx rgba(0, 0, 0, 0.05);
	z-index: 99;
}

.action-left {
	display: flex;
	gap: 8rpx;
	flex-shrink: 0;
}

.action-icon {
	display: flex;
	flex-direction: column;
	align-items: center;
	gap: 2rpx;
	padding: 6rpx 14rpx;
}

.action-ico {
	font-size: 38rpx;
	line-height: 1;
}

.action-label {
	font-size: 18rpx;
	color: #8a94a6;
	font-weight: 500;
}

.action-btns {
	flex: 1;
	display: flex;
	gap: 16rpx;
	margin-left: 16rpx;
}

.chat-btn,
.buy-btn {
	flex: 1;
	height: 80rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 28rpx;
	font-weight: 700;
	border-radius: 40rpx;
	transition: all 0.2s;
}

.chat-btn {
	background: #f5f6f8;
	color: #1a1d21;
	border: 1rpx solid #e4e6eb;
}

.chat-btn:active {
	background: #eaebed;
}

.buy-btn {
	background: #1a1d21;
	color: #fff;
}

.buy-btn:active {
	background: #333;
}

/* ===== 图片预览 ===== */
.preview-mask {
	position: fixed;
	inset: 0;
	background: #000;
	z-index: 999;
	display: flex;
	flex-direction: column;
}

.preview-swiper {
	flex: 1;
	width: 100%;
}

.preview-image {
	width: 100%;
	height: 100%;
}

.preview-close {
	position: absolute;
	top: calc(var(--status-bar-height, 44px) + 20rpx);
	left: 30rpx;
	width: 60rpx;
	height: 60rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	border-radius: 50%;
	color: #fff;
	font-size: 32rpx;
	font-weight: 300;
}

.preview-counter {
	position: absolute;
	bottom: 80rpx;
	left: 50%;
	transform: translateX(-50%);
	font-size: 26rpx;
	color: rgba(255, 255, 255, 0.8);
	font-weight: 500;
}
</style>