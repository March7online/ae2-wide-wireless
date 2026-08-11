# AE2LT 天枢样板终端宽屏兼容实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 让 AE2 Lightning Tech 2.0.6 的有线和无线天枢样板终端在 AE2 Wide Wireless Terminal 的 18 列布局中保持完整居中，同时保留窄屏原始布局。

**架构：** 通过 AE2 屏幕样式覆盖把天枢专属控件的宽屏坐标统一增加 81；通过只指向 AE2LT 四个编码面板的客户端 Mixin 修正其硬编码背景绘制坐标。Mixin 使用字符串目标，避免把 AE2LT 变成强制依赖。

**技术栈：** Minecraft 1.21.1 NeoForge、Java 21、Mixin 0.8、AE2 ScreenStyle JSON、PowerShell 回归脚本、Vineflower/javap 字节码检查。

---

## 文件职责

- 创建：`tests/Test-TianshuWideLayout.ps1`，运行真实样式文件并检查天枢宽屏坐标。
- 创建：`src/main/java/dev/codex/ae2widewireless/mixin/TianshuEncodingPanelMixin.java`，只修复 AE2LT 四种天枢编码面板的宽屏背景位置。
- 修改：`src/main/java/dev/codex/ae2widewireless/mixin/MEStorageScreenMixin.java`，为天枢有线/无线 Screen 选择 AE2LT 专属样式路径。
- 修改：`src/main/resources/ae2_wide_wireless.mixins.json`，注册天枢面板 Mixin。
- 创建：`src/main/resources/assets/ae2/screens/terminals/tianshu_pattern_encoding_terminal.json`，覆盖有线天枢宽屏控件坐标。
- 创建：`src/main/resources/assets/ae2/screens/wireless_tianshu_pattern_encoding_terminal.json`，覆盖无线天枢样式并复用有线天枢覆盖。
- 修改：`tests/Test-LayoutRegression.ps1`，加入天枢资源覆盖和专属 Mixin 注册回归检查。

### 任务 1：先写天枢布局红灯测试

**文件：**

- 创建：`tests/Test-TianshuWideLayout.ps1`

- [ ] **步骤 1：编写失败测试**

  使用 `ConvertFrom-Json` 读取两个天枢样式；断言 `modeTabButton2/3/4` 为 254、`modePanel0..5` 为 90、`processingClearPattern` 为 160、`closedLoopPanel` 为 90，并断言 `TianshuEncodingPanelMixin` 注册且拥有四个 AE2LT 目标类名。

- [ ] **步骤 2：运行红灯测试**

  运行：

  ```powershell
  powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\tests\Test-TianshuWideLayout.ps1
  ```

  预期：FAIL，当前工程没有天枢专属覆盖文件，且 Mixin 清单没有天枢面板适配。

### 任务 2：添加天枢宽屏样式覆盖

**文件：**

- 创建：`src/main/resources/assets/ae2/screens/terminals/tianshu_pattern_encoding_terminal.json`
- 创建：`src/main/resources/assets/ae2/screens/wireless_tianshu_pattern_encoding_terminal.json`

- [ ] **步骤 1：以 AE2LT 2.0.6 样式为基线创建覆盖**

  有线样式包含 AE2LT 原始 `terminals/tianshu_pattern_encoding_terminal.json` 的功能字段，并 include 当前宽屏 `terminals/pattern_encoding_terminal.json`；无线样式 include 有线天枢样式和 `wtlib/universal_terminal.json`。

- [ ] **步骤 2：写入宽屏坐标**

  将天枢专属控件的窄屏横坐标统一加 81：模式面板 90、页签 254、处理按钮 160/179/162/175、闭环面板 90、滚动条 96、闭环按钮 139/149/178/205/214、文本框 160。垂直坐标和尺寸保持 AE2LT 2.0.6 原值。

