<template>
	<view class="container">
		<!-- 自定义导航栏 -->
		<view class="custom-navbar" :style="{ paddingTop: statusBarHeight + 'px' }">
			<view class="nav-content">
				<view class="left" @tap="goBack">
					<u-icon name="arrow-left" size="40" color="#FFFFFF"></u-icon>
				</view>
				<view class="title">收到喜欢</view>
				<view class="right">
					<u-icon name="setting" size="40" color="#FFFFFF"></u-icon>
				</view>
			</view>
		</view>

		<!-- 列表内容 -->
		<scroll-view scroll-y class="list-container" @scrolltolower="loadMore">
			<view v-for="(item, index) in list" :key="index" class="notification-item">
				<view class="item-header">
					<view class="avatar-group">
						<u-avatar :src="getFullImageUrl(item.fromUserAvatar)" size="80"></u-avatar>
						<!-- 这里通常后端会返回点赞人数及其中的代表人物 -->
						<!-- 如果只是单个人，显示单个人头像；如果是多人，前端模拟聚合显示 -->
						<view v-if="item.likeCount > 1" class="more-avatars">
							<u-avatar :src="getFullImageUrl(item.extraAvatar)" size="80"></u-avatar>
						</view>
					</view>
					<view class="content-box">
						<view class="like-info">
							<text class="username">{{ item.fromUserName }}</text>
							<text v-if="item.likeCount > 1" class="others-text">、{{ item.extraUserName }} 等总计{{ item.likeCount }}人</text>
							<text class="action-text">赞了我的{{ item.targetType === 'post' ? '帖子' : '评论' }}</text>
						</view>
						<view class="item-footer">
							<text class="time">{{ formatTime(item.createTime) }}</text>
						</view>
					</view>
					<!-- 右侧引用的原内容图片/文字预览 -->
					<view class="origin-preview">
						<view class="preview-text">{{ item.originContent }}</view>
					</view>
					<u-icon name="more-dot-fill" size="32" color="#666"></u-icon>
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
			types: '1,2' // 帖子点赞、评论点赞
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
				// 后端点赞消息通常是合并的，如果不是，前端可以根据 targetId 进行简单的聚类（可选）
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
	background-color: #0F0F0F;
	color: #FFFFFF;
}

.custom-navbar {
	position: sticky;
	top: 0;
	z-index: 100;
	background-color: rgba(15, 15, 15, 0.95);
	backdrop-filter: blur(20px);

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
}

.list-container {
	height: calc(100vh - 88rpx);
}

.notification-item {
	padding: 30rpx;
	border-bottom: 1rpx solid rgba(255, 255, 255, 0.05);
	display: flex;
	align-items: center;
	gap: 20rpx;

	.avatar-group {
		display: flex;
		position: relative;
		width: 100rpx;
		height: 80rpx;
		flex-shrink: 0;
		.more-avatars {
			position: absolute;
			left: 20rpx;
			z-index: 1;
			border: 4rpx solid #0F0F0F;
			border-radius: 50%;
			overflow: hidden;
		}
	}

	.content-box {
		flex: 1;
		min-width: 0;
		.like-info {
			font-size: 30rpx;
			line-height: 1.4;
			margin-bottom: 8rpx;
			.username {
				font-weight: bold;
			}
			.others-text {
				font-weight: bold;
			}
			.action-text {
				color: #999;
				margin-left: 8rpx;
			}
		}
		.item-footer {
			.time {
				font-size: 24rpx;
				color: #666;
			}
		}
	}

	.origin-preview {
		width: 128rpx;
		height: 128rpx;
		background-color: rgba(255, 255, 255, 0.05);
		border-radius: 8rpx;
		overflow: hidden;
		flex-shrink: 0;
		.preview-text {
			font-size: 20rpx;
			color: #666;
			padding: 12rpx;
			display: -webkit-box;
			-webkit-box-orient: vertical;
			-webkit-line-clamp: 4;
			overflow: hidden;
		}
	}
}
</style>
