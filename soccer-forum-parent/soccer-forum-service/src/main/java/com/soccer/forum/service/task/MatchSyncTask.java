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
 * 赛事同步定时任务
 * <p>
 * 每 5 分钟调用外部 Python 脚本抓取最新赛事数据。
 * </p>
 */
@Component
public class MatchSyncTask {

    private static final Logger log = LoggerFactory.getLogger(MatchSyncTask.class);

    @Value("${match.sync.script-path:D:/project/football/scripts/fetch_schedule.py}")
    private String scriptPath;

    @Value("${match.crawler.script-path:D:/project/football/scripts/live_crawler.py}")
    private String crawlerPath;

    @Value("${match.stats.script-path:D:/project/football/scripts/fetch_match_stats.py}")
    private String statsScriptPath;

    @Value("${news.script-path:D:/project/football/scripts/fetch_news.py}")
    private String newsScriptPath;

    @Value("${match.sync.python-path:python}")
    private String pythonPath;

    /**
     * 每 1 分钟执行一次实时比分同步 (跨越比赛列表)
     * cron: 0 0/1 * * * ?
     */
    @Scheduled(fixedRate = 60000, initialDelay = 10000)
    public void liveScoreSync() {
        log.info("开始执行顶级实时比分同步...");
        executePythonScript(crawlerPath, "实时比分同步");
    }

    /**
     * 每 5 分钟执行一次全量 API 同步
     * initialDelay = 5000 表示启动后 5 秒开始执行第一次
     */
    @Scheduled(fixedRate = 300000, initialDelay = 5000)
    public void syncMatchData() {
        log.info("开始执行赛事全量 API 同步任务...");
        executePythonScript(scriptPath, "API全量同步");
    }

    /**
     * 每天凌晨 3:00 执行一次完赛真实数据统计与关键事件同步
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void syncMatchStats() {
        log.info("开始执行完赛数据真实统计同步...");
        executePythonScript(statsScriptPath, "完赛真实数据同步");
    }

    /**
     * 每天凌晨 4:00 执行一次新闻抓取
     */
    @Scheduled(cron = "0 0 4 * * ?")
    public void syncNews() {
        log.info("开始执行足球新闻同步...");
        executePythonScript(newsScriptPath, "足球新闻同步");
    }

    /**
     * 通过 Python 脚本执行逻辑
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
                log.error("{} 执行失败，退出码: {}", taskName, exitCode);
            }
        } catch (Exception e) {
            log.error("调用 Python 脚本执行 {} 时发生异常", taskName, e);
        }
    }
}
