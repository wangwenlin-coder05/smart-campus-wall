<template>
	<view class="page">
		<view class="header">
			<text class="title">专业交流</text>
			<view class="post-btn" @click="showPostModal = true">
				<text class="post-icon">+</text>
				<text>发帖</text>
			</view>
		</view>

		<view class="filter-tabs">
			<view class="filter-tab" :class="{ active: filterOpen === 'major' || selectedFilters.major }" @click="toggleFilter('major')">
				<text>专业</text>
				<text class="arrow">{{ filterOpen === 'major' ? '▲' : '▼' }}</text>
			</view>
			<view class="filter-tab" :class="{ active: filterOpen === 'school' || selectedFilters.school }" @click="toggleFilter('school')">
				<text>学校</text>
				<text class="arrow">{{ filterOpen === 'school' ? '▲' : '▼' }}</text>
			</view>
			<view class="filter-tab" :class="{ active: filterOpen === 'degree' || selectedFilters.degree }" @click="toggleFilter('degree')">
				<text>学历</text>
				<text class="arrow">{{ filterOpen === 'degree' ? '▲' : '▼' }}</text>
			</view>
			<view class="filter-tab" :class="{ active: filterOpen === 'experience' || selectedFilters.experience }" @click="toggleFilter('experience')">
				<text>经验</text>
				<text class="arrow">{{ filterOpen === 'experience' ? '▲' : '▼' }}</text>
			</view>
		</view>

		<view class="selected-tags" v-if="hasSelectedFilters">
			<view class="selected-tag" v-for="tag in selectedFilterTags" :key="tag.key">
				<text>{{ tag.label }}: {{ tag.value }}</text>
				<text class="tag-close" @click="removeFilter(tag.key)">×</text>
			</view>
			<view class="clear-all" @click="clearAllFilters">清除全部</view>
		</view>

		<view class="filter-popup" v-if="filterOpen" @click="filterOpen = ''">
			<view class="filter-content" @click.stop>
				<view class="filter-header">
					<text class="filter-title">{{ getFilterTitle(filterOpen) }}</text>
					<view class="clear-btn" v-if="getSelectedFilter(filterOpen)" @click="clearFilter(filterOpen)">清除</view>
				</view>
				<scroll-view class="filter-options" scroll-y>
					<view
						v-for="item in getFilterOptions(filterOpen)"
						:key="item.id"
						class="filter-option"
						:class="{ active: getSelectedFilter(filterOpen) === item.id }"
						@click="selectFilter(filterOpen, item)"
					>
						<view class="option-radio" :class="{ checked: getSelectedFilter(filterOpen) === item.id }"></view>
						<text>{{ item.name }}</text>
					</view>
				</scroll-view>
			</view>
		</view>

		<scroll-view class="post-list" scroll-y :show-scrollbar="false" :style="postListStyle">
			<view v-for="post in filteredPosts" :key="post.id" class="post-card">
				<view class="post-header">
					<view class="avatar-placeholder">
						<text class="avatar-text">{{ post.username.charAt(0) }}</text>
					</view>
					<view class="user-info">
						<text class="username">{{ post.username }}</text>
						<view class="user-tags">
							<text class="user-tag school-tag">{{ post.school }}</text>
							<text class="user-tag major-tag">{{ post.major }}</text>
						</view>
					</view>
					<text class="post-time">{{ post.time }}</text>
				</view>

				<text class="post-title">{{ post.title }}</text>
				<view class="content-wrap">
					<text class="post-content" :class="{ collapsed: shouldShowExpand(post) && !isExpanded(post.id) }">{{ post.content }}</text>
					<text v-if="shouldShowExpand(post)" class="expand-btn" @click="toggleExpand(post.id)">
						{{ isExpanded(post.id) ? '收起' : '展开' }}
					</text>
				</view>

				<view class="post-tags">
					<text class="post-tag degree-tag">{{ post.degree }}</text>
					<text class="post-tag experience-tag">{{ post.experience }}</text>
				</view>
			</view>

			<view class="loading-more" v-if="loading">
				<text>加载中...</text>
			</view>
			<view class="no-more" v-else-if="!hasMore && filteredPosts.length">
				<text>已加载全部</text>
			</view>
			<view class="no-result" v-if="filteredPosts.length === 0 && !loading">
				<text>暂无相关帖子</text>
			</view>
		</scroll-view>

		<view class="modal-overlay" v-if="showPostModal" @click="showPostModal = false">
			<view class="modal-content" @click.stop>
				<view class="modal-header">
					<text class="modal-title">发布帖子</text>
					<text class="modal-close" @click="showPostModal = false">×</text>
				</view>
				<view class="modal-body">
					<input v-model="newPost.title" class="input-field" placeholder="请输入标题" maxlength="50" />
					<textarea v-model="newPost.content" class="textarea-field" placeholder="分享你的经验..." maxlength="500"></textarea>
					<view class="form-row">
						<view class="form-label">专业</view>
						<picker :value="newPost.majorIndex" :range="majorOptions" @change="onMajorChange">
							<view class="picker-field">{{ newPost.major || '选择专业' }}</view>
						</picker>
					</view>
					<view class="form-row">
						<view class="form-label">学校</view>
						<picker :value="newPost.schoolIndex" :range="schoolOptions" @change="onSchoolChange">
							<view class="picker-field">{{ newPost.school || '选择学校' }}</view>
						</picker>
					</view>
					<view class="form-row">
						<view class="form-label">学历</view>
						<picker :value="newPost.degreeIndex" :range="degreeOptions" @change="onDegreeChange">
							<view class="picker-field">{{ newPost.degree || '选择学历' }}</view>
						</picker>
					</view>
					<view class="form-row">
						<view class="form-label">经验</view>
						<picker :value="newPost.experienceIndex" :range="experienceOptions" @change="onExperienceChange">
							<view class="picker-field">{{ newPost.experience || '选择经验' }}</view>
						</picker>
					</view>
				</view>
				<view class="modal-footer">
					<view class="cancel-btn" @click="showPostModal = false">取消</view>
					<view class="submit-btn" :class="{ disabled: !canSubmit }" @click="submitPost">发布</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'

