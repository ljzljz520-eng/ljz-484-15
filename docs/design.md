# 设计文档 (Design)

## 1. 设计理念
本项目遵循“现代、简洁、高级”的设计原则。采用浅色系主题，配合毛玻璃效果 (Glassmorphism) 和优雅的微交互。

## 2. 视觉规范
- **色彩系统**:
  - 主背景色: `#f8fafc` (Slate 50)
  - 强调色: `#6366f1` (Indigo 600)
  - 文字主色: `#1e293b` (Slate 800)
  - 文字辅助色: `#64748b` (Slate 500)
- **字体**:
  - 系统 UI: Inter, sans-serif
  - 阅读正文: Merriweather, serif (提升长文阅读体验)
- **组件样式**:
  - 圆角: 16px (大圆角设计，增强现代感)
  - 阴影: 柔和的扩散阴影，增加层次感。

## 3. 关键页面设计
- **首页**: 沉浸式英雄区 (Hero Section)，双色渐变标题，列表卡片采用悬浮提升效果。
- **详情页**: 动态背景模糊设计，根据小说封面自动生成氛围背景。
- **阅读页**: 经典的“护眼纸质”配色 (`#fcf6e5`)，无干扰布局。

## 4. 接口设计 (RESTful API)
- `GET /api/novels`: 获取小说列表（支持分页与搜索）。
- `GET /api/novels/{id}`: 获取小说详细信息及章节目录（仅已发布章节）。
- `GET /api/chapters/{id}`: 获取具体章节正文内容（草稿章节返回 404）。
- `GET /api/author/stats`: 作者工作台统计：作品数、草稿章节数、已发布章节数、最近更新时间、本周（周一 00:00 起）新增章节数与新增字数。
- `GET /api/author/novels`: 作者视角作品列表，含每部作品的草稿/已发布章节数与总字数。
- `GET /api/author/chapters`: 作者视角章节列表，支持 `novelId`、`status=DRAFT|PUBLISHED`、`thisWeek=true`、`keyword` 过滤与分页。

## 5. 数据模型
- **Novel (小说)**: ID, Title, Description, CoverUrl, CreatedAt, UpdatedAt.
- **Chapter (章节)**: ID, NovelId, Title, OrderNo, Content, Status(DRAFT/PUBLISHED), CreatedAt, UpdatedAt.
- **统计口径**: 字数按正文去除空白字符后的字符数计算；"本周"指本周一 00:00 至今；最近更新时间取作品/章节更新时间的最大值。所有统计均由 Java 后端基于本地内存数据实时计算。
