# ADR-0002：BaseViewModel 只提供能力，不规定状态形状

- 状态：已采用
- 日期：2026-10-20

## 背景

上一版 `BaseViewModel` 暴露 `launchWithState(flow)`，把 `UiState<T>` 固定为基类约定。
结果是三类页面各自绕开它：

| 页面 | 绕开方式 |
|---|---|
| 首页 | 不继承基类，直接注入 ApiService，用 LiveData + postValue |
| 文章列表（Paging） | 不继承基类，直接暴露 `Flow<PagingData>` |
| 我的 | 继承基类但完全不用基类方法，自己 `stateIn` |

一个模板里出现三种写法，是比"没有基类"更糟的状态。

## 决策

`BaseViewModel` 只提供三样东西：

1. `MutableStateFlow<UiState<T>>.launchWithState { }` —— **扩展函数**，谁需要谁用；
2. `launchCatching { }` —— 只处理失败的一次性任务；
3. `effects: SharedFlow<UiEffect>` —— 一次性副作用通道。

不定义 `uiState` 字段，不强制单状态；Paging 页面可以只用 `Flow<PagingData>`。

## 理由

- 状态形状是页面级决策：列表页是 `UiState<List<T>>`，分页页是 `PagingData`，表单页是字段级校验；
- 用组合替代继承后，"不适用"的场景不再需要绕开基类，范式自然统一。

## 后果

- 新页面必须自己声明 `_state`（3 行样板），换来的是每个页面的状态都能精确表达；
- 错误处理仍然统一：`launchWithState` 内部把异常转成 `AppError → UiMessage`，页面只负责渲染。