const filterOpen = ref('')
const loading = ref(false)
const hasMore = ref(true)
const showPostModal = ref(false)
const expandedIds = ref([])

const selectedFilters = ref({
	major: '',
	school: '',
	degree: '',
	experience: ''
})

const majorOptions = ['计算机科学', '软件工程', '电子信息', '机械工程', '工商管理', '会计学', '金融学', '英语', '汉语言文学', '临床医学']
const schoolOptions = ['北京大学', '清华大学', '复旦大学', '上海交通大学', '浙江大学', '南京大学', '中国人民大学', '武汉大学', '四川大学', '吉林大学']
const degreeOptions = ['高中', '专科', '本科', '硕士', '博士']
const experienceOptions = ['应届生', '1年以内', '1-3年', '3-5年', '5-10年', '10年以上']

const newPost = ref({
	title: '',
	content: '',
	major: '',
	school: '',
	degree: '',
	experience: '',
	majorIndex: 0,
	schoolIndex: 0,
	degreeIndex: 0,
	experienceIndex: 0
})

const posts = ref([])

const filterConfigs = {
	major: {
		title: '选择专业',
		options: majorOptions.map(name => ({ id: name, name }))
	},
	school: {
		title: '选择学校',
		options: schoolOptions.map(name => ({ id: name, name }))
	},
	degree: {
		title: '选择学历',
		options: degreeOptions.map(name => ({ id: name, name }))
	},
	experience: {
		title: '选择工作经验',
		options: experienceOptions.map(name => ({ id: name, name }))
	}
}

const mockBackendData = [
	{
		id: 1,
		username: '学霸小明',
		time: '2小时前',
		title: '分享我的考研复习计划',
		content: '经过一年的努力，我终于考上了理想的研究生院校。在这里分享一下我的复习计划：制定详细的学习时间表，分阶段复习，基础、强化、冲刺都要安排清楚，定期做真题模拟，保持良好的心态。希望这些经验能帮助到正在备考的同学们。',
		school: '浙江大学',
		major: '计算机科学',
		degree: '硕士',
		experience: '应届生'
	},	{
		id: 1,
		username: '学霸小明',
		time: '2小时前',
		title: '分享我的考研复习计划',
		content: '经过一年的努力，我终于考上了理想的研究过一年的努力，我终于考上了理想的研究生院校。在这里分享一下我的复习计划：制定详细的学习时间表，分阶段复习，基础、强化、冲刺都要安排清楚，定期做真题模拟，保生院校。在这里分享一下我的复习计划：制定详细的学习时间表，分阶段复习，基础、强化、冲刺都要安排清楚，定期做真题模拟，保持良好的心态。希望这些经验能帮助到正在备考的同学们。',
		school: '浙江大学',
		major: '计算机科学',
		degree: '硕士',
		experience: '应届生'
	},
	{
		id: 2,
		username: '职场达人Lisa',
		time: '5小时前',
		title: '应届生求职经验分享',
		content: '简历制作要突出重点，面试前要做好充分准备。',
		school: '上海交通大学',
		major: '工商管理',
		degree: '本科',
		experience: '1年以内'
	},
	{
		id: 3,
		username: '码农小王',
		time: '昨天',
		title: 'Python 数据分析入门指南',
		content: '最近在学习 Python 数据分析，整理了一份入门指南，包括环境搭建、常用库介绍、实战项目推荐。适合零基础的同学学习，建议从 NumPy 和 Pandas 开始，掌握基础数据结构和操作，再逐步进入可视化与机器学习方向。',
		school: '清华大学',
		major: '软件工程',
		degree: '本科',
		experience: '3-5年'
	},
	{
		id: 4,
		username: '小确幸',
		time: '2天前',
		title: '如何平衡学业和恋爱',
		content: '大学期间谈恋爱是很美好的事情，但也要给彼此空间，安排好学习和生活。',
		school: '武汉大学',
		major: '汉语言文学',
		degree: '本科',
		experience: '应届生'
	}
]

