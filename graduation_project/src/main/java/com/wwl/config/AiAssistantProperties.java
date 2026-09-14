package com.wwl.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AI 助手配置类
 * 
 * 原理：Spring Boot 启动时会自动扫描 application.yml 中
 *       "ai.assistant" 开头的配置项，自动注入到本类的字段中。
 * 
 * 例如 application.yml 中：
 *   ai:
 *     assistant:
 *       api-key: sk-xxx      → 自动赋给 this.apiKey
 *       base-url: https://... → 自动赋给 this.baseUrl
 *       model: deepseek-chat  → 自动赋给 this.model
 * 
 * 好处：密钥和地址集中管理，改配置不用改代码。
 */
@Data
@Component
@ConfigurationProperties(prefix = "ai.assistant")
public class AiAssistantProperties {
    /** API 密钥，用于鉴权，从环境变量 AI_API_KEY 或 yml 中读取 */
    private String apiKey;
    /** 大模型 API 地址，兼容 OpenAI 格式 */
    private String baseUrl;
    /** 使用的模型名称，如 deepseek-chat / gpt-3.5-turbo */
    private String model;
}