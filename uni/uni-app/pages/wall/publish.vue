<template>
	<view class="publish-page">
		<!-- 1. 顶部输入区 -->
		<view class="card">
			<textarea v-model="content" placeholder="请输入内容" maxlength="750" class="content-textarea" :auto-height="true"
				@input="onContentInput" />
			<view class="char-count">{{ content.length }}/750</view>

			<!-- 媒体上传区域 -->
			<view class="media-upload">
				<view class="media-grid" v-if="mediaFiles.length > 0">
					<view v-for="(file, idx) in mediaFiles" :key="idx" class="media-item"
						:class="{ sorting: sortFromIndex === idx }"
						@click="handleMediaTap(idx)"
						@longpress="startSortMedia(idx)">
						<image v-if="file.type === 'image'" :src="file.tempPath" mode="aspectFill" :lazy-load="false"
							class="media-preview" />
						<view v-else-if="file.type === 'video'" class="video-thumb-wrapper">
							<video v-if="shouldUseNativeVideoThumb(file)" :src="file.tempPath"
								class="media-preview video-thumb native-video-thumb" :controls="false"
								:show-center-play-btn="false" muted autoplay loop :initial-time="0.1"
								object-fit="cover" />
							<image v-else :src="getVideoThumb(file)" mode="aspectFill" :lazy-load="false"
								class="media-preview video-thumb" />
							<view class="play-overlay">
								<text class="play-icon">▶</text>
							</view>
						</view>
						<view class="delete-icon" @click.stop="removeMedia(idx)">×</view>
						<view v-if="sortFromIndex === idx" class="sort-badge">换位</view>
					</view>
				</view>
				<view v-if="mediaFiles.length < 9" class="add-media-btn" @click="chooseMedia">
					<text class="add-icon">+</text>
					<text class="add-text">上传图片/视频</text>
				</view>
			</view>
		</view>

		<!-- 2. 功能选项区 -->
		<view class="card">
			<!-- 分类选择 -->
			<view class="option-item" @click="openCategoryPicker">
				<view class="option-left">
					<!-- <image src="/static/wall/publish/category.png" class="option-icon" mode="aspectFit" /> -->
					<text class="option-label">请选择分类</text>
				</view>
				<view class="option-right">
					<text class="option-value">{{ selectedCategoryName || '未选择' }}</text>
					<text class="arrow">›</text>
				</view>
			</view>

			<!-- 马甲身份（匿名） -->
			<view class="option-item">
				<view class="option-left">
					<image src="/static/wall/publish/anonymous.png" class="option-icon" mode="aspectFit" />
					<text class="option-label">马甲身份</text>
				</view>
				<switch :checked="isAnonymous" @change="isAnonymous = $event.detail.value" color="#007aff" />
			</view>

			<!-- 置顶 -->
			<view class="option-item">
				<view class="option-left">
					<image src="/static/wall/publish/top.png" class="option-icon" mode="aspectFit" />
					<text class="option-label">置顶</text>
				</view>
				<switch :checked="isTop" @change="toggleTop" color="#007aff" />
			</view>
			<view v-if="isTop" class="top-duration">
				<view v-for="opt in topOptions" :key="opt.value" class="duration-btn"
					:class="{ active: topDuration === opt.value }" @click="topDuration = opt.value">
					{{ opt.label }}
				</view>
			</view>

			<!-- 定时发布 -->
			<view class="option-item">
				<view class="option-left">
					<image src="/static/wall/publish/timing.png" class="option-icon" mode="aspectFit" />
					<text class="option-label">定时发布</text>
				</view>
				<switch :checked="isTiming" @change="toggleTiming" color="#007aff" />
			</view>
			<view v-if="isTiming" class="time-picker-row">
				<picker mode="date" :value="publishDate" @change="onDateChange">
					<view class="picker-display">{{ publishDate || '选择日期' }}</view>
				</picker>
				<picker mode="time" :value="publishTime" @change="onTimeChange">
					<view class="picker-display">{{ publishTime || '选择时间' }}</view>
				</picker>
			</view>
		</view>

		<!-- 3. 发布按钮 -->
		<view class="submit-btn" :class="{ disabled: !canSubmit }" @click="canSubmit && submit()">
			立即发布
		</view>
		<view class="notice">请务必遵守【社区规范】，禁止发布违规内容</view>

		<!-- 图片预览组件 -->
		<ImagePreview
			v-model:visible="previewVisible"
			v-model:currentIndex="previewIndex"
			:urls="previewUrls"
		/>

		<!-- 独立视频全屏预览 -->
		<view class="preview-mask" v-if="singleVideoVisible" @click.self="closePreview">
			<video id="publishVideoPlayer" :src="singleVideoUrl" class="preview-video-full" controls autoplay
				:show-center-play-btn="false" @click.stop />
			<view class="close-btn" @click.stop="closePreview">✕</view>
		</view>

		<!-- 分类选择弹窗（底部弹出） -->
		<view class="category-mask" v-if="showCategory" @click="showCategory = false" />
		<view class="category-panel" :class="{ show: showCategory }">
			<view class="panel-header">
				<text class="panel-title">选择发布分类</text>
				<text class="panel-close" @click="showCategory = false">×</text>
			</view>
			<scroll-view scroll-y class="panel-list">
				<view v-for="parent in parentCategoryList" :key="parent.id" class="parent-group">
					<text class="parent-name">{{ parent.name }}</text>
					<view class="child-grid">
						<view v-for="sub in getSubByParent(parent.id)" :key="sub.id" class="child-tag"
							:class="{ selected: selectedSubId === sub.id }" @click="selectSubCategory(sub)">
							{{ sub.name }}
						</view>
					</view>
				</view>
			</scroll-view>
			<view class="panel-footer">
				<button class="panel-confirm" @click="confirmCategory">确定</button>
			</view>
		</view>
	</view>
