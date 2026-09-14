<template>
	<view class="wall-page">
		<!-- 顶部标题 + 搜索框（始终固定） -->
		<view class="header">
			<text class="title">校园贴吧</text>
			<view class="search-box" :class="{ 'search-expanded': isSearchFocused }">
				<input v-model="keyword" placeholder="搜索帖子..." class="search-input" @confirm="onSearch"
					@focus="isSearchFocused = true" @blur="isSearchFocused = false" />
				<text class="search-icon" @click="onSearch">搜索</text>
			</view>
		</view>

		<!-- 分类栏（相对定位，浮在列表上方，从底部收起/展开） -->
		<view class="top-bar" :class="{ 'top-bar-hidden': !categoryVisible }">
			<view class="category-rows">
				<!-- 绗竴琛?-->
				<scroll-view class="category-scroll" scroll-x enable-flex :show-scrollbar="false">
					<view class="category-list">
						<view class="cate-item"
							:class="{ active: selectedParentIds.length === 0 && selectedSubIds.length === 0 &&filterAnonymous === 0 }"
							@click="toggleParentFilter(null)">全部</view>
						<!-- 匿名甯栧瓙 浼犲浐瀹氭爣璇?-1 -->
						<view class="cate-item" :class="{ active: filterAnonymous === 1 }"
							@click="toggleParentFilter(-1)">
							匿名帖子
						</view>
						<view v-for="item in categoryRow1" :key="item.id" class="cate-item"
							:class="{ active: selectedParentIds.includes(item.id) }"
							@click="toggleParentFilter(item.id)">
							{{ item.name }}
						</view>
					</view>
				</scroll-view>

				<!-- 绗簩琛?-->
				<scroll-view class="category-scroll" scroll-x enable-flex :show-scrollbar="false">
					<view class="category-list">
						<view v-for="item in categoryRow2" :key="item.id" class="cate-item"
							:class="{ active: selectedParentIds.includes(item.id) }"
							@click="toggleParentFilter(item.id)">
							{{ item.name }}
						</view>
					</view>
				</scroll-view>
			</view>

			<!-- 鍥哄畾绛涢€夋寜閽?-->
			<view class="filter-btn-wrapper">
				<view class="gradient-mask"></view>
				<view class="filter-btn" @click="showFilter = true">
					<text class="filter-icon">≡</text>
				</view>
			</view>
		</view>

		<!-- 帖子列表（动态padding-top，和分类栏动画同步） -->
		<scroll-view class="post-list" :style="{paddingTop: listTopPadding}" scroll-y
			:refresher-enabled="true"
			:refresher-triggered="refreshing"
			@refresherrefresh="onRefresh"
			@scroll="onScroll" @scrolltolower="loadMore">



			<view class="post-card" v-for="(post, index) in postList" :key="post.postNo || index"
				:style="{ animationDelay: index * 0.03 + 's' }">
				<!-- 头部：头像 + 名称/时间 + 右侧操作 -->
				<view class="card-header">
					<view class="avatar-wrapper" @click="goUserPage(post)">
						<image class="avatar" :src="post.avatar" mode="aspectFill"></image>
					</view>
					<view class="user-info">
						<text class="nickname">{{ post.nickname }}</text>
						<text class="create-time">{{ formatTime(post.createTime) }}</text>
					</view>
					<view class="header-actions">
						<text class="top-tag" v-if="post.isTop">置顶</text>
						<text class="anonymous-tag" v-if="post.isAnonymous">匿名</text>
						<view class="follow-btn" :class="{ followed: post.isFollow }" @click="toggleFollow(post)">
							<text>{{ post.isFollow ? '已关注' : '关注' }}</text>
						</view>
						<view class="report-btn" @click="reportPost(post)">举报</view>
					</view>
				</view>

				<!-- 正文内容 -->
				<view class="content-text">{{ post.content }}</view>

				<!-- 濯掍綋鍖哄煙锛堟贩鍚堝睍绀猴級 -->
				<view v-if="post.mediaUrlList && post.mediaUrlList.length" class="media-container">
					<!-- 鍗曚釜濯掍綋锛堝師濮嬫瘮渚嬶紝妯珫鑷€傚簲锛?-->
					<view v-if="post.mediaUrlList.length === 1" class="media-single">
						<image v-if="isImageUrl(post.mediaUrlList[0])" :src="post.mediaUrlList[0]" mode="widthFix"
							class="single-image" @click.stop="previewMedia(post.mediaUrlList, 0)" />
						<view v-else-if="isVideoUrl(post.mediaUrlList[0])" class="single-video-wrapper"
							@click.stop="previewMedia(post.mediaUrlList, 0)">
							<image :src="getVideoThumb(post.mediaUrlList[0])" mode="aspectFill"
								class="single-video" />
							<view class="play-icon-overlay">
						<text class="play-icon">▶</text>
							</view>
						</view>
						<view v-else class="single-audio" @click="playAudio(post.mediaUrlList[0])">
							<text class="audio-icon">音频</text>
							<text class="audio-text">点击播放音频</text>
						</view>
					</view>

					<!-- 多个媒体（正方格网格，强制1:1比例） -->
					<view v-else class="media-grid">
						<view v-for="(url, idx) in post.mediaUrlList" :key="idx" class="grid-item"
							@click.stop="previewMedia(post.mediaUrlList, idx)">
							<image v-if="isImageUrl(url)" :src="url" mode="aspectFill" class="grid-image-fill" />
							<view v-else-if="isVideoUrl(url)" class="grid-video-wrapper">
								<image :src="getVideoThumb(url)" mode="aspectFill" class="grid-image-fill" />
								<view class="video-play-overlay">
									<text class="play-icon-small">▶</text>
								</view>
							</view>
							<view v-else class="grid-audio-item" @click.stop="playAudio(url)">
								<text class="audio-icon-small">音频</text>
							</view>
						</view>
					</view>
				</view>

				<!-- 搴曢儴浜掑姩鏍?-->
				<view class="card-footer">
					<view class="footer-left">
						<view class="action-item">
							<image src="/static/wall/view.png" mode="aspectFill"></image>
							<text v-if="post.viewCount > 0">{{ post.viewCount }}</text>
						</view>
					</view>
					<view class="footer-right">
						<view class="action-item" @click="toggleCollect(post)">
							<image
								:src="post.isCollect ? '/static/wall/collect-active.png' : '/static/wall/collect.png'"
								mode="aspectFill"></image>
							<text v-if="post.collectCount > 0">{{ post.collectCount }}</text>
						</view>
						<view class="action-item" @click="toggleLike(post)">
							<image :src="post.isLike ? '/static/wall/like-active.png' : '/static/wall/like.png'"
								mode="aspectFill"></image>
							<text v-if="post.likeCount > 0">{{ post.likeCount }}</text>
						</view>
						<view class="action-item" @click="openCommentPanel(post)">
							<image src="/static/wall/comment.png" mode="aspectFill"></image>
							<text v-if="post.commentCount > 0">{{ post.commentCount }}</text>
						</view>
						<view class="action-item" @click="sharePost(post)">
							<text class="share-icon">分享</text>
						</view>
					</view>
				</view>

				<!-- 棬璇勮 -->
				<view class="comment-preview" v-if="post.hotComments && post.hotComments.length">
					<view class="comment-item" v-for="(com, cIdx) in getTopThreeHotComments(post.hotComments)" :key="cIdx" @click="openCommentPanel(post, com.id)">
						<text class="com-nick">{{ com.nickname }}：</text>
						<text class="com-text">{{ com.content }}</text>
						<view class="com-like" @click.stop="toggleCommentLike(com)">
							<image
								:src="com.isLike ? '/static/wall/comment-like-active.png' : '/static/wall/comment-like.png'"
								mode="aspectFill" style="width:24rpx;height:24rpx;"></image>
							<text v-if="com.likeCount > 0">{{ com.likeCount }}</text>
						</view>
					</view>
				</view>
			</view>

			<!-- 加载状态 -->
			<view class="load-more" v-if="loading">加载中...</view>
			<view class="load-more" v-if="noMore && postList.length">没有更多了</view>
			<view class="empty" v-if="!loading && postList.length === 0">暂无帖子，快来发布吧</view>
		</scroll-view>

		<!-- 鍙戝竷鎸夐挳 -->
		<view class="float-btn" @click="goPublish">
			<image src="/static/wall/edit.png" mode="aspectFill"></image>
		</view>

		<!-- 绛涢€夋诞绐楋紙鍙充晶婊戝嚭锛?-->
		<view class="filter-mask" v-if="showFilter" @click="showFilter = false"></view>
		<view class="filter-drawer" :class="{ show: showFilter }">
			<view class="drawer-header">
				<text class="drawer-title">分类筛选</text>
				<text class="drawer-close" @click="showFilter = false">×</text>
			</view>
			<scroll-view class="drawer-content" scroll-y @touchmove.stop.prevent>
				<view class="filter-group" v-for="p in parentCategoryList" :key="p.id">
					<text class="parent-name">{{ p.name }}</text>
					<view class="child-list">
						<view v-for="c in getSubByParent(p.id)" :key="c.id" class="child-item"
							:class="{ checked: selectedSubIds.includes(c.id) }" @click="toggleSubFilter(c.id)">
							{{ c.name }}
							<text class="hide-tag" v-if="c.isShow === 0">隐藏</text>
						</view>
					</view>
				</view>
			</scroll-view>
			<view class="drawer-footer">
				<view class="reset-btn" @click="resetFilter">重置</view>
				<view class="confirm-btn" @click="confirmFilter">确定</view>
			</view>
		</view>

		<!-- 评论区 抖音风格 -->
		<view class="comment-mask" v-if="commentPanelVisible" @click="closeCommentPanel"></view>
		<view class="comment-drawer" :class="{ show: commentPanelVisible }">
			<!-- 头部：评论数 + 排序切换 -->
			<view class="comment-header">
				<text class="comment-count">{{ commentList.length }}条评论</text>
				<view class="comment-sort-tabs">
					<text class="sort-tab" :class="{ active: commentSort === 'hot' }" @click="commentSort = 'hot'">最热</text>
					<text class="sort-tab" :class="{ active: commentSort === 'new' }" @click="commentSort = 'new'">最新</text>
				</view>
				<text class="drawer-close" @click="closeCommentPanel">×</text>
			</view>

			<!-- 评论列表 -->
			<scroll-view class="comment-list" scroll-y :show-scrollbar="false">
				<view v-if="commentLoading" class="comment-empty">
					<text class="loading-spinner"></text>
					<text>加载中...</text>
				</view>
				<view v-else-if="sortedComments.length === 0" class="comment-empty">
					<text class="empty-icon">💬</text>
					<text class="empty-text">还没有评论</text>
					<text class="empty-desc">来坐第一排，说点什么吧</text>
				</view>
				<view v-else>
					<view class="comment-item" v-for="(item, index) in sortedComments" :key="item.id">
						<image class="comment-avatar" :src="item.avatar || '/static/default-avatar.png'" mode="aspectFill" @error="$event.target.src='/static/default-avatar.png'" />
						<view class="comment-body">
							<view class="comment-top">
								<text class="comment-name">{{ item.nickname || '同学' }}</text>
								<text class="comment-time">{{ relativeTime(item.createTime) }}</text>
							</view>
							<view class="comment-text" @click="replyToComment(item)">
								<image v-if="item.mediaUrls" class="comment-image" :src="item.mediaUrls" mode="aspectFill" @click.stop="previewMedia([item.mediaUrls], 0)" />
								<text v-if="item.content">{{ item.content }}</text>
							</view>

							<!-- 回复列表 -->
							<view v-if="item.children && item.children.length > 0" class="reply-section">
								<view class="reply-item" v-for="reply in getDisplayReplies(item, index)" :key="reply.id">
									<image class="reply-avatar" :src="reply.avatar || '/static/default-avatar.png'" mode="aspectFill" @error="$event.target.src='/static/default-avatar.png'" />
									<view class="reply-content-wrapper">
										<view class="reply-header">
											<text class="reply-name" @click="replyToComment(reply, item)">{{ reply.nickname || '同学' }}</text>
											<text v-if="reply.replyNickname" class="reply-to"> 回复 </text>
											<text v-if="reply.replyNickname" class="reply-target" @click="replyToComment(reply, item)">{{ reply.replyNickname }}</text>
											<text class="reply-colon">：</text>
										</view>
										<view class="reply-text-row">
											<image v-if="reply.mediaUrls" class="reply-image" :src="reply.mediaUrls" mode="aspectFill" @click.stop="previewMedia([reply.mediaUrls], 0)" />
											<text v-if="reply.content" class="reply-content">{{ reply.content }}</text>
											<!-- 二级评论点赞按钮 -->
											<view class="reply-like-btn" @click.stop="toggleCommentLike(reply)">
												<image
													:src="reply.isLike ? '/static/wall/comment-like-active.png' : '/static/wall/comment-like.png'"
													mode="aspectFit"
													style="width:28rpx;height:28rpx;">
												</image>
												<text v-if="(reply.likeCount || 0) > 0" class="reply-like-count">{{ reply.likeCount }}</text>
											</view>
										</view>
									</view>
								</view>
								<view v-if="item.children.length > (index === 0 ? firstReplyShowCount : replyShowCount) && !showAllReplies[item.id]" class="expand-replies" @click="expandReplies(item.id)">
									<text>展开{{ item.children.length - (index === 0 ? firstReplyShowCount : replyShowCount) }}条回复 ▾</text>
								</view>
								<view v-if="showAllReplies[item.id] && item.children.length > (index === 0 ? firstReplyShowCount : replyShowCount)" class="expand-replies" @click="collapseReplies(item.id)">
									<text>收起 ▴</text>
								</view>
							</view>
						</view>
						<!-- 右侧点赞按钮 -->
						<view class="comment-like-btn" @click="toggleCommentLike(item)">
							<image
								:src="item.isLike ? '/static/wall/comment-like-active.png' : '/static/wall/comment-like.png'"
								mode="aspectFit"
								style="width:36rpx;height:36rpx;">
							</image>
							<text v-if="(item.likeCount || 0) > 0" class="like-count">{{ item.likeCount }}</text>
						</view>
					</view>
					<view class="comment-bottom-safe"></view>
				</view>
			</scroll-view>

			<!-- 底部输入栏 -->
			<view class="comment-input-bar">
				<view class="reply-indicator" v-if="replyTarget">
					<text class="reply-indicator-text">回复 @{{ replyTarget.nickname }}</text>
					<text class="cancel-reply" @click="cancelReply">×</text>
				</view>
				<!-- 评论文本图片预览 -->
				<view class="comment-image-preview" v-if="commentTempImage || commentMediaUrls">
					<image :src="commentTempImage || commentMediaUrls" mode="aspectFill" class="comment-preview-img" />
					<view class="comment-preview-remove" @click="commentTempImage = ''; commentMediaUrls = ''">×</view>
				</view>
				<view class="input-row">
					<image class="input-avatar" :src="currentUserAvatar" mode="aspectFill" @error="$event.target.src='/static/default-avatar.png'" />
					<view class="input-box">
						<input class="comment-input" 
							v-model="commentContent" 
							maxlength="200" 
							:placeholder="commentPlaceholder" 
							confirm-type="send" 
							@confirm="submitComment"
							@focus="inputFocused = true"
							@blur="inputFocused = false" />
					</view>
					<view class="emoji-btn" @click="showEmojiPanel = !showEmojiPanel">😀</view>
					<view class="image-btn" @click="chooseCommentImage">📷</view>
					<button class="comment-submit-btn" :disabled="(!commentContent.trim() && !commentMediaUrls && !commentTempImage) || submittingComment" @click="submitComment()">发送</button>
				</view>
				<!-- 表情面板 -->
				<view class="emoji-panel" v-if="showEmojiPanel">
					<text v-for="emoji in quickEmojis" :key="emoji" class="emoji-item" @click="appendEmoji(emoji)">{{ emoji }}</text>
				</view>
			</view>
		</view>

		<!-- 鍥剧墖棰勮缁勪欢 -->
		<ImagePreview 
			v-model:visible="previewVisible" 
			v-model:currentIndex="previewIndex" 
			:urls="previewUrls" 
		/>

				<!-- 独立视频全屏预览 -->
		<view class="preview-mask" v-if="singleVideoVisible" @click.self="closePreview">
			<video id="singleVideoPlayer" :src="singleVideoUrl" class="preview-video-full" controls autoplay
				:show-center-play-btn="false" @click.stop />
			<view class="close-btn" @click.stop="closePreview">×</view>
		</view>

		<!-- 
		========================================
		AI 校园助手 
		原理：右下角悬浮按钮 → 点击打开聊天面板 → 输入问题 → 
		  前端调用后端 /ai/chat 接口 → 后端转发给大模型 API → 返回结果显示
		========================================
		-->
		<view class="ai-assistant" :class="{ 'ai-open': aiPanelOpen }">
			<!-- 聊天面板：aiPanelOpen 为 true 时才显示 -->
			<view class="ai-panel" v-if="aiPanelOpen">
				<!-- 顶部标题栏 -->
				<view class="ai-header">
					<text class="ai-title">🤖 校园助手小墙</text>
					<text class="ai-close" @click="aiPanelOpen = false">×</text>
				</view>
				<!-- 消息列表区域：scroll-view 实现滚动，scroll-top 控制自动滚到底部 -->
				<scroll-view class="ai-messages" scroll-y :scroll-top="aiScrollTop" scroll-with-animation>
					<!-- 欢迎页：没有消息时显示快捷问题入口 -->
					<view class="ai-welcome" v-if="aiMessages.length === 0">
						<text class="ai-welcome-text">👋 你好！我是小墙，有什么可以帮你的吗？</text>
						<view class="ai-quick-questions">
							<text class="ai-quick-btn" @click="askAi('图书馆几点关门？')">📚 图书馆几点关门？</text>
							<text class="ai-quick-btn" @click="askAi('食堂哪个窗口好吃？')">🍜 食堂哪个窗口好吃？</text>
							<text class="ai-quick-btn" @click="askAi('怎么充值校园卡？')">💳 怎么充值校园卡？</text>
							<text class="ai-quick-btn" @click="askAi('快递在哪里取？')">📦 快递在哪里取？</text>
						</view>
					</view>
					<!-- 消息列表：根据 role 区分左右对齐（user=右，bot=左） -->
					<view v-for="(msg, i) in aiMessages" :key="i" :class="['ai-msg', msg.role === 'user' ? 'ai-user' : 'ai-bot']">
						<text class="ai-msg-text">{{ msg.content }}</text>
					</view>
					<!-- 加载中：显示 typing 动画 -->
					<view v-if="aiLoading" class="ai-msg ai-bot">
						<text class="ai-msg-text ai-typing">小墙正在思考...</text>
					</view>
				</scroll-view>
				<!-- 底部输入栏 -->
				<view class="ai-input-row">
					<input class="ai-input" v-model="aiInput" placeholder="问小墙任何校园问题..." confirm-type="send" @confirm="sendAiMessage" />
					<button class="ai-send-btn" :disabled="!aiInput.trim() || aiLoading" @click="sendAiMessage">发送</button>
				</view>
			</view>
			<!-- 悬浮按钮：点击切换聊天面板开关 -->
			<view class="ai-float-btn" @click="aiPanelOpen = !aiPanelOpen">
				<text class="ai-float-icon">{{ aiPanelOpen ? '×' : '🤖' }}</text>
			</view>
		</view>
	</view>
