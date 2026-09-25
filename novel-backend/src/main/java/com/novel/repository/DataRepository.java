package com.novel.repository;

import com.novel.model.Chapter;
import com.novel.model.ChapterStatus;
import com.novel.model.Novel;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class DataRepository {
        private final Map<Long, Novel> novels = new ConcurrentHashMap<>();
        private final Map<Long, Chapter> chapters = new ConcurrentHashMap<>();
        private final AtomicLong novelIdGenerator = new AtomicLong(1);
        private final AtomicLong chapterIdGenerator = new AtomicLong(1);

        @PostConstruct
        public void init() {
                // 本周起点：周一 00:00（用于构造确定落在“本周新增”范围内的种子数据）
                LocalDateTime weekStart = LocalDateTime.now()
                                .toLocalDate().atStartOfDay()
                                .minusDays(java.time.DayOfWeek.from(LocalDateTime.now()).getValue() - 1);

                // Seeding Data
                Novel novel1 = new Novel(novelIdGenerator.getAndIncrement(),
                                "星际穿越之编程大师",
                                "讲述一位程序员意外穿越到未来，用代码拯救宇宙的故事。",
                                "https://images.unsplash.com/photo-1550751827-4bd374c3f58b?q=80&w=800&auto=format&fit=crop",
                                weekStart.minusDays(20));
                novels.put(novel1.getId(), novel1);

                addChapter(novel1.getId(), "第一章：Hello World", 1,
                                "他醒来时，发现眼前只有绿色的代码流在黑暗中不断滚动。\n" +
                                "屏幕中央跳出一行古老的提示：欢迎来到新世界。",
                                ChapterStatus.PUBLISHED, weekStart.minusDays(15), weekStart.minusDays(15));
                addChapter(novel1.getId(), "第二章：变量声明", 2,
                                "“你是谁？”面前的机器人冷冷地问道。\n" +
                                "他沉默片刻，回答：“Define me.”",
                                ChapterStatus.PUBLISHED, weekStart.minusDays(8), weekStart.minusDays(2));
                addChapter(novel1.getId(), "第三章：循环陷阱", 3,
                                "时间仿佛陷入了死循环，他必须找到 break 的条件。\n" +
                                "每一次黎明到来，记忆都会重置，只留下指尖残留的代码残影。",
                                ChapterStatus.PUBLISHED, weekStart.plusDays(2), weekStart.plusDays(2));
                addChapter(novel1.getId(), "第四章：递归之梦", 4,
                                "（草稿）他在梦里调用了自己，一层又一层，直到堆栈的尽头出现一扇门。",
                                ChapterStatus.DRAFT, weekStart.plusDays(3), weekStart.plusDays(4));

                Novel novel2 = new Novel(novelIdGenerator.getAndIncrement(),
                                "灵气复苏时代的架构师",
                                "灵气复苏，万物进化。他发现修仙法门竟然符合微服务架构原理。",
                                "https://images.unsplash.com/photo-1518770660439-4636190af475?q=80&w=600&auto=format&fit=crop",
                                weekStart.minusDays(10));
                novels.put(novel2.getId(), novel2);

                addChapter(novel2.getId(), "第一章：单体应用破碎", 1,
                                "天地巨变，世界原本的秩序（Monolith）在灵气潮汐中轰然崩塌。",
                                ChapterStatus.PUBLISHED, weekStart.minusDays(9), weekStart.minusDays(9));
                addChapter(novel2.getId(), "第二章：服务发现", 2,
                                "他感应到了周围漂浮的灵气节点，就像注册中心里的服务实例一样清晰。\n" +
                                "每一个节点都在广播自己的心跳，而他终于读懂了那份心跳协议。",
                                ChapterStatus.PUBLISHED, weekStart.minusDays(5), weekStart.minusDays(1));
                addChapter(novel2.getId(), "第三章：熔断心法", 3,
                                "（草稿）过载之时，断舍离。第三章尚未写完，主角将在限流大阵中悟出熔断器。",
                                ChapterStatus.DRAFT, weekStart.plusDays(4), LocalDateTime.now());

                Novel novel3 = new Novel(novelIdGenerator.getAndIncrement(),
                                "只有我知道剧情的测试员",
                                "作为世界系统的唯一QA，他能看到由于Bug导致的隐藏剧情。",
                                "https://images.unsplash.com/photo-1555949963-ff9fe0c870eb?q=80&w=800&auto=format&fit=crop",
                                weekStart.minusDays(30));
                novels.put(novel3.getId(), novel3);

                addChapter(novel3.getId(), "第一章：第一个缺陷单", 1,
                                "入职第一天，他在世界系统的日志里发现了一条没人看得见的 WARN。",
                                ChapterStatus.PUBLISHED, weekStart.minusDays(28), weekStart.minusDays(28));
        }

        private void addChapter(Long novelId, String title, int orderNo, String content,
                                ChapterStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
                long id = chapterIdGenerator.getAndIncrement();
                chapters.put(id, new Chapter(id, novelId, title, orderNo, content, status, createdAt, updatedAt));
        }

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

        public Collection<Novel> findAllNovels() {
                return novels.values();
        }

        public List<Chapter> findChaptersByNovelId(Long novelId) {
                // 读者视角：仅返回已发布章节
                return chapters.values().stream()
                                .filter(c -> c.getNovelId().equals(novelId))
                                .filter(c -> c.getStatus() == ChapterStatus.PUBLISHED)
                                .sorted(Comparator.comparing(Chapter::getOrderNo))
                                .collect(Collectors.toList());
        }

        public Chapter findChapterById(Long id) {
                return chapters.get(id);
        }

        // ===== 作者工作台查询（包含草稿在内的全量数据） =====

        public List<Chapter> findAllChapters() {
                return chapters.values().stream()
                                .sorted(Comparator.comparing(Chapter::getUpdatedAt,
                                                Comparator.nullsLast(Comparator.reverseOrder())))
                                .collect(Collectors.toList());
        }

        public List<Chapter> findChapters(ChapterStatus status, Long novelId) {
                return chapters.values().stream()
                                .filter(c -> status == null || c.getStatus() == status)
                                .filter(c -> novelId == null || c.getNovelId().equals(novelId))
                                .sorted(Comparator.comparing(Chapter::getUpdatedAt,
                                                Comparator.nullsLast(Comparator.reverseOrder())))
                                .collect(Collectors.toList());
        }
}
