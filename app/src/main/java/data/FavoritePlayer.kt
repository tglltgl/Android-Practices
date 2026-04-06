package com.example.praktica3.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoritePlayer(
    @PrimaryKey val id: Int,
    val name: String,
    val position: String,
    val photoRes: Int,
    val number: Int
)