<template>
	<view class="page">
		<view class="header">
			<view>
				<text class="eyebrow">MY WALL</text>
				<text class="title">我的发布</text>
			</view>
			<view class="publish-btn" @click="goPublish">发布</view>
		</view>

		<!-- 吸附 tabs（scroll-view 外面，position:fixed 不会被裁剪） -->
		<view class="sticky-tabs-mask" v-show="tabsSticky" :style="{ top: stickyTop + 'px' }">
			<view class="tabs tabs-fixed">
				<view
					v-for="tab in tabs"
					:key="tab.value"
					class="tab"
					:class="{ active: activeTab === tab.value }"
					@click="switchTab(tab.value)"
				>
					{{ tab.label }}
				</view>
			</view>
		</view>

		<scroll-view class="main-scroll" scroll-y enhanced :show-scrollbar="false" @scroll="onScroll" @scrolltolower="loadMore">
			<view class="summary-card">
				<view class="summary-main">
					<text class="summary-num">{{ stats.total }}</text>
					<text class="summary-label">已发布帖子</text>
				</view>
				<view class="summary-line"></view>
				<view class="summary-grid">
					<view class="summary-item">
						<text>{{ stats.views }}</text>
						<text>浏览</text>
					</view>
					<view class="summary-item">
						<text>{{ stats.likes }}</text>
						<text>点赞</text>
					</view>
					<view class="summary-item">
						<text>{{ stats.comments }}</text>
						<text>评论</text>
					</view>
				</view>
			</view>

			<view class="tabs-wrapper">
				<view class="tabs">
					<view
						v-for="tab in tabs"
						:key="tab.value"
						class="tab"
						:class="{ active: activeTab === tab.value }"
						@click="switchTab(tab.value)"
					>
						{{ tab.label }}
					</view>
				</view>
			</view>

			<view class="post-list">
				<view v-for="(post, index) in filteredPosts" :key="post.id" class="post-card"
					:style="{ animationDelay: Math.min(index, 6) * 0.03 + 's' }">
					<view class="card-top">
						<image class="avatar" :src="displayAvatar(post)" mode="aspectFill"></image>
						<view class="user-block">
							<view class="name-row">
								<text class="nickname">{{ displayName(post) }}</text>
								<text v-if="post.isTop" class="mini-tag dark">置顶</text>
								<text v-if="post.isAnonymous" class="mini-tag">匿名</text>
							</view>
							<text class="time">{{ formatTime(post.createTime) }}</text>
						</view>
						<view class="status-pill" :class="statusClass(post.status)">
							{{ statusText(post.status) }}
						</view>
					</view>

					<view class="category-row">
						<text>{{ post.categoryName || '未分类' }}</text>
						<text v-if="post.subCategoryName">{{ post.subCategoryName }}</text>
					</view>

					<view class="content-wrap">
					<text
						class="content"
						:class="{ collapsed: !expandedIds.includes(post.id) && shouldCollapse(post) }"
					>{{ post.content }}</text>
					<view
						v-if="shouldCollapse(post) && !expandedIds.includes(post.id)"
						class="expand-fab"
						@click="toggleExpand(post.id)"
					>
						<text>展开</text>
					</view>
					<text
						v-if="shouldCollapse(post) && expandedIds.includes(post.id)"
						class="expand-inline"
						@click="toggleExpand(post.id)"
					> 收起</text>
				</view>

					<view v-if="post.mediaUrlList && post.mediaUrlList.length" class="media-container">
						<!-- 单个媒体（原始比例，横竖自适应） -->
						<view v-if="post.mediaUrlList.length === 1" class="media-single">
							<image v-if="isImageUrl(post.mediaUrlList[0])" :src="post.mediaUrlList[0]" mode="widthFix" :lazy-load="mediaShouldLazy(index)"
								class="single-image" @click.stop="previewMedia(post.mediaUrlList, 0)" />
							<view v-else-if="isVideoUrl(post.mediaUrlList[0])" class="single-video-wrapper"
								@click.stop="previewMedia(post.mediaUrlList, 0)">
								<image :src="getVideoThumb(post.mediaUrlList[0])" mode="aspectFill" :lazy-load="mediaShouldLazy(index)"
									class="single-video" />
								<view class="play-icon-overlay">
									<text class="play-icon">▶</text>
								</view>
							</view>
							<view v-else class="single-audio" @click="playAudio(post.mediaUrlList[0])">
								<text class="audio-icon">🔊</text>
								<text class="audio-text">点击播放音频</text>
							</view>
						</view>

						<!-- 多个媒体（正方形网格，强制1:1裁剪） -->
						<view v-else class="media-grid">
							<view v-for="(url, idx) in post.mediaUrlList" :key="idx" class="grid-item"
								@click.stop="previewMedia(post.mediaUrlList, idx)">
								<image v-if="isImageUrl(url)" :src="url" mode="aspectFill" :lazy-load="mediaShouldLazy(index)" class="grid-image-fill" />
								<view v-else-if="isVideoUrl(url)" class="grid-video-wrapper">
									<image :src="getVideoThumb(url)" mode="aspectFill" :lazy-load="mediaShouldLazy(index)" class="grid-image-fill" />
									<view class="video-play-overlay">
										<text class="play-icon-small">▶</text>
									</view>
								</view>
								<view v-else class="grid-audio-item" @click.stop="playAudio(url)">
									<text class="audio-icon-small">🔊</text>
								</view>
							</view>
						</view>
					</view>

					<view class="stat-row">
						<view class="stat">
							<image src="/static/wall/view.png" mode="aspectFit"></image>
							<text>{{ post.viewCount || 0 }}</text>
						</view>
						<view class="stat">
							<image src="/static/wall/like.png" mode="aspectFit"></image>
							<text>{{ post.likeCount || 0 }}</text>
						</view>
						<view class="stat">
							<image src="/static/wall/comment.png" mode="aspectFit"></image>
							<text>{{ post.commentCount || 0 }}</text>
						</view>
						<view class="stat">
							<image src="/static/wall/collect.png" mode="aspectFit"></image>
							<text>{{ post.collectCount || 0 }}</text>
						</view>
					</view>

					<view class="action-row">
						<view class="ghost-btn" @click="openEdit(post)">编辑</view>
						<view class="danger-btn" @click="confirmDelete(post)">删除</view>
					</view>
				</view>

				<view v-if="loading" class="state-text">加载中...</view>
				<view v-if="noMore && filteredPosts.length" class="state-text">没有更多了</view>
				<view v-if="!loading && !filteredPosts.length" class="empty">
					<text class="empty-title">还没有发布内容</text>
					<text class="empty-desc">写下这一刻，校园墙会替你保存。</text>
					<view class="empty-btn" @click="goPublish">去发布</view>
				</view>
			</view>
		</scroll-view>

		<!-- 图片预览组件 -->
		<ImagePreview 
			v-model:visible="previewVisible" 
			v-model:currentIndex="previewIndex" 
			:urls="previewUrls" 
		/>

		<!-- 独立视频全屏预览 -->
		<view class="preview-mask" v-if="singleVideoVisible" @click.self="closePreview">
			<video id="singleVideoPlayer" :src="singleVideoUrl" class="preview-video-full" controls autoplay
				:show-center-play-btn="false" @click.stop />
			<view class="close-btn" @click.stop="closePreview">✕</view>
		</view>
	</view>
