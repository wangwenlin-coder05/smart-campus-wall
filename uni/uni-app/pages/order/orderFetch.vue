<template>
	<view class="wrap">
		<!-- 地址卡片（取件地址 + 送达地址） -->
		<view class="card address-card">
			<view class="addr-item" @click="goSelectAddress('start')">
				<view class="addr-icon start-icon">取</view>
				<view class="addr-text">
					<text v-if="startAddress">{{ startAddress.displayName }}</text>
					<text v-else class="placeholder">取件地址</text>
				</view>
				<view class="arrow">›</view>
			</view>
			<view class="addr-item" @click="goSelectAddress('end')" style="border-bottom: none;">
				<view class="addr-icon end-icon">终</view>
				<view class="addr-text">
					<text v-if="endAddress">{{ endAddress.displayName }}</text>
					<text v-else class="placeholder">送到哪里</text>
				</view>
				<view class="arrow">›</view>
			</view>
		</view>

		<!-- 服务类型卡片（每行5个，等大方块，自动换行） -->
		<view class="card">
			<view class="card-title">
				<text class="label">服务类型</text>
			</view>
			<view class="service-grid">
				<view v-for="(item, index) in serviceTypeList" :key="index" class="service-item"
					:class="{ active: subType === item.name }" @click="onSubTypeChange(item.name)">
					<image class="service-icon-img" :src="item.icon" mode="aspectFit"></image>
					<text class="service-name">{{ item.name }}</text>
				</view>
			</view>
		</view>

		<!-- ========== 代取服务（parentTypeId === 1） ========== -->
		<template v-if="parentTypeId === 1">
			<!-- 快递包裹：显示规格选择，规格总价作为赏金 -->
			<view class="card" v-if="subType === '快递包裹'">
				<view class="spec-item" v-for="(spec, index) in specList" :key="index">
					<view class="spec-left">
						<image class="spec-img" src="/static/11/express.png" mode="aspectFit"></image>
						<view class="spec-info">
							<text class="spec-name">{{ spec.name }}</text>
							<text class="spec-desc">{{ spec.desc }}</text>
							<text class="spec-price">¥{{ spec.price }}</text>
						</view>
					</view>
					<view class="spec-right">
						<view class="count-btn" @click="countChange(index, -1)">-</view>
						<text class="count-num">{{ spec.count }}</text>
						<view class="count-btn" @click="countChange(index, 1)">+</view>
					</view>
				</view>
			</view>
			<!-- 非快递包裹：赏金输入 -->
			<view class="card" v-else>
				<view class="card-title">
					<text class="label">赏金 (元)</text>
				</view>
				<input id="reward-input-1" class="form-input budget-input" v-model="rewardPrice" type="digit"
					placeholder="请输入赏金金额" :adjust-position="true" :cursor-spacing="150" @focus="onFocus"
					@input="onRewardInput" />
			</view>
		</template>

		<!-- ========== 帮买服务（parentTypeId === 2） ========== -->
		<template v-if="parentTypeId === 2">
			<view class="card">
				<view class="card-title">
					<text class="label">商品信息</text>
				</view>
				<view class="price-row">
					<view class="price-item">
						<text class="price-label">物品金额</text>
						<input id="goods-budget-input" class="form-input price-input" v-model="goodsBudget" type="digit"
							placeholder="预估价格" :adjust-position="true" :cursor-spacing="150" @focus="onFocus"
							@input="onGoodsBudgetInput" />
					</view>
					<view class="price-item">
						<text class="price-label">赏金</text>
						<input id="reward-input-2" class="form-input price-input" v-model="rewardPrice" type="digit"
							placeholder="跑腿赏金" :adjust-position="true" :cursor-spacing="150" @focus="onFocus"
							@input="onRewardInput" />
					</view>
				</view>
			</view>
		</template>

		<!-- ========== 跑腿代办（parentTypeId === 3） ========== -->
		<template v-if="parentTypeId === 3">
			<view class="card">
				<view class="card-title">
					<text class="label">赏金 (元)</text>
				</view>
				<input id="reward-input-3" class="form-input budget-input" v-model="rewardPrice" type="digit"
					placeholder="请输入跑腿赏金" :adjust-position="true" :cursor-spacing="150" @focus="onFocus"
					@input="onRewardInput" />
			</view>
		</template>

		<!-- 期望送达时间卡片 -->
		<view class="card">
			<view class="card-title">
				<text class="label">{{ expectTitle }}</text>
			</view>
			<view class="time-picker-row">
				<picker class="time-picker-item" mode="date" :value="expectDate" @change="onDateChange">
					<view class="picker-display">{{ expectDate || '选择日期' }}</view>
				</picker>
				<picker class="time-picker-item" mode="time" :value="expectTime" @change="onTimeChange">
					<view class="picker-display">{{ expectTime || '选择时间' }}</view>
				</picker>
			</view>
		</view>

		<!-- 图片上传卡片 -->
		<view class="card">
			<view class="card-title">
				<text class="label">📷 图片上传</text>
				<text class="count-tip">({{ uploadImages.length }}/9) 最多上传9张图片,可空</text>
			</view>
			<view class="upload-grid">
				<view v-for="(img, idx) in uploadImages" :key="idx" class="upload-item" @click="previewImage(idx)">
					<image :src="img" mode="aspectFill"></image>
					<view class="del-btn" @click.stop="delImage(idx)">×</view>
				</view>
				<view v-if="uploadImages.length < 9" class="upload-btn" @click="chooseImage">+</view>
			</view>
		</view>

		<!-- 取件码卡片 -->
		<view class="card">
			<view class="card-title">
				<text class="label">📦 取件码或物品信息</text>
			</view>
			<textarea id="fetch-code-textarea" class="code-textarea" v-model="fetchCode" placeholder="输入取件码，如图片已包含可不填"
				:auto-height="false" :cursor-spacing="200" @focus="onFocus"></textarea>
		</view>

		<!-- 订单备注卡片 -->
		<view class="card">
			<view class="card-title">
				<text class="label">📝 订单备注</text>
			</view>
			<textarea id="remark-textarea" class="remark-textarea" v-model="remark" placeholder="填写特殊要求、联系方式等"
				:auto-height="true" :cursor-spacing="200" @focus="onFocus"></textarea>
		</view>

		<!-- 底部提交栏 -->
		<view class="submit-bar">
			<view class="total">合计：¥{{ totalPrice }}</view>
			<view class="submit-btn" :class="{ disabled: submitting }" @click="submitOrder">
				{{ submitting ? '提交中...' : '提交订单' }}
			</view>
		</view>
	</view>
