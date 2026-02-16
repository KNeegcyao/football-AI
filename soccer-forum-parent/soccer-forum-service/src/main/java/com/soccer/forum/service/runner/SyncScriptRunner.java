package com.soccer.forum.service.runner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

@Component
public class SyncScriptRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(SyncScriptRunner.class);

    private Process pythonProcess;
    private Thread outputThread;

    @Override
    public void run(String... args) throws Exception {
        // 1. Determine script path
        String userDir = System.getProperty("user.dir");
        log.info("当前工作目录: {}", userDir);
        
        // 尝试相对路径 (假设从 soccer-forum-service 或 soccer-forum-parent 运行)
        File scriptFile = new File(userDir, "../../scripts/spider/sync_schedule.py");
        if (!scriptFile.exists()) {
             // 尝试绝对路径 (兜底)
             scriptFile = new File("D:/project/football/scripts/spider/sync_schedule.py");
        }
        
        if (!scriptFile.exists()) {
            log.error("未找到 Python 同步脚本: {}", scriptFile.getAbsolutePath());
            return;
        }

        log.info("找到 Python 同步脚本: {}", scriptFile.getAbsolutePath());

        // 2. Build command
        ProcessBuilder pb = new ProcessBuilder("python", scriptFile.getAbsolutePath());
        pb.redirectErrorStream(true); // 合并 stderr 到 stdout
        
        // 设置编码环境变量，防止中文乱码
        pb.environment().put("PYTHONIOENCODING", "utf-8");

        // 3. Start process
        try {
            this.pythonProcess = pb.start();
            log.info("Python 同步脚本已启动, PID: {}", this.pythonProcess.pid());

            // 4. Consume output in a separate thread
            outputThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(pythonProcess.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        log.info("[Python-Sync] {}", line);
                    }
                } catch (Exception e) {
                    if (pythonProcess.isAlive()) {
                        log.error("读取 Python 脚本输出时出错", e);
                    }
                }
            });
            outputThread.setDaemon(true);
            outputThread.setName("python-sync-logger");
            outputThread.start();

        } catch (Exception e) {
            log.error("启动 Python 脚本失败", e);
        }
    }

    @PreDestroy
    public void cleanup() {
        if (this.pythonProcess != null && this.pythonProcess.isAlive()) {
            log.info("正在停止 Python 同步脚本...");
            this.pythonProcess.destroy(); // SIGTERM
            try {
                if (!this.pythonProcess.waitFor(5, TimeUnit.SECONDS)) {
                    this.pythonProcess.destroyForcibly(); // SIGKILL
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                this.pythonProcess.destroyForcibly();
            }
            log.info("Python 同步脚本已停止");
        }
    }
}
