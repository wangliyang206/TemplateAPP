package com.template.core.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.template.core.R

/**
 * 页面状态复用组件：Loading / Empty / Error / Content 四态切换
 *
 * 用法：在 XML 中包裹内容：
 * <StatefulLayout>
 *     <!-- 真正内容 -->
 * </StatefulLayout>
 *
 * 在 Activity/Fragment 中：
 * statefulLayout.showLoading()
 * statefulLayout.showContent()
 * statefulLayout.showEmpty("暂无数据")
 * statefulLayout.showError("加载失败") { retry() }
 */
class StatefulLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    FrameLayout(context, attrs, defStyleAttr) {

    sealed class State {
        object Content : State()
        object Loading : State()
        data class Empty(val message: String) : State()
        data class Error(val message: String, val onRetry: (() -> Unit)? = null) : State()
    }

    private var currentState: State = State.Content
    private var loadingView: StateCardView? = null
    private var emptyView: StateCardView? = null
    private var errorView: StateCardView? = null

    override fun onFinishInflate() {
        super.onFinishInflate()
        require(childCount <= 1) {
            "StatefulLayout 只能包裹一个内容子 View，当前有 $childCount 个。请把多个子 View 包进一个容器里。"
        }
    }

    fun showLoading() {
        switchTo(State.Loading)
    }

    fun showContent() {
        switchTo(State.Content)
    }

    fun showEmpty(message: String = "暂无数据") {
        switchTo(State.Empty(message))
    }

    fun showError(message: String = "加载失败", onRetry: (() -> Unit)? = null) {
        switchTo(State.Error(message, onRetry))
    }

    private fun switchTo(state: State) {
        if (currentState is State.Content && state is State.Content) return
        currentState = state

        // 隐藏所有
        for (i in 0 until childCount) {
            getChildAt(i).visibility = GONE
        }

        when (state) {
            is State.Content -> getChildAt(0)?.visibility = VISIBLE
            is State.Loading -> {
                ensureLoadingView()
                loadingView?.show(R.drawable.state_loading, "加载中...")
            }
            is State.Empty -> {
                ensureEmptyView()
                emptyView?.show(R.drawable.state_empty, state.message)
            }
            is State.Error -> {
                ensureErrorView()
                errorView?.show(R.drawable.state_error, state.message, state.onRetry)
            }
        }
    }

    private fun ensureLoadingView() {
        if (loadingView == null) {
            // 注意：不要 inflate(R.layout.view_state_card) —— 它的根是 <merge>，
            // 只能由 StateCardView 自己的 init 以 attachToRoot=true 的方式填充。
            loadingView = StateCardView(context).also { addView(it) }
        }
        loadingView?.visibility = VISIBLE
    }

    private fun ensureEmptyView() {
        if (emptyView == null) {
            emptyView = StateCardView(context).also { addView(it) }
        }
        emptyView?.visibility = VISIBLE
    }

    private fun ensureErrorView() {
        if (errorView == null) {
            errorView = StateCardView(context).also { addView(it) }
        }
        errorView?.visibility = VISIBLE
    }
}
