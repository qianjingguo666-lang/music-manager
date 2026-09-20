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
    public String generateTags(String title, String artist, String album) throws Exception {
        String prompt = String.format(
                "根据歌曲信息，用中文给出3-5个简短的风格或情绪标签（如：安静、欢快、适合运动、伤感、夜晚），"
                        + "严格按JSON数组格式返回，不要多余文字：[\"标签1\",\"标签2\"]。"
                        + "歌曲信息：歌名《%s》，歌手：%s，专辑：%s", title, artist, album);

        ObjectNode textBlock = mapper.createObjectNode();
        textBlock.put("type", "text");
        textBlock.put("text", prompt);

        ArrayNode contentArray = mapper.createArrayNode();
        contentArray.add(textBlock);

        ObjectNode messageObj = mapper.createObjectNode();
        messageObj.put("role", "user");
        messageObj.set("content", contentArray);

        ArrayNode messagesArray = mapper.createArrayNode();
        messagesArray.add(messageObj);

        ObjectNode requestBody = mapper.createObjectNode();
        requestBody.put("model", "deepseek-chat"); // 纯文字任务用deepseek-chat，比vision模型更便宜
        requestBody.set("messages", messagesArray);

        String requestBodyJson = mapper.writeValueAsString(requestBody);

        RequestBody body = RequestBody.create(requestBodyJson, MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(apiUrl)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseStr = response.body().string();
            JsonNode root = mapper.readTree(responseStr);
            return root.path("choices").get(0).path("message").path("content").asText();
        }
    }
    public String searchByNaturalLanguage(String query, String songListText) throws Exception {
        String prompt = String.format(
                "用户想找这样的歌曲：\"%s\"。以下是歌曲库列表：\n%s\n"
                        + "请根据歌曲的标签，判断哪些歌曲符合用户描述，只返回匹配的歌曲id，"
                        + "严格按JSON数组格式返回，不要多余文字，如果没有匹配的返回空数组：[1,2,3]",
                query, songListText);

        ObjectNode textBlock = mapper.createObjectNode();
        textBlock.put("type", "text");
        textBlock.put("text", prompt);

        ArrayNode contentArray = mapper.createArrayNode();
        contentArray.add(textBlock);

        ObjectNode messageObj = mapper.createObjectNode();
        messageObj.put("role", "user");
        messageObj.set("content", contentArray);

        ArrayNode messagesArray = mapper.createArrayNode();
        messagesArray.add(messageObj);

        ObjectNode requestBody = mapper.createObjectNode();
        requestBody.put("model", "deepseek-chat");
        requestBody.set("messages", messagesArray);

        String requestBodyJson = mapper.writeValueAsString(requestBody);

        RequestBody body = RequestBody.create(requestBodyJson, MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(apiUrl)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseStr = response.body().string();
            JsonNode root = mapper.readTree(responseStr);
            return root.path("choices").get(0).path("message").path("content").asText();
        }
    }
}