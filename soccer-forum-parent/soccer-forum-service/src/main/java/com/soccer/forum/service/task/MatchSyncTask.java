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

    @Value("${match.crawler.script-path:D:/project/football/scripts/live_crawler.py}")
    private String crawlerPath;

    @Value("${match.sync.python-path:python}")
    private String pythonPath;

    /**
     * 每 1 分钟执行一次实时爬虫 (针对进行中比赛)
     * cron: 0 0/1 * * * ?
     */
    // @Scheduled(fixedRate = 60000, initialDelay = 10000)
    public void liveScoreSync() {
        log.info("开始执行秒级实时爬虫同步任务...");
        executePythonScript(crawlerPath, "实时比分同步");
    }

    /**
     * 每 5 分钟执行一次全量 API 同步
     * initialDelay = 5000 表示服务启动 5 秒后立即执行第一次
     */
    // @Scheduled(fixedRate = 300000, initialDelay = 5000)
    public void syncMatchData() {
        log.info("开始执行赛程全量 API 同步任务...");
        executePythonScript(scriptPath, "API全量同步");
    }

    /**
     * 通用的 Python 脚本执行逻辑
     */
    private void executePythonScript(String path, String taskName) {
        try {
            ProcessBuilder pb = new ProcessBuilder(pythonPath, path);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.debug("{} Output: {}", taskName, line);
                }
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                log.info("{} 执行成功", taskName);
            } else {
                log.error("{} 执行失败，退出代码: {}", taskName, exitCode);
            }
        } catch (Exception e) {
            log.error("调用 Python 脚本执行 {} 时发生异常", taskName, e);
        }
    }
}
