# 终端布局回归修复实现计划

> **面向 AI 代理的工作者：** 在当前会话中按测试驱动方式逐任务执行；项目不是 Git 仓库，使用独立编译和打包目录替代 worktree/commit 检查点。

**目标：** 修复天枢终端宽窄切换与重开错位，移除无线通用终端的宽窄按钮，并校正双布局合成区和人物装备区。

**架构：** 天枢的目标样式在其构造器调用 `MEStorageScreen` 前选定，避免先按冲突资源初始化、再在同一界面上补救。通用终端通过独立屏幕策略识别 AE2WTLib 的 WCT/WET 通用模式；视觉坐标使用 AE2 原生样板背景和 AE2WTLib 无线合成终端作为基准。

**技术栈：** Java 21、NeoForge 1.21.1、Mixin、AE2 19.2.17、AE2WTLib 19.5.1、PowerShell 回归脚本。

---

### 任务 1：天枢样式在构造阶段确定

**文件：**
- 创建：`src/main/java/dev/codex/ae2widewireless/TerminalWidthState.java`
- 创建：`src/main/java/dev/codex/ae2widewireless/mixin/TianshuInitialStyleMixin.java`
- 修改：`src/main/java/dev/codex/ae2widewireless/mixin/TianshuPatternEncodingScreenMixin.java`
- 修改：`src/main/java/dev/codex/ae2widewireless/mixin/MEStorageScreenMixin.java`
- 测试：`tests/Test-TianshuWideLayout.ps1`
- 测试：`tests/Test-TerminalScreenPolicy.ps1`

- [ ] 添加失败检查：要求天枢构造器的 `MEStorageScreen.<init>` 参数在调用前替换，且初始 `init` 不再排队二次套用天枢样式。
- [ ] 运行专项测试，确认因缺少构造阶段样式选择而失败。
- [ ] 添加共享宽窄状态和天枢私有样式路径选择，在构造阶段传入正确样式。
- [ ] 运行天枢专项测试，确认宽/窄、有线/无线四条路径均被覆盖。

### 任务 2：无线通用终端固定宽度

**文件：**
- 创建：`src/main/java/dev/codex/ae2widewireless/TerminalScreenPolicy.java`
- 修改：`src/main/java/dev/codex/ae2widewireless/mixin/MEStorageScreenMixin.java`
- 测试：`tests/java/dev/codex/ae2widewireless/TerminalScreenPolicyTest.java`
- 测试：`tests/Test-TerminalScreenPolicy.ps1`

- [ ] 添加失败测试：重写通用终端始终固定宽度；WCT/WET 仅在 `isWUT=true` 时固定；普通无线合成、样板和天枢终端仍保留切换。
- [ ] 运行测试，确认缺少策略类而失败。
- [ ] 用策略结果统一控制宽窄按钮创建和记忆样式应用。
- [ ] 运行策略测试，确认全部分支通过。

### 任务 3：双布局像素与人物模块

**文件：**
- 修改：`src/main/java/dev/codex/ae2widewireless/client/WideUniversalScreen.java`
- 修改：`src/main/resources/assets/ae2/screens/ae2_wide_wireless/wide_universal_terminal.json`
- 修改：`tests/Test-ManualCraftingBounds.ps1`
- 修改：`tests/Test-LayoutRegression.ps1`

- [ ] 添加失败检查：合成背景必须位于第一个槽位左侧 7 像素；人物模块采用无线合成终端的 `8/26/73` 横坐标、18 像素装备间距和 30 倍实体渲染。
- [ ] 运行两个布局测试，确认旧坐标失败。
- [ ] 将合成背景移到 `x=8`；直接使用 AE2WTLib 的 `PlayerEntityWidget`，并重排装备、副手和人物背景。
- [ ] 运行两个布局测试，确认通过。

### 任务 4：构建与发布验证

**文件：**
- 修改：`gradle.properties`
- 产物：`build/libs/ae2_wide_wireless-neoforge-1.21.1-2.1.19.jar`

- [ ] 将版本更新为 `2.1.19`。
- [ ] 使用 Java 21 和 `build/manual-compile-libs-2.1.17b` 全源码编译到全新的类目录。
- [ ] 运行全部专项与既有回归测试。
- [ ] 用独立 stage 打包，检查版本、Mixin 类、资源路径、ZIP 分隔符和文件名不含 `final`。
- [ ] 计算 SHA-256 并交付可安装 JAR。
