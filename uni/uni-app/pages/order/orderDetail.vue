<template>
	<view class="page">
		<!-- 订单内容 -->
		<scroll-view class="body-scroll" scroll-y :show-scrollbar="false" :enhanced="true">
			<view class="loading-text" v-if="loading">加载中...</view>
			<view class="loading-text" v-else-if="!orderData">未找到订单信息</view>
			<template v-else>
			<view class="order-card">
				<view class="card-header">
					<view class="order-no-row">
						<text class="no-text">订单号：{{ orderData.orderNo }}</text>
						<text class="copy-btn" @click="copyOrderNo(orderData.orderNo)">复制</text>
					</view>
					<view class="status-tag" :style="{
						backgroundColor: getStatusBg(orderData.orderStatus),
						color: getStatusColor(orderData.orderStatus)
					}">
						{{ getStatusText(orderData.orderStatus) }}
					</view>
				</view>
				<view class="category-row">
					<view class="tags">
						<text class="parent-tag" :style="{ background: getParentTypeColor(orderData.parentType) }">
							{{ orderData.parentType }}
						</text>
						<text class="sub-tag">{{ orderData.subType }}</text>
					</view>
					<text class="expect-time" v-if="orderData.expectTime">希望送达时间：{{ orderData.expectTime }}</text>
				</view>
				<view class="address-block">
					<view class="addr-line">
						<view class="addr-icon pickup-icon">取</view>
						<text class="addr-text">{{ orderData.startAddressText }}</text>
					</view>
					<view class="addr-line">
						<view class="addr-icon delivery-icon">送</view>
						<text class="addr-text">{{ orderData.endAddressText }}</text>
					</view>
				</view>
				<view class="extra-info">
					<view class="status-tip" v-if="statusTip.show">
						<text class="status-title">最新动态：{{ statusTip.title }}</text>
						<text class="status-desc" v-if="statusTip.desc">{{ statusTip.desc }}</text>
					</view>
					<view class="fetch-code-row" v-if="orderData.fetchCode">
						<view class="code-tag">
							<text class="code-label">取件码/物品</text>
							<text class="code-value">{{ orderData.fetchCode }}</text>
						</view>
					</view>
					<view class="remark-row" v-if="orderData.remark">
						<text class="remark-text">备注：{{ orderData.remark }}</text>
					</view>
				</view>
				<view class="card-footer">
					<view class="footer-left">
						<text class="time-text">{{ formatOrderTime(orderData.createTime) }}</text>
						<text class="price-tag" :class="orderData.payStatus === 1 ? 'paid' : 'unpaid'">
							{{ orderData.payStatus === 1 ? '实付' : '待支付' }}：¥{{ formatPrice(orderData.orderPrice) }}
						</text>
					</view>
					<view class="footer-actions">
						<view class="action-btn cancel-btn"
							v-if="orderData.orderStatus < 2"
							@click="openCancelDialog">取消订单</view>
						<view class="action-btn confirm-btn"
							v-if="orderData.orderStatus === 3"
							@click="confirmReceive">确认收货</view>
					</view>
				</view>
			</view>

			<!-- 订单轨迹 -->
			<view class="timeline-section" v-if="lifecycleList.length">
				<view class="timeline-title">订单轨迹</view>
				<view class="timeline-list">
					<view class="timeline-item" v-for="(item, idx) in lifecycleList" :key="idx"
						:class="{ active: idx === 0 }">
						<view class="timeline-dot" :class="{ current: idx === 0 }"></view>
						<view class="timeline-content">
							<text class="timeline-item-title">{{ item.title }}</text>
							<text class="timeline-item-desc" v-if="item.desc">{{ item.desc }}</text>
							<text class="timeline-item-time">{{ item.time }}</text>
						</view>
					</view>
				</view>
			</view>

			<!-- 取消订单弹窗 -->
			<view class="dialog-mask" v-if="showCancelDialog" @click.self="closeCancelDialog">
				<view class="dialog-card" @click.stop>
					<text class="dialog-title">取消订单</text>
					<text class="dialog-subtitle">请选择或输入取消原因</text>

					<view class="reason-list">
						<view class="reason-item"
							v-for="reason in cancelReasonOptions" :key="reason"
							:class="{ active: selectedCancelReason === reason }"
							@click="pickCancelReason(reason)">
							<text>{{ reason }}</text>
						</view>
					</view>

					<textarea class="dialog-input" v-model="customCancelReason"
						placeholder="也可以输入其他原因" placeholder-class="reason-placeholder"
						:maxlength="100" :auto-height="true" />

					<view class="dialog-actions">
						<view class="dialog-btn cancel" @click="closeCancelDialog">取消</view>
						<view class="dialog-btn confirm" :class="{ disabled: submittingCancel }"
							@click="submitCancel">
							{{ submittingCancel ? '提交中...' : '确定' }}
						</view>
					</view>
				</view>
			</view>
			</template>
		</scroll-view>
	</view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import request from '@/utils/request.js'