</template>

<script setup>
import { computed, nextTick, onMounted, onBeforeUnmount, ref, watch } from 'vue'
import { onShow, onBackPress } from '@dcloudio/uni-app'
import request from '@/utils/request.js'
import ImagePreview from '@/components/ImagePreview/ImagePreview.vue'
import { getCurrentUser } from '@/utils/auth.js'

const userUid = ref('')
const postList = ref([])
const stats = ref({
	total: 0,
	views: 0,
	likes: 0,
	comments: 0
})
const pageNum = ref(1)
const pageSize = 10
const loading = ref(false)
const noMore = ref(false)
const activeTab = ref('all')
const CACHE_KEY = 'myPublishWallPostCache'
let lastFetchAt = 0
const preloadedUrls = new Set()
const hasHydratedCache = ref(false)

// 图片预览相关
const previewVisible = ref(false)
const previewUrls = ref([])
const previewIndex = ref(0)

// 独立视频预览
const singleVideoVisible = ref(false)
const singleVideoUrl = ref('')

// 展开/收起功能
const expandedIds = ref([])
// 滚动控制
const tabsSticky = ref(false)
const tabsThreshold = ref(0)
const stickyTop = ref(0)

const tabs = [
	{ label: '全部', value: 'all' },
	{ label: '已置顶', value: 'top' },
	{ label: '匿名', value: 'anonymous' }
]

