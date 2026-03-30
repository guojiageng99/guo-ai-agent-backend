package com.guo.guoaiagentbackend.agent;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.guo.guoaiagentbackend.agent.model.AgentState;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 处理工具调用的基础代理类，具体实现了 think 和 act 方法，可以用作创建实例的父类  
 */  
@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public class ToolCallAgent extends ReActAgent {  
  
    // 可用的工具  
    private final ToolCallback[] availableTools;
  
    // 保存了工具调用信息的响应  
    private ChatResponse toolCallChatResponse;
  
    // 工具调用管理者  
    private final ToolCallingManager toolCallingManager;
  
    // 禁用内置的工具调用机制，自己维护上下文  
    private final ChatOptions chatOptions;

    public ToolCallAgent(ToolCallback[] availableTools) {
        super();
        this.availableTools = availableTools;
        this.toolCallingManager = ToolCallingManager.builder().build();
        // 禁用 Spring AI 内置的工具调用机制，自己维护选项和消息上下文
        this.chatOptions = DashScopeChatOptions.builder()
                .withInternalToolExecutionEnabled(false)
                .build();
    }


    /**
     * 处理当前状态并决定下一步行动
     *
     * @return 是否需要执行行动
     */
    @Override
    public boolean think() {
        if (getNextStepPrompt() != null && !getNextStepPrompt().isEmpty()) {
            UserMessage userMessage = new UserMessage(getNextStepPrompt());
            getMessageList().add(userMessage);
        }
        List<Message> messageList = getMessageList();
        Prompt prompt = new Prompt(messageList, chatOptions);
        try {
            // 获取带工具选项的响应
            ChatResponse chatResponse = getChatClient().prompt(prompt)
                    .system(getSystemPrompt())
                    .toolCallbacks(availableTools)
                    .call()
                    .chatResponse();
            // 记录响应，用于 Act
            this.toolCallChatResponse = chatResponse;
            AssistantMessage assistantMessage = chatResponse.getResult().getOutput();
            // 输出提示信息
            String result = assistantMessage.getText();
            List<AssistantMessage.ToolCall> toolCallList = assistantMessage.getToolCalls();
            log.info(getName() + "的思考: " + result);
            log.info(getName() + "选择了 " + toolCallList.size() + " 个工具来使用");
            String toolCallInfo = toolCallList.stream()
                    .map(toolCall -> String.format("工具名称：%s，参数：%s",
                            toolCall.name(),
                            toolCall.arguments())
                    )
                    .collect(Collectors.joining("\n"));
            log.info(toolCallInfo);
            if (toolCallList.isEmpty()) {
                // 只有不调用工具时，才记录助手消息
                getMessageList().add(assistantMessage);
                return false;
            } else {
                // 需要调用工具时，无需记录助手消息，因为调用工具时会自动记录
                return true;
            }
        } catch (Exception e) {
            log.error(getName() + "的思考过程遇到了问题: " + e.getMessage());
            getMessageList().add(
                    new AssistantMessage("处理时遇到错误: " + e.getMessage()));
            return false;
        }
    }


    /**
     * 执行工具调用并处理结果
     *
     * @return 执行结果
     */
    @Override
    public String act() {
        if (!toolCallChatResponse.hasToolCalls()) {
            return "没有工具调用";
        }
        // 调用工具
        Prompt prompt = new Prompt(getMessageList(), chatOptions);
        ToolExecutionResult toolExecutionResult = toolCallingManager.executeToolCalls(prompt, toolCallChatResponse);
        // 记录消息上下文，conversationHistory 已经包含了助手消息和工具调用返回的结果
        setMessageList(toolExecutionResult.conversationHistory());
        // 当前工具调用的结果
        ToolResponseMessage toolResponseMessage = (ToolResponseMessage) CollUtil.getLast(toolExecutionResult.conversationHistory());
        String results = toolResponseMessage.getResponses().stream()
                .map(response -> "工具 " + response.name() + " 完成了它的任务！结果: " + response.responseData())
                .collect(Collectors.joining("\n"));
// 判断是否调用了终止工具
        boolean terminateToolCalled = toolResponseMessage.getResponses().stream()
                .anyMatch(response -> "doTerminate".equals(response.name()));
        if (terminateToolCalled) {
            setState(AgentState.FINISHED);
        }
        log.info(results);
        return results;

    }

    /**
     * Manus 等工具型智能体：不向用户流式输出工具原始返回（JSON、路径等），只输出模型「不再调用工具」时的自然语言回复。
     * 工具过程仅写日志，便于排障。
     */
    @Override
    public SseEmitter runStream(String userPrompt) {
        SseEmitter emitter = new SseEmitter(300_000L);

        CompletableFuture.runAsync(() -> {
            try {
                if (this.getState() != AgentState.IDLE) {
                    emitter.send("错误：无法从状态运行代理: " + this.getState());
                    emitter.complete();
                    return;
                }
                if (userPrompt == null || userPrompt.isBlank()) {
                    emitter.send("错误：不能使用空提示词运行代理");
                    emitter.complete();
                    return;
                }

                this.setState(AgentState.RUNNING);
                this.getMessageList().add(new UserMessage(userPrompt));

                boolean streamedUserFacing = false;
                String lastStreamedNormalized = null;
                try {
                    for (int i = 0; i < this.getMaxSteps() && this.getState() != AgentState.FINISHED; i++) {
                        int stepNumber = i + 1;
                        this.setCurrentStep(stepNumber);
                        log.info("Executing step {}/{}", stepNumber, this.getMaxSteps());

                        boolean shouldAct = think();
                        if (shouldAct) {
                            act();
                        } else {
                            String visible = this.extractLatestAssistantTextForUser();
                            if (visible != null && !visible.isBlank()) {
                                String norm = normalizeForDedupe(visible);
                                if (lastStreamedNormalized != null && norm.equals(lastStreamedNormalized)) {
                                    log.debug("Skip duplicate user-facing reply (same as previous chunk)");
                                } else {
                                    emitter.send(visible);
                                    lastStreamedNormalized = norm;
                                    streamedUserFacing = true;
                                }
                            }
                        }
                    }

                    if (this.getCurrentStep() >= this.getMaxSteps() && this.getState() != AgentState.FINISHED) {
                        this.setState(AgentState.FINISHED);
                        log.warn("Agent reached max steps ({}) without terminate; not showing limit message to user",
                                this.getMaxSteps());
                    }

                    if (!streamedUserFacing) {
                        String fallback = this.extractLatestAssistantTextForUser();
                        if (fallback != null && !fallback.isBlank()) {
                            emitter.send(fallback);
                        } else {
                            emitter.send("任务已结束。如需下载生成的文件，请在消息中查找「下载 PDF」按钮或说明你的下一步需求。");
                        }
                    }

                    emitter.complete();
                } catch (Exception e) {
                    this.setState(AgentState.ERROR);
                    log.error("执行智能体失败", e);
                    try {
                        emitter.send("执行错误: " + e.getMessage());
                        emitter.complete();
                    } catch (Exception ex) {
                        emitter.completeWithError(ex);
                    }
                } finally {
                    this.cleanup();
                }
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });

        emitter.onTimeout(() -> {
            this.setState(AgentState.ERROR);
            this.cleanup();
            log.warn("SSE connection timed out");
        });

        emitter.onCompletion(() -> {
            if (this.getState() == AgentState.RUNNING) {
                this.setState(AgentState.FINISHED);
            }
            this.cleanup();
            log.info("SSE connection completed");
        });

        return emitter;
    }

    private String extractLatestAssistantTextForUser() {
        List<Message> list = getMessageList();
        for (int i = list.size() - 1; i >= 0; i--) {
            Message m = list.get(i);
            if (m instanceof AssistantMessage am) {
                String t = am.getText();
                if (t != null && !t.isBlank()) {
                    return t;
                }
            }
        }
        return null;
    }

    private static String normalizeForDedupe(String s) {
        if (s == null) {
            return "";
        }
        return s.replaceAll("\\s+", " ").trim();
    }

}
