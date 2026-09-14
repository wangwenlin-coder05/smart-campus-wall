<template>
	<view class="page" v-if="item">
		<!-- 头部大图 -->
		<view class="hero">
			<image class="hero-avatar" :src="item.avatar || defaultAvatar" mode="aspectFill"></image>
			<view class="hero-overlay">
				<view class="hero-gender" :class="item.gender === 'male' ? 'gender-male' : 'gender-female'">
					<text>{{ item.gender === 'male' ? '♂' : '♀' }}</text>
				</view>
			</view>
			<view class="hero-status" :class="getStatusClass(item.status)">
				<text>{{ getStatusText(item.status) }}</text>
			</view>
		</view>

		<!-- 基本信息卡片 -->
		<view class="info-card">
			<view class="info-top">
				<text class="info-nickname">{{ item.nickname }}</text>
				<text class="info-age">{{ item.age }}岁</text>
			</view>
			<view class="info-tags">
				<text class="info-tag">{{ item.school }}</text>
				<text class="info-tag">{{ item.major }}</text>
				<text class="info-tag">{{ item.occupation }}</text>
				<text class="info-tag">身高 {{ item.height }}cm</text>
			</view>
		</view>

		<!-- 兴趣爱好 -->
		<view class="section-card" v-if="item.hobbies && item.hobbies.length">
			<text class="card-title">兴趣爱好</text>
			<view class="hobby-list">
				<text class="hobby-tag" v-for="(h, i) in item.hobbies" :key="i">{{ h }}</text>
			</view>
		</view>

		<!-- 自我介绍 -->
		<view class="section-card">
			<text class="card-title">关于我</text>
			<text class="intro-text">{{ item.selfIntro }}</text>
		</view>

		<!-- 期望对象 -->
		<view class="section-card">
			<text class="card-title">期望TA</text>
			<view class="requirement-box">
				<text class="req-text">{{ item.requirements }}</text>
			</view>
		</view>

		<!-- 操作栏 -->
		<view class="action-bar">
			<view class="like-btn" :class="{ liked: liked }" @click="toggleLike">
				<text class="like-icon">{{ liked ? '❤️' : '🤍' }}</text>
				<text class="like-count">{{ item.likeCount || 0 }}</text>
			</view>
			<view class="match-btn" v-if="item.status !== 'in_chat'" @click="handleMatch">
				<text>立即表白</text>
			</view>
			<view class="chat-btn" v-else @click="goChat">
				<text>进入聊天</text>
			</view>
		</view>

		<!-- 心动弹窗 -->
		<view class="modal-overlay" v-if="showModal" @click="showModal = false">
			<view class="match-modal" @click.stop>
				<view class="match-hearts">❤️</view>
				<text class="match-title">确认对 {{ item.nickname }} 心动？</text>
				<text class="match-sub">对方确认后即可交换联系方式，开始聊天</text>
				<view class="match-btn-full" @click="confirmMatch">
					<text>确认表白</text>
				</view>
				<view class="match-cancel" @click="showModal = false">
					<text>再想想</text>
				</view>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getCurrentUser } from '@/utils/auth.js'
import request from '@/utils/request.js'

const defaultAvatar = 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'

const item = ref(null)
const liked = ref(false)
const showModal = ref(false)
const currentUid = ref('')

const mockDetail = {
	id: 'p1',
	nickname: '小雅',
	avatar: 'https://picsum.photos/200/200?random=10',
	gender: 'female',
	age: 22,
	school: '北京大学',
	major: '金融学',
	height: 165,
	occupation: '研究生在读',
	hobbies: ['摄影', '旅行', '钢琴', '跑步'],
	selfIntro: '喜欢摄影和旅行，周末经常去探索北京的各大景点。希望找一个志同道合的男生，一起发现生活的美好。性格开朗乐观，善于沟通。研究生在读，研究方向是金融工程，平时课业比较忙但也会抽时间旅行。希望找一个有上进心、幽默风趣的男生，身高170以上。',
	requirements: '希望对方170以上，有责任心，喜欢旅行，性格开朗',
	likeCount: 128,
	status: 'in_chat'
}

const getStatusClass = (status) => {
	const map = { in_chat: 'status-chat', pending: 'status-pending', matched: 'status-matched' }
	return map[status] || 'status-pending'
}

const getStatusText = (status) => {
	const map = { in_chat: '❤️ 正在热聊', pending: '待匹配', matched: '已成功' }
	return map[status] || '待匹配'
}

const toggleLike = () => {
	if (!currentUid.value) {
		uni.navigateTo({ url: '/pages/login/login' })
		return
	}
	liked.value = !liked.value
	item.value.likeCount = (item.value.likeCount || 0) + (liked.value ? 1 : -1)
	uni.showToast({ title: liked.value ? '已心动' : '取消心动', icon: 'none' })
}

const handleMatch = () => {
	if (!currentUid.value) {
		uni.navigateTo({ url: '/pages/login/login' })
		return
	}
	showModal.value = true
}

const confirmMatch = async () => {
	try {
		const res = await request({
			url: '/partner/like',
			method: 'POST',
			data: {
				targetId: item.value.id,
				userId: currentUid.value
			}
		})
		if (res && res.code === 1) {
			uni.showToast({ title: '表白成功！', icon: 'success' })
			showModal.value = false
			item.value.status = 'in_chat'
		} else {
			uni.showToast({ title: res?.msg || '表白失败', icon: 'none' })
		}
	} catch (e) {
		uni.showToast({ title: '表白失败', icon: 'none' })
	}
}

const goChat = () => {
	uni.navigateTo({ url: `/pages/message/message?uid=${item.value.id}&nickname=${item.value.nickname}` })
}

