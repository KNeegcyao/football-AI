<template>
	<view class="container">
		<!-- 自定义导航栏 -->
		<view class="custom-navbar" :style="{ paddingTop: statusBarHeight + 'px' }">
			<view class="nav-content">
				<view class="left" @tap="goBack">
					<u-icon name="arrow-left" size="40" color="#FFFFFF"></u-icon>
				</view>
				<view class="title">新增粉丝</view>
				<view class="right">
					<u-icon name="setting" size="40" color="#FFFFFF"></u-icon>
				</view>
			</view>
		</view>

		<!-- 列表内容 -->
		<scroll-view scroll-y class="list-container" @scrolltolower="loadMore">
			<view v-for="(item, index) in list" :key="index" class="notification-item">
				<view class="item-content">
					<view class="avatar-box">
						<u-avatar :src="getFullImageUrl(item.fromUserAvatar)" size="112"></u-avatar>
					</view>
					<view class="info-box">
						<view class="user-line">
							<text class="username">{{ item.fromUserName }}</text>
							<text class="action-text">关注了你</text>
						</view>
						<text class="time">{{ formatTime(item.createTime) }}</text>
					</view>
				</view>
				<button class="follow-btn" :class="{ 'followed': item.isFollowed }" @tap.stop="handleFollow(item)">
					{{ item.isFollowed ? '已互粉' : '回关' }}
				</button>
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
			types: '5' // 新增粉丝
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
			if (date.getFullYear() === now.getFullYear()) {
				return (date.getMonth() + 1) + '月' + date.getDate() + '日'
			}
			return date.getFullYear() + '年' + (date.getMonth() + 1) + '月' + date.getDate() + '日'
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
		},
		handleFollow(item) {
			// 这里调用关注接口
			uni.showToast({
				title: item.isFollowed ? '已取消回关' : '回关成功',
				icon: 'none'
			})
			item.isFollowed = !item.isFollowed
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
}

.list-container {
	height: calc(100vh - 88rpx);
}

.notification-item {
	padding: 40rpx 30rpx;
	border-bottom: 1rpx solid rgba(255, 255, 255, 0.05);
	display: flex;
	align-items: center;
	justify-content: space-between;

	.item-content {
		display: flex;
		align-items: center;
		flex: 1;
		gap: 30rpx;
	}

	.info-box {
		display: flex;
		flex-direction: column;
		gap: 10rpx;
		.user-line {
			display: flex;
			align-items: center;
			gap: 10rpx;
			.username {
				font-size: 30rpx;
				font-weight: 600;
			}
			.action-text {
				font-size: 28rpx;
				color: #999;
			}
		}
		.time {
			font-size: 24rpx;
			color: #666;
		}
	}

	.follow-btn {
		min-width: 140rpx;
		height: 64rpx;
		line-height: 64rpx;
		border-radius: 32rpx;
		background-color: #991B1B;
		color: #FFFFFF;
		font-size: 26rpx;
		font-weight: 500;
		padding: 0 30rpx;
		margin: 0;
		border: none;

		&.followed {
			background-color: transparent;
			border: 1rpx solid #333;
			color: #999;
		}

		&:active {
			opacity: 0.8;
		}
	}
}
</style>
