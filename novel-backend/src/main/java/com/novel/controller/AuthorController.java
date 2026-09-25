package com.novel.controller;

import com.novel.dto.AuthorStats;
import com.novel.dto.ChapterItem;
import com.novel.dto.NovelItem;
import com.novel.model.ChapterStatus;
import com.novel.service.AuthorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/author")
@CrossOrigin(origins = "*")
@Tag(name = "Author Workbench API", description = "作者工作台统计与列表")
public class AuthorController {

    private final AuthorService authorService;

    public AuthorController(AuthorService authorService) {
        this.authorService = authorService;
    }

    @GetMapping("/stats")
    @Operation(summary = "工作台统计：作品数、草稿/已发布章节、最近更新、本周新增字数")
    public AuthorStats getStats() {
        return authorService.getStats();
    }

    @GetMapping("/novels")
    @Operation(summary = "作者作品列表（含每部作品的章节统计）")
    public List<NovelItem> getNovels() {
        return authorService.listNovels();
    }

    @GetMapping("/chapters")
    @Operation(summary = "作者章节列表（含草稿），可按状态/作品/本周过滤")
    public List<ChapterItem> getChapters(
            @RequestParam(required = false) ChapterStatus status,
            @RequestParam(required = false) Long novelId,
            @RequestParam(defaultValue = "false") boolean thisWeek) {
        return authorService.listChapters(status, novelId, thisWeek);
    }
}
