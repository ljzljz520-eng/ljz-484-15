package com.novel.dto;

import com.novel.model.Chapter;

import java.time.LocalDateTime;

/**
 * 作者视角的章节列表项，携带所属作品标题与字数。
 */
public class ChapterManageItem {
    private Long id;
    private Long novelId;
    private String novelTitle;
    private String title;
    private Integer orderNo;
    private String content;
    private String status;
    private long wordCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ChapterManageItem() {}

    public ChapterManageItem(Chapter chapter, String novelTitle) {
        this.id = chapter.getId();
        this.novelId = chapter.getNovelId();
        this.novelTitle = novelTitle;
        this.title = chapter.getTitle();
        this.orderNo = chapter.getOrderNo();
        this.content = chapter.getContent();
        this.status = chapter.getStatus();
        this.wordCount = countWords(chapter.getContent());
        this.createdAt = chapter.getCreatedAt();
        this.updatedAt = chapter.getUpdatedAt();
    }

    public static long countWords(String content) {
        if (content == null || content.isEmpty()) {
            return 0;
        }
        // 中文小说按字符计：去掉所有空白字符后的字符数
        return content.chars().filter(c -> !Character.isWhitespace(c)).count();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getNovelId() { return novelId; }
    public void setNovelId(Long novelId) { this.novelId = novelId; }

    public String getNovelTitle() { return novelTitle; }
    public void setNovelTitle(String novelTitle) { this.novelTitle = novelTitle; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Integer getOrderNo() { return orderNo; }
    public void setOrderNo(Integer orderNo) { this.orderNo = orderNo; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public long getWordCount() { return wordCount; }
    public void setWordCount(long wordCount) { this.wordCount = wordCount; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
