package com.guo.guoaiagentbackend.app;

import com.guo.guoaiagentbackend.advisor.MyLoggerAdvisor;
import com.guo.guoaiagentbackend.advisor.ReReadingAdvisor;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.mcp.SyncMcpToolCallback;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;


@Component
@Slf4j
public class LoveApp {

//    private static final Logger log = LoggerFactory.getLogger(LoveApp.class);
    @PostConstruct
    public void checkMcpCallback() {
        log.info("✅ toolCallbackProvider 是否为 null：{}", toolCallbackProvider == null);
        if (toolCallbackProvider != null) {
            log.info("✅ toolCallbackProvider 类型：{}", toolCallbackProvider.getClass().getName());
        }
    }
    private final ChatClient chatClient;

    private static final String SYSTEM_PROMPT = """
            你是产品「恋语」（恋语 AI）中的恋爱与情感顾问，语气专业、温和、可信赖。需要称呼自己时，用「恋语」即可，勿虚构「某老师」、姓氏或固定人名。
            不要编造从业年限、服务人数等具体数字履历，除非用户主动问起，再用一句话概括即可。
            仅在整场对话的首次回复中，可用一两句欢迎，并点明你是恋语、可倾诉情感问题；从用户第二次提问起，禁止重复自我介绍、禁止再次铺垫身份资历，直接理解问题、给出分析与建议，必要时简短追问细节。
            围绕单身、恋爱、已婚三种状态展开：单身可问社交圈与追求心仪对象的困扰；恋爱可问沟通与习惯差异矛盾；已婚可问家庭责任与亲属关系。
            引导用户详述经过、对方反应与自身想法，以便给出可执行的建议。""";

//    private static final String SYSTEM_PROMPT = "你可以调用本地的guo-image-search-mcp-server工具完成图片搜索任务，调用工具后直接返回工具返回的图片链接列表，无需额外的文字解释。";
    public LoveApp(ChatModel dashscopeChatModel, ChatMemory chatMemory) {
//        // 初始化基于内存的对话记忆
//        ChatMemory chatMemory = new InMemoryChatMemory();
        /*// 初始化基于内存的对话记忆
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                .maxMessages(20)
                .build();*/

        chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build()
                        )
                .build();

        /*chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(chatMemory),
                        // 自定义日志 Advisor，可按需开启
                        new MyLoggerAdvisor()
                        // 自定义推理增强 Advisor，可按需开启
//                        new ReReadingAdvisor()
                        )
                .build();
*/

    }


    public String doChat(String message, String chatId) {
        ChatResponse response = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
//                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
//                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .call()
                .chatResponse();
        String content = response.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

    record LoveReport(String title, List<String> suggestions) {
    }

    public LoveReport doChatWithReport(String message, String chatId) {
        LoveReport loveReport = chatClient
                .prompt()
                .system(SYSTEM_PROMPT + "每次对话后都要生成恋爱结果，标题为{用户名}的恋爱报告，内容为建议列表")
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
//                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
//                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .call()
                .entity(LoveReport.class);
        log.info("loveReport: {}", loveReport);
        return loveReport;
    }


    @Resource
    private VectorStore loveAppVectorStore;

    public String doChatWithRag(String message, String chatId) {
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
//                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
//                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                // 开启日志，便于观察效果
                .advisors(new MyLoggerAdvisor())
                // 应用知识库问答
                .advisors(new QuestionAnswerAdvisor(loveAppVectorStore))
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

    private static final String PARTNER_RECOMMEND_SYSTEM_SUFFIX = """
            
            【对象推荐任务】知识库中另有「候选人档案」类文档（含年龄、星座、职业、性格、期望对象等）。
            当用户咨询择偶、交友、想认识怎样的人、谁能和我合得来等问题时：
            1）结合检索到的档案片段，推荐 1～3 位最匹配的人选（用资料里的称呼即可）；
            2）每人简要说明匹配理由（对应用户提到的点）；
            3）若档案中无合适人选，请诚实说明，并可给出泛化建议，勿编造库中不存在的人。
            语气亲切、负责；勿重复自我介绍或堆砌资历。""";

    /**
     * RAG 扩展：根据用户问题，从本地向量库中的「恋爱对象资料」里检索并推荐潜在匹配对象。
     * 资料文件：{@code classpath:document/恋爱对象推荐资料库.md}（与其它 md 一并加载进 {@link #loveAppVectorStore}）。
     */
    public String doChatRecommendPartner(String message, String chatId) {
        ChatResponse chatResponse = chatClient
                .prompt()
                .system(SYSTEM_PROMPT + PARTNER_RECOMMEND_SYSTEM_SUFFIX)
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .advisors(new MyLoggerAdvisor())
                .advisors(new QuestionAnswerAdvisor(loveAppVectorStore))
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("recommendPartner content: {}", content);
        return content;
    }


    @Resource
    private Advisor loveAppRagCloudAdvisor;

    public String doChatWithRag1(String message, String chatId) {
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
//                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
//                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                // 开启日志，便于观察效果
                .advisors(new MyLoggerAdvisor())
                // 应用增强检索服务（云知识库服务）
                .advisors(loveAppRagCloudAdvisor)
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }


    @Resource
    private ToolCallback[] allTools;

    public String doChatWithTools(String message, String chatId) {
        ChatResponse response = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
//                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
//                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                // 开启日志，便于观察效果
                .advisors(new MyLoggerAdvisor())
                .toolCallbacks(toolCallbackProvider)
                .call()
                .chatResponse();
        String content = response.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }


    @Resource
    private ToolCallbackProvider toolCallbackProvider;

    public String doChatWithMcp(String message, String chatId) {
        ChatResponse response = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
//                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
//                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                // 开启日志，便于观察效果
                .advisors(new MyLoggerAdvisor())
                .toolCallbacks(toolCallbackProvider)
//                .tools(toolCallbackProvider)
                .call()
                .chatResponse();
        String content = response.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }


  


    public Flux<String> doChatByStream(String message, String chatId) {
        return chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
//                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
//                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .stream()
                .content();
    }

}
