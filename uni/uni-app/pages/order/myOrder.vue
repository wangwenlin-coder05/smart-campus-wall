<template>
	<view class="order-list-page">
		<!-- 顶部标题 -->
		<view class="header">
			<text class="title">我的订单</text>
		</view>

		<!-- 可滚动列表 -->
		<scroll-view class="scroll-container" scroll-y @scroll="onScroll">
			<view class="list-container">
				<view class="order-card" v-for="(order, index) in orderList" :key="order.orderNo"
					:id="`order-${order.orderNo}`"
					:style="{ animationDelay: index * 0.03 + 's' }">
					<!-- 1. 卡片头部区域 -->
					<view class="card-header">
						<view class="order-no-row">
							<text class="no-text">订单号：{{ order.orderNo }}</text>
							<text class="copy-btn" @click="copyOrderNo(order.orderNo)">复制</text>
						</view>
						<view class="status-tag" :style="{
                backgroundColor: getStatusBg(order.orderStatus),
                color: getStatusColor(order.orderStatus)
              }">
							{{ getStatusText(order.orderStatus) }}
						</view>
					</view>

					<!-- 第二行：分类 + 期望时间 -->
					<view class="category-row">
						<view class="tags">
							<text class="parent-tag" :style="{ background: getParentTypeColor(order.parentType) }">
								{{ order.parentType }}
							</text>
							<text class="sub-tag">{{ order.subType }}</text>
						</view>
						<text class="expect-time" v-if="order.expectTime">
							希望送达时间：{{ order.expectTime }}
						</text>
					</view>

					<!-- 2. 地址信息区域 -->
					<view class="address-block">
						<view class="addr-line">
							<view class="addr-icon pickup-icon">取</view>
							<text class="addr-text">{{ order.startAddressText }}</text>
						</view>
						<view class="addr-line">
							<view class="addr-icon delivery-icon">送</view>
							<text class="addr-text">{{ order.endAddressText }}</text>
						</view>
					</view>

					<!-- 3. 附加信息区域 -->
					<view class="extra-info">
						<view class="status-tip" v-if="getStatusTip(order).show">
							<text class="status-title">最新动态：{{ getStatusTip(order).title }}</text>
							<text class="status-desc" v-if="getStatusTip(order).desc">{{ getStatusTip(order).desc }}</text>
						</view>

						<view class="fetch-code-row" v-if="order.fetchCode">
							<view class="code-tag">
								<text class="code-label">取件码/物品</text>
								<text class="code-value">{{ order.fetchCode }}</text>
							</view>
						</view>

						<view class="remark-row" v-if="order.remark">
							<text class="remark-text">备注：{{ order.remark }}</text>
						</view>

						<view v-if="order.orderImageList && order.orderImageList.length > 0" class="image-area">
							<view class="image-toggle" @click="toggleImages(order.orderNo)">
								<text class="toggle-text">
									{{ expandedImages[order.orderNo] ? '隐藏图片' : '展开图片' }}
								</text>
								<text class="toggle-arrow">
									{{ expandedImages[order.orderNo] ? '▲' : '▼' }}
								</text>
							</view>
							<view class="image-grid" v-if="expandedImages[order.orderNo]">
								<image v-for="(img, imgIdx) in order.orderImageList" :key="imgIdx" :src="img"
									mode="aspectFill" class="grid-image"
									@click.stop="openPreview(order.orderImageList, imgIdx)"></image>
							</view>
						</view>
					</view>

					<!-- 4. 底部状态栏 -->
					<view class="card-footer">
						<view class="footer-left">
							<text class="time-text">{{ formatTime(order.createTime) }}</text>
							<text class="price-tag" :class="order.payStatus === 1 ? 'paid' : 'unpaid'">
								{{ order.payStatus === 1 ? '实付' : '待支付' }}：¥{{
                  formatPrice(order.orderPrice)
                }}
							</text>
						</view>
						<view class="footer-actions">
							<view class="action-btn cancel-btn" v-if="order.orderStatus < 2"
								@click="openCancelDialog(order)">
								取消订单
							</view>
							<view class="action-btn confirm-btn" v-if="order.orderStatus > 2 && order.orderStatus < 5"
								@click="confirmReceive(order)">
								确认收货
							</view>
							<!-- 查看详情 改为 展开时间明细 -->
							<view class="action-btn detail-btn" @click="toggleTimeDetail(order.orderNo)">
								查看详情
							</view>
						</view>
					</view>

					<!-- 5. 时间明细折叠区域（点击查看详情后展开） -->
					<view class="time-info-wrap" v-if="expandedTimes[order.orderNo]">
						<view class="timeline-content">
							<view class="timeline-item" v-for="(item, itemIndex) in getOrderTimeline(order)" :key="itemIndex">
								<view class="timeline-dot" :class="{ active: itemIndex === 0 }"></view>
								<view class="timeline-body">
									<view class="timeline-head">
										<text class="timeline-title">{{ item.title }}</text>
										<text class="timeline-time">{{ item.time }}</text>
									</view>
									<text class="timeline-desc">{{ item.desc }}</text>
								</view>
							</view>
						</view>
					</view>
				</view>

				<!-- 加载状态 -->
				<view class="load-more-status" v-if="loading">
					<view class="loading-animation"></view>
					<text>加载中...</text>
				</view>
				<view class="load-more-status" v-if="noMore && orderList.length > 0">
					— 没有更多了 —
				</view>
				<view class="empty" v-if="!loading && orderList.length === 0">
					暂无订单
				</view>
			</view>
		</scroll-view>

		<!-- 取消订单弹窗 -->
		<view class="cancel-mask" v-if="cancelDialogVisible" @click.self="closeCancelDialog">
			<view class="cancel-dialog">
				<view class="cancel-dialog-title">取消订单</view>
				<view class="cancel-dialog-subtitle">请选择或输入取消原因，剩余 {{ cancelRemainCount }} 次</view>

				<view class="reason-list">
					<view class="reason-item" v-for="reason in cancelReasonOptions" :key="reason"
						:class="{ active: cancelReason === reason }" @click="selectCancelReason(reason)">
						{{ reason }}
					</view>
				</view>

				<textarea class="reason-input" v-model="customCancelReason" maxlength="100"
					placeholder="也可以输入其他原因" placeholder-class="reason-placeholder" />

				<view class="cancel-dialog-actions">
					<view class="dialog-btn dialog-cancel-btn" @click="closeCancelDialog">取消</view>
					<view class="dialog-btn dialog-confirm-btn" :class="{ disabled: submittingCancel }"
						@click="submitCancelOrder">
						{{ submittingCancel ? '提交中...' : '确定' }}
					</view>
				</view>
			</view>
		</view>

		<!-- 图片预览遮罩（支持缩放） -->
		<view class="preview-mask" v-if="previewVisible" @click.self="closePreview">
			<swiper class="preview-swiper" :current="previewIndex" @change="onPreviewSwiperChange"
				:indicator-dots="true" indicator-color="rgba(255,255,255,0.5)" indicator-active-color="#fff"
				@touchmove.stop @mousedown.stop @mouseup.stop @click.stop>
				<swiper-item v-for="(url, idx) in previewUrls" :key="idx">
					<view class="zoom-container" :style="{ transform: `scale(${scale})` }" @touchstart="onTouchStart"
						@touchmove="onTouchMove" @touchend="onTouchEnd" @wheel="onWheel">
						<image :src="url" mode="aspectFit" class="preview-image" @click.stop="closePreview"></image>
					</view>
				</swiper-item>
			</swiper>
			<view class="close-btn" @click.stop="closePreview">✕</view>
		</view>
	</view>
