package com.qianjingguo.musicmanager.controller;


import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/song")
public class ItunesProxyController {

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build();

    private final ObjectMapper mapper = new ObjectMapper();

    // 后端代为请求iTunes搜索接口，前端只需要同源调用这个接口，避免浏览器端跨域/隐私拦截问题
    @GetMapping("/itunes-preview")
    public String itunesPreview(@RequestParam String title, @RequestParam(required = false) String artist) throws Exception {
        String term = URLEncoder.encode(((artist == null ? "" : artist) + " " + title).trim(), StandardCharsets.UTF_8);
        String url = "https://itunes.apple.com/search?term=" + term + "&media=music&limit=1";

        Request request = new Request.Builder().url(url).get().build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                return "{\"previewUrl\": null}";
            }
            String body = response.body().string();
            JsonNode root = mapper.readTree(body);
            JsonNode results = root.path("results");
            if (results.isArray() && results.size() > 0) {
                String previewUrl = results.get(0).path("previewUrl").asText(null);
                return mapper.writeValueAsString(new PreviewResult(previewUrl));
            }
            return "{\"previewUrl\": null}";
        } catch (Exception e) {
            return "{\"previewUrl\": null}";
        }
    }

    record PreviewResult(String previewUrl) {}
}