</template>

<script setup>
	import {
		ref,
		computed,
		nextTick,
		onMounted,
		onBeforeUnmount,
		watch
	} from 'vue'
	import {
		onBackPress,
		onShow
	} from '@dcloudio/uni-app'
	import request from '@/utils/request.js'
	import { getCurrentUser } from '@/utils/auth.js'
	import { uploadFile } from '@/utils/upload.js'
	import ImagePreview from '@/components/ImagePreview/ImagePreview.vue'

	// 搜索
	const isSearchFocused = ref(false)
	const keyword = ref('')
	const parentCategoryList = ref([])
	const selectedParentIds = ref([])
	const allSubList = ref([])
	const selectedSubIds = ref([])
	const showFilter = ref(false)
	const isNavigating = ref(false)

	const filterAnonymous = ref(0)

	const postList = ref([])
	const pageNum = ref(1)
	const pageSize = 10
	const loading = ref(false)
	const refreshing = ref(false)
	const noMore = ref(false)
	let loadMoreLock = false

	const previewVisible = ref(false)
	const previewUrls = ref([])
	const previewIndex = ref(0)

	const commentPanelVisible = ref(false)
	const currentCommentPost = ref(null)
	const commentList = ref([])
	const commentTree = ref([])
	const commentLoading = ref(false)
	const submittingComment = ref(false)
	const commentContent = ref('')
	const commentTempImage = ref('')
	const commentMediaUrls = ref('')
	const replyTarget = ref(null)
	const commentSort = ref('hot')
	const showAllReplies = ref({})
	const showEmojiPanel = ref(false)
	const inputFocused = ref(false)
	const replyShowCount = 0