</template>

<script setup>
	import {
		ref,
		computed,
		onMounted,
		onBeforeUnmount,
		nextTick
	} from 'vue'
	import {
		onBackPress,
		onShow,
		onLoad
	} from '@dcloudio/uni-app'
	import request from '@/utils/request.js'
	import { getCurrentUser } from '@/utils/auth.js'

	const orderList = ref([])
	const pageNum = ref(1)
	const pageSize = 10
	const total = ref(0)
	const loading = ref(false)
	const noMore = ref(false)
	const targetOrderNo = ref('')

	const previewVisible = ref(false)
	const previewUrls = ref([])
	const previewIndex = ref(0)

	const expandedImages = ref({})
	const expandedTimes = ref({}) // 时间明细展开状态

	const cancelDialogVisible = ref(false)
	const currentCancelOrder = ref(null)
	const cancelReason = ref('')
	const customCancelReason = ref('')
	const submittingCancel = ref(false)
	const submittingConfirm = ref(false)
	const cancelReasonOptions = [
		'临时有事，不需要了',
		'信息填写错误',
		'等待时间太长',
		'想重新下单'
	]
	const cancelRemainCount = computed(() => {
		const order = currentCancelOrder.value
		const used = order && order.cancelApplyCount != null ? Number(order.cancelApplyCount) : 0
		return Math.max(0, 5 - used)
	})
	const isCancelRejected = (order) => {
		return !!(order && order.riderCancelReason && order.cancelReason && order.orderStatus !== 4 && order.orderStatus !== 6)
	}
	const getStatusTip = (order) => {
		if (!order) return { show: false, title: '', desc: '' }
		const s = order.orderStatus
		const rj = order.riderCancelReason
		if (s === 6) return { show: true, title: '订单已取消', desc: order.cancelReason ? '理由：' + order.cancelReason : '' }
		if (s === 5) return { show: true, title: '订单已完成', desc: '' }
		if (s === 4) return { show: true, title: '纠纷处理中', desc: '' }
		if (s === 3) return { show: true, title: '已送达', desc: '' }
		if (s === 2) return { show: true, title: rj ? '骑手已拒绝取消' : '骑手已取件，配送中', desc: rj ? '理由：' + rj : '' }
		if (s === 1) return { show: true, title: rj ? '骑手已拒绝取消' : '骑手已接单', desc: rj ? '理由：' + rj : '' }
		if (s === 0) return { show: true, title: '等待骑手接单', desc: '' }
		return { show: false, title: '', desc: '' }
	}
	const getOrderTimeline = (order) => {
		if (order.lifecycleLog) {
			const parsed = String(order.lifecycleLog)
				.split('\n')
				.map(line => {
					const parts = line.split('|')
					return {
						time: parts[0] || '暂无时间',
						title: parts[1] || '订单状态变化',
						desc: parts.slice(2).join('|') || ''
					}
				})
				.filter(item => item.title || item.desc)
			if (parsed.length) return parsed.reverse()
		}

		const items = []
		const push = (title, time, desc) => {
			items.push({
				title,
				time: formatTime(time) || '暂无时间',
				desc
			})
		}

		push('客户发布订单', order.createTime, `客户发布了${order.parentType || ''}${order.subType ? '-' + order.subType : ''}订单`)
		if (order.acceptTime) {
			push('骑手接单', order.acceptTime, '骑手接下订单，开始准备配送')
		}
		if (order.pickTime) {
			push('骑手取件', order.pickTime, '骑手已取到商品/物品')
		}
		if (order.deliverTime) {
			push('骑手送达', order.deliverTime, '骑手确认已送达')
		}
		if (order.cancelReason) {
			push('客户申请取消订单', order.updateTime, `客户申请取消订单，理由：${order.cancelReason}`)
		}
		if (order.riderCancelReason) {
			if (isCancelRejected(order)) {
				push('骑手不同意取消', order.updateTime, `骑手拒绝客户的取消申请，理由：${order.riderCancelReason}`)
			} else if (order.orderStatus === 6) {
				push('骑手同意取消', order.updateTime, `骑手同意客户取消，处理说明：${order.riderCancelReason}`)
			} else {
				push('骑手处理订单', order.updateTime, `骑手处理订单，说明：${order.riderCancelReason}`)
			}
		}
		if (order.finishTime) {
			push('客户确认完成', order.finishTime, '客户确认收货，订单完成')
		} else if (order.orderStatus === 6 && !order.riderCancelReason) {
			push('订单已取消', order.updateTime, `客户取消订单，理由：${order.cancelReason || '未填写'}`)
		}
		return items
	}

	const scale = ref(1)
	let lastDist = 0

	const getDistance = (touches) => {
		if (touches.length < 2) return 0
		const dx = touches[0].pageX - touches[1].pageX
		const dy = touches[0].pageY - touches[1].pageY
		return Math.sqrt(dx * dx + dy * dy)
	}

	const onTouchStart = (e) => {
		if (e.touches && e.touches.length === 2) {
			e.preventDefault()
			lastDist = getDistance(e.touches)
		}
	}

	const onTouchMove = (e) => {
		if (e.touches && e.touches.length === 2) {
			e.preventDefault()
			const currentDist = getDistance(e.touches)
			if (lastDist > 0) {
				const delta = currentDist / lastDist
				scale.value = Math.min(Math.max(scale.value * delta, 0.5), 4)
			}
			lastDist = currentDist
		}
	}

	const onTouchEnd = () => {
		lastDist = 0
	}

	const onWheel = (e) => {
		e.preventDefault()
		const delta = e.deltaY > 0 ? 0.95 : 1.05
		scale.value = Math.min(Math.max(scale.value * delta, 0.5), 4)
	}

	const fetchOrders = async () => {
		if (loading.value || noMore.value) return
		loading.value = true
		try {
			const currentUser = getCurrentUser()
			if (!currentUser || !currentUser.uid) {
				uni.navigateTo({ url: '/pages/login/login' })
				return
			}
			const res = await request({
				url: `/order/userUid/${currentUser.uid}/page`,
				method: 'GET',
				data: {
					pageNum: pageNum.value,
					pageSize: pageSize
				}
			})

			if (res.code === 1) {
				const list = res.data.list || []
				orderList.value = orderList.value.concat(list.filter(item => item))
				if (list.length < pageSize) {
					noMore.value = true
				} else {
					if (res.data.total) {
						total.value = Number(res.data.total) || 0
						noMore.value = orderList.value.length >= total.value
					} else {
						noMore.value = false
					}
				}

				if (targetOrderNo.value) {
					nextTick(() => {
						scrollToOrder(targetOrderNo.value)
					})
				}
			} else {
				uni.showToast({
					title: res.msg || '加载失败',
					icon: 'none'
				})
			}
		} catch (e) {
			uni.showToast({
				title: '网络异常',
				icon: 'none'
			})
		} finally {
			loading.value = false
		}
	}

	const scrollToOrder = (orderNo) => {
		uni.createSelectorQuery()
			.select(`#order-${orderNo}`)
			.boundingClientRect((rect) => {
				if (rect) {
					uni.pageScrollTo({
						scrollTop: rect.top - 100,
						duration: 300
					})
				}
			})
			.exec()
	}
