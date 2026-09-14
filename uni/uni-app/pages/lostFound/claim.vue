<template>
	<view class="page-wrap">
		<view class="item-card">
			<view class="item-head">
				<text class="item-title">{{ item.title || '失物招领' }}</text>
				<text class="item-badge">{{ applyType === 'claim' ? '申请领取' : '申请归还' }}</text>
			</view>
			<text class="item-desc">
				{{ item.description || '请填写真实信息，发布方同意后双方可查看联系方式。' }}
			</text>
			<text class="item-meta">地点 {{ item.location || '未知地点' }}</text>
		</view>

		<view class="form-wrap">
			<text class="form-title">{{ applyType === 'claim' ? '领取信息' : '归还信息' }}</text>
			<view class="form-group">
				<text class="field-label">姓名</text>
				<input v-model="form.name" class="field-input" placeholder="请输入真实姓名" maxlength="20" />
			</view>
			<view class="form-group">
				<text class="field-label">手机号</text>
				<input v-model="form.phone" class="field-input" type="number" maxlength="11" placeholder="请输入手机号" />
			</view>
			<view class="form-group">
				<text class="field-label">学号</text>
				<input v-model="form.studentId" class="field-input" placeholder="请输入学号" maxlength="30" />
			</view>
			<view class="form-group" v-if="applyType === 'claim'">
				<text class="field-label">身份证号</text>
				<input v-model="form.idCard" class="field-input" maxlength="18" placeholder="请输入身份证号" />
			</view>
			<button class="submit-btn" :disabled="submitting" @click="submitApply">
				{{ applyType === 'claim' ? '提交领取申请' : '提交归还申请' }}
			</button>
			<text class="notice-text">申请提交后发布方会收到通知；同意后双方可相互查看手机号、姓名和学号。</text>
		</view>
	</view>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import request from '@/utils/request.js'
import { getCurrentUser } from '@/utils/auth.js'

const itemId = ref('')
const applyType = ref('claim')
const item = ref({})
const submitting = ref(false)
const form = reactive({
	name: '',
	phone: '',
	studentId: '',
	idCard: ''
})

function currentUid() {
	return getCurrentUser()?.uid || ''
}

async function loadItem() {
	if (!itemId.value) return
	const res = await request({
		url: `/lost-found/get/${itemId.value}`,
		method: 'GET',
		data: { userUid: currentUid() }
	})
	if (res.code === 1) item.value = res.data || {}
}

function toast(title) {
	uni.showToast({ title, icon: 'none' })
	return false
}

function validate() {
	if (!currentUid()) {
		uni.navigateTo({ url: '/pages/login/login' })
		return false
	}
	if (!form.name.trim()) return toast('请输入真实姓名')
	if (!form.phone.trim()) return toast('请输入手机号')
	if (!/^1[3-9]\d{9}$/.test(form.phone.trim())) return toast('手机号格式不正确')
	if (!form.studentId.trim()) return toast('请输入学号')
	if (applyType.value === 'claim' && !/^\d{17}[\dXx]$/.test(form.idCard.trim())) {
		return toast('请输入正确的身份证号')
	}
	return true
}

async function submitApply() {
	if (!validate() || submitting.value) return
	submitting.value = true
	try {
		const res = await request({
			url: '/lost-found/apply',
			method: 'POST',
			data: {
				itemId: Number(itemId.value),
				applyType: applyType.value,
				userUid: currentUid(),
				name: form.name.trim(),
				phone: form.phone.trim(),
				studentId: form.studentId.trim(),
				idCard: applyType.value === 'claim' ? form.idCard.trim() : ''
			}
		})
		if (res.code === 1) {
			uni.showToast({ title: '申请已提交', icon: 'success' })
			setTimeout(() => uni.navigateBack(), 500)
		} else {
			uni.showToast({ title: res.msg || '申请失败', icon: 'none' })
		}
	} finally {
		submitting.value = false
	}
}

onLoad((query) => {
	itemId.value = query.itemId || ''
	applyType.value = query.applyType === 'return' ? 'return' : 'claim'
	loadItem()
})
</script>

<style scoped>
.page-wrap {
	min-height: 100vh;
	background: #f5f6f9;
	padding: 24rpx 28rpx 60rpx;
	box-sizing: border-box;
}

.item-card,
.form-wrap {
	background: #ffffff;
	border-radius: 8rpx;
	padding: 28rpx;
	margin-bottom: 24rpx;
	border: 1rpx solid #edf0f5;
}

.item-head {
	display: flex;
	align-items: center;
	justify-content: space-between;
	gap: 20rpx;
}

.item-title {
	flex: 1;
	font-size: 34rpx;
	font-weight: 700;
	color: #1f2933;
	line-height: 1.35;
}

.item-badge {
	flex-shrink: 0;
	background: #e3f4f1;
	color: #237d72;
	font-size: 22rpx;
	padding: 8rpx 18rpx;
	border-radius: 8rpx;
}

.item-desc {
	display: block;
	margin-top: 16rpx;
	font-size: 26rpx;
	color: #667085;
	line-height: 1.5;
}

.item-meta {
	display: block;
	margin-top: 12rpx;
	font-size: 24rpx;
	color: #98a2b3;
}

.form-title {
	display: block;
	margin-bottom: 28rpx;
	font-size: 32rpx;
	font-weight: 700;
	color: #1f2933;
}

.form-group {
	margin-bottom: 24rpx;
}

.field-label {
	display: block;
	margin-bottom: 10rpx;
	font-size: 24rpx;
	color: #667085;
}

.field-input {
	width: 100%;
	height: 84rpx;
	background: #f7f8fa;
	border-radius: 8rpx;
	padding: 0 20rpx;
	box-sizing: border-box;
	font-size: 28rpx;
	color: #222222;
}

.submit-btn {
	margin-top: 16rpx;
	height: 88rpx;
	line-height: 88rpx;
	border-radius: 8rpx;
	background: #3d7a72;
	color: #ffffff;
	font-size: 30rpx;
}

.submit-btn[disabled] {
	opacity: 0.55;
}

.notice-text {
	display: block;
	text-align: center;
	margin-top: 20rpx;
	font-size: 22rpx;
	color: #98a2b3;
	line-height: 1.5;
}
</style>
