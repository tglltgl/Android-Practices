package com.example.praktica3.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites")
    fun getAllFavorites(): Flow<List<FavoritePlayer>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(player: FavoritePlayer)

    @Delete
    suspend fun removeFavorite(player: FavoritePlayer)

    @Query("SELECT EXISTS(SELECT * FROM favorites WHERE id = :id)")
    fun isFavorite(id: Int): Flow<Boolean>
}