</template>

<script setup>
	import {
		uploadMultipleFiles
	} from '@/utils/upload.js'
	import {
		ref,
		computed,
		onMounted,
		onUnmounted,
		getCurrentInstance,
		watch
	} from 'vue'
	import request from '@/utils/request.js'
	import { getCurrentUser } from '@/utils/auth.js'

	const uploadUrls = ref([])
	const submitting = ref(false)
	// ---------- 获取当前组件实例（用于选择器） ----------
	const instance = getCurrentInstance()

	// ---------- 一级分类动态配置 ----------
	const parentTypeId = ref(1) // 1=代取服务, 2=帮买服务, 3=跑腿代办
	const parentTypeName = ref('代取服务')

	// 二级分类配置（图标路径自行准备）
	const serviceConfig = {
		1: [{
				name: '快递包裹',
				icon: '/static/order/package.png'
			},
			{
				name: '奶茶外卖',
				icon: '/static/order/service_milk_tea.png'
			},
			{
				name: '文件资料',
				icon: '/static/order/service_file.png'
			},
			{
				name: '生活用品',
				icon: '/static/order/service_daily.png'
			},
			{
				name: '其他',
				icon: '/static/order/service_other.png'
			}
		],
		2: [{
				name: '日常用品',
				icon: '/static/order/service_daily.png'
			},
			{
				name: '药品采购',
				icon: '/static/order/medicine.png'
			},
			{
				name: '零食饮料',
				icon: '/static/order/snack.png'
			},
			{
				name: '瓜果生鲜',
				icon: '/static/order/fruit.png'
			},
			{
				name: '其他',
				icon: '/static/order/service_other.png'
			}
		],
		3: [{
				name: '寻找替身',
				icon: '/static/order/stand.png'
			},
			{
				name: '校外捎带',
				icon: '/static/order/outside.png'
			},
			{
				name: '校内跑腿',
				icon: '/static/order/run.png'
			},
			{
				name: '搬运租凭',
				icon: '/static/order/carry.png'
			},
			{
				name: '其他',
				icon: '/static/order/service_other.png'
			}
		]
	}

	const serviceTypeList = ref(serviceConfig[1])
	const subType = ref(serviceTypeList.value[0].name)

	const selectType = ref('')
	const startAddress = ref(null)
	const endAddress = ref(null)

	const fetchCode = ref('')
	const remark = ref('')
	// 字段语义：goodsBudget = 物品金额，rewardPrice = 赏金，订单总价 = goodsBudget + rewardPrice
	const goodsBudget = ref('')   // 物品金额（帮买场景下的商品价格）
	const rewardPrice = ref('')   // 赏金（快递规格总价 或 手动输入的跑腿费）
	const uploadImages = ref([])

	const expectDate = ref('')
	const expectTime = ref('')

	const specList = ref([{
			name: '小件',
			desc: '巴掌大的快递',
			price: 2,
			count: 0
		},
		{
			name: '中间',
			desc: '鞋盒那么大',
			price: 4,
			count: 0
		},
		{
			name: '大件',
			desc: '皮箱那么大',
			price: 6,
			count: 0
		}
	])

	// ---------- 辅助函数：根据规格计算赏金（仅快递包裹场景） ----------
	const updateRewardFromSpec = () => {
		if (parentTypeId.value === 1 && subType.value === '快递包裹') {
			let total = 0
			specList.value.forEach(item => total += item.price * item.count)
			rewardPrice.value = total.toFixed(2)
		}
	}

	// ---------- 监听规格变化自动更新赏金 ----------
	watch(specList, () => {
		updateRewardFromSpec()
	}, { deep: true })

	// ---------- 子类型切换处理 ----------
	const onSubTypeChange = (newType) => {
		subType.value = newType
		resetSpecCount()
		// 若切换到快递包裹，立即根据规格计算赏金
		if (parentTypeId.value === 1 && newType === '快递包裹') {
			updateRewardFromSpec()
		}
	}

	// ---------- 初始化默认期望时间 ----------
	const setDefaultExpectTime = () => {
		const now = new Date()
		const offsetHours = (parentTypeId.value === 1 && subType.value === '快递包裹') ? 24 : 2
		const target = new Date(now.getTime() + offsetHours * 60 * 60 * 1000)
		const y = target.getFullYear()
		const m = String(target.getMonth() + 1).padStart(2, '0')
		const d = String(target.getDate()).padStart(2, '0')
		const h = String(target.getHours()).padStart(2, '0')
		const min = String(target.getMinutes()).padStart(2, '0')
		expectDate.value = `${y}-${m}-${d}`
		expectTime.value = `${h}:${min}`
	}

	onMounted(() => {
		const pages = getCurrentPages()
		const curPage = pages[pages.length - 1]
		const queryType = curPage?.options?.parentType
		if (queryType) {
			const type = parseInt(queryType)
			parentTypeId.value = type
			const nameMap = {
				1: '代取服务',
				2: '帮买服务',
				3: '跑腿代办'
			}
			parentTypeName.value = nameMap[type] || '代取服务'
			serviceTypeList.value = serviceConfig[type] || serviceConfig[1]
			subType.value = serviceTypeList.value[0].name
		}
		setDefaultExpectTime()
		uni.$on('addressSelected', onAddressSelected)
		// 若初始为快递包裹，计算赏金
		if (parentTypeId.value === 1 && subType.value === '快递包裹') {
			updateRewardFromSpec()
		}
	})

	onUnmounted(() => uni.$off('addressSelected', onAddressSelected))

	const expectTitle = computed(() => {
		if (parentTypeId.value === 3) return '时间（默认2小时内）'
		if (parentTypeId.value === 1 && subType.value === '快递包裹') return '⏰ 期望送达（默认24小时内）'
		return '⏰ 期望送达（默认2小时内）'
	})

	// ---------- 事件处理 ----------
	const countChange = (index, delta) => {
		const newCount = specList.value[index].count + delta
		if (newCount >= 0) specList.value[index].count = newCount
		// 规格变更会通过watch自动更新rewardPrice
	}

	const resetSpecCount = () => {
		specList.value.forEach(item => item.count = 0)
		goodsBudget.value = ''
		rewardPrice.value = ''
		setDefaultExpectTime()
	}

	const onDateChange = (e) => {
		expectDate.value = e.detail.value
	}
	const onTimeChange = (e) => {
		expectTime.value = e.detail.value
	}

	// 物品金额输入处理
	const onGoodsBudgetInput = (e) => {
		goodsBudget.value = e.detail.value.replace(/[^\d.]/g, '')
	}
	// 赏金输入处理
	const onRewardInput = (e) => {
		rewardPrice.value = e.detail.value.replace(/[^\d.]/g, '')
	}

	const goSelectAddress = (type) => {
		selectType.value = type
		uni.navigateTo({
			url: '/pages/address/list?mode=select'
		})
	}

	const onAddressSelected = (data) => {
		if (!data) return
		const addressDetail = data.address || data.name || '未知地址'
		const typeText = data.addressType === 1 ? '校内' : '校外'
		const displayName = `${addressDetail}（${typeText}）`
		const selected = {
			addressNo: data.addressNo,
			displayName
		}
		if (selectType.value === 'start') startAddress.value = selected
		else if (selectType.value === 'end') endAddress.value = selected
	}

	// 图片上传相关
	const chooseImage = () => {
		const remain = 9 - uploadImages.value.length
		if (remain <= 0) return uni.showToast({
			title: '最多上传9张图片',
			icon: 'none'
		})
		uni.chooseImage({
			count: remain,
			sizeType: ['compressed'],
			sourceType: ['album', 'camera'],
			success: (res) => uploadImages.value.push(...res.tempFilePaths)
		})
	}

	const delImage = (index) => uploadImages.value.splice(index, 1)
	const previewImage = (currentIndex) => {
		if (!uploadImages.value.length) return
		uni.previewImage({
			urls: uploadImages.value,
			current: currentIndex,
			indicator: 'number',
			loop: true
		})
	}

	// ---------- 键盘自动定位 ----------
	const onFocus = (e) => {
		setTimeout(() => {
			// #ifdef H5
			const el = document.getElementById(e.target.id)
			if (el) {
				const rect = el.getBoundingClientRect()
				const scrollTop = rect.top + window.pageYOffset - 230
				window.scrollTo({
					top: scrollTop,
					behavior: 'smooth'
				})
			}
			// #endif
			// #ifndef H5
			uni.pageScrollTo({
				selector: '#' + e.target.id,
				duration: 150,
				offsetTop: -230
			})
			// #endif
		}, 200)
	}

	// ---------- 总价计算：物品金额 + 赏金 ----------
	const totalPrice = computed(() => {
		const budget = parseFloat(goodsBudget.value) || 0
		const reward = parseFloat(rewardPrice.value) || 0
		return (budget + reward).toFixed(2)
	})

	// ---------- 提交订单 ----------
	const submitOrder = async () => {
		if (submitting.value) return
		const currentUser = getCurrentUser()
		if (!currentUser || !currentUser.uid) {
			uni.navigateTo({ url: '/pages/login/login' })
			return
		}
		if (!startAddress.value) return uni.showToast({
			title: '请选择取件地址',
			icon: 'error'
		})
		if (!endAddress.value) return uni.showToast({
			title: '请选择送达地址',
			icon: 'error'
		})

		const orderPriceVal = parseFloat(totalPrice.value)
		if (isNaN(orderPriceVal) || orderPriceVal < 1) {
			return uni.showToast({
				title: '订单金额无效或小于1元',
				icon: 'error'
			})
		}

		// 校验赏金（如果赏金大于0则不能小于0.01）
		const rawReward = parseFloat(rewardPrice.value)
		if (!isNaN(rawReward) && rawReward > 0 && rawReward < 0.01) {
			return uni.showToast({
				title: '赏金不能低于0.01元',
				icon: 'error'
			})
		}
		// 校验物品金额（如果物品金额大于0则不能小于0.01）
		const rawGoodsBudget = parseFloat(goodsBudget.value)
		if (!isNaN(rawGoodsBudget) && rawGoodsBudget > 0 && rawGoodsBudget < 0.01) {
			return uni.showToast({
				title: '物品金额不能低于0.01元',
				icon: 'error'
			})
		}

		const finalGoodsName = subType.value

		let loadingVisible = false
		try {
			submitting.value = true
			let uploadedImgUrls = []
			if (uploadImages.value.length > 0) {
				uni.showLoading({
					title: '图片上传中...',
					mask: true
				})
				loadingVisible = true
				const fileList = uploadImages.value.map((path) => {
					return {
						path: path,
						ext: path.split('.').pop().toLowerCase()
					}
				})
				uploadedImgUrls = await uploadMultipleFiles(
					fileList,
					'order',
					(index, process) => {
						uni.showToast({
							title: `第${index+1}个 ${process}%`,
							icon: 'none',
							duration: 800
						})
					}
				)
				uni.hideLoading()
				loadingVisible = false
			}

			uni.showLoading({
				title: '数据提交中...',
				mask: true
			})
			loadingVisible = true

			// 组装订单参数
			const orderParam = {
				userUid: currentUser.uid,
				parentType: parentTypeName.value,
				subType: subType.value,
				startAddressNo: startAddress.value.addressNo,
				endAddressNo: endAddress.value.addressNo,
				goodsName: finalGoodsName,
				fetchCode: fetchCode.value || '',
				remark: remark.value || '',
				orderPrice: orderPriceVal,
				orderStatus: 0,
				payStatus: 1,
				orderImageList: uploadedImgUrls,
				expectTime: `${expectDate.value} ${expectTime.value}`,
				goodsBudget: 0,   // 物品金额，先默认0
				rewardPrice: 0    // 赏金，先默认0
			}

			// 根据不同场景赋值 goodsBudget 和 rewardPrice
			if (parentTypeId.value === 1) {
				if (subType.value === '快递包裹') {
					// 快递包裹：规格总价作为赏金，物品金额为0
					orderParam.rewardPrice = parseFloat(rewardPrice.value) || 0
					orderParam.goodsBudget = 0
				} else {
					// 代取其他：只有赏金
					orderParam.rewardPrice = parseFloat(rewardPrice.value) || 0
					orderParam.goodsBudget = 0
				}
			} else if (parentTypeId.value === 2) {
				// 帮买服务：物品金额 + 赏金
				orderParam.goodsBudget = parseFloat(goodsBudget.value) || 0
				orderParam.rewardPrice = parseFloat(rewardPrice.value) || 0
			} else if (parentTypeId.value === 3) {
				// 跑腿代办：只有赏金
				orderParam.rewardPrice = parseFloat(rewardPrice.value) || 0
				orderParam.goodsBudget = 0
			}

			console.log(orderParam)
			const submitRes = await request({
				url: '/order/addOrderDataByUid',
				method: 'POST',
				data: orderParam
			})

			uni.hideLoading()
			loadingVisible = false
			uni.showToast({
				title: '订单发布成功',
				icon: 'success'
			})
			setTimeout(() => uni.navigateBack(), 1200)
		} catch (err) {
			if (loadingVisible) {
				uni.hideLoading()
				loadingVisible = false
			}
			uni.showToast({
				title: '提交失败，请重试',
				icon: 'error'
			})
		} finally {
			submitting.value = false
			if (loadingVisible) {
				uni.hideLoading()
			}
		}
	}
