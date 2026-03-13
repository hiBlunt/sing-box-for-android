# GitHub 开源前检查清单

## 文档层

- README 已明确说明这是上游 `sing-box-for-android` 的 `wangwang` 适配 fork
- README 已给出协议说明、使用方式、AI 协作和 skill 文档入口
- 文档中没有把它描述成“万能规避方案”或过度承诺的产品
- 文档里已经区分“核心仓库”和“Android 仓库”的职责

## 代码与仓库层

- 当前改动范围尽量集中在 `wangwang` 适配相关文件
- 没有把本地产物、临时文件、备份文件一起提交
- `.gitignore` 已覆盖常见备份文件
- 没有把调试日志、抓包结果、私有脚本误提交到仓库

## 配置与敏感信息

- 没有提交真实服务器 IP、域名、端口和密码
- 没有提交真实 fallback 站点信息
- 示例 JSON 使用的是公开占位值
- 如果 `app/release.keystore` 是私有签名文件，公开前已确认处理方式

## 构建与验证

- Android 工程可以正常同步
- 目标 APK 至少完成过一次 debug 构建
- 本地配置能导入
- 含 `wangwang` outbound 的配置会显示 `Wangwang Editor`
- 编辑 `server`、`server_port`、`password`、`method` 后可以保存
- 启动配置后能正常拉起 VPN 并完成连通验证

## GitHub 展示层

- 仓库名、描述和 README 标题一致
- 准备好一段简短仓库简介，说明这是 `wangwang` Android 适配 fork
- 准备好 companion core repo 的链接
- 如果准备发 release，先确认签名、文件名和发布说明

## 推荐发布顺序

1. 先清理敏感信息和签名文件问题。
2. 再整理 README 与 `docs/`。
3. 然后做一次最小联调验证。
4. 最后再推送 GitHub 并补 release 说明。
