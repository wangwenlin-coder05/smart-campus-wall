<script setup>
import { initGlobalWebSocket, updateGlobalBadge } from '@/utils/globalWebSocket.js'
</script>

<script>
	export default {
		onLaunch: function() {
			console.log('App Launch')
			try { initGlobalWebSocket() } catch(e) {}
			try { setTimeout(() => updateGlobalBadge(), 500) } catch(e) {}
		},
		onShow: function() {
			console.log('App Show')
			try { initGlobalWebSocket() } catch(e) {}
			try { updateGlobalBadge() } catch(e) {}
		},
		onHide: function() {
			console.log('App Hide')
		}
	}
</script>

<style>
	/* 全局CSS Reset —— 统一清除浏览器默认样式，消除平台差异 */
	page {
		--status-bar-height: 0px;
		margin: 0;
		padding: 0;
		box-sizing: border-box;
		font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
		font-size: 28rpx;
		color: #333;
		background-color: #f5f5f5;
		-webkit-font-smoothing: antialiased;
		-webkit-tap-highlight-color: transparent;
	}

	view, text, image, scroll-view, swiper, swiper-item,
	navigator, button, input, textarea, label, form,
	block, cover-view, cover-image, movable-area, movable-view {
		margin: 0;
		padding: 0;
		box-sizing: border-box;
	}

	text {
		word-break: break-all;
		line-height: 1.5;
	}

	image {
		display: block;
	}

	button {
		margin: 0;
		padding: 0;
		border: none;
		border-radius: 0;
		background: none;
		font-size: inherit;
		color: inherit;
		line-height: inherit;
	}

	button::after {
		border: none;
	}

	input, textarea {
		font-size: inherit;
		background: none;
	}

	navigator {
		color: inherit;
		text-decoration: none;
	}

	/* 弹性布局快捷类 */
	.flex { display: flex; }
	.flex-center { display: flex; align-items: center; justify-content: center; }
	.flex-between { display: flex; align-items: center; justify-content: space-between; }
	.flex-1 { flex: 1; }

	/* 文本省略 */
	.ellipsis { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
</style>