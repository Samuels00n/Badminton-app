package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "players")
data class PlayerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val hand: String = "Pravák", // Pravák / Levák
    val style: String = "Všestranný", // Všestranný, Útočný, Obranný
    val skillLevel: String = "Pokročilý", // Začátečník, Pokročilý, Profesionál
    val notes: String = "",
    val colorHex: String = "#00897B",
    val avatarIcon: String = "🏸",
    val createdAt: Long = System.currentTimeMillis()
)
