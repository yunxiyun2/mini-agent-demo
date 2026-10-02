package com.dyx;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
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
import java.util.Map;
import java.util.stream.Stream;

public class MiniOpenAIClient {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();

    public static class Req{
        public String model;
        public List<Map<String, String>> messages;
        public boolean stream;
        public Req(String model, List<Map<String, String>> messages) {
            this.model = model;
            this.messages = messages;
        }
    }

    public static class Chunk{
        public String id;
        public List<Choice> choices;
        public static class Choice{
            public Map<String, Object> delta;
            public String finish_reason;       }
    }

    public String chat(String baseUrl, String apiKey, String model,
                       List<Map<String, String>> messages){
        Req req = new Req(model, messages);
        req.stream = false;
        try {
            String body = objectMapper.writeValueAsString(req);
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
            return resp.body();

        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Stream<String> chatStream(String baseUrl, String apiKey, String model,
                                     List<Map<String, String>> messages){
        Req req = new Req(model, messages);
        req.stream = true;
        try {
            String body = objectMapper.writeValueAsString(req);
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
    private String extractDelta(String data){
        try{
            JsonNode root = objectMapper.readTree(data);
            JsonNode delta = root.path("choices").path(0).path("delta");
            // 先看 content，再看 reasoning_content
            String content = delta.path("content").asText("");
            if (!content.isEmpty()) return "[content]" + content;
            String reasoning = delta.path("reasoning_content").asText("");
            if (!reasoning.isEmpty()) return "[思考] " + reasoning;
            return "";
        } catch (JsonProcessingException e) {
            return "";
        }
    }

    public static void main(String[] args){
        MiniOpenAIClient miniOpenAIClient = new MiniOpenAIClient();
        List<Map<String, String>> messages = List.of(
                Map.of("role", "system", "content", "你是助手"),
                Map.of("role", "user", "content", "用一句话介绍杭州")
        );
        String baseUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1";
        //String baseUrl = "https://open.bigmodel.cn/api/paas/v4";
        String apiKey = System.getenv("ZHIPU_API_KEY");
        System.out.println(miniOpenAIClient.chat(baseUrl, apiKey, "glm-5.2", messages));

        miniOpenAIClient.chatStream(baseUrl, apiKey, "glm-5.2", messages)
                .forEach(System.out::println);

    }

}
