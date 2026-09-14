<template>
	<view class="page">
		<view class="form-card">
			<view class="form-group">
				<text class="form-label">物品类型</text>
				<view class="type-selector">
					<view
						class="type-btn"
						:class="{ active: form.type === 'lost' }"
						@click="form.type = 'lost'"
					>
						<text>寻物启事</text>
					</view>
					<view
						class="type-btn"
						:class="{ active: form.type === 'found' }"
						@click="form.type = 'found'"
					>
						<text>失物招领</text>
					</view>
				</view>
			</view>

			<view class="form-group">
				<text class="form-label">物品名称</text>
				<input
					v-model="form.title"
					class="form-input"
					placeholder="请输入物品名称"
					maxlength="50"
				/>
			</view>

			<view class="form-group">
				<text class="form-label">详细描述</text>
				<textarea
					v-model="form.description"
					class="form-textarea"
					placeholder="请详细描述物品特征、丢失/捡到经过等"
					maxlength="500"
				></textarea>
				<text class="char-count">{{ form.description.length }}/500</text>
			</view>

			<view class="form-group">
				<text class="form-label">丢失/捡到地点</text>
				<input
					v-model="form.location"
					class="form-input"
					placeholder="请输入具体地点"
					maxlength="100"
				/>
			</view>

			<view class="form-group">
				<text class="form-label">物品图片<text class="form-optional">（可选，上传一张）</text></text>
				<view class="image-uploader">
					<view class="image-box" v-if="form.image" @click="previewImage">
						<image class="preview-image" :src="form.image" mode="aspectFill"></image>
						<view class="remove-btn" @click.stop="removeImage">
							<text>×</text>
						</view>
						<view class="upload-progress" v-if="uploadProgress > 0 && uploadProgress < 100">
							<text>{{ uploadProgress }}%</text>
						</view>
					</view>
					<view class="upload-btn" v-else @click="chooseImage" :class="{ disabled: uploading }">
						<text class="upload-icon">+</text>
						<text class="upload-text">{{ uploading ? '上传中...' : '添加图片' }}</text>
					</view>
				</view>
			</view>
		</view>

		<view class="form-card">
			<text class="card-title">联系方式</text>
			<view class="form-group">
				<text class="form-label">姓名</text>
				<input
					v-model="form.name"
					class="form-input"
					placeholder="请输入真实姓名"
					maxlength="20"
				/>
			</view>
			<view class="form-group">
				<text class="form-label">手机号</text>
				<input
					v-model="form.phone"
					class="form-input"
					type="number"
					maxlength="11"
					placeholder="请输入手机号"
				/>
			</view>
			<view class="form-group">
				<text class="form-label">学号</text>
				<input
					v-model="form.studentId"
					class="form-input"
					placeholder="请输入学号"
					maxlength="20"
				/>
			</view>
		</view>

		<view class="submit-bar">
			<button class="submit-btn" :disabled="!canSubmit || submitting" @click="handleSubmit">
				{{ submitting ? '发布中...' : '发布' }}
			</button>
		</view>
	</view>
</template>

<script setup>
import { reactive, computed, ref } from 'vue'
import request from '@/utils/request.js'
import { getCurrentUser } from '@/utils/auth.js'
import { uploadFile } from '@/utils/upload.js'

const submitting = ref(false)
const uploading = ref(false)
const uploadProgress = ref(0)

const form = reactive({
	type: 'lost',
	title: '',
	description: '',
	location: '',
	name: '',
	phone: '',
	studentId: '',
	image: ''
})

const canSubmit = computed(() => {
	return form.title.trim() &&
		form.description.trim() &&
		form.location.trim() &&
		form.name.trim() &&
		form.phone.trim() &&
		form.studentId.trim() &&
		/^1[3-9]\d{9}$/.test(form.phone)
})