const firstReplyShowCount = 3

	// ========== AI 助手状态变量 ==========
	// 聊天面板是否打开
	const aiPanelOpen = ref(false)
	// 输入框内容（双向绑定）
	const aiInput = ref('')
	// 消息列表：[{ role: 'user'|'assistant', content: '...' }]
	const aiMessages = ref([])
	// 是否正在等待 AI 回复
	const aiLoading = ref(false)
	// 滚动位置：初始大值确保一打开就在底部，后续每次 +1 触发 scroll-view 重渲染
	const aiScrollTop = ref(999999)
	const currentUserUid = computed(() => {
		const user = getCurrentUser()
		return user?.uid || ''
	})
	const currentUserAvatar = computed(() => {
		const user = getCurrentUser()
		return user?.avatar || '/static/default-avatar.png'
	})

	const singleVideoVisible = ref(false)
	const singleVideoUrl = ref('')

	const categoryVisible = ref(true)
	let lastScrollTop = 0

	const categoryRow1 = ref([])
	const categoryRow2 = ref([])


	const listTopPadding = ref('190rpx')




	const fetchParentCategory = async () => {
		try {
			const res = await request({
				url: '/wall/category/list',
				method: 'GET'
			})
			if (res.code === 1) {
				const all = res.data || []
				parentCategoryList.value = all
				const half = Math.ceil(all.length / 2) + 1
				categoryRow1.value = all.slice(0, half)
				categoryRow2.value = all.slice(half)
			}
		} catch (e) {
			console.error(e)
		}
	}

	const fetchAllSub = async () => {
		try {
			const res = await request({
				url: '/wall/sub-category/list',
				method: 'GET'
			})
			if (res.code === 1) allSubList.value = res.data || []
		} catch (e) {
			console.error(e)
		}
	}

	const getSubByParent = (parentId) => allSubList.value.filter(s => s.categoryId === parentId)

	const toggleParentFilter = (id) => {
		//娓呯┖搜索妗?		keyword.value = ''
		//全部甯栧瓙
		if (id === null) {
			selectedParentIds.value = []
			selectedSubIds.value = []
			filterAnonymous.value = 0
		} // 1. 判断点击的是全部帖子
		else if (id === -1) {
			selectedParentIds.value = []
			selectedSubIds.value = []
			filterAnonymous.value = 1
		} else {
			filterAnonymous.value = 0

			// 2. 判断当前点击的分类是否已经选中
			if (selectedParentIds.value.includes(id)) {
				selectedParentIds.value = []
			}
			// 如果未选中 -> 选中当前这个分类
			else {
				selectedParentIds.value = [id]
			}

			// 3. 点一级分类，清空所有二级分类选中
			selectedSubIds.value = []
		}


		pageNum.value = 1
		postList.value = []
		noMore.value = false
		fetchPosts()
	}

	const toggleSubFilter = (id) => {
		const idx = selectedSubIds.value.indexOf(id)
		if (idx === -1) selectedSubIds.value.push(id)
		else selectedSubIds.value.splice(idx, 1)
	}

	const resetFilter = () => {
		selectedParentIds.value = []
		selectedSubIds.value = []
	}

	const confirmFilter = () => {
		showFilter.value = false
			filterAnonymous.value = 0
		const bindParentIds = []
		selectedSubIds.value.forEach(subId => {
			const subItem = allSubList.value.find(item => item.id === subId)
			if (subItem) bindParentIds.push(subItem.categoryId)
		})
		selectedParentIds.value = [...new Set(bindParentIds)]
		pageNum.value = 1
		postList.value = []
		noMore.value = false
		fetchPosts()
	}

