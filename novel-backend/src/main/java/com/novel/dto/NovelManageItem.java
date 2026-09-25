package com.novel.dto;

import com.novel.model.Novel;

import java.time.LocalDateTime;

/**
 * 作者视角的作品列表项，携带该作品的章节统计。
 */
public class NovelManageItem {
    private Long id;
    private String title;
    private String description;
    private String coverUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private long draftChapterCount;
    private long publishedChapterCount;
    private long totalChapterCount;
    private long wordCount;

    public NovelManageItem() {}

    public NovelManageItem(Novel novel) {
        this.id = novel.getId();
        this.title = novel.getTitle();
        this.description = novel.getDescription();
        this.coverUrl = novel.getCoverUrl();
        this.createdAt = novel.getCreatedAt();
        this.updatedAt = novel.getUpdatedAt();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public long getDraftChapterCount() { return draftChapterCount; }
    public void setDraftChapterCount(long draftChapterCount) { this.draftChapterCount = draftChapterCount; }

    public long getPublishedChapterCount() { return publishedChapterCount; }
    public void setPublishedChapterCount(long publishedChapterCount) { this.publishedChapterCount = publishedChapterCount; }

    public long getTotalChapterCount() { return totalChapterCount; }
    public void setTotalChapterCount(long totalChapterCount) { this.totalChapterCount = totalChapterCount; }

    public long getWordCount() { return wordCount; }
    public void setWordCount(long wordCount) { this.wordCount = wordCount; }
}
