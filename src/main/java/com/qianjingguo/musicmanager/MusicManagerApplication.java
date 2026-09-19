package com.qianjingguo.musicmanager;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@MapperScan("com.qianjingguo.musicmanager.mapper")

@SpringBootApplication
public class MusicManagerApplication {


    public static void main(String[] args) {
        SpringApplication.run(MusicManagerApplication.class, args);
    }

}
