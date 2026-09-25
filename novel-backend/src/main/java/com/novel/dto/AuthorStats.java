package com.novel.dto;

import java.time.LocalDateTime;

/**
 * 作者工作台统计信息（全部由后端基于本地数据计算）。
 */
public record AuthorStats(
        long novelCount,          // 作品数
        long draftChapterCount,   // 草稿章节数
        long publishedChapterCount, // 已发布章节数
        LocalDateTime lastUpdatedAt, // 最近更新时间（作品/章节中最新的时间，无任何内容时为 null）
        long weeklyWordCount      // 本周新增字数（本周一 00:00 之后创建的章节字数，含草稿）
) {
}
