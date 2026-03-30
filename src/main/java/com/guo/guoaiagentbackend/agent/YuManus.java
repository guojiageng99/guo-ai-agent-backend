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
                调用 generatePDF 成功后，向用户复述返回信息中的下载路径（含 /api/files/pdf/），以便界面出现下载按钮。
                """;  
        this.setSystemPrompt(SYSTEM_PROMPT);  
        String NEXT_STEP_PROMPT = """
                根据用户需求，主动选择最合适的工具或工具组合；复杂任务拆解步骤，逐步调用工具完成。
                【对用户可见内容】用户界面只会展示你「不调用工具」时的自然语言回复；工具返回的 JSON、原始网页、
                服务端路径等不会展示给用户。因此：不要指望用户看到工具原文；每次工具执行后，你应内化结果再决策。
                在调用 `doTerminate` 之前，须先用一段话向用户说明：完成了什么、关键产物（例如 PDF 下载路径），
                语言简洁，禁止粘贴工具返回的全文或大段 JSON。
                若已向用户说明完毕且无需再调用任何工具（例如 PDF 已生成并告知下载方式），下一轮必须立即调用 `doTerminate`，
                禁止用相同或几乎相同的话再次总结，禁止无意义循环。
                任务无法继续推进时，也应调用 `doTerminate` 结束流程。
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
