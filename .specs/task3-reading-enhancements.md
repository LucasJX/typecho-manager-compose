# Task 3: 阅读增强功能（进度条 + TOC + Lightbox）

## 目标
为文章阅读体验添加三个核心增强功能。

## 项目路径
`/home/flypigs/typecho-manager-compose`

## 1. 阅读进度条（Scroll Progress Indicator）
- 位置：页面最顶部（在 AppBar 上方或与 AppBar 同层）
- 样式：细线（2~3dp 高）
- 颜色：M3 primary color（你的蓝紫渐变色）
- 行为：随页面滚动从 0% 到 100%
- 实现：用 NotificationListener<ScrollNotification> 监听滚动，计算 scrollExtent 和 pixels 的比例

## 2. 图片 Lightbox（点击放大 + 滑动切换）
- 正文中的图片：点击后全屏显示
- 全屏模式支持：
  - 双指缩放（InteractiveViewer）
  - 左右滑动切换上下图片（PageView）
  - 双击回到原始大小
  - 下滑关闭（Dismissible 或手势）
  - 顶栏显示：当前第几张 / 总共几张
- **不要影响正文中的图片正常显示**

## 3. 自动目录 TOC（Table of Contents）
- 从正文中提取 H2/H3 标题，生成目录
- **触发方式**：页面右下角浮动按钮（小图标，如 list icon）
- **点击后**：从右侧滑出 Drawer 或 Bottom Sheet 显示目录
- **目录项**：
  - H2 项：正常显示
  - H3 项：缩进 16px
  - 点击目录项：滚动到对应位置（用 ScrollController + GlobalKey）
- **可选**：当前阅读位置高亮

## ⚠️ 注意事项
- 这三个功能互相独立，可以分别实现
- 进度条不能遮挡内容
- Lightbox 不能影响正常图片加载
- TOC 需要解析文章 HTML/Markdown 中的标题
- 改完必须编译验证通过
- 不要 git push