const filteredPosts = computed(() => {
	if (activeTab.value === 'top') return postList.value.filter(item => item.isTop === 1)
	if (activeTab.value === 'anonymous') return postList.value.filter(item => item.isAnonymous === 1)
	return postList.value
})

// 判断是否需要折叠
const shouldCollapse = post => {
	if (!post.content) return false
	return post.content.length > 50 || post.content.includes('\n')
}

const getCurrentUid = () => {
	const user = getCurrentUser()
	if (!user) {
		uni.navigateTo({ url: '/pages/login/login' })
		return ''
	}
	return user.uid
}

const normalizeList = data => {
	if (Array.isArray(data)) return data
	if (Array.isArray(data?.list)) return data.list
	if (Array.isArray(data?.records)) return data.records
	if (Array.isArray(data?.rows)) return data.rows
	return []
}

const normalizePost = item => {
	const mediaUrlList = Array.isArray(item.mediaUrlList)
		? item.mediaUrlList
		: typeof item.mediaUrlList === 'string' && item.mediaUrlList
			? item.mediaUrlList.split(',').filter(Boolean)
			: []

	return {
		...item,
		mediaUrlList,
		isTop: Number(item.isTop || 0),
		isAnonymous: Number(item.isAnonymous || 0)
	}
}

// 切换展开/收起
const toggleExpand = id => {
	const index = expandedIds.value.indexOf(id)
	if (index > -1) {
		expandedIds.value.splice(index, 1)
	} else {
		expandedIds.value.push(id)
	}
}

// 滚动处理 - tabs 滚动到顶部时吸附
const onScroll = e => {
	const { scrollTop } = e.detail
	tabsSticky.value = scrollTop >= tabsThreshold.value
}

// 测量 tabs 在 scroll-view 内的位置，判断吸附阈值
const measureStickyLayout = () => {
	nextTick(() => {
		const query = uni.createSelectorQuery()
		query.select('.header').boundingClientRect()
		query.select('.summary-card').boundingClientRect()
		query.select('.tabs-wrapper').boundingClientRect()
		query.exec(res => {
			const headerRect = res?.[0]
			const summaryRect = res?.[1]
			const tabsRect = res?.[2]
			// 固定tabs的top = header高度，贴在header下方
			stickyTop.value = headerRect?.height || 0
			// scrollTop >= summary.height + tabs距离顶部 = 触发吸附
			const tabsTop = tabsRect?.top || 0
			tabsThreshold.value = (summaryRect?.height || 0) + tabsTop - stickyTop.value
		})
	})
}

