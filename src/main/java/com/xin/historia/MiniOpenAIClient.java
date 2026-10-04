package com.xin.historia;

import com.xin.historia.dto.OpenAIMessage;
import com.xin.historia.dto.OpenAIRequest;
import com.xin.historia.dto.OpenAIResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.stream.Stream;

public class MiniOpenAIClient {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();

    public OpenAIResponse chat(String baseUrl, String apiKey, OpenAIRequest openAIRequest) {

        openAIRequest.setStream(false);
        try {
            String body = objectMapper.writeValueAsString(openAIRequest);
            HttpRequest httpReq = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/chat/completions"))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            HttpResponse<String> resp = httpClient.send(httpReq,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            if(resp.statusCode() >= 400){
                throw new RuntimeException("HTTP " + resp.statusCode() + ": " + resp.body());
            }

            return objectMapper.readValue(resp.body(), OpenAIResponse.class);// 反序列化

        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Stream<OpenAIResponse> chatStream(String baseUrl, String apiKey, OpenAIRequest openAIRequest) {
        openAIRequest.setStream(true);
        try {
            String body = objectMapper.writeValueAsString(openAIRequest);
            HttpRequest httpReq = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/chat/completions"))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .header("Accept", "text/event-stream")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            HttpResponse<InputStream> resp = httpClient.send(httpReq,
                    HttpResponse.BodyHandlers.ofInputStream());

            if(resp.statusCode() >= 400){
                String errBody = new String(resp.body().readAllBytes(), StandardCharsets.UTF_8);
                throw new RuntimeException("HTTP " + resp.statusCode() + ": " + errBody);
            }

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resp.body(), StandardCharsets.UTF_8)
            );

            return reader.lines()
                    //.peek(line -> System.err.println("[RAW] " + line))
                    .filter(line -> line.startsWith("data:"))
                    .map(line -> line.substring(5).trim())
                    .takeWhile(data -> !"[DONE]".equals(data))
                    .filter(data -> !data.isEmpty())
                    .map(this::extractDelta);

        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
    }
    private OpenAIResponse extractDelta(String data){
        try{
            return objectMapper.readValue(data, OpenAIResponse.class);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    public static void main(String[] args){
        MiniOpenAIClient miniOpenAIClient = new MiniOpenAIClient();
        OpenAIRequest openAIRequest = new OpenAIRequest();
        openAIRequest.setModel("glm-5.2");
        openAIRequest.setTemperature(0.7);
        openAIRequest.setMessages(List.of(
                new OpenAIMessage("system","你是助手"),
                new OpenAIMessage("user","用一句话介绍杭州")
        ));

        String baseUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1";
        //String baseUrl = "https://open.bigmodel.cn/api/paas/v4";
        String apiKey = System.getenv("ZHIPU_API_KEY");

        OpenAIResponse chatResp = miniOpenAIClient.chat(baseUrl, apiKey, openAIRequest);
        System.out.println("[Sync] " + chatResp.getFirstChoice().getMessage().getContent());
        System.out.println("[USAGE] total=" + chatResp.getUsage().getTotalTokens()
                + ", reasoning=" + chatResp.getUsage().getCompletionTokensDetails().getReasoningTokens());


        miniOpenAIClient.chatStream(baseUrl, apiKey, openAIRequest)
                .filter(r -> r!=null && r.getFirstChoice()!=null)
                .map(r -> {
                    OpenAIMessage delta = r.getFirstChoice().getDelta();
                    String content = delta.getContent();
                    if(content!=null && !content.isEmpty())return "[输出] " + content;
                    String reasoning = delta.getReasoningContent();
                    if(reasoning!=null && !reasoning.isEmpty())return "[思考] " + reasoning;
                    return "";
                })
                .filter(s -> !s.isEmpty())
                .forEach(s -> System.out.println(s));

    }

}
