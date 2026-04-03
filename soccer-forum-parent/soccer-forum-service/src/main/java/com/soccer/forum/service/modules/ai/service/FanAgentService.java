package com.soccer.forum.service.modules.ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.soccer.forum.domain.entity.User;
import com.soccer.forum.domain.enums.UserRole;
import com.soccer.forum.domain.enums.UserStatus;
import com.soccer.forum.service.modules.ai.agent.FanAgent;
import com.soccer.forum.service.modules.community.model.CommentCreateReq;
import com.soccer.forum.service.modules.community.service.CommentService;
import com.soccer.forum.service.modules.user.mapper.UserMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class FanAgentService {

    private static final Logger log = LoggerFactory.getLogger(FanAgentService.class);

    private final FanAgent fanAgent;
    private final CommentService commentService;
    private final UserMapper userMapper;

    // Cache of Persona -> Bot User ID
    private final ConcurrentMap<String, Long> botUsers = new ConcurrentHashMap<>();

    private final String[] PERSONAS = {
            "战术宅",
            "情怀粉",
            "毒舌喷子"
    };

    public FanAgentService(FanAgent fanAgent, CommentService commentService, UserMapper userMapper) {
        this.fanAgent = fanAgent;
        this.commentService = commentService;
        this.userMapper = userMapper;
    }

    @PostConstruct
    public void initBotUsers() {
        log.info("初始化虚拟球迷用户...");
        initUser("战术宅", "tactics_nerd", "数据不撒谎，场上见真章", "https://api.dicebear.com/7.x/avataaars/svg?seed=tactics_nerd");
        initUser("情怀粉", "nostalgic_fan", "足球不只是胜负，更是人生", "https://api.dicebear.com/7.x/avataaars/svg?seed=nostalgic_fan");
        initUser("毒舌喷子", "toxic_critic", "别跟我讲理，我只看球", "https://api.dicebear.com/7.x/avataaars/svg?seed=toxic_critic");
        log.info("虚拟球迷用户初始化完成，当前缓存Bots: {}", botUsers.keySet());
    }

    private void initUser(String persona, String username, String bio, String avatar) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (user == null) {
            user = new User();
            user.setUsername(username);
            user.setPassword("NO_PASSWORD_BOT");
            user.setNickname(persona);
            user.setBio(bio);
            user.setAvatar(avatar);
            user.setRole(UserRole.USER);
            user.setStatus(UserStatus.NORMAL);
            user.setLevel(99); // bot level
            user.setExperience(9999);
            userMapper.insert(user);
            log.info("创建虚拟球迷用户成功: {} (ID: {})", username, user.getId());
        }
        botUsers.put(persona, user.getId());
    }

    /**
     * 为新帖子自动生成虚拟球迷评论
     *
     * @param postId 帖子ID
     * @param postContent 帖子内容
     */
    @Async
    public void generateCommentsForNewPost(Long postId, String postContent) {
        log.info("触发虚拟球迷互动体，为帖子 {} 生成评论...", postId);
        try {
            // 随机选择 1 到 2 个不同的虚拟球迷
            Random random = new Random();
            int count = random.nextInt(2) + 1; // 1 or 2
            
            List<String> selectedPersonas = new ArrayList<>();
            List<String> availablePersonas = new ArrayList<>(List.of(PERSONAS));
            Collections.shuffle(availablePersonas);
            
            for (int i = 0; i < count; i++) {
                selectedPersonas.add(availablePersonas.get(i));
            }

            for (String persona : selectedPersonas) {
                log.debug("虚拟球迷 [{}] 正在生成评论...", persona);
                String commentText = fanAgent.generateComment(persona, postContent);
                
                // 去除可能带有的引号或其他多余格式
                if (commentText != null) {
                    commentText = commentText.replaceAll("^[\"']|[\"']$", "").trim();
                    
                    Long botUserId = botUsers.get(persona);
                    if (botUserId != null) {
                        CommentCreateReq req = new CommentCreateReq();
                        req.setPostId(postId);
                        req.setContent(commentText);
                        
                        commentService.createComment(req, botUserId);
                        log.info("虚拟球迷 [{}] 评论成功，帖子ID: {}", persona, postId);
                    }
                }
                
                // 稍微延迟一下，模拟真实的回复时间差
                Thread.sleep(1000 + random.nextInt(2000));
            }
            
        } catch (Exception e) {
            log.error("虚拟球迷生成评论失败，帖子ID: {}, 错误: {}", postId, e.getMessage(), e);
        }
    }
}
