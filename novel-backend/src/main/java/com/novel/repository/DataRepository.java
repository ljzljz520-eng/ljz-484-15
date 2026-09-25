package com.novel.repository;

import com.novel.model.Chapter;
import com.novel.model.Novel;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class DataRepository {
    public static final String STATUS_DRAFT = "DRAFT";
    public static final String STATUS_PUBLISHED = "PUBLISHED";

    private final Map<Long, Novel> novels = new ConcurrentHashMap<>();
    private final Map<Long, Chapter> chapters = new ConcurrentHashMap<>();
    private final AtomicLong novelIdGenerator = new AtomicLong(1);
    private final AtomicLong chapterIdGenerator = new AtomicLong(1);

    @PostConstruct
    public void init() {
        // 种子数据：时间点分布在“本周”与“上周及更早”，章节包含草稿与已发布，
        // 便于真实验证工作台统计（本周新增字数、草稿/发布数、最近更新时间）。
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime monday = LocalDate.now()
                .with(DayOfWeek.MONDAY)
                .atTime(LocalTime.MIN);
        LocalDateTime lastWeek = monday.minusDays(3); // 上周（本周一之前）
        LocalDateTime tuesday = monday.plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime thursday = monday.plusDays(3).withHour(15).withMinute(30).withSecond(0).withNano(0);
        if (thursday.isAfter(now)) {
            thursday = now.minusHours(1);
        }

        // ---------------- 作品 1 ----------------
        Novel novel1 = saveNovel(new Novel(novelIdGenerator.getAndIncrement(),
                "星际穿越之编程大师",
                "讲述一位程序员意外穿越到未来，用代码拯救宇宙的故事。",
                "https://images.unsplash.com/photo-1550751827-4bd374c3f58b?q=80&w=800&auto=format&fit=crop",
                lastWeek.minusDays(10), lastWeek));
        saveChapter(new Chapter(chapterIdGenerator.getAndIncrement(), novel1.getId(),
                "第一章：Hello World", 1,
                "他醒来时，发现眼前只有绿色的代码流。\n光标在虚空中闪烁，仿佛等待着他敲下第一行指令。",
                STATUS_PUBLISHED, lastWeek.minusDays(10), lastWeek.minusDays(10)));
        saveChapter(new Chapter(chapterIdGenerator.getAndIncrement(), novel1.getId(),
                "第二章：变量声明", 2,
                "“你是谁？”面前的机器人冷冷地问道。\n“Define me.”他回答，声音在金属走廊里回荡。",
                STATUS_PUBLISHED, lastWeek.minusDays(6), lastWeek.minusDays(6)));
        saveChapter(new Chapter(chapterIdGenerator.getAndIncrement(), novel1.getId(),
                "第三章：循环陷阱", 3,
                "时间仿佛陷入了死循环，他必须找到 break 的条件。\n每一次醒来，都是同一个清晨。",
                STATUS_PUBLISHED, tuesday, tuesday));
        saveChapter(new Chapter(chapterIdGenerator.getAndIncrement(), novel1.getId(),
                "第四章：异常捕获（草稿）", 4,
                "抛出的异常被一只无形的手接住了。\n他翻开日志，发现堆栈的尽头写着自己的名字。",
                STATUS_DRAFT, thursday, thursday));
        refreshNovelUpdatedAt(novel1.getId());

        // ---------------- 作品 2 ----------------
        Novel novel2 = saveNovel(new Novel(novelIdGenerator.getAndIncrement(),
                "灵气复苏时代的架构师",
                "灵气复苏，万物进化。他发现修仙法门竟然符合微服务架构原理。",
                "https://images.unsplash.com/photo-1518770660439-4636190af475?q=80&w=600&auto=format&fit=crop",
                lastWeek.minusDays(8), tuesday));
        saveChapter(new Chapter(chapterIdGenerator.getAndIncrement(), novel2.getId(),
                "第一章：单体应用破碎", 1,
                "天地巨变，世界原本的秩序（Monolith）崩塌了。\n旧时代的法门在一夜之间全部失效。",
                STATUS_PUBLISHED, lastWeek.minusDays(8), lastWeek.minusDays(8)));
        saveChapter(new Chapter(chapterIdGenerator.getAndIncrement(), novel2.getId(),
                "第二章：服务发现", 2,
                "他感应到了周围的灵气节点，就像注册中心里的服务一样清晰。\n每一个节点，都有心跳。",
                STATUS_PUBLISHED, tuesday, tuesday));
        saveChapter(new Chapter(chapterIdGenerator.getAndIncrement(), novel2.getId(),
                "第三章：熔断心法（草稿）", 3,
                "当灵气洪流超过阈值，最稳妥的选择竟然是主动断开。\n他第一次理解了什么叫降级。",
                STATUS_DRAFT, thursday, thursday));
        refreshNovelUpdatedAt(novel2.getId());

        // ---------------- 作品 3 ----------------
        Novel novel3 = saveNovel(new Novel(novelIdGenerator.getAndIncrement(),
                "只有我知道剧情的测试员",
                "作为世界系统的唯一QA，他能看到由于Bug导致的隐藏剧情。",
                "https://images.unsplash.com/photo-1555949963-ff9fe0c870eb?q=80&w=800&auto=format&fit=crop",
                lastWeek.minusDays(5), lastWeek.minusDays(5)));
        saveChapter(new Chapter(chapterIdGenerator.getAndIncrement(), novel3.getId(),
                "第一章：第一个缺陷", 1,
                "世界的边缘有一道裂缝，只有他看得见。\n裂缝里透出的，不是光，是控制台的报错信息。",
                STATUS_PUBLISHED, lastWeek.minusDays(5), lastWeek.minusDays(5)));
        saveChapter(new Chapter(chapterIdGenerator.getAndIncrement(), novel3.getId(),
                "第二章：复现步骤（草稿）", 2,
                "要触发隐藏剧情，需要严格按照步骤复现。\n他掏出笔记本，开始记录第二条用例。",
                STATUS_DRAFT, thursday, thursday));
        refreshNovelUpdatedAt(novel3.getId());
    }

    // ---------------- 作品查询（读者侧） ----------------

    public List<Novel> findAllNovels(String keyword, int page, int size) {
        return novels.values().stream()
                .filter(n -> keyword == null || keyword.isEmpty() || n.getTitle().contains(keyword)
                        || n.getDescription().contains(keyword))
                .sorted(Comparator.comparing(Novel::getId).reversed())
                .skip((long) (page - 1) * size)
                .limit(size)
                .collect(Collectors.toList());
    }

    public long countNovels(String keyword) {
        return novels.values().stream()
                .filter(n -> keyword == null || keyword.isEmpty() || n.getTitle().contains(keyword)
                        || n.getDescription().contains(keyword))
                .count();
    }

    public Novel findNovelById(Long id) {
        return novels.get(id);
    }

    /** 仅返回已发布章节（读者目录） */
    public List<Chapter> findChaptersByNovelId(Long novelId) {
        return chapters.values().stream()
                .filter(c -> c.getNovelId().equals(novelId))
                .filter(c -> STATUS_PUBLISHED.equals(c.getStatus()))
                .sorted(Comparator.comparing(Chapter::getOrderNo))
                .collect(Collectors.toList());
    }

    /** 章节内容仅在已发布时对读者可见 */
    public Chapter findChapterById(Long id) {
        Chapter chapter = chapters.get(id);
        if (chapter == null || !STATUS_PUBLISHED.equals(chapter.getStatus())) {
            return null;
        }
        return chapter;
    }

    // ---------------- 作者工作台查询 ----------------

    public List<Novel> findAllNovelsForAuthor(String keyword) {
        return novels.values().stream()
                .filter(n -> keyword == null || keyword.isEmpty() || n.getTitle().contains(keyword)
                        || n.getDescription().contains(keyword))
                .sorted(Comparator.comparing(Novel::getUpdatedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }

    public List<Chapter> findAllChaptersForAuthor(Long novelId, String status, String keyword) {
        return chapters.values().stream()
                .filter(c -> novelId == null || c.getNovelId().equals(novelId))
                .filter(c -> status == null || status.isEmpty() || status.equalsIgnoreCase(c.getStatus()))
                .filter(c -> keyword == null || keyword.isEmpty() || c.getTitle().contains(keyword))
                .sorted(Comparator.comparing(Chapter::getUpdatedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }

    public long countChapters(Long novelId, String status) {
        return chapters.values().stream()
                .filter(c -> novelId == null || c.getNovelId().equals(novelId))
                .filter(c -> status == null || status.isEmpty() || status.equalsIgnoreCase(c.getStatus()))
                .count();
    }

    public long countNovels() {
        return novels.size();
    }

    /** 全部数据中的最近更新时间（作品与章节一并考虑） */
    public LocalDateTime findLastUpdatedAt() {
        LocalDateTime latest = null;
        for (Novel novel : novels.values()) {
            latest = maxTime(latest, novel.getUpdatedAt());
            latest = maxTime(latest, novel.getCreatedAt());
        }
        for (Chapter chapter : chapters.values()) {
            latest = maxTime(latest, chapter.getUpdatedAt());
            latest = maxTime(latest, chapter.getCreatedAt());
        }
        return latest;
    }

    /** 指定时间（含）之后新增的章节，按创建时间过滤 */
    public List<Chapter> findChaptersCreatedAfter(LocalDateTime since) {
        return chapters.values().stream()
                .filter(c -> c.getCreatedAt() != null && !c.getCreatedAt().isBefore(since))
                .collect(Collectors.toList());
    }

    public String findNovelTitle(Long novelId) {
        Novel novel = novels.get(novelId);
        return novel == null ? null : novel.getTitle();
    }

    // ---------------- 内部工具 ----------------

    private Novel saveNovel(Novel novel) {
        novels.put(novel.getId(), novel);
        return novel;
    }

    private void saveChapter(Chapter chapter) {
        chapters.put(chapter.getId(), chapter);
    }

    private void refreshNovelUpdatedAt(Long novelId) {
        Novel novel = novels.get(novelId);
        if (novel == null) {
            return;
        }
        LocalDateTime latest = novel.getCreatedAt();
        for (Chapter chapter : chapters.values()) {
            if (chapter.getNovelId().equals(novelId)) {
                latest = maxTime(latest, chapter.getUpdatedAt());
                latest = maxTime(latest, chapter.getCreatedAt());
            }
        }
        novel.setUpdatedAt(latest);
    }

    private LocalDateTime maxTime(LocalDateTime a, LocalDateTime b) {
        if (a == null) {
            return b;
        }
        if (b == null) {
            return a;
        }
        return b.isAfter(a) ? b : a;
    }
}
