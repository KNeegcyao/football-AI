package com.soccer.forum.service.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soccer.forum.domain.entity.Post;
import com.soccer.forum.service.mapper.PostMapper;
import com.soccer.forum.service.model.dto.PostCreateReq;
import com.soccer.forum.service.model.dto.PostPageReq;
import com.soccer.forum.service.service.impl.PostServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostMapper postMapper;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private PostServiceImpl postService;

    private PostCreateReq createReq;
    private Post post;
    private Long userId = 100L;
    private Long postId = 1L;

    @BeforeEach
    void setUp() {
        createReq = new PostCreateReq();
        createReq.setTitle("Test Post");
        createReq.setContent("Test Content");

        post = new Post();
        post.setId(postId);
        post.setUserId(userId);
        post.setTitle("Test Post");
        post.setStatus(1);
        post.setViews(10);
    }

    @Test
    void createPost_Success() {
        when(postMapper.insert(any(Post.class))).thenAnswer(invocation -> {
            Post p = invocation.getArgument(0);
            p.setId(postId);
            return 1;
        });

        Long resultId = postService.createPost(createReq, userId);

        assertNotNull(resultId);
        assertEquals(postId, resultId);
        verify(postMapper).insert(any(Post.class));
    }

    @Test
    void getPostById_Found_IncrementsViews_Redis() {
        when(postMapper.selectById(postId)).thenReturn(post);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(anyString())).thenReturn(1L);

        Post result = postService.getPostById(postId);

        assertNotNull(result);
        assertEquals(11, result.getViews()); // 10 + 1 (from memory)
        // Redis count is 1, so no DB update
        verify(postMapper, never()).updateById(any(Post.class));
    }

    @Test
    void getPostById_Found_SyncsToDB_Every10thView() {
        when(postMapper.selectById(postId)).thenReturn(post);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(anyString())).thenReturn(10L);

        Post result = postService.getPostById(postId);

        assertNotNull(result);
        assertEquals(20, result.getViews()); // 10 + 10 (synced)
        verify(postMapper).updateById(any(Post.class));
    }

    @Test
    void getPostById_NotFound() {
        when(postMapper.selectById(999L)).thenReturn(null);

        Post result = postService.getPostById(999L);

        assertNull(result);
        verify(postMapper, never()).updateById(any(Post.class));
    }

    @Test
    void getPostPage_Success() {
        PostPageReq pageReq = new PostPageReq();
        pageReq.setPage(1);
        pageReq.setSize(10);
        
        when(postMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(new Page<Post>());

        Page<Post> result = postService.getPostPage(pageReq);

        assertNotNull(result);
        verify(postMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    void deletePost_Success_Owner() {
        when(postMapper.selectById(postId)).thenReturn(post);
        when(postMapper.updateById(post)).thenReturn(1);

        assertDoesNotThrow(() -> postService.deletePost(postId, userId));
        
        assertEquals(0, post.getStatus()); // Should be marked as deleted
        verify(postMapper).updateById(post);
    }

    @Test
    void deletePost_Fail_NotOwner() {
        when(postMapper.selectById(postId)).thenReturn(post);
        Long otherUserId = 200L;

        Exception exception = assertThrows(RuntimeException.class, 
            () -> postService.deletePost(postId, otherUserId));
            
        assertEquals("权限不足: 只能删除自己的帖子", exception.getMessage());
        verify(postMapper, never()).updateById(any(Post.class));
    }

    @Test
    void deletePost_Fail_NotFound() {
        when(postMapper.selectById(postId)).thenReturn(null);

        Exception exception = assertThrows(RuntimeException.class, 
            () -> postService.deletePost(postId, userId));

        assertEquals("未找到帖子", exception.getMessage());
    }
}
