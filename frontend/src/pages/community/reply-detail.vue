<template>
	<view class="container">
		<!-- 自定义导航栏 -->
		<view class="custom-navbar" :style="{ paddingTop: statusBarHeight + 'px' }">
			<view class="nav-content">
				<view class="left" @tap="goBack">
					<u-icon name="arrow-left" size="44rpx" color="#FFFFFF"></u-icon>
				</view>
				<view class="title">回复与@</view>
				<view class="right">
					<u-icon name="setting" size="40rpx" color="#FFFFFF"></u-icon>
				</view>
			</view>
			<!-- 顶部筛选 Tab -->
			<view class="tabs-box">
				<view class="tab-item active">全部</view>
				<view class="tab-item">@我</view>
			</view>
		</view>

		<!-- 列表内容 -->
		<scroll-view scroll-y class="list-container" @scrolltolower="loadMore">
			<view v-for="(item, index) in list" :key="index" class="notification-item" @tap="goToPost(item)">
				<view class="item-header">
					<view class="avatar-box">
						<u-avatar :src="item.fromUser.avatar || '/static/default-avatar.png'" size="72" shape="square"></u-avatar>
						<view v-if="item.fromUser.id === 1" class="up-badge">OFFICIAL</view>
					</view>
					<view class="content-box">
						<view class="user-info">
							<text class="username">{{ item.fromUser.nickname }}</text>
							<text class="action-text">{{ item.type === 3 ? '评论了我的帖子' : '回复了我的评论' }}</text>
						</view>
						<view class="reply-content">
							<text v-if="item.replyTo" class="at-text">回复 <text class="highlight">@{{ item.replyTo }}</text> :</text>
							{{ item.content }}
						</view>
						<view v-if="item.quoteContent" class="quote-box">
							<text class="quote-text">{{ item.replyTo }}: {{ item.quoteContent }}</text>
						</view>
						<view class="item-footer">
							<text class="time">{{ formatTime(item.createdAt) }}</text>
							<view class="actions">
								<view class="action-btn" @tap.stop="handleReply(item)">
									<u-icon name="chat" size="28"></u-icon>
									<text>回复</text>
								</view>
								<view class="action-btn" @tap.stop="handleLike(item)">
									<u-icon :name="item.isLiked ? 'thumb-up-fill' : 'thumb-up'" :color="item.isLiked ? '#991B1B' : ''" size="28"></u-icon>
									<text :style="{ color: item.isLiked ? '#991B1B' : '' }">点赞</text>
								</view>
								<view class="more-btn" @tap.stop="handleMore(item)">
									<u-icon name="more-dot-fill" size="32" color="#999"></u-icon>
								</view>
							</view>
						</view>
					</view>
					<!-- 右侧引用的原内容图片/文字预览 -->
					<view class="origin-preview">
						<view class="preview-text">{{ item.postTitle }}</view>
					</view>
				</view>
			</view>

			<u-loadmore :status="loadStatus" color="#999" />
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

		<!-- 快捷回复弹窗 -->
		<u-popup 
			:show="showReplyPopup" 
			mode="bottom" 
			@close="showReplyPopup = false" 
			round="24rpx"
			bgColor="#1a1811"
		>
			<view class="reply-popup-content">
				<view class="popup-header">
					<text class="title">回复 @{{ currentItem.fromUser ? currentItem.fromUser.nickname : '' }}</text>
					<text class="send-btn" :class="{ disabled: !replyContent.trim() }" @tap="submitReply">发送</text>
				</view>
				<textarea 
					class="reply-input" 
					v-model="replyContent" 
					placeholder="说点什么吧..." 
					fixed 
					focus 
					auto-height 
					cursor-spacing="40"
					placeholder-style="color: rgba(255, 255, 255, 0.2)"
				/>
			</view>
		</u-popup>
	</view>
</template>

<script>
import { notificationApi, postApi } from '@/api'

