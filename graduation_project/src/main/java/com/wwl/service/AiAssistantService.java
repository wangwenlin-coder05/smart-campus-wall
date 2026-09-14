package com.wwl.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wwl.config.AiAssistantProperties;
import com.wwl.mapper.LostFoundMapper;
import com.wwl.mapper.OrganizationActivityMapper;
import com.wwl.mapper.WallPostMapper;
import com.wwl.model.entity.LostFoundItem;
import com.wwl.model.entity.OrganizationActivity;
import com.wwl.model.entity.WallPost;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * AI 校园助手核心业务服务（RAG 版本）
 * 
 * RAG = Retrieval-Augmented Generation（检索增强生成）
 * 
 * 工作流程：
 * 1. 用户提问（例如"今晚有什么活动？"）
 * 2. 检索阶段：搜索数据库中的帖子、组局、失物招领
 * 3. 增强阶段：把检索结果拼成文本，注入到 System Prompt 中
 * 4. 生成阶段：AI 基于实时数据 + 知识库生成回答
 * 
 * 好处：
 * - 不用自己训练大模型（省显卡）
 * - 数据实时更新（数据库有变化，AI 马上知道）
 * - 成本低（只按 API 调用量付费）
 */
@Slf4j
@Service
public class AiAssistantService {

    /** 注入配置（application.yml 中的 ai.assistant 配置） */
    private final AiAssistantProperties properties;
    /** Java 原生 HTTP 客户端，用来给 AI API 发请求 */
    private final HttpClient httpClient;
    /** Jackson JSON 工具，用来序列化请求体、解析响应 */
    private final ObjectMapper objectMapper;

    /** 注入 Mapper，用于搜索数据库中的实时数据 */
    private final WallPostMapper wallPostMapper;
    private final OrganizationActivityMapper organizationActivityMapper;
    private final LostFoundMapper lostFoundMapper;

    /**
     * System Prompt（系统提示词）—— 静态知识库
     * 
     * 这是"死"知识，写死在代码里的校园基本信息。
     * 动态知识（帖子、组局等）通过 RAG 检索后动态注入。
     */
    private static final String SYSTEM_PROMPT = """
            你是校园助手小墙，为大学生提供校园生活帮助。请用亲切友好的语气回答。
            
            校园基本信息（知识库）：
            - 图书馆开放时间：周一至周日 8:00-22:00，节假日另行通知
            - 食堂营业时间：早餐 6:30-9:00，午餐 11:00-13:00，晚餐 17:00-19:00
            - 教学楼开放时间：每天 7:00-22:30
            - 校园卡充值：可在食堂一楼充值窗口办理，或通过校园APP在线充值
            - 快递驿站：位于学生宿舍区，营业时间 9:00-20:00
            - 校医院：24小时值班，急诊电话 120
            - 校园网：学生账号免费使用，密码为身份证后6位
            
            常见问题：
            - 选课：每学期末通过教务系统选课，具体时间关注教务处通知
            - 奖学金：每年9月评选，包括国家奖学金、校级奖学金等
            - 社团：每学期初有社团招新活动，可关注学生会公众号
            
            当用户询问活动、帖子、失物招领等实时信息时，请优先参考"当前校园动态数据"。
            如果用户问的问题你不知道，请诚实告知。
            用简洁的格式回复，适当使用emoji让回复更亲切。
            """;

