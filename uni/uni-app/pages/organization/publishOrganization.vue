<template>
	<view class="page">
		<scroll-view class="page-scroll" scroll-y enhanced :show-scrollbar="false">
			<view class="hero">
				<view>
					<text class="eyebrow">CREATE ACTIVITY</text>
					<text class="title">我要组局</text>
				</view>
				<text class="hero-copy">把时间、地点和玩法说清楚，找到同频的校园搭子。</text>
			</view>

			<view class="form-card">
				<view class="section-head">
					<text class="section-title">基础信息</text>
					<text class="section-sub">让大家一眼看懂</text>
				</view>

				<view class="field">
					<text class="label">组局标题</text>
					<input
						v-model.trim="form.title"
						class="input"
						maxlength="32"
						placeholder="例如：周五羽毛球双打"
						placeholder-class="placeholder"
					/>
				</view>

				<view class="field cover-category-field">
					<view class="cover-picker" @click="choosePoster">
						<image class="cover-image" :src="form.posterImg" mode="aspectFill"></image>
						<view class="cover-action">换图</view>
					</view>
					<view class="cover-side">
						<view class="date-time-row">
							<view class="field compact-field flex-1">
								<text class="label">日期</text>
								<picker mode="multiSelector" :range="dateRange" :value="dateValue" @change="onDateChange">
									<view class="picker-field">{{ formatDateDisplay() || '选择' }}</view>
								</picker>
							</view>
							<view class="field compact-field flex-1">
								<text class="label">时间</text>
								<picker mode="time" :value="form.activityClock" @change="onTimeChange">
									<view class="picker-field">{{ form.activityClock || '选择' }}</view>
								</picker>
							</view>
						</view>
						<view class="field compact-field people-field">
							<text class="label">人数上限</text>
							<view class="people-selector">
								<view
									v-for="num in presetPeople"
									:key="num"
									class="people-btn"
									:class="{ active: !isCustomPeople && form.maxPeople === num }"
									@click="selectPeople(num)"
								>
									{{ num }}
								</view>
								<view
									class="people-btn custom-btn"
									:class="{ active: isCustomPeople }"
									@click="enableCustomPeople"
								>
									自定义
								</view>
							</view>
							<input
								v-if="isCustomPeople"
								v-model.number="form.maxPeople"
								class="input compact-input custom-input"
								type="number"
								placeholder="请输入人数"
								placeholder-class="placeholder"
							/>
						</view>
					</view>
				</view>

				<view class="field">
					<text class="label">活动分类</text>
					<view class="chip-row">
						<view
							v-for="item in categories"
							:key="item"
							class="chip"
							:class="{ active: form.category === item }"
							@click="selectCategory(item)"
						>
							{{ item }}
						</view>
					</view>
				</view>

				<view class="field" v-if="subCategories.length">
					<text class="label">小分类</text>
					<view class="sub-chip-row">
						<view
							v-for="item in subCategories"
							:key="item.id"
							class="sub-chip"
							:class="{ active: form.subCategoryId === item.id }"
							@click="selectSubCategory(item)"
						>
							{{ item.name }}
						</view>
					</view>
				</view>

				<view class="field">
					<text class="label">集合地点</text>
					<input
						v-model.trim="form.address"
						class="input"
						maxlength="28"
						placeholder="例如：体育馆二楼 3 号场"
						placeholder-class="placeholder"
					/>
				</view>

				<view class="field">
					<text class="label">联系二维码</text>
					<view class="qr-picker" @click="chooseQrcode">
						<image v-if="form.qrcodeUrl" class="qr-image" :src="form.qrcodeUrl" mode="aspectFill"></image>
						<view v-else class="qr-empty">
							<text class="qr-plus">+</text>
							<text>上传报名联系二维码</text>
						</view>
						<view v-if="form.qrcodeUrl" class="qr-action">更换二维码</view>
					</view>
				</view>
			</view>

			<view class="form-card">
				<view class="section-head">
					<text class="section-title">活动规则</text>
					<text class="section-sub">降低沟通成本</text>
				</view>

				<view class="field">
					<text class="label">性别限制</text>
					<view class="seg-row">
						<view
							v-for="item in genderOptions"
							:key="item"
							class="seg"
							:class="{ active: form.genderLimit === item }"
							@click="form.genderLimit = item"
						>
							{{ item }}
						</view>
					</view>
				</view>

				<view class="field">
					<text class="label">费用方式</text>
					<view class="seg-row">
						<view
							v-for="item in feeOptions"
							:key="item"
							class="seg"
							:class="{ active: form.feeType === item }"
							@click="form.feeType = item"
						>
							{{ item }}
						</view>
					</view>
				</view>

				<view class="field switch-line" @click="toggleDeposit">
					<view>
						<text class="label no-margin">是否收保证金</text>
						<text class="hint">适合需要提前占位或预订场地的活动</text>
					</view>
					<view class="switch" :class="{ on: form.depositRequired }"></view>
				</view>

				<view class="field" v-if="form.depositRequired">
					<text class="label">保证金金额</text>
					<input
						v-model.number="form.depositAmount"
						class="input"
						type="number"
						placeholder="10"
						placeholder-class="placeholder"
					/>
				</view>
			</view>

			<view class="form-card">
				<view class="section-head">
					<text class="section-title">主理人说明</text>
					<text class="section-sub">写点真实的期待</text>
				</view>

				<view class="field">
					<textarea
						v-model.trim="form.hostDesc"
						class="textarea"
						maxlength="120"
						placeholder="例如：新手友好，准时集合，装备可以一起商量。"
						placeholder-class="placeholder"
					/>
				</view>
			</view>

			<view class="activity-card preview-activity">
				<view class="main-row">
					<view class="poster-stack">
						<image class="poster-shadow poster-back" :src="form.posterImg" mode="aspectFill"></image>
						<image class="poster" :src="form.posterImg" mode="aspectFill"></image>
						<view class="poster-watermark">{{ form.subCategoryName }}</view>
					</view>

					<view class="content">
						<view class="organizer-row">
							<image class="brand-avatar" :src="currentUserAvatar" mode="aspectFill"></image>
							<text class="brand-name">我的组局</text>
							<view class="times-pill">新发布</view>
						</view>

						<text class="activity-title">{{ form.title || '你的组局标题' }}</text>

						<view class="meta-row">
							<view class="meta-item">
								<view class="clock-icon"></view>
								<text>{{ activityTimeText || '活动时间' }}</text>
							</view>
							<view class="meta-item">
								<view class="pin-icon"></view>
								<text>{{ form.address || '集合地点' }}</text>
							</view>
						</view>

						<view class="info-chip-row">
							<view class="info-chip">1/{{ form.maxPeople || 2 }}人</view>
							<view class="info-chip">{{ form.genderLimit }}</view>
							<view class="info-chip">{{ form.feeType }}</view>
						</view>

						<view class="deposit-row">
							<text>{{ form.depositRequired ? `报名押金 ¥${form.depositAmount || 10}` : '免报名押金' }}</text>
						</view>

						<view class="bottom-row">
							<view class="join-area">
								<view class="avatar-group">
									<image class="join-avatar" :src="currentUserAvatar" mode="aspectFill"></image>
								</view>
								<text class="join-count">已有 1 人报名</text>
							</view>
							<view class="signup-btn">
								<text class="go-text">GO!</text>
								<text class="signup-text">去报名</text>
							</view>
						</view>
					</view>
				</view>

				<view class="host-note">
					<text class="host-label">主理人说：</text>
					<text class="host-text">{{ form.hostDesc || '主理人说明会展示在这里。' }}</text>
				</view>
			</view>
		</scroll-view>

		<view class="bottom-bar">
			<view class="draft-btn" @click="saveDraft">存草稿</view>
			<view class="submit-btn" :class="{ disabled: submitting }" @click="submitForm">
				{{ submitting ? '提交中...' : '发布组局' }}
			</view>
		</view>
	</view>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import request from '@/utils/request.js'