const fetchPosts = async (reset = false) => {
	if (loading.value || (!reset && noMore.value)) return
	if (reset) {
		pageNum.value = 1
		noMore.value = false
	}

	loading.value = true
	try {
		const res = await request({
			url: '/wall/post/listByUserId',
			method: 'GET',
			data: {
				UID: userUid.value,
				pageNum: pageNum.value,
				pageSize
			}
		})

		if (res.code === 1) {
			const source = normalizeList(res.data).map(normalizePost)
			const nextList = reset ? source : postList.value.concat(source)
			postList.value = nextList
			if (reset) saveCachedPosts(nextList)
			preloadVisibleMedia(source)
			noMore.value = res.data?.hasNextPage === false || source.length < pageSize
			lastFetchAt = Date.now()
			return
		}

		throw new Error(res.message || '加载失败')
	} catch (error) {
		console.error('获取帖子列表失败:', error)
		if (reset && !hasHydratedCache.value) {
			postList.value = []
		}
		uni.showToast({
			title: '加载失败，请稍后重试',
			icon: 'none'
		})
	} finally {
		loading.value = false
	}
}

const normalizeStats = data => ({
	total: Number(data?.total || 0),
	views: Number(data?.views || 0),
	likes: Number(data?.likes || 0),
	comments: Number(data?.comments || 0)
})

const fetchPostStats = async () => {
	if (!userUid.value) return
	try {
		const res = await request({
			url: '/wall/post/statsByUserId',
			method: 'GET',
			data: {
				UID: userUid.value
			}
		})

		if (res.code === 1) {
			stats.value = normalizeStats(res.data)
		}
	} catch (error) {
		console.error('获取我的发布统计失败:', error)
	}
}

const loadMore = () => {
	if (loading.value || noMore.value) return
	pageNum.value += 1
	fetchPosts(false)
}

const hydrateCachedPosts = () => {
	const cached = uni.getStorageSync(CACHE_KEY)
	const list = Array.isArray(cached) ? cached : []
	if (!list.length) return
	postList.value = list.map(normalizePost)
	hasHydratedCache.value = true
	preloadVisibleMedia(postList.value)
}

const saveCachedPosts = posts => {
	uni.setStorageSync(CACHE_KEY, posts.slice(0, 30))
}

const switchTab = value => {
	activeTab.value = value
}

const displayName = post => post.isAnonymous ? '匿名同学' : post.nickname || ''
const displayAvatar = post => post.avatar
const formatTime = time => time ? String(time).replace('T', ' ') : ''
const statusText = status => ({ 0: '待审核', 1: '已发布', 2: '未通过' }[Number(status)] || '已发布')
const statusClass = status => Number(status) === 1 ? 'success' : Number(status) === 2 ? 'error' : 'pending'

const isImageUrl = url => {
	const ext = url?.split('?')[0].split('.').pop()?.toLowerCase()
	return ['jpg', 'jpeg', 'png', 'gif', 'webp', 'bmp', 'heic'].includes(ext)
}

const isVideoUrl = url => {
	const ext = url?.split('?')[0].split('.').pop()?.toLowerCase()
	return ['mp4', 'mov', 'avi', 'mkv'].includes(ext)
}

const preloadImage = url => {
	if (!url || preloadedUrls.has(url)) return
	preloadedUrls.add(url)
	// #ifdef H5
	const img = new Image()
	img.src = url
	// #endif
	// #ifndef H5
	uni.getImageInfo({ src: url })
	// #endif
}

const getVideoThumb = url => {
	if (url && url.includes('aliyuncs.com')) return url + '?x-oss-process=video/snapshot,t_0,f_jpg'
	return '/static/wall/video-placeholder.png'
}

const preloadPostMedia = posts => {
	posts.forEach(post => {
		;(post.mediaUrlList || []).forEach(url => {
			if (isImageUrl(url)) {
				preloadImage(url)
			} else if (isVideoUrl(url)) {
				preloadImage(getVideoThumb(url))
			}
		})
	})
}

const mediaShouldLazy = index => index > 2

const preloadVisibleMedia = posts => {
	preloadPostMedia(posts.slice(0, 3))
	setTimeout(() => {
		preloadPostMedia(posts.slice(3, 10))
	}, 350)
}

