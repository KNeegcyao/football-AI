package com.soccer.forum.service.modules.ai.agent;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

/**
 * 数据查询助手
 * 通过调用工具查询数据库或 API
 * 注意：此接口通过 AiConfig 手动注册为 Bean，以绑定 Tools
 */
public interface DataQueryAgent {

    @SystemMessage("你是一个足球数据助手，可以通过调用工具查询球队和球星数据。请准确提取用户意图并调用相应工具。如果用户问的问题不在工具能力范围内，请礼貌告知。")
    String query(@UserMessage String question);
}
