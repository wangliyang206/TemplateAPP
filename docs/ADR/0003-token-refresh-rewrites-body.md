# ADR-0003：Token 刷新必须重写请求体，并做 single-flight

- 状态：已采用
- 日期：2026-10-20

## 背景

后端规范要求 token 放在**请求体** `ApiRequest.token` 中，而不是 `Authorization` 头。

上一版的 `TokenAuthenticator` 有两个问题：

1. 它没被接进 `OkHttpClient`（`RetrofitClient` 只挂了 Header 和 Logging），整条刷新链路是死代码；
2. 实现里刷新成功后 `return response.request` —— 重放的是**原请求体**，token 仍是旧的，
   要么再次 401，要么死循环；同时用 `runBlocking + synchronized + Boolean isRefreshing`，
   并发 401 时第二个线程会直接返回原请求，造成重试风暴。

## 决策

`TokenAuthenticator` 按以下顺序处理 401：

1. 超过 1 次重试 → 放弃（`priorResponse` 链长度判断）；
2. 取失败请求体里的 token `failedToken`；
3. 进入锁：
   - 读本地最新 token，若 `latest != failedToken`，说明别的线程刚刷完 → 直接用 `latest`；
   - 否则调用 `TokenRefresher.refresh(failedToken)`；
4. 刷新成功 → 用 Gson 把请求体 JSON 的 `token` 字段改写为新值，重建 `RequestBody` 后重放；
5. 刷新失败 → `SessionManager.expire()` 清理登录态并广播，由 `App` 统一跳登录页。

## 理由

- token 在 body 里，重放原请求在协议层面就不可能成功；
- "读最新 token 再决定是否刷新"天然实现 single-flight，比布尔标志可靠；
- 401 是**全局**信号，交给 Application 处理，业务页面不需要写登出逻辑。

## 验证

`core/src/test/.../TokenAuthenticatorTest.kt` 覆盖四个场景：
重写请求体、刷新失败清理、无 token 不刷新、其他线程已刷新时复用。

## 遗留

`authenticate()` 是同步 API，读取 DataStore 仍需 `runBlocking`（运行在 OkHttp 线程，不阻塞主线程）。
若后续把 token 缓存进内存（MMKV），可去掉这次阻塞。