// 预览分流：视频独立，图片走组件
const previewMedia = (urls, index) => {
	const url = urls[index]
	if (isVideoUrl(url)) {
		// 保存当前滚动位置
		const scrollView = document.querySelector('.page')
		if (scrollView) {
			savedScrollTop = scrollView.scrollTop
		}
		singleVideoUrl.value = url
		singleVideoVisible.value = true
		document.body.classList.add('preview-lock')
		nextTick(() => {
			uni.createVideoContext('singleVideoPlayer').play()
		})
		// #ifdef H5
		history.pushState(null, '', location.href)
		// #endif
	} else {
		// 只取图片URL
		const imageUrls = urls.filter(u => isImageUrl(u))
		const imageIndex = imageUrls.indexOf(url)
		previewUrls.value = imageUrls
		previewIndex.value = imageIndex >= 0 ? imageIndex : 0
		previewVisible.value = true
	}
}

// 关闭预览
const closePreview = () => {
	previewVisible.value = false
	previewUrls.value = []
	previewIndex.value = 0
	singleVideoVisible.value = false
	singleVideoUrl.value = ''
	document.body.classList.remove('preview-lock')
	// 恢复滚动位置
	nextTick(() => {
		const scrollView = document.querySelector('.page')
		if (scrollView && savedScrollTop > 0) {
			scrollView.scrollTop = savedScrollTop
		}
	})
}

// 保存滚动位置
let savedScrollTop = 0

// H5 popstate 监听
const handlePopState = (e) => {
	e.preventDefault()
	e.stopPropagation()
	if (singleVideoVisible.value) {
		closePreview()
	}
}

// 返回键拦截
const backPressHandler = () => {
	if (singleVideoVisible.value) {
		closePreview()
		return true
	}
}

// 监听视频预览状态变化，同步处理 body 样式
watch(singleVideoVisible, (val) => {
	if (val) {
		document.body.classList.add('preview-lock')
	} else {
		document.body.classList.remove('preview-lock')
	}
})

const playAudio = url => {
	const audio = uni.createInnerAudioContext()
	audio.src = url
	audio.play()
}

const openEdit = post => {
	const editData = encodeURIComponent(JSON.stringify(post))
	uni.navigateTo({
		url: `/pages/wall/publish?editData=${editData}`
	})
}

const confirmDelete = post => {
	uni.showModal({
		title: '删除帖子',
		content: '删除后该帖子将不再展示，确定继续吗？',
		confirmText: '删除',
		confirmColor: '#d84f4f',
		success: async res => {
			if (!res.confirm) return
			await deletePost(post.id)
		}
	})
}

const deletePost = async id => {
	try {
		const res = await request({
			url: `/wall/post/del/${id}`,
			method: 'DELETE'
		})

		if (res.code === 1) {
			postList.value = postList.value.filter(item => item.id !== id)
			fetchPostStats()
			uni.showToast({
				title: '已删除',
				icon: 'success'
			})
			return
		}

		throw new Error(res.message || '删除失败')
	} catch (error) {
		uni.showToast({
			title: '删除失败',
			icon: 'none'
		})
	}
}

const goPublish = () => {
	uni.navigateTo({
		url: '/pages/wall/publish'
	})
}

onMounted(() => {
	userUid.value = getCurrentUid()
	hydrateCachedPosts()
	fetchPostStats()
	fetchPosts(true)
	measureStickyLayout()
	// #ifdef H5
	window.addEventListener('popstate', handlePopState)
	// #endif
})

onBeforeUnmount(() => {
	// #ifdef H5
	window.removeEventListener('popstate', handlePopState)
	// #endif
})

// 返回键拦截
onBackPress(backPressHandler)

onShow(() => {
	if (!userUid.value) return
	fetchPostStats()
	if (Date.now() - lastFetchAt < 3000) {
		return
	}
	fetchPosts(true)
})
</script>

