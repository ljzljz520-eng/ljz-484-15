package com.novel.repository;

import com.novel.model.Chapter;
import com.novel.model.Novel;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
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
                // Seeding Data
                Novel novel1 = new Novel(novelIdGenerator.getAndIncrement(),
                                "星际穿越之编程大师",
                                "讲述一位程序员意外穿越到未来，用代码拯救宇宙的故事。",
                                "https://images.unsplash.com/photo-1550751827-4bd374c3f58b?q=80&w=800&auto=format&fit=crop",
                                daysAgo(30));
                novels.put(novel1.getId(), novel1);

                seedChapter(novel1.getId(), "第一章：Hello World", 1,
                                "他醒来时，发现眼前只有绿色的代码流...", Chapter.STATUS_PUBLISHED,
                                daysAgo(20), daysAgo(15));
                seedChapter(novel1.getId(), "第二章：变量声明", 2,
                                "“你是谁？”面前的机器人冷冷地问道。“Define me.”他回答。", Chapter.STATUS_PUBLISHED,
                                daysAgo(12), daysAgo(10));
                seedChapter(novel1.getId(), "第三章：循环陷阱", 3,
                                "时间仿佛陷入了死循环，他必须找到 break 的条件，跳出这永无止境的轮回。",
                                Chapter.STATUS_PUBLISHED,
                                daysAgo(3), daysAgo(1));
                seedChapter(novel1.getId(), "第四章：递归之海（草稿）", 4,
                                "函数在深处调用了自己，记忆如潮水般层层叠来，他在递归的海底看见了最初的调用栈。",
                                Chapter.STATUS_DRAFT,
                                daysAgo(2), daysAgo(2));

                Novel novel2 = new Novel(novelIdGenerator.getAndIncrement(),
                                "灵气复苏时代的架构师",
                                "灵气复苏，万物进化。他发现修仙法门竟然符合微服务架构原理。",
                                "https://images.unsplash.com/photo-1518770660439-4636190af475?q=80&w=600&auto=format&fit=crop",
                                daysAgo(25));
                novels.put(novel2.getId(), novel2);

                seedChapter(novel2.getId(), "第一章：单体应用破碎", 1,
                                "天地巨变，世界原本的秩序（Monolith）崩塌了，九州灵气如失控的请求般四散奔涌。",
                                Chapter.STATUS_PUBLISHED,
                                daysAgo(25), daysAgo(25));
                seedChapter(novel2.getId(), "第二章：服务发现", 2,
                                "他感应到了周围的灵气节点，就像注册中心里的服务一样清晰，只需一次心跳，便能定位同道。",
                                Chapter.STATUS_PUBLISHED,
                                daysAgo(2), daysAgo(1));

                Novel novel3 = new Novel(novelIdGenerator.getAndIncrement(),
                                "只有我知道剧情的测试员",
                                "作为世界系统的唯一QA，他能看到由于Bug导致的隐藏剧情。",
                                "https://images.unsplash.com/photo-1555949963-ff9fe0c870eb?q=80&w=800&auto=format&fit=crop",
                                daysAgo(5));
                novels.put(novel3.getId(), novel3);

                seedChapter(novel3.getId(), "第一章：第一个Bug（草稿）", 1,
                                "他在入职第一天就发现，这个世界的登录界面居然可以用空密码绕过。",
                                Chapter.STATUS_DRAFT,
                                daysAgo(2), daysAgo(2));
        }

        private void seedChapter(Long novelId, String title, int orderNo, String content,
                                 String status, LocalDateTime createdAt, LocalDateTime updatedAt) {
                long id = chapterIdGenerator.getAndIncrement();
                chapters.put(id, new Chapter(id, novelId, title, orderNo, content, status, createdAt, updatedAt));
        }

        private static LocalDateTime daysAgo(long days) {
                return LocalDateTime.now().minusDays(days);
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

        public List<Chapter> findChaptersByNovelId(Long novelId) {
                return chapters.values().stream()
                                .filter(c -> c.getNovelId().equals(novelId))
                                .sorted(Comparator.comparing(Chapter::getOrderNo))
                                .collect(Collectors.toList());
        }

        public Chapter findChapterById(Long id) {
                return chapters.get(id);
        }

        // ===== 作者工作台：统计与筛选，全部基于本地数据实时计算 =====

        public long countAllNovels() {
                return novels.size();
        }

        public long countChaptersByStatus(String status) {
                if (status == null) {
                        return chapters.size();
                }
                return chapters.values().stream()
                                .filter(c -> status.equals(c.getStatus()))
                                .count();
        }

        /** 最近一次章节更新时间（优先 updatedAt，缺失时回退 createdAt）。 */
        public LocalDateTime findLatestChapterUpdate() {
                return chapters.values().stream()
                                .map(DataRepository::chapterUpdateTime)
                                .filter(java.util.Objects::nonNull)
                                .max(Comparator.naturalOrder())
                                .orElse(null);
        }

        /** 本周新增字数：创建时间 >= since 的章节字数之和（草稿与已发布均计入）。 */
        public long sumWordCountCreatedSince(LocalDateTime since) {
                return chapters.values().stream()
                                .filter(c -> c.getCreatedAt() != null && !c.getCreatedAt().isBefore(since))
                                .mapToInt(Chapter::getWordCount)
                                .sum();
        }

        /**
         * 作者视角的章节列表：可按状态过滤、可只看本周新增，按更新时间倒序。
         */
        public List<Chapter> findChaptersForAuthor(String status, boolean weeklyOnly,
                                                   LocalDateTime weekStart, int page, int size) {
                return chapters.values().stream()
                                .filter(c -> status == null || status.equals(c.getStatus()))
                                .filter(c -> !weeklyOnly
                                                || (c.getCreatedAt() != null && !c.getCreatedAt().isBefore(weekStart)))
                                .sorted(Comparator.comparing(DataRepository::chapterUpdateTime,
                                                Comparator.nullsLast(Comparator.reverseOrder())))
                                .skip((long) (page - 1) * size)
                                .limit(size)
                                .collect(Collectors.toList());
        }

        public long countChaptersForAuthor(String status, boolean weeklyOnly, LocalDateTime weekStart) {
                return chapters.values().stream()
                                .filter(c -> status == null || status.equals(c.getStatus()))
                                .filter(c -> !weeklyOnly
                                                || (c.getCreatedAt() != null && !c.getCreatedAt().isBefore(weekStart)))
                                .count();
        }

        /** 本周起始：本周一 00:00（ISO 规则，周一为一周第一天）。 */
        public static LocalDateTime currentWeekStart() {
                return java.time.LocalDate.now()
                                .with(java.time.DayOfWeek.MONDAY)
                                .atTime(LocalTime.MIN);
        }

        private static LocalDateTime chapterUpdateTime(Chapter c) {
                return c.getUpdatedAt() != null ? c.getUpdatedAt() : c.getCreatedAt();
        }
}
