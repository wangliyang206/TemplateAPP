# ADR-0001：不默认引入 domain（UseCase）层

- 状态：已采用
- 日期：2026-10-20

## 背景

Clean Architecture 的教科书结构是 `presentation → domain → data`。
上一版模板在 README 里被建议"补齐 UseCase"，但代码里并不存在。

## 决策

模板默认只有两层：`presentation（ViewModel）→ data（Repository → DataSource）`。
只有当**同一业务规则被 2 个以上页面复用**，或需要组合多个数据源且组合逻辑会变化时，
才在对应 feature 内新增 `domain/`（UseCase + 领域模型）。

## 理由

- 强制 Clean 的典型产物是 `UseCase` 直通车：`class GetUserUseCase(repo) { suspend fun invoke() = repo.getUser() }`，
  没有不变量、没有组合逻辑，只增加一层跳转和一次文件创建；
- 模板的信噪比决定新人上手速度。让正确写法成为**最短路径**，比让结构看起来"标准"更重要；
- 需要时再引入是增量的，不需要重构已有页面。

## 后果

- ViewModel 直接依赖 Repository，Repository 承担「数据编排」职责；
- 业务规则出现重复时，必须提 UseCase，禁止复制粘贴到第二个 ViewModel。
