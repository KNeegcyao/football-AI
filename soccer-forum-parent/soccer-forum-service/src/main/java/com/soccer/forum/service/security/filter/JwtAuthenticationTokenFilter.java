package com.soccer.forum.service.security.filter;

import com.soccer.forum.service.security.model.LoginUser;
import com.soccer.forum.service.service.TeamFollowService;
import com.soccer.forum.service.service.TeamService;
import com.soccer.forum.service.utils.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationTokenFilter.class);

    private final UserDetailsService userDetailsService;
    private final JwtUtils jwtUtils;
    private final TeamService teamService;
    private final TeamFollowService teamFollowService;

    public JwtAuthenticationTokenFilter(UserDetailsService userDetailsService, 
                                       JwtUtils jwtUtils,
                                       TeamService teamService,
                                       TeamFollowService teamFollowService) {
        this.userDetailsService = userDetailsService;
        this.jwtUtils = jwtUtils;
        this.teamService = teamService;
        this.teamFollowService = teamFollowService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        final String authHeader = request.getHeader("Authorization");

        final String jwt;
        final String username;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);
        try {
            username = jwtUtils.extractUsername(jwt);
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
                if (jwtUtils.validateToken(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    // 异步更新在线人数（续命）
                    if (userDetails instanceof LoginUser) {
                        Long userId = ((LoginUser) userDetails).getUser().getId();
                        // 这里可以考虑频率控制，但目前先简单实现
                        new Thread(() -> {
                            try {
                                java.util.List<Long> followedTeamIds = teamFollowService.getFollowedTeamIds(userId);
                                for (Long teamId : followedTeamIds) {
                                    teamService.updateOnlineCount(teamId, userId, true);
                                }
                            } catch (Exception e) {
                                // 忽略异步执行中的错误
                            }
                        }).start();
                    }
                }
            }
        } catch (Exception e) {
            log.error("Token processing error: {}", e.getMessage());
        }
        
        filterChain.doFilter(request, response);
    }
}
