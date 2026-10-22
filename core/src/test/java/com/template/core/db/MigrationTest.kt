package com.template.core.db

import androidx.room.migration.AutoMigrationSpec
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Room 迁移测试 —— 用 `MigrationTestHelper` 在 **JVM** 上验证，不需要真机/模拟器。
 *
 * 关键点（这套配置踩过坑，照抄即可）：
 * 1. `core/build.gradle.kts` 里把 `schemas/` 挂到 test 的 assets 上，
 *    否则 helper 找不到 `<数据库类全名>/<version>.json`；
 * 2. 必须显式传 `FrameworkSQLiteOpenHelperFactory`，否则 Robolectric 下拿不到 SQLite 实现。
 *
 * 它做的事比"跑一遍不崩"多得多：`runMigrationsAndValidate(..., validateDroppedTables = true)`
 * 会把迁移后的真实表结构与 schema JSON 逐列比对，列顺序、类型、NOT NULL、主键、
 * 索引名任何一处不一致都会失败 —— 这正是手写 `CREATE TABLE` 最容易出错的地方。
 */
@RunWith(RobolectricTestRunner::class)
class MigrationTest {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java,
        emptyList<AutoMigrationSpec>(),
        FrameworkSQLiteOpenHelperFactory(),
    )

    @Test
    fun `v1 升到 v2 后新增收藏与历史表且旧数据保留`() {
        // 1. 造一个 v1 的库（结构由 core/schemas/.../1.json 决定）
        helper.createDatabase(DB_NAME, 1).close()

        // 2. 按当前 MIGRATIONS 升到最新版，并让 Room 校验结构
        val db = helper.runMigrationsAndValidate(DB_NAME, APP_DATABASE_VERSION, true, *AppDatabase.MIGRATIONS)

        // 3. 新表可用：写入后能读回来
        db.execSQL(
            "INSERT INTO favorite (articleId, title, `desc`, url, author, createTime, savedAt) " +
                "VALUES (1, '标题', '描述', 'https://example.com', '作者', '2026-01-01', 1000)",
        )
        db.query("SELECT title FROM favorite WHERE articleId = 1").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals("标题", cursor.getString(0))
        }

        db.execSQL(
            "INSERT INTO history (articleId, title, `desc`, url, author, createTime, viewedAt) " +
                "VALUES (1, '标题', '描述', 'https://example.com', '作者', '2026-01-01', 1000)",
        )
        db.query("SELECT COUNT(*) FROM history").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(1, cursor.getInt(0))
        }

        db.close()
    }

    @Test
    fun `老用户升级后 users 表数据不丢`() {
        helper.createDatabase(DB_NAME, 1).use { v1 ->
            v1.execSQL(
                "INSERT INTO users (user_id, user_name, phone) VALUES ('u-1', '张三', '13800138000')",
            )
        }

        val db = helper.runMigrationsAndValidate(DB_NAME, APP_DATABASE_VERSION, true, *AppDatabase.MIGRATIONS)
        db.query("SELECT user_name FROM users WHERE user_id = 'u-1'").use { cursor ->
            assertTrue("迁移后老数据丢失", cursor.moveToFirst())
            assertEquals("张三", cursor.getString(0))
        }
        db.close()
    }

    private companion object {
        const val DB_NAME = "migration-test.db"
    }
}