onLoad((options) => {
	currentUid.value = getCurrentUser()?.uid || ''
	const id = options?.id
	if (id) {
		request({
			url: `/partner/detail/${id}`,
			method: 'GET'
		}).then(res => {
			if (res && res.code === 1 && res.data) {
				item.value = res.data
			} else {
				item.value = { ...mockDetail, id }
			}
		}).catch(() => {
			item.value = { ...mockDetail, id }
		})
	} else {
		item.value = mockDetail
	}
})
</script>

<script>
export default {
	onShareAppMessage() {
		return {
			title: `${this.item?.nickname}的相亲帖`,
			path: `/pages/partner/partnerDetail?id=${this.item?.id}`
		}
	}
}
</script>

<style scoped>
.page {
	background: #f5f6f9;
	min-height: 100vh;
	padding-bottom: 140rpx;
}

.hero {
	position: relative;
	height: 520rpx;
}

.hero-avatar {
	width: 100%;
	height: 100%;
}

.hero-overlay {
	position: absolute;
	bottom: 24rpx;
	left: 24rpx;
}

.hero-gender {
	width: 60rpx;
	height: 60rpx;
	border-radius: 50%;
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 32rpx;
	color: #fff;
}

.gender-male { background: #74b9ff; }
.gender-female { background: #fd79a8; }

.hero-status {
	position: absolute;
	top: calc(var(--status-bar-height) + 16rpx);
	right: 24rpx;
	padding: 8rpx 20rpx;
	border-radius: 24rpx;
	font-size: 22rpx;
	color: #fff;
}

.status-chat { background: linear-gradient(135deg, #e84393, #fd79a8); }
.status-pending { background: rgba(0, 0, 0, 0.4); }
.status-matched { background: linear-gradient(135deg, #a29bfe, #6c5ce7); }

.info-card {
	margin: -60rpx 24rpx 24rpx;
	padding: 28rpx;
	background: #fff;
	border-radius: 20rpx;
	position: relative;
	z-index: 1;
	box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.06);
}

.info-top {
	display: flex;
	align-items: center;
	gap: 16rpx;
	margin-bottom: 16rpx;
}

.info-nickname {
	font-size: 40rpx;
	font-weight: 700;
	color: #333;
}

.info-age {
	font-size: 28rpx;
	color: #666;
}

.info-tags {
	display: flex;
	flex-wrap: wrap;
	gap: 12rpx;
}

.info-tag {
	font-size: 22rpx;
	color: #666;
	background: #f5f6f9;
	padding: 6rpx 16rpx;
	border-radius: 8rpx;
}

.section-card {
	margin: 0 24rpx 24rpx;
	padding: 28rpx;
	background: #fff;
	border-radius: 20rpx;
}

.card-title {
	display: block;
	font-size: 30rpx;
	font-weight: 700;
	color: #333;
	margin-bottom: 16rpx;
}

.hobby-list {
	display: flex;
	flex-wrap: wrap;
	gap: 12rpx;
}

.hobby-tag {
	font-size: 24rpx;
	color: #e84393;
	background: #fff0f5;
	padding: 8rpx 18rpx;
	border-radius: 20rpx;
}

.intro-text {
	font-size: 28rpx;
	color: #555;
	line-height: 1.8;
}

.requirement-box {
	background: #f9f0ff;
	padding: 20rpx;
	border-radius: 12rpx;
}

.req-text {
	font-size: 28rpx;
	color: #6c5ce7;
	line-height: 1.6;
}

.action-bar {
	position: fixed;
	bottom: 0;
	left: 0;
	right: 0;
	display: flex;
	align-items: center;
	gap: 20rpx;
	padding: 20rpx 32rpx;
	padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
	background: #fff;
	box-shadow: 0 -2rpx 20rpx rgba(0, 0, 0, 0.05);
}

.like-btn {
	display: flex;
	align-items: center;
	gap: 8rpx;
	padding: 16rpx 28rpx;
	background: #fff0f5;
	border-radius: 36rpx;
}

.like-icon {
	font-size: 32rpx;
}

.like-count {
	font-size: 26rpx;
	color: #e84393;
	font-weight: 600;
}

.match-btn, .chat-btn {
	flex: 1;
	height: 88rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 30rpx;
	font-weight: 600;
	color: #fff;
	border-radius: 44rpx;
}

.match-btn {
	background: linear-gradient(135deg, #e84393, #fd79a8);
}

.chat-btn {
	background: linear-gradient(135deg, #a29bfe, #6c5ce7);
}

.modal-overlay {
	position: fixed;
	inset: 0;
	z-index: 200;
	background: rgba(0, 0, 0, 0.5);
	display: flex;
	align-items: center;
	justify-content: center;
}

.match-modal {
	width: 560rpx;
	padding: 48rpx 40rpx;
	background: #fff;
	border-radius: 24rpx;
	display: flex;
	flex-direction: column;
	align-items: center;
	gap: 16rpx;
}

.match-hearts {
	font-size: 80rpx;
	animation: heartbeat 1s ease-in-out infinite;
}

@keyframes heartbeat {
	0%, 100% { transform: scale(1); }
	50% { transform: scale(1.2); }
}

.match-title {
	font-size: 36rpx;
	font-weight: 700;
	color: #333;
}

.match-sub {
	font-size: 26rpx;
	color: #999;
	text-align: center;
	line-height: 1.5;
}

.match-btn-full {
	width: 100%;
	padding: 24rpx;
	background: linear-gradient(135deg, #e84393, #fd79a8);
	color: #fff;
	font-size: 32rpx;
	font-weight: 600;
	border-radius: 48rpx;
	text-align: center;
	margin-top: 16rpx;
}

.match-cancel {
	font-size: 26rpx;
	color: #999;
	padding: 12rpx;
}
</style>