</script>

<style scoped>
	page {
		background-color: #f5f5f5;
	}

	.wrap {
		padding-bottom: 140rpx;
		padding-top: 40rpx;
	}

	.card {
		background-color: #ffffff;
		border-radius: 20rpx;
		padding: 32rpx;
		margin: 0 32rpx 32rpx 32rpx;
	}

	.card-title {
		display: flex;
		justify-content: space-between;
		align-items: baseline;
		margin-bottom: 24rpx;
	}

	.label {
		font-size: 30rpx;
		font-weight: 500;
		color: #1a1a1a;
	}

	.count-tip {
		font-size: 24rpx;
		color: #999;
	}

	.address-card {
		padding: 0;
	}

	.addr-item {
		display: flex;
		align-items: center;
		padding: 32rpx;
		border-bottom: 1rpx solid #f0f0f0;
	}

	.addr-icon {
		width: 48rpx;
		height: 48rpx;
		border-radius: 50%;
		display: flex;
		align-items: center;
		justify-content: center;
		margin-right: 20rpx;
		font-size: 26rpx;
		font-weight: 600;
	}

	.start-icon {
		background-color: #e3f2fd;
		color: #1e88e5;
	}

	.end-icon {
		background-color: #f3e5f5;
		color: #8e24aa;
	}

	.addr-text {
		flex: 1;
		font-size: 28rpx;
		color: #333;
	}

	.placeholder {
		color: #bbb;
	}

	.arrow {
		font-size: 36rpx;
		color: #ccc;
		font-weight: 300;
	}

	/* 服务类型网格 */
	.service-grid {
		display: flex;
		flex-wrap: wrap;
		gap: 16rpx;
	}

	.service-item {
		width: calc((100% - 4 * 16rpx) / 5);
		padding: 20rpx 0;
		border-radius: 20rpx;
		background-color: #fafafa;
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		transition: all 0.2s;
		box-sizing: border-box;
	}

	.service-item.active {
		background-color: #f0f0f0;
		transform: scale(1.05);
	}

	.service-icon-img {
		width: 48rpx;
		height: 48rpx;
		margin-bottom: 12rpx;
	}

	.service-item.active .service-icon-img {
		filter: brightness(0.7);
	}

	.service-name {
		font-size: 24rpx;
		color: #999;
		text-align: center;
		white-space: nowrap;
		overflow: hidden;
		text-overflow: ellipsis;
		max-width: 100%;
	}

	.service-item.active .service-name {
		color: #333;
		font-weight: 400;
	}

	/* 快递规格 */
	.spec-item {
		display: flex;
		align-items: center;
		justify-content: space-between;
		padding: 24rpx 0;
		border-bottom: 1rpx solid #f0f0f0;
	}

	.spec-item:last-child {
		border-bottom: none;
	}

	.spec-left {
		display: flex;
		align-items: center;
		gap: 20rpx;
	}

	.spec-img {
		width: 72rpx;
		height: 72rpx;
		border-radius: 12rpx;
		background-color: #f9e45b;
	}

	.spec-info {
		display: flex;
		flex-direction: column;
	}

	.spec-name {
		font-size: 30rpx;
		font-weight: 700;
		color: #222;
	}

	.spec-desc {
		font-size: 24rpx;
		color: #999;
		margin-top: 6rpx;
	}

	.spec-price {
		font-size: 26rpx;
		color: #e74c3c;
		margin-top: 8rpx;
	}

	.spec-right {
		display: flex;
		align-items: center;
		gap: 20rpx;
	}

	.count-btn {
		width: 60rpx;
		height: 60rpx;
		background-color: #f5f5f5;
		border-radius: 8rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 36rpx;
		font-weight: bold;
		color: #666;
	}

	.count-num {
		min-width: 48rpx;
		text-align: center;
		font-size: 30rpx;
		color: #333;
	}

	/* 通用输入框 */
	.form-input {
		background-color: #f5f5f5;
		border-radius: 12rpx;
		padding: 20rpx 24rpx;
		font-size: 28rpx;
		color: #333;
		width: 100%;
		box-sizing: border-box;
	}

	.budget-input {
		min-height: 80rpx;
		line-height: 1.4;
	}

	/* 帮买一行布局 */
	.price-row {
		display: flex;
		gap: 20rpx;
	}

	.price-item {
		flex: 1;
	}

	.price-label {
		font-size: 26rpx;
		color: #666;
		margin-bottom: 12rpx;
		display: block;
	}

	.price-input {
		height: 80rpx;
		padding: 0 20rpx;
		font-size: 30rpx;
		background-color: #f5f5f5;
		border-radius: 12rpx;
		text-align: center;
		box-sizing: border-box;
		position: relative;
	}

	/* 时间选择器 */
	.time-picker-row {
		display: flex;
		gap: 20rpx;
	}

	.time-picker-item {
		flex: 1;
		background-color: #f5f5f5;
		border-radius: 12rpx;
		padding: 20rpx 24rpx;
	}

	.picker-display {
		font-size: 28rpx;
		color: #333;
		text-align: center;
	}

	/* 图片上传 */
	.upload-grid {
		display: flex;
		flex-wrap: wrap;
		gap: 10rpx;
	}

	.upload-item,
	.upload-btn {
		width: 199rpx;
		height: 199rpx;
		border-radius: 16rpx;
		position: relative;
	}

	.upload-item image {
		width: 100%;
		height: 100%;
		border-radius: 16rpx;
	}

	.del-btn {
		position: absolute;
		top: -0rpx;
		right: -0rpx;
		width: 40rpx;
		height: 40rpx;
		background: rgba(62, 62, 62, 0.6);
		color: #fff;
		border-radius: 50%;
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 32rpx;
	}

	.upload-btn {
		background-color: #f5f5f5;
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 56rpx;
		color: #999;
		border: 1rpx dashed #ddd;
	}

	/* 文本域 */
	.code-textarea {
		background-color: #f5f5f5;
		border-radius: 12rpx;
		padding: 24rpx;
		font-size: 28rpx;
		width: 100%;
		height: 300rpx;
		box-sizing: border-box;
	}

	.remark-textarea {
		background-color: #f5f5f5;
		border-radius: 12rpx;
		padding: 24rpx;
		font-size: 28rpx;
		width: 100%;
		min-height: 160rpx;
		box-sizing: border-box;
		overflow: hidden;
		resize: none;
	}

	/* 底部提交栏 */
	.submit-bar {
		position: fixed;
		bottom: 0;
		left: 0;
		right: 0;
		background-color: #fff;
		display: flex;
		align-items: center;
		justify-content: space-between;
		padding: 20rpx 32rpx;
		padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
		box-shadow: 0 -2rpx 12rpx rgba(0, 0, 0, 0.04);
		z-index: 10;
	}

	.total {
		font-size: 32rpx;
		font-weight: 600;
		color: #e74c3c;
	}

	.submit-btn {
		background-color: #333;
		color: #fff;
		font-size: 32rpx;
		font-weight: 500;
		padding: 24rpx 48rpx;
		border-radius: 40rpx;
		transition: 0.2s;
	}

	.submit-btn:active {
		opacity: 0.9;
		transform: scale(0.98);
	}

	.submit-btn.disabled {
		opacity: 0.55;
		pointer-events: none;
	}
</style>