// 上拉加载防抖锁，防止短时间多次触发加载
let loadMoreLock = false
/**
 * 页面滚动监听事件
 * 实现上拉触底加载更多订单
 * @param {Object} e - 滚动事件对象
 */
const onScroll = (e) => {
	// 获取滚动距离、滚动区域总高度
	const {
		scrollTop,
		scrollHeight
	} = e.detail
	// 计算距离底部剩余高度
	const remain = scrollHeight - scrollTop

	// 正在加载 / 无更多数据 / 加载锁定时，直接返回，不执行加载
	if (loading.value || noMore.value || loadMoreLock) return

	// 距离底部小于900px，触发加载更多
	if (remain < 900) {
		loadMoreLock = true // 开启加载锁，防止重复请求
		pageNum.value++     // 页码+1，请求下一页数据
		fetchOrders().finally(() => {
			// 请求结束（无论成功失败），解锁加载
			loadMoreLock = false
		})
	}
}

const reloadOrders = () => {
	orderList.value = []
	pageNum.value = 1
	total.value = 0
	noMore.value = false
	fetchOrders()
}

onLoad((options) => {
	if (options?.orderNo) {
		targetOrderNo.value = decodeURIComponent(options.orderNo)
	}
})

onShow(() => {
	reloadOrders()
})

