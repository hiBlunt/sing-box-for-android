# Android 使用方式

## 使用前提

要在当前客户端里使用 `wangwang`，至少要满足两件事：

1. 你已经有一个包含 `wangwang` 协议实现的 `sing-box` 核心 fork。
2. 你已经把对应的 `libbox.aar` / `libbox-legacy.aar` 接进了当前 Android 工程。

如果只改了 Android UI，但底层 `libbox` 里没有 `wangwang`，配置仍然无法真正运行。

## 当前客户端支持的方式

当前 Android 端推荐的使用路径是：

1. 先准备一个完整的 JSON 配置
2. 在 App 中导入这个本地配置
3. 如果配置里存在 `type: "wangwang"` 的 outbound，就会看到 `Wangwang Editor`
4. 用 `Wangwang Editor` 修改最常用字段
5. 需要调整高级能力时，继续使用 JSON 编辑器

## 最小可编辑字段

`Wangwang Editor` 目前只编辑下面四个字段：

- `server`
- `server_port`
- `password`
- `method`

这样做是为了降低 Android 端维护成本，同时保留配置完整性。

## 推荐配置思路

建议把配置职责分成两层：

- JSON 负责完整配置：`dns`、`route`、`fallback`、`tun`、`rule_set`、`multiplex`
- `Wangwang Editor` 负责日常变更最频繁的连接参数

这会比在手机上维护一大段 JSON 更轻松，也比在 App 内补一个不完整的大表单更稳。

## 一个典型的 outbound 片段

下面是一个适合导入到 Android 客户端中的 `wangwang` outbound 示例片段，公开仓库里请始终使用占位值：

```json
{
  "type": "wangwang",
  "tag": "wangwang-out",
  "server": "203.0.113.10",
  "server_port": 443,
  "password": "replace-with-your-password",
  "method": "chacha20-poly1305"
}
```

你可以把它放进完整的 `outbounds` 数组，再配合 `tun`、`dns`、`route` 一起使用。

## 导入步骤

1. 打开 App。
2. 导入本地 JSON 配置。
3. 回到配置详情页，确认配置被识别为本地配置。
4. 如果配置中包含 `wangwang` outbound，会看到 `Wangwang Editor` 入口。
5. 点击进入后修改 `server`、`server_port`、`password`、`method`。
6. 保存后启动配置，并授予 VPN 权限。

## 适合保留在 JSON 的内容

这些内容更适合留在 JSON 中维护：

- `tun` 入站
- 分流规则
- DNS 服务器与规则
- `fallback`
- `rule_set`
- 特殊路由或实验性字段

## 日常使用建议

- Android 的 `Private DNS` 建议设为关闭或确认不会和当前配置冲突。
- 分应用代理建议继续使用 SFA 自带的 per-app proxy 界面，而不是手改 JSON 包名列表。
- 如果服务端没有 IPv6 出口，代理 DNS 建议明确使用 IPv4 策略。
- 初次联调时优先用最简单的单一 `wangwang-out` 路由，确认连通后再逐步补规则。

## 常见问题

### 看不到 Wangwang Editor

通常是以下原因之一：

- 当前配置不是本地配置
- 配置文件不存在或读取失败
- `outbounds` 中没有 `type: "wangwang"`
- 当前 `libbox` 实际上不支持 `wangwang`

### 能导入但启动失败

优先检查：

- `server`、`server_port`、`password`、`method` 是否和服务端一致
- 你的 `libbox` 是否来自带 `wangwang` 的核心构建
- 配置里是否还有与 `dns`、`tun` 或 `route` 相关的错误

### 修改后保存失败

当前编辑器会对保存内容做最基本校验：

- `server` 不能为空
- `server_port` 必须在 `1..65535`
- `password` 不能为空
- `method` 不能为空

如果配置整体非法，底层 `Libbox.checkConfig` 也会拒绝保存。
