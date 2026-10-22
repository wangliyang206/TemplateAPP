package com.template.core.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * 数据库版本号的单一真相源。
 *
 * `@Database(version = ...)` 与迁移测试（`AppDatabaseTest`）都引用这里，
 * 升版本时只改这一处，避免"注解改了、测试还在校验旧版本"。
 */
const val APP_DATABASE_VERSION = 2

/**
 * 应用数据库。
 *
 * 规范：
 * - `exportSchema = true`，schema 输出到 core/schemas，升级前先 diff 再写迁移；
 * - **禁止 fallbackToDestructiveMigration**：线上升级会清空用户数据；
 * - 新增版本时在 [MIGRATIONS] 中追加，并补一条迁移测试（Room 的 MigrationTestHelper）。
 */
@Database(
    entities = [UserEntity::class, FavoriteEntity::class, HistoryEntity::class],
    version = APP_DATABASE_VERSION,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao

    abstract fun favoriteDao(): FavoriteDao

    abstract fun historyDao(): HistoryDao

    companion object {

        private const val DB_NAME = "template_app.db"

        /**
         * 1 → 2：新增收藏表与浏览历史表。
         *
         * 只做新增表，不动已有 `users` 表——这类迁移风险最低，
         * 老用户升级后原有数据完整保留，新表从空开始。
         *
         * 注意 SQL 要与 Room 生成的建表语句**逐字一致**（列顺序、NOT NULL、默认值），
         * 否则 `MigrationTestHelper` 校验 schema 时会报错。
         */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `favorite` (" +
                        "`articleId` INTEGER NOT NULL, " +
                        "`title` TEXT NOT NULL, " +
                        "`desc` TEXT NOT NULL, " +
                        "`url` TEXT NOT NULL, " +
                        "`author` TEXT NOT NULL, " +
                        "`createTime` TEXT NOT NULL, " +
                        "`savedAt` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`articleId`))",
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `history` (" +
                        "`articleId` INTEGER NOT NULL, " +
                        "`title` TEXT NOT NULL, " +
                        "`desc` TEXT NOT NULL, " +
                        "`url` TEXT NOT NULL, " +
                        "`author` TEXT NOT NULL, " +
                        "`createTime` TEXT NOT NULL, " +
                        "`viewedAt` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`articleId`))",
                )
            }
        }

        /**
         * 迁移列表。新增版本时按如下方式追加：
         * ```
         * private val MIGRATION_2_3 = object : Migration(2, 3) {
         *     override fun migrate(db: SupportSQLiteDatabase) {
         *         db.execSQL("ALTER TABLE users ADD COLUMN gender TEXT NOT NULL DEFAULT ''")
         *     }
         * }
         * ```
         */
        val MIGRATIONS: Array<Migration> = arrayOf(MIGRATION_1_2)

        fun build(context: Context): AppDatabase = Room.databaseBuilder(context, AppDatabase::class.java, DB_NAME)
            // 逐个 addMigrations 而不是 *MIGRATIONS 展开：
            // 展开会先复制整个数组，且空数组时是纯开销；这里也顺便避免 detekt 的性能告警
            .apply { MIGRATIONS.forEach { addMigrations(it) } }
            .build()
    }
}
