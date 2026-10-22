package com.template.core.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {

    /** 收藏列表按收藏时间倒序，供「我的收藏」页直接消费 */
    @Query("SELECT * FROM favorite ORDER BY savedAt DESC")
    fun observeAll(): Flow<List<FavoriteEntity>>

    /**
     * 详情页收藏按钮的状态源。
     * 用 Flow 而不是一次性查询：从收藏页取消收藏返回详情页时，按钮要跟着变。
     */
    @Query("SELECT EXISTS(SELECT 1 FROM favorite WHERE articleId = :articleId)")
    fun observeIsFavorite(articleId: Long): Flow<Boolean>

    /** 一次性读取当前状态，供"切换收藏"这类需要先判断再写的场景使用 */
    @Query("SELECT EXISTS(SELECT 1 FROM favorite WHERE articleId = :articleId)")
    suspend fun isFavorite(articleId: Long): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: FavoriteEntity)

    @Query("DELETE FROM favorite WHERE articleId = :articleId")
    suspend fun deleteById(articleId: Long)

    /**
     * 收藏总数，供「我的」页显示条数。
     *
     * 用 Flow 而不是 `suspend fun count()`：收藏条数在详情页随时会变，
     * 「我的」页回到前台时要显示最新值，观察比回来再查一次省事，也不会漏更新。
     */
    @Query("SELECT COUNT(*) FROM favorite")
    fun observeCount(): Flow<Int>
}
