package com.novel.dto;

import com.novel.model.Chapter;

import java.time.LocalDateTime;

/**
 * 作者视角的章节列表项：附加字数与所属作品标题。
 */
public record ChapterItem(
        Long id,
        Long novelId,
        String novelTitle,
        String title,
        Integer orderNo,
        String status,          // DRAFT / PUBLISHED
        long wordCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ChapterItem from(Chapter chapter, String novelTitle, long wordCount) {
        return new ChapterItem(chapter.getId(), chapter.getNovelId(), novelTitle,
                chapter.getTitle(), chapter.getOrderNo(),
                chapter.getStatus() == null ? null : chapter.getStatus().name(),
                wordCount, chapter.getCreatedAt(), chapter.getUpdatedAt());
    }
}
