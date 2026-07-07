package com.template.core.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
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
class StatefulLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

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

    private val inflater = LayoutInflater.from(context)

    override fun onFinishInflate() {
        super.onFinishInflate()
        // 确保子 view 数量 ≤ 1（即内容本身）
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
            loadingView = inflater.inflate(R.layout.view_state_card, this, false) as StateCardView
            addView(loadingView)
        }
        loadingView?.visibility = VISIBLE
    }

    private fun ensureEmptyView() {
        if (emptyView == null) {
            emptyView = inflater.inflate(R.layout.view_state_card, this, false) as StateCardView
            addView(emptyView)
        }
        emptyView?.visibility = VISIBLE
    }

    private fun ensureErrorView() {
        if (errorView == null) {
            errorView = inflater.inflate(R.layout.view_state_card, this, false) as StateCardView
            addView(errorView)
        }
        errorView?.visibility = VISIBLE
    }
}
