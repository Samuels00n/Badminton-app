package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "matches")
data class MatchEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val matchType: String = "SINGLES", // SINGLES or DOUBLES
    val category: String = "Přátelský", // Přátelský, Turnaj, Liga
    val player1Id: Long,
    val player2Id: Long,
    val player3Id: Long? = null, // Doubles Team 1 second player
    val player4Id: Long? = null, // Doubles Team 2 second player
    val setsWinner: Int, // 1 = Team 1 won, 2 = Team 2 won
    val scoreSetsPlayer1: Int,
    val scoreSetsPlayer2: Int,
    val set1Player1: Int,
    val set1Player2: Int,
    val set2Player1: Int? = null,
    val set2Player2: Int? = null,
    val set3Player1: Int? = null,
    val set3Player2: Int? = null,
    val courtType: String = "Hala", // Hala, Venku, Jiné
    val durationMinutes: Int = 30,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
