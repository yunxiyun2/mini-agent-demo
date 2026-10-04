package com.xin.historia.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenAIUsage {
    @JsonProperty("prompt_tokens")
    private int promptTokens;

    @JsonProperty("completion_tokens")
    private int completionTokens;

    @JsonProperty("total_tokens")
    private int totalTokens;

    @JsonProperty("completion_tokens_details")
    private CompletionTokensDetails completionTokensDetails;

    public int getPromptTokens() { return promptTokens; }
    public void setPromptTokens(int v) { this.promptTokens = v; }
    public int getCompletionTokens() { return completionTokens; }
    public void setCompletionTokens(int v) { this.completionTokens = v; }
    public int getTotalTokens() { return totalTokens; }
    public void setTotalTokens(int v) { this.totalTokens = v; }
    public CompletionTokensDetails getCompletionTokensDetails() { return completionTokensDetails; }
    public void setCompletionTokensDetails(CompletionTokensDetails d) { this.completionTokensDetails = d; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CompletionTokensDetails {
        @JsonProperty("reasoning_tokens")
        private Integer reasoningTokens;
        public Integer getReasoningTokens() { return reasoningTokens; }
        public void setReasoningTokens(Integer v) { this.reasoningTokens = v; }
    }
}