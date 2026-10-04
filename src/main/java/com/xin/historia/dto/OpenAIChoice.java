package com.xin.historia.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** 一个 choice，包含 message（非流式）或 delta（流式）。 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenAIChoice {
    private Integer index;

    /** 非流式响应的完整消息。 */
    private OpenAIMessage message;

    /** 流式响应的增量消息。 */
    private OpenAIMessage delta;

    @JsonProperty("finish_reason")
    private String finishReason;

    public Integer getIndex() { return index; }
    public void setIndex(Integer index) { this.index = index; }
    public OpenAIMessage getMessage() { return message; }
    public void setMessage(OpenAIMessage message) { this.message = message; }
    public OpenAIMessage getDelta() { return delta; }
    public void setDelta(OpenAIMessage delta) { this.delta = delta; }
    public String getFinishReason() { return finishReason; }
    public void setFinishReason(String finishReason) { this.finishReason = finishReason; }

    /**
     * 取有效 message
     *   - 非流式：返回 message
     *   - 流式：返回 delta
     */
    public OpenAIMessage getEffectiveMessage() {
        return message != null ? message : delta;
    }
}