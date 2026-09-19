package com.qianjingguo.musicmanager.controller;

import com.qianjingguo.musicmanager.entity.Song;
import com.qianjingguo.musicmanager.mapper.SongMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/song")
public class SongController {

    @Autowired
    private SongMapper songMapper;

    // 查询所有歌曲
    @GetMapping("/list")
    public List<Song> list() {
        return songMapper.selectList(null);
    }

    // 新增歌曲
    @PostMapping("/add")
    public String add(@RequestBody Song song) {
        songMapper.insert(song);
        return "添加成功";
    }

    // 根据id删除
    @DeleteMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        songMapper.deleteById(id);
        return "删除成功";
    }

    // 更新歌曲信息
    @PutMapping("/update")
    public String update(@RequestBody Song song) {
        songMapper.updateById(song);
        return "更新成功";
    }
}