export default {
	data() {
		return {
			statusBarHeight: uni.getSystemInfoSync().statusBarHeight,
			list: [],
			page: 1,
			loadStatus: 'loadmore',
			types: '3,4', // 回复与@
			showActionSheet: false,
			actionActions: [
				{ name: '不再通知', value: 'mute' },
				{ name: '删除', value: 'delete', color: '#ff4d4f' }
			],
			currentItem: {},
			showReplyPopup: false,
			replyContent: ''
		}
	},
	onLoad() {
		this.loadData(true)
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
				uni.navigateTo({
					url: `/pages/community/post-detail?id=${item.postId}`
				})
			}
		},
		handleReply(item) {
			this.currentItem = item
			this.replyContent = ''
			this.showReplyPopup = true
		},
		async submitReply() {
			if (!this.replyContent.trim()) return
			try {
				await postApi.createComment({
					postId: this.currentItem.postId,
					content: this.replyContent,
					parentId: this.currentItem.targetId // 如果是回复评论，targetId 就是 commentId
				})
				uni.showToast({ title: '回复成功', icon: 'success' })
				this.showReplyPopup = false
			} catch (e) {
				uni.showToast({ title: '回复失败', icon: 'none' })
			}
		},
		async handleLike(item) {
			try {
				await postApi.like({
					targetId: item.targetId,
					targetType: 2 // 评论点赞
				})
				item.isLiked = !item.isLiked
				uni.showToast({ title: item.isLiked ? '点赞成功' : '取消点赞', icon: 'none' })
			} catch (e) {
				uni.showToast({ title: '操作失败', icon: 'none' })
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
				const newList = res.records || []
				this.list = refresh ? newList : [...this.list, ...newList]
				this.loadStatus = newList.length < 10 ? 'nomore' : 'loadmore'
				this.page++
			} catch (e) {
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

	.tabs-box {
		display: flex;
		padding: 20rpx 40rpx;
		gap: 48rpx;

		.tab-item {
			font-size: 28rpx;
			color: rgba(255, 255, 255, 0.4);
			font-weight: 600;
			position: relative;
			padding-bottom: 12rpx;

			&.active {
				color: #fff;
				&::after {
					content: '';
					position: absolute;
					bottom: 0;
					left: 50%;
					transform: translateX(-50%);
					width: 32rpx;
					height: 4rpx;
					background-color: $pitch-pulse-primary;
					border-radius: 2rpx;
					box-shadow: 0 0 10rpx rgba($pitch-pulse-primary, 0.5);
				}
			}
		}
	}
}

.list-container {
	height: calc(100vh - 100px);
}

.notification-item {
	padding: 24rpx 32rpx;
	border-bottom: 1rpx solid rgba(255, 255, 255, 0.05);

	&:active {
		background-color: rgba(255, 255, 255, 0.02);
	}

	.item-header {
		display: flex;
		gap: 20rpx;
	}

	.avatar-box {
		position: relative;
		flex-shrink: 0;
		
		:deep(.u-avatar) {
			border: 2rpx solid rgba(255, 255, 255, 0.1);
			background-color: rgba(255, 255, 255, 0.05);
			border-radius: 12rpx !important;
			
			.u-avatar__img {
				border-radius: 12rpx !important;
			}
		}

		.up-badge {
			position: absolute;
			bottom: -4rpx;
			right: -4rpx;
			background-color: $pitch-pulse-primary;
			color: #000;
			font-size: 14rpx;
			font-weight: 800;
			padding: 2rpx 4rpx;
			border-radius: 4rpx;
			border: 2rpx solid $pitch-pulse-bg-dark;
		}
	}

	.content-box {
		flex: 1;
		min-width: 0;
		.user-info {
			display: flex;
			align-items: center;
			gap: 12rpx;
			margin-bottom: 6rpx;
			.username {
				font-size: 26rpx;
				font-weight: 700;
				color: #fff;
			}
			.action-text {
				font-size: 22rpx;
				color: rgba(255, 255, 255, 0.4);
			}
		}
		.reply-content {
			font-size: 26rpx;
			line-height: 1.4;
			margin-bottom: 12rpx;
			color: rgba(255, 255, 255, 0.9);
			.at-text {
				color: rgba(255, 255, 255, 0.4);
				margin-right: 8rpx;
				.highlight {
					color: $pitch-pulse-primary;
					font-weight: 500;
				}
			}
		}
		.quote-box {
			margin-bottom: 12rpx;
			padding: 12rpx 16rpx;
			background-color: rgba(255, 255, 255, 0.03);
			border-radius: 8rpx;
			border-left: 4rpx solid $pitch-pulse-primary;
			.quote-text {
				font-size: 22rpx;
				color: rgba(255, 255, 255, 0.4);
				display: -webkit-box;
				-webkit-box-orient: vertical;
				-webkit-line-clamp: 2;
				overflow: hidden;
			}
		}
		.item-footer {
			display: flex;
			justify-content: space-between;
			align-items: center;
			.time {
				font-size: 20rpx;
				color: rgba(255, 255, 255, 0.3);
			}
			.actions {
				display: flex;
				align-items: center;
				gap: 24rpx;
				.action-btn {
					display: flex;
					align-items: center;
					gap: 6rpx;
					font-size: 22rpx;
					color: rgba(255, 255, 255, 0.4);
					
					&:active {
						color: #fff;
					}
				}
				.more-btn {
					padding: 0 10rpx;
					display: flex;
					align-items: center;
					justify-content: center;
					&:active {
						opacity: 0.6;
					}
				}
			}
		}
	}

	.origin-preview {
		width: 80rpx;
		height: 80rpx;
		background-color: rgba(255, 255, 255, 0.05);
		border-radius: 12rpx;
		overflow: hidden;
		flex-shrink: 0;
		border: 1rpx solid rgba(255, 255, 255, 0.1);
		
		.preview-text {
			font-size: 18rpx;
			color: rgba(255, 255, 255, 0.3);
			padding: 6rpx;
			display: -webkit-box;
			-webkit-box-orient: vertical;
			-webkit-line-clamp: 3;
			overflow: hidden;
		}
	}

	.reply-popup-content {
		padding: 32rpx;
		background-color: #1a1811;
		border-radius: 24rpx 24rpx 0 0;
		padding-bottom: calc(32rpx + env(safe-area-inset-bottom));
		width: 100%;
		box-sizing: border-box;
		
		.popup-header {
			display: flex;
			justify-content: space-between;
			align-items: center;
			margin-bottom: 32rpx;
			width: 100%;
			
			.title {
				font-size: 28rpx;
				color: rgba(255, 255, 255, 0.4);
				flex: 1;
			}
			
			.send-btn {
				font-size: 30rpx;
				color: $pitch-pulse-primary;
				font-weight: 600;
				padding: 10rpx 20rpx;
				margin-left: 20rpx;
				
				&.disabled {
					color: rgba(255, 255, 255, 0.1);
				}
			}
		}
		
		.reply-input {
			width: 100%;
			min-height: 120rpx;
			max-height: 300rpx;
			background-color: rgba(255, 255, 255, 0.05);
			border-radius: 12rpx;
			padding: 24rpx;
			font-size: 30rpx;
			color: #fff;
			line-height: 1.5;
			display: block;
		}
	}
}
</style>
