package com.soccer.forum.service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soccer.forum.domain.entity.User;
import com.soccer.forum.service.model.dto.PostCreateReq;
import com.soccer.forum.service.security.model.LoginUser;
import com.soccer.forum.service.service.PostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PostService postService;

    @Autowired
    private ObjectMapper objectMapper;

    private LoginUser loginUser;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testUser");
        loginUser = new LoginUser(user);
    }

    @Test
    void testCreatePost_Success() throws Exception {
        PostCreateReq req = new PostCreateReq();
        req.setTitle("这是一个合法的标题");
        req.setContent("这是一个合法的帖子内容，超过五个字。");

        when(postService.createPost(any(), anyLong())).thenReturn(1L);

        mockMvc.perform(post("/api/posts")
                .with(user(loginUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void testCreatePost_InvalidTitle() throws Exception {
        PostCreateReq req = new PostCreateReq();
        req.setTitle("短"); // 标题太短
        req.setContent("这是一个合法的帖子内容，超过五个字。");

        mockMvc.perform(post("/api/posts")
                .with(user(loginUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk()) // 即使校验失败，我们也可能返回 200，但 code 会是错误码（取决于全局异常处理）
                .andExpect(jsonPath("$.code").value(500)); // 这里的 500 是因为 GlobalExceptionHandler 捕获了 MethodArgumentNotValidException 并返回了 fail
    }

    @Test
    void testCreatePost_EmptyContent() throws Exception {
        PostCreateReq req = new PostCreateReq();
        req.setTitle("这是一个合法的标题");
        req.setContent(""); // 内容为空

        mockMvc.perform(post("/api/posts")
                .with(user(loginUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }
}