</template>

<script setup>
	import {
		ref,
		computed,
		onMounted,
		onBeforeUnmount,
		nextTick,
		watch
	} from 'vue'
	import { onLoad, onBackPress } from '@dcloudio/uni-app'
	import request from '@/utils/request.js'
	import { getCurrentUser } from '@/utils/auth.js'
	import {
		uploadMultipleFiles
	} from '@/utils/upload.js'
	import ImagePreview from '@/components/ImagePreview/ImagePreview.vue'

	// 发布人信息：从登录态读取，不再写死UID。
	const userUid = ref('')
	const nickname = ref('')
	const avatar = ref('')

	const syncCurrentUser = () => {
		const user = getCurrentUser()
		if (!user) {
			uni.navigateTo({ url: '/pages/login/login' })
			return false
		}
		userUid.value = user.uid
		nickname.value = user.username || user.nickname
		avatar.value = user.avatar
		return true
	}

	// 是否为编辑模式
	const isEditMode = ref(false)
	const editPostId = ref(null)

	// 表单字段
	const content = ref('')
	const mediaFiles = ref([]) // { type: 'image'|'video', tempPath }
	const sortFromIndex = ref(-1)
	const isAnonymous = ref(false)
	const isTop = ref(false)
	const topDuration = ref(2) // 默认2小时
	const isTiming = ref(false)
	const publishDate = ref('')
	const publishTime = ref('')

	// 分类相关
	const parentCategoryList = ref([]) // 一级分类列表
	const selectedCategoryId = ref(null); // 一级分类ID
	const allSubList = ref([]) // 全部二级分类
	const selectedSubId = ref(null) // 选中的二级分类ID
	const selectedCategoryName = ref('') // 显示的分类名称
	const showCategory = ref(false)

	// 置顶选项
	const topOptions = [{
			label: '2小时',
			value: 2
		},
		{
			label: '1天',
			value: 24
		},
		{
			label: '3天',
			value: 72
		},
		{
			label: '7天',
			value: 168
		}
	]

	// ✅ 新增 input 事件处理方法
	const onContentInput = (e) => {
		const val = e.detail.value
		if (val.length >= 750) {
			uni.showToast({
				title: '最多输入750字',
				icon: 'none'
			})
		}
	}
	// 获取一级分类
	const fetchParentCategory = async () => {
		const res = await request({
			url: '/wall/category/list',
			method: 'GET'
		})
		if (res.code === 1) parentCategoryList.value = res.data || []
	}
	// 获取全部二级分类
	const fetchAllSub = async () => {
		const res = await request({
			url: '/wall/sub-category/list',
			method: 'GET'
		})
		if (res.code === 1) allSubList.value = res.data || []
	}
	const getSubByParent = (parentId) => allSubList.value.filter(s => s.categoryId === parentId)

	// 分类操作
	const openCategoryPicker = () => {
		showCategory.value = true
	}
	const selectSubCategory = (sub) => {
		selectedSubId.value = sub.id;
		selectedCategoryName.value = sub.name;
		// 获取一级分类ID
		selectedCategoryId.value = sub.categoryId;
	};
	const confirmCategory = () => {
		showCategory.value = false
	}

	// 置顶开关
	const toggleTop = (e) => {
		isTop.value = e.detail.value
		if (!isTop.value) topDuration.value = 2
	}

	// 定时开关
	const toggleTiming = (e) => {
		isTiming.value = e.detail.value
		if (isTiming.value) {
			const now = new Date()
			const year = now.getFullYear()
			const month = String(now.getMonth() + 1).padStart(2, '0')
			const day = String(now.getDate()).padStart(2, '0')
			const hours = String(now.getHours()).padStart(2, '0')
			const minutes = String(now.getMinutes()).padStart(2, '0')
			publishDate.value = `${year}-${month}-${day}`
			publishTime.value = `${hours}:${minutes}`
		} else {
			publishDate.value = ''
			publishTime.value = ''
		}
	}

	// 日期/时间选择
	const onDateChange = (e) => {
		publishDate.value = e.detail.value
	}
	const onTimeChange = (e) => {
		publishTime.value = e.detail.value
	}

	// 媒体上传
	const chooseMedia = () => {
		const remain = 9 - mediaFiles.value.length;
		if (remain <= 0) return uni.showToast({
			title: '最多9个文件',
			icon: 'none'
		});

		// 		uni.chooseMedia({
		// 			count:remain,
		// 			mediaType:['mix'],
		// 			sizeType:['compressed'],
		// 			sourceType: ['album', 'camera'],
		// 			maxDuration: 300,
		// success: (result) => {
		// 	// 遍历所有选中的文件
		// 	result.tempFiles.forEach(file => {
		// 		// 优先取原文件名，没有就取本地临时路径
		// 		const targetStr = file.name || file.tempFilePath
		// 		// 切割获取后缀并转小写
		// 		const ext = targetStr.split('.').pop().toLowerCase()

		// 		// 定义视频、图片合法后缀数组
		// 		const videoArr = ['mp4', 'mov', 'avi', 'mkv']
		// 		const imgArr = ['jpg', 'jpeg', 'png', 'gif', 'webp', 'bmp']

		// 		// 先默认类型为其他
		// 		let type = 'other'

		// 		// 判断是不是视频
		// 		if (videoArr.includes(ext)) {
		// 			type = 'video'
		// 		}
		// 		// 判断是不是图片
		// 		else if (imgArr.includes(ext)) {
		// 			type = 'image'
		// 		}

		// 		// 把文件信息加入列表
		// 		mediaFiles.value.push({
		// 			type: type,        // 文件类型 图片/视频/其他
		// 			tempPath: file.tempFilePath, // 本地临时路径
		// 			ext: ext           // 文件小写后缀
		// 		})
		// 	})
		// },
		// 			fail: () => {
		// 			  uni.showToast({ title: '选择文件失败', icon: 'none' })
		// 			}
		// 		})

		uni.showActionSheet({
			itemList: ['图片', '视频'],
			success: (res) => {
				if (res.tapIndex === 0) {
					uni.chooseImage({
						count: remain,
						sizeType: ['compressed'],
						sourceType: ['album', 'camera'],
						success: (result) => {
							result.tempFiles.forEach((file) => {
								// H5 下 file.name 存在，小程序下可能没有，兜底用 jpg
								const ext = (file.name && file.name.split('.')
									.pop()) || 'jpg';
								const tempPath = file.path || file.tempFilePath
								const imageFile = {
									type: 'image',
									tempPath,
									ext
								}
								mediaFiles.value.push(imageFile);
								preloadImage(imageFile.tempPath)
							});
						}
					});
				} else if (res.tapIndex === 1) {
					uni.chooseVideo({
						sourceType: ['album', 'camera'],
						maxDuration: 300,
						success: (result) => {
							// 视频默认 mp4（也可尝试从 tempFilePath 提取，但比较困难）
							const ext = 'mp4';
							const rawFile = result.tempFile || result.tempFiles?.[0] || null
							const canCreateObjectUrl = typeof URL !== 'undefined' && typeof Blob !== 'undefined' && rawFile instanceof Blob
							const tempPath = result.tempFilePath || rawFile?.path || rawFile?.tempFilePath || rawFile?.url ||
								(canCreateObjectUrl ? URL.createObjectURL(rawFile) : '')
							const videoFile = {
								type: 'video',
								tempPath,
								rawFile,
								thumb: result.thumbTempFilePath || '',
								ext
							}
							mediaFiles.value.push(videoFile);
							prepareVideoThumb(videoFile, mediaFiles.value.length - 1)
						}
					});
				}
			}
		});
	};
	const removeMedia = (index) => {
	mediaFiles.value.splice(index, 1)
	sortFromIndex.value = -1
}

	const startSortMedia = (index) => {
		sortFromIndex.value = index
		uni.showToast({ title: '再点一张图片可交换顺序', icon: 'none' })
	}

	const handleMediaTap = (index) => {
		if (sortFromIndex.value >= 0) {
			if (sortFromIndex.value !== index) {
				const files = [...mediaFiles.value]
				const temp = files[sortFromIndex.value]
				files[sortFromIndex.value] = files[index]
				files[index] = temp
				mediaFiles.value = files
			}
			sortFromIndex.value = -1
			return
		}
		previewMedia(index)
	}

	const preloadImage = (url) => {
		if (!url) return
		// #ifdef H5
		const img = new Image()
		img.src = url
		// #endif
		// #ifndef H5
		uni.getImageInfo({ src: url })
		// #endif
	}

	const preloadMediaFiles = (files) => {
		files.forEach(file => {
			if (file.type === 'image') {
				preloadImage(file.tempPath)
			} else if (file.type === 'video') {
				preloadImage(getVideoThumb(file))
			}
		})
	}

	const createLocalVideoThumb = (source) => {
		return new Promise((resolve, reject) => {
			if (typeof document === 'undefined') {
				reject(new Error('video thumbnail only supports h5'))
				return
			}
			const video = document.createElement('video')
			const canvas = document.createElement('canvas')
			const objectUrl = typeof Blob !== 'undefined' && source instanceof Blob ? URL.createObjectURL(source) : ''
			const videoUrl = objectUrl || source
			let settled = false
			const finish = (value, error) => {
				if (settled) return
				settled = true
				if (objectUrl) URL.revokeObjectURL(objectUrl)
				error ? reject(error) : resolve(value)
			}
			video.muted = true
			video.playsInline = true
			video.preload = 'metadata'
			video.src = videoUrl
			const drawFrame = () => {
				try {
					const width = video.videoWidth || 320
					const height = video.videoHeight || 320
					canvas.width = width
					canvas.height = height
					canvas.getContext('2d').drawImage(video, 0, 0, width, height)
					finish(canvas.toDataURL('image/jpeg', 0.82))
				} catch (error) {
					finish('', error)
				}
			}
			video.onloadedmetadata = () => {
				try {
					video.currentTime = Math.min(0.1, Math.max(0, (video.duration || 0) / 2))
				} catch (error) {
					drawFrame()
				}
			}
			video.onseeked = drawFrame
			video.onloadeddata = () => {
				if (!video.duration) drawFrame()
			}
			video.onerror = () => finish('', new Error('video load failed'))
			video.load()
		})
	}

	const updateMediaFile = (index, patch) => {
		if (index < 0 || !mediaFiles.value[index]) return
		mediaFiles.value.splice(index, 1, {
			...mediaFiles.value[index],
			...patch
		})
	}

	const prepareVideoThumb = async (file, index = -1) => {
		if (file.thumb || !file.tempPath || file.tempPath.includes('aliyuncs.com')) {
			preloadImage(getVideoThumb(file))
			return
		}
		try {
			const thumb = await createLocalVideoThumb(file.rawFile || file.tempPath)
			updateMediaFile(index, { thumb })
			preloadImage(thumb)
		} catch (error) {
			preloadImage(getVideoThumb(file))
		}
	}

	const shouldUseNativeVideoThumb = (file) => {
		return file?.type === 'video' && file.tempPath && !file.thumb && !file.tempPath.includes('aliyuncs.com')
	}

	const isRemoteUrl = (url) => /^https?:\/\//.test(String(url || ''))

	const uploadMediaFilesIfNeeded = async (onItemProgress) => {
		const urlList = []
		for (let i = 0; i < mediaFiles.value.length; i++) {
			const file = mediaFiles.value[i]
			if (isRemoteUrl(file.tempPath)) {
				urlList.push(file.tempPath)
				continue
			}
			const uploaded = await uploadMultipleFiles([{
				path: file.tempPath,
				ext: file.ext
			}], 'wall', (_, progress) => {
				onItemProgress && onItemProgress(i, progress)
			})
			urlList.push(uploaded[0])
		}
		return urlList
	}

	// 获取视频首帧封面
	const getVideoThumb = (source) => {
		const url = typeof source === 'string' ? source : source?.tempPath
		const thumb = typeof source === 'string' ? '' : source?.thumb
		if (thumb) return thumb
		if (url && url.includes('aliyuncs.com')) return url + '?x-oss-process=video/snapshot,t_0,f_jpg'
		return '/static/wall/video-placeholder.png'
	}

	// 视频预览状态
	const singleVideoVisible = ref(false)
	const singleVideoUrl = ref('')
	const previewVisible = ref(false)
	const previewUrls = ref([])
	const previewIndex = ref(0)

	// 判断是否为图片URL
	const isImageUrl = (url) => {
		const ext = url?.split('?')[0].split('.').pop()?.toLowerCase()
		return ['jpg', 'jpeg', 'png', 'gif', 'webp', 'bmp', 'heic'].includes(ext)
	}

	// 判断是否为视频URL
	const isVideoUrl = (url) => {
		const ext = url?.split('?')[0].split('.').pop()?.toLowerCase()
		return ['mp4', 'mov', 'avi', 'mkv'].includes(ext)
	}

	// 媒体预览
	const previewMedia = (index) => {
		const file = mediaFiles.value[index]
		if (file.type === 'video' || isVideoUrl(file.tempPath)) {
			singleVideoUrl.value = file.tempPath
			singleVideoVisible.value = true
			document.body.classList.add('preview-lock')
			nextTick(() => {
				uni.createVideoContext('publishVideoPlayer').play()
			})
			// #ifdef H5
			history.pushState(null, '', location.href)
			// #endif
		} else {
			const imageUrls = mediaFiles.value.filter(f => f.type === 'image' || isImageUrl(f.tempPath)).map(f => f.tempPath)
			const imageIndex = imageUrls.indexOf(file.tempPath)
			previewUrls.value = imageUrls
			previewIndex.value = imageIndex >= 0 ? imageIndex : 0
			previewVisible.value = true
		}
	}

	// 关闭预览
	const closePreview = () => {
		previewVisible.value = false
		previewUrls.value = []
		previewIndex.value = 0
		singleVideoVisible.value = false
		singleVideoUrl.value = ''
		document.body.classList.remove('preview-lock')
	}

	// H5 popstate 监听
	const handlePopState = (e) => {
		e.preventDefault()
		e.stopPropagation()
		if (singleVideoVisible.value || previewVisible.value) {
			closePreview()
		}
	}

	// 返回键拦截
	const backPressHandler = () => {
		if (singleVideoVisible.value || previewVisible.value) {
			closePreview()
			return true
		}
	}

	// 计算是否能提交
	const canSubmit = computed(() => {
		return content.value.trim().length > 0 || mediaFiles.value.length > 0
	})

	// 提交
	// 提交表白墙发布表单
	const submit = async () => {
		if (!syncCurrentUser()) return

		// ====================== 第一步：表单基础校验 ======================
		// 未选择二级分类，直接提示终止提交
		if (!selectedSubId.value) return uni.showToast({
			title: '请选择分类',
			icon: 'none'
		})

		// 既没有输入文字内容，也没有上传图片视频，禁止提交
		if (!content.value && mediaFiles.value.length === 0) return uni.showToast({
			title: '请输入内容或上传文件',
			icon: 'none'
		})

		// 定义加载状态标记，防止重复关闭loading
		let loading = false

		try {
			// 弹出加载框：提示正在上传文件
			uni.showLoading({
				title: '上传文件中...',
				mask: true // 遮罩层禁止点击其他区域
			})
			loading = true

			// ====================== 第二步：上传新增的本地媒体，保留已有线上媒体 ======================
			const uploadedUrls = await uploadMediaFilesIfNeeded((index, progress) => {
				uni.showToast({
					title: `第${index + 1}个 ${progress}%`,
					icon: 'none',
					duration: 800
				});
			});

			// 文件全部上传完成，关闭上传loading
			uni.hideLoading()
			loading = false

			// 开启发布/保存中loading
			uni.showLoading({
				title: isEditMode.value ? '保存中...' : '发布中...',
				mask: true
			})
			loading = true

			// ====================== 第四步：组装后端发布接口参数 ======================
			const params = {
				userUid: userUid.value, // 用户脱敏唯一标识（不用真实userId）
				nickname: nickname.value, // 发布昵称
				avatar: avatar.value, // 发布头像
				categoryId: selectedCategoryId.value, // 一级分类ID
				subCategoryId: selectedSubId.value, // 二级分类ID
				content: content.value, // 表白墙文字内容
				mediaUrlList: uploadedUrls, // 上传完成的OSS线上链接数组
				isAnonymous: isAnonymous.value ? 1 : 0, // 是否匿名 1是 0否
				isTop: isTop.value ? 1 : 0, // 是否置顶 1是 0否
				isTiming: isTiming.value ? 1 : 0, // 是否定时发布 1是 0否
			};

			if (isEditMode.value) {
				params.id = editPostId.value
			}

			// 如果开启置顶，传入置顶时长（小时）
			if (isTop.value) {
				params.topHour = topDuration.value
			}

			// 如果开启定时发布，拼接成 年月日 时分 格式传给后端
			if (isTiming.value) {
				params.publishTime = `${publishDate.value} ${publishTime.value}:00`;
			}

			// 打印最终提交参数，方便调试
			console.log('最终发布参数：', params);

			// ====================== 第五步：请求后端发布/编辑接口 ======================
			const res = await request({
				url: isEditMode.value ? '/wall/post/edit' : '/wall/post/add',
				method: isEditMode.value ? 'PUT' : 'POST',
				data: params // 提交的所有表单数据
			})

			// 接口请求完成关闭loading
			uni.hideLoading()
			loading = false

			// ====================== 第六步：处理接口返回结果 ======================
			// 后端约定 code=1 代表成功
			if (res.code === 1) {
				uni.showToast({
					title: isEditMode.value ? '保存成功' : '发布成功',
					icon: 'success'
				})
				// 1.5秒后自动返回上一级页面
				setTimeout(() => uni.navigateBack(), 1500)
			} else {
				// 后端返回失败提示文案
				uni.showToast({
					title: res.msg || '发布失败',
					icon: 'none'
				})
			}

			// ====================== 全局异常捕获 ======================
		} catch (e) {
			// 报错强制关闭loading，防止卡死
			if (loading) uni.hideLoading()
			// 打印错误信息（上传失败、网络错误、校验错误都会进这里）
			console.error('发布出错：', e)
			uni.showToast({
				title: '操作失败，请重试',
				icon: 'none'
			})
		}
	}

	onMounted(() => {
		fetchParentCategory()
		fetchAllSub()
		// #ifdef H5
		window.addEventListener('popstate', handlePopState)
		// #endif
	})

	onBeforeUnmount(() => {
		// #ifdef H5
		window.removeEventListener('popstate', handlePopState)
		// #endif
	})

	// 返回键拦截
	onBackPress(backPressHandler)

	// 接收编辑数据
	onLoad((options) => {
		syncCurrentUser()

		if (options && options.editData) {
			try {
				const editData = JSON.parse(decodeURIComponent(options.editData))
				isEditMode.value = true
				editPostId.value = editData.id
				
				// 回填数据
				content.value = editData.content || ''
				isAnonymous.value = editData.isAnonymous === 1
				isTop.value = editData.isTop === 1
				selectedCategoryId.value = editData.categoryId
				selectedSubId.value = editData.subCategoryId
				selectedCategoryName.value = editData.subCategoryName || ''
				
				// 如果有媒体，添加预览
				if (editData.mediaUrlList && editData.mediaUrlList.length > 0) {
					mediaFiles.value = editData.mediaUrlList.map(url => ({
						type: isVideoUrl(url) ? 'video' : 'image',
						tempPath: url,
						ext: url.split('?')[0].split('.').pop()?.toLowerCase() || 'jpg'
					}))
					preloadMediaFiles(mediaFiles.value)
				}
				
				uni.setNavigationBarTitle({ title: '编辑帖子' })
			} catch (e) {
				console.error('解析编辑数据失败', e)
			}
		}
	})