import { getCurrentUser } from '@/utils/auth.js'
import { uploadMultipleFiles } from '@/utils/upload.js'

const submitting = ref(false)
const loading = ref(false)

const currentUser = ref({
	avatar: '/static/user/default-avatar.png',
	nickname: '当前用户'
})

const defaultAvatar = '/static/default-avatar.png'

const mockBackendData = {
	categories: [{
			name: '体育运动',
			start: 1,
			tags: ['篮球', '足球', '羽毛球', '乒乓球', '排球', '网球', '台球', '飞盘', '游泳', '跑步', '骑行', '徒步', '露营', '健身', '瑜伽', '跳舞', '武术搏击', '滑板轮滑', '体育训练', '运动搭子']
		},
		{
			name: '学业备考',
			start: 21,
			tags: ['自习', '考研', '考公', '英语学习', '考证备考', '期末复习', '专业课学习', '小组作业', '论文互助', '毕设组队', '编程开发', '设计剪辑', '办公软件', '口语练习', '阅读分享', '技能学习', '学科竞赛', '创业交流', '学习监督', '学习搭子']
		},
		{
			name: '美食觅食',
			start: 41,
			tags: ['火锅', '烤肉', '烧烤', '聚餐干饭', '探店', '夜宵', '小吃', '早餐', '午饭', '晚饭', '奶茶', '咖啡', '甜品', '自助餐', '食堂搭子', '轻食减脂餐', '野餐', '节日聚餐', '拼桌约饭', '美食分享']
		},
		{
			name: '休闲玩乐',
			start: 61,
			tags: ['桌游', '剧本杀', '密室逃脱', '狼人杀', '麻将', '扑克', '线下牌局', '电竞开黑', '手游组队', '端游组队', '联机游戏', '网吧开黑', 'KTV', '看电影', 'DIY手作', '漫展', '音乐节', '演唱会', '宠物互动', '娱乐聚会']
		},
		{
			name: '社交搭伴',
			start: 81,
			tags: ['逛街', '散步', '城市漫步', '网红打卡', '书店', '图书馆', '看展', '摄影', '志愿活动', '社团活动', '球赛观战', '校园活动', '新生交友', '同专业交流', '兴趣交友', '脱单交友', '闲聊唠嗑', '周边出游', '临时组局', '找搭子']
		}
	],
	defaultPoster: '/static/organization/feature-card.jpg'
}

