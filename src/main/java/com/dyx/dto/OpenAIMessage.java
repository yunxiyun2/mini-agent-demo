package com.dyx.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 一条消息。同时用于：
 *   - request.messages[]（你发给模型的）
 *   - response.choices[0].message（非流式响应）
 *   - response.choices[0].delta（流式 chunk）
 *
 * ★ 关键：reasoning_content 字段同时认两个名字
 *   - DeepSeek / DashScope / GLM 用 reasoning_content
 *   - vLLM 部署用 reasoning
 *   @JsonAlias 让反序列化时两个字段名都映射到 reasoningContent
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenAIMessage {
    private String role;
    private String content;

    @JsonProperty("reasoning_content")
    @JsonAlias("reasoning")
    private String reasoningContent;

    @JsonProperty("tool_calls")
    private Object toolCalls;             // 阶段后期再细化成 List<OpenAIToolCall>

    public OpenAIMessage() {}

    public OpenAIMessage(String role, String content) {
        this.role = role;
        this.content = content;
    }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getReasoningContent() { return reasoningContent; }
    public void setReasoningContent(String reasoningContent) { this.reasoningContent = reasoningContent; }
    public Object getToolCalls() { return toolCalls; }
    public void setToolCalls(Object toolCalls) { this.toolCalls = toolCalls; }
}