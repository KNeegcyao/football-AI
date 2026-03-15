package com.soccer.forum.service.modules.user.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soccer.forum.common.R;
import com.soccer.forum.domain.entity.ExperienceRecord;
import com.soccer.forum.service.modules.user.model.LoginUser;
import com.soccer.forum.service.modules.user.service.ExperienceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 经验流水管理控制器。
 * <p>
 * 提供用户经验值的增加流水查询、分页获取等功能。
 * </p>
 *
 * @author Soccer Forum Dev Team
 * @version 1.0
 */
@Tag(name = "经验值记录", description = "用户经验值变动记录接口")
@RestController
@RequestMapping("/api/user/experience")
@RequiredArgsConstructor
public class ExperienceRecordController {

    private static final Logger log = LoggerFactory.getLogger(ExperienceRecordController.class);

    private final ExperienceService experienceService;

    /**
     * 获取我的经验流水
     */
    @Operation(summary = "获取我的经验流水")
    @GetMapping("/records")
    public R<List<ExperienceRecord>> getMyRecords(@Parameter(hidden = true) @AuthenticationPrincipal LoginUser loginUser) {
        log.info("获取经验流水: 用户ID={}", loginUser.getUser().getId());
        List<ExperienceRecord> records = experienceService.getRecordsByUserId(loginUser.getUser().getId());
        return R.<List<ExperienceRecord>>ok(records);
    }

    /**
     * 分页获取我的经验流水
     */
    @Operation(summary = "分页获取我的经验流水")
    @GetMapping("/records/page")
    public R<Page<ExperienceRecord>> getMyRecordsPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @Parameter(hidden = true) @AuthenticationPrincipal LoginUser loginUser) {
        log.info("分页获取经验流水: 用户ID={}, page={}, size={}", loginUser.getUser().getId(), page, size);
        Page<ExperienceRecord> recordsPage = experienceService.getRecordsPageByUserId(loginUser.getUser().getId(), page, size);
        return R.ok(recordsPage);
    }
}
