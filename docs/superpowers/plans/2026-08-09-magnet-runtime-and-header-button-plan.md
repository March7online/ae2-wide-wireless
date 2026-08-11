# 磁力功能与顶部按钮实现计划

> **面向 AI 代理的工作者：** 在当前会话中逐项执行，并对每项运行回归验证。

**目标：** 修复双布局无线通用终端的磁力缓存选择，并把磁力按钮缩小后放到搜索框左侧。

**架构：** 通过一个最小 Mixin 在 AE2WTLib 为新打开的 `WideUniversalMenu` 查找终端时清除定位器和磁力宿主缓存。客户端继续使用 AE2WTLib 原生 `IconButton`，但改为普通 16×16 背景并由样式固定到顶部。

**技术栈：** Java 21、NeoForge 21.1、Mixin、AE2 19.2、AE2WTLib 19.5、PowerShell 回归测试。

---

### 任务 1：建立失败回归测试

**文件：**
- 修改：`tests/Test-LayoutRegression.ps1`

- [ ] 添加断言：样式中存在 `magnetCardMenuButton`，位置为 `(223, 2)`，尺寸为 `16×16`，与搜索框相隔 2 像素。
- [ ] 添加字节码/结构断言：最终类包含磁力缓存刷新桥接，并且 Mixin 配置注册该桥接。
- [ ] 运行 `powershell -ExecutionPolicy Bypass -File tests/Test-LayoutRegression.ps1`，确认新断言在当前 2.1.4 实现上失败。

### 任务 2：实现磁力缓存刷新与按钮迁移

**文件：**
- 创建：`src/main/java/dev/codex/ae2widewireless/mixin/CraftingTerminalHandlerMixin.java`
- 修改：`src/main/resources/ae2_wide_wireless.mixins.json`
- 修改：`src/main/java/dev/codex/ae2widewireless/client/WideUniversalScreen.java`
- 修改：`src/main/resources/assets/ae2/screens/ae2_wide_wireless/wide_universal_terminal.json`

- [ ] 新菜单第一次查找终端时清除 `locator`、`menuHost` 和 `magnetHost`，同一菜单不重复清除。
- [ ] 将按钮从 `addToLeftToolbar` 改为 `widgets.add("magnetCardMenuButton", ...)`，使用 16×16 原生按钮背景。
- [ ] 在样式中设置按钮坐标和尺寸。
- [ ] 编译 Java 21 源码并运行回归测试，确认通过。

### 任务 3：版本、打包与最终验证

**文件：**
- 修改：`gradle.properties`
- 修改：`README.md`
- 生成：`build/libs/ae2_wide_wireless-neoforge-1.21.1-2.1.5.jar`

- [ ] 将版本提升至 2.1.5，并补充变更说明。
- [ ] 使用全新暂存目录打包 JAR。
- [ ] 解包最终 JAR，验证版本、Mixin、按钮样式和所需类。
- [ ] 计算 SHA-256，并进行只读代码审查。
