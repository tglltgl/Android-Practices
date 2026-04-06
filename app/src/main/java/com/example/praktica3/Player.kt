package com.example.praktica3

import androidx.annotation.DrawableRes

data class Player(
    val id: Int,
    val name: String,
    val number: Int,
    val age: Int,
    val position: String,
    val team: String,
    val stats: List<Pair<String, Long>>,
    @DrawableRes val photoRes: Int
)

val mockPlayers = listOf(
    Player(
        id = 1, name = "Alexey Ionov", number = 11, age = 35,
        position = "Полузащитники", team = "ФК Урал Екатеринбург",
        stats = listOf("4 игр" to 0xFF757575, "1 гол" to 0xFF4DB6AC),
        photoRes = R.drawable.ionov
    ),
    Player(
        id = 2, name = "Erik Bicfalvi", number = 10, age = 36,
        position = "Полузащитники", team = "ФК Урал Екатеринбург",
        stats = listOf("2 игры" to 0xFF757575),
        photoRes = R.drawable.bicfalvi
    ),
    Player(
        id = 3, name = "Yuriy Zheleznov", number = 14, age = 21,
        position = "Полузащитники", team = "ФК Урал Екатеринбург",
        stats = listOf("5 игр" to 0xFF757575),
        photoRes = R.drawable.zheleznov
    ),

    Player(
        id = 4, name = "Artem Mamin", number = 4, age = 28,
        position = "Защитники", team = "ФК Урал Екатеринбург",
        stats = listOf("10 игр" to 0xFF757575, "Защита" to 0xFF1976D2),
        photoRes = R.drawable.mamin
    )
)