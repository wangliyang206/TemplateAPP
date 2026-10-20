# ADR-0006：登录态为什么有两个入口（core 的 SessionManager 与 app 的 SessionRepository）

- 状态：已采用
- 日期：2026-10-20

## 背景

复核审计指出「登录态写入有两条路径」：`SessionManager.expire()` 清 token、
`SessionRepository.save/clear()` 写 DataStore，底层是同一份 DataStore。

有人建议合并成一个入口。

## 决策

**保留两个入口，但明确分工与不变量**：

| 类 | 模块 | 职责 | 谁调用 |
|---|---|---|---|
| `SessionManager` | core | **token 生命周期**：401 刷新失败时清 token、广播 `expired` | 只由 `TokenAuthenticator` 调用 |
| `SessionRepository` | app | **本地会话数据**：登录/注册成功写入、登出清除（DataStore + Room） | LoginRepository / RegisterRepository / MineViewModel |

不变量（两条路径共同维护）：

1. **token 是登录态的唯一权威**：`Session.isLoggedIn == token.isNotEmpty()`。
   `expire()` 清掉 token 后，无论 Room 里是否还留有用户资料，页面一律视为未登录。
2. **core 不知道 app**：SessionRepository 依赖 Room 与业务 User 模型，
   把它下沉进 core 会引入反向依赖，模块边界就破了。

## 理由

- 两者响应的是不同事件：一个是**网络层的失效信号**（被动、全局、在 OkHttp 线程触发），
  一个是**业务层的会话操作**（主动、页面驱动）。强行合并要么让 core 依赖业务模型，
  要么让 401 处理绕道 app 层（`runBlocking` + 回调，复杂度更高）。
- 该决策由 Konsist 架构测试间接守护：UI 层禁止 import 网络栈与 api 包，
  ViewModel 不许直连 ApiService，登录态的读写只能经由这两个入口。

## 后果

- 新人需要理解「为什么不是只有一个类」——本文档即答案；
- 新增登录态字段时：`SessionRepository.save()` 与 `Session` 数据类同步修改，
  `SessionManager` 不感知字段；
- 如果未来 token 迁移出 DataStore（例如内存 + 加密存储），只改 core 的 `TokenStore`，
  app 层无感知。