/**
 * 复制订单编号到剪贴板
 * @param {String} orderNo - 订单编号
 */
const copyOrderNo = (orderNo) => {
	// uni-app 剪贴板API
	uni.setClipboardData({
		data: orderNo,
		// 复制成功后提示
		success: () => uni.showToast({
			title: '已复制',
			icon: 'success'
		})
	})
}


	const toggleImages = (orderNo) => {
		expandedImages.value[orderNo] = !expandedImages.value[orderNo]
	}

	const toggleTimeDetail = (orderNo) => {
		expandedTimes.value[orderNo] = !expandedTimes.value[orderNo]
	}

	const openPreview = (urls, current) => {
		previewUrls.value = urls
		previewIndex.value = current
		previewVisible.value = true
		scale.value = 1
		document.body.classList.add('preview-lock')
		// #ifdef H5
		history.pushState(null, '', location.href)
		// #endif
	}

	const closePreview = () => {
		previewVisible.value = false
		previewUrls.value = []
		previewIndex.value = 0
		scale.value = 1
		document.body.classList.remove('preview-lock')
	}

	const onPreviewSwiperChange = (e) => {
		previewIndex.value = e.detail.current
	}

	onBackPress((e) => {
		if (previewVisible.value) {
			closePreview()
			return true
		}
	})

	const handlePopState = () => {
		if (previewVisible.value) {
			closePreview()
			history.pushState(null, '', location.href)
		}
	}

	onMounted(() => {
		// #ifdef H5
		window.addEventListener('popstate', handlePopState)
		// #endif
	})

	onBeforeUnmount(() => {
		// #ifdef H5
		window.removeEventListener('popstate', handlePopState)
		// #endif
	})

