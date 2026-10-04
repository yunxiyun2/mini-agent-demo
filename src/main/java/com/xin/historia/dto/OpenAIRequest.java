package com.xin.historia.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/** OpenAI Chat Completions 请求体。字段名和 OpenAI JSON 对齐。 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OpenAIRequest {
    private String model;
    private List<OpenAIMessage> messages;
    private boolean stream;
    private Double temperature;
    private Integer maxTokens;

    @JsonProperty("stream_options")
    private Map<String, Object> streamOptions;

    // getters / setters
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public List<OpenAIMessage> getMessages() { return messages; }
    public void setMessages(List<OpenAIMessage> messages) { this.messages = messages; }
    public boolean isStream() { return stream; }
    public void setStream(boolean stream) { this.stream = stream; }
    public Double getTemperature() { return temperature; }
    public void setTemperature(Double temperature) { this.temperature = temperature; }

    @JsonProperty("max_tokens")
    public Integer getMaxTokens() { return maxTokens; }          // ★ getter 上的注解也能用
    @JsonProperty("max_tokens")
    public void setMaxTokens(Integer maxTokens) { this.maxTokens = maxTokens; }

    public Map<String, Object> getStreamOptions() { return streamOptions; }
    public void setStreamOptions(Map<String, Object> streamOptions) { this.streamOptions = streamOptions; }
}
