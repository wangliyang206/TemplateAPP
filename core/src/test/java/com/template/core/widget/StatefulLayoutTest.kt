package com.template.core.widget

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * StatefulLayout 回归测试。
 *
 * 背景：`view_state_card.xml` 的根节点是 `<merge>`，只能由 [StateCardView] 自己的 init
 * 以 attachToRoot=true 填充。曾经这里写成
 * `inflater.inflate(R.layout.view_state_card, this, false) as StateCardView`，
 * 导致真机启动即崩：
 * `InflateException: <merge /> can be used only with a valid ViewGroup root and attachToRoot=true`。
 *
 * 这个崩溃编译期查不出来、普通 JVM 单测也查不出来（因为不 inflate 布局），
 * 所以必须有 Robolectric 这类"真实资源 + 真实 inflate"的测试兜底。
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class StatefulLayoutTest {

    private fun newLayout(): StatefulLayout {
        val context: Context = ApplicationProvider.getApplicationContext()
        return StatefulLayout(context).apply {
            addView(TextView(context).apply { text = "content" })
        }
    }

    @Test
    fun `showLoading 能真实 inflate 状态卡片且不抛异常`() {
        val layout = newLayout()

        layout.showLoading()

        // 内容 + loading 卡片
        assertEquals(2, layout.childCount)
        assertTrue(layout.getChildAt(0).visibility == View.GONE)
        assertTrue(layout.getChildAt(1).visibility == View.VISIBLE)
    }

    @Test
    fun `showEmpty 复用同一个卡片实例不会重复添加子 View`() {
        val layout = newLayout()

        layout.showEmpty("暂无数据")
        layout.showEmpty("还是空的")

        assertEquals(2, layout.childCount)
    }

    @Test
    fun `showError 与 showLoading 使用不同卡片`() {
        val layout = newLayout()

        layout.showLoading()
        layout.showError("加载失败")
        layout.showLoading()

        // 内容 + loading + error，各一份
        assertEquals(3, layout.childCount)
        val visible = (0 until layout.childCount).filter { layout.getChildAt(it).visibility == View.VISIBLE }
        assertEquals(1, visible.size)
        assertEquals(1, visible.first()) // loading 卡片
    }

    @Test
    fun `showContent 恢复内容并隐藏所有状态卡片`() {
        val layout = newLayout()

        layout.showLoading()
        layout.showContent()

        val visible = (0 until layout.childCount).filter { layout.getChildAt(it).visibility == View.VISIBLE }
        assertEquals(listOf(0), visible)
    }
}
