# 贡献指南

本仓库是团队 MVVM 模板项目。改这里 = 改所有新项目的起点，**标准从紧**。

## 开发环境

- JDK 17（Temurin）、Android Studio Ladybug+
- 首次构建前复制 `local.properties` 配置 SDK 路径（不入库）

## 分支与提交

- 分支：`feature/xxx`、`fix/xxx`，从 `develop` 切出，PR 回 `develop`
- 提交信息：`type(scope): summary`，type 取 `feat / fix / refactor / docs / test / build / chore`

## 提交前必须本地通过（CI 同样会跑）

```bash
./gradlew detekt spotlessCheck          # 静态检查，0 容忍（maxIssues: 0）
./gradlew :core:testDebugUnitTest :app:testDevDebugUnitTest
./gradlew :app:lintDevDebug             # 0 errors, 0 warnings
./gradlew :app:assembleDevDebug
```

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
