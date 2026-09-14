<template>
	<view class="page">
		<view class="form">
			<!-- 基本信息 -->
			<view class="section">
				<text class="section-title">基本信息</text>

				<view class="form-group">
					<text class="form-label">昵称</text>
					<input v-model="form.nickname" class="form-input" placeholder="你的昵称" maxlength="20" />
				</view>

				<view class="form-group">
					<text class="form-label">性别</text>
					<view class="gender-select">
						<view class="gender-option" :class="{ active: form.gender === 'male' }" @click="form.gender = 'male'">
							<text class="gender-icon">♂</text>
							<text>男</text>
						</view>
						<view class="gender-option" :class="{ active: form.gender === 'female' }" @click="form.gender = 'female'">
							<text class="gender-icon">♀</text>
							<text>女</text>
						</view>
					</view>
				</view>

				<view class="form-group">
					<text class="form-label">年龄</text>
					<input v-model="form.age" class="form-input" type="number" placeholder="18-35" maxlength="2" />
				</view>

				<view class="form-group">
					<text class="form-label">身高 (cm)</text>
					<input v-model="form.height" class="form-input" type="number" placeholder="150-200" maxlength="3" />
				</view>
			</view>

			<!-- 学校信息 -->
			<view class="section">
				<text class="section-title">学校信息</text>

				<view class="form-group">
					<text class="form-label">学校</text>
					<input v-model="form.school" class="form-input" placeholder="你的学校" maxlength="30" />
				</view>

				<view class="form-group">
					<text class="form-label">专业</text>
					<input v-model="form.major" class="form-input" placeholder="你的专业" maxlength="30" />
				</view>

				<view class="form-group">
					<text class="form-label">职业/身份</text>
					<input v-model="form.occupation" class="form-input" placeholder="如：研究生在读、工程师" maxlength="30" />
				</view>
			</view>

			<!-- 个人介绍 -->
			<view class="section">
				<text class="section-title">个人介绍</text>

				<view class="form-group">
					<text class="form-label">自我介绍</text>
					<textarea
						v-model="form.selfIntro"
						class="form-textarea"
						placeholder="介绍一下自己吧，让TA更了解你..."
						maxlength="300"
						:show-count="true"
					></textarea>
				</view>

				<view class="form-group">
					<text class="form-label">兴趣爱好 <text class="form-hint">（点击选择）</text></text>
					<view class="tag-grid">
						<view
							v-for="tag in hobbyOptions"
							:key="tag"
							class="tag-item"
							:class="{ selected: form.hobbies.includes(tag) }"
							@click="toggleTag(tag, 'hobbies')"
						>
							{{ tag }}
						</view>
					</view>
				</view>
			</view>

			<!-- 期望对象 -->
			<view class="section">
				<text class="section-title">期望对象</text>

				<view class="form-group">
					<text class="form-label">期望条件</text>
					<textarea
						v-model="form.requirements"
						class="form-textarea"
						placeholder="你希望TA是什么样的？如：希望对方170以上，有责任心..."
						maxlength="200"
						:show-count="true"
					></textarea>
				</view>
			</view>

			<!-- 头像 -->
			<view class="section">
				<text class="section-title">头像</text>
				<view class="form-group">
					<view class="image-uploader">
						<view class="image-box" v-if="form.avatar" @click="previewImage">
							<image class="preview-image" :src="form.avatar" mode="aspectFill"></image>
							<view class="remove-btn" @click.stop="removeImage">
								<text>×</text>
							</view>
							<view class="upload-progress" v-if="uploading">
								<text>{{ uploadProgress }}%</text>
							</view>
						</view>
						<view class="upload-btn" v-else @click="chooseImage" :class="{ disabled: uploading }">
							<text class="upload-icon">+</text>
							<text class="upload-text">{{ uploading ? '上传中...' : '上传头像' }}</text>
						</view>
					</view>
				</view>
			</view>

			<view class="submit-wrap">
				<button class="submit-btn" :disabled="submitting || !canSubmit" @click="handleSubmit">
					{{ submitting ? '发布中...' : '发布到相亲墙' }}
				</button>
			</view>
		</view>
	</view>
