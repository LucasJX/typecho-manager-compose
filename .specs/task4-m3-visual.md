# Task 4: M3 视觉规范统一（圆角 + 阴影 + 颜色 + Spacing）

## 目标
文章页全面应用 Material 3 视觉规范，统一设计语言。

## 项目路径
`/home/flypigs/typecho-manager-compose`

## 1. 圆角系统（统一）
所有组件统一使用以下圆角值：
- **Card / 大容器**：16
- **Button / 按钮**：12
- **Image / 图片**：16
- **Input / 输入框**：12
- **代码块**：12
- **Bottom Sheet**：topLeft: 16, topRight: 16
- **Chip / 胶囊**：全圆 (StadiumBorder)

检查文章页中所有圆角使用，统一为以上值。

## 2. 阴影（极弱）
M3 原则：阴影不要重
- elevation 1~2 即可
- Card 不要超过 elevation 2
- 如果用 Container + boxShadow，opacity 不超过 0.05~0.08
- 深色模式下阴影可以完全去掉

## 3. 颜色系统
- **背景色**：#F7F8FC (light) / #121212 (dark)
- **卡片色**：#FFFFFF (light) / #1C1C1E (dark)
- **文字主色**：#1A1A1A (light) / #FFFFFF (dark)
- **文字次色**：#666666 (light) / #999999 (dark)
- **分割线**：#E8E8E8 (light) / #2C2C2E (dark)
- **主色调**：保留现有的蓝紫渐变（M3 primary）
- **代码块背景**：#1C1C1E (light & dark 统一深色)

## 4. Spacing 8pt System
所有间距统一基于 8 的倍数：
- 最小间距：4 (特殊情况)
- 常用间距：8, 12, 16, 20, 24, 32
- 大间距：40, 48, 56
- 页面水平 padding：20~24 (移动端)
- 卡片内 padding：16
- 段落间距：12~16
- 标题上方间距：24~32 (H2), 16~20 (H3)
- 标题下方间距：12~16 (H2), 8~12 (H3)

**检查清单**：
- [ ] 所有 hardcoded 的间距值是否是 8 的倍数
- [ ] 圆角值是否统一
- [ ] 颜色是否使用 theme 中定义的色值
- [ ] 阴影是否足够弱

## ⚠️ 注意事项
- 这些规范只应用于文章内容页，不要影响其他页面
- 如果发现其他页面也有不一致的地方，记录下来但不修改
- 暗色模式必须同时适配
- 改完必须编译验证通过
- 不要 git push
