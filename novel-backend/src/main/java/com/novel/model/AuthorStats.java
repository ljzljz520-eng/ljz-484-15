package com.novel.model;

import java.time.LocalDateTime;

/**
 * 作者工作台统计信息。全部数值由后端根据本地数据计算。
 */
public class AuthorStats {
    private Long novelCount;
    private Long draftChapterCount;
    private Long publishedChapterCount;
    private LocalDateTime lastUpdatedAt;
    private Long weeklyWordCount;
    /** 本周起始时间（周一 00:00），便于前端展示统计口径 */
    private LocalDateTime weekStart;

    public AuthorStats() {
    }

    public AuthorStats(Long novelCount, Long draftChapterCount, Long publishedChapterCount,
                       LocalDateTime lastUpdatedAt, Long weeklyWordCount, LocalDateTime weekStart) {
        this.novelCount = novelCount;
        this.draftChapterCount = draftChapterCount;
        this.publishedChapterCount = publishedChapterCount;
        this.lastUpdatedAt = lastUpdatedAt;
        this.weeklyWordCount = weeklyWordCount;
        this.weekStart = weekStart;
    }

    public Long getNovelCount() { return novelCount; }
    public void setNovelCount(Long novelCount) { this.novelCount = novelCount; }

    public Long getDraftChapterCount() { return draftChapterCount; }
    public void setDraftChapterCount(Long draftChapterCount) { this.draftChapterCount = draftChapterCount; }

    public Long getPublishedChapterCount() { return publishedChapterCount; }
    public void setPublishedChapterCount(Long publishedChapterCount) { this.publishedChapterCount = publishedChapterCount; }

    public LocalDateTime getLastUpdatedAt() { return lastUpdatedAt; }
    public void setLastUpdatedAt(LocalDateTime lastUpdatedAt) { this.lastUpdatedAt = lastUpdatedAt; }

    public Long getWeeklyWordCount() { return weeklyWordCount; }
    public void setWeeklyWordCount(Long weeklyWordCount) { this.weeklyWordCount = weeklyWordCount; }

    public LocalDateTime getWeekStart() { return weekStart; }
    public void setWeekStart(LocalDateTime weekStart) { this.weekStart = weekStart; }
}
