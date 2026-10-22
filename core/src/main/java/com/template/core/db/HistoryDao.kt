package com.template.core.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {

    @Query("SELECT * FROM history ORDER BY viewedAt DESC")
    fun observeAll(): Flow<List<HistoryEntity>>

    /** 同一条文章重复浏览只保留一行：REPLACE 会覆盖主键相同的那条，viewedAt 随之更新 */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: HistoryEntity)

    @Query("DELETE FROM history WHERE articleId = :articleId")
    suspend fun deleteById(articleId: Long)

    @Query("DELETE FROM history")
    suspend fun clear()

    /**
     * 只保留最近 [keep] 条。
     *
     * 浏览历史是"只增不减"的数据，不设上限会无限膨胀。
     * 用一条 SQL 完成裁剪，比先查全表再算要删哪些更省内存。
     */
    @Query(
        "DELETE FROM history WHERE articleId NOT IN " +
            "(SELECT articleId FROM history ORDER BY viewedAt DESC LIMIT :keep)",
    )
    suspend fun trimTo(keep: Int)

    /** 历史条数，供「我的」页显示；与 [FavoriteDao.observeCount] 同一套写法 */
    @Query("SELECT COUNT(*) FROM history")
    fun observeCount(): Flow<Int>
}
