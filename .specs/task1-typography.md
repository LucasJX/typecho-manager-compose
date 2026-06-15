# Task 1: Typography 排版系统重构

## 目标
建立完整的中文排版系统，让文章页从"渲染Markdown原文"变成"可阅读的文章"。

## 项目路径
`/home/flypigs/typecho-manager-compose`

## 1. 字体系统（M3标准）
在文章内容页的 theme/text style 中统一设置：

- **正文**：
  - font-size: 16~17sp
  - line-height: 1.8 ~ 2.0 (使用 height 属性)
  - font-weight: 400
  - letter-spacing: 0.2
- **标题分级**：
  - H1: 22~24sp, bold
  - H2: 20sp, bold
  - H3: 18sp, w600

## 2. 段落间距规则
- 每段上下间距 12~16px
- 段落不要贴在一起
- **长段落自动拆分优化（paragraph reflow）**：
  - 如果单段超过 150 个汉字，在合适的位置（句号、分号等标点后）自动拆分为多段
  - 目标：每段不超过 3~4 行屏幕宽度

## 3. 行宽控制
- 移动端每行最多 18~22 个汉字
- 如果内容区域太宽，增加左右 padding
- 目标：正文区域左右 padding 不小于 20dp，不大于 32dp

## 4. 代码块样式
- 背景：黑色或深灰 (#1C1C1E 或 #2D2D2D)
- 圆角：12
- padding: 12~16
- 可横滑（HorizontalChildScrollView）
- 字体：等宽字体（monospace）
- 代码文字颜色：浅灰或绿 (#A8D8A8 或 #E0E0E0)

## 5. 引用块样式
- 左侧竖线（3~4px 宽，M3 primary color 或灰色）
- 背景：浅灰 (#F5F5F5)
- padding: 12 左侧，8 上下
- 字体：italic 或 lighter weight

## 6. 列表样式
- bullet 与文字间距 8px
- 列表项之间间距 8~12px
- 嵌套列表缩进 16~20px

## 7. 标题增强
- H2 自动加底部分割线（1px, 浅灰）
- 或 subtle divider line
- H2 上方间距 24~32px，下方间距 12~16px
- H3 上方间距 16~20px，下方间距 8~12px

## ⚠️ 注意事项
- 不要破坏已有功能（文章加载、图片显示、评论等）
- 只改排版样式，不改数据逻辑
- 改完必须编译验证通过
- 不要 git push
