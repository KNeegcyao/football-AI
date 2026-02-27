<template>
	<view class="container">
		<!-- 自定义导航栏 -->
		<view class="custom-navbar" :style="{ paddingTop: statusBarHeight + 'px' }">
			<view class="nav-content">
				<view class="left" @tap="goBack">
					<u-icon name="arrow-left" size="40" color="#FFFFFF"></u-icon>
				</view>
				<view class="title">回复与@</view>
				<view class="right">
					<u-icon name="setting" size="40" color="#FFFFFF"></u-icon>
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
			<view v-for="(item, index) in list" :key="index" class="notification-item">
				<view class="item-header">
					<view class="avatar-box">
						<u-avatar :src="getFullImageUrl(item.fromUserAvatar)" size="96"></u-avatar>
						<view v-if="item.isUp" class="up-badge">UP</view>
					</view>
					<view class="content-box">
						<view class="user-info">
							<text class="username">{{ item.fromUserName }}</text>
							<text class="action-text">回复了我的评论</text>
						</view>
						<view class="reply-content">
							<text v-if="item.replyTo" class="at-text">回复 <text class="highlight">@{{ item.replyTo }}</text> :</text>
							{{ item.content }}
						</view>
						<view v-if="item.quoteContent" class="quote-box">
							<text class="quote-text">{{ item.replyTo }}: {{ item.quoteContent }}</text>
						</view>
						<view class="item-footer">
							<text class="time">{{ formatTime(item.createTime) }}</text>
							<view class="actions">
								<view class="action-btn">
									<u-icon name="chat" size="28"></u-icon>
									<text>回复</text>
								</view>
								<view class="action-btn">
									<u-icon name="thumb-up" size="28"></u-icon>
									<text>点赞</text>
								</view>
								<u-icon name="more-dot-fill" size="32" color="#999"></u-icon>
							</view>
						</view>
					</view>
					<!-- 右侧引用的原内容图片/文字预览 -->
					<view class="origin-preview">
						<view class="preview-text">{{ item.originContent }}</view>
					</view>
				</view>
			</view>

			<u-loadmore :status="loadStatus" color="#999" />
		</scroll-view>
	</view>
</template>

<script>
import { notificationApi } from '@/api'
import { getFullImageUrl } from '@/utils/utils'

export default {
	data() {
		return {
			statusBarHeight: uni.getSystemInfoSync().statusBarHeight,
			list: [],
			page: 1,
			loadStatus: 'loadmore',
			types: '3,4' // 回复与@
		}
	},
	onLoad() {
		this.loadData(true)
	},
	methods: {
		getFullImageUrl,
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
		async loadData(refresh = false) {
			if (refresh) {
				this.page = 1
				this.list = []
			}
			this.loadStatus = 'loading'
			try {
				const res = await notificationApi.getList({
					page: this.page,
					pageSize: 10,
					type: this.types
				})
				const newList = res.data.list || []
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
	background-color: #121212;
	color: #FFFFFF;
}

.custom-navbar {
	position: sticky;
	top: 0;
	z-index: 100;
	background-color: rgba(18, 18, 18, 0.95);
	backdrop-filter: blur(20px);
	border-bottom: 1rpx solid rgba(255, 255, 255, 0.05);

	.nav-content {
		height: 88rpx;
		display: flex;
		align-items: center;
		justify-content: space-between;
		padding: 0 30rpx;

		.title {
			font-size: 32rpx;
			font-weight: bold;
		}
	}

	.tabs-box {
		display: flex;
		padding: 20rpx 30rpx;
		gap: 40rpx;

		.tab-item {
			font-size: 28rpx;
			color: #999;
			font-weight: 600;
			position: relative;
			padding-bottom: 10rpx;

			&.active {
				color: #FFFFFF;
				&::after {
					content: '';
					position: absolute;
					bottom: 0;
					left: 50%;
					transform: translateX(-50%);
					width: 40rpx;
					height: 4rpx;
					background-color: #991B1B;
					border-radius: 2rpx;
				}
			}
		}
	}
}

.list-container {
	height: calc(100vh - 200rpx);
}

.notification-item {
	padding: 30rpx;
	border-bottom: 1rpx solid rgba(255, 255, 255, 0.05);

	.item-header {
		display: flex;
		gap: 20rpx;
	}

	.avatar-box {
		position: relative;
		flex-shrink: 0;
		.up-badge {
			position: absolute;
			bottom: -4rpx;
			right: -4rpx;
			background-color: #991B1B;
			color: #FFF;
			font-size: 16rpx;
			font-weight: bold;
			padding: 2rpx 6rpx;
			border-radius: 4rpx;
			border: 2rpx solid #121212;
		}
	}

	.content-box {
		flex: 1;
		.user-info {
			display: flex;
			align-items: center;
			gap: 10rpx;
			margin-bottom: 8rpx;
			.username {
				font-size: 28rpx;
				font-weight: bold;
			}
			.action-text {
				font-size: 24rpx;
				color: #999;
			}
		}
		.reply-content {
			font-size: 28rpx;
			line-height: 1.5;
			margin-bottom: 12rpx;
			.at-text {
				.highlight {
					color: #991B1B;
					font-weight: 500;
				}
			}
		}
		.quote-box {
			background-color: rgba(255, 255, 255, 0.03);
			border-left: 4rpx solid rgba(255, 255, 255, 0.1);
			padding: 10rpx 20rpx;
			margin-bottom: 20rpx;
			.quote-text {
				font-size: 24rpx;
				color: #999;
			}
		}
	}

	.item-footer {
		display: flex;
		justify-content: space-between;
		align-items: center;
		.time {
			font-size: 22rpx;
			color: #666;
		}
		.actions {
			display: flex;
			align-items: center;
			gap: 30rpx;
			.action-btn {
				display: flex;
				align-items: center;
				gap: 6rpx;
				font-size: 24rpx;
				color: #999;
			}
		}
	}

	.origin-preview {
		width: 112rpx;
		height: 112rpx;
		background-color: rgba(255, 255, 255, 0.05);
		border-radius: 8rpx;
		overflow: hidden;
		flex-shrink: 0;
		.preview-text {
			font-size: 20rpx;
			color: #666;
			padding: 8rpx;
			display: -webkit-box;
			-webkit-box-orient: vertical;
			-webkit-line-clamp: 3;
			overflow: hidden;
		}
	}
}
</style>