const fetchBackendData = () => {
	return request({
		url: '/organization/categories',
		method: 'GET'
	}).then(res => ({
		categories: res.code === 1 && Array.isArray(res.data) ? res.data : mockBackendData.categories,
		defaultPoster: mockBackendData.defaultPoster
	})).catch(() => mockBackendData)
}

const fetchCurrentUser = () => {
	const user = getCurrentUser()
	return Promise.resolve(user || {
		uid: '',
		avatar: defaultAvatar,
		nickname: '校园搭子'
	})
}

// 获取当前日期的月日
const getTodayMonthDay = () => {
	const date = new Date()
	const month = date.getMonth() + 1
	const day = date.getDate()
	return { month, day }
}

// 生成月份数组
const months = Array.from({ length: 12 }, (_, i) => `${i + 1}月`)

// 生成天数数组（根据月份动态调整）
const getDaysInMonth = (month) => {
	// 使用今年作为基础年（2025年，因为当前环境是2025年）
	const date = new Date(2025, month, 0)
	return date.getDate()
}

// 当前选择的日期值（索引）
const dateValue = ref([0, 0])

// 日期范围数据
const dateRange = computed(() => {
	const daysInSelectedMonth = getDaysInMonth(dateValue.value[0] + 1)
	const days = Array.from({ length: daysInSelectedMonth }, (_, i) => `${i + 1}日`)
	return [months, days]
})

// 格式化日期显示
const formatDateDisplay = () => {
	const month = dateValue.value[0] + 1
	const day = dateValue.value[1] + 1
	return `${month}月${day}日`
}

// 格式化日期（保留向后兼容）
const formatDate = (dateStr) => {
	if (!dateStr) return ''
	const date = new Date(dateStr)
	const month = date.getMonth() + 1
	const day = date.getDate()
	return `${month}月${day}日`
}

const categoryGroups = ref([])

const initCategoryGroups = (data) => {
	categoryGroups.value = data.categories.map(group => ({
		...group,
		tags: group.tags.map((name, index) => ({
			id: group.start + index,
			name,
			groupName: group.name
		}))
	}))
}
const categories = computed(() => categoryGroups.value.map(item => item.name))
const genderOptions = ['不限', '限男', '限女']
const feeOptions = ['免费', 'AA', '固定人均']
const presetPeople = [2, 4, 6, 8]
const isCustomPeople = ref(false)

const form = ref({
	title: '',
	posterImg: '/static/organization/feature-card.jpg',
	qrcodeUrl: '',
	category: '体育运动',
	subCategoryId: 1,
	subCategoryName: '篮球',
	activityMonth: 1,
	activityDay: 1,
	activityClock: '',
	address: '',
	maxPeople: 2,
	genderLimit: '不限',
	feeType: '免费',
	depositRequired: false,
	depositAmount: 0,
	hostDesc: ''
})

const selectPeople = (num) => {
	isCustomPeople.value = false
	form.value.maxPeople = num
}

const enableCustomPeople = () => {
	isCustomPeople.value = true
	form.value.maxPeople = ''
}

const subCategories = computed(() => {
	const group = categoryGroups.value.find(item => item.name === form.value.category)
	return group ? group.tags : []
})

const activityTimeText = computed(() => {
	if (!form.value.activityClock) return ''
	return `${formatDateDisplay()} ${form.value.activityClock}`
})

const validateForm = () => {
	if (!form.value.title) return '请填写组局标题'
	if (!form.value.activityClock) return '请选择活动时间'
	if (!form.value.address) return '请填写集合地点'
	if (!form.value.qrcodeUrl) return '请上传联系二维码'
	if (!form.value.maxPeople || Number(form.value.maxPeople) < 2) return '人数上限至少 2 人'
	if (form.value.depositRequired && (!form.value.depositAmount || Number(form.value.depositAmount) <= 0)) {
		return '请填写保证金金额'
	}
	if (!form.value.hostDesc) return '请填写主理人说明'
	return ''
}

const buildPayload = () => ({
	...form.value,
	tagId: form.value.subCategoryId,
	tagName: form.value.subCategoryName,
	activityTime: activityTimeText.value,
	maxPeople: Number(form.value.maxPeople),
	depositAmount: form.value.depositRequired ? Number(form.value.depositAmount) : 0,
	userId: currentUser.value.uid || '',
	nickname: currentUser.value.nickname || currentUser.value.username || '校园搭子',
	avatar: currentUser.value.avatar || defaultAvatar,
	brandName: currentUser.value.nickname || currentUser.value.username || '校园搭子',
	brandAvatar: currentUser.value.avatar || defaultAvatar
})

const shouldUploadAsset = url => {
	const value = String(url || '')
	return value && !value.startsWith('/static/') && !/^https?:\/\//.test(value)
}

const isBlobUrl = url => String(url || '').startsWith('blob:')

const displayImage = (url, fallback = defaultAvatar) => {
	const value = String(url || '')
	return value && !isBlobUrl(value) ? value : fallback
}

const currentUserAvatar = computed(() => displayImage(currentUser.value.avatar))

