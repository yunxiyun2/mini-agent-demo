package com.dyx.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/** OpenAI 响应体。流式 chunk 和非流式响应共用一个类型。 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenAIResponse {
    private String id;
    private String model;
    private List<OpenAIChoice> choices;

    @JsonProperty("usage")
    private OpenAIUsage usage;

    @JsonProperty("error")
    private OpenAIError error;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public List<OpenAIChoice> getChoices() { return choices; }
    public void setChoices(List<OpenAIChoice> choices) { this.choices = choices; }
    public OpenAIUsage getUsage() { return usage; }
    public void setUsage(OpenAIUsage usage) { this.usage = usage; }
    public OpenAIError getError() { return error; }
    public void setError(OpenAIError error) { this.error = error; }

    /** 取第一个 choice */
    public OpenAIChoice getFirstChoice() {
        return choices == null || choices.isEmpty() ? null : choices.get(0);
    }

    /** 是否是错误响应。 */
    public boolean isError() { return error != null; }

    /** 是否是流式 chunk（有 choices 且 delta 非空）。 */
    public boolean isChunk() {
        OpenAIChoice c = getFirstChoice();
        return c != null && c.getDelta() != null;
    }
}