<style scoped>
.page {
	min-height: 100vh;
	height: 100vh;
	overflow: hidden;
	padding-top: var(--status-bar-height);
	background: #f2f3f5;
	color: #20242a;
}

.header {
	padding: 26rpx 28rpx 18rpx;
	display: flex;
	align-items: center;
	justify-content: space-between;
	background: #f2f3f5;
	position: relative;
	z-index: 10;
}

.eyebrow {
	display: block;
	font-size: 20rpx;
	color: #9aa3af;
	font-weight: 800;
	letter-spacing: 0;
	margin-bottom: 8rpx;
}

.title {
	display: block;
	font-size: 46rpx;
	line-height: 1;
	font-weight: 900;
}

.publish-btn {
	height: 64rpx;
	padding: 0 26rpx;
	border-radius: 16rpx;
	background: #20242a;
	color: #fff;
	font-size: 25rpx;
	font-weight: 900;
	display: flex;
	align-items: center;
	justify-content: center;
}

.main-scroll {
	height: calc(100vh - 140rpx);
	will-change: scroll-position;
	-webkit-overflow-scrolling: touch;
}

.summary-card {
	margin: 0 28rpx 22rpx;
	padding: 28rpx;
	border-radius: 16rpx;
	background: #ffffff;
	box-shadow: 0 22rpx 54rpx rgba(31, 40, 51, 0.06);
	border: 1rpx solid rgba(32, 36, 42, 0.06);
	display: flex;
	align-items: center;
}

.summary-main {
	width: 190rpx;
}

.summary-num {
	display: block;
	font-size: 58rpx;
	font-weight: 900;
	line-height: 1;
}

.summary-label {
	display: block;
	font-size: 22rpx;
	color: #8b95a1;
	font-weight: 700;
	margin-top: 8rpx;
}

.summary-line {
	width: 1rpx;
	height: 86rpx;
	background: #edf0f4;
	margin: 0 28rpx 0 12rpx;
}

.summary-grid {
	flex: 1;
	display: flex;
	justify-content: space-between;
}

.summary-item {
	display: flex;
	flex-direction: column;
	align-items: center;
	gap: 8rpx;
}

.summary-item text:first-child {
	font-size: 32rpx;
	font-weight: 900;
}

.summary-item text:last-child {
	font-size: 21rpx;
	color: #8b95a1;
	font-weight: 700;
}

.tabs-wrapper {
	background: #f2f3f5;
	height: 98rpx;
}

.tabs-wrapper .tabs {
	display: flex;
	gap: 14rpx;
	padding: 16rpx 28rpx 18rpx;
	background:#f2f3f5;
	box-shadow: 0 3rpx 8rpx -5rpx rgba(0, 0, 0, 0.08);
}

/* 吸附 tabs（scroll-view 外面，position:fixed 不会被裁剪） */
.sticky-tabs-mask {
	position: fixed;
	top: 0;
	left: 0;
	right: 0;
	z-index: 100;
	background: #f2f3f5;
}

.tabs-fixed {
	display: flex;
	gap: 14rpx;
	padding: 16rpx 28rpx 16rpx;
	background: #f2f3f5;
	box-shadow: 0 2rpx 12rpx -4rpx rgba(0, 0, 0, 0.08);
}

.tab {
	flex: 1;
	height: 64rpx;
	border-radius: 16rpx;
	background: #ffffff;
	border: 1rpx solid rgba(32, 36, 42, 0.06);
	color: #7d8792;
	font-size: 24rpx;
	font-weight: 800;
	display: flex;
	align-items: center;
	justify-content: center;
}

.tab.active {
	background: #20242a;
	color: #ffffff;
	box-shadow: 0 14rpx 30rpx rgba(32, 36, 42, 0.12);
}

.post-list {
	padding: 10rpx 28rpx 44rpx;
}