const getUploadExt = url => {
	const value = String(url || '')
	if (isBlobUrl(value)) return 'jpg'
	const ext = value.split('?')[0].split('#')[0].split('.').pop()?.toLowerCase()
	return /^[a-z0-9]+$/.test(ext || '') ? ext : 'jpg'
}

const uploadAssetIfNeeded = async (url, category) => {
	if (!shouldUploadAsset(url)) return url
	const ext = getUploadExt(url)
	const uploaded = await uploadMultipleFiles([{ path: url, ext }], category)
	return uploaded[0] || url
}

const uploadAvatarIfNeeded = async () => {
	try {
		const avatar = await uploadAssetIfNeeded(currentUser.value.avatar, 'avatar')
		currentUser.value.avatar = displayImage(avatar)
		const cachedUser = uni.getStorageSync('userInfo') || {}
		if (cachedUser.uid) {
			uni.setStorageSync('userInfo', {
				...cachedUser,
				avatar: currentUser.value.avatar
			})
		}
	} catch (error) {
		currentUser.value.avatar = defaultAvatar
	}
}

const selectCategory = category => {
	form.value.category = category
	const firstSub = categoryGroups.value.find(item => item.name === category)?.tags[0]
	if (firstSub) selectSubCategory(firstSub)
}

const selectSubCategory = item => {
	form.value.subCategoryId = item.id
	form.value.subCategoryName = item.name
}

const onDateChange = event => {
	dateValue.value = event.detail.value
	form.value.activityMonth = dateValue.value[0] + 1
	form.value.activityDay = dateValue.value[1] + 1
}

const onTimeChange = event => {
	form.value.activityClock = event.detail.value
}

const toggleDeposit = () => {
	form.value.depositRequired = !form.value.depositRequired
	form.value.depositAmount = form.value.depositRequired ? Number(form.value.depositAmount || 10) : 0
}

const choosePoster = () => {
	uni.chooseImage({
		count: 1,
		sizeType: ['compressed'],
		sourceType: ['album', 'camera'],
		success: res => {
			form.value.posterImg = res.tempFilePaths[0]
		}
	})
}

const chooseQrcode = () => {
	uni.chooseImage({
		count: 1,
		sizeType: ['compressed'],
		sourceType: ['album', 'camera'],
		success: res => {
			form.value.qrcodeUrl = res.tempFilePaths[0]
		}
	})
}

const submitForm = async () => {
	if (submitting.value) return

	const message = validateForm()
	if (message) {
		uni.showToast({
			title: message,
			icon: 'none'
		})
		return
	}
	if (!currentUser.value.uid) {
		uni.showToast({
			title: '请先登录后发布',
			icon: 'none'
		})
		return
	}

	submitting.value = true
	let loadingShown = false

	try {
		uni.showLoading({
			title: '上传图片中...',
			mask: true
		})
		loadingShown = true
		form.value.posterImg = await uploadAssetIfNeeded(form.value.posterImg, 'organization/poster')
		form.value.qrcodeUrl = await uploadAssetIfNeeded(form.value.qrcodeUrl, 'organization/qrcode')
		await uploadAvatarIfNeeded()
		const payload = buildPayload()
		const res = await request({
			url: '/organization/publish',
			method: 'POST',
			data: payload
		})
		if (res.code !== 1 || !res.data) {
			throw new Error(res.msg || '发布失败')
		}
		submitting.value = false
		uni.showToast({
			title: '发布成功',
			icon: 'success'
		})
		uni.removeStorageSync('publishOrganizationDraft')
		setTimeout(() => {
			uni.navigateBack()
		}, 500)
	} catch (error) {
		submitting.value = false
		console.error('发布组局失败:', error)
		uni.showToast({
			title: error?.status === 403 ? '上传权限不足，请刷新后重试' : '发布失败，请稍后再试',
			icon: 'none'
		})
	} finally {
		if (loadingShown) uni.hideLoading()
	}
}

const saveDraft = () => {
	uni.setStorageSync('publishOrganizationDraft', form.value)
	uni.showToast({
		title: '已保存草稿',
		icon: 'none'
	})
}

onMounted(async () => {
	loading.value = true
	try {
		const [backendData, user] = await Promise.all([
			fetchBackendData(),
			fetchCurrentUser()
		])
		
		currentUser.value = user
		initCategoryGroups(backendData)
		
		const today = getTodayMonthDay()
		dateValue.value = [today.month - 1, today.day - 1]
		form.value.activityMonth = today.month
		form.value.activityDay = today.day
		form.value.posterImg = backendData.defaultPoster

		const draft = uni.getStorageSync('publishOrganizationDraft')
		if (draft) {
			form.value = {
				...form.value,
				...draft
			}
			if (draft.activityMonth && draft.activityDay) {
				dateValue.value = [draft.activityMonth - 1, draft.activityDay - 1]
			}
		}
	} catch (error) {
		console.error('初始化失败:', error)
	} finally {
		loading.value = false
	}
})
</script>

<style scoped>
page {
	background: #f6f4ef;
}

