<template>
	<view class="preview-mask" v-if="visible" @click.self="handleClose">
		<swiper 
			class="preview-swiper" 
			:current="currentIndex" 
			@change="onSwiperChange"
			indicator-dots 
			indicator-color="rgba(255,255,255,0.5)" 
			indicator-active-color="#fff"
			@touchmove.stop 
			@mousedown.stop 
			@mouseup.stop 
			@click.stop
		>
			<swiper-item v-for="(url, idx) in urls" :key="idx">
				<image :src="url" mode="aspectFit" class="preview-image" @click.stop="handleClose" />
			</swiper-item>
		</swiper>
		<view class="close-btn" @click.stop="handleClose">✕</view>
	</view>
</template>

<script setup>
	import { onMounted, onBeforeUnmount, watch } from 'vue'
	import { onBackPress } from '@dcloudio/uni-app'

	const props = defineProps({
		visible: {
			type: Boolean,
			default: false
		},
		urls: {
			type: Array,
			default: () => []
		},
		currentIndex: {
			type: Number,
			default: 0
		}
	})

	const emit = defineEmits(['update:visible', 'update:currentIndex'])

	const handleClose = () => {
		emit('update:visible', false)
		document.body.classList.remove('preview-lock')
	}

	const onSwiperChange = (e) => {
		emit('update:currentIndex', e.detail.current)
	}

	const handlePopState = () => {
		if (props.visible) {
			handleClose()
		}
	}

	const backPressHandler = () => {
		if (props.visible) {
			handleClose()
			return true
		}
	}

	watch(() => props.visible, (val) => {
		if (val) {
			document.body.classList.add('preview-lock')
			// #ifdef H5
			history.pushState(null, '', location.href)
			// #endif
		} else {
			document.body.classList.remove('preview-lock')
		}
	})

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

	onBackPress(backPressHandler)
</script>

<style scoped>
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
	}

	.preview-image {
		width: 100%;
		height: 100%;
		object-fit: contain;
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
	}
</style>

<style>
	body.preview-lock {
		overflow: hidden;
	}

	::-webkit-scrollbar {
		display: none !important;
		width: 0 !important;
		height: 0 !important;
		background: transparent !important;
	}
</style>