const getFilterTitle = (type) => filterConfigs[type]?.title || ''
const getFilterOptions = (type) => filterConfigs[type]?.options || []
const getSelectedFilter = (type) => selectedFilters.value[type]

const hasSelectedFilters = computed(() => Object.values(selectedFilters.value).some(Boolean))

const selectedFilterTags = computed(() => {
	const labels = {
		major: '专业',
		school: '学校',
		degree: '学历',
		experience: '经验'
	}
	return Object.keys(selectedFilters.value)
		.filter(key => selectedFilters.value[key])
		.map(key => ({
			key,
			label: labels[key],
			value: selectedFilters.value[key]
		}))
})

const filteredPosts = computed(() => {
	return posts.value.filter(post => {
		if (selectedFilters.value.major && post.major !== selectedFilters.value.major) return false
		if (selectedFilters.value.school && post.school !== selectedFilters.value.school) return false
		if (selectedFilters.value.degree && post.degree !== selectedFilters.value.degree) return false
		if (selectedFilters.value.experience && post.experience !== selectedFilters.value.experience) return false
		return true
	})
})

const canSubmit = computed(() => newPost.value.title.trim() && newPost.value.content.trim())

const postListStyle = computed(() => ({
	paddingTop: '0',
	paddingBottom: '40rpx',
	boxSizing: 'border-box'
}))

const toggleFilter = (type) => {
	filterOpen.value = filterOpen.value === type ? '' : type
}

const selectFilter = (type, item) => {
	selectedFilters.value[type] = selectedFilters.value[type] === item.id ? '' : item.id
	filterOpen.value = ''
}

const clearFilter = (type) => {
	selectedFilters.value[type] = ''
}

const clearAllFilters = () => {
	selectedFilters.value = {
		major: '',
		school: '',
		degree: '',
		experience: ''
	}
}

const removeFilter = (type) => {
	selectedFilters.value[type] = ''
}

const toggleExpand = (id) => {
	const index = expandedIds.value.indexOf(id)
	if (index > -1) {
		expandedIds.value.splice(index, 1)
	} else {
		expandedIds.value.push(id)
	}
}

const isExpanded = (id) => expandedIds.value.includes(id)

const shouldShowExpand = (post) => {
	const content = post?.content || ''
	const lineCount = content.split(/\r?\n/).length
	return lineCount > 5 || content.length > 110
}

const onMajorChange = (e) => {
	const index = e.detail.value
	newPost.value.majorIndex = index
	newPost.value.major = majorOptions[index]
}

const onSchoolChange = (e) => {
	const index = e.detail.value
	newPost.value.schoolIndex = index
	newPost.value.school = schoolOptions[index]
}

const onDegreeChange = (e) => {
	const index = e.detail.value
	newPost.value.degreeIndex = index
	newPost.value.degree = degreeOptions[index]
}

const onExperienceChange = (e) => {
	const index = e.detail.value
	newPost.value.experienceIndex = index
	newPost.value.experience = experienceOptions[index]
}

const submitPost = () => {
	if (!canSubmit.value) return

	posts.value.unshift({
		id: Date.now(),
		username: '当前用户',
		time: '刚刚',
		title: newPost.value.title,
		content: newPost.value.content,
		school: newPost.value.school || '未填写',
		major: newPost.value.major || '未填写',
		degree: newPost.value.degree || '未填写',
		experience: newPost.value.experience || '未填写'
	})

	showPostModal.value = false
	newPost.value = {
		title: '',
		content: '',
		major: '',
		school: '',
		degree: '',
		experience: '',
		majorIndex: 0,
		schoolIndex: 0,
		degreeIndex: 0,
		experienceIndex: 0
	}

	uni.showToast({ title: '发布成功', icon: 'success' })
}