</template>

<script setup>
import { reactive, computed, ref } from 'vue'
import { getCurrentUser } from '@/utils/auth.js'
import { uploadFile } from '@/utils/upload.js'
import request from '@/utils/request.js'

const submitting = ref(false)
const uploading = ref(false)
const uploadProgress = ref(0)

const form = reactive({
	nickname: '',
	gender: 'male',
	age: '',
	school: '',
	major: '',
	height: '',
	occupation: '',
	selfIntro: '',
	hobbies: [],
	requirements: '',
	avatar: ''
})

const hobbyOptions = [
	'摄影', '旅行', '健身', '跑步', '篮球', '足球',
	'游泳', '瑜伽', '舞蹈', '钢琴', '吉他', '绘画',
	'电影', '追剧', '美食', '烹饪', '旅行', '阅读',
	'写作', '游戏', '电竞', '猫', '狗', '自驾',
	'爬山', '滑雪', '冲浪', '追星', '演唱会'
]

const canSubmit = computed(() => {
	return form.nickname.trim() &&
		form.age &&
		form.school.trim() &&
		form.major.trim() &&
		form.selfIntro.trim().length >= 20 &&
		form.requirements.trim()
})

const toggleTag = (tag, field) => {
	const arr = form[field]
	const idx = arr.indexOf(tag)
	if (idx > -1) {
		arr.splice(idx, 1)
	} else {
		if (arr.length >= 6) {
			uni.showToast({ title: '最多选择6个', icon: 'none' })
			return
		}
		arr.push(tag)
	}
}

const chooseImage = () => {
	if (uploading.value) return
	uni.chooseImage({
		count: 1,
		sizeType: ['compressed'],
		sourceType: ['album', 'camera'],
		success: async (res) => {
			const tempFile = res.tempFilePaths[0]
			uploading.value = true
			uploadProgress.value = 0
			try {
				const url = await uploadFile(tempFile, 'avatar', (progress) => {
					uploadProgress.value = progress
				})
				form.avatar = url
			} catch (e) {
				uni.showToast({ title: e.message || '图片上传失败', icon: 'none' })
			} finally {
				uploading.value = false
				uploadProgress.value = 0
			}
		}
	})
}

const removeImage = () => {
	form.avatar = ''
}

const previewImage = () => {
	if (!form.avatar) return
	uni.previewImage({ urls: [form.avatar] })
}

const handleSubmit = async () => {
	if (!canSubmit.value || submitting.value) return

	const user = getCurrentUser()
	if (!user) {
		uni.navigateTo({ url: '/pages/login/login' })
		return
	}

	submitting.value = true
	try {
		const res = await request({
			url: '/partner/publish',
			method: 'POST',
			data: {
				userId: user.uid,
				nickname: form.nickname.trim(),
				gender: form.gender,
				age: Number(form.age),
				school: form.school.trim(),
				major: form.major.trim(),
				height: Number(form.height),
				occupation: form.occupation.trim(),
				selfIntro: form.selfIntro.trim(),
				hobbies: form.hobbies.join(','),
				requirements: form.requirements.trim(),
				avatar: form.avatar || ''
			}
		})

		if (res && res.code === 1) {
			uni.showToast({ title: '发布成功', icon: 'success' })
			setTimeout(() => {
				uni.navigateBack()
			}, 1000)
		} else {
			uni.showToast({ title: res?.msg || '发布失败', icon: 'none' })
		}
	} finally {
		submitting.value = false
	}
}
</script>

<style scoped>
.page {
	background: #f5f6f9;
	min-height: 100vh;
}

.form {
	padding: 24rpx;
	padding-bottom: 120rpx;
}

.section {
	background: #fff;
	border-radius: 16rpx;
	padding: 28rpx;
	margin-bottom: 24rpx;
}

