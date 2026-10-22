package com.template.app.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.template.app.data.model.Article
import com.template.core.db.AppDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * 收藏 / 历史仓储的行为测试（Robolectric 提供真实 SQLite，不需要真机）。
 *
 * 这些用例盯着两个最容易写错的地方：
 * - `toggle` 的返回值必须等于**切换之后**的状态，否则详情页按钮会慢一拍；
 * - `record` 之后要顺手 `trimTo`，否则浏览历史会无限涨。
 */
@RunWith(RobolectricTestRunner::class)
class FavoriteHistoryRepositoryTest {

    private lateinit var database: AppDatabase
    private lateinit var favoriteRepository: FavoriteRepository
    private lateinit var historyRepository: HistoryRepository

    private fun article(id: Long) = Article(
        id = id,
        title = "标题$id",
        desc = "描述$id",
        url = "https://example.com/$id",
        author = "作者",
        createTime = "2026-01-01",
    )

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        favoriteRepository = FavoriteRepository(database.favoriteDao())
        historyRepository = HistoryRepository(database.historyDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `toggle 返回切换后的状态且与库内一致`() = runTest {
        val target = article(1)

        assertEquals(true, favoriteRepository.toggle(target))
        assertTrue(favoriteRepository.observeIsFavorite(1).first())
        assertEquals(1, favoriteRepository.observeCount().first())

        assertEquals(false, favoriteRepository.toggle(target))
        assertFalse(favoriteRepository.observeIsFavorite(1).first())
        assertEquals(0, favoriteRepository.observeCount().first())
    }

    @Test
    fun `收藏列表按收藏时间倒序返回 Article`() = runTest {
        favoriteRepository.toggle(article(1), now = 100)
        favoriteRepository.toggle(article(2), now = 300)
        favoriteRepository.toggle(article(3), now = 200)

        val ids = favoriteRepository.observeAll().first().map { it.id }
        assertEquals(listOf(2L, 3L, 1L), ids)
    }

    @Test
    fun `重复浏览同一篇只留一条并刷新时间`() = runTest {
        historyRepository.record(article(1), now = 100)
        historyRepository.record(article(1), now = 900)

        val rows = historyRepository.observeAll().first()
        assertEquals(1, rows.size)
        assertEquals(1L, rows.first().id)
    }

    @Test
    fun `浏览历史超过上限时裁掉最旧的`() = runTest {
        val overflow = HistoryRepository.MAX_ENTRIES + 5
        repeat(overflow) { index ->
            historyRepository.record(article(index + 1L), now = index.toLong())
        }

        assertEquals(HistoryRepository.MAX_ENTRIES, historyRepository.observeCount().first())
    }

    @Test
    fun `clear 清空历史但不影响收藏`() = runTest {
        favoriteRepository.toggle(article(1))
        historyRepository.record(article(1))

        historyRepository.clear()

        assertTrue(historyRepository.observeAll().first().isEmpty())
        assertEquals(1, favoriteRepository.observeCount().first())
    }
}
