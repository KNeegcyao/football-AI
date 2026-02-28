package com.soccer.forum.service.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * 赛程同步定时任务
 * <p>
 * 每 5 分钟调用外部 Python 脚本抓取最新赛程数据。
 * </p>
 */
@Component
public class MatchSyncTask {

    private static final Logger log = LoggerFactory.getLogger(MatchSyncTask.class);

    @Value("${match.sync.script-path:D:/project/football/scripts/fetch_schedule.py}")
    private String scriptPath;

    @Value("${match.sync.python-path:python}")
    private String pythonPath;

    /**
     * 每 5 分钟执行一次 (cron: 0 0/5 * * * ?)
     * initialDelay = 5000 表示服务启动 5 秒后立即执行第一次
     */
    @Scheduled(fixedRate = 300000, initialDelay = 5000)
    public void syncMatchData() {
        log.info("开始执行赛程同步定时任务...");
        try {
            ProcessBuilder pb = new ProcessBuilder(pythonPath, scriptPath);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            // 读取脚本输出，防止缓冲区溢出导致进程挂起
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.debug("Python Script Output: {}", line);
                }
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                log.info("赛程同步任务执行成功");
            } else {
                log.error("赛程同步任务执行失败，退出代码: {}", exitCode);
            }
        } catch (Exception e) {
            log.error("调用 Python 脚本执行赛程同步时发生异常", e);
        }
    }
}
