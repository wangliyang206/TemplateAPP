package com.template.core.db

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File

/**
 * Room 的 JVM 测试（Robolectric 提供 SQLite 与 Context，不需要真机）。
 *
 * 覆盖两件事：
 * 1. DAO 的读写语义（REPLACE 冲突策略、按 id 删除、清空）；
 * 2. **schema 导出与版本号是否同步** —— 这是最容易出的岔子：
 *    改了 `@Database(version = N)` 却忘了让 KSP 重新导出 schema，
 *    下次写迁移时就没有基线可比对，线上迁移直接崩。
 */
@RunWith(RobolectricTestRunner::class)
class AppDatabaseTest {

    private lateinit var database: AppDatabase

    private val user = UserEntity(userId = "u-1", userName = "张三", phone = "13800138000")

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

    @Test
    fun `插入后可按 id 查询`() = runTest {
        database.userDao().insert(user)

        assertEquals(user, database.userDao().getById("u-1"))
    }

    @Test
    fun `同一 userId 再次插入是覆盖而不是新增`() = runTest {
        database.userDao().insert(user)
        database.userDao().insert(user.copy(userName = "李四"))

        assertEquals(1, database.userDao().getAll().size)
        assertEquals("李四", database.userDao().getById("u-1")?.userName)
    }

    @Test
    fun `删除与清空`() = runTest {
        database.userDao().insert(user)
        database.userDao().insert(user.copy(userId = "u-2"))

        database.userDao().deleteById("u-1")
        assertNull(database.userDao().getById("u-1"))

        database.userDao().clear()
        assertTrue(database.userDao().getAll().isEmpty())
    }

    @Test
    fun `导出的 schema 与当前数据库版本一一对应`() {
        val schemaFile = listOf(
            File("schemas/${AppDatabase::class.java.name}/$APP_DATABASE_VERSION.json"),
            File("core/schemas/${AppDatabase::class.java.name}/$APP_DATABASE_VERSION.json"),
        ).firstOrNull { it.exists() }

        assertTrue(
            "缺少 v$APP_DATABASE_VERSION 的 schema 导出（core/schemas/${AppDatabase::class.java.name}/）。" +
                "升版本后请重新执行 KSP 构建并提交 schemas 目录。",
            schemaFile != null,
        )

        if (APP_DATABASE_VERSION > 1) {
            assertTrue(
                "版本已升到 $APP_DATABASE_VERSION，但 MIGRATIONS 为空，禁止无迁移升级",
                AppDatabase.MIGRATIONS.isNotEmpty(),
            )
        }
    }
}
