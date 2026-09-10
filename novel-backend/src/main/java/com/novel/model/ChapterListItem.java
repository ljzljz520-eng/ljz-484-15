package com.novel.model;

import java.time.LocalDateTime;

/**
 * 作者工作台的章节列表项。附带所属作品标题，且不返回正文全文。
 */
public class ChapterListItem {
    private Long id;
    private Long novelId;
    private String novelTitle;
    private String title;
    private Integer orderNo;
    private String status;
    private Integer wordCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ChapterListItem() {
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

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getWordCount() { return wordCount; }
    public void setWordCount(Integer wordCount) { this.wordCount = wordCount; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