const fetchPosts = () => {
	loading.value = true
	setTimeout(() => {
		posts.value = [...mockBackendData]
		loading.value = false
		hasMore.value = false
	}, 300)
}

onMounted(fetchPosts)
</script>

<style>
page {
	height: 100%;
	overflow: hidden;
}
</style>

<style scoped>
.page {
	height: 100vh;
	overflow: hidden;
	background: #f5f5f5;
	display: flex;
	flex-direction: column;
}

.header {
	flex-shrink: 0;
	z-index: 100;
	display: flex;
	align-items: center;
	justify-content: space-between;
	height: 88rpx;
	padding: 0 32rpx;
	padding-top: var(--status-bar-height);
	background: #ffffff;
	border-bottom: 1rpx solid #f0f0f0;
	box-sizing: content-box;
}

.title {
	font-size: 36rpx;
	font-weight: 600;
	color: #333333;
}

.post-btn {
	display: flex;
	align-items: center;
	gap: 8rpx;
	padding: 12rpx 24rpx;
	background: #333333;
	color: #ffffff;
	font-size: 26rpx;
	border-radius: 40rpx;
}

.post-icon {
	font-size: 32rpx;
	font-weight: 300;
}

.filter-tabs {
	flex-shrink: 0;
	z-index: 99;
	display: flex;
	background: #ffffff;
	border-bottom: 1rpx solid #f0f0f0;
}

.filter-tab {
	flex: 1;
	display: flex;
	align-items: center;
	justify-content: center;
	gap: 8rpx;
	height: 80rpx;
	font-size: 26rpx;
	color: #666666;
	border-bottom: 4rpx solid transparent;
}

.filter-tab.active {
	color: #333333;
	font-weight: 600;
	border-bottom-color: #333333;
}

.arrow {
	font-size: 20rpx;
	color: #999999;
}

.filter-popup {
	position: fixed;
	top: 0;
	left: 0;
	right: 0;
	bottom: 0;
	padding-top: calc(88rpx + var(--status-bar-height) + 80rpx);
	background: rgba(0, 0, 0, 0.5);
	z-index: 1000;
	box-sizing: border-box;
}

.filter-content {
	background: #ffffff;
	max-height: 60vh;
	border-radius: 0 0 16rpx 16rpx;
}

.filter-header {
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding: 32rpx;
	border-bottom: 1rpx solid #f0f0f0;
}

.filter-title {
	font-size: 32rpx;
	font-weight: 600;
	color: #333333;
}

.clear-btn {
	font-size: 26rpx;
	color: #666666;
}

.filter-options {
	max-height: 50vh;
	padding: 16rpx 32rpx;
}

.filter-option {
	display: flex;
	align-items: center;
	gap: 20rpx;
	height: 80rpx;
	font-size: 28rpx;
	color: #333333;
}

.filter-option.active {
	font-weight: 600;
}

.option-radio {
	width: 32rpx;
	height: 32rpx;
	border-radius: 50%;
	border: 2rpx solid #d0d0d0;
}

.option-radio.checked {
	background: #333333;
	border-color: #333333;
}

.selected-tags {
	flex-shrink: 0;
	z-index: 98;
	display: flex;
	flex-wrap: wrap;
	gap: 16rpx;
	padding: 16rpx 24rpx;
	background: #ffffff;
	border-bottom: 1rpx solid #f0f0f0;
}

.selected-tag {
	display: flex;
	align-items: center;
	gap: 8rpx;
	padding: 8rpx 16rpx;
	background: #f0f5ff;
	color: #3366ff;
	font-size: 24rpx;
	border-radius: 20rpx;
}

.tag-close {
	font-size: 28rpx;
	line-height: 1;
}

.clear-all {
	font-size: 24rpx;
	color: #999999;
	padding: 8rpx 16rpx;
}

.post-list {
	flex: 1;
	min-height: 0;
	height: auto;
	box-sizing: border-box;
	overflow: hidden;
}

.post-card {
	margin: 24rpx;
	padding: 24rpx;
	background: #ffffff;
	border-radius: 8rpx;
}

.post-header {
	display: flex;
	align-items: flex-start;
	margin-bottom: 16rpx;
}

.avatar-placeholder {
	width: 72rpx;
	height: 72rpx;
	border-radius: 50%;
	background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
	display: flex;
	align-items: center;
	justify-content: center;
	flex-shrink: 0;
}

