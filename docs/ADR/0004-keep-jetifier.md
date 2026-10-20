# ADR-0004：暂时保留 Jetifier

- 状态：已采用（技术债）
- 日期：2026-10-20

## 背景

`gradle.properties` 原本开启 `android.enableJetifier=true`。本项目已完全 AndroidX 化，
理论上可以关闭以加快构建。实际关闭后编译失败：

```
Cannot access 'android.support.v4.view.NestedScrollingParent'
which is a supertype of 'com.scwang.smart.refresh.layout.SmartRefreshLayout'
```

升级到 `io.github.scwang90:refresh-layout-kernel:2.1.1` 后问题依旧 ——
该库至今仍直接引用 support-v4 的类，必须由 Jetifier 重写。

## 决策

保留 `android.enableJetifier=true`，并在 `gradle.properties` 注释里写明原因。

## 后续

替换下拉刷新实现（AndroidX `SwipeRefreshLayout` + 自研 footer）后，把该开关改为 `false`。
替换工作量评估：约 1 个布局 + `ArticleListFragment` 中 `finishRefresh/finishLoadMore` 的状态处理。
