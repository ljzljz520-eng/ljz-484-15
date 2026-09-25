package com.novel.service;

import com.novel.dto.AuthorStats;
import com.novel.dto.ChapterItem;
import com.novel.dto.NovelItem;
import com.novel.model.Chapter;
import com.novel.model.ChapterStatus;
import com.novel.model.Novel;
import com.novel.repository.DataRepository;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 作者工作台统计：所有数字均基于本地内存数据实时计算，前端不持有任何假数据。
 */
@Service
public class AuthorService {

    private final DataRepository dataRepository;

    public AuthorService(DataRepository dataRepository) {
        this.dataRepository = dataRepository;
    }

    /**
     * 统计工作台概览数据。
     */
    public AuthorStats getStats() {
        List<Chapter> allChapters = dataRepository.findAllChapters();

        long draftCount = allChapters.stream()
                .filter(c -> c.getStatus() == ChapterStatus.DRAFT)
                .count();
        long publishedCount = allChapters.stream()
                .filter(c -> c.getStatus() == ChapterStatus.PUBLISHED)
                .count();

        // 最近更新时间：所有作品的创建时间与章节更新时间中的最大值
        LocalDateTime latestNovelTime = dataRepository.findAllNovels().stream()
                .map(Novel::getCreatedAt)
                .filter(t -> t != null)
                .max(Comparator.naturalOrder())
                .orElse(null);
        LocalDateTime latestChapterTime = allChapters.stream()
                .map(Chapter::getUpdatedAt)
                .filter(t -> t != null)
                .max(Comparator.naturalOrder())
                .orElse(null);
        LocalDateTime lastUpdatedAt = maxTime(latestNovelTime, latestChapterTime);

        // 本周新增字数：本周一 00:00（含）之后创建的章节字数，草稿与已发布均计入
        LocalDateTime weekStart = LocalDateTime.now()
                .toLocalDate().atStartOfDay()
                .minusDays(DayOfWeek.from(LocalDateTime.now()).getValue() - 1);
        long weeklyWordCount = allChapters.stream()
                .filter(c -> c.getCreatedAt() != null && !c.getCreatedAt().isBefore(weekStart))
                .mapToLong(c -> countWords(c.getContent()))
                .sum();

        return new AuthorStats(dataRepository.findAllNovels().size(),
                draftCount, publishedCount, lastUpdatedAt, weeklyWordCount);
    }

    /**
     * 作品列表：附带每部作品的章节统计、字数与最近更新时间。
     */
    public List<NovelItem> listNovels() {
        List<Chapter> allChapters = dataRepository.findAllChapters();
        Map<Long, List<Chapter>> chaptersByNovel = allChapters.stream()
                .collect(Collectors.groupingBy(Chapter::getNovelId));

        return dataRepository.findAllNovels().stream()
                .sorted(Comparator.comparing(Novel::getId).reversed())
                .map(novel -> {
                    List<Chapter> own = chaptersByNovel.getOrDefault(novel.getId(), List.of());
                    long draft = own.stream().filter(c -> c.getStatus() == ChapterStatus.DRAFT).count();
                    long published = own.stream().filter(c -> c.getStatus() == ChapterStatus.PUBLISHED).count();
                    long words = own.stream().mapToLong(c -> countWords(c.getContent())).sum();
                    LocalDateTime latestChapterTime = own.stream()
                            .map(Chapter::getUpdatedAt)
                            .filter(t -> t != null)
                            .max(Comparator.naturalOrder())
                            .orElse(null);
                    LocalDateTime lastUpdatedAt = maxTime(novel.getCreatedAt(), latestChapterTime);
                    return NovelItem.from(novel, own.size(), draft, published, words, lastUpdatedAt);
                })
                .collect(Collectors.toList());
    }

    /**
     * 章节列表（作者视角，含草稿），按更新时间倒序。
     *
     * @param status   DRAFT / PUBLISHED，为空则全部
     * @param novelId  仅查看某部作品，为空则全部作品
     * @param thisWeek true 时仅返回本周一 00:00 之后创建的章节（与“本周新增字数”口径一致）
     */
    public List<ChapterItem> listChapters(ChapterStatus status, Long novelId, boolean thisWeek) {
        LocalDateTime weekStart = LocalDateTime.now()
                .toLocalDate().atStartOfDay()
                .minusDays(DayOfWeek.from(LocalDateTime.now()).getValue() - 1);

        return dataRepository.findChapters(status, novelId).stream()
                .filter(c -> !thisWeek || (c.getCreatedAt() != null && !c.getCreatedAt().isBefore(weekStart)))
                .map(c -> {
                    Novel novel = dataRepository.findNovelById(c.getNovelId());
                    String novelTitle = novel == null ? "未知作品" : novel.getTitle();
                    return ChapterItem.from(c, novelTitle, countWords(c.getContent()));
                })
                .collect(Collectors.toList());
    }

    /**
     * 字数统计：去除空白字符后的字符数（中文按字计）。
     */
    private long countWords(String content) {
        if (content == null || content.isEmpty()) {
            return 0;
        }
        return content.replaceAll("\\s+", "").length();
    }

    private LocalDateTime maxTime(LocalDateTime a, LocalDateTime b) {
        if (a == null) {
            return b;
        }
        if (b == null) {
            return a;
        }
        return a.isAfter(b) ? a : b;
    }
}
