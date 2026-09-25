package com.novel.dto;

import java.time.LocalDateTime;

/**
 * 作者工作台统计信息。所有数值均由后端基于本地数据实时计算。
 */
public class AuthorStats {
    /** 作品总数 */
    private long novelCount;
    /** 草稿章节数 */
    private long draftChapterCount;
    /** 已发布章节数 */
    private long publishedChapterCount;
    /** 章节总数 */
    private long totalChapterCount;
    /** 最近更新时间（全部作品/章节中的最新更新时间） */
    private LocalDateTime lastUpdatedAt;
    /** 本周新增字数（本周一 00:00 起，新增章节正文去空白字符数） */
    private long weeklyNewWords;
    /** 本周新增章节数（用于展示口径说明） */
    private long weeklyNewChapterCount;
    /** 统计计算时间 */
    private LocalDateTime calculatedAt;

    public long getNovelCount() { return novelCount; }
    public void setNovelCount(long novelCount) { this.novelCount = novelCount; }

    public long getDraftChapterCount() { return draftChapterCount; }
    public void setDraftChapterCount(long draftChapterCount) { this.draftChapterCount = draftChapterCount; }

    public long getPublishedChapterCount() { return publishedChapterCount; }
    public void setPublishedChapterCount(long publishedChapterCount) { this.publishedChapterCount = publishedChapterCount; }

    public long getTotalChapterCount() { return totalChapterCount; }
    public void setTotalChapterCount(long totalChapterCount) { this.totalChapterCount = totalChapterCount; }

    public LocalDateTime getLastUpdatedAt() { return lastUpdatedAt; }
    public void setLastUpdatedAt(LocalDateTime lastUpdatedAt) { this.lastUpdatedAt = lastUpdatedAt; }

    public long getWeeklyNewWords() { return weeklyNewWords; }
    public void setWeeklyNewWords(long weeklyNewWords) { this.weeklyNewWords = weeklyNewWords; }

    public long getWeeklyNewChapterCount() { return weeklyNewChapterCount; }
    public void setWeeklyNewChapterCount(long weeklyNewChapterCount) { this.weeklyNewChapterCount = weeklyNewChapterCount; }

    public LocalDateTime getCalculatedAt() { return calculatedAt; }
    public void setCalculatedAt(LocalDateTime calculatedAt) { this.calculatedAt = calculatedAt; }
}
