# AE2 Wide Wireless Terminal Client（NeoForge 1.21.1）

这是 `AE2 Wide Wireless Terminal` 的纯客户端版本。服务器无需安装本 Mod。

Client-only edition. No server installation is required.

## 保留的功能

- 将受支持的 AE2、AE2WTLib 终端扩展为 18 列宽屏。
- 其他受支持终端保留宽窄切换。
- 保留顶部的物品/流体显示切换按钮。
- 保留无线合成终端、无线样板编码终端、EAEP 与天枢样板终端的客户端布局兼容。
- 无线通用终端继续使用 AE2WTLib 原生的当前终端界面，并保持固定宽度，不显示本 Mod 的宽窄按钮。

## 与完整版的差异

纯客户端无法安全地新增服务器容器槽位或菜单协议，因此本分支不包含完整版的无线通用终端双工作区、双区域 JEI 拉取、自定义合成区、磁力缓存刷新和自定义垃圾桶入口。

客户端版与完整版使用相同的 Mod ID，不能同时安装。需要双布局功能时，请使用 `main` 分支的完整版，并在客户端和服务器两端安装。

The client-only and full editions cannot be installed at the same time.

## 依赖

- Minecraft 1.21.1
- NeoForge 21.1.215 或更高的 21.1.x 版本
- Applied Energistics 2 19.2.17
- AE2WTLib 19.5.1
- JEI 19.x（可选）

AE2 与 AE2WTLib 的版本保持固定，因为本 Mod 会适配它们在 Minecraft 1.21.1 下的具体客户端界面 API。