.page {
	background: linear-gradient(180deg, #fbfaf7 0%, #f6f4ef 100%);
	color: #202124;
	box-sizing: border-box;
	display: flex;
	flex-direction: column;
}

.page-scroll {
	flex: 1;
	overflow-y: auto;
	padding-bottom: 160rpx;
}

.hero {
	margin: 28rpx 28rpx 22rpx;
	padding: 34rpx;
	border-radius: 16rpx;
	background:
		linear-gradient(135deg, rgba(255, 255, 255, 0.92), rgba(246, 244, 239, 0.7)),
		radial-gradient(circle at 88% 0%, rgba(205, 196, 248, 0.3), transparent 36%);
	border: 1rpx solid rgba(32, 33, 36, 0.08);
	box-shadow: 0 24rpx 60rpx rgba(32, 33, 36, 0.07);
}

.eyebrow {
	display: block;
	font-size: 20rpx;
	color: #8b8690;
	font-weight: 800;
	letter-spacing: 0;
	margin-bottom: 10rpx;
}

.title {
	display: block;
	font-size: 58rpx;
	line-height: 1;
	font-weight: 900;
	margin-bottom: 18rpx;
}

.hero-copy {
	display: block;
	font-size: 26rpx;
	line-height: 1.62;
	color: #68636d;
}

.form-card,
.preview-card {
	margin: 0 28rpx 22rpx;
	padding: 28rpx;
	border-radius: 16rpx;
	background: rgba(255, 255, 255, 0.82);
	border: 1rpx solid rgba(32, 33, 36, 0.07);
	box-shadow: 0 18rpx 44rpx rgba(32, 33, 36, 0.05);
}

.section-head {
	display: flex;
	align-items: flex-end;
	justify-content: space-between;
	margin-bottom: 24rpx;
}

.section-title {
	font-size: 32rpx;
	font-weight: 900;
}

.section-sub {
	font-size: 22rpx;
	color: #8b8690;
	font-weight: 700;
}

.field {
	margin-bottom: 24rpx;
}

.field:last-child {
	margin-bottom: 0;
}

.label {
	display: block;
	font-size: 24rpx;
	font-weight: 900;
	color: #343238;
	margin-bottom: 12rpx;
}

.label.no-margin {
	margin-bottom: 6rpx;
}

.hint {
	display: block;
	font-size: 22rpx;
	color: #8b8690;
}

.input,
.textarea,
.picker-field {
	width: 100%;
	border-radius: 20rpx;
	background: #fafafb;
	color: #202124;
	font-size: 28rpx;
	box-sizing: border-box;
	border: 2rpx solid #f0f1f3;
	transition: border-color 0.2s ease;
}

.input:focus,
.textarea:focus,
.picker-field:active {
	border-color: #e0e2e6;
}

.input {
	height: 88rpx;
	padding: 0 24rpx;
}

.compact-input {
	height: 72rpx;
	font-size: 26rpx;
}

.picker-field {
	height: 88rpx;
	padding: 0 24rpx;
	display: flex;
	align-items: center;
	color: #202124;
}

.textarea {
	height: 220rpx;
	padding: 24rpx;
	line-height: 1.6;
}

.placeholder {
	color: #aaa5ad;
	font-size: 26rpx;
}

.two-col {
	display: flex;
	gap: 16rpx;
}

.half {
	flex: 1;
	min-width: 0;
}

.chip-row,
.seg-row {
	display: flex;
	flex-wrap: wrap;
	gap: 12rpx;
}

.chip,
.seg {
	height: 60rpx;
	padding: 0 20rpx;
	border-radius: 999rpx;
	background: #f6f7f8;
	color: #68636d;
	font-size: 24rpx;
	font-weight: 800;
	display: flex;
	align-items: center;
	justify-content: center;
	border: 1rpx solid transparent;
}

.chip.active,
.seg.active {
	background: #202124;
	color: #ffffff;
}

.switch-line {
	display: flex;
	align-items: center;
	justify-content: space-between;
	gap: 20rpx;
	padding: 22rpx;
	border-radius: 16rpx;
	background: #f6f7f8;
}

.switch {
	flex: 0 0 auto;
	width: 74rpx;
	height: 42rpx;
	border-radius: 999rpx;
	background: #d9dde2;
	position: relative;
}

.switch::after {
	content: "";
	position: absolute;
	width: 34rpx;
	height: 34rpx;
	left: 4rpx;
	top: 4rpx;
	border-radius: 50%;
	background: #ffffff;
	transition: all 0.2s ease;
}

.switch.on {
	background: #202124;
}

.switch.on::after {
	left: 36rpx;
}

.cover-picker {
	position: relative;
	height: 300rpx;
	border-radius: 16rpx;
	overflow: hidden;
	background: #eef1f3;
}

.cover-image {
	width: 100%;
	height: 100%;
	display: block;
}

.cover-action {
	position: absolute;
	right: 18rpx;
	bottom: 18rpx;
	height: 56rpx;
	padding: 0 20rpx;
	border-radius: 999rpx;
	background: rgba(32, 33, 36, 0.82);
	color: #ffffff;
	font-size: 23rpx;
	font-weight: 900;
	display: flex;
	align-items: center;
	justify-content: center;
}

.qr-picker {
	position: relative;
	width: 220rpx;
	height: 220rpx;
	border-radius: 18rpx;
	overflow: hidden;
	background: #f6f7f8;
	border: 2rpx dashed #d9dde2;
	display: flex;
	align-items: center;
	justify-content: center;
}

.qr-image {
	width: 100%;
	height: 100%;
	display: block;
}

.qr-empty {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	gap: 10rpx;
	color: #7d8996;
	font-size: 23rpx;
	font-weight: 800;
}

.qr-plus {
	font-size: 56rpx;
	line-height: 1;
}

.qr-action {
	position: absolute;
	left: 0;
	right: 0;
	bottom: 0;
	height: 54rpx;
	background: rgba(32, 33, 36, 0.82);
	color: #ffffff;
	font-size: 22rpx;
	font-weight: 900;
	display: flex;
	align-items: center;
	justify-content: center;
}



.main-row {
	display: flex;
	gap: 22rpx;
}

.poster-stack {
	position: relative;
	flex: 0 0 190rpx;
	height: 250rpx;
}

.poster,
.poster-shadow {
	position: absolute;
	width: 168rpx;
	height: 224rpx;
	border-radius: 16rpx;
	object-fit: cover;
}

.poster {
	left: 0;
	top: 0;
	z-index: 2;
	box-shadow: 0 14rpx 30rpx rgba(32, 33, 36, 0.14);
}

.poster-back {
	right: 0;
	bottom: 0;
	opacity: 0.28;
	filter: blur(1rpx);
	transform: rotate(5deg);
}

.poster-watermark {
	position: absolute;
	left: 12rpx;
	bottom: 36rpx;
	z-index: 3;
	height: 40rpx;
	padding: 0 14rpx;
	border-radius: 999rpx;
	background: rgba(255, 255, 255, 0.86);
	color: #202124;
	font-size: 19rpx;
	font-weight: 900;
	display: flex;
	align-items: center;
}

.content {
	flex: 1;
	min-width: 0;
}

.organizer-row {
	display: flex;
	align-items: center;
	gap: 10rpx;
	margin-bottom: 14rpx;
}

.brand-avatar {
	width: 42rpx;
	height: 42rpx;
	border-radius: 50%;
	background: #eef1f3;
}

.brand-name {
	font-size: 23rpx;
	font-weight: 900;
	max-width: 124rpx;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}

.times-pill {
	height: 34rpx;
	padding: 0 12rpx;
	border-radius: 999rpx;
	background: #f2f4f6;
	color: #77737d;
	font-size: 18rpx;
	font-weight: 800;
	display: flex;
	align-items: center;
}

.card-title {
	display: -webkit-box;
	-webkit-line-clamp: 2;
	-webkit-box-orient: vertical;
	overflow: hidden;
	font-size: 31rpx;
	line-height: 1.28;
	font-weight: 900;
	margin-bottom: 16rpx;
}

.meta-row {
	display: flex;
	flex-direction: column;
	gap: 10rpx;
	margin-bottom: 16rpx;
}

.meta-item {
	display: flex;
	align-items: center;
	gap: 10rpx;
	min-width: 0;
	color: #77737d;
	font-size: 22rpx;
	font-weight: 700;
}

.meta-item text {
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}

.clock-icon,
.pin-icon {
	width: 20rpx;
	height: 20rpx;
	border-radius: 50%;
	border: 4rpx solid #b8c3cf;
	box-sizing: border-box;
	flex: 0 0 auto;
}

.pin-icon {
	border-radius: 50% 50% 50% 0;
	transform: rotate(-45deg);
}

.info-chip-row {
	display: flex;
	flex-wrap: wrap;
	gap: 10rpx;
}

.info-chip {
	height: 38rpx;
	padding: 0 12rpx;
	border-radius: 999rpx;
	background: #f6f7f8;
	color: #55515d;
	font-size: 20rpx;
	font-weight: 800;
	display: flex;
	align-items: center;
}

.host-note {
	margin-top: 18rpx;
	padding: 18rpx;
	border-radius: 16rpx;
	background: #f6f7f8;
	color: #55515d;
	font-size: 24rpx;
	line-height: 1.55;
}

.host-label {
	font-weight: 900;
	color: #202124;
}

.host-text {
	color: #55515d;
}

.cover-category-field {
	display: flex;
	align-items: stretch;
	gap: 18rpx;
}

.cover-side {
	flex: 1;
	min-width: 0;
	display: flex;
	flex-direction: column;
	justify-content: space-between;
}

.date-time-row {
	display: flex;
	gap: 12rpx;
}

.flex-1 {
	flex: 1;
	min-width: 0;
}

.compact-field {
	margin-bottom: 12rpx;
}

.compact-field:last-child {
	margin-bottom: 0;
}

.compact-field .label {
	margin-bottom: 8rpx;
}

.people-field {
	width: 100%;
}

.people-selector {
	display: flex;
	gap: 10rpx;
	flex-wrap: wrap;
}

.people-btn {
	flex: 1;
	min-width: 0;
	height: 64rpx;
	border-radius: 14rpx;
	background: #f6f7f8;
	color: #68636d;
	font-size: 25rpx;
	font-weight: 800;
	display: flex;
	align-items: center;
	justify-content: center;
	border: 2rpx solid transparent;
	transition: all 0.2s ease;
}

.people-btn.active {
	background: #202124;
	color: #ffffff;
	border-color: #202124;
}

.people-btn:active {
	transform: scale(0.96);
}

.custom-btn {
	flex: 1.2;
}

.custom-input {
	margin-top: 12rpx;
}

.side-row {
	display: flex;
	gap: 12rpx;
}

.sub-chip-row {
	display: flex;
	flex-wrap: wrap;
	gap: 12rpx;
}

.sub-chip {
	flex: 0 0 auto;
	height: 56rpx;
	padding: 0 18rpx;
	border-radius: 999rpx;
	background: #f6f7f8;
	color: #68636d;
	font-size: 23rpx;
	font-weight: 800;
	display: flex;
	align-items: center;
	border: 1rpx solid transparent;
}

.sub-chip.active {
	background: #202124;
	color: #ffffff;
}

.cover-category-field .cover-picker {
	flex: 0 0 198rpx;
	height: 250rpx;
	border-radius: 18rpx;
	border: 1rpx solid rgba(32, 33, 36, 0.08);
}

.cover-category-field .cover-image {
	background: #f7f9fb;
}

.activity-card {
  margin: 0 28rpx 22rpx; /* 未被覆盖，保留 */
  padding: 0; /* 后者覆盖前者 24rpx */
  border-radius: 24rpx; /* 后者覆盖前者 16rpx */
  background: linear-gradient(180deg, #ffffff 0%, #f1f8ff 100%); /* 渐变覆盖纯色 */
  overflow: hidden; /* 后者新增 */
  box-shadow: 0 18rpx 42rpx rgba(70, 83, 104, 0.12), inset 0 1rpx 0 rgba(255, 255, 255, 0.96); /* 后者覆盖 */
  border: 1rpx solid rgba(255, 255, 255, 0.9); /* 后者覆盖 */
}


.main-row {
	min-height: 286rpx;
	padding: 24rpx 22rpx 18rpx;
	box-sizing: border-box;
	position: relative;
	gap: 0;
	background:
		linear-gradient(90deg, rgba(255, 255, 255, 0.72), rgba(244, 250, 255, 0.28)),
		repeating-linear-gradient(135deg, rgba(222, 235, 246, 0.32) 0 20rpx, rgba(255, 255, 255, 0) 20rpx 42rpx);
}

.poster-stack {
	width: 170rpx;
	height: 224rpx;
	margin: 6rpx 18rpx 0 0;
	flex: 0 0 170rpx;
}

.poster,
.poster-shadow {
	left: 12rpx;
	top: 8rpx;
	width: 160rpx;
	height: 200rpx;
	border-radius: 18rpx;
	border: 4rpx solid #ffffff;
	background: #f7f9fb;
	transform: rotate(-5deg) translate(-12rpx, -6rpx);
}

.poster {
	z-index: 3;
	box-shadow: 0 10rpx 18rpx rgba(18, 35, 52, 0.18);
}

.poster-back {
	right: auto;
	bottom: auto;
	transform: rotate(6deg) translate(-12rpx, -6rpx);
	opacity: 0.96;
	filter: none;
}

.poster-watermark {
	z-index: 5;
	left: 0;
	right: 0;
	bottom: -67rpx;
	height: 42rpx;
	padding: 0;
	border-radius: 0;
	background: transparent;
	color: rgba(222, 235, 246, 1);
	font-size: 38rpx;
	justify-content: center;
	white-space: nowrap;
	overflow: hidden;
	text-overflow: ellipsis;
	pointer-events: none;
}

.organizer-row {
	gap: 0;
}

.brand-avatar {
	width: 34rpx;
	height: 34rpx;
	background: #f6c23e;
	margin-right: 9rpx;
	flex-shrink: 0;
}

.brand-name {
	font-size: 25rpx;
	color: #1c2834;
	margin-right: 10rpx;
	max-width: 190rpx;
}

.times-pill {
	height: 32rpx;
	border-radius: 8rpx;
	background: #dfe9f5;
	color: #314253;
	font-size: 21rpx;
	white-space: nowrap;
}

.activity-title {
	display: block;
	font-size: 32rpx;
	line-height: 1.28;
	color: #17202a;
	font-weight: 900;
	margin-bottom: 12rpx;
	white-space: nowrap;
	overflow: hidden;
	text-overflow: ellipsis;
}

.meta-row {
	flex-direction: row;
	gap: 0;
	align-items: center;
	margin-bottom: 14rpx;
}

.meta-item {
	margin-right: 16rpx;
	gap: 0;
	color: #7d8996;
}

.meta-item text {
	max-width: 180rpx;
}

.clock-icon {
	width: 18rpx;
	height: 18rpx;
	border-color: #c3ccd6;
	margin-right: 5rpx;
	position: relative;
}

.clock-icon::after {
	content: '';
	position: absolute;
	left: 5rpx;
	top: 2rpx;
	width: 4rpx;
	height: 8rpx;
	background: #c3ccd6;
	border-radius: 99rpx;
}

.pin-icon {
	width: 16rpx;
	height: 16rpx;
	border: 0;
	background: #c3ccd6;
	margin-right: 5rpx;
	position: relative;
	flex-shrink: 0;
}

.pin-icon::after {
	content: '';
	position: absolute;
	width: 6rpx;
	height: 6rpx;
	border-radius: 50%;
	background: #eef7ff;
	left: 5rpx;
	top: 5rpx;
}

.info-chip-row {
	gap: 8rpx;
	margin-bottom: 10rpx;
}

.info-chip {
	height: 34rpx;
	background: #edf4fb;
	color: #43566b;
	font-size: 21rpx;
}

.host-note {
	margin-top: 0;
	padding: 18rpx 24rpx 24rpx;
	border-radius: 0;
	background: linear-gradient(180deg, #f1f8ff, #ffffff);
	border-top: 1rpx solid rgba(211, 226, 240, 0.8);
	color: #4b5b6c;
}

.deposit-row {
	display: flex;
	justify-content: space-between;
	color: #91a0af;
	font-size: 21rpx;
	font-weight: 800;
	margin-bottom: 16rpx;
}

.bottom-row {
	display: flex;
	align-items: center;
	justify-content: space-between;
}

.join-area {
	display: flex;
	align-items: center;
	min-width: 0;
}

.avatar-group {
	display: flex;
	align-items: center;
	margin-right: 12rpx;
}

.join-avatar {
	width: 36rpx;
	height: 36rpx;
	border-radius: 50%;
	border: 3rpx solid #ffffff;
	background: #dfe9f5;
}

.join-avatar + .join-avatar {
	margin-left: -10rpx;
}

.join-count {
	color: #7d8996;
	font-size: 21rpx;
	font-weight: 800;
	white-space: nowrap;
}

.signup-btn {
	width: 114rpx;
	height: 60rpx;
	background: #222933;
	border-radius: 13rpx;
	color: #ffffff;
	position: relative;
	display: flex;
	align-items: center;
	justify-content: center;
	margin-left: 16rpx;
	box-shadow: 0 8rpx 16rpx rgba(20, 30, 40, 0.2);
	flex-shrink: 0;
	transform: rotate(-1deg);
}

.signup-btn:active {
	transform: rotate(-1deg) scale(0.96);
	opacity: 0.9;
}

.go-text {
	font-family: "Arial Black", Gadget, sans-serif;
	font-style: italic;
	font-size: 35rpx;
	line-height: 1;
	color: #ffd63c;
	-webkit-text-stroke: 3rpx #101418;
	font-weight: 1000;
	text-shadow: -2rpx 2rpx 0 #101418;
	transform: rotate(-10deg);
	position: absolute;
	left: -15rpx;
	top: -19rpx;
}

.signup-text {
	font-size: 25rpx;
	line-height: 1.1;
	font-weight: 900;
}

.preview-card {
	background: #202124;
	color: #ffffff;
}

.preview-top {
	display: flex;
	align-items: center;
	margin-bottom: 20rpx;
}

.preview-mark {
	width: 76rpx;
	height: 76rpx;
	border-radius: 16rpx;
	background: rgba(255, 255, 255, 0.14);
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 32rpx;
	font-weight: 900;
	margin-right: 18rpx;
}

.preview-info {
	flex: 1;
	min-width: 0;
}

.preview-title {
	display: block;
	font-size: 31rpx;
	font-weight: 900;
	margin-bottom: 8rpx;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}

.preview-meta {
	display: block;
	font-size: 22rpx;
	color: rgba(255, 255, 255, 0.64);
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}

.preview-tags {
	display: flex;
	gap: 12rpx;
	margin-bottom: 18rpx;
}

.preview-tags text {
	height: 42rpx;
	padding: 0 14rpx;
	border-radius: 999rpx;
	background: rgba(255, 255, 255, 0.12);
	color: rgba(255, 255, 255, 0.84);
	font-size: 21rpx;
	font-weight: 800;
	display: flex;
	align-items: center;
}

.preview-desc {
	display: block;
	font-size: 25rpx;
	line-height: 1.6;
	color: rgba(255, 255, 255, 0.78);
}

.bottom-bar {
	position: fixed;
	left: 0;
	right: 0;
	bottom: 0;
	z-index: 10;
	padding: 18rpx 28rpx 28rpx;
	background: rgba(246, 244, 239, 0.95);
	backdrop-filter: blur(18rpx);
	display: flex;
	gap: 16rpx;
	box-sizing: border-box;
}

.draft-btn,
.submit-btn {
	height: 82rpx;
	border-radius: 16rpx;
	font-size: 27rpx;
	font-weight: 900;
	display: flex;
	align-items: center;
	justify-content: center;
}

.draft-btn {
	width: 210rpx;
	background: #ffffff;
	color: #202124;
	border: 1rpx solid rgba(32, 33, 36, 0.08);
}

.submit-btn {
	flex: 1;
	background: #202124;
	color: #ffffff;
	box-shadow: 0 18rpx 40rpx rgba(32, 33, 36, 0.18);
}

.submit-btn.disabled {
	opacity: 0.62;
}
</style>
