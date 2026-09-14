<template>
	<view class="receive-page" :style="{ height: pageHeight + 'px' }">
		<!-- 状态导航栏（固定在顶部） -->
		<scroll-view class="status-nav" scroll-x :show-scrollbar="false">
			<view v-for="(tab, idx) in statusTabs" :key="idx" class="status-tab"
				:class="{ active: currentStatus === tab.value }" @click="switchStatus(tab.value)">
				{{ tab.label }}
				<!-- 显示数量（新订单和全部订单不显示） -->
				<view v-if="tab.value !== 0 && tab.value !== -1 && tab.value !== 6 && tab.value !== 5 && getStatusCount(tab.value) > 0" class="tab-badge">
					{{ getStatusCount(tab.value) > 99 ? '99+' : getStatusCount(tab.value) }}
				</view>
			</view>
		</scroll-view>

		<!-- 中间订单列表（可滚动区域） -->
		<view class="main-content">
			<scroll-view class="order-list" scroll-y @scroll="onScroll">
				<view v-for="order in displayOrders" :key="order.orderNo" class="order-card" :class="{ 
            'order-card-leave': animatingOrderNo === order.orderNo,
            'order-card-in': true
          }">
					<!-- 顶部：倒计时 + 价格 -->
					<view class="card-header">
						<view class="delivery-info">
							<view class="expect-row">期望送达：{{ order.deliveryDate }}</view>
							<view class="remain-row">剩余：{{ order.remainTime }}</view>
						</view>
						<view class="price-area" :class="{ 'help-buy': order.parentType === '帮买服务' }">
							<template v-if="order.parentType !== '帮买服务'">
								<text class="reward-price">赏金¥{{ formatPrice(order.rewardPrice) }}</text>
							</template>
							<template v-else>
								<view class="price-col">
									<text class="reward-price">赏金¥{{ formatPrice(order.rewardPrice) }}</text>
									<text class="goods-price">物品价¥{{ formatPrice(order.goodsBudget) }}</text>
								</view>
							</template>
						</view>
					</view>

					<!-- 分类标签 -->
					<view class="category-tag">
						<view class="primary-tag" :style="getPrimaryTagStyle(order.parentType)">
							{{ order.parentType }}-{{ order.subType }}
						</view>
					</view>

					<!-- 地址 -->
					<view class="address-section">
						<view class="addr-detail">
							<view class="addr-row">
								<view class="addr-icon start">取</view>
								<text
									class="addr-text">{{ order.startAddressText || order.startAddress || '待补充' }}</text>
							</view>
							<view class="addr-row">
								<view class="addr-icon end">送</view>
								<text class="addr-text">{{ order.endAddressText || order.endAddress || '待补充' }}</text>
							</view>
						</view>
					</view>

					<!-- 备注 -->
					<view class="remark-area" v-if="order.remark && order.remark.trim()">
						<text class="remark-text">{{ order.remark }}</text>
					</view>

					<!-- 取件码 & 图片（非新订单显示） -->
					<view v-if="order.orderStatus !== 0">
						<view class="fetch-code-row" v-if="order.fetchCode">
							<view class="code-tag">
								<text class="code-label">取件码/物品</text>
								<text class="code-value">{{ order.fetchCode }}</text>
							</view>
						</view>

						<view v-if="order.orderImageList && order.orderImageList.length > 0" class="image-area">
							<view class="image-grid">
								<image v-for="(img, imgIdx) in order.orderImageList" :key="imgIdx" :src="img"
									mode="aspectFill" class="grid-image"
									@click.stop="openPreview(order.orderImageList, imgIdx)"></image>
							</view>
						</view>
					</view>

					<!-- 新订单滑块 -->
					<view v-if="order.orderStatus === 0" class="slider-container" :id="'slider-' + order.orderNo">
						<view v-if="order.acceptState === 'idle'" class="slider-track">
							<view class="track-bg">
								<text class="track-text"
									:style="{ opacity: (sliderX[order.orderNo] || 0) > 10 ? 0 : 1 }">立即接单</text>
							</view>
							<view class="progress-fill" v-if="(sliderX[order.orderNo] || 0) > 0"
								:style="{ width: ((sliderX[order.orderNo] || 0) + blockWidthPx + 10) + 'px' }"></view>
							<view class="slider-block" :class="{ 'sliding-active': isSliding[order.orderNo] }"
								:style="{ transform: 'translateX(' + (sliderX[order.orderNo] || 0) + 'px)' }"
								@touchstart="onTouchStart($event, order)" @touchmove="onTouchMove($event, order)"
								@touchend="onTouchEnd($event, order)">
								<image src="/static/order/partTimeOrder/right.png" mode="aspectFit" class="slider-icon">
								</image>
							</view>
						</view>
						<view v-else-if="order.acceptState === 'loading'" class="slider-loading">
							<image class="loading-icon" src="/static/loading-spinner.gif"></image>
							<text class="loading-text">抢单中...</text>
						</view>
						<view v-else-if="order.acceptState === 'success'" class="slider-success">
							<text class="success-icon">✓</text>
							<text class="success-text">抢单成功</text>
						</view>
					</view>

					<!-- 纠纷中操作 -->
					<view v-if="order.orderStatus === 4" class="dispute-section">
						<view class="dispute-notice">
							<text class="notice-text">客户申请取消订单：{{ order.cancelReason || '' }}</text>
						</view>
						<view class="dispute-buttons">
							<view class="dispute-btn reject" @click="handleDispute(order, 2)">不同意取消</view>
							<view class="dispute-btn agree" @click="handleDispute(order, 1)">同意取消</view>
						</view>
					</view>

					<!-- 已接单 / 配送中 操作按钮 -->
					<view v-else-if="order.orderStatus === 1 || order.orderStatus === 2" class="action-buttons action-buttons-row">
						<view v-if="order.orderStatus === 1" class="slide-style-btn" @click="handlePickUp(order)">我已取件
						</view>
						<view v-if="order.orderStatus === 2" class="slide-style-btn" @click="handleDeliver(order)">我已送达
						</view>
						<view v-if="order.orderStatus === 1" class="slide-style-btn cancel-order-btn" @click="openRiderCancelDialog(order)">取消订单</view>
					</view>

					<!-- 已送达（不可点击） -->
					<view v-else-if="order.orderStatus === 3" class="action-buttons">
						<view class="slide-style-btn disabled-btn">已送达</view>
					</view>

					<!-- 其他状态（已完成、已撤销等） -->
					<view v-else-if="order.orderStatus > 4" class="disabled-swipe">
						<text>{{ getStatusText(order.orderStatus) }}</text>
					</view>

					<!-- 时间明细折叠 -->
					<view class="time-info-wrap" v-if="order.orderStatus !== 0">
						<view class="time-toggle-bar" @click="toggleTimeInfo(order.orderNo)">
							<text class="toggle-title">订单时间明细</text>
							<text class="toggle-arrow">{{ expandedTimeInfo[order.orderNo] ? '收起 ▲' : '展开 ▼' }}</text>
						</view>
						<view class="time-info-content" v-if="expandedTimeInfo[order.orderNo]">
							<view class="time-item">
								<text class="time-label">下单时间：</text>
								<text class="time-val">{{ formatFullTime(order.createTime) || '暂无' }}</text>
							</view>
							<view class="time-item">
								<text class="time-label">接单时间：</text>
								<text class="time-val">{{ formatFullTime(order.acceptTime) || '暂无' }}</text>
							</view>
							<view class="time-item">
								<text class="time-label">取件时间：</text>
								<text class="time-val">{{ formatFullTime(order.pickTime) || '暂无' }}</text>
							</view>
							<view class="time-item">
								<text class="time-label">送达时间：</text>
								<text class="time-val">{{ formatFullTime(order.deliverTime) || '暂无' }}</text>
							</view>
							<view class="time-item">
								<text class="time-label">完成时间：</text>
								<text class="time-val">{{ formatFullTime(order.finishTime) || '暂无' }}</text>
							</view>
						</view>
					</view>
				</view>

				<!-- 加载状态 -->
				<view v-if="loading" class="load-more">加载中...</view>
				<view v-if="noMore && displayOrders.length > 0" class="load-more">没有更多了</view>
				<view v-if="displayOrders.length === 0 && !loading" class="empty">暂无订单</view>
			</scroll-view>
		</view>

		<!-- 底部操作栏（固定在底部） -->
		<view class="bottom-bar">
			<picker class="filter-picker" :range="typeOptions" range-key="label" @change="onTypeChange">
				<view class="picker-item">
					<text class="picker-label">{{ typeOptions[filterTypeIndex].label }}</text>
					<text class="arrow">▼</text>
				</view>
			</picker>
			<view class="refresh-btn" @click="refreshOrders">
				<text class="refresh-icon">↻</text>
				<text>刷新订单</text>
			</view>
			<view class="sort-btn" @click="toggleSort">
				<text>按赏金排序</text>
				<text class="arrow" v-if="sortEnabled">{{ sortOrder === 'asc' ? '↑' : '↓' }}</text>
			</view>
		</view>

		<!-- 图片预览遮罩 -->
		<view class="preview-mask" v-if="previewVisible" @click.self="closePreview">
			<swiper class="preview-swiper" :current="previewIndex" @change="onPreviewSwiperChange"
				:indicator-dots="true" indicator-color="rgba(255,255,255,0.5)" indicator-active-color="#fff"
				@touchmove.stop @mousedown.stop @mouseup.stop @click.stop>
				<swiper-item v-for="(url, idx) in previewUrls" :key="idx">
					<view class="zoom-container" :style="{ transform: `scale(${scale})` }"
						@touchstart="onZoomTouchStart" @touchmove="onZoomTouchMove" @touchend="onZoomTouchEnd"
						@wheel="onWheel">
						<image :src="url" mode="aspectFit" class="preview-image" @click.stop="closePreview"></image>
					</view>
				</swiper-item>
			</swiper>
			<view class="close-btn" @click.stop="closePreview">✕</view>
		</view>

		<view class="reason-mask" v-if="reasonDialogVisible" @click.self="closeReasonDialog">
			<view class="reason-dialog">
				<view class="reason-title">{{ reasonDialogTitle }}</view>
				<view class="reason-subtitle">请选择或输入原因</view>
				<view class="reason-list">
					<view class="reason-item" v-for="reason in currentReasonOptions" :key="reason"
						:class="{ active: selectedReason === reason }" @click="selectReason(reason)">
						{{ reason }}
					</view>
				</view>
				<textarea class="reason-input" v-model="customReason" maxlength="120" placeholder="也可以输入其他原因"
					placeholder-class="reason-placeholder" />
				<view class="reason-actions">
					<view class="reason-btn reason-close" @click="closeReasonDialog">取消</view>
					<view class="reason-btn reason-submit" :class="{ disabled: submittingReason }" @click="submitReasonAction">
						{{ submittingReason ? '提交中...' : '确定' }}
					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script setup>
	import config from '@/config/env.js'
	import {
		ref,
		computed,
		reactive,
		onMounted,
		onBeforeUnmount,
		nextTick
	} from 'vue'
	import {
		onBackPress,
		onLoad
	} from '@dcloudio/uni-app'
	import request from '@/utils/request.js'

	// ==================== WebSocket 管理类（纠纷推送专用） ====================
	class DisputeWebSocket {
		constructor(url, options = {}) {
			this.url = url;
			this.reconnectInterval = options.reconnectInterval || 5000;
			this.onMessageCallback = options.onMessage || (() => {});
			this.socket = null;
			this.manualClose = false;
			this._connect();
		}

		_connect() {
			this.socket = uni.connectSocket({
				url: this.url,
				success: () => {
					console.log('纠纷WebSocket 连接中...');
				},
				fail: (err) => {
					console.error('纠纷WebSocket 连接失败', err);
					if (!this.manualClose) {
						setTimeout(() => this._connect(), this.reconnectInterval);
					}
				}
			});

			this.socket.onOpen(() => {
				console.log('纠纷WebSocket 已打开');
			});

			this.socket.onMessage((res) => {
				try {
					const data = JSON.parse(res.data);
					this.onMessageCallback(data);
				} catch (e) {
					console.error('纠纷WebSocket 消息解析失败', e);
				}
			});

			this.socket.onError((err) => {
				console.error('纠纷WebSocket 错误', err);
			});

			this.socket.onClose(() => {
				console.log('纠纷WebSocket 关闭');
				if (!this.manualClose) {
					setTimeout(() => this._connect(), this.reconnectInterval);
				}
			});
		}

		send(data) {
			if (this.socket && this.socket.readyState === 1) {
				this.socket.send({
					data: JSON.stringify(data)
				});
			}
		}

		close() {
			this.manualClose = true;
			if (this.socket) {
				this.socket.close();
			}
		}
	}

	// ==================== 骑手用户ID ====================
	const getRiderUserId = () => {
		const cached = uni.getStorageSync('riderUserId');
		if (cached) return cached;
		return '202605271111';
	};
	const riderUserId = ref(getRiderUserId());

	// ==================== 各状态订单数量（用于右上角展示） ====================
	const statusCounts = reactive({
		received: 0, // 已接单(1)
		delivering: 0, // 配送中(2)
		finished: 0, // 已送达(3)
		dispute: 0, // 纠纷中(4)
		complete: 0, // 已完成(5)
		cancel: 0 // 已撤销(6)
	});

	// 根据状态值获取对应的显示数量
	const getStatusCount = (statusValue) => {
		switch (statusValue) {
			case 1:
				return statusCounts.received;
			case 2:
				return statusCounts.delivering;
			case 3:
				return statusCounts.finished;
			case 4:
				return statusCounts.dispute;
			case 5:
				return statusCounts.complete;
			case 6:
				return statusCounts.cancel;
			default:
				return 0;
		}
	};

	// 更新本地数量（加减操作）
	const updateLocalCount = (statusValue, delta) => {
		switch (statusValue) {
			case 1:
				statusCounts.received = Math.max(0, statusCounts.received + delta);
				break;
			case 2:
				statusCounts.delivering = Math.max(0, statusCounts.delivering + delta);
				break;
			case 3:
				statusCounts.finished = Math.max(0, statusCounts.finished + delta);
				break;
			case 4:
				statusCounts.dispute = Math.max(0, statusCounts.dispute + delta);
				break;
			case 5:
				statusCounts.complete = Math.max(0, statusCounts.complete + delta);
				break;
			case 6:
				statusCounts.cancel = Math.max(0, statusCounts.cancel + delta);
				break;
		}
	};

	// ==================== 调用后端接口获取全状态数量 ====================
	const fetchAllStatusCount = async () => {
		try {
			const res = await request({
				url: '/rider/order/order-all-count',
				method: 'GET',
				data: {
					riderUserId: riderUserId.value
				}
			});
			if (res.code === 1 && res.data) {
				statusCounts.received = res.data.received || 0;
				statusCounts.delivering = res.data.delivering || 0;
				statusCounts.finished = res.data.finished || 0;
				statusCounts.dispute = res.data.dispute || 0;
				statusCounts.complete = res.data.complete || 0;
				statusCounts.cancel = res.data.cancel || 0;
				console.log('获取各状态数量成功', statusCounts);
			} else {
				console.error('获取状态数量失败', res.msg);
			}
		} catch (err) {
			console.error('获取状态数量网络错误', err);
		}
	};

	// ==================== WebSocket 实例 ====================
	let disputeWs = null;

	const buildWsUrl = () => {
		let base = config.baseUrl || '';
		if (!base && typeof window !== 'undefined') {
			const {
				protocol,
				host
			} = window.location;
			base = `${protocol}//${host}`;
		}
		let wsBase = base.replace(/^http/, 'ws');
		if (!wsBase.endsWith('/')) wsBase += '/';
		const cleanBase = wsBase.replace(/\/api\/?$/, '');
		return `${cleanBase}ws/dispute?riderUserId=${riderUserId.value}`;
	};

	const initDisputeWebSocket = () => {
		const wsUrl = buildWsUrl();
		if (!wsUrl) {
			console.warn('WebSocket URL 无效，无法建立纠纷推送连接');
			return;
		}
		disputeWs = new DisputeWebSocket(wsUrl, {
			reconnectInterval: 5000,
			onMessage: (data) => {
				if (data.type === 'dispute_count' && typeof data.count === 'number') {
					// 后端推送纠纷数量，直接更新
					statusCounts.dispute = data.count;
					console.log(`[纠纷推送] 最新纠纷数量: ${data.count}`);
				}
			}
		});
	};

	// ==================== 筛选与排序 ====================
	const typeOptions = [{
			label: '全部类型',
			value: null
		},
		{
			label: '代取服务',
			value: '代取服务'
		},
		{
			label: '帮买服务',
			value: '帮买服务'
		},
		{
			label: '跑腿代办',
			value: '跑腿代办'
		}
	];
	const filterTypeIndex = ref(0);
	const filterType = computed(() => typeOptions[filterTypeIndex.value].value);
	const sortOrder = ref('asc');
	const sortEnabled = ref(false);

	// 状态导航
	const statusTabs = [{
			label: '新订单',
			value: 0
		},
		{
			label: '已接单',
			value: 1
		},
		{
			label: '配送中',
			value: 2
		},
		{
			label: '已送达',
			value: 3
		},
		{
			label: '纠纷中',
			value: 4
		},
		{
			label: '已完成',
			value: 5
		},
		{
			label: '已撤销',
			value: 6
		},
		{
			label: '全部订单',
			value: -1
		}
	];
	const currentStatus = ref(0);

	// 分页与列表
	const pageNum = ref(1);
	const pageSize = 20;
	const total = ref(0);
	const loading = ref(false);
	const noMore = ref(false);
	const allOrders = ref([]);
	const targetOrderNo = ref('');
	const loadMoreLock = ref(false);

	// 滑块相关
	const sliderX = reactive({});
	const isSliding = reactive({});
	const trackWidths = reactive({});
	const currentSlideId = ref('');
	const blockWidthRpx = 85;
	const blockWidthPx = ref(0);
	let startX = 0;
	let startPos = 0;

	// 接单状态
	const orderAcceptState = reactive({});

	// 时间展开
	const expandedTimeInfo = reactive({});

	// 图片预览
	const previewVisible = ref(false);
	const previewUrls = ref([]);
	const previewIndex = ref(0);
	const scale = ref(1);
	let lastDist = 0;

	// 页面高度
	const pageHeight = ref(0);

	// 动画控制
	const animatingOrderNo = ref('');
	const reasonDialogVisible = ref(false);
	const reasonDialogType = ref('');
	const reasonDialogTitle = ref('');
	const currentReasonOrder = ref(null);
	const selectedReason = ref('');
	const customReason = ref('');
	const submittingReason = ref(false);
	const riderCancelReasons = [
		'联系不上用户、地址有误',
		'用户要求取消',
		'商家缺货/出餐慢',
		'车辆故障、身体不适',
		'道路拥堵/封闭'
	];
	const rejectCancelReasons = [
		'已取件，商品无法退回',
		'临近送达、产生配送成本'
	];
	const currentReasonOptions = computed(() => reasonDialogType.value === 'rejectCancel' ? rejectCancelReasons : riderCancelReasons);

	// ==================== 辅助函数 ====================
	const formatPrice = (val) => {
		const num = parseFloat(val);
		return isNaN(num) ? '0.00' : num.toFixed(2);
	};

	const formatFullTime = (timeStr) => {
		if (!timeStr) return '';
		return timeStr.replace('T', ' ');
	};

	const toggleTimeInfo = (orderNo) => {
		expandedTimeInfo[orderNo] = !expandedTimeInfo[orderNo];
	};

	const getExpectDate = (order) => {
		if (!order.expectTime) return '尽快送达';
		const date = new Date(order.expectTime.replace(/-/g, '/'));
		const month = (date.getMonth() + 1).toString().padStart(2, '0');
		const day = date.getDate().toString().padStart(2, '0');
		const hour = date.getHours().toString().padStart(2, '0');
		const min = date.getMinutes().toString().padStart(2, '0');
		return `${month}月${day}日 ${hour}:${min}`;
	};

	const getRemainTime = (order) => {
		if (!order.expectTime) return '尽快';
		const now = new Date();
		const target = new Date(order.expectTime.replace(/-/g, '/'));
		const diffMs = target - now;
		if (diffMs <= 0) return '已超时';
		const days = Math.floor(diffMs / (1000 * 60 * 60 * 24));
		const hours = Math.floor((diffMs % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
		const minutes = Math.floor((diffMs % (1000 * 60 * 60)) / (1000 * 60));
		if (days > 0) return `${days}天${hours}时${minutes}分`;
		return `${hours}时${minutes}分`;
	};

	const getPrimaryTagStyle = (parentType) => {
		const map = {
			'代取服务': {
				backgroundColor: '#FFF3E0',
				color: '#F57C00'
			},
			'帮买服务': {
				backgroundColor: '#FFEBEE',
				color: '#E53935'
			},
			'跑腿代办': {
				backgroundColor: '#E8F5E9',
				color: '#2E7D32'
			}
		};
		const style = map[parentType] || map['代取服务'];
		return `background-color: ${style.backgroundColor}; color: ${style.color};`;
	};

	const getStatusText = (status) => {
		const map = {
			1: '已接单',
			2: '配送中',
			3: '已送达',
			4: '纠纷中',
			5: '已完成',
			6: '已撤销'
		};
		return map[status] || '';
	};

	const displayOrders = computed(() => {
		let list = [...allOrders.value];
		if (filterType.value) {
			list = list.filter(o => o.parentType === filterType.value);
		}
		if (targetOrderNo.value) {
			list = list.filter(o => String(o.orderNo) === String(targetOrderNo.value));
		}
		return list.map(order => ({
			...order,
			deliveryDate: getExpectDate(order),
			remainTime: getRemainTime(order),
			acceptState: orderAcceptState[order.orderNo] || 'idle'
		}));
	});

	// ==================== 接口请求 ====================
	const fetchOrders = async (reset = false) => {
		if (loading.value) return;
		if (reset) {
			pageNum.value = 1;
			allOrders.value = [];
			noMore.value = false;
			total.value = 0;
			Object.keys(sliderX).forEach(key => delete sliderX[key]);
			Object.keys(trackWidths).forEach(key => delete trackWidths[key]);
			Object.keys(isSliding).forEach(key => delete isSliding[key]);
			Object.keys(orderAcceptState).forEach(key => delete orderAcceptState[key]);
		}
		if (noMore.value && !reset) return;

		loading.value = true;
		try {
			const reqData = {
				pageNum: pageNum.value,
				pageSize,
				riderUserId: riderUserId.value,
				parentType: filterType.value,
			};
			if (sortEnabled.value) {
				reqData.sortField = 'reward_price';
				reqData.sortOrder = sortOrder.value;
			}

			let url = '';
			if (currentStatus.value === 0) {
				url = '/order/receiveHallList';
				reqData.orderStatus = 0;
			} else {
				url = '/rider/order/list';
				reqData.orderStatus = currentStatus.value === -1 ? null : currentStatus.value;
			}

			const res = await request({
				url,
				method: 'GET',
				data: reqData
			});
			if (res.code === 1 && res.data?.list) {
				const newOrders = res.data.list;
				if (reset) {
					allOrders.value = newOrders;
				} else {
					allOrders.value.push(...newOrders);
				}
				total.value = res.data.total || 0;
				noMore.value = allOrders.value.length >= total.value;

				newOrders.forEach(order => {
					if (sliderX[order.orderNo] === undefined) sliderX[order.orderNo] = 0;
					if (orderAcceptState[order.orderNo] === undefined) orderAcceptState[order.orderNo] =
						'idle';
				});
				await nextTick();
				setTimeout(() => allOrders.value.forEach(order => getTrackWidth(order.orderNo)), 150);
			} else {
				uni.showToast({
					title: res.msg || '获取失败',
					icon: 'none'
				});
			}
		} catch (err) {
			uni.showToast({
				title: '网络错误',
				icon: 'none'
			});
		} finally {
			loading.value = false;
		}
	};

	const getTrackWidth = (orderNo) => {
		uni.createSelectorQuery()
			.select(`#slider-${orderNo} .slider-track`)
			.boundingClientRect(rect => {
				if (rect) trackWidths[orderNo] = rect.width;
			})
			.exec();
	};

	const removeOrder = (orderNo) => {
		const idx = allOrders.value.findIndex(o => o.orderNo === orderNo);
		if (idx !== -1) {
			allOrders.value.splice(idx, 1);
			total.value = Math.max(total.value - 1, 0);
		}
		delete sliderX[orderNo];
		delete trackWidths[orderNo];
		delete isSliding[orderNo];
		delete orderAcceptState[orderNo];
	};

	// ==================== 接单 ====================
	const acceptOrderApi = async (orderNo, serviceFee) => {
		return await request({
			url: '/rider/order/accept',
			method: 'POST',
			header: {
				'Content-Type': 'application/json'
			},
			data: {
				orderNo,
				riderUserId: riderUserId.value,
				serviceFee
			}
		});
	};

	const acceptOrder = async (order) => {
		const orderNo = order.orderNo;
		if (orderAcceptState[orderNo] !== 'idle') return;
		orderAcceptState[orderNo] = 'loading';
		const serviceFee = parseFloat(order.orderPrice) || 0;
		try {
			const res = await acceptOrderApi(orderNo, serviceFee);
			if (res.code === 1) {
				orderAcceptState[orderNo] = 'success';
				uni.showToast({
					title: '请到已接单页面查看',
					icon: 'success'
				});
				// 本地更新数量：新订单减少1，已接单增加1
				// 注意：新订单数量不在右上角显示，但会影响后续切换tab时的总数，不过切换tab会重新获取，所以可以不减新订单计数
				updateLocalCount(1, 1); // 已接单 +1
				animatingOrderNo.value = orderNo;
				setTimeout(() => {
					removeOrder(orderNo);
					animatingOrderNo.value = '';
				}, 400);
			} else {
				orderAcceptState[orderNo] = 'idle';
				uni.showToast({
					title: res.msg || '手慢了，订单被抢走了',
					icon: 'none'
				});
				animatingOrderNo.value = orderNo;
				setTimeout(() => {
					removeOrder(orderNo);
					animatingOrderNo.value = '';
				}, 400);
			}
		} catch (err) {
			orderAcceptState[orderNo] = 'idle';
			uni.showToast({
				title: '请求异常',
				icon: 'none'
			});
		}
	};

	// ==================== 订单状态更新 ====================
	const updateOrderStatusApi = async (data) => {
		return await request({
			url: '/rider/order/update',
			method: 'POST',
			header: {
				'Content-Type': 'application/json'
			},
			data
		});
	};

	const closeReasonDialog = (force = false) => {
		if (submittingReason.value && !force) return;
		reasonDialogVisible.value = false;
		reasonDialogType.value = '';
		reasonDialogTitle.value = '';
		currentReasonOrder.value = null;
		selectedReason.value = '';
		customReason.value = '';
	};

	const selectReason = (reason) => {
		selectedReason.value = reason;
		customReason.value = '';
	};

	const openRiderCancelDialog = (order) => {
		currentReasonOrder.value = order;
		reasonDialogType.value = 'riderCancel';
		reasonDialogTitle.value = '骑手取消订单';
		selectedReason.value = '';
		customReason.value = '';
		reasonDialogVisible.value = true;
	};

	const openDisputeReasonDialog = (order) => {
		currentReasonOrder.value = order;
		reasonDialogType.value = 'rejectCancel';
		reasonDialogTitle.value = '不同意取消';
		selectedReason.value = '';
		customReason.value = '';
		reasonDialogVisible.value = true;
	};

	const submitReasonAction = async () => {
		if (submittingReason.value) return;
		const order = currentReasonOrder.value;
		const reason = (customReason.value || selectedReason.value).trim();
		if (!order || !order.orderNo) {
			uni.showToast({ title: '订单信息异常', icon: 'none' });
			return;
		}
		if (!reason) {
			uni.showToast({ title: '请选择或输入原因', icon: 'none' });
			return;
		}
		submittingReason.value = true;
		try {
			const result = reasonDialogType.value === 'rejectCancel'
				? await submitDispute(order, 2, reason)
				: await request({
					url: '/rider/order/cancel',
					method: 'POST',
					header: { 'Content-Type': 'application/json' },
					data: {
						orderNo: order.orderNo,
						riderUserId: riderUserId.value,
						cancelReason: reason
					}
				});
			if (result.code === 1) {
				const oldStatus = order.orderStatus;
				uni.showToast({
					title: reasonDialogType.value === 'rejectCancel' ? '已拒绝取消' : '订单已回到大厅',
					icon: 'success'
				});
				if (reasonDialogType.value === 'rejectCancel') {
					updateLocalCount(4, -1);
				} else {
					updateLocalCount(oldStatus, -1);
				}
				animatingOrderNo.value = order.orderNo;
				closeReasonDialog(true);
				setTimeout(() => {
					removeOrder(order.orderNo);
					animatingOrderNo.value = '';
				}, 400);
			} else {
				uni.showToast({ title: result.msg || '操作失败', icon: 'none' });
			}
		} catch (e) {
			uni.showToast({ title: '网络错误', icon: 'none' });
		} finally {
			submittingReason.value = false;
		}
	};

	const handlePickUp = async (order) => {
		uni.showModal({
			title: '确认取件',
			content: '确定已取到物品？',
			success: async (res) => {
				if (!res.confirm) return;
				try {
					const result = await updateOrderStatusApi({
						orderNo: order.orderNo,
						riderUserId: riderUserId.value,
						orderStatus: 2
					});
					if (result.code === 1) {
						uni.showToast({
							title: '配送中，请到配送中页面查看',
							icon: 'success'
						});
						// 本地数量更新：已接单 -1，配送中 +1
						updateLocalCount(1, -1);
						updateLocalCount(2, 1);
						animatingOrderNo.value = order.orderNo;
						setTimeout(() => {
							removeOrder(order.orderNo);
							animatingOrderNo.value = '';
						}, 400);
					} else {
						uni.showToast({
							title: result.msg || '操作失败',
							icon: 'none'
						});
					}
				} catch (e) {
					uni.showToast({
						title: '网络错误',
						icon: 'none'
					});
				}
			}
		});
	};

	const handleDeliver = async (order) => {
		uni.showModal({
			title: '确认送达',
			content: '确定已将物品送达？',
			success: async (res) => {
				if (!res.confirm) return;
				try {
					const result = await updateOrderStatusApi({
						orderNo: order.orderNo,
						riderUserId: riderUserId.value,
						serviceFee: parseFloat(order.orderPrice) || 0,
						orderStatus: 3
					});
					if (result.code === 1) {
						uni.showToast({
							title: '已送达，请到已送达页面查看',
							icon: 'success'
						});
						// 本地数量更新：配送中 -1，已送达 +1
						updateLocalCount(2, -1);
						updateLocalCount(3, 1);
						animatingOrderNo.value = order.orderNo;
						setTimeout(() => {
							removeOrder(order.orderNo);
							animatingOrderNo.value = '';
						}, 400);
					} else {
						uni.showToast({
							title: result.msg || '操作失败',
							icon: 'none'
						});
					}
				} catch (e) {
					uni.showToast({
						title: '网络错误',
						icon: 'none'
					});
				}
			}
		});
	};

	// ==================== 纠纷处理 ====================
	const submitDispute = async (order, disposeType, reason = '') => {
		return await request({
			url: '/rider/order/dispute',
			method: 'POST',
			header: {
				'Content-Type': 'application/json'
			},
			data: {
				orderNo: order.orderNo,
				riderUserId: riderUserId.value,
				disposeType,
				reason
			}
		});
	};

	const pushDisputeRejectedMessage = (order, reason = '') => {
		const orderNo = order?.orderNo || ''
		if (!orderNo) return
		pushLocalSystemMessage({
			id: `order_reject_${orderNo}_${Date.now()}`,
			title: '订单取消申请被拒绝',
			lastMsg: `订单 ${orderNo} 的取消申请已被拒绝`,
			desc: reason ? `拒绝原因：${reason}` : '骑手不同意取消，请进入订单查看详情',
			sourceType: 'system',
			sourceId: orderNo,
			targetUrl: `/pages/order/myOrder?orderNo=${encodeURIComponent(orderNo)}`
		})
	}

	const pushOrderStatusMessage = (order, statusType) => {
		const orderNo = order?.orderNo || ''
		if (!orderNo) return
		
		const statusConfig = {
			accepted: {
				title: '订单已被接单',
				lastMsg: `订单 ${orderNo} 已被骑手接单`,
				desc: '骑手正在赶来，请保持手机畅通'
			},
			picked: {
				title: '骑手已取件',
				lastMsg: `订单 ${orderNo} 骑手已取件`,
				desc: '骑手正在配送中，预计很快送达'
			},
			delivered: {
				title: '订单已送达',
				lastMsg: `订单 ${orderNo} 已送达`,
				desc: '请确认收货'
			}
		}
		
		const config = statusConfig[statusType]
		if (!config) return
		
		pushLocalSystemMessage({
			id: `order_${statusType}_${orderNo}_${Date.now()}`,
			title: config.title,
			lastMsg: config.lastMsg,
			desc: config.desc,
			sourceType: 'system',
			sourceId: orderNo,
			targetUrl: `/pages/order/myOrder?orderNo=${encodeURIComponent(orderNo)}`
		})
	}

	const handleDispute = async (order, disposeType) => {
		if (disposeType === 2) {
			openDisputeReasonDialog(order);
			return;
		}
		const actionText = disposeType === 1 ? '同意取消' : '驳回取消';
		uni.showModal({
			title: '确认操作',
			content: `确定要${actionText}该订单吗？`,
			success: async (res) => {
				if (!res.confirm) return;
				uni.showLoading({
					title: '处理中...'
				});
				try {
					const result = await submitDispute(order, disposeType);
					if (result.code === 1) {
						uni.hideLoading();
						uni.showToast({
							title: '处理成功',
							icon: 'success'
						});
						// 纠纷订单被处理（同意取消或驳回取消），纠纷数量 -1
						// 注意：根据业务逻辑，若驳回取消可能返回原状态，但后端一般会将订单移出纠纷，这里简单减1
						updateLocalCount(4, -1);
						animatingOrderNo.value = order.orderNo;
						setTimeout(() => {
							removeOrder(order.orderNo);
							animatingOrderNo.value = '';
						}, 400);
					} else {
						uni.hideLoading();
						uni.showToast({
							title: result.msg || '操作失败',
							icon: 'none'
						});
					}
				} catch (e) {
					uni.hideLoading();
					uni.showToast({
						title: '网络错误',
						icon: 'none'
					});
				}
			}
		});
	};

	// ==================== 滑块交互 ====================
	const onTouchStart = (e, order) => {
		const orderNo = order.orderNo;
		if (orderAcceptState[orderNo] !== 'idle' || (currentSlideId.value && currentSlideId.value !== orderNo)) return;
		currentSlideId.value = orderNo;
		isSliding[orderNo] = true;
		startX = e.touches[0].pageX;
		startPos = sliderX[orderNo] || 0;
	};

	const onTouchMove = (e, order) => {
		const orderNo = order.orderNo;
		if (currentSlideId.value !== orderNo || !isSliding[orderNo]) return;
		const trackWidth = trackWidths[orderNo];
		if (!trackWidth) return;
		const maxMove = trackWidth - blockWidthPx.value - 10;
		const diff = e.touches[0].pageX - startX;
		let now = startPos + diff;
		if (now < 0) now = 0;
		if (now > maxMove) now = maxMove;
		sliderX[orderNo] = now;
	};

	const onTouchEnd = async (e, order) => {
		const orderNo = order.orderNo;
		if (currentSlideId.value !== orderNo) return;
		isSliding[orderNo] = false;
		const curr = sliderX[orderNo] || 0;
		const trackWidth = trackWidths[orderNo];
		if (trackWidth && curr >= trackWidth - blockWidthPx.value - 10) {
			await acceptOrder(order);
		}
		if (orderAcceptState[orderNo] === 'idle') {
			sliderX[orderNo] = 0;
		}
		currentSlideId.value = '';
	};

	// ==================== UI 事件 ====================
	const switchStatus = async (val) => {
		currentStatus.value = val;
		// 切换tab时重新获取各状态数量
		await fetchAllStatusCount();
		fetchOrders(true);
	};

	const onTypeChange = (e) => {
		filterTypeIndex.value = e.detail.value;
		fetchOrders(true);
	};

	const toggleSort = () => {
		sortEnabled.value = true;
		sortOrder.value = sortOrder.value === 'asc' ? 'desc' : 'asc';
		fetchOrders(true);
	};

	const refreshOrders = () => fetchOrders(true);

	const onScroll = (e) => {
		const {
			scrollTop: st,
			scrollHeight
		} = e.detail;
		if (loading.value || noMore.value || loadMoreLock.value) return;
		if (scrollHeight - st < 900) {
			loadMoreLock.value = true;
			pageNum.value++;
			fetchOrders().finally(() => {
				loadMoreLock.value = false;
			});
		}
	};

	// ==================== 图片预览 ====================
	const openPreview = (urls, current) => {
		previewUrls.value = urls;
		previewIndex.value = current;
		previewVisible.value = true;
		scale.value = 1;
		// #ifdef H5
		document.body.classList.add('preview-lock');
		history.pushState(null, '', location.href);
		// #endif
	};

	const closePreview = () => {
		previewVisible.value = false;
		previewUrls.value = [];
		previewIndex.value = 0;
		scale.value = 1;
		// #ifdef H5
		document.body.classList.remove('preview-lock');
		// #endif
	};

	const onPreviewSwiperChange = (e) => {
		previewIndex.value = e.detail.current;
	};

	const getDistance = (touches) => {
		if (touches.length < 2) return 0;
		const dx = touches[0].pageX - touches[1].pageX;
		const dy = touches[0].pageY - touches[1].pageY;
		return Math.sqrt(dx * dx + dy * dy);
	};

	const onZoomTouchStart = (e) => {
		if (e.touches && e.touches.length === 2) {
			e.preventDefault();
			lastDist = getDistance(e.touches);
		}
	};

	const onZoomTouchMove = (e) => {
		if (e.touches && e.touches.length === 2) {
			e.preventDefault();
			const dist = getDistance(e.touches);
			if (lastDist > 0) {
				const delta = dist / lastDist;
				scale.value = Math.min(Math.max(scale.value * delta, 0.5), 4);
			}
			lastDist = dist;
		}
	};

	const onZoomTouchEnd = () => {
		lastDist = 0;
	};

	const onWheel = (e) => {
		e.preventDefault();
		const delta = e.deltaY > 0 ? 0.95 : 1.05;
		scale.value = Math.min(Math.max(scale.value * delta, 0.5), 4);
	};

	// 返回键拦截
	onBackPress((e) => {
		if (previewVisible.value) {
			closePreview();
			return true;
		}
	});

	const handlePopState = () => {
		if (previewVisible.value) {
			closePreview();
			history.pushState(null, '', location.href);
		}
	};

	// ==================== 生命周期 ====================
	onLoad((options) => {
		if (options?.status !== undefined) {
			currentStatus.value = Number(options.status);
		}
		if (options?.orderNo) {
			targetOrderNo.value = decodeURIComponent(options.orderNo);
		}
	});

	onMounted(async () => {
		const sysInfo = uni.getSystemInfoSync();
		const ratio = sysInfo.windowWidth / 750;
		blockWidthPx.value = blockWidthRpx * ratio;
		pageHeight.value = sysInfo.windowHeight;
		// #ifdef H5
		window.addEventListener('popstate', handlePopState);
		// #endif

		// 获取各状态订单数量（必须在加载列表之前，以保证右上角数字正确）
		await fetchAllStatusCount();
		// 加载订单列表
		fetchOrders(true);
		// 建立纠纷 WebSocket 连接（实时接收纠纷数量更新）
		initDisputeWebSocket();
	});

	onBeforeUnmount(() => {
		if (disputeWs) {
			disputeWs.close();
			disputeWs = null;
		}
		// #ifdef H5
		window.removeEventListener('popstate', handlePopState);
		document.body.classList.remove('preview-lock');
		// #endif
	});
</script>

<style scoped>
	/* 与之前完全一致，省略以避免重复，请保留原有样式 */
	/* 核心布局：页面不可滚动，内部列表滚动 */
	.receive-page {
		display: flex;
		flex-direction: column;
		overflow: hidden;
		background-color: #f5f5f5;
	}

	/* 顶部状态导航 */
	.status-nav {
		white-space: nowrap;
		background: #fff;
		padding: 10rpx 20rpx;
		border-bottom: 1rpx solid #eee;
		flex-shrink: 0;
	}

	.status-tab {
		position: relative;
		display: inline-block;
		padding: 12rpx 28rpx;
		margin-right: 16rpx;
		margin-top: 10rpx;
		font-size: 26rpx;
		color: #666;
		background: #f5f5f5;
		border-radius: 40rpx;
	}

	.status-tab.active {
		background: #333;
		color: #fff;
	}

	/* 小红点样式 */
	.tab-badge {
		position: absolute;
		top: -8rpx;
		right: 100rpx;
		min-width: 32rpx;
		height: 32rpx;
		background: #e74c3c;
		color: #fff;
		border-radius: 32rpx;
		font-size: 20rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		padding: 0 8rpx;
		box-sizing: border-box;
		font-weight: bold;
		line-height: 1;
		white-space: nowrap;
		transform: scale(0.9);
		box-shadow: 0 2rpx 6rpx rgba(0, 0, 0, 0.2);
	}

	/* 中间滚动区域 */
	.main-content {
		flex: 1;
		overflow: hidden;
	}

	.order-list {
		height: 100%;
		padding: 5rpx 32rpx 0;
		box-sizing: border-box;
	}

	/* 订单卡片基础 */
	.order-card {
		background: #fff;
		border-radius: 24rpx;
		margin-bottom: 24rpx;
		padding: 28rpx;
		box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
		border: 2rpx solid #000;
	}

	/* 卡片进场动画 */
	.order-card-in {
		animation: cardFadeInUp 0.5s ease forwards;
	}

	@keyframes cardFadeInUp {
		from {
			opacity: 0;
			transform: translateY(30rpx) scale(0.96);
		}

		to {
			opacity: 1;
			transform: translateY(0) scale(1);
		}
	}

	/* 卡片退场动画 */
	.order-card-leave {
		animation: cardSlideOut 0.5s ease forwards;
	}

	@keyframes cardSlideOut {
		0% {
			transform: scale(1) translateX(0);
			opacity: 1;
		}

		50% {
			transform: scale(0.92) translateX(0);
			opacity: 0.8;
		}

		100% {
			transform: scale(0.85) translateX(120%);
			opacity: 0;
		}
	}

	.card-header {
		display: flex;
		justify-content: space-between;
		align-items: flex-start;
		margin-bottom: 20rpx;
	}

	.delivery-info {
		display: flex;
		flex-direction: column;
	}

	.expect-row {
		font-size: 26rpx;
		color: #333;
		font-weight: 500;
		margin-bottom: 6rpx;
	}

	.remain-row {
		font-size: 24rpx;
		color: #e74c3c;
	}

	.price-area {
		display: flex;
		align-items: flex-start;
	}

	.price-col {
		display: flex;
		flex-direction: column;
	}

	.reward-price {
		font-size: 32rpx;
		font-weight: bold;
		color: #e74c3c;
	}

	.goods-price {
		font-size: 24rpx;
		color: #999;
		margin-bottom: 4rpx;
	}

	.category-tag {
		margin-bottom: 24rpx;
	}

	.primary-tag {
		display: inline-block;
		padding: 6rpx 20rpx;
		border-radius: 30rpx;
		font-size: 24rpx;
		font-weight: 500;
	}

	.address-section {
		margin-bottom: 20rpx;
	}

	.addr-row {
		display: flex;
		align-items: center;
		margin-bottom: 20rpx;
	}

	.addr-row:last-child {
		margin-bottom: 0;
	}

	.addr-icon {
		width: 48rpx;
		height: 48rpx;
		border-radius: 50%;
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 24rpx;
		font-weight: bold;
		margin-right: 16rpx;
		flex-shrink: 0;
		background-color: #b4b4b4;
		color: #fff;
	}

	.addr-text {
		font-size: 28rpx;
		color: #333;
		white-space: nowrap;
		overflow: hidden;
		text-overflow: ellipsis;
	}

	.remark-area {
		background: #f8f8f8;
		border-radius: 16rpx;
		padding: 12rpx 20rpx;
		margin-bottom: 24rpx;
	}

	.remark-text {
		font-size: 24rpx;
		font-weight: 550;
		color: #747474;
	}

	.fetch-code-row {
		margin-bottom: 20rpx;
	}

	.code-tag {
		display: flex;
		flex-wrap: wrap;
		align-items: center;
		background: #f5f5f5;
		border-radius: 12rpx;
		padding: 10rpx 20rpx;
		gap: 10rpx;
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

	.image-area {
		margin-bottom: 20rpx;
	}

	.image-grid {
		display: flex;
		flex-wrap: wrap;
		gap: 12rpx;
		padding: 0 4rpx;
	}

	.grid-image {
		width: 198rpx;
		height: 198rpx;
		border-radius: 12rpx;
		background: #f5f5f5;
	}

	/* 滑块相关 */
	.slider-container {
		margin-top: 8rpx;
		position: relative;
		width: 100%;
		height: 88rpx;
	}

	.slider-track {
		position: relative;
		width: 100%;
		height: 100%;
		overflow: hidden;
		touch-action: none;
		pointer-events: none;
	}

	.track-bg {
		position: absolute;
		left: 0;
		top: 0;
		width: 100%;
		height: 100%;
		background-color: #000;
		border-radius: 15rpx;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.track-text {
		font-size: 32rpx;
		font-weight: bold;
		color: #b2ff15;
		letter-spacing: 2rpx;
		z-index: 1;
		transition: opacity 0.1s;
	}

	.progress-fill {
		position: absolute;
		left: 0;
		top: 0;
		height: 100%;
		background-color: #8B4513;
		border-radius: 15rpx;
		z-index: 2;
	}

	.slider-block {
		pointer-events: auto;
		position: absolute;
		left: 5rpx;
		top: 5rpx;
		width: 85rpx;
		height: 78rpx;
		background-color: #fff;
		border-radius: 10rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
		z-index: 3;
		will-change: transform;
		transition: width 0.1s, height 0.1s, left 0.1s, top 0.1s;
	}

	.slider-block.sliding-active {
		width: 95rpx;
		height: 82rpx;
		left: 3rpx;
		top: 3rpx;
	}

	.slider-icon {
		width: 60%;
		height: 60%;
	}

	.slider-loading {
		width: 100%;
		height: 100%;
		background-color: #8B4513;
		border-radius: 15rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		gap: 12rpx;
	}

	.loading-icon {
		width: 40rpx;
		height: 40rpx;
	}

	.loading-text {
		font-size: 28rpx;
		color: #fff;
		font-weight: bold;
	}

	.slider-success {
		width: 100%;
		height: 100%;
		background-color: #27ae60;
		border-radius: 15rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		gap: 12rpx;
		animation: successPop 0.3s ease;
	}

	.success-icon {
		font-size: 40rpx;
		color: #fff;
		font-weight: bold;
	}

	.success-text {
		font-size: 28rpx;
		color: #fff;
		font-weight: bold;
	}

	@keyframes successPop {
		0% {
			transform: scale(0.8);
			opacity: 0;
		}

		100% {
			transform: scale(1);
			opacity: 1;
		}
	}

	/* 操作按钮（通用） */
	.action-buttons {
		margin-top: 16rpx;
	}

	.action-buttons-row {
		display: flex;
		gap: 16rpx;
	}

	.slide-style-btn {
		width: 100%;
		height: 88rpx;
		line-height: 88rpx;
		text-align: center;
		font-size: 28rpx;
		font-weight: 600;
		background: #8B4513;
		color: #fff;
		border-radius: 15rpx;
		box-sizing: border-box;
	}

	.action-buttons-row .slide-style-btn {
		flex: 1;
	}

	.cancel-order-btn {
		background: #d93026;
	}

	.slide-style-btn.disabled-btn {
		opacity: 0.5;
		pointer-events: none;
	}

	.disabled-swipe {
		height: 88rpx;
		background: #e0e0e0;
		border-radius: 15rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		color: #999;
		font-size: 28rpx;
		margin-top: 8rpx;
	}

	/* 纠纷区块 */
	.dispute-section {
		margin-top: 16rpx;
	}

	.dispute-notice {
		display: flex;
		align-items: center;
		margin-bottom: 16rpx;
	}

	.notice-text {
		font-size: 26rpx;
		color: #e74c3c;
		font-weight: 500;
		flex: 1;
	}

	.dispute-buttons {
		display: flex;
		gap: 20rpx;
	}

	.dispute-btn {
		flex: 1;
		text-align: center;
		padding: 22rpx 0;
		border-radius: 15rpx;
		font-size: 28rpx;
		font-weight: 600;
	}

	.dispute-btn.agree {
		background: #e74c3c;
		color: #fff;
	}

	.dispute-btn.reject {
		background: #333;
		color: #fff;
	}

	/* 时间明细折叠 */
	.time-info-wrap {
		margin-top: 16rpx;
		border-top: 1rpx solid #f0f0f0;
		padding-top: 16rpx;
	}

	.time-toggle-bar {
		display: flex;
		justify-content: space-between;
		align-items: center;
		padding: 10rpx 0;
		font-size: 26rpx;
		color: #333;
	}

	.toggle-title {
		font-weight: 500;
	}

	.toggle-arrow {
		font-size: 24rpx;
		color: #666;
	}

	.time-info-content {
		background: #fafafa;
		border-radius: 12rpx;
		padding: 20rpx;
		margin-top: 10rpx;
	}

	.time-item {
		display: flex;
		align-items: center;
		font-size: 24rpx;
		line-height: 50rpx;
	}

	.time-label {
		color: #666;
		width: 140rpx;
		flex-shrink: 0;
	}

	.time-val {
		color: #333;
		flex: 1;
	}

	.load-more,
	.empty {
		text-align: center;
		padding: 30rpx 0;
		font-size: 26rpx;
		color: #999;
	}

	/* 底部操作栏 */
	.bottom-bar {
		height: 100rpx;
		background: #fff;
		display: flex;
		align-items: center;
		justify-content: space-between;
		padding: 0 24rpx;
		padding-bottom: env(safe-area-inset-bottom);
		box-shadow: 0 -2rpx 12rpx rgba(0, 0, 0, 0.04);
		flex-shrink: 0;
	}

	.filter-picker {
		flex-shrink: 0;
	}

	.picker-item {
		display: flex;
		align-items: center;
		padding: 12rpx 20rpx;
		background: #f5f5f5;
		border-radius: 40rpx;
		font-size: 26rpx;
		color: #333;
	}

	.picker-label {
		max-width: 120rpx;
		white-space: nowrap;
		overflow: hidden;
		text-overflow: ellipsis;
	}

	.refresh-btn {
		display: flex;
		align-items: center;
		gap: 8rpx;
		padding: 14rpx 32rpx;
		background: linear-gradient(135deg, #333, #555);
		border-radius: 50rpx;
		color: #fff;
		font-size: 26rpx;
		box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
		flex-shrink: 0;
	}

	.refresh-icon {
		font-size: 30rpx;
		font-weight: bold;
	}

	.sort-btn {
		display: flex;
		align-items: center;
		padding: 12rpx 20rpx;
		background: #f5f5f5;
		border-radius: 40rpx;
		font-size: 26rpx;
		color: #666;
		flex-shrink: 0;
	}

	.arrow {
		margin-left: 6rpx;
		font-size: 22rpx;
		color: #999;
	}

	.reason-mask {
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
		box-sizing: border-box;
	}

	.reason-dialog {
		width: 100%;
		background: #fff;
		border-radius: 24rpx;
		padding: 34rpx 28rpx 28rpx;
		box-sizing: border-box;
	}

	.reason-title {
		font-size: 34rpx;
		font-weight: 700;
		color: #1a1a1a;
		text-align: center;
		margin-bottom: 10rpx;
	}

	.reason-subtitle {
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
		color: #d93026;
		background: #fff2f0;
		border-color: #d93026;
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

	.reason-actions {
		display: flex;
		justify-content: flex-end;
		gap: 16rpx;
		margin-top: 28rpx;
	}

	.reason-btn {
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

	.reason-close {
		color: #666;
		background: #fff;
	}

	.reason-submit {
		color: #fff;
		background: #d93026;
		border-color: #d93026;
	}

	.reason-submit.disabled {
		opacity: 0.6;
	}
</style>

<style>
	/* 预览遮罩（全局） */
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

	body.preview-lock {
		overflow: hidden;
	}
</style>
