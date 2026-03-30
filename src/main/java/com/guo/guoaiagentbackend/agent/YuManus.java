package com.guo.guoaiagentbackend.agent;

import com.guo.guoaiagentbackend.advisor.MyLoggerAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Component;

@Component
public class YuManus extends ToolCallAgent {  
  
    public YuManus(ToolCallback[] allTools, ChatModel dashscopeChatModel) {
        super(allTools);  
        this.setName("yuManus");  
        String SYSTEM_PROMPT = """
                你是 YuManus，全能型 AI 助手，负责完成用户提出的各类任务。
                你拥有多种可调用的工具，请按需组合使用以高效完成任务。

                【语言】除非用户明确要求使用其他语言，否则你的一切输出（含思考说明、对用户可见的总结、
                写入文件/PDF 的正文、工具参数中的说明性文字等）一律使用简体中文。
                生成 PDF、Markdown、计划书、列表等内容时，正文必须为简体中文。
                """;  
        this.setSystemPrompt(SYSTEM_PROMPT);  
        String NEXT_STEP_PROMPT = """  
                根据用户需求，主动选择最合适的工具或工具组合。
                复杂任务请拆解步骤，逐步调用工具完成。
                每次使用工具后，用简体中文说明执行结果与下一步建议。
                任务已完成或无法继续推进时，调用 `terminate` 工具结束流程。
                """;  
        this.setNextStepPrompt(NEXT_STEP_PROMPT);  
        this.setMaxSteps(20);  
        // 初始化客户端  
        ChatClient chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultAdvisors(new MyLoggerAdvisor())
                .build();  
        this.setChatClient(chatClient);  
    }  
}