</script>

<style scoped>
	.publish-page {
		min-height: 100vh;
		background: #f5f6f8;
		padding: 20rpx;
		overflow-y: auto;
		/* 确保页面可滚动 */
		-webkit-overflow-scrolling: touch;
		/* iOS 平滑滚动 */
	}

	.card {
		background: #fff;
		border-radius: 20rpx;
		padding: 24rpx;
		margin-bottom: 20rpx;
	}

	/* 输入区 */
	.content-textarea {
		width: 100%;
		min-height: 200rpx;
		font-size: 28rpx;
		line-height: 1.6;
	}

	.char-count {
		text-align: right;
		font-size: 22rpx;
		color: #999;
		margin-top: 10rpx;
	}

	.media-upload {
		margin-top: 20rpx;
	}

	.media-grid {
		display: flex;
		flex-wrap: wrap;
		/* justify-content:space-between; */
		gap: 12rpx;
	}

	.media-item {
		width: 212rpx;
		height: 212rpx;
		position: relative;
		border-radius: 12rpx;
		overflow: hidden;
		background: #eee;
	}

	.media-item.sorting {
		outline: 4rpx solid #1677ff;
		outline-offset: -4rpx;
	}

	.media-preview {
		width: 100%;
		height: 100%;
	}

	.delete-icon {
		position: absolute;
		top: 4rpx;
		right: 4rpx;
		width: 40rpx;
		height: 40rpx;
		background: rgba(0, 0, 0, 0.5);
		color: #fff;
		border-radius: 50%;
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 28rpx;
	}

	.sort-badge {
		position: absolute;
		left: 8rpx;
		bottom: 8rpx;
		padding: 4rpx 12rpx;
		border-radius: 999rpx;
		background: rgba(22, 119, 255, 0.92);
		color: #fff;
		font-size: 20rpx;
	}

	.add-media-btn {
		width: 212rpx;
		height: 212rpx;
		border: 2rpx dashed #ccc;
		border-radius: 12rpx;
		margin-top: 12rpx;
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		color: #999;
	}

	.add-icon {
		font-size: 48rpx;
	}

	.add-text {
		font-size: 22rpx;
	}

	/* 功能选项 */
	.option-item {
		display: flex;
		align-items: center;
		justify-content: space-between;
		padding: 24rpx 0;
		border-bottom: 1rpx solid #f0f0f0;
	}

	.option-item:last-child {
		border-bottom: none;
	}

	.option-left {
		display: flex;
		align-items: center;
		gap: 12rpx;
	}

	.option-icon {
		width: 62rpx;
		height: 62rpx;
		flex-shrink: 0;
	}

	.option-label {
		font-size: 28rpx;
		color: #333;
	}

	.option-right {
		display: flex;
		align-items: center;
		gap: 8rpx;
		color: #999;
	}

	.option-value {
		font-size: 26rpx;
	}

	.arrow {
		font-size: 32rpx;
	}

	.top-duration {
		display: flex;
		gap: 16rpx;
		padding: 16rpx 0;
		justify-content: flex-start;
	}

	.duration-btn {
		padding: 8rpx 28rpx;
		border-radius: 30rpx;
		background: #f5f5f5;
		font-size: 26rpx;
		color: #666;
	}

	.duration-btn.active {
		background: #007aff;
		color: #fff;
	}

	.time-picker-row {
		display: flex;
		gap: 20rpx;
		padding: 16rpx 0;
		justify-content: flex-start;
	}

	.picker-display {
		flex: 1;
		background: #f5f5f5;
		border-radius: 12rpx;
		padding: 16rpx;
		text-align: center;
		font-size: 26rpx;
		color: #333;
	}

	/* 发布按钮 */
	.submit-btn {
		background: #202124;
		color: #fff;
		text-align: center;
		padding: 28rpx;
		border-radius: 40rpx;
		font-size: 32rpx;
		font-weight: 500;
		margin: 20rpx 0 10rpx;
	}

	.submit-btn.disabled {
		background: #ccc;
		color: #fff;
	}

	.notice {
		text-align: center;
		font-size: 22rpx;
		color: #aaa;
		margin-bottom: 40rpx;
	}

	/* 分类底部弹窗 */
	.category-mask {
		position: fixed;
		top: 0;
		left: 0;
		right: 0;
		bottom: 0;
		background: rgba(0, 0, 0, 0.4);
		z-index: 999;
	}

	.category-panel {
		position: fixed;
		bottom: 0;
		left: 0;
		right: 0;
		background: #fff;
		border-radius: 30rpx 30rpx 0 0;
		padding: 30rpx;
		max-height: 70vh;
		transform: translateY(100%);
		transition: transform 0.3s;
		z-index: 1000;
		display: flex;
		flex-direction: column;
	}

	.category-panel.show {
		transform: translateY(0);
	}

	.panel-header {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-bottom: 20rpx;
	}

	.panel-title {
		font-size: 34rpx;
		font-weight: 600;
	}

	.panel-close {
		font-size: 40rpx;
		color: #999;
	}

	.panel-list {
		flex: 1;
		overflow-y: auto;
	}

	.parent-group {
		margin-bottom: 20rpx;
	}

	.parent-name {
		font-size: 28rpx;
		font-weight: 500;
		margin-bottom: 12rpx;
	}

	.child-grid {
		display: flex;
		flex-wrap: wrap;
		gap: 16rpx;
	}

	.child-tag {
		padding: 10rpx 24rpx;
		background: #f5f5f5;
		border-radius: 20rpx;
		font-size: 26rpx;
		color: #555;
	}

	.child-tag.selected {
		background: #007aff;
		color: #fff;
	}

	.panel-footer {
		margin-top: 20rpx;
	}

	.panel-confirm {
		width: 100%;
		background: #007aff;
		color: #fff;
		border-radius: 40rpx;
		padding: 24rpx 0;
		font-size: 30rpx;
		font-weight: 500;
	}

	/* 视频缩略图样式 */
	.video-thumb-wrapper {
		width: 100%;
		height: 100%;
		position: relative;
		border-radius: 12rpx;
		overflow: hidden;
	}

	.video-thumb {
		width: 100%;
		height: 100%;
	}

	.native-video-thumb {
		pointer-events: none;
	}

	.play-overlay {
		position: absolute;
		top: 50%;
		left: 50%;
		transform: translate(-50%, -50%);
		width: 80rpx;
		height: 80rpx;
		background: rgba(0, 0, 0, 0.5);
		border-radius: 50%;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.play-icon {
		color: #fff;
		font-size: 40rpx;
	}

	/* 视频预览遮罩 */
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

	.preview-video-full {
		width: 100%;
		height: 100%;
		max-width: 800px;
		max-height: 90vh;
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
