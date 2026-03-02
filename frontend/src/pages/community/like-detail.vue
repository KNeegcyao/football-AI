<template>
	<view class="container">
		<!-- 自定义导航栏 -->
		<view class="custom-navbar" :style="{ paddingTop: statusBarHeight + 'px' }">
			<view class="nav-content">
				<view class="left" @tap="goBack">
					<u-icon name="arrow-left" size="44rpx" color="#FFFFFF"></u-icon>
				</view>
				<view class="title">收到喜欢</view>
				<view class="right">
					<u-icon name="setting" size="40rpx" color="#FFFFFF"></u-icon>
				</view>
			</view>
		</view>

		<!-- 列表内容 -->
		<scroll-view scroll-y class="list-container" @scrolltolower="loadMore">
			<view v-for="item in list" :key="item.id" class="notification-item" @tap="goToPost(item)">
				<view class="item-header">
					<view class="avatar-group">
						<u-avatar :src="item.fromUser.avatar || '/static/default-avatar.png'" size="50" shape="square"></u-avatar>
						<!-- 这里通常后端会返回点赞人数及其中的代表人物 -->
						<!-- 如果只是单个人，显示单个人头像；如果是多人，前端模拟聚合显示 -->
						<view v-if="item.likeCount > 1" class="more-avatars">
							<u-avatar :src="item.extraAvatar || '/static/default-avatar.png'" size="50" shape="square"></u-avatar>
						</view>
					</view>
					<view class="content-box">
						<view class="like-info">
							<text class="username">{{ item.fromUser.nickname }}</text>
							<text v-if="item.likeCount > 1" class="others-text">、{{ item.extraUserName }} 等总计{{ item.likeCount }}人</text>
							<text class="action-text">赞了我的{{ item.type === 1 ? '帖子' : '评论' }}</text>
						</view>
						<view class="item-footer">
							<text class="time">{{ formatTime(item.createdAt) }}</text>
						</view>
					</view>
					<!-- 右侧引用的原内容图片/文字预览 -->
					<view class="right-preview-area">
						<view class="more-btn" @tap.stop="handleMore(item)">
							<u-icon name="more-dot-fill" size="16" color="#999" customStyle="transform: rotate(90deg)"></u-icon>
						</view>
						<view class="origin-preview">
							<view class="preview-text">{{ item.postTitle }}</view>
						</view>
					</view>
				</view>
			</view>

			<u-loadmore :status="loadStatus" color="#999" />
			<view style="height: 40rpx;"></view>
		</scroll-view>

		<!-- 更多操作菜单 -->
		<u-action-sheet 
			:show="showActionSheet" 
			:actions="actionActions" 
			@close="showActionSheet = false" 
			@select="onActionSelect" 
			cancelText="取消"
			bgColor="#1a1811"
			:safeAreaInsetBottom="true"
		></u-action-sheet>
	</view>
</template>

<script>
import { notificationApi } from '@/api'

export default {
	data() {
		return {
			statusBarHeight: uni.getSystemInfoSync().statusBarHeight,
			navbarHeight: 0,
			list: [],
			page: 1,
			loadStatus: 'loadmore',
			isLoading: false,
			types: '1,2', // 帖子点赞、评论点赞
			showActionSheet: false,
			actionActions: [
				{ name: '不再通知', value: 'mute' },
				{ name: '删除', value: 'delete', color: '#ff4d4f' }
			],
			currentItem: {}
		}
	},
	onLoad() {
		// 计算导航栏高度
		// #ifdef MP-WEIXIN
		const menuButtonInfo = uni.getMenuButtonBoundingClientRect()
		this.navbarHeight = menuButtonInfo.bottom + menuButtonInfo.top - this.statusBarHeight
		// #endif
		// #ifndef MP-WEIXIN
		this.navbarHeight = 44 // H5/App 默认高度
		// #endif
		
		this.loadData(true)
	},
	onPullDownRefresh() {
		this.loadData(true)
	},
	onReachBottom() {
		this.loadMore()
	},
	methods: {
		goBack() {
			uni.navigateBack()
		},
		formatTime(time) {
			if (!time) return ''
			const date = new Date(time)
			const now = new Date()
			const diff = now - date
			if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
			if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
			return (date.getMonth() + 1) + '月' + date.getDate() + '日'
		},
		goToPost(item) {
			if (item.postId) {
				const targetParam = item.targetId ? `&targetId=${item.targetId}` : '';
				uni.navigateTo({
					url: `/pages/post/detail?id=${item.postId}${targetParam}`
				})
			}
		},
		handleMore(item) {
			this.currentItem = item
			this.showActionSheet = true
		},
		async onActionSelect(action) {
			if (action.value === 'delete') {
				try {
					await notificationApi.delete(this.currentItem.id)
					this.list = this.list.filter(i => i.id !== this.currentItem.id)
					uni.showToast({ title: '已删除', icon: 'none' })
				} catch (e) {
					uni.showToast({ title: '删除失败', icon: 'none' })
				}
			} else if (action.value === 'mute') {
				uni.showToast({ title: '已开启不再通知', icon: 'none' })
			}
			this.showActionSheet = false
		},
		async loadData(refresh = false) {
			if (refresh) {
				this.page = 1
				this.list = []
			}
			this.loadStatus = 'loading'
			try {
				const res = await notificationApi.getList({
					page: this.page,
					size: 10,
					type: this.types
				})
				console.log('Like Detail API Full Response:', res);
				const newList = res.records || []
				console.log('Like Detail Records:', newList);
				
				// 后端点赞消息通常是合并的，如果不是，前端可以根据 targetId 进行简单的聚类（可选）
				this.list = refresh ? newList : [...this.list, ...newList]
				this.loadStatus = newList.length < 10 ? 'nomore' : 'loadmore'
				this.page++
			} catch (e) {
				console.error('Like Detail Load Error:', e);
				this.loadStatus = 'loadmore'
			}
		},
		loadMore() {
			if (this.loadStatus === 'loadmore') {
				this.loadData()
			}
		}
	}
}
</script>

