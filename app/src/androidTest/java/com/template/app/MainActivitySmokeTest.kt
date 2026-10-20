package com.template.app

import android.view.View
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * 冒烟测试：应用能否真的启动并渲染出首页。
 *
 * 这类测试的价值不在"覆盖率"，而在于**拦住"编译通过、单测通过、装上就崩"的问题**——
 * 之前 `StatefulLayout` 的 `<merge />` 崩溃就是这一类：
 * assemble 成功、JVM 单测全绿，装到手机上一启动就 FATAL。
 *
 * 断言方式用 `onActivity { findViewById(...) }` 而不是 Espresso `onView`：
 * 冒烟测试只关心"视图树里有没有、是不是可见"，不需要交互，
 * 少一层 Espresso 的 RESUMED 状态同步，稳定性更好。
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class MainActivitySmokeTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun 启动应用后底部导航与首页容器可见() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val bottomNav = activity.findViewById<View>(R.id.bottom_nav)
                assertNotNull("底部导航未创建，检查 activity_main.xml / Navigation 图", bottomNav)
                assertEquals(View.VISIBLE, bottomNav.visibility)

                // stateLayout 属于首页 Fragment，能在这里找到说明 Fragment 已真实挂载并完成 inflate
                val stateLayout = activity.findViewById<View>(R.id.stateLayout)
                assertNotNull("首页未挂载，可能是 HomeFragment 初始化异常被吞掉", stateLayout)
                assertEquals(View.VISIBLE, stateLayout.visibility)
            }
        }
    }

    @Test
    fun 启动应用后Activity不会立即结束() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                assertTrue(
                    "MainActivity 启动后立即结束，通常是初始化异常被吞掉",
                    !activity.isFinishing,
                )
            }
        }
    }
}
