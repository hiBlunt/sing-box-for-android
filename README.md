# sing-box-for-android-wangwang

`sing-box-for-android-wangwang` 是基于上游 `sing-box-for-android` 的 Android 客户端改造版，用来配合支持 `wangwang` 协议的 `sing-box` 核心 fork 使用。

这个仓库的目标不是重写一套新的 Android 客户端，而是在尽量保持上游 SFA 结构和交互方式的前提下，让 Android 端可以：

- 导入并运行包含 `wangwang` outbound 的本地配置
- 在保留 JSON 编辑器的同时，提供一个最小可用的 `Wangwang Editor`
- 继续复用 SFA 原有的 TUN、DNS、路由、分应用代理和 libbox 集成能力
- 把协议设计、适配过程、AI 协作和可复用 workflow 一起沉淀下来

## 项目定位

- 上游 `sing-box-for-android` 的定向 fork
- 面向 `wangwang` 协议的 Android 适配与交付
- 尽量少改 UI，只补最必要的编辑能力
- 保留 JSON 导入和 JSON 编辑器作为完整能力兜底
- 适合作为研究型开源项目和后续继续维护的基础

## 这次改了什么

当前仓库围绕 `wangwang` 的 Android 适配主要做了两件事：

1. `libbox` 可以识别 `type: "wangwang"` 的配置。
2. 对包含 `wangwang` outbound 的本地配置，额外显示一个简化编辑页，只编辑四个核心字段：
   - `server`
   - `server_port`
   - `password`
   - `method`

这样做的原因很直接：

- JSON 仍然是完整配置的唯一真相来源
- UI 只覆盖最常改、最容易输错的核心字段
- 高级能力如 `dns`、`route`、`fallback`、`tun`、`multiplex` 继续交给 JSON

## 文档入口

- 协议设计初衷、依据、优缺点：
  [docs/protocol-design.md](docs/protocol-design.md)
- Android 端使用方式：
  [docs/usage.md](docs/usage.md)
- AI 协作过程与 skill 固化方法：
  [docs/ai-skill-workflow.md](docs/ai-skill-workflow.md)
- GitHub 开源前检查清单：
  [docs/publishing-checklist.md](docs/publishing-checklist.md)

## 建议的仓库阅读顺序

如果你第一次接触这个项目，建议按下面顺序看：

1. 先读 [docs/protocol-design.md](docs/protocol-design.md)，了解 `wangwang` 为什么这样设计。
2. 再读 [docs/usage.md](docs/usage.md)，了解 Android 端怎么导入和编辑配置。
3. 最后读 [docs/ai-skill-workflow.md](docs/ai-skill-workflow.md)，看这套流程是怎么在 AI 帮助下沉淀成可复用 skill 的。

## 仓库结构

- `app/`: Android 客户端主体代码
- `app/src/main/java/io/nekohasekai/sfa/compose/screen/profile/`: 本次 `Wangwang Editor` 的主要 UI 改动位置
- `app/src/main/res/values*/`: 新增的文案资源
- `docs/`: 面向 GitHub 的项目说明文档
- `config/`, `gradle/`, `third_party/`: 保留上游结构，便于继续同步

## 依赖关系

这个仓库只负责 Android 客户端侧适配。要真正使用 `wangwang`，还需要一个已经把 `wangwang` 编译进 `libbox` 的 `sing-box` 核心 fork。

建议在 GitHub 上把两个仓库成对说明：

- 核心仓库：负责 `wangwang` 协议实现、示例配置、服务端与客户端联调
- Android 仓库：负责 AAR 接入、配置导入、最小化编辑器和 APK 交付

## 关于 AI 协作

这个项目有一条很明确的工程思路：不是让 AI 一次性“生成整个项目”，而是让 AI 参与几个更适合机器协作的环节：

- 整理协议设计问题
- 帮忙映射到 `sing-box` / Android 的代码结构
- 协助排查构建、联调和 UI 接入问题
- 最后把命令、前置条件和验收标准固化成 skill

这部分细节见 [docs/ai-skill-workflow.md](docs/ai-skill-workflow.md)。

## 上游文档

原始 SFA 文档：

https://sing-box.sagernet.org/installation/clients/sfa/

## License

```
Copyright (C) 2022 by nekohasekai <contact-sagernet@sekai.icu>

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.

In addition, no derivative work may use the name or imply association
with this application without prior consent.
```

根据上游许可，fork 后不应继续以原始应用名义在 F-Droid 或其他应用商店发布。