<style lang="scss" scoped>
.container {
	min-height: 100vh;
	background-color: $pitch-pulse-bg-dark;
	color: #fff;
}

.custom-navbar {
	position: sticky;
	top: 0;
	z-index: 100;
	background-color: rgba($pitch-pulse-bg-dark, 0.8);
	backdrop-filter: blur(20px);
	border-bottom: 1rpx solid rgba(255, 255, 255, 0.05);

	.nav-content {
		height: 44px;
		display: flex;
		align-items: center;
		padding: 0 40rpx;

		.left {
			display: flex;
			align-items: center;
			min-width: 80rpx;
		}

		.title {
			flex: 1;
			text-align: center;
			font-size: 32rpx;
			font-weight: 700;
			color: #fff;
		}

		.right {
			display: flex;
			align-items: center;
			justify-content: flex-end;
			min-width: 80rpx;
		}
	}
}

.list-container {
	height: calc(100vh - 44px);
}

.notification-item {
	padding: 24rpx 32rpx;
	border-bottom: 1rpx solid rgba(255, 255, 255, 0.05);

	&:active {
		background-color: rgba(255, 255, 255, 0.02);
	}

	.item-header {
		display: flex;
		align-items: center;
		gap: 20rpx;
		width: 100%;
	}

	.avatar-group {
		display: flex;
		position: relative;
		width: 80rpx;
		height: 80rpx;
		flex-shrink: 0;
		
		:deep(.u-avatar) {
			border: 2rpx solid rgba(255, 255, 255, 0.1);
			background-color: rgba(255, 255, 255, 0.05);
			border-radius: 12rpx !important;
			
			.u-avatar__img {
				border-radius: 12rpx !important;
			}
		}

		.more-avatars {
			position: absolute;
			left: 24rpx;
			z-index: 1;
			border: 4rpx solid $pitch-pulse-bg-dark;
			border-radius: 12rpx;
			overflow: hidden;
		}
	}

	.content-box {
		flex: 1;
		min-width: 0;
		.like-info {
			font-size: 26rpx;
			line-height: 1.4;
			margin-bottom: 6rpx;
			color: rgba(255, 255, 255, 0.9);
			
			.username {
				font-weight: 700;
				color: #fff;
			}
			.others-text {
				font-weight: 700;
				color: #fff;
			}
			.action-text {
				color: rgba(255, 255, 255, 0.5);
				margin-left: 8rpx;
				font-size: 24rpx;
			}
		}
		.item-footer {
			.time {
				font-size: 20rpx;
				color: rgba(255, 255, 255, 0.4);
			}
		}
	}

	.right-preview-area {
		display: flex;
		align-items: center;
		gap: 12rpx;
		flex-shrink: 0;
		margin-top: 32rpx;
		margin-right: 10rpx; // 向右微调，减小原来的左偏量

		.more-btn {
			padding: 10rpx 0; // 增加点击热区
			display: flex;
			align-items: center;
			justify-content: center;
			&:active {
				opacity: 0.6;
			}
		}

		.origin-preview {
			width: 80rpx;
			height: 80rpx;
			background-color: rgba(255, 255, 255, 0.05);
			border-radius: 12rpx;
			overflow: hidden;
			border: 1rpx solid rgba(255, 255, 255, 0.1);
			
			.preview-text {
				font-size: 18rpx;
				color: rgba(255, 255, 255, 0.4);
				padding: 6rpx;
				display: -webkit-box;
				-webkit-box-orient: vertical;
				-webkit-line-clamp: 3;
				overflow: hidden;
			}
		}
	}
}
</style>
