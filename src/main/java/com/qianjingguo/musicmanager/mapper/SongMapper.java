package com.qianjingguo.musicmanager.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qianjingguo.musicmanager.entity.Song;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SongMapper extends BaseMapper<Song> {
}