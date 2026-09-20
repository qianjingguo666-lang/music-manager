package com.qianjingguo.musicmanager.service;

import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.util.Base64;
import java.util.concurrent.TimeUnit;

@Service
public class AiRecognitionService {

    @Value("${deepseek.api.key}")
    private String apiKey;

    @Value("${deepseek.api.url}")
    private String apiUrl;

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();

    private final ObjectMapper mapper = new ObjectMapper();

    public String recognizeCover(MultipartFile file) throws Exception {
        byte[] imageBytes = file.getBytes();
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);
        String mimeType = file.getContentType();

        String prompt = "这是一张音乐专辑封面图片，请识别出歌手名和专辑名，"
                + "严格按照以下JSON格式返回，不要输出多余文字：{\"artist\":\"歌手名\",\"album\":\"专辑名\"}";

        // 用Jackson构建请求体，自动处理转义
        ObjectNode textBlock = mapper.createObjectNode();
        textBlock.put("type", "text");
        textBlock.put("text", prompt);

        ObjectNode imageUrlObj = mapper.createObjectNode();
        imageUrlObj.put("url", "data:" + mimeType + ";base64," + base64Image);

        ObjectNode imageBlock = mapper.createObjectNode();
        imageBlock.put("type", "image_url");
        imageBlock.set("image_url", imageUrlObj);

        ArrayNode contentArray = mapper.createArrayNode();
        contentArray.add(textBlock);
        contentArray.add(imageBlock);

        ObjectNode messageObj = mapper.createObjectNode();
        messageObj.put("role", "user");
        messageObj.set("content", contentArray);

        ArrayNode messagesArray = mapper.createArrayNode();
        messagesArray.add(messageObj);

        ObjectNode requestBody = mapper.createObjectNode();
        requestBody.put("model", "deepseek-v4-flash-vision-exp");
        requestBody.set("messages", messagesArray);

        String requestBodyJson = mapper.writeValueAsString(requestBody);

        RequestBody body = RequestBody.create(
                requestBodyJson, MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(apiUrl)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseStr = response.body().string();
          

            JsonNode root = mapper.readTree(responseStr);
            String contentText = root.path("choices").get(0).path("message").path("content").asText();

            // content本身也是一段JSON字符串，需要再解析一次
            JsonNode resultJson = mapper.readTree(contentText);
            return resultJson.toString(); // 返回干净的 {"artist":"...","album":"..."}
        }
    }
}