// 触发搜索事件
const onSearch = () => {
	// 重置到第一页
	// 清空原有帖子列表
	postList.value = []
	selectedParentIds.value = []
	selectedSubIds.value = []
	// 取消没有更多数据状态
	// 重新请求搜索结果
	fetchPosts()
}


	const fetchPosts = async () => {
		if (loading.value || noMore.value) return
		loading.value = true
		try {
			const params = {
				pageNum: pageNum.value,
				pageSize: pageSize,
				keyword: keyword.value || undefined,
				categoryIdList: undefined,
				categoryGroup: undefined,
				isAnonymous: filterAnonymous.value === 1 ? 1 : undefined,
				currentUserUid: getCurrentUser()?.uid || undefined
			}
			if (selectedParentIds.value.length > 0 && selectedSubIds.value.length === 0) {
				params.categoryIdList = selectedParentIds.value
			}
			if (selectedSubIds.value.length > 0) {
				const groupArr = []
				selectedSubIds.value.forEach(subId => {
					const subItem = allSubList.value.find(item => item.id === subId)
					if (subItem) groupArr.push(`${subItem.categoryId}-${subId}`)
				})
				params.categoryGroup = groupArr.join(',')
			}
			console.log(params);
			const res = await request({
				url: '/wall/post/list',
				method: 'GET',
				data: params
			})
			if (res.code === 1) {
				const list = (res.data.list || []).filter(item => item)
				list.forEach(item => {
					item.isFollow = false
					item.isCollect = !!item.isCollect
					item.isLike = !!item.isLike
					item.postNo = item.postNo || ''
					if (item.hotComments) item.hotComments.forEach(c => c.isLike = false)
				})
				postList.value = pageNum.value === 1 ? list : postList.value.concat(list)
				noMore.value = !res.data.hasNextPage || list.length < pageSize
			}
		} catch (e) {
			uni.showToast({
				title: '加载失败',
				icon: 'none'
			})
		} finally {
			loading.value = false
		}
	}

	const refreshPosts = () => {
		pageNum.value = 1
		noMore.value = false
		postList.value = []
		return fetchPosts()
	}

	const onRefresh = async () => {
		refreshing.value = true
		try {
			await refreshPosts()
		} finally {
			refreshing.value = false
		}
	}

	const isImageUrl = (url) => {
		const ext = url?.split('.').pop().toLowerCase()
		return ['jpg', 'jpeg', 'png', 'gif', 'webp', 'bmp', 'heic'].includes(ext)
	}
	const isVideoUrl = (url) => {
		const ext = url?.split('.').pop().toLowerCase()
		return ['mp4', 'mov', 'avi', 'mkv'].includes(ext)
	}

	const getVideoThumb = (url) => {
		if (url && url.includes('aliyuncs.com')) return url + '?x-oss-process=video/snapshot,t_0,f_jpg'
		return '/static/wall/video-placeholder.png'
	}

	// 预览分流：视频独立，图片走swiper
	const previewMedia = (urls, index) => {
		const url = urls[index]
		if (isVideoUrl(url)) {
			// 保存当前滚动位置
			const scrollView = document.querySelector('.wall-page')
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
			// 鍙彇鍥剧墖URL
			const imageUrls = urls.filter(u => isImageUrl(u))
			const imageIndex = imageUrls.indexOf(url)
			previewUrls.value = imageUrls
			previewIndex.value = imageIndex >= 0 ? imageIndex : 0
			previewVisible.value = true
		}
	}

	const closePreview = () => {
		previewVisible.value = false
		previewUrls.value = []
		previewIndex.value = 0
		singleVideoVisible.value = false
		singleVideoUrl.value = ''
		document.body.classList.remove('preview-lock')
		// 鎭㈠婊氬姩浣嶇疆
		nextTick(() => {
			const scrollView = document.querySelector('.wall-page')
			if (scrollView && savedScrollTop > 0) {
				scrollView.scrollTop = savedScrollTop
			}
		})
	}

	// 淇濆瓨婊氬姩浣嶇疆
	let savedScrollTop = 0

	// H5 popstate 鐩戝惉
	const handlePopState = (e) => {
		e.preventDefault()
		e.stopPropagation()
		if (singleVideoVisible.value) {
			closePreview()
		}
	}

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

	const playAudio = (url) => {
		const audio = uni.createInnerAudioContext()
		audio.src = url
		audio.play()
	}

	// 监听分类显示自动切换
	watch(categoryVisible, (val) => {
		listTopPadding.value = val ? '190rpx' : '0rpx'
	})

