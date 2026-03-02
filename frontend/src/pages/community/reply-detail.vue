<template>
	<view class="container">
		<!-- 自定义导航栏 -->
		<view class="custom-navbar" :style="{ paddingTop: statusBarHeight + 'px' }">
			<view class="nav-content">
				<view class="left" @tap="goBack">
					<u-icon name="arrow-left" size="44rpx" color="#FFFFFF"></u-icon>
				</view>
				<view class="title">回复与@</view>
				<view class="right" @tap="showSettingSheet = true">
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
						<u-avatar :src="item.fromUser.avatar || '/static/default-avatar.png'" size="50" shape="square"></u-avatar>
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
									<u-icon name="chat" size="22"></u-icon>
									<text>回复</text>
								</view>
								<view class="action-btn" @tap.stop="handleLike(item)">
									<u-icon :name="item.isLiked ? 'thumb-up-fill' : 'thumb-up'" :color="item.isLiked ? '#991B1B' : ''" size="22"></u-icon>
									<text :style="{ color: item.isLiked ? '#991B1B' : '' }">点赞</text>
								</view>
							</view>
							<view class="more-btn" @tap.stop="handleMore(item)">
								<u-icon name="more-dot-fill" size="15" color="#999" customStyle="transform: rotate(90deg)"></u-icon>
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

		<!-- 设置菜单 (自定义 Popup 以实现极致流畅) -->
		<u-popup :show="showSettingSheet" mode="bottom" @close="showSettingSheet = false" round="24rpx" bgColor="#1a1811">
			<view class="setting-popup-content">
				<view class="popup-header">接收谁的回复提醒</view>
				<view class="setting-list">
					<view 
						v-for="(item, index) in settingActions.slice(0, 3)" 
						:key="index"
						class="setting-item"
						hover-class="setting-item-hover"
						:hover-stay-time="50"
						@tap="onSettingSelect(item)"
					>
						<text class="item-name" :class="{ 'active': item.active }">{{ item.name }}</text>
						<text v-if="item.subname" class="item-sub">{{ item.subname }}</text>
					</view>
				</view>
				<view class="popup-gap"></view>
				<view 
					class="popup-cancel" 
					hover-class="setting-item-hover"
					:hover-stay-time="50"
					@tap="showSettingSheet = false"
				>
					取消
				</view>
			</view>
		</u-popup>

		<!-- 提示组件 -->
		<u-toast ref="uToast" />

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
import { notificationApi, postApi, userApi } from '@/api'

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
			showSettingSheet: false,
			settingActions: [
				{ name: '所有人', value: 'all', subname: '推荐', active: true },
				{ name: '关注的人', value: 'following', active: false },
				{ name: '不接受任何消息提醒', value: 'none', active: false }
			],
			currentItem: {},
			showReplyPopup: false,
			replyContent: ''
		}
	},
	async onLoad() {
		this.loadData(true)
		// 初始显示“所有人”
		this.updateSettingUI('all')
		this.fetchNotificationSetting()
	},
	methods: {
		updateSettingUI(currentSetting) {
			this.settingActions.forEach(item => {
				item.color = item.value === currentSetting ? '#f2b90d' : ''
			})
		},
		async fetchNotificationSetting() {
			try {
				const res = await userApi.getProfile()
				console.log('User Profile for setting:', res)
				const currentSetting = res.replyNotificationType || 'all'
				this.updateSettingUI(currentSetting)
			} catch (e) {
				console.error('Fetch notification setting error:', e)
			}
		},
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
		async onSettingSelect(item) {
			this.showSettingSheet = false
			
			this.$refs.uToast.show({
				type: 'loading',
				title: '正在保存设置...',
				duration: 0
			})
			try {
				await userApi.updateNotificationSetting({ replyType: item.value })
				this.updateSettingUI(item.value)
				this.$refs.uToast.show({
					type: 'success',
					title: '设置已更新',
					message: '回复提醒设置已保存'
				})
			} catch (e) {
				console.error('Update notification setting error:', e)
				this.$refs.uToast.show({
					type: 'error',
					title: '设置失败',
					message: e.message || '服务器繁忙，请稍后再试'
				})
			}
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
				console.log('Reply Detail API Full Response:', res);
				const newList = res.records || []
				console.log('Reply Detail Records:', newList);
				
				this.list = refresh ? newList : [...this.list, ...newList]
				this.loadStatus = newList.length < 10 ? 'nomore' : 'loadmore'
				this.page++
			} catch (e) {
				console.error('Reply Detail Load Error:', e);
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

/* 移除 u-action-sheet 相关样式，改用自定义 popup 样式 */
.setting-popup-content {
	padding-bottom: env(safe-area-inset-bottom);
	
	.popup-header {
		padding: 32rpx;
		text-align: center;
		font-size: 28rpx;
		color: rgba(255, 255, 255, 0.4);
		border-bottom: 1rpx solid rgba(255, 255, 255, 0.05);
	}
	
	.setting-list {
		.setting-item {
			padding: 36rpx 0;
			display: flex;
			flex-direction: column;
			align-items: center;
			justify-content: center;
			border-bottom: 1rpx solid rgba(255, 255, 255, 0.05);
			transition: all 0.1s;
			
			.item-name {
				font-size: 32rpx;
				color: #fff;
				
				&.active {
					color: $pitch-pulse-primary;
					font-weight: bold;
				}
			}
			
			.item-sub {
				font-size: 24rpx;
				color: rgba(255, 255, 255, 0.3);
				margin-top: 8rpx;
			}
		}
	}
	
	.popup-gap {
		height: 12rpx;
		background-color: rgba(0, 0, 0, 0.3);
	}
	
	.popup-cancel {
		padding: 36rpx 0;
		text-align: center;
		font-size: 32rpx;
		color: rgba(255, 255, 255, 0.6);
		transition: all 0.1s;
	}
	
	.setting-item-hover {
		background-color: rgba(255, 255, 255, 0.05) !important;
	}
}

/* 统一 toast 背景色 */
::v-deep .u-toast {
	&__content {
		background-color: rgba(30, 30, 30, 0.9) !important;
	}
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
			bottom: -2rpx;
			right: -2rpx;
			background-color: $pitch-pulse-primary;
			color: #000;
			font-size: 12rpx;
			font-weight: 800;
			padding: 1rpx 3rpx;
			border-radius: 3rpx;
			border: 1rpx solid $pitch-pulse-bg-dark;
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
			justify-content: flex-start; // 整体靠左显示
			align-items: center;
			gap: 32rpx; // 增加时间与回复/点赞按钮组之间的间距
			
			.time {
				font-size: 20rpx;
				color: rgba(255, 255, 255, 0.3);
			}
			.actions {
				display: flex;
				align-items: center;
				gap: 24rpx; // 回复和点赞按钮之间的间距
				.action-btn {
					display: flex;
					align-items: center;
					gap: 4rpx;
					font-size: 20rpx;
					color: rgba(255, 255, 255, 0.4);
					
					&:active {
						color: #fff;
					}
				}
			}
			.more-btn {
				margin-left: auto; // 强制“更多”按钮推到最右侧
				padding: 0 4rpx;
				display: flex;
				align-items: center;
				justify-content: center;
				&:active {
					opacity: 0.6;
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
