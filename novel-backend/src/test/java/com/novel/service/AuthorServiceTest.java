package com.novel.service;

import com.novel.dto.AuthorStats;
import com.novel.dto.ChapterItem;
import com.novel.dto.NovelItem;
import com.novel.model.Chapter;
import com.novel.model.ChapterStatus;
import com.novel.repository.DataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AuthorServiceTest {

    private DataRepository repository;
    private AuthorService authorService;

    @BeforeEach
    void setUp() {
        repository = new DataRepository();
        repository.init(); // 加载种子数据
        authorService = new AuthorService(repository);
    }

    @Test
    void statsAreComputedFromLocalData() {
        AuthorStats stats = authorService.getStats();

        assertEquals(3, stats.novelCount());
        assertEquals(2, stats.draftChapterCount());
        assertEquals(6, stats.publishedChapterCount());
        assertNotNull(stats.lastUpdatedAt());

        // 本周新增字数 = 所有本周一 00:00 后创建章节的字数之和，且与章节列表口径一致
        List<ChapterItem> weeklyChapters = authorService.listChapters(null, null, true);
        long expectedWeeklyWords = weeklyChapters.stream().mapToLong(ChapterItem::wordCount).sum();
        assertTrue(expectedWeeklyWords > 0);
        assertEquals(expectedWeeklyWords, stats.weeklyWordCount());

        // 本周章节的创建时间均不早于本周一 00:00
        LocalDateTime weekStart = LocalDateTime.now().toLocalDate().atStartOfDay()
                .minusDays(DayOfWeek.from(LocalDateTime.now()).getValue() - 1);
        weeklyChapters.forEach(c -> assertFalse(c.createdAt().isBefore(weekStart)));
    }

    @Test
    void draftAndPublishedListsAreFilteredCorrectly() {
        List<ChapterItem> drafts = authorService.listChapters(ChapterStatus.DRAFT, null, false);
        List<ChapterItem> published = authorService.listChapters(ChapterStatus.PUBLISHED, null, false);

        assertTrue(drafts.stream().allMatch(c -> "DRAFT".equals(c.status())));
        assertTrue(published.stream().allMatch(c -> "PUBLISHED".equals(c.status())));
        assertEquals(2, drafts.size());
        assertEquals(6, published.size());

        // 最近更新列表按更新时间倒序，包含全部状态
        List<ChapterItem> all = authorService.listChapters(null, null, false);
        assertEquals(8, all.size());
        for (int i = 1; i < all.size(); i++) {
            assertFalse(all.get(i - 1).updatedAt().isBefore(all.get(i).updatedAt()));
        }
    }

    @Test
    void novelItemsAggregateChapterStats() {
        List<NovelItem> items = authorService.listNovels();
        assertEquals(3, items.size());

        NovelItem first = items.stream().filter(n -> n.id() == 1L).findFirst().orElseThrow();
        assertEquals(4, first.totalChapters());
        assertEquals(1, first.draftChapters());
        assertEquals(3, first.publishedChapters());
        assertTrue(first.wordCount() > 0);
        assertNotNull(first.lastUpdatedAt());

        // 各作品的已发布 + 草稿 = 总章节
        items.forEach(n -> assertEquals(n.totalChapters(), n.draftChapters() + n.publishedChapters()));
    }

    @Test
    void publicChapterListExcludesDrafts() {
        List<Chapter> publicChapters = repository.findChaptersByNovelId(1L);
        assertEquals(3, publicChapters.size());
        assertTrue(publicChapters.stream().allMatch(c -> c.getStatus() == ChapterStatus.PUBLISHED));
    }
}