.post-card {
	margin-bottom: 22rpx;
	padding: 26rpx;
	border-radius: 16rpx;
	background: #ffffff;
	border: 1rpx solid rgba(32, 36, 42, 0.06);
	box-shadow: 0 18rpx 46rpx rgba(31, 40, 51, 0.05);
	opacity: 0;
	transform: scale(0.85) translateZ(0);
	animation: cardFadeIn 0.35s cubic-bezier(0.25, 0.46, 0.45, 0.94) forwards;
	will-change: transform, opacity;
}

@keyframes cardFadeIn {
	to {
		opacity: 1;
		transform: scale(1) translateZ(0);
	}
}

.card-top {
	display: flex;
	align-items: center;
	margin-bottom: 18rpx;
}

.avatar {
	width: 74rpx;
	height: 74rpx;
	border-radius: 50%;
	margin-right: 18rpx;
	background: #eef1f5;
}

.user-block {
	flex: 1;
	min-width: 0;
}

.name-row {
	display: flex;
	align-items: center;
	gap: 10rpx;
	margin-bottom: 7rpx;
}

.nickname {
	font-size: 28rpx;
	font-weight: 900;
	max-width: 260rpx;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}

.mini-tag {
	height: 34rpx;
	padding: 0 12rpx;
	border-radius: 999rpx;
	background: #eef1f5;
	color: #7a8490;
	font-size: 19rpx;
	font-weight: 800;
	display: flex;
	align-items: center;
}

.mini-tag.dark {
	background: #20242a;
	color: #ffffff;
}

.time {
	display: block;
	font-size: 22rpx;
	color: #99a2ad;
}

.status-pill {
	height: 44rpx;
	padding: 0 16rpx;
	border-radius: 999rpx;
	font-size: 21rpx;
	font-weight: 800;
	display: flex;
	align-items: center;
}

.status-pill.success {
	background: #edf8f2;
	color: #3f9466;
}

.status-pill.pending {
	background: #f7f2e7;
	color: #a6782a;
}

.status-pill.error {
	background: #fceeee;
	color: #cc5b5b;
}

.category-row {
	display: flex;
	gap: 12rpx;
	margin-bottom: 16rpx;
}

.category-row text {
	height: 42rpx;
	padding: 0 16rpx;
	border-radius: 999rpx;
	background: #f5f6f8;
	color: #717b87;
	font-size: 21rpx;
	font-weight: 800;
	display: flex;
	align-items: center;
}

.content-wrap {
	margin-bottom: 18rpx;
	position: relative;
}

.content {
	display: block;
	font-size: 29rpx;
	line-height: 1.7;
	color: #2e333a;
	transition: max-height 0.35s ease;
}

.content.collapsed {
	display: -webkit-box;
	-webkit-box-orient: vertical;
	-webkit-line-clamp: 5;
	overflow: hidden;
}

