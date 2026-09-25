package com.novel.dto;

import com.novel.model.Novel;

import java.time.LocalDateTime;

/**
 * 作者视角的作品列表项：在作品基础信息上附加章节统计。
 */
public record NovelItem(
        Long id,
        String title,
        String description,
        String coverUrl,
        LocalDateTime createdAt,
        long totalChapters,   // 章节总数（含草稿）
        long draftChapters,   // 草稿章节数
        long publishedChapters, // 已发布章节数
        long wordCount,       // 全部章节字数（含草稿）
        LocalDateTime lastUpdatedAt // 作品或其章节的最新时间
) {
    public static NovelItem from(Novel novel, long totalChapters, long draftChapters,
                                 long publishedChapters, long wordCount, LocalDateTime lastUpdatedAt) {
        return new NovelItem(novel.getId(), novel.getTitle(), novel.getDescription(),
                novel.getCoverUrl(), novel.getCreatedAt(), totalChapters, draftChapters,
                publishedChapters, wordCount, lastUpdatedAt);
    }
}