const validate = () => {
	if (!form.title.trim()) {
		uni.showToast({ title: '请输入物品名称', icon: 'none' })
		return false
	}
	if (!form.description.trim()) {
		uni.showToast({ title: '请输入详细描述', icon: 'none' })
		return false
	}
	if (!form.location.trim()) {
		uni.showToast({ title: '请输入地点', icon: 'none' })
		return false
	}
	if (!form.name.trim()) {
		uni.showToast({ title: '请输入姓名', icon: 'none' })
		return false
	}
	if (!form.phone.trim()) {
		uni.showToast({ title: '请输入手机号', icon: 'none' })
		return false
	}
	if (!/^1[3-9]\d{9}$/.test(form.phone)) {
		uni.showToast({ title: '手机号格式不正确', icon: 'none' })
		return false
	}
	if (!form.studentId.trim()) {
		uni.showToast({ title: '请输入学号', icon: 'none' })
		return false
	}
	return true
}

const handleSubmit = async () => {
	if (!validate() || submitting.value) return

	const user = getCurrentUser()
	if (!user) {
		uni.navigateTo({ url: '/pages/login/login' })
		return
	}

	submitting.value = true
	try {
		const res = await request({
			url: '/lost-found/publish',
			method: 'POST',
			data: {
				userUid: user.uid,
				type: form.type,
				title: form.title.trim(),
				description: form.description.trim(),
				location: form.location.trim(),
				name: form.name.trim(),
				phone: form.phone.trim(),
				studentId: form.studentId.trim(),
				image: form.image || ''
			}
		})

		if (res.code === 1) {
			uni.showToast({ title: '发布成功', icon: 'success' })
			setTimeout(() => {
				uni.navigateBack()
			}, 1000)
		} else {
			uni.showToast({ title: res.msg || '发布失败', icon: 'none' })
		}
	} finally {
		submitting.value = false
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
				const url = await uploadFile(tempFile, 'lostfound', (progress) => {
					uploadProgress.value = progress
				})
				form.image = url
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
	form.image = ''
}

const previewImage = () => {
	if (!form.image) return
	uni.previewImage({
		urls: [form.image]
	})
}
</script>

<style scoped>
.page {
	background: #f5f6f9;
	padding-bottom: 140rpx;
}

.form-card {
	margin: 24rpx;
	padding: 28rpx;
	background: #ffffff;
	border-radius: 16rpx;
}

.card-title {
	display: block;
	font-size: 32rpx;
	font-weight: 600;
	color: #333;
	margin-bottom: 28rpx;
	padding-bottom: 20rpx;
	border-bottom: 1rpx solid #eee;
}

.form-group {
	margin-bottom: 28rpx;
}

.form-group:last-child {
	margin-bottom: 0;
}

.form-label {
	display: block;
	font-size: 26rpx;
	color: #666;
	margin-bottom: 12rpx;
}

.form-input {
	width: 100%;
	height: 88rpx;
	padding: 0 24rpx;
	background: #f8f9fa;
	border-radius: 12rpx;
	font-size: 28rpx;
	box-sizing: border-box;
}

.form-textarea {
	width: 100%;
	height: 200rpx;
	padding: 20rpx 24rpx;
	background: #f8f9fa;
	border-radius: 12rpx;
	font-size: 28rpx;
	line-height: 1.6;
	box-sizing: border-box;
}

.char-count {
	display: block;
	text-align: right;
	font-size: 22rpx;
	color: #999;
	margin-top: 8rpx;
}

.type-selector {
	display: flex;
	gap: 20rpx;
}

.type-btn {
	flex: 1;
	height: 88rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	background: #f8f9fa;
	border-radius: 12rpx;
	font-size: 28rpx;
	color: #666;
	border: 2rpx solid transparent;
}

.type-btn.active {
	background: #f0eeff;
	color: #6c5ce7;
	border-color: #6c5ce7;
}

.submit-bar {
	position: fixed;
	left: 0;
	right: 0;
	bottom: 0;
	padding: 20rpx 32rpx;
	padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
	background: #fff;
	box-shadow: 0 -2rpx 20rpx rgba(0, 0, 0, 0.05);
}

.submit-btn {
	height: 88rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	background: #6c5ce7;
	color: #fff;
	font-size: 32rpx;
	border-radius: 44rpx;
}

.submit-btn[disabled] {
	background: #ccc;
}

.form-optional {
	font-size: 24rpx;
	color: #999;
	font-weight: normal;
}

.image-uploader {
	display: flex;
	gap: 20rpx;
}

.upload-btn {
	width: 200rpx;
	height: 200rpx;
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
	font-size: 24rpx;
	color: #999;
}

.image-box {
	width: 200rpx;
	height: 200rpx;
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
</style>