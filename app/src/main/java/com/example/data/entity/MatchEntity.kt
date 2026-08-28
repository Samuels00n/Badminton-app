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
    val setsSequence: String = "", // e.g. "21:18,21:15,19:21,21:14" for unlimited sets
    val isRetired: Boolean = false,
    val retiringPlayer: Int? = null, // 1 = Player 1 retired (P2 wins), 2 = Player 2 retired (P1 wins)
    val timestamp: Long = System.currentTimeMillis()
) {
    fun getAllSetScores(): List<Pair<Int, Int>> {
        if (setsSequence.isNotBlank()) {
            val list = setsSequence.split(",").mapNotNull { part ->
                val scores = part.trim().split(":")
                if (scores.size == 2) {
                    val s1 = scores[0].toIntOrNull()
                    val s2 = scores[1].toIntOrNull()
                    if (s1 != null && s2 != null) Pair(s1, s2) else null
                } else null
            }
            if (list.isNotEmpty()) return list
        }
        val list = mutableListOf<Pair<Int, Int>>()
        list.add(Pair(set1Player1, set1Player2))
        if (set2Player1 != null && set2Player2 != null) list.add(Pair(set2Player1, set2Player2))
        if (set3Player1 != null && set3Player2 != null) list.add(Pair(set3Player1, set3Player2))
        return list
    }
}