.section-title {
	display: block;
	font-size: 30rpx;
	font-weight: 700;
	color: #333;
	margin-bottom: 24rpx;
	padding-bottom: 16rpx;
	border-bottom: 1rpx solid #f0f0f0;
}

.form-group {
	margin-bottom: 24rpx;
}

.form-group:last-child {
	margin-bottom: 0;
}

.form-label {
	display: block;
	font-size: 26rpx;
	color: #666;
	margin-bottom: 12rpx;
	font-weight: 500;
}

.form-hint {
	font-size: 22rpx;
	color: #999;
	font-weight: normal;
}

.form-input {
	height: 80rpx;
	padding: 0 24rpx;
	background: #f5f6f9;
	border-radius: 12rpx;
	font-size: 28rpx;
}

.form-textarea {
	width: 100%;
	padding: 20rpx;
	background: #f5f6f9;
	border-radius: 12rpx;
	font-size: 28rpx;
	min-height: 160rpx;
	box-sizing: border-box;
	line-height: 1.6;
}

.gender-select {
	display: flex;
	gap: 20rpx;
}

.gender-option {
	flex: 1;
	height: 80rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	gap: 10rpx;
	background: #f5f6f9;
	border-radius: 12rpx;
	border: 2rpx solid transparent;
	font-size: 28rpx;
	color: #666;
}

.gender-option.active {
	background: #fff0f5;
	border-color: #e84393;
	color: #e84393;
}

.gender-icon {
	font-size: 32rpx;
}

.tag-grid {
	display: flex;
	flex-wrap: wrap;
	gap: 16rpx;
}

.tag-item {
	padding: 10rpx 20rpx;
	background: #f5f6f9;
	border-radius: 20rpx;
	font-size: 24rpx;
	color: #666;
	border: 2rpx solid transparent;
}

.tag-item.selected {
	background: #fff0f5;
	border-color: #e84393;
	color: #e84393;
}

.image-uploader {
	display: flex;
	gap: 20rpx;
}

.upload-btn {
	width: 180rpx;
	height: 180rpx;
	background: #f8f9fa;
	border-radius: 16rpx;
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	gap: 12rpx;
	border: 2rpx dashed #ddd;
}

.upload-btn.disabled {
	opacity: 0.6;
}

.upload-icon {
	font-size: 60rpx;
	color: #bbb;
	line-height: 1;
}

.upload-text {
	font-size: 22rpx;
	color: #999;
}

.image-box {
	width: 180rpx;
	height: 180rpx;
	position: relative;
	border-radius: 16rpx;
	overflow: hidden;
}

.preview-image {
	width: 100%;
	height: 100%;
}

.remove-btn {
	position: absolute;
	top: 8rpx;
	right: 8rpx;
	width: 40rpx;
	height: 40rpx;
	background: rgba(0, 0, 0, 0.6);
	border-radius: 50%;
	display: flex;
	align-items: center;
	justify-content: center;
}

.remove-btn text {
	font-size: 32rpx;
	color: #fff;
	line-height: 1;
}

.upload-progress {
	position: absolute;
	left: 0;
	right: 0;
	bottom: 0;
	height: 48rpx;
	background: rgba(0, 0, 0, 0.6);
	display: flex;
	align-items: center;
	justify-content: center;
}

.upload-progress text {
	font-size: 22rpx;
	color: #fff;
}

.submit-wrap {
	position: fixed;
	bottom: 0;
	left: 0;
	right: 0;
	padding: 24rpx 32rpx;
	padding-bottom: calc(24rpx + env(safe-area-inset-bottom));
	background: #fff;
	box-shadow: 0 -2rpx 20rpx rgba(0, 0, 0, 0.05);
}

.submit-btn {
	width: 100%;
	height: 88rpx;
	background: linear-gradient(135deg, #e84393, #fd79a8);
	color: #fff;
	font-size: 32rpx;
	font-weight: 600;
	border-radius: 44rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	border: none;
}

.submit-btn[disabled] {
	background: #ccc;
}
</style>
