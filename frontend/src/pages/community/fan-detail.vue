<template>
	<view class="container">
		<!-- 自定义导航栏 -->
		<view class="custom-navbar" :style="{ paddingTop: statusBarHeight + 'px' }">
			<view class="nav-content">
				<view class="left" @tap="goBack">
					<u-icon name="arrow-left" size="44rpx" color="#FFFFFF"></u-icon>
				</view>
				<view class="title">新增粉丝</view>
				<view class="right">
					<u-icon name="setting" size="40rpx" color="#FFFFFF"></u-icon>
				</view>
			</view>
		</view>

		<!-- 列表内容 -->
		<scroll-view scroll-y class="list-container" @scrolltolower="loadMore">
			<view v-for="item in list" :key="item.id" class="notification-item">
				<view class="item-content">
					<view class="avatar-box">
						<u-avatar :src="item.fromUser.avatar || '/static/default-avatar.png'" size="80" shape="square"></u-avatar>
					</view>
					<view class="info-box">
						<view class="user-line">
							<text class="username">{{ item.fromUser.nickname }}</text>
							<text class="action-text">关注了你</text>
						</view>
						<text class="time">{{ formatTime(item.createdAt) }}</text>
					</view>
				</view>
				<button class="follow-btn" :class="{ 'followed': item.isFollowed }" @tap.stop="handleFollow(item)">
					{{ item.isFollowed ? '已互粉' : '回关' }}
				</button>
			</view>

			<u-loadmore :status="loadStatus" color="#999" />
			<view style="height: 40rpx;"></view>
		</scroll-view>
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
			types: '5' // 新增粉丝
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
			if (date.getFullYear() === now.getFullYear()) {
				return (date.getMonth() + 1) + '月' + date.getDate() + '日'
			}
			return date.getFullYear() + '年' + (date.getMonth() + 1) + '月' + date.getDate() + '日'
		},
		async loadData(refresh = false) {
			if (this.isLoading) return
			
			if (refresh) {
				this.page = 1
				this.loadStatus = 'loading'
			} else if (this.loadStatus === 'nomore') {
				return
			}

			this.isLoading = true
			try {
				const res = await notificationApi.getList({
					page: this.page,
					size: 10,
					type: this.types
				})
				
				const newList = res.records || []
				
				if (refresh) {
					this.list = newList
					uni.stopPullDownRefresh()
				} else {
					// 过滤重复数据
					const existingIds = this.list.map(item => item.id)
					const filteredNewList = newList.filter(item => !existingIds.includes(item.id))
					this.list = [...this.list, ...filteredNewList]
				}

				if (newList.length < 10) {
					this.loadStatus = 'nomore'
				} else {
					this.loadStatus = 'loadmore'
					this.page++
				}
			} catch (e) {
				console.error('加载粉丝列表失败:', e)
				this.loadStatus = 'loadmore'
				if (refresh) uni.stopPullDownRefresh()
			} finally {
				this.isLoading = false
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
	background-color: $pitch-pulse-bg-dark;
	color: #FFFFFF;
	display: flex;
	flex-direction: column;
}

.custom-navbar {
	background-color: rgba($pitch-pulse-bg-dark, 0.8);
	backdrop-filter: blur(20px);
	border-bottom: 1rpx solid rgba(255, 255, 255, 0.05);
	flex-shrink: 0;

	.nav-content {
		height: 88rpx;
		display: flex;
		align-items: center;
		padding: 0 40rpx;

		.left {
			flex: 1;
			display: flex;
			align-items: center;
		}

		.title {
			flex: 2;
			text-align: center;
			font-size: 32rpx;
			font-weight: 700;
			letter-spacing: 2rpx;
		}

		.right {
			flex: 1;
			display: flex;
			align-items: center;
			justify-content: flex-end;
		}
	}
}

.list-container {
	flex: 1;
	height: 0; // 配合 flex: 1 使用，确保在不同设备上高度正确
}

.notification-item {
	padding: 32rpx 40rpx;
	border-bottom: 1rpx solid rgba(255, 255, 255, 0.05);
	display: flex;
	align-items: center;
	justify-content: space-between;

	.item-content {
		display: flex;
		align-items: center;
		flex: 1;
		gap: 24rpx;

		:deep(.u-avatar) {
			border: 2rpx solid rgba(255, 255, 255, 0.1);
			background-color: rgba(255, 255, 255, 0.05);
			border-radius: 16rpx !important;
			
			.u-avatar__img {
				border-radius: 16rpx !important;
			}
		}
	}

	.info-box {
		display: flex;
		flex-direction: column;
		gap: 4rpx;

		.user-line {
			display: flex;
			align-items: center;
			gap: 12rpx;

			.username {
				font-size: 28rpx;
				font-weight: 600;
				color: #fff;
			}

			.action-text {
				font-size: 26rpx;
				color: rgba(255, 255, 255, 0.4);
			}
		}

		.time {
			font-size: 22rpx;
			color: rgba(255, 255, 255, 0.2);
		}
	}

	.follow-btn {
		min-width: 130rpx;
		height: 56rpx;
		line-height: 56rpx;
		border-radius: 28rpx;
		background-color: $pitch-pulse-primary;
		color: #FFFFFF;
		font-size: 24rpx;
		font-weight: 600;
		padding: 0 28rpx;
		margin: 0;
		border: none;
		box-shadow: 0 4rpx 12rpx rgba($pitch-pulse-primary, 0.3);
		transition: all 0.2s ease;

		&.followed {
			background-color: rgba(255, 255, 255, 0.05);
			border: 1rpx solid rgba(255, 255, 255, 0.1);
			color: rgba(255, 255, 255, 0.4);
			box-shadow: none;
		}

		&:active {
			transform: scale(0.95);
			opacity: 0.9;
		}
	}
}
</style>
