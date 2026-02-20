package com.soccer.forum.service.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class DatabaseMigrationConfig implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseMigrationConfig.class);

    private final JdbcTemplate jdbcTemplate;

    public DatabaseMigrationConfig(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("Checking database schema...");
        try {
            // Check if news_id column exists in favorites table
            String checkSql = "SELECT count(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'favorites' AND column_name = 'news_id'";
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class);
            
            if (count != null && count == 0) {
                log.info("Adding news_id column to favorites table...");
                String alterSql = "ALTER TABLE favorites ADD COLUMN news_id BIGINT DEFAULT NULL COMMENT '新闻ID'";
                jdbcTemplate.execute(alterSql);
                log.info("Successfully added news_id column.");
            } else {
                log.info("Column news_id already exists in favorites table.");
            }
            
            // Check if post_id is nullable
            String checkPostIdSql = "SELECT is_nullable FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'favorites' AND column_name = 'post_id'";
            String isNullable = jdbcTemplate.queryForObject(checkPostIdSql, String.class);
            
            if ("NO".equals(isNullable)) {
                log.info("Modifying post_id column to be nullable...");
                String alterPostIdSql = "ALTER TABLE favorites MODIFY COLUMN post_id BIGINT DEFAULT NULL COMMENT '帖子ID'";
                jdbcTemplate.execute(alterPostIdSql);
                log.info("Successfully modified post_id column.");
            }
            
            // Add unique constraint for news_id and user_id if not exists
            String checkIndexSql = "SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'favorites' AND index_name = 'uk_news_user'";
            Integer indexCount = jdbcTemplate.queryForObject(checkIndexSql, Integer.class);
            
            if (indexCount != null && indexCount == 0) {
                log.info("Adding unique constraint uk_news_user to favorites table...");
                String addIndexSql = "CREATE UNIQUE INDEX uk_news_user ON favorites(news_id, user_id)";
                jdbcTemplate.execute(addIndexSql);
                log.info("Successfully added uk_news_user constraint.");
            }
            
        } catch (Exception e) {
            log.error("Database migration failed: {}", e.getMessage());
        }
    }
}
