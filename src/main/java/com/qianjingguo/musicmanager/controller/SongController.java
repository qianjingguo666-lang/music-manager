package com.qianjingguo.musicmanager.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qianjingguo.musicmanager.entity.Song;
import com.qianjingguo.musicmanager.mapper.SongMapper;
import com.qianjingguo.musicmanager.service.AiRecognitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    // 给指定歌曲生成AI标签
    @PostMapping("/generate-tags/{id}")
    public Song generateTags(@PathVariable Long id) throws Exception {
        Song song = songMapper.selectById(id);
        if (song == null) {
            throw new RuntimeException("歌曲不存在");
        }

        String tagsJson = aiRecognitionService.generateTags(
                song.getTitle(), song.getArtist(), song.getAlbum());

        // 把AI返回的 ["标签1","标签2"] 转成 "标签1,标签2" 存进数据库
        ObjectMapper mapper = new ObjectMapper();
        JsonNode tagsArray = mapper.readTree(tagsJson);
        StringBuilder tagsBuilder = new StringBuilder();
        for (JsonNode tag : tagsArray) {
            if (tagsBuilder.length() > 0) tagsBuilder.append(",");
            tagsBuilder.append(tag.asText());
        }
        song.setTags(tagsBuilder.toString());
        songMapper.updateById(song);

        return song;
    }

    @GetMapping("/search")
    public List<Song> search(@RequestParam String query) throws Exception {
        // 优化点：只查询已经打过标签的歌曲，且限制最大数量，避免全量数据发给AI
        QueryWrapper<Song> wrapper = new QueryWrapper<>();
        wrapper.isNotNull("tags")
                .orderByDesc("create_time")
                .last("LIMIT 200"); // 最多取最近200首参与AI判断

        List<Song> candidateSongs = songMapper.selectList(wrapper);

        if (candidateSongs.isEmpty()) {
            return new java.util.ArrayList<>(); // 没有可检索的歌曲，直接返回空列表
        }

        StringBuilder songListText = new StringBuilder();
        for (Song song : candidateSongs) {
            songListText.append(String.format("id:%d, 歌名:%s, 标签:%s\n",
                    song.getId(), song.getTitle(), song.getTags()));
        }

        String matchedIdsJson = aiRecognitionService.searchByNaturalLanguage(query, songListText.toString());

        ObjectMapper mapper = new ObjectMapper();
        JsonNode idsArray = mapper.readTree(matchedIdsJson);
        List<Song> result = new java.util.ArrayList<>();
        for (JsonNode idNode : idsArray) {
            Long id = idNode.asLong();
            candidateSongs.stream()
                    .filter(s -> s.getId().equals(id))
                    .findFirst()
                    .ifPresent(result::add);
        }
        return result;
    }
}