// 滚动监听：带动分类栏显示/隐藏 + 列表到底加载更多
const onScroll = (e) => {
  const { scrollTop, scrollHeight } = e.detail
  const remain = scrollHeight - scrollTop

  const delta = scrollTop - lastScrollTop

  // 如果帖子列表为空，始终显示分类栏，且不触发隐藏逻辑
  if (postList.value.length === 0) {
    categoryVisible.value = true
    lastScrollTop = scrollTop
    return
  }

  const minScrollableHeight = 1200
  if (scrollHeight > minScrollableHeight) {
    if ((scrollTop > 50 && delta > 20) || remain < 800) {
      categoryVisible.value = false
    } else if (delta < -5) {
      categoryVisible.value = true
    }
  } else {
    // 内容不足以滚动时，强制显示分类栏
    categoryVisible.value = true
  }

  lastScrollTop = scrollTop

  // 加载更多逻辑保持不变...
  if (loading.value || noMore.value || loadMoreLock) return
  if (remain < 1000) {
    loadMoreLock = true
    pageNum.value++
    fetchPosts().finally(() => {
      loadMoreLock = false
    })
  }
}

	const loadMore = () => {
		if (loading.value || noMore.value || loadMoreLock) return
		loadMoreLock = true
		pageNum.value++
		fetchPosts().finally(() => {
			loadMoreLock = false
		})
	}

	const ensureLogin = () => {
		const user = getCurrentUser()
		if (!user || !user.uid) {
			if (!isNavigating.value) {
				isNavigating.value = true
				uni.navigateTo({ url: '/pages/login/login' })
			}
			return null
		}
		return user
	}

	const toggleLike = async (post) => {
		if (!ensureLogin() || !post.postNo) return
		const next = !post.isLike
		post.isLike = next
		post.likeCount = Math.max((post.likeCount || 0) + (next ? 1 : -1), 0)
		try {
			const res = await request({
				url: `/wall/post/like/${post.postNo}?userUid=${encodeURIComponent(getCurrentUser().uid)}&liked=${next}`,
				method: 'PUT'
			})
			if (res.code !== 1) throw new Error(res.msg || '点赞失败')
		} catch (e) {
			post.isLike = !next
			post.likeCount = Math.max((post.likeCount || 0) + (next ? -1 : 1), 0)
		}
	}
	const toggleCollect = async (post) => {
		if (!ensureLogin() || !post.postNo) return
		const next = !post.isCollect
		post.isCollect = next
		post.collectCount = Math.max((post.collectCount || 0) + (next ? 1 : -1), 0)
		try {
			const res = await request({
				url: `/wall/post/collect/${post.postNo}?userUid=${encodeURIComponent(getCurrentUser().uid)}&collected=${next}`,
				method: 'PUT'
			})
			if (res.code !== 1) throw new Error(res.msg || '收藏失败')
		} catch (e) {
			post.isCollect = !next
			post.collectCount = Math.max((post.collectCount || 0) + (next ? -1 : 1), 0)
		}
	}
	const toggleCommentLike = async (com) => {
		const user = ensureLogin()
		if (!user || !com.id) return
		
		// 乐观更新UI
		const wasLiked = com.isLike || false
		const oldCount = com.likeCount || 0
		com.isLike = !wasLiked
		com.likeCount = oldCount + (com.isLike ? 1 : -1)
		
		try {
			const res = await request({
				url: `/wall/comment/like/${com.id}?userUid=${encodeURIComponent(user.uid)}&liked=${com.isLike}`,
				method: 'PUT'
			})
			if (res.code !== 1) {
				// 失败时回滚
				com.isLike = wasLiked
				com.likeCount = oldCount
				uni.showToast({ title: res.msg || '操作失败', icon: 'none' })
			}
		} catch (e) {
			// 网络错误时回滚
			com.isLike = wasLiked
			com.likeCount = oldCount
			uni.showToast({ title: '操作失败', icon: 'none' })
		}
	}
	const toggleFollow = async (post) => {
		if (!ensureLogin() || !post.postNo) return
		const next = !post.isFollow
		post.isFollow = next
		try {
			const res = await request({
				url: `/wall/post/follow/${post.postNo}?userUid=${encodeURIComponent(getCurrentUser().uid)}&followed=${next}`,
				method: 'PUT'
			})
			if (res.code !== 1) throw new Error(res.msg || '操作失败')
		} catch (e) {
			post.isFollow = !next
		}
		uni.showToast({
			title: post.isFollow ? '已关注' : '已取消关注',
			icon: 'none'
		})
	}

	const openCommentPanel = async (post, expandCommentId = null) => {
		const user = ensureLogin()
		if (!user || !post.postNo) return
		currentCommentPost.value = post
		commentPanelVisible.value = true
		commentContent.value = ''
		commentSort.value = 'hot'
		showAllReplies.value = {}
		showEmojiPanel.value = false
		replyTarget.value = null
		await request({
			url: `/wall/post/view/${post.postNo}?userUid=${encodeURIComponent(user.uid)}`,
			method: 'PUT'
		}).then(res => {
			if (res && res.code === 1 && res.data === true) post.viewCount = (post.viewCount || 0) + 1
		}).catch(() => {})
		await fetchComments(post)
		// 等待排序完成后再设置展开状态
		await nextTick()
		if (expandCommentId) {
			showAllReplies.value = { [expandCommentId]: true }
		}
	}

	const closeCommentPanel = () => {
		commentPanelVisible.value = false
		currentCommentPost.value = null
		commentList.value = []
		commentTree.value = []
		sortedComments.value = []
		commentContent.value = ''
		commentTempImage.value = ''
		commentMediaUrls.value = ''
		replyTarget.value = null
		showAllReplies.value = {}
		showEmojiPanel.value = false
		commentSort.value = 'hot'
	}

	const buildCommentTree = (list) => {
		const roots = []
		const map = new Map()
		list.forEach(item => {
			item.children = []
			map.set(item.id, item)
		})
		list.forEach(item => {
			if (item.parentId && item.parentId !== 0 && map.has(item.parentId)) {
				map.get(item.parentId).children.push(item)
			} else {
				roots.push(item)
			}
		})
		return roots
	}

	const sortedComments = ref([])

	const sortComments = (list) => {
		if (!list || list.length === 0) return []
		
		// 按点赞数排序所有评论
		const sortedByLike = [...list].sort((a, b) => (b.likeCount || 0) - (a.likeCount || 0))
		
		if (sortedByLike.length === 1) {
			return sortedByLike
		}
		
		// 第1条：点赞数最高
		const hottest = sortedByLike[0]
		
		// 第2条：最新发布（从剩余评论中找）
		const remaining = sortedByLike.slice(1)
		const latest = [...remaining].sort((a, b) => new Date(b.createTime || 0) - new Date(a.createTime || 0))[0]
		
		// 其余：按点赞数排名
		const rest = remaining.filter(item => item.id !== latest.id)
		
		return [hottest, latest, ...rest]
	}

	const getDisplayReplies = (item, index) => {
		if (!item.children) return []
		// 第一条一级评论默认显示前5条二级评论
		if (index === 0) return showAllReplies.value[item.id] ? item.children : item.children.slice(0, firstReplyShowCount)
		// 其他一级评论需要点击展开才显示
		if (showAllReplies.value[item.id]) return item.children
		return item.children.slice(0, replyShowCount)
	}

	const expandReplies = (id) => {
		showAllReplies.value = { ...showAllReplies.value, [id]: true }
	}

	const collapseReplies = (id) => {
		showAllReplies.value = { ...showAllReplies.value, [id]: false }
	}

	// 获取点赞数最高的三条热门评论（一级评论）
	const getTopThreeHotComments = (hotComments) => {
		if (!hotComments || hotComments.length === 0) return []
		// 后端已经按点赞数排序并返回前3条，这里做兜底保证
		return hotComments.slice(0, 3)
	}

	const relativeTime = (timeStr) => {
		if (!timeStr) return ''
		const date = new Date(timeStr)
		if (isNaN(date.getTime())) return timeStr
		const now = new Date()
		const diff = now - date
		const seconds = Math.floor(diff / 1000)
		const minutes = Math.floor(seconds / 60)
		const hours = Math.floor(minutes / 60)
		const days = Math.floor(hours / 24)
		if (seconds < 60) return '刚刚'
		if (minutes < 60) return `${minutes}分钟前`
		if (hours < 24) return `${hours}小时前`
		if (days === 1) return '昨天'
		if (days < 7) return `${days}天前`
		const month = date.getMonth() + 1
		const day = date.getDate()
		return `${month}-${day}`
	}

	const quickEmojis = ['😀','😂','😍','😎','😢','😡','👍','👎','🎉','💪','🔥','❤️','🙏','🤔','😅','🤣','😊','💔','✨','🌟']

	const commentPlaceholder = computed(() => {
		return replyTarget.value ? `回复 ${replyTarget.value.nickname || '同学'}：` : '说点什么...'
	})

	const replyToComment = (comment, root = null) => {
		if (!comment) return
		replyTarget.value = {
			id: comment.id,
			rootId: root?.id || comment.parentId || comment.id,
			userUid: comment.userUid || '',
			nickname: comment.nickname || '同学'
		}
		if (root) {
			showAllReplies.value = { ...showAllReplies.value, [root.id]: true }
		}
	}

	const cancelReply = () => {
		replyTarget.value = null
		commentContent.value = ''
	}

	const deleteComment = async (item) => {
		if (!item || !item.id) return
		const user = ensureLogin()
		if (!user) return
		uni.showModal({
			title: '删除评论',
			content: '确定要删除这条评论吗？',
			success: async (res) => {
				if (res.confirm) {
					try {
						const result = await request({
							url: `/wall/comment/delete/${item.id}?userUid=${encodeURIComponent(user.uid)}`,
							method: 'DELETE'
						})
						if (result.code === 1) {
							uni.showToast({ title: '已删除', icon: 'none' })
							const post = currentCommentPost.value
							if (post) {
								post.commentCount = Math.max(0, (post.commentCount || 0) - 1)
								await fetchComments(post)
							}
						} else {
							uni.showToast({ title: result.msg || '删除失败', icon: 'none' })
						}
					} catch (e) {
						uni.showToast({ title: '删除失败', icon: 'none' })
					}
				}
			}
		})
	}

	const appendEmoji = (emoji) => {
		commentContent.value += emoji
	}

	const chooseCommentImage = () => {
		uni.chooseImage({
			count: 1,
			sizeType: ['compressed'],
			sourceType: ['album', 'camera'],
			success: (res) => {
				const filePath = res.tempFilePaths && res.tempFilePaths[0]
				if (filePath) {
					commentTempImage.value = filePath
					commentMediaUrls.value = ''
				}
			}
		})
	}

	const recordVoicePlaceholder = () => {
		uni.showModal({
			title: '语音评论',
			content: '当前先提交一条语音占位消息；接入录音上传后会自动保存真实语音地址。',
			success: (res) => {
				if (res.confirm) submitComment('[voice]local-voice')
			}
		})
	}

	const renderCommentText = (reply) => {
		if (reply.mediaUrls) return '[图片]'
		return reply.content || ''
	}

	const fetchComments = async (post) => {
		if (!post || !post.postNo) return
		commentLoading.value = true
		try {
			const res = await request({
				url: `/wall/comment/listByPostNo/${post.postNo}`,
				method: 'GET'
			})
			if (res.code === 1) {
				commentList.value = (res.data || []).map(c => {
					c.isLike = c.isLike || false
					c.likeCount = c.likeCount || 0
					return c
				})
				commentTree.value = buildCommentTree(commentList.value)
				sortedComments.value = sortComments(commentTree.value)
			}
		} finally {
			commentLoading.value = false
		}
	}

	const submitComment = async (presetContent = '') => {
		const user = ensureLogin()
		const post = currentCommentPost.value
		const content = (presetContent || commentContent.value).trim()
		if (!user || !post || !post.postNo || submittingComment.value) return
		if (!content && !commentMediaUrls.value && !commentTempImage.value) {
			uni.showToast({ title: '先写点内容或上传图片吧', icon: 'none' })
			return
		}
		submittingComment.value = true
		try {
			let imageUrl = commentMediaUrls.value || ''
			if (commentTempImage.value) {
				uni.showLoading({ title: '上传图片中...' })
				try {
					imageUrl = await uploadFile(commentTempImage.value, 'wall/comment')
				} catch (e) {
					uni.hideLoading()
					uni.showToast({ title: '图片上传失败', icon: 'none' })
					return
				}
				uni.hideLoading()
			}
			const res = await request({
				url: `/wall/comment/addByPostNo/${post.postNo}`,
				method: 'POST',
				data: {
					userUid: user.uid,
					content: content || '',
					mediaUrls: imageUrl || null,
					parentId: replyTarget.value?.rootId || 0,
					replyCommentId: replyTarget.value?.id || 0,
					replyUserUid: replyTarget.value?.userUid || '',
					replyNickname: replyTarget.value?.nickname || '',
					isAnonymous: 0
				}
			})
			if (res.code === 1) {
				commentContent.value = ''
				commentTempImage.value = ''
				commentMediaUrls.value = ''
				replyTarget.value = null
				post.commentCount = (post.commentCount || 0) + 1
				await fetchComments(post)
			} else {
				uni.showToast({ title: res.msg || '评论失败', icon: 'none' })
			}
		} catch (e) {
			uni.showToast({ title: '评论失败', icon: 'none' })
		} finally {
			submittingComment.value = false
		}
	}

	/**
	 * 快捷提问：预填问题并立即发送
	 * 用于欢迎页的快捷问题按钮
	 */
	const askAi = (question) => {
		aiInput.value = question
		sendAiMessage()
	}

	/**
	 * 发送消息给 AI
	 * 流程：
	 * 1. 用户消息加入消息列表（右侧蓝泡泡）
	 * 2. 调用后端 POST /ai/chat
	 * 3. AI 回复加入消息列表（左侧灰泡泡）
	 */
	const sendAiMessage = async () => {
		const text = aiInput.value.trim()
		if (!text || aiLoading.value) return
		// 添加用户消息
		aiMessages.value.push({ role: 'user', content: text })
		aiInput.value = ''
		aiScrollToBottom()
		aiLoading.value = true
		try {
			// 调用后端 AI 接口
			const res = await request({
				url: '/ai/chat',
				method: 'POST',
				data: { message: text }
			})
			if (res.code === 1) {
				// 成功：添加 AI 回复
				aiMessages.value.push({ role: 'assistant', content: res.data })
			} else {
				// 失败：显示错误提示
				aiMessages.value.push({ role: 'assistant', content: '抱歉，小墙暂时无法回答，请稍后再试~' })
			}
		} catch (e) {
			// 网络异常：显示错误提示
			aiMessages.value.push({ role: 'assistant', content: '网络连接失败，请检查网络后重试~' })
		} finally {
			aiLoading.value = false
			aiScrollToBottom()
		}
	}

	/**
	 * 滚动到底部
	 * 原理：设置 scroll-top 为一个很大的值，超出内容高度时自动停在底部。
	 * 初始值 999999 确保一打开就在底部，后续每次 +1 触发重新渲染。
	 */
	const aiScrollToBottom = () => {
		// 用 nextTick 确保 DOM 更新后再设置滚动位置
		nextTick(() => {
			aiScrollTop.value = aiScrollTop.value + 1
		})
	}

	onMounted(() => {
		fetchParentCategory()
		fetchAllSub()
		fetchPosts()
		// #ifdef H5
		window.addEventListener('popstate', handlePopState)
		// #endif
	})

	onShow(() => {
		// 从发布页返回时刷新第一页，保证置顶和最新帖子立即出现在顶部。
		if (postList.value.length > 0) refreshPosts()
	})

	onBeforeUnmount(() => {
		// #ifdef H5
		window.removeEventListener('popstate', handlePopState)
		// #endif
	})

	onBackPress(backPressHandler)

	const goDetail = (post) => {
		openCommentPanel(post)
	}
	const sharePost = (post) => {
		const base = typeof location !== 'undefined' ? `${location.origin}${location.pathname}` : ''
		const link = `${base}#/pages/wall/wall?postNo=${encodeURIComponent(post.postNo || '')}`
		// #ifdef H5
		navigator.clipboard?.writeText(link)
		// #endif
		uni.showToast({ title: '帖子链接已复制', icon: 'none' })
	}
	const goPublish = () => {
		uni.navigateTo({
			url: '/pages/wall/publish'
		})
	}
	const goUserPage = (post) => {
		if (!post || !post.userUid) {
			uni.showToast({ title: '用户信息获取中', icon: 'none' })
			return
		}
		const url = `/pages/user/home?userId=${encodeURIComponent(post.userUid)}&name=${encodeURIComponent(post.nickname || '用户')}&avatar=${encodeURIComponent(post.avatar || '')}`
		uni.navigateTo({ url })
	}
	const reportPost = (post) => {
		uni.showToast({
			title: '举报鍔熻兘寮€鍙戜腑',
			icon: 'none'
		})
	}
	const formatTime = (timeStr) => {
		return timeStr ? timeStr.replace('T', ' ') : ''
	}
