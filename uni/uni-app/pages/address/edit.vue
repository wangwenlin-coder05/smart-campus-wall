<template>
	<view class="page">
		<view class="form-card">
			<view class="form-item">
				<text class="label">收货人</text>
				<input v-model="form.receiverName" placeholder="请输入姓名" class="input" />
			</view>
			<view class="form-item">
				<text class="label">手机号码</text>
				<input v-model="form.receiverPhone" placeholder="请输入11位手机号" maxlength="11" type="number"
					class="input" />
			</view>
			<view class="form-item">
				<text class="label">地址类型</text>
				<view class="type-group">
					<view class="type" :class="{ active: form.addressType === 1 }" @click="form.addressType = 1">校内
					</view>
					<view class="type" :class="{ active: form.addressType === 2 }" @click="form.addressType = 2">校外
					</view>
				</view>
			</view>
			<!-- 详细地址改为单行输入，限制100字，自动过滤换行 -->
			<view class="form-item">
				<text class="label">详细地址</text>
				<textarea v-model="form.address" placeholder="校区＋楼栋＋宿舍号" maxlength="100" class="textarea"
					@input="onAddressInput" />
			</view>
			<view class="form-item switch-item">
				<text class="label">设为默认地址</text>
				<switch :checked="form.isDefault === 1" @change="e => form.isDefault = e.detail.value ? 1 : 0" :active-color="'#333'" />
			</view>
		</view>

		<view class="submit-btn" @click="submit">保存</view>
	</view>
</template>

<script setup>
	import {
		ref,
		onMounted
	} from 'vue'
	import request from '@/utils/request.js'
	import { getCurrentUser } from '@/utils/auth.js'

	const currentUid = ref('')
	const editId = ref(null)

	const form = ref({
		receiverName: '',
		receiverPhone: '',
		addressType: 1,
		address: '',
		isDefault: 0
	})

	const syncCurrentUser = () => {
		const user = getCurrentUser()
		if (!user || !user.uid) {
			uni.navigateTo({ url: '/pages/login/login' })
			return false
		}
		currentUid.value = user.uid
		return true
	}

	// 处理地址输入，移除换行符确保单行
	const onAddressInput = (e) => {
		form.value.address = e.detail.value.replace(/[\r\n]/g, '')
	}

	onMounted(() => {
		if (!syncCurrentUser()) return
		const pages = getCurrentPages()
		const options = pages[pages.length - 1]?.options
		if (options?.addressNo) {
			editId.value = options.addressNo
			fetchAddressDetail(options.addressNo)
		}
		uni.setNavigationBarTitle({
			title: editId.value ? '编辑收货地址' : '新增收货地址'
		})
	})

	const fetchAddressDetail = async (id) => {
		const res = await request({
			url: `/address/getByNo/${currentUid.value}/${id}`,
			method: 'GET'
		})
		if (res.code === 1) {
			const data = res.data
			form.value = {
				addressNo: data.addressNo,
				receiverName: data.receiverName || '',
				receiverPhone: data.receiverPhone || '',
				addressType: data.addressType || 1,
				address: data.address || '',
				isDefault: data.isDefault || 0
			}
		} else {
			uni.showToast({
				title: '地址加载失败',
				icon: 'none'
			})
		}
	}

	const goBack = () => uni.navigateBack()

	const submit = async () => {
		if (!syncCurrentUser()) return
		if (!form.value.receiverName) return uni.showToast({
			title: '请填写收货人',
			icon: 'none'
		})
		if (!form.value.receiverPhone || form.value.receiverPhone.length !== 11) return uni.showToast({
			title: '手机号格式错误',
			icon: 'none'
		})
		if (!form.value.address) return uni.showToast({
			title: '请填写详细地址',
			icon: 'none'
		})

		const url = editId.value ? `/address/updateByUid/${currentUid.value}` : `/address/addByUid/${currentUid.value}`
		const method = editId.value ? 'PUT' : 'POST'
		const data = editId.value ? {
			...form.value,
			addressNo: editId.value
		} : form.value

		const res = await request({
			url,
			method,
			data
		})
		if (res.code === 1) {
			uni.showToast({
				title: editId.value ? '修改成功' : '添加成功'
			})
			setTimeout(() => uni.navigateBack(), 700)
		} else {
			uni.showToast({
				title: '操作失败',
				icon: 'none'
			})
		}
	}
</script>

<style scoped>
	page {
		background-color: #f5f5f5;
	}

	.page {
		min-height: 100vh;
	}

	.form-card {
		margin: 20rpx;
		background: #fff;
		border-radius: 24rpx;
		padding: 0 24rpx;
	}

	.form-item {
		display: flex;
		align-items: center;
		padding: 30rpx 0;
		border-bottom: 1rpx solid #f0f0f0;
	}

	.form-item:last-child {
		border-bottom: none;
	}

	.switch-item {
		justify-content: space-between;
	}

	.label {
		width: 160rpx;
		font-size: 28rpx;
		color: #333;
		font-weight: 500;
	}

	.input {
		flex: 1;
		font-size: 28rpx;
		color: #333;
	}

	/* 地址类型选择 */
	.type-group {
		display: flex;
		gap: 20rpx;
	}

	.type {
		padding: 8rpx 28rpx;
		border: 1rpx solid #ddd;
		border-radius: 30rpx;
		font-size: 26rpx;
		color: #666;
	}

	.type.active {
		background: #333;
		color: #fff;
		border-color: #333;
	}

	.textarea {
		flex: 1;
		font-size: 28rpx;
		min-height: 160rpx;
		/* 增大输入区域 */
		padding: 10rpx 0;
		line-height: 1.5;
	}

	.submit-btn {
		margin: 40rpx;
		background: #333;
		color: #fff;
		text-align: center;
		padding: 26rpx;
		border-radius: 40rpx;
		font-size: 30rpx;
		font-weight: 500;
		letter-spacing: 2rpx;
	}

	.submit-btn:active {
		opacity: 0.85;
	}
</style>
