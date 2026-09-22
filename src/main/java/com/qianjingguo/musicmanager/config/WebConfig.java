package com.qianjingguo.musicmanager.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload.path}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = uploadPath.endsWith("/") ? uploadPath : uploadPath + "/";
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + location);
    }

    // Vue Router 用的是 history 模式，浏览器直接刷新 /song/2 这类前端路由时
    // 请求会打到服务器，服务器并不认识这个路径，需要统一转发回 index.html，
    // 交给前端 Vue Router 自己根据当前 URL 匹配对应页面。
    // 注意：只转发"看起来像页面路径"的请求，不能把真正的接口路径（/song/list、
    // /song/add 这类已经在 SongController 里定义的具体接口）也转发掉，
    // 否则会覆盖真实的后端接口。这里用一个不会跟现有 REST 接口冲突的前缀来处理。
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/add").setViewName("forward:/index.html");
        registry.addViewController("/search").setViewName("forward:/index.html");
        registry.addViewController("/song/{id:[0-9]+}").setViewName("forward:/index.html");
    }
}

