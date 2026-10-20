package com.template.core.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration

/**
 * 应用数据库。
 *
 * 规范：
 * - `exportSchema = true`，schema 输出到 core/schemas，升级前先 diff 再写迁移；
 * - **禁止 fallbackToDestructiveMigration**：线上升级会清空用户数据；
 * - 新增版本时在 [MIGRATIONS] 中追加，并在 androidTest 覆盖迁移测试。
 */
@Database(
    entities = [UserEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao

    companion object {

        private const val DB_NAME = "template_app.db"

        /**
         * 迁移列表。新增版本时按如下方式追加：
         * ```
         * private val MIGRATION_1_2 = object : Migration(1, 2) {
         *     override fun migrate(db: SupportSQLiteDatabase) {
         *         db.execSQL("ALTER TABLE users ADD COLUMN gender TEXT NOT NULL DEFAULT ''")
         *     }
         * }
         * ```
         */
        val MIGRATIONS: Array<Migration> = emptyArray()

        fun build(context: Context): AppDatabase = Room.databaseBuilder(context, AppDatabase::class.java, DB_NAME)
            // 逐个 addMigrations 而不是 *MIGRATIONS 展开：
            // 展开会先复制整个数组，且空数组时是纯开销；这里也顺便避免 detekt 的性能告警
            .apply { MIGRATIONS.forEach { addMigrations(it) } }
            .build()
    }
}
