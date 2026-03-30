package com.guo.guoaiagentbackend.tools;

import org.springframework.ai.tool.annotation.Tool;

public class TerminateTool {
  
    @Tool(description = """
            当用户需求已满足，或当前任务无法继续推进时，调用本工具结束对话。
            所有任务完成后必须调用本工具收尾。
            """)  
    public String doTerminate() {  
        return "任务结束";  
    }  
}