.expand-fab {
	position: absolute;
	right: 0;
	bottom: 2rpx;
	background: linear-gradient(to right, transparent 0%, #ffffff 50%, #ffffff 100%);
	padding-left: 48rpx;
}

.expand-fab text {
	font-size: 26rpx;
	color: #6c5ce7;
	font-weight: 700;
}

.expand-inline {
	font-size: 26rpx;
	color: #6c5ce7;
	font-weight: 700;
}

/* 媒体样式 */
.media-container {
	margin-top: 16rpx;
	margin-bottom: 18rpx;
	contain: layout style paint;
}

.media-single {
	width: 100%;
	display: flex;
	justify-content: center;
}

.single-image {
	max-width: 100%;
	height: auto;
	border-radius: 16rpx;
}

.single-video-wrapper {
	width: 100%;
	position: relative;
	border-radius: 16rpx;
	overflow: hidden;
}

.single-video {
	width: 100%;
	height: 380rpx;
}

.play-icon-overlay {
	position: absolute;
	top: 50%;
	left: 50%;
	transform: translate(-50%, -50%);
	width: 100rpx;
	height: 100rpx;
	background: rgba(0, 0, 0, 0.5);
	border-radius: 50%;
	display: flex;
	align-items: center;
	justify-content: center;
}

.play-icon {
	color: #fff;
	font-size: 48rpx;
}

.single-audio {
	display: flex;
	align-items: center;
	background: #f5f5f5;
	border-radius: 16rpx;
	padding: 20rpx;
}

.audio-icon {
	font-size: 40rpx;
	margin-right: 12rpx;
}

.audio-text {
	font-size: 24rpx;
	color: #666;
}

.media-grid {
	display: flex;
	flex-wrap: wrap;
	gap: 10rpx;
}

.media-grid.single .media-item {
	width: 100%;
	height: 380rpx;
}

.grid-item {
	width: calc((100% - 20rpx) / 3);
	height: 190rpx;
	border-radius: 16rpx;
	overflow: hidden;
	background: #f0f0f0;
	position: relative;
}

.grid-image-fill {
	width: 100%;
	height: 100%;
	object-fit: cover;
}

.grid-video-wrapper {
	width: 100%;
	height: 100%;
	position: relative;
}

.video-play-overlay {
	position: absolute;
	top: 50%;
	left: 50%;
	transform: translate(-50%, -50%);
	width: 60rpx;
	height: 60rpx;
	background: rgba(0, 0, 0, 0.6);
	border-radius: 50%;
	display: flex;
	align-items: center;
	justify-content: center;
}

.play-icon-small {
	color: #fff;
	font-size: 28rpx;
}

.grid-audio-item {
	display: flex;
	align-items: center;
	justify-content: center;
	background: #e0e0e0;
	width: 100%;
	height: 100%;
}

.audio-icon-small {
	font-size: 48rpx;
}

.stat-row {
	display: flex;
	align-items: center;
	gap: 26rpx;
	padding-top: 16rpx;
	border-top: 1rpx solid #f0f2f5;
	margin-bottom: 18rpx;
}

.stat {
	display: flex;
	align-items: center;
	gap: 8rpx;
	color: #8b95a1;
	font-size: 23rpx;
	font-weight: 700;
}

.stat image {
	width: 30rpx;
	height: 30rpx;
	opacity: 0.72;
}

.action-row {
	display: flex;
	gap: 14rpx;
}

.ghost-btn,
.danger-btn {
	flex: 1;
	height: 68rpx;
	border-radius: 16rpx;
	font-size: 25rpx;
	font-weight: 900;
	display: flex;
	align-items: center;
	justify-content: center;
}

.ghost-btn {
	background: #f4f6f8;
	color: #20242a;
}

.danger-btn {
	background: #fff1f1;
	color: #d84f4f;
}

.state-text {
	text-align: center;
	padding: 30rpx 0;
	color: #99a2ad;
	font-size: 24rpx;
}

.empty {
	margin-top: 80rpx;
	display: flex;
	flex-direction: column;
	align-items: center;
	text-align: center;
}

.empty-title {
	font-size: 32rpx;
	font-weight: 900;
	margin-bottom: 12rpx;
}

.empty-desc {
	font-size: 24rpx;
	color: #8b95a1;
	margin-bottom: 28rpx;
}

.empty-btn {
	height: 68rpx;
	padding: 0 34rpx;
	border-radius: 16rpx;
	background: #20242a;
	color: #ffffff;
	font-size: 25rpx;
	font-weight: 900;
	display: flex;
	align-items: center;
	justify-content: center;
}

/* 视频预览遮罩 */
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

.preview-video-full {
	width: 100%;
	height: 100%;
	max-width: 800px;
	max-height: 90vh;
}

.close-btn {
	position: absolute;
	top: 30rpx;
	right: 30rpx;
	width: 60rpx;
	height: 60rpx;
	color: #fff;
	font-size: 36rpx;
	background: rgba(0, 0, 0, 0.5);
	border-radius: 50%;
	display: flex;
	align-items: center;
	justify-content: center;
}
</style>