// 打开取消订单弹窗，赋值当前订单信息、清空原因输入框
const openCancelDialog = (order) => {
	if ((Number(order.cancelApplyCount) || 0) >= 5) {
		uni.showToast({
			title: '客户最多只能取消5次',
			icon: 'none'
		})
		return
	}
	// 缓存当前要取消的订单信息
	currentCancelOrder.value = order
	// 清空下拉选择的取消原因
	cancelReason.value = ''
	// 清空自定义输入的补充原因
	customCancelReason.value = ''
	// 弹出取消订单弹窗
	cancelDialogVisible.value = true
}


	const closeCancelDialog = () => {
		if (submittingCancel.value) return

		currentCancelOrder.value = null
		cancelReason.value = ''
		customCancelReason.value = ''
		cancelDialogVisible.value = false
	}

	const selectCancelReason = (reason) => {
		cancelReason.value = reason
		customCancelReason.value = ''
	}
const submitCancelOrder = async () => {
	if (submittingCancel.value) return

	const order = currentCancelOrder.value
	const reason = (customCancelReason.value || cancelReason.value).trim()

	if (!reason) {
		uni.showToast({
			title: '请选择或输入取消原因',
			icon: 'none'
		})
		return
	}

	if (!order || !order.orderNo) {
		uni.showToast({
			title: '订单信息异常',
			icon: 'none'
		})
		return
	}

	submittingCancel.value = true
	try {
		const res = await request({
			url: '/order/cancelOrder',
			method: 'POST',
			data: {
				orderNo: order.orderNo,
				cancelReason: reason
			}
		})

		if (res.code === 1) {
			order.orderStatus = 4
			order.cancelReason = reason
			order.riderCancelReason = ''
			order.cancelApplyCount = (Number(order.cancelApplyCount) || 0) + 1
			//直接关闭弹窗
			cancelDialogVisible.value = false
			currentCancelOrder.value = null
			cancelReason.value = ''
			customCancelReason.value = ''
			uni.showToast({ title: res.msg || '取消成功', icon: 'success' })
		} else {
			cancelDialogVisible.value = false
			currentCancelOrder.value = null
			cancelReason.value = ''
			customCancelReason.value = ''
			uni.showToast({ title: res.msg || '取消失败', icon: 'none' })
		}
	} catch (e) {
		uni.showToast({
			title: '网络异常',
			icon: 'none'
		})
	} finally {
		submittingCancel.value = false
	}
}

	const confirmReceive = (order) => {
		if (submittingConfirm.value) return

		uni.showModal({
			title: '确认收货',
			content: '请确认已收到物品？',
			success: async (res) => {
				if (!res.confirm) return

				submittingConfirm.value = true
				try {
					const result = await request({
						url: '/order/confirmReceive',
						method: 'POST',
						data: {
							orderNo: order.orderNo
						}
					})

					if (result.code === 1) {
						order.orderStatus = 3
						uni.showToast({
							title: result.msg || '确认成功',
							icon: 'success'
						})
					} else {
						uni.showToast({
							title: result.msg || '确认失败',
							icon: 'none'
						})
					}
				} catch (e) {
					uni.showToast({
						title: '网络异常',
						icon: 'none'
					})
				} finally {
					submittingConfirm.value = false
				}
			}
		})
	}

	const formatPrice = (val) => (val != null ? Number(val).toFixed(2) : '0.00')
	const formatTime = (timeStr) => (timeStr || '').replace('T', ' ')
	const getParentTypeColor = (type) => {
		if (!type) return '#f5f5f5'
		if (type.includes('代取')) return '#e3f2fd'
		if (type.includes('帮买')) return '#fff3e0'
		if (type.includes('跑腿')) return '#e8f5e9'
		return '#f5f5f5'
	}
const getStatusText = (status) => {
	const map = {
		0: '待接单',
		1: '已接单',
		2: '配送中',
		3: '已送达',
		4: '纠纷中',
		5: '已完成',
		6: '已撤销'
	}
	return map[status] || '未知'
}
// 文字色
const getStatusColor = (status) => {
	const map = {
		0: '#FF7D00', // 橙
		1: '#1677FF', // 蓝
		2: '#00B42A', // 绿
		3: '#722ED1', // 紫
		4: '#F53F3F', // 红
		5: '#07C160', // 深绿
		6: '#86909C'  // 灰
	}
	return map[status] || '#666'
}
// 背景浅色
const getStatusBg = (status) => {
	const map = {
		0: '#FFF7E6',
		1: '#E8F3FF',
		2: '#E6FFEA',
		3: '#F7F0FF',
		4: '#FFECEC',
		5: '#F0FFF4',
		6: '#F2F3F5'
	}
	return map[status] || '#f5f5f5'
}

</script>

<style scoped>
	.order-list-page {
		background-color: #f5f6f8;
		position: fixed;
		top: 0;
		left: 0;
		right: 0;
		bottom: 0;
		display: flex;
		flex-direction: column;
	}

	.header {
		padding: 30rpx 32rpx 10rpx;
		flex-shrink: 0;
	}

	.title {
		font-size: 36rpx;
		font-weight: 700;
		color: #1a1a1a;
	}

	.scroll-container {
		flex: 1;
		height: 100%;
		overflow-y: auto;
	}

	.list-container {
		padding: 0 24rpx;
	}

	.order-card {
		background: #fff;
		border-radius: 24rpx;
		padding: 28rpx 24rpx;
		margin-bottom: 20rpx;
		border: 2rpx solid #e0e0e0;
		box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
		opacity: 0;
		transform: scale(0.85);
		animation: cardFadeIn 0.35s cubic-bezier(0.25, 0.46, 0.45, 0.94) forwards;
	}

	@keyframes cardFadeIn {
		to {
			opacity: 1;
			transform: scale(1);
		}
	}

	.card-header {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-bottom: 16rpx;
	}

	.order-no-row {
		display: flex;
		align-items: center;
		gap: 12rpx;
	}

	.no-text {
		font-size: 26rpx;
		color: #333;
		font-weight: 500;
	}

	.copy-btn {
		font-size: 22rpx;
		color: #3677ff;
		padding: 4rpx 12rpx;
		background: #f0f5ff;
		border-radius: 8rpx;
	}

	.status-tag {
		font-size: 24rpx;
		font-weight: 600;
		padding: 6rpx 18rpx;
		border-radius: 30rpx;
		line-height: 1.2;
		white-space: nowrap;
	}

	.category-row {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-bottom: 20rpx;
	}

	.tags {
		display: flex;
		align-items: center;
		gap: 12rpx;
	}

	.parent-tag {
		font-size: 22rpx;
		padding: 4rpx 14rpx;
		border-radius: 8rpx;
		font-weight: 500;
		color: #333;
	}

	.sub-tag {
		font-size: 22rpx;
		color: #999;
	}

	.expect-time {
		font-size: 24rpx;
		color: #888;
	}

	.address-block {
		background: #fafafa;
		border-radius: 16rpx;
		padding: 20rpx 16rpx;
		margin-bottom: 20rpx;
		display: flex;
		flex-direction: column;
		gap: 14rpx;
	}

	.addr-line {
		display: flex;
		align-items: center;
		gap: 12rpx;
	}

	.addr-icon {
		width: 44rpx;
		height: 44rpx;
		border-radius: 50%;
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 24rpx;
		font-weight: 600;
		flex-shrink: 0;
	}

	.pickup-icon {
		background-color: #e3f2fd;
		color: #1e88e5;
	}

	.delivery-icon {
		background-color: #fff3e0;
		color: #fb8c00;
	}

	.addr-text {
		font-size: 28rpx;
		color: #333;
		font-weight: 500;
		flex: 1;
		overflow: hidden;
		white-space: nowrap;
		text-overflow: ellipsis;
	}

	.extra-info {
		margin-bottom: 20rpx;
	}

	.fetch-code-row {
		margin-bottom: 14rpx;
	}

	.code-tag {
		display: flex;
		flex-wrap: wrap;
		align-items: center;
		background: #f5f5f5;
		border-radius: 12rpx;
		padding: 10rpx 20rpx;
		gap: 10rpx;
		word-break: break-all;
	}

	.code-label {
		font-size: 24rpx;
		color: #999;
		white-space: nowrap;
	}

	.code-value {
		font-size: 28rpx;
		color: #333;
		font-weight: 500;
		flex: 1;
		word-break: break-all;
	}

	.remark-row {
		margin-bottom: 14rpx;
		padding: 12rpx 16rpx;
		background: #f9f9f9;
		border-radius: 12rpx;
	}

	.status-tip {
		display: flex;
		flex-direction: column;
		gap: 6rpx;
		margin-bottom: 14rpx;
		padding: 16rpx 18rpx;
		background: #fff2f0;
		border: 1rpx solid #ffccc7;
		border-radius: 14rpx;
	}

	.status-title {
		font-size: 26rpx;
		font-weight: 700;
		color: #d93026;
	}

	.status-desc {
		font-size: 24rpx;
		color: #7a2b24;
		line-height: 1.4;
		word-break: break-all;
	}

	.remark-text {
		font-size: 24rpx;
		color: #888;
		word-break: break-all;
		line-height: 1.5;
	}

	.image-area {
		margin-bottom: 10rpx;
	}

	.image-toggle {
		display: flex;
		align-items: center;
		justify-content: center;
		padding: 12rpx 0;
		background: #fafafa;
		border-radius: 12rpx;
		margin-bottom: 10rpx;
	}

	.toggle-text {
		font-size: 24rpx;
		color: #666;
		margin-right: 8rpx;
	}

	.toggle-arrow {
		font-size: 22rpx;
		color: #999;
	}

	.image-grid {
		display: flex;
		flex-wrap: wrap;
		gap: 10rpx;
		margin-bottom: 10rpx;
		padding: 0 12rpx;
	}

	.grid-image {
		width: 200rpx;
		height: 200rpx;
		border-radius: 12rpx;
		background: #f5f5f5;
	}

	.card-footer {
		display: flex;
		justify-content: space-between;
		align-items: flex-end;
		border-top: 1rpx solid #f0f0f0;
		padding-top: 18rpx;
		margin-bottom: 10rpx;
	}

	.footer-left {
		display: flex;
		flex-direction: column;
		gap: 8rpx;
	}

	.time-text {
		font-size: 22rpx;
		color: #bbb;
	}

	.price-tag {
		font-size: 26rpx;
		font-weight: 600;
	}

	.price-tag.paid {
		color: #e74c3c;
	}

	.price-tag.unpaid {
		color: #ff9800;
	}

	.footer-actions {
		display: flex;
		gap: 12rpx;
	}

	.action-btn {
		padding: 8rpx 20rpx;
		border-radius: 30rpx;
		font-size: 24rpx;
		font-weight: 500;
		border: 1rpx solid #ddd;
		color: #666;
		background: #fff;
		transition: all 0.2s;
	}

	.action-btn:active {
		opacity: 0.7;
	}

	.cancel-btn {
		color: #e74c3c;
		border-color: #e74c3c;
	}

	.confirm-btn {
		color: #3677ff;
		border-color: #3677ff;
	}

	.detail-btn {
		color: #333;
		border-color: #ccc;
	}

	/* 时间明细折叠区域 */
	.time-info-wrap {
		margin-top: 10rpx;
	}

	.timeline-content {
		background: #fafafa;
		border-radius: 12rpx;
		padding: 22rpx 18rpx;
	}

	.timeline-item {
		position: relative;
		display: flex;
		gap: 16rpx;
		padding-bottom: 24rpx;
	}

	.timeline-item:last-child {
		padding-bottom: 0;
	}

	.timeline-item::before {
		content: "";
		position: absolute;
		left: 13rpx;
		top: 30rpx;
		bottom: -2rpx;
		width: 2rpx;
		background: #e5e7eb;
	}

	.timeline-item:last-child::before {
		display: none;
	}

	.timeline-dot {
		width: 28rpx;
		height: 28rpx;
		border-radius: 50%;
		background: #d1d5db;
		border: 6rpx solid #fafafa;
		box-sizing: border-box;
		flex-shrink: 0;
		margin-top: 4rpx;
	}

	.timeline-dot.active {
		background: #1677ff;
		box-shadow: 0 0 0 6rpx rgba(22, 119, 255, 0.12);
	}

	.timeline-body {
		flex: 1;
		min-width: 0;
	}

	.timeline-head {
		display: flex;
		align-items: baseline;
		justify-content: space-between;
		gap: 16rpx;
		margin-bottom: 8rpx;
	}

	.timeline-title {
		font-size: 26rpx;
		font-weight: 700;
		color: #1f2937;
	}

	.timeline-time {
		font-size: 22rpx;
		color: #9ca3af;
		flex-shrink: 0;
	}

	.timeline-desc {
		display: block;
		font-size: 24rpx;
		color: #4b5563;
		line-height: 1.45;
		word-break: break-all;
	}

	.load-more-status {
		display: flex;
		align-items: center;
		justify-content: center;
		height: 100rpx;
		padding: 20rpx;
		font-size: 24rpx;
		color: #999;
	}

	.loading-animation {
		width: 32rpx;
		height: 32rpx;
		border: 4rpx solid #e0e0e0;
		border-top-color: #666;
		border-radius: 50%;
		margin-right: 12rpx;
		animation: spin 0.8s linear infinite;
	}

	@keyframes spin {
		to {
			transform: rotate(360deg);
		}
	}

	.empty {
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		padding: 160rpx 0;
		gap: 20rpx;
		color: #aaa;
	}

	.empty::before {
		content: '📭';
		font-size: 80rpx;
		opacity: 0.5;
	}

	.cancel-mask {
		position: fixed;
		top: 0;
		left: 0;
		right: 0;
		bottom: 0;
		z-index: 9998;
		background: rgba(0, 0, 0, 0.45);
		display: flex;
		align-items: center;
		justify-content: center;
		padding: 40rpx;
	}

	.cancel-dialog {
		width: 100%;
		background: #fff;
		border-radius: 24rpx;
		padding: 34rpx 28rpx 28rpx;
		box-sizing: border-box;
	}

	.cancel-dialog-title {
		font-size: 34rpx;
		font-weight: 700;
		color: #1a1a1a;
		text-align: center;
		margin-bottom: 10rpx;
	}

	.cancel-dialog-subtitle {
		font-size: 24rpx;
		color: #888;
		text-align: center;
		margin-bottom: 28rpx;
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

	.reason-input {
		width: 100%;
		height: 150rpx;
		box-sizing: border-box;
		background: #fafafa;
		border-radius: 16rpx;
		padding: 18rpx;
		font-size: 26rpx;
		color: #333;
		line-height: 1.5;
	}

	.reason-placeholder {
		color: #aaa;
		font-size: 26rpx;
	}

	.cancel-dialog-actions {
		display: flex;
		justify-content: flex-end;
		gap: 16rpx;
		margin-top: 28rpx;
	}

	.dialog-btn {
		min-width: 132rpx;
		height: 64rpx;
		border-radius: 32rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 26rpx;
		font-weight: 500;
		border: 1rpx solid #ddd;
		box-sizing: border-box;
	}

	.dialog-cancel-btn {
		color: #666;
		background: #fff;
	}

	.dialog-confirm-btn {
		color: #fff;
		background: #e74c3c;
		border-color: #e74c3c;
	}

	.dialog-confirm-btn.disabled {
		opacity: 0.6;
	}

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

	.preview-swiper {
		width: 100%;
		height: 100%;
		max-width: 800px;
		max-height: 90vh;
		margin: 0 auto;
	}

	.zoom-container {
		width: 100%;
		height: 100%;
		display: flex;
		align-items: center;
		justify-content: center;
		transition: transform 0.1s ease-out;
	}

	.preview-image {
		width: 100%;
		height: 100%;
		object-fit: contain;
		display: block;
		pointer-events: auto;
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
		z-index: 10000;
	}
</style>

<style>
	body.preview-lock {
		overflow: hidden;
	}
</style>