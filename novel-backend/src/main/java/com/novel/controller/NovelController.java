package com.novel.controller;

import com.novel.model.AuthorStats;
import com.novel.model.Chapter;
import com.novel.model.ChapterListItem;
import com.novel.model.Novel;
import com.novel.repository.DataRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // Allow frontend to access
@Tag(name = "Novel System API")
public class NovelController {

    private final DataRepository dataRepository;

    public NovelController(DataRepository dataRepository) {
        this.dataRepository = dataRepository;
    }

    @GetMapping("/novels")
    @Operation(summary = "Get Novel List")
    public Map<String, Object> getNovels(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        List<Novel> list = dataRepository.findAllNovels(keyword, page, size);
        long total = dataRepository.countNovels(keyword);

        Map<String, Object> response = new HashMap<>();
        response.put("data", list);
        response.put("total", total);
        response.put("page", page);
        response.put("size", size);
        return response;
    }

    @GetMapping("/novels/{id}")
    @Operation(summary = "Get Novel Details (with chapters)")
    public Map<String, Object> getNovelDetail(@PathVariable Long id) {
        Novel novel = dataRepository.findNovelById(id);
        if (novel == null) {
            throw new RuntimeException("Novel not found");
        }
        List<Chapter> chapters = dataRepository.findChaptersByNovelId(id);

        Map<String, Object> response = new HashMap<>();
        response.put("novel", novel);
        response.put("chapters", chapters); // Include chapters as requested ("merge directory into detail")
        return response;
    }

    @GetMapping("/novels/{id}/chapters")
    @Operation(summary = "Get Chapters for a Novel")
    public List<Chapter> getChapters(@PathVariable Long id) {
        return dataRepository.findChaptersByNovelId(id);
    }

    @GetMapping("/chapters/{id}")
    @Operation(summary = "Get Chapter Content")
    public Chapter getChapter(@PathVariable Long id) {
        Chapter chapter = dataRepository.findChapterById(id);
        if (chapter == null) {
            throw new RuntimeException("Chapter not found");
        }
        return chapter;
    }

    @GetMapping("/author/stats")
    @Operation(summary = "Author Workbench Statistics")
    public AuthorStats getAuthorStats() {
        LocalDateTime weekStart = DataRepository.currentWeekStart();
        return new AuthorStats(
                dataRepository.countAllNovels(),
                dataRepository.countChaptersByStatus(Chapter.STATUS_DRAFT),
                dataRepository.countChaptersByStatus(Chapter.STATUS_PUBLISHED),
                dataRepository.findLatestChapterUpdate(),
                dataRepository.sumWordCountCreatedSince(weekStart),
                weekStart);
    }

    @GetMapping("/author/chapters")
    @Operation(summary = "Author Chapter List (filterable by status / this week)")
    public Map<String, Object> getAuthorChapters(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "false") boolean weekly,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        LocalDateTime weekStart = DataRepository.currentWeekStart();
        List<ChapterListItem> list = dataRepository
                .findChaptersForAuthor(status, weekly, weekStart, page, size)
                .stream()
                .map(this::toChapterListItem)
                .collect(Collectors.toList());
        long total = dataRepository.countChaptersForAuthor(status, weekly, weekStart);

        Map<String, Object> response = new HashMap<>();
        response.put("data", list);
        response.put("total", total);
        response.put("page", page);
        response.put("size", size);
        return response;
    }

    private ChapterListItem toChapterListItem(Chapter chapter) {
        ChapterListItem item = new ChapterListItem();
        item.setId(chapter.getId());
        item.setNovelId(chapter.getNovelId());
        Novel novel = dataRepository.findNovelById(chapter.getNovelId());
        item.setNovelTitle(novel != null ? novel.getTitle() : "未知作品");
        item.setTitle(chapter.getTitle());
        item.setOrderNo(chapter.getOrderNo());
        item.setStatus(chapter.getStatus());
        item.setWordCount(chapter.getWordCount());
        item.setCreatedAt(chapter.getCreatedAt());
        item.setUpdatedAt(chapter.getUpdatedAt());
        return item;
    }
}
