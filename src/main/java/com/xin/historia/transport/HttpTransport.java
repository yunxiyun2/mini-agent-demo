package com.xin.historia.transport;

import reactor.core.publisher.Flux;

import java.io.IOException;

/**
 * HTTP 传输抽象。
 *
 * ★ 关键设计：stream() 返回的 Flux<String> 已经是 SSE 切帧后的 data payload
 *   —— "data:" 前缀已剥掉，空行/[DONE] 已过滤。
 *   上层（OpenAIClient）拿到的就是干净的 JSON 字符串，直接反序列化。
 */
public interface HttpTransport {

    /**
     * 同步请求
     * @param request
     * @return
     * @throws IOException
     */
    HttpResponse execute(HttpRequest request) throws IOException;

    /**
     * 流式请求
     * 每个String是一帧SSE的data内容（剥离"data:"前缀，已过滤空行和[DONE]）
     * @param request
     * @return SSE data payload流
     */
    Flux<String> stream(HttpRequest request);
}
