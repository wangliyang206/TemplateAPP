package com.template.core.db

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
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
 * 收藏 / 浏览历史 DAO 的行为测试。
 *
 * 这里验的都是**业务规则**，不是 Room 会不会用：
 * - `REPLACE` 冲突策略让"同一篇文章看两次"只留一行；
 * - `trimTo` 的上限裁剪只留最近的 N 条；
 * - `COUNT(*)` 是 Flow —— 「我的」页的数字要能随写入自动变。
 *
 * 故意用 `Flow.first()` 而不是 `collect`：Room 的 Flow 首次发射就是当前快照，
 * 取一次足够，测试不必和协程调度器纠缠。
 */
@RunWith(RobolectricTestRunner::class)
class FavoriteHistoryDaoTest {

    private lateinit var database: AppDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    private fun favorite(id: Long, savedAt: Long) = FavoriteEntity(
        articleId = id,
        title = "标题$id",
        desc = "描述$id",
        url = "https://example.com/$id",
        author = "作者",
        createTime = "2026-01-01",
        savedAt = savedAt,
    )

    private fun history(id: Long, viewedAt: Long) = HistoryEntity(
        articleId = id,
        title = "标题$id",
        desc = "描述$id",
        url = "https://example.com/$id",
        author = "作者",
        createTime = "2026-01-01",
        viewedAt = viewedAt,
    )

    @Test
    fun `收藏列表按收藏时间倒序`() = runTest {
        val dao = database.favoriteDao()
        dao.insert(favorite(1, savedAt = 100))
        dao.insert(favorite(2, savedAt = 300))
        dao.insert(favorite(3, savedAt = 200))

        assertEquals(listOf(2L, 3L, 1L), dao.observeAll().first().map { it.articleId })
    }

    @Test
    fun `收藏状态与数量随写入变化`() = runTest {
        val dao = database.favoriteDao()
        assertFalse(dao.observeIsFavorite(1).first())
        assertEquals(0, dao.observeCount().first())

        dao.insert(favorite(1, savedAt = 100))
        assertTrue(dao.observeIsFavorite(1).first())
        assertTrue(dao.isFavorite(1))
        assertEquals(1, dao.observeCount().first())

        dao.deleteById(1)
        assertFalse(dao.observeIsFavorite(1).first())
        assertFalse(dao.isFavorite(1))
        assertEquals(0, dao.observeCount().first())
    }

    @Test
    fun `同一篇文章浏览两次只保留一行并更新时间`() = runTest {
        val dao = database.historyDao()
        dao.insert(history(1, viewedAt = 100))
        dao.insert(history(1, viewedAt = 500))

        val rows = dao.observeAll().first()
        assertEquals(1, rows.size)
        assertEquals(500, rows.first().viewedAt)
    }

    @Test
    fun `trimTo 只保留最近 N 条`() = runTest {
        val dao = database.historyDao()
        repeat(5) { index -> dao.insert(history(index + 1L, viewedAt = index * 100L)) }

        dao.trimTo(keep = 2)

        // 剩下的应该是 viewedAt 最大的两条：id 5(400) 与 id 4(300)
        assertEquals(listOf(5L, 4L), dao.observeAll().first().map { it.articleId })
        assertEquals(2, dao.observeCount().first())
    }

    @Test
    fun `clear 清空全部历史`() = runTest {
        val dao = database.historyDao()
        dao.insert(history(1, viewedAt = 100))
        dao.insert(history(2, viewedAt = 200))

        dao.clear()

        assertTrue(dao.observeAll().first().isEmpty())
    }
}
