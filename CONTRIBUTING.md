# 贡献指南

本仓库是团队 MVVM 模板项目。改这里 = 改所有新项目的起点，**标准从紧**。

## 开发环境

- JDK 17（Temurin）、Android Studio Ladybug+
- 首次构建前把 `local.properties.example` 复制成 `local.properties` 并填 `sdk.dir`（`local.properties` 不入库）

## 分支与提交

- 远程默认分支是 **`master`**；本地开发在 **`devlop`**，推送时快进到 master：

  ```bash
  git push origin devlop:master
  ```

- 临时改动可开 `feature/xxx`、`fix/xxx`，从 `devlop` 切出，合回 `devlop` 后再推 master
- `origin` 用 SSH（`git@github.com:...`）：本机 HTTPS 推送会卡在凭据管理器
- 提交信息：`type(scope): summary`，type 取 `feat / fix / refactor / docs / test / build / chore`

## 提交前必须本地通过（CI 同样会跑）

```bash
./gradlew detekt spotlessCheck          # 静态检查，0 容忍（maxIssues: 0）
./scripts/check-docs.sh                 # 文档引用的代码路径必须真实存在
./scripts/check-encoding.sh             # 源文件不得带 UTF-8 BOM（会让 @file:Suppress 失效）
./gradlew :core:testDebugUnitTest :app:testDevDebugUnitTest
./gradlew :app:lintDevDebug             # 0 errors, 0 warnings
./gradlew :app:assembleDevDebug
```

> 关于 `check-encoding.sh`：BOM 会让 Kotlin 的 `@file:Suppress(...)` **静默失效**，
> 而且增量构建看不出来（detekt 任务往往是 UP-TO-DATE），只有干净构建才报错。
> 本仓库踩过一次，所以直接做成门禁。

一次通过门禁的快捷方式：

```bash
./gradlew detekt spotlessCheck :core:testDebugUnitTest :app:testDevDebugUnitTest :app:lintDevDebug :app:assembleDevDebug
```

## 改动约定

1. **文档与代码同步**：改了架构、构建配置、数值规范（字号/规格/签名），必须同一次提交里更新
   `README.md` / `docs/` 对应文档；文档过期比没有文档更糟。
2. **新页面**：用 `./scripts/new-feature.sh <name>` 脚手架生成，遵循 UDF（ViewModel 单向状态流）。
3. **新增依赖**：一律进 `gradle/libs.versions.toml` 版本目录，禁止模块内硬编码版本号。
   `api` vs `implementation` 按最小暴露原则选择。
4. **字符串/颜色**：全部走 `strings.xml` / 语义色（`surface`、`text_primary`…），禁止布局硬编码；
   新增中文文案必须在 `values-en/strings.xml` 补英文。
5. **ProGuard**：新增反射/序列化模型时在 `app/proguard-rules.pro` 用 `**.data.model.**` 这类
   通配规则，不要写死包名（开新项目改名后规则会失效）。
6. **签名**：debug key 入库（保证跨机器一致），release key 永不入库，见 `docs/SIGNING.md`。

## PR 检查清单

- [ ] 静态检查 / 单测 / lint / assemble 全绿
- [ ] 文档已同步（含 `docs/CHECKLIST.md` 中受影响项）
- [ ] 新增资源无水印、无硬编码中文
- [ ] `CHANGELOG.md` 已记录用户可感知的变化
