# ADR-0005：序列化继续用 Gson，暂不迁 kotlinx.serialization

- 状态：已采用（待观察）
- 日期：2026-10-20

## 背景

kotlinx.serialization 的优势明确：无反射、R8 友好、支持默认值、可多平台。
但本项目的接口规范里有一个特殊约束：

```
sign = MD5(json(data) + "&key=" + secret)
```

签名是对**业务参数 `data` 的 JSON 表达**做哈希，由 `SignUtils` 在构建 `ApiRequest` 时计算；
而这个 JSON 最终又由请求体序列化器输出。两者必须是**同一种序列化结果**（字段名、null 处理一致），
否则后端验签失败。

## 决策

保持 Gson（`AppRequest` 构建 + Retrofit converter 共用同一个 `Gson` 实例）。
不迁移 kotlinx.serialization。

## 理由

- 迁移需要让 `SignUtils` 与 converter 共用同一套 `Json` 配置，而 `ApiRequest.build(data: T)`
  是泛型调用点（Repository / PagingSource / Mock 共 8 处），
  改为 `reified + 显式 serializer` 会贯穿全部调用点，收益（体积/性能）不足以抵消回归风险；
- Gson 下 R8 只需保留模型字段名，已在 `proguard-rules.pro` 与 `core/consumer-rules.pro` 中收敛；
- 迁移的**前置条件**是后端支持对 header/URI 签名，届时 `data` 的 JSON 表达不再参与签名，
  迁移成本会降到「换 converter + 加注解」。

## 后果

- 模型类需要保留字段名（已配置 keep 规则），新增模型忘记加规则会导致 release 包解析失败；
- 每次新增数据模型，必须在 `assembleProdRelease` 后用真机验证一次接口连通性。
