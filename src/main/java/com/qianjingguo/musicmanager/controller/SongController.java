package com.qianjingguo.musicmanager.controller;

import com.qianjingguo.musicmanager.entity.Song;
import com.qianjingguo.musicmanager.mapper.SongMapper;
import com.qianjingguo.musicmanager.service.AiRecognitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@RestController

@RequestMapping("/song")
public class SongController {
    @Autowired
    private AiRecognitionService aiRecognitionService;

    @Autowired
    private SongMapper songMapper;

    // 查询所有歌曲
    @GetMapping("/list")
    public List<Song> list() {
        return songMapper.selectList(null);
    }

    @PostMapping("/recognize-cover")
    public String recognizeCover(@RequestParam("file") MultipartFile file) throws Exception {
        return aiRecognitionService.recognizeCover(file);
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

    @PostMapping("/recognize-and-add")
    public Song recognizeAndAdd(@RequestParam("file") MultipartFile file) throws Exception {
        // 1. 调用AI识别，拿到干净的JSON结果
        String resultJson = aiRecognitionService.recognizeCover(file);

        // 2. 解析识别结果
        ObjectMapper mapper = new ObjectMapper();
        JsonNode resultNode = mapper.readTree(resultJson);
        String artist = resultNode.path("artist").asText();
        String album = resultNode.path("album").asText();

        // 3. 构建Song对象并写入数据库
        Song song = new Song();
        song.setArtist(artist);
        song.setAlbum(album);
        song.setTitle(album); // 暂时用专辑名占位，歌名可以后续手动补充
        songMapper.insert(song);

        return song; // 返回完整的Song对象（包含数据库自动生成的id）
    }
}