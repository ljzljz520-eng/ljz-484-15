package com.novel.controller;

import com.novel.dto.AuthorStats;
import com.novel.dto.ChapterManageItem;
import com.novel.dto.NovelManageItem;
import com.novel.model.Chapter;
import com.novel.model.Novel;
import com.novel.repository.DataRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

/**
 * 作者工作台接口：统计信息与作品/章节管理列表。
 * 所有统计数字均在后端基于本地数据实时计算，前端只负责展示。
 */
@RestController
@RequestMapping("/api/author")
@CrossOrigin(origins = "*")
@Tag(name = "Author Workbench API")
public class AuthorController {

    private static final Set<String> ALLOWED_STATUS = Set.of(
            DataRepository.STATUS_DRAFT, DataRepository.STATUS_PUBLISHED);

    private final DataRepository dataRepository;

    public AuthorController(DataRepository dataRepository) {
        this.dataRepository = dataRepository;
    }

    @GetMapping("/stats")
    @Operation(summary = "工作台统计：作品数/草稿章节/已发布章节/最近更新时间/本周新增字数")
    public AuthorStats getStats() {
        LocalDateTime weekStart = LocalDate.now()
                .with(DayOfWeek.MONDAY)
                .atTime(LocalTime.MIN);
        LocalDateTime now = LocalDateTime.now();

        AuthorStats stats = new AuthorStats();
        stats.setNovelCount(dataRepository.countNovels());
        stats.setDraftChapterCount(
                dataRepository.countChapters(null, DataRepository.STATUS_DRAFT));
        stats.setPublishedChapterCount(
                dataRepository.countChapters(null, DataRepository.STATUS_PUBLISHED));
        stats.setTotalChapterCount(
                stats.getDraftChapterCount() + stats.getPublishedChapterCount());
        stats.setLastUpdatedAt(dataRepository.findLastUpdatedAt());

        long weeklyWords = 0;
        long weeklyChapters = 0;
        for (Chapter chapter : dataRepository.findChaptersCreatedAfter(weekStart)) {
            weeklyWords += ChapterManageItem.countWords(chapter.getContent());
            weeklyChapters++;
        }
        stats.setWeeklyNewWords(weeklyWords);
        stats.setWeeklyNewChapterCount(weeklyChapters);
        stats.setCalculatedAt(now);
        return stats;
    }

    @GetMapping("/novels")
    @Operation(summary = "作者-作品列表（含每部作品的章节统计与字数）")
    public Map<String, Object> listAuthorNovels(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        List<NovelManageItem> all = dataRepository.findAllNovelsForAuthor(keyword).stream()
                .map(this::toManageItem)
                .toList();

        long total = all.size();
        List<NovelManageItem> paged = paginate(all, page, size);

        Map<String, Object> response = new HashMap<>();
        response.put("data", paged);
        response.put("total", total);
        response.put("page", page);
        response.put("size", size);
        return response;
    }

    @GetMapping("/chapters")
    @Operation(summary = "作者-章节列表（可按作品/状态过滤，草稿与已发布均可见）")
    public Map<String, Object> listAuthorChapters(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long novelId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean thisWeek) {
        if (status != null && !status.isEmpty()
                && ALLOWED_STATUS.stream().noneMatch(s -> s.equalsIgnoreCase(status))) {
            throw new IllegalArgumentException("status 仅支持 DRAFT 或 PUBLISHED");
        }

        LocalDateTime weekStart = LocalDate.now()
                .with(DayOfWeek.MONDAY)
                .atTime(LocalTime.MIN);

        List<ChapterManageItem> all = dataRepository
                .findAllChaptersForAuthor(novelId, status, keyword).stream()
                .filter(c -> !Boolean.TRUE.equals(thisWeek)
                        || (c.getCreatedAt() != null && !c.getCreatedAt().isBefore(weekStart)))
                .map(c -> new ChapterManageItem(c, dataRepository.findNovelTitle(c.getNovelId())))
                .toList();

        long total = all.size();
        List<ChapterManageItem> paged = paginate(all, page, size);

        Map<String, Object> response = new HashMap<>();
        response.put("data", paged);
        response.put("total", total);
        response.put("page", page);
        response.put("size", size);
        return response;
    }

    private NovelManageItem toManageItem(Novel novel) {
        NovelManageItem item = new NovelManageItem(novel);
        long draft = dataRepository.countChapters(novel.getId(), DataRepository.STATUS_DRAFT);
        long published = dataRepository.countChapters(novel.getId(), DataRepository.STATUS_PUBLISHED);
        item.setDraftChapterCount(draft);
        item.setPublishedChapterCount(published);
        item.setTotalChapterCount(draft + published);
        long words = 0;
        for (Chapter chapter : dataRepository.findAllChaptersForAuthor(novel.getId(), null, null)) {
            words += ChapterManageItem.countWords(chapter.getContent());
        }
        item.setWordCount(words);
        return item;
    }

    private <T> List<T> paginate(List<T> all, int page, int size) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(size, 1);
        int from = Math.min((safePage - 1) * safeSize, all.size());
        int to = Math.min(from + safeSize, all.size());
        return all.subList(from, to);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(org.springframework.http.HttpStatus.BAD_REQUEST)
    public Map<String, String> handleBadRequest(IllegalArgumentException ex) {
        Map<String, String> body = new HashMap<>();
        body.put("message", ex.getMessage());
        return body;
    }
}