import { getCurrentUser as getLoginUser } from '@/utils/auth.js'

const loading = ref(true)
const orderData = ref(null)
const currentUser = ref(null)
const orderNoParam = ref('')

const showCancelDialog = ref(false)
const selectedCancelReason = ref('')
const customCancelReason = ref('')
const submittingCancel = ref(false)
const cancelReasonOptions = [
	'临时有事，不需要了',
	'信息填写错误',
	'等待时间太长',
	'想重新下单'
]

function formatPrice(val) { return (val != null ? Number(val).toFixed(2) : '0.00') }
function formatOrderTime(timeStr) { return (timeStr || '').replace('T', ' ') }

function getStatusText(status) {
	const map = { 0: '待接单', 1: '已接单', 2: '配送中', 3: '已送达', 4: '纠纷中', 5: '已完成', 6: '已取消' }
	return map[status] || '未知'
}
function getStatusColor(status) {
	const map = { 0: '#ff9800', 1: '#2196f3', 2: '#4caf50', 3: '#9e9e9e', 4: '#f44336', 5: '#4caf50', 6: '#9e9e9e' }
	return map[status] || '#999'
}
function getStatusBg(status) {
	const map = { 0: '#fff3e0', 1: '#e3f2fd', 2: '#e8f5e9', 3: '#f5f5f5', 4: '#ffebee', 5: '#e8f5e9', 6: '#f5f5f5' }
	return map[status] || '#f5f5f5'
}
function getParentTypeColor(type) {
	const map = { '快递': '#e3f2fd', '文件': '#fff3e0', '外卖': '#fce4ec', '代买': '#e8f5e9', '跑腿': '#f3e5f5', '搬运': '#e1f5fe', '其他': '#f5f5f5' }
	return map[type] || '#f5f5f5'
}

const lifecycleList = computed(() => {
	const raw = orderData.value?.lifecycleLog
	if (!raw) return []
	return raw.split('\n').filter(Boolean).map(line => {
		const [time, title, desc] = line.split('|')
		return { time: time || '', title: title || '', desc: desc || '' }
	}).reverse()
})

const statusTip = computed(() => {
	const o = orderData.value
	if (!o) return { show: false, title: '', desc: '' }
	const s = o.orderStatus
	const rj = o.riderCancelReason
	if (s === 6) return { show: true, title: '订单已取消', desc: o.cancelReason ? '理由：' + o.cancelReason : '' }
	if (s === 5) return { show: true, title: '订单已完成', desc: '' }
	if (s === 4) return { show: true, title: '纠纷处理中', desc: '' }
	if (s === 3) return { show: true, title: '已送达', desc: '' }
	if (s === 2) return { show: true, title: rj ? '骑手已拒绝取消' : '骑手已取件，配送中', desc: rj ? '理由：' + rj : '' }
	if (s === 1) return { show: true, title: rj ? '骑手已拒绝取消' : '骑手已接单', desc: rj ? '理由：' + rj : '' }
	if (s === 0) return { show: true, title: '等待骑手接单', desc: '' }
	return { show: false, title: '', desc: '' }
})