</script>

<style scoped>
	.wall-page {
		background: #f5f6f8;
		height: 100vh;
		display: flex;
		flex-direction: column;
		position: relative;
		padding-top: var(--status-bar-height);
	}

	.header {
		display: flex;
		align-items: center;
		padding: 20rpx;
		background: #fff;
	}

	/* 鏍囬锛氬厑璁告敹缂╁苟鏄剧ず鐪佺暐鍙?*/
	.title {
		font-size: 34rpx;
		font-weight: bold;
		white-space: nowrap;
		overflow: hidden;
		padding-left: 50rpx;
		text-overflow: ellipsis;
		flex-shrink: 1;
		/* 绌洪棿涓嶈冻鏃惰嚜鍔ㄧ缉鐭?*/
		min-width: 120rpx;
		/* 鑷冲皯淇濈暀 120rpx 瀹藉害 */
		margin-right: 20rpx;
	}

	/* 搜索妗嗭細鍙充晶涓嶅姩锛屽悜宸︽墿灞?*/
	.search-box {
		display: flex;
		align-items: center;
		background: #f5f5f5;
		border-radius: 30rpx;
		padding: 0 16rpx;
		width: 300rpx;
		/* 初始宽度 */
		transition: width 0.3s ease;
		flex-shrink: 0;
		/* 宽度可变，但不被压缩 */
		margin-left: auto;
		/* 关键：始终靠右对齐，右侧固定 */
	}

	/* 展开状态 */
	.search-expanded {
		width: 400rpx;
		/* 宽度变为更大 */
	}

	.search-input {
		flex: 1;
		height: 60rpx;
		font-size: 26rpx;
		background: transparent;
	}

	.search-icon {
		font-size: 32rpx;
		margin-left: 10rpx;
		flex-shrink: 0;
	}

	/* 分类栏：相对定位，从底部收起/展开 */
	.top-bar {
		position: absolute;
		top: 100rpx;
		left: 0;
		right: 0;
		background: #fff;
		display: flex;
		align-items: center;
		z-index: 100;
		transition: transform 0.3s ease, opacity 0.3s ease;

		transform-origin: top center;
	}

	.top-bar-hidden {
		transform: scaleY(0);
		opacity: 0;
		pointer-events: none;
	}

	.category-rows {
		flex: 1;
		display: flex;
		flex-direction: column;
		gap: 5rpx;
		justify-content: space-evenly;
		width: 100%;
		height: 100%;
		padding: 0 24rpx;
		z-index: 1;
	}

	.category-scroll {
		width: 100%;
		height: 80rpx;
		white-space: nowrap;
	}

	.category-list {
		display: flex;
		flex-direction: row;
		gap: 8rpx;
		width: fit-content;
		height: 100%;
		align-items: center;
		padding-right: 110rpx;
	}

	.cate-item {
		padding: 14rpx 36rpx;
		border-radius: 16rpx;
		font-size: 26rpx;
		color: #666;
		background: #f5f5f5;
		flex-shrink: 0;
	}

	.cate-item.active {
		background: #007aff;
		color: #fff;
	}

	.filter-btn-wrapper {
		position: absolute;
		right: 0;
		top: 0;
		bottom: 0;
		display: flex;
		align-items: center;
		z-index: 10;
		pointer-events: none;
	}

	.gradient-mask {
		width: 30rpx;
		height: 100%;
		background: linear-gradient(to left, rgba(255, 255, 255, 1), rgba(255, 255, 255, 0));
		pointer-events: none;
		border-radius: 0 16rpx 0 16rpx;
	}

	.filter-btn {
		background: #f0f0f0;
		border-radius: 16rpx 0 0 16rpx;
		height: 94%;
		display: flex;
		align-items: center;
		padding: 0 24rpx;
		gap: 6rpx;
		font-size: 26rpx;
		color: #666;
		margin-right: 0;
		white-space: nowrap;
		flex-shrink: 0;
		pointer-events: auto;
	}

	/* 帖子列表：padding-top 带动画，和分类栏同步变化 */
	.post-list {
		flex: 1;
		/* 只留左右下，top 交给动态绑定 */
		padding: 0 24rpx 100rpx;
		box-sizing: border-box;
		background-color: #fff;
		overflow-y: auto;
		transition: padding-top 0.3s ease;
	}



	.post-card {
		width: 100%;
		background: #fff;
		border-radius: 20rpx;
		padding: 24rpx;
		box-sizing: border-box;
		margin: 0 auto 20rpx auto;
		display: block;
		border-top: 2rpx solid #e0e0e0;
		border-left: 4rpx solid #e0e0e0;
		border-right: 4rpx solid #e0e0e0;
		border-bottom: 2rpx solid #e0e0e0;
		opacity: 0;
		transform: scale(0.95);
		animation: cardEnter 0.4s cubic-bezier(0.25, 0.46, 0.45, 0.94) forwards;
	}

	.post-card:last-child {
		margin-bottom: 0;
	}

	@keyframes cardEnter {
		0% {
			opacity: 0;
			transform: scale(0.95);
		}

		60% {
			transform: scale(1.02);
		}

		100% {
			opacity: 1;
			transform: scale(1);
		}
	}

	.card-header {
		display: flex;
		align-items: center;
		margin-bottom: 16rpx;
	}

	.avatar-wrapper {
		margin-right: 16rpx;
	}

	.avatar {
		width: 72rpx;
		height: 72rpx;
		border-radius: 50%;
	}

	.user-info {
		flex: 1;
	}

	.nickname {
		font-size: 28rpx;
		font-weight: 600;
		color: #333;
		display: block;
	}

	.create-time {
		font-size: 22rpx;
		color: #999;
		margin-top: 4rpx;
	}

	.header-actions {
		display: flex;
		align-items: center;
		gap: 12rpx;
	}

	.top-tag {
		background: #ff9500;
		color: #fff;
		font-size: 20rpx;
		padding: 2rpx 12rpx;
		border-radius: 8rpx;
	}

	.anonymous-tag {
		background: #ccc;
		color: #fff;
		font-size: 20rpx;
		padding: 2rpx 12rpx;
		border-radius: 8rpx;
	}

	.follow-btn,
	.report-btn {
		font-size: 22rpx;
		color: #007aff;
		padding: 2rpx 12rpx;
		background-color: transparent;
		border-radius: 20rpx;
		border: 1rpx solid #007aff;
	}

	.content-text {
		font-size: 28rpx;
		color: #333;
		line-height: 1.6;
		margin-bottom: 16rpx;
		word-break: break-all;
		
		  /* 限制最多显示5行，超出显示省略号 */
		  display: -webkit-box;
		  -webkit-line-clamp: 5;
		  -webkit-box-orient: vertical;
		  overflow: hidden;
		  text-overflow: ellipsis;
	}

	/* 濯掍綋鏍峰紡 */
	.media-container {
		margin-top: 16rpx;
		margin-bottom: 16rpx;
	}

	.media-single {
		width: 100%;
		display: flex;
		justify-content: center;
	}

	.single-image {
		max-width: 100%;
		height: auto;
		border-radius: 12rpx;
	}

	.single-video-wrapper {
		width: 100%;
		position: relative;
		border-radius: 12rpx;
		overflow: hidden;
	}

	.single-video {
		width: 100%;
		height: 400rpx;
	}

	.play-icon-overlay {
		position: absolute;
		top: 50%;
		left: 50%;
		transform: translate(-50%, -50%);
		width: 80rpx;
		height: 80rpx;
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
		border-radius: 12rpx;
		padding: 20rpx;
	}

	.audio-icon {
		font-size: 40rpx;
		margin-right: 12rpx;
	}

	.audio-text {
		font-size: 26rpx;
		color: #666;
	}

	.media-grid {
		display: flex;
		flex-wrap: wrap;
		gap: 8rpx;
	}

	.grid-item {
		width: calc((100% - 16rpx) / 3);
		aspect-ratio: 1;
		overflow: hidden;
		border-radius: 8rpx;
		position: relative;
		background: #f0f0f0;
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
		width: 48rpx;
		height: 48rpx;
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

	.card-footer {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-top: 16rpx;
		padding-top: 16rpx;
		border-top: 1rpx solid #f0f0f0;
	}

	.footer-right {
		display: flex;
		gap: 24rpx;
	}

	.action-item {
		display: flex;
		align-items: center;
		font-size: 24rpx;
		color: #666;
	}

	.action-item image {
		width: 32rpx;
		height: 32rpx;
		margin-right: 6rpx;
	}

	.share-icon {
		font-size: 24rpx;
		color: #666;
	}

	.comment-preview {
		background: #f9f9f9;
		border-radius: 12rpx;
		padding: 16rpx;
		margin-top: 16rpx;
	}

	.comment-item {
		display: flex;
		align-items: flex-start;
		margin-bottom: 2rpx;
		font-size: 24rpx;
	}

	.com-nick {
		color: #25a8ff;
		font-weight: 500;
	}

	.com-text {
		flex: 1;
		color: #333;
		margin-left: 4rpx;
	}

	.com-like {
		display: flex;
		align-items: center;
		margin-left: 8rpx;
		color: #999;
	}

	/* ========== 评论区 抖音风格 ========== */
	.comment-mask {
		position: fixed;
		left: 0;
		top: 0;
		right: 0;
		bottom: 0;
		background: rgba(0, 0, 0, 0.4);
		z-index: 1200;
	}

	.comment-drawer {
		position: fixed;
		left: 0;
		right: 0;
		bottom: 0;
		height: 75vh;
		max-height: calc(100vh - env(safe-area-inset-top));
		background: #fff;
		border-radius: 24rpx 24rpx 0 0;
		z-index: 1201;
		display: flex;
		flex-direction: column;
		transform: translateY(100%);
		transition: transform 0.28s cubic-bezier(0.32, 0.72, 0, 1);
	}

	.comment-drawer.show {
		transform: translateY(0);
	}

	/* 头部 */
	.comment-header {
		height: 88rpx;
		padding: 0 28rpx;
		display: flex;
		align-items: center;
		flex-shrink: 0;
		border-bottom: 1rpx solid #f2f2f2;
		position: relative;
		z-index: 5;
		background: #fff;
	}

	.comment-count {
		font-size: 30rpx;
		font-weight: 700;
		color: #222;
		position: absolute;
		left: 50%;
		transform: translateX(-50%);
	}

	.comment-sort-tabs {
		display: flex;
		gap: 24rpx;
		margin-left: auto;
		margin-right: 48rpx;
	}

	.sort-tab {
		font-size: 26rpx;
		color: #999;
		padding: 4rpx 0;
	}

	.sort-tab.active {
		color: #222;
		font-weight: 600;
	}

	.drawer-close {
		font-size: 40rpx;
		color: #999;
		line-height: 1;
		font-weight: 300;
	}

	/* 列表 */
	.comment-list {
		flex: 1;
		padding: 0;
		box-sizing: border-box;
		overflow-y: auto;
	}

	.comment-bottom-safe {
		height: 24rpx;
	}

	.comment-item {
		display: flex;
		padding: 2rpx 10rpx;
		/* border-bottom: 1rpx solid #f7f7f7; */
	}

	.comment-avatar {
		width: 68rpx;
		height: 68rpx;
		border-radius: 50%;
		background: #f0f0f0;
		flex-shrink: 0;
		margin-right: 20rpx;
	}

	.comment-body {
		flex: 1;
		min-width: 0;
		margin-right: 16rpx;
	}

	.comment-top {
		display: flex;
		align-items: center;
		justify-content: space-between;
		margin-bottom: 8rpx;
	}

	.comment-name {
		font-size: 26rpx;
		font-weight: 600;
		color: #507daf;
		max-width: 300rpx;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.comment-time {
		font-size: 22rpx;
		color: #bbb;
		flex-shrink: 0;
	}

	.comment-text {
		font-size: 28rpx;
		color: #222;
		line-height: 1.55;
		word-break: break-all;
		margin-bottom: 10rpx;
	}

	.comment-image {
		width: 180rpx;
		height: 180rpx;
		border-radius: 8rpx;
		background: #f0f0f0;
		display: block;
	}

	.voice-bubble {
		display: inline-flex;
		align-items: center;
		min-width: 160rpx;
		height: 60rpx;
		padding: 0 20rpx;
		border-radius: 30rpx;
		background: #e8f7dc;
		color: #1f2933;
		font-size: 24rpx;
	}

	/* 右侧点赞按钮 */
	.comment-like-btn {
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: flex-start;
		padding-top: 8rpx;
		flex-shrink: 0;
		min-width: 50rpx;
	}

	.like-count {
		font-size: 20rpx;
		color: #999;
		margin-top: 4rpx;
	}

	/* 回复区域 */
	.reply-section {
		margin-top: 12rpx;
		margin-left: -8rpx;
		padding: 14rpx 18rpx;
		border-radius: 12rpx;
		background: #f8f8f8;
	}

	.reply-item {
		display: flex;
		align-items: flex-start;
		font-size: 26rpx;
		line-height: 1.7;
		color: #333;
		padding: 8rpx 0;
	}

	.reply-avatar {
		width: 48rpx;
		height: 48rpx;
		border-radius: 50%;
		background: #f0f0f0;
		flex-shrink: 0;
		margin-right: 12rpx;
	}

	.reply-content-wrapper {
		flex: 1;
		min-width: 0;
	}

	.reply-header {
		display: flex;
		align-items: center;
		flex-wrap: wrap;
		margin-bottom: 4rpx;
	}

	.reply-name {
		color: #507daf;
		font-weight: 500;
	}

	.reply-to {
		color: #666;
		font-size: 24rpx;
	}

	.reply-target {
		color: #507daf;
		font-weight: 500;
	}

	.reply-colon {
		color: #666;
	}

	.reply-text-row {
		display: flex;
		align-items: flex-start;
		justify-content: space-between;
	}

	.reply-content {
		flex: 1;
		color: #333;
		word-break: break-all;
		margin-right: 12rpx;
	}

	.reply-image {
		width: 120rpx;
		height: 120rpx;
		border-radius: 8rpx;
		margin: 6rpx 8rpx 6rpx 0;
	}

	/* 二级评论点赞按钮 */
	.reply-like-btn {
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: flex-start;
		flex-shrink: 0;
		min-width: 40rpx;
		padding-top: 4rpx;
	}

	.reply-like-count {
		font-size: 18rpx;
		color: #999;
		margin-top: 2rpx;
	}

	.expand-replies {
		padding: 10rpx 0 4rpx;
		font-size: 24rpx;
		color: #507daf;
	}

	/* 空状态 */
	.comment-empty {
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		padding: 120rpx 0;
		color: #999;
	}

	.empty-icon {
		font-size: 64rpx;
		margin-bottom: 20rpx;
	}

	.empty-text {
		font-size: 28rpx;
		color: #666;
		margin-bottom: 8rpx;
	}

	.empty-desc {
		font-size: 24rpx;
		color: #bbb;
	}

	.loading-spinner {
		display: inline-block;
		width: 40rpx;
		height: 40rpx;
		border: 3rpx solid #eee;
		border-top-color: #507daf;
		border-radius: 50%;
		animation: spin 0.8s linear infinite;
		margin-bottom: 16rpx;
	}

	@keyframes spin {
		to { transform: rotate(360deg); }
	}

	/* 输入栏 */
	.comment-input-bar {
		flex-shrink: 0;
		background: #fff;
		border-top: 1rpx solid #f2f2f2;
		position: relative;
		z-index: 10;
	}

	.reply-indicator {
		display: flex;
		align-items: center;
		justify-content: space-between;
		padding: 12rpx 28rpx;
		background: #f9f9f9;
		border-bottom: 1rpx solid #f0f0f0;
	}

	.reply-indicator-text {
		font-size: 24rpx;
		color: #507daf;
	}

	.cancel-reply {
		font-size: 32rpx;
		color: #999;
		line-height: 1;
	}

	.comment-image-preview {
		position: relative;
		display: inline-block;
		margin: 8rpx 20rpx 0;
	}

	.comment-preview-img {
		width: 120rpx;
		height: 120rpx;
		border-radius: 12rpx;
	}

	.comment-preview-remove {
		position: absolute;
		top: -10rpx;
		right: -10rpx;
		width: 36rpx;
		height: 36rpx;
		background: rgba(0, 0, 0, 0.5);
		color: #fff;
		border-radius: 50%;
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 24rpx;
	}

	.input-row {
		display: flex;
		align-items: center;
		padding: 16rpx 20rpx;
		padding-bottom: calc(16rpx + env(safe-area-inset-bottom));
		gap: 16rpx;
	}

	.input-avatar {
		width: 60rpx;
		height: 60rpx;
		border-radius: 50%;
		overflow: hidden;
		flex-shrink: 0;
		background: #f0f0f0;
	}

	.input-avatar image {
		width: 100%;
		height: 100%;
	}

	.input-box {
		flex: 1;
		min-width: 0;
	}

	.comment-input {
		width: 100%;
		height: 68rpx;
		padding: 0 24rpx;
		border-radius: 34rpx;
		background: #f5f6f8;
		font-size: 26rpx;
		box-sizing: border-box;
	}

	.emoji-btn,
	.image-btn {
		width: 56rpx;
		height: 56rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 32rpx;
		flex-shrink: 0;
	}

	.comment-submit-btn {
		height: 68rpx;
		padding: 0 32rpx;
		border-radius: 34rpx;
		background: #ff2a4b;
		color: #fff;
		font-size: 26rpx;
		font-weight: 600;
		line-height: 68rpx;
		flex-shrink: 0;
		border: none;
	}

	.comment-submit-btn[disabled] {
		background: #ffb3bf;
		color: rgba(255,255,255,0.7);
	}

	/* 表情面板 */
	.emoji-panel {
		display: flex;
		flex-wrap: wrap;
		padding: 16rpx 20rpx;
		gap: 12rpx;
		background: #f9f9f9;
		border-top: 1rpx solid #f0f0f0;
	}

	.emoji-item {
		font-size: 40rpx;
		padding: 8rpx;
	}

	.load-more,
	.empty {
		text-align: center;
		padding: 30rpx;
		color: #999;
		font-size: 24rpx;
	}

	.float-btn {
		position: fixed;
		bottom: 100rpx;
		right: 40rpx;
		width: 100rpx;
		height: 100rpx;
		background: #007aff;
		border-radius: 50%;
		display: flex;
		align-items: center;
		justify-content: center;
		box-shadow: 0 8rpx 20rpx rgba(0, 122, 255, 0.3);
		z-index: 9;
	}

	.float-btn image {
		width: 48rpx;
		height: 48rpx;
	}

	/* 绛涢€夋诞绐?*/
	.filter-mask {
		position: fixed;
		left: 0;
		top: 0;
		right: 0;
		bottom: 0;
		background: rgba(0, 0, 0, 0.4);
		z-index: 999;
	}

	.filter-drawer {
		position: fixed;
		top: 0;
		right: 0;
		width: 640rpx;
		height: 100vh;
		background: #fff;
		border-radius: 50rpx 0 0 50rpx;
		display: flex;
		flex-direction: column;
		transform: translateX(100%);
		transition: transform 0.3s ease;
		z-index: 1000;
	}

	.filter-drawer.show {
		transform: translateX(0);
	}

	.drawer-header {
		display: flex;
		justify-content: space-between;
		align-items: center;
		padding: 30rpx;
		border-bottom: 1rpx solid #f2f2f2;
		flex-shrink: 0;
	}

	.drawer-title {
		font-size: 34rpx;
		font-weight: 600;
		color: #111;
	}

	.drawer-close {
		font-size: 32rpx;
		color: #999;
	}

	.drawer-content {
		flex: 1;
		padding: 30rpx;
		box-sizing: border-box;
		overflow-y: auto;
	}

	.filter-group {
		margin-bottom: 40rpx;
	}

	.parent-name {
		font-size: 30rpx;
		color: #222;
		font-weight: 500;
		margin-bottom: 20rpx;
	}

	.child-list {
		display: flex;
		flex-wrap: wrap;
		gap: 16rpx;
	}

	.child-item {
		padding: 12rpx 26rpx;
		background: #f6f6f6;
		border-radius: 24rpx;
		font-size: 26rpx;
		color: #555;
	}

	.child-item.checked {
		background: #007aff;
		color: #fff;
	}

	.hide-tag {
		font-size: 22rpx;
		color: #aaa;
		margin-left: 6rpx;
	}

	.drawer-footer {
		display: flex;
		gap: 20rpx;
		padding: 24rpx 30rpx 40rpx;
		border-top: 1rpx solid #f2f2f2;
		flex-shrink: 0;
	}

	.reset-btn,
	.confirm-btn {
		flex: 1;
		text-align: center;
		padding: 16rpx 0;
		border-radius: 26rpx;
		font-size: 28rpx;
	}

	.reset-btn {
		background: #f5f5f5;
		color: #666;
	}

	.confirm-btn {
		background: #007aff;
		color: #fff;
	}

	/* 瑙嗛棰勮閬僵 */
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

	/* AI 校园助手 */
	.ai-assistant {
		position: fixed;
		right: 20rpx;
		bottom: 160rpx;
		z-index: 999;
	}

	.ai-float-btn {
		width: 100rpx;
		height: 100rpx;
		border-radius: 50%;
		background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
		box-shadow: 0 8rpx 24rpx rgba(102, 126, 234, 0.4);
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.ai-float-icon {
		font-size: 48rpx;
	}

	.ai-panel {
		position: fixed;
		right: 0;
		bottom: 0;
		width: 100%;
		max-width: 500rpx;
		height: 600rpx;
		background: #fff;
		border-radius: 20rpx 20rpx 0 0;
		box-shadow: 0 -4rpx 20rpx rgba(0, 0, 0, 0.1);
		display: flex;
		flex-direction: column;
		overflow: hidden;
	}

	.ai-open .ai-panel {
		right: 20rpx;
		bottom: 140rpx;
		border-radius: 20rpx;
		width: 560rpx;
		height: 700rpx;
		box-shadow: 0 4rpx 24rpx rgba(0, 0, 0, 0.15);
	}

	.ai-header {
		display: flex;
		align-items: center;
		justify-content: space-between;
		padding: 20rpx 24rpx;
		background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
	}

	.ai-title {
		font-size: 30rpx;
		font-weight: bold;
		color: #fff;
	}

	.ai-close {
		font-size: 36rpx;
		color: #fff;
		padding: 0 8rpx;
	}

	.ai-messages {
		flex: 1;
		padding: 20rpx;
		overflow-y: auto;
	}

	.ai-welcome {
		text-align: center;
		padding: 40rpx 0;
	}

	.ai-welcome-text {
		font-size: 28rpx;
		color: #666;
		display: block;
		margin-bottom: 30rpx;
	}

	.ai-quick-questions {
		display: flex;
		flex-wrap: wrap;
		justify-content: center;
		gap: 16rpx;
	}

	.ai-quick-btn {
		background: #f0f0ff;
		color: #667eea;
		font-size: 24rpx;
		padding: 12rpx 24rpx;
		border-radius: 30rpx;
	}

	.ai-msg {
		margin-bottom: 20rpx;
		display: flex;
	}

	.ai-user {
		justify-content: flex-end;
	}

	.ai-user .ai-msg-text {
		background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
		color: #fff;
		border-radius: 20rpx 20rpx 4rpx 20rpx;
	}

	.ai-bot .ai-msg-text {
		background: #f5f5f5;
		color: #333;
		border-radius: 20rpx 20rpx 20rpx 4rpx;
	}

	.ai-msg-text {
		max-width: 80%;
		padding: 16rpx 24rpx;
		font-size: 26rpx;
		line-height: 1.6;
		word-break: break-all;
	}

	.ai-typing {
		color: #999;
		font-style: italic;
	}

	.ai-input-row {
		display: flex;
		align-items: center;
		padding: 16rpx 20rpx;
		border-top: 1rpx solid #f0f0f0;
		gap: 12rpx;
	}

	.ai-input {
		flex: 1;
		height: 64rpx;
		background: #f5f5f5;
		border-radius: 32rpx;
		padding: 0 24rpx;
		font-size: 26rpx;
	}

	.ai-send-btn {
		height: 64rpx;
		line-height: 64rpx;
		padding: 0 28rpx;
		background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
		color: #fff;
		font-size: 26rpx;
		border-radius: 32rpx;
		border: none;
	}

	.ai-send-btn[disabled] {
		opacity: 0.5;
	}
</style>

<!-- 全局样式 -->
<style>
	body,
	page {
		height: 100%;
		overflow: hidden;
	}

	::-webkit-scrollbar {
		display: none !important;
		width: 0 !important;
		height: 0 !important;
		background: transparent !important;
	}
</style>