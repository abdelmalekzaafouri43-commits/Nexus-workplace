package com.example.data

import java.util.UUID

data class LibraryItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val type: ItemType,
    val details: String,
    val timestamp: Long = System.currentTimeMillis(),
    val contentPreview: String
)

enum class ItemType {
    WORKSHEET, POWERPOINT
}

enum class AppThemeMode {
    SAPPHIRE, EMERALD, AMETHYST
}