- [ ] **步骤 3：运行布局测试确认仍为红灯或暴露实现缺口**

  运行同一测试，预期坐标断言通过，但 Mixin 注册断言仍失败，确保测试确实分别覆盖资源和实现两层。

### 任务 3：添加天枢面板背景 Mixin

**文件：**

- 创建：`src/main/java/dev/codex/ae2widewireless/mixin/TianshuEncodingPanelMixin.java`
- 修改：`src/main/resources/ae2_wide_wireless.mixins.json`

- [ ] **步骤 1：实现最小 Mixin**

  使用字符串目标指向：

  ```text
  com.moakiee.ae2lt.client.TianshuCraftingEncodingPanel
  com.moakiee.ae2lt.client.TianshuProcessingEncodingPanel
  com.moakiee.ae2lt.client.TianshuSmithingTableEncodingPanel
  com.moakiee.ae2lt.client.TianshuStonecuttingEncodingPanel
  ```

  在 `drawBackgroundLayer` 的常量 `8` 上做 `ModifyConstant`；当传入终端边界宽度大于 195 时返回 89，否则返回原值 8。实现不导入 AE2LT 类型。

- [ ] **步骤 2：注册 Mixin**

  将 `TianshuEncodingPanelMixin` 加入 client mixin 列表，保持其他 Mixin 顺序和配置不变。

- [ ] **步骤 3：运行布局测试确认绿灯**

  运行 `Test-TianshuWideLayout.ps1`，预期完整通过。

### 任务 4：修正屏幕样式路由

**文件：**

- 修改：`src/main/java/dev/codex/ae2widewireless/mixin/MEStorageScreenMixin.java`

- [ ] **步骤 1：添加精确类名路由**

  在 `ae2Wide$getStylePath` 中，将 `TianshuWirelessPatternEncodingTermScreen` 和 `TianshuPatternEncodingTermScreen` 的宽屏路径分别指向 `wireless_tianshu_pattern_encoding_terminal.json` 与 `terminals/tianshu_pattern_encoding_terminal.json`；窄屏路径保持现有 narrow 目录规则。

- [ ] **步骤 2：保留既有终端路由**

  确认 WCT、WET、普通 PatternEncodingTermScreen、CraftingTermScreen 和默认终端分支不改变。

- [ ] **步骤 3：运行现有五项回归测试**

  运行 `Test-LayoutRegression.ps1`、`Test-MagnetRuntime.ps1`、`Test-ManualCraftingBounds.ps1`、`Test-TerminalSelector.ps1`、`Test-UniversalOpenFallback.ps1`。

### 任务 5：编译、打包与最终验证

**文件：**

- 修改：`gradle.properties`，将版本从 `2.1.11` 升至 `2.1.12`。
- 产物：`build/libs/ae2_wide_wireless-neoforge-1.21.1-2.1.12-final.jar`

- [ ] **步骤 1：编译源码**

  运行：

  ```powershell
  .\gradlew.bat compileJava --offline --no-daemon
  ```

- [ ] **步骤 2：执行全部回归测试**

  运行新增天枢测试和现有五项回归测试，确认退出码为 0。

- [ ] **步骤 3：手工发布 JAR**

  使用 `tests/Build-ManualRelease.ps1`，以本次编译类目录和已验证的 2.1.9 基础 JAR 生成 2.1.12；不把 AE2LT JAR 打入发布包。

- [ ] **步骤 4：审核最终 JAR**

  检查 NeoForge `modLoader`/`loaderVersion`、Manifest 版本、Mixin 清单、天枢样式条目、无反斜杠 ZIP 路径、无旧类残留，并用 `javap` 检查新 Mixin 的四个目标类和条件偏移逻辑。

- [ ] **步骤 5：输出 SHA-256 和安装说明**

  交付新 JAR，并要求用户移走旧版本，只保留一个 `ae2_wide_wireless` JAR；若仍有问题，收集最新 `latest.log` 和截图。