.avatar-text {
	font-size: 28rpx;
	color: #ffffff;
	font-weight: 600;
}

.user-info {
	flex: 1;
	margin-left: 16rpx;
	min-width: 0;
}

.username {
	display: block;
	font-size: 28rpx;
	font-weight: 600;
	color: #333333;
	margin-bottom: 8rpx;
}

.user-tags {
	display: flex;
	flex-wrap: wrap;
	gap: 8rpx;
}

.user-tag {
	padding: 4rpx 12rpx;
	font-size: 20rpx;
	border-radius: 12rpx;
}

.school-tag {
	background: #e8f4fd;
	color: #1e88e5;
}

.major-tag {
	background: #f3e5f5;
	color: #7e57c2;
}

.post-time {
	font-size: 22rpx;
	color: #999999;
	flex-shrink: 0;
}

.post-title {
	display: block;
	font-size: 32rpx;
	font-weight: 600;
	color: #333333;
	line-height: 1.4;
	margin-bottom: 12rpx;
}

.content-wrap {
	display: flex;
	flex-direction: column;
	margin-bottom: 16rpx;
}

.post-content {
	font-size: 26rpx;
	color: #666666;
	line-height: 1.6;
}

.post-content.collapsed {
	display: -webkit-box;
	-webkit-box-orient: vertical;
	-webkit-line-clamp: 5;
	overflow: hidden;
}

.expand-btn {
	align-self: flex-start;
	font-size: 26rpx;
	color: #3366ff;
	margin-top: 8rpx;
}

.post-tags {
	display: flex;
	flex-wrap: wrap;
	gap: 12rpx;
}

.post-tag {
	padding: 6rpx 16rpx;
	font-size: 22rpx;
	border-radius: 20rpx;
}

.degree-tag {
	background: #e8f5e9;
	color: #43a047;
}

.experience-tag {
	background: #fff3e0;
	color: #fb8c00;
}

.loading-more,
.no-more,
.no-result {
	text-align: center;
	padding: 32rpx;
	font-size: 24rpx;
	color: #999999;
}

.modal-overlay {
	position: fixed;
	top: 0;
	left: 0;
	right: 0;
	bottom: 0;
	background: rgba(0, 0, 0, 0.5);
	z-index: 1000;
	display: flex;
	align-items: flex-end;
}

.modal-content {
	width: 100%;
	background: #ffffff;
	border-radius: 16rpx 16rpx 0 0;
	padding-bottom: env(safe-area-inset-bottom);
	max-height: 85vh;
	overflow-y: auto;
}

.modal-header {
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding: 32rpx;
	border-bottom: 1rpx solid #f0f0f0;
	position: sticky;
	top: 0;
	background: #ffffff;
}

.modal-title {
	font-size: 32rpx;
	font-weight: 600;
	color: #333333;
}

.modal-close {
	font-size: 48rpx;
	color: #999999;
	line-height: 1;
}

.modal-body {
	padding: 32rpx;
}

.form-row {
	display: flex;
	align-items: center;
	gap: 20rpx;
	margin-bottom: 20rpx;
}

.form-label {
	width: 120rpx;
	font-size: 26rpx;
	color: #666666;
}

.input-field {
	width: 100%;
	height: 80rpx;
	padding: 0 24rpx;
	border: 1rpx solid #e0e0e0;
	border-radius: 8rpx;
	font-size: 28rpx;
	box-sizing: border-box;
	margin-bottom: 20rpx;
}

.textarea-field {
	width: 100%;
	height: 240rpx;
	padding: 20rpx 24rpx;
	border: 1rpx solid #e0e0e0;
	border-radius: 8rpx;
	font-size: 28rpx;
	line-height: 1.6;
	box-sizing: border-box;
	margin-bottom: 20rpx;
}

.picker-field {
	flex: 1;
	height: 80rpx;
	padding: 0 24rpx;
	background: #f5f5f5;
	border-radius: 8rpx;
	font-size: 28rpx;
	display: flex;
	align-items: center;
	color: #333333;
}

.modal-footer {
	display: flex;
	gap: 24rpx;
	padding: 24rpx 32rpx;
	border-top: 1rpx solid #f0f0f0;
}

.cancel-btn,
.submit-btn {
	flex: 1;
	height: 80rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 28rpx;
	border-radius: 8rpx;
}

.cancel-btn {
	background: #f5f5f5;
	color: #666666;
}

.submit-btn {
	background: #333333;
	color: #ffffff;
}

.submit-btn.disabled {
	background: #cccccc;
}
</style>
