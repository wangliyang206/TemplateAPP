package com.template.core.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: UserEntity)

    @Query("SELECT * FROM users WHERE user_id = :userId LIMIT 1")
    suspend fun getById(userId: String): UserEntity?

    @Query("SELECT * FROM users")
    suspend fun getAll(): List<UserEntity>

    @Query("DELETE FROM users WHERE user_id = :userId")
    suspend fun deleteById(userId: String)

    @Query("DELETE FROM users")
    suspend fun clear()
}
