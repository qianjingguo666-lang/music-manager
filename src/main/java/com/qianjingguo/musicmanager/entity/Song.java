package com.qianjingguo.musicmanager.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Song {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String artist;
    private String album;
    private String filePath;
    private String coverPath;
    private LocalDateTime createTime;
}