function copyOrderNo(no) {
	uni.setClipboardData({ data: no })
	uni.showToast({ title: '已复制', icon: 'none' })
}

function openCancelDialog() {
	selectedCancelReason.value = ''
	customCancelReason.value = ''
	showCancelDialog.value = true
}

function pickCancelReason(reason) {
	selectedCancelReason.value = reason
	customCancelReason.value = ''
}

function closeCancelDialog() {
	if (submittingCancel.value) return
	showCancelDialog.value = false
	selectedCancelReason.value = ''
	customCancelReason.value = ''
}

async function submitCancel() {
	if (submittingCancel.value) return
	const reason = (customCancelReason.value || selectedCancelReason.value).trim()
	if (!reason) {
		uni.showToast({ title: '请选择或输入取消原因', icon: 'none' })
		return
	}
	submittingCancel.value = true
	try {
		const result = await request({
			url: '/order/cancelOrder',
			method: 'POST',
			data: { orderNo: orderData.value.orderNo, cancelReason: reason }
		})
		if (result.code === 1) {
			showCancelDialog.value = false
			selectedCancelReason.value = ''
			customCancelReason.value = ''
			uni.showToast({ title: result.msg || '申请已提交', icon: 'success' })
			orderData.value = { ...orderData.value, orderStatus: 4 }
		} else {
			uni.showToast({ title: result.msg || '取消失败', icon: 'none' })
		}
	} catch (e) {
		uni.showToast({ title: '操作失败', icon: 'none' })
	} finally {
		submittingCancel.value = false
	}
}

async function confirmReceive() {
	const res = await uni.showModal({ title: '确认收货', content: '确认已收到物品吗？' })
	if (!res.confirm) return
	try {
		const result = await request({
			url: '/order/confirmReceive',
			method: 'POST',
			data: { orderNo: orderData.value.orderNo }
		})
		if (result.code === 1) {
			uni.showToast({ title: '确认成功', icon: 'success' })
			orderData.value = { ...orderData.value, orderStatus: 5 }
		} else {
			uni.showToast({ title: result.msg || '操作失败', icon: 'none' })
		}
	} catch (e) {
		uni.showToast({ title: '操作失败', icon: 'none' })
	}
}

async function loadOrder() {
	const user = getLoginUser()
	if (!user || !user.uid) {
		uni.showToast({ title: '请先登录', icon: 'none' })
		loading.value = false
		return
	}
	currentUser.value = user
	loading.value = true
	try {
		const res = await request({
			url: `/order/userUid/${user.uid}/page`,
			method: 'GET',
			data: { pageNum: 1, pageSize: 100 }
		})
		if (res.code === 1 && Array.isArray(res.data.list)) {
			const found = res.data.list.find(o => String(o.orderNo) === String(orderNoParam.value))
			if (found) {
				orderData.value = found
			} else {
				uni.showToast({ title: '未找到该订单', icon: 'none' })
			}
		} else {
			uni.showToast({ title: res.msg || '加载失败', icon: 'none' })
		}
	} catch (e) {
		console.error('loadOrder error:', e)
		uni.showToast({ title: '网络异常，请重试', icon: 'none' })
	} finally {
		loading.value = false
	}
}

onLoad((options) => {
	orderNoParam.value = options.orderNo || ''
	if (!orderNoParam.value) {
		uni.showToast({ title: '缺少订单号', icon: 'none' })
		setTimeout(() => uni.navigateBack(), 800)
		return
	}
	loadOrder()
})
</script>

<style scoped>
.page {
	min-height: 100vh;
	height: 100vh;
	display: flex;
	flex-direction: column;
	background: #f4f6f8;
}

.body-scroll {
	flex: 1;
	padding: 24rpx;
	box-sizing: border-box;
}

.loading-text {
	display: block;
	text-align: center;
	padding: 80rpx 0;
	color: #9aa3af;
	font-size: 28rpx;
}

.order-card {
	background: #ffffff;
	border-radius: 20rpx;
	padding: 24rpx;
}

.card-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 0 0 12rpx;
}

.order-no-row {
	display: flex;
	align-items: center;
	gap: 10rpx;
}

.no-text {
	font-size: 24rpx;
	color: #6a7484;
	font-weight: 600;
}

.copy-btn {
	font-size: 22rpx;
	color: #3677ff;
	padding: 4rpx 12rpx;
	background: #eef3ff;
	border-radius: 8rpx;
}

.status-tag {
	padding: 6rpx 16rpx;
	border-radius: 10rpx;
	font-size: 22rpx;
	font-weight: 700;
}

.category-row {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 10rpx 0 16rpx;
	border-bottom: 1rpx solid #f0f2f5;
}

.tags {
	display: flex;
	gap: 8rpx;
	align-items: center;
}

.parent-tag {
	padding: 4rpx 12rpx;
	border-radius: 8rpx;
	font-size: 22rpx;
	font-weight: 700;
	color: #314253;
}

.sub-tag {
	font-size: 22rpx;
	color: #6a7484;
}

.expect-time {
	font-size: 21rpx;
	color: #8a94a6;
}

.address-block {
	padding: 18rpx 0 14rpx;
	border-bottom: 1rpx solid #f0f2f5;
}

.addr-line {
	display: flex;
	align-items: center;
	margin-bottom: 10rpx;
}

.addr-line:last-child {
	margin-bottom: 0;
}

.addr-icon {
	width: 36rpx;
	height: 36rpx;
	border-radius: 8rpx;
	font-size: 22rpx;
	font-weight: 800;
	color: #ffffff;
	display: flex;
	align-items: center;
	justify-content: center;
	margin-right: 12rpx;
	flex-shrink: 0;
}

.pickup-icon {
	background: #4caf50;
}

.delivery-icon {
	background: #ff9800;
}

.addr-text {
	font-size: 26rpx;
	color: #1a1d21;
	line-height: 1.4;
}

.extra-info {
	padding: 14rpx 0;
}

.status-tip {
	background: #fff3e0;
	padding: 14rpx 16rpx;
	border-radius: 12rpx;
	margin-bottom: 12rpx;
}

.status-title {
	font-size: 24rpx;
	font-weight: 700;
	color: #e65100;
	display: block;
}

.status-desc {
	font-size: 22rpx;
	color: #bf360c;
	margin-top: 6rpx;
	display: block;
}

.fetch-code-row {
	margin-bottom: 10rpx;
}

.code-tag {
	display: flex;
	align-items: center;
	gap: 12rpx;
	padding: 10rpx 16rpx;
	background: #f5f6f8;
	border-radius: 10rpx;
}

.code-label {
	font-size: 22rpx;
	color: #8a94a6;
}

.code-value {
	font-size: 26rpx;
	font-weight: 700;
	color: #1a1d21;
}

.remark-row {
	margin-top: 8rpx;
}

.remark-text {
	font-size: 23rpx;
	color: #6a7484;
}

.card-footer {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 16rpx 0 0;
	border-top: 1rpx solid #f0f2f5;
}

.footer-left {
	display: flex;
	flex-direction: column;
	gap: 6rpx;
}

.time-text {
	font-size: 22rpx;
	color: #9aa3af;
}

.price-tag {
	font-size: 24rpx;
	font-weight: 700;
}

.price-tag.paid {
	color: #16a34a;
}

.price-tag.unpaid {
	color: #ff6b3d;
}

.footer-actions {
	display: flex;
	gap: 12rpx;
	align-items: center;
}

.action-btn {
	height: 56rpx;
	padding: 0 20rpx;
	border-radius: 28rpx;
	font-size: 24rpx;
	font-weight: 700;
	display: flex;
	align-items: center;
	justify-content: center;
}

.cancel-btn {
	background: #fef0f0;
	color: #e53935;
}

.confirm-btn {
	background: #e8f5e9;
	color: #16a34a;
}

.timeline-section {
	margin-top: 28rpx;
	background: #ffffff;
	border-radius: 20rpx;
	padding: 24rpx;
}

.timeline-title {
	font-size: 28rpx;
	font-weight: 700;
	color: #1f2933;
	margin-bottom: 20rpx;
}

.timeline-list {
	position: relative;
	padding-left: 32rpx;
}

.timeline-list::before {
	content: '';
	position: absolute;
	left: 10rpx;
	top: 8rpx;
	bottom: 0;
	width: 2rpx;
	background: #e5e7eb;
}

.timeline-item {
	position: relative;
	padding-bottom: 28rpx;
}

.timeline-item:last-child {
	padding-bottom: 0;
}

.timeline-dot {
	position: absolute;
	left: -29rpx;
	top: 6rpx;
	width: 16rpx;
	height: 16rpx;
	border-radius: 50%;
	background: #d1d5db;
	border: 3rpx solid #ffffff;
	box-shadow: 0 0 0 2rpx #d1d5db;
}

.timeline-dot.current {
	background: #3677ff;
	box-shadow: 0 0 0 2rpx #3677ff;
}

.timeline-item.active .timeline-content {
	color: #1a1d21;
}

.timeline-content {
	display: flex;
	flex-direction: column;
	gap: 4rpx;
}

.timeline-item-title {
	font-size: 26rpx;
	font-weight: 600;
	color: #314253;
}

.timeline-item-desc {
	font-size: 22rpx;
	color: #6a7484;
	line-height: 1.5;
}

.timeline-item-time {
	font-size: 20rpx;
	color: #9aa3af;
}

.dialog-mask {
	position: fixed;
	inset: 0;
	background: rgba(0, 0, 0, 0.45);
	display: flex;
	align-items: center;
	justify-content: center;
	z-index: 1000;
}

.dialog-card {
	width: 600rpx;
	background: #ffffff;
	border-radius: 20rpx;
	padding: 36rpx;
}

.dialog-title {
	font-size: 32rpx;
	font-weight: 700;
	color: #1f2933;
	display: block;
	margin-bottom: 8rpx;
}

.dialog-subtitle {
	font-size: 24rpx;
	color: #888;
	display: block;
	margin-bottom: 22rpx;
}

.reason-list {
	display: flex;
	flex-wrap: wrap;
	gap: 14rpx;
	margin-bottom: 18rpx;
}

.reason-item {
	padding: 10rpx 20rpx;
	border-radius: 30rpx;
	font-size: 24rpx;
	color: #666;
	background: #fafafa;
	border: 1rpx solid #ddd;
}

.reason-item.active {
	color: #e74c3c;
	background: #fff5f5;
	border-color: #e74c3c;
}

.dialog-input {
	width: 100%;
	min-height: 120rpx;
	padding: 16rpx;
	background: #fafafa;
	border-radius: 12rpx;
	font-size: 26rpx;
	color: #1a1d21;
	box-sizing: border-box;
	margin-bottom: 24rpx;
}

.reason-placeholder {
	color: #aaa;
	font-size: 26rpx;
}

.dialog-actions {
	display: flex;
	gap: 16rpx;
	justify-content: flex-end;
}

.dialog-btn {
	min-width: 132rpx;
	height: 64rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	border-radius: 32rpx;
	font-size: 26rpx;
	font-weight: 700;
}

.dialog-btn.cancel {
	background: #f2f3f5;
	color: #6a7484;
}

.dialog-btn.confirm {
	background: #3677ff;
	color: #ffffff;
}

.dialog-btn.disabled {
	opacity: 0.5;
	pointer-events: none;
}
</style>