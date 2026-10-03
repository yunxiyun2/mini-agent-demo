package com.dyx.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenAIError {
    private String message;
    private String type;
    private String code;

    public String getMessage() { return message; }
    public void setMessage(String m) { this.message = m; }
    public String getType() { return type; }
    public void setType(String t) { this.type = t; }
    public String getCode() { return code; }
    public void setCode(String c) { this.code = c; }
}