    /**
     * 构造函数，Spring 自动注入所有依赖
     */
    public AiAssistantService(AiAssistantProperties properties,
                              ObjectMapper objectMapper,
                              WallPostMapper wallPostMapper,
                              OrganizationActivityMapper organizationActivityMapper,
                              LostFoundMapper lostFoundMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.wallPostMapper = wallPostMapper;
        this.organizationActivityMapper = organizationActivityMapper;
        this.lostFoundMapper = lostFoundMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    /**
     * 和 AI 对话的入口方法（RAG 版本）
     * 
     * 调用流程：
     * ① searchRelevantData(userMessage) → 搜索数据库
     * ② buildRequestBody(userMessage, context) → 把搜索结果注入 prompt
     * ③ 发送请求给大模型 API → 拿到回答
     */
    public String chat(String userMessage) {
        try {
            // ① 检索阶段：搜索数据库中与问题相关的数据
            String context = searchRelevantData(userMessage);

            // ② 构建请求体（把 context 注入 system prompt）
            String requestBody = buildRequestBody(userMessage, context);

            // ③ 发送请求给大模型
            HttpRequest request = HttpRequest.newBuilder()
                    // 请求地址：拼成 https://api.deepseek.com/v1/chat/completions
                    .uri(URI.create(properties.getBaseUrl() + "/chat/completions"))
                    // 告诉服务器"我发的是 JSON 格式的数据"
                    .header("Content-Type", "application/json")
                    // 身份验证：Bearer Token 方式，相当于"我是付费用户，请让我用你的 API"
                    // Bearer 后面跟你注册 DeepSeek 时拿到的 API Key
                    .header("Authorization", "Bearer " + properties.getApiKey())
                    // 最多等 30 秒，因为大模型生成回答需要时间（不像普通接口秒回）
                    .timeout(Duration.ofSeconds(30))
                    // 指定 POST 方法，并把构造好的 JSON 字符串作为请求体发送
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("AI API 返回错误: HTTP {} body={}", response.statusCode(), response.body());
                return "抱歉，AI 服务异常(HTTP " + response.statusCode() + ")，请稍后再试~";
            }

            // ④ 解析响应
            JsonNode root = objectMapper.readTree(response.body());
            JsonNode choices = root.get("choices");
            if (choices == null || choices.isEmpty()) {
                log.error("AI API 返回空 choices: {}", response.body());
                return "抱歉，AI 服务返回为空，请稍后再试~";
            }
            JsonNode message = choices.get(0).get("message");
            String answer = message.get("content").asText();
            log.info("AI 回答: {}", answer.substring(0, Math.min(50, answer.length())));
            return answer;

        } catch (Exception e) {
            log.error("AI 调用异常", e);
            String detail = e.getMessage() != null ? e.getMessage() : "未知错误";
            if (detail.length() > 100) detail = detail.substring(0, 100) + "...";
            return "抱歉，小墙出错了: " + detail;
        }
    }

    /**
     * RAG 核心：搜索数据库中的相关数据
     * 
     * 用用户的问题作为关键词，在三个表中搜索：
     * - wall_post（帖子）：标题 + 内容匹配
     * - organization_activity（组局）：标题 + 分类 + 描述匹配
     * - lost_found_item（失物招领）：标题 + 描述 + 地点匹配
     * 
     * 关键优化：先把用户问题拆成关键词再搜，而不是整句匹配。
     * 例如 "今晚有什么活动？" → ["今晚", "活动"] → 能匹配到 "今晚五排开黑"
     * 每条最多取 5 条，组装成文本返回，供 AI 参考。
     */
    private String searchRelevantData(String userMessage) {
        // 提取关键词：去掉语气词，拆成有效词组
        List<String> keywords = extractKeywords(userMessage);
        log.info("RAG 关键词: {}", keywords);

        StringBuilder sb = new StringBuilder();
        sb.append("当前校园动态数据：\n");

        try {
            // 1. 搜索帖子（多关键词 OR 匹配）
            List<WallPost> posts = wallPostMapper.selectByKeywords(keywords, 5);
            if (posts != null && !posts.isEmpty()) {
                sb.append("\n【相关帖子】\n");
                int count = 0;
                for (WallPost post : posts) {
                    if (count >= 5) break;
                    String content = post.getContent();
                    if (content != null && content.length() > 80) {
                        content = content.substring(0, 80) + "...";
                    }
                    sb.append("- ").append(content != null ? content : "无内容")
                            .append("（").append(post.getViewCount()).append("次浏览，")
                            .append(post.getLikeCount()).append("赞）\n");
                    count++;
                }
            }
        } catch (Exception e) {
            log.warn("搜索帖子失败: {}", e.getMessage());
        }

        try {
            // 2. 搜索组局活动（多关键词 OR 匹配）
            List<OrganizationActivity> activities = organizationActivityMapper.selectByKeywords(keywords, 5);
            if (activities != null && !activities.isEmpty()) {
                sb.append("\n【相关组局活动】\n");
                for (OrganizationActivity act : activities) {
                    sb.append("- ").append(act.getTitle())
                            .append(" | 分类：").append(act.getCategory())
                            .append(" | 时间：").append(act.getActivityTime())
                            .append(" | 地点：").append(act.getAddress())
                            .append(" | 人数：").append(act.getJoinNum()).append("/").append(act.getMaxPeople())
                            .append(" | 费用：").append(act.getFeeType());
                    if (act.getHostDesc() != null && !act.getHostDesc().isEmpty()) {
                        sb.append(" | 描述：").append(act.getHostDesc());
                    }
                    sb.append("\n");
                }
            }
        } catch (Exception e) {
            log.warn("搜索组局失败: {}", e.getMessage());
        }

        try {
            // 3. 搜索失物招领（多关键词 OR 匹配）
            List<LostFoundItem> items = lostFoundMapper.selectByKeywords(keywords, 5);
            if (items != null && !items.isEmpty()) {
                sb.append("\n【相关失物招领】\n");
                for (LostFoundItem item : items) {
                    sb.append("- [").append("lost".equals(item.getType()) ? "寻物" : "招领")
                            .append("] ").append(item.getTitle())
                            .append(" | 地点：").append(item.getLocation() != null ? item.getLocation() : "未知");
                    if (item.getDescription() != null && !item.getDescription().isEmpty()) {
                        String desc = item.getDescription();
                        if (desc.length() > 50) desc = desc.substring(0, 50) + "...";
                        sb.append(" | 描述：").append(desc);
                    }
                    sb.append(" | 状态：").append("resolved".equals(item.getStatus()) ? "已解决" : "进行中");
                    sb.append("\n");
                }
            }
        } catch (Exception e) {
            log.warn("搜索失物招领失败: {}", e.getMessage());
        }

        String context = sb.toString();
        log.info("RAG 检索结果长度: {} 字符", context.length());
        return context;
    }

    /**
     * 中文关键词提取（简易版，无需 NLP 库）
     * 
     * 原理：把用户输入的自然语言问题拆成有效关键词。
     * 
     * 处理步骤：
     * ① 去掉标点符号和语气词（的、了、吗、呢、什么、怎么、有、是、我、你、？、！等）
     * ② 按 2 字一组切成 bigram 词组（中文常用词以 2 字为主）
     * ③ 保留原始短文本（≤4字）作为整体关键词
     * 
     * 示例：
     *   "今晚有什么活动？" → ["今晚", "活动"]
     *   "图书馆几点关门" → ["图书", "书馆", "几点", "关门", "图书馆"]
     *   "有人丢东西吗" → ["丢东西"]
     */
    private List<String> extractKeywords(String message) {
        // 去掉标点和语气词
        String cleaned = message.replaceAll("[？?！!，,。.、\\s]+", "")
                .replaceAll("(的|了|吗|呢|什么|怎么|有|有没有|是|我|你|你们|他|她|在|吗|啊|吧|哦|嗯|哈|呀|啦|嘛|呗|么)",
                        " ");

        List<String> keywords = new ArrayList<>();

        // 如果清理后还是短文本，直接作为整体关键词
        if (cleaned.replace(" ", "").length() <= 4) {
            if (!cleaned.replace(" ", "").isEmpty()) {
                keywords.add(cleaned.replace(" ", "").trim());
            }
        }

        // 按空格切分，提取 2 字以上的词组
        String[] parts = cleaned.split("\\s+");
        for (String part : parts) {
            String trimmed = part.trim();
            if (trimmed.isEmpty() || trimmed.length() < 2) continue;
            // 整体加入
            if (!keywords.contains(trimmed)) {
                keywords.add(trimmed);
            }
            // 2 字一组切分（bigram）
            for (int i = 0; i <= trimmed.length() - 2; i++) {
                String bigram = trimmed.substring(i, i + 2);
                if (!keywords.contains(bigram)) {
                    keywords.add(bigram);
                }
            }
        }

        // 兜底：至少有一个关键词
        if (keywords.isEmpty()) {
            keywords.add(message.replaceAll("[？?！!，,。.、\\s]+", ""));
        }

        // 去重，限制数量（避免 SQL 太长）
        return keywords.size() > 10 ? keywords.subList(0, 10) : keywords;
    }

    /**
     * 构造符合 OpenAI chat 接口规范的请求体
     * @param userMessage 用户问题
     * @param context RAG 检索到的实时数据
     * @return JSON 字符串
     */
    private String buildRequestBody(String userMessage, String context) {
        try {
            // 把静态知识库 + 动态检索数据合并成 system prompt
            String fullSystemPrompt = SYSTEM_PROMPT + "\n\n" + context;

            Map<String, Object> body = Map.of(
                    "model", properties.getModel(),
                    "messages", List.of(
                            Map.of("role", "system", "content", fullSystemPrompt),
                            Map.of("role", "user", "content", userMessage)
                    ),
                    "temperature", 0.7,
                    "max_tokens", 500
            );
            return objectMapper.writeValueAsString(body);
        } catch (Exception e) {
            throw new RuntimeException("Failed to build request body", e);
        }
    }
}