package com.example.data.repository

import com.example.data.dao.BadmintonDao
import com.example.data.entity.MatchEntity
import com.example.data.entity.PlayerEntity
import kotlinx.coroutines.flow.Flow

class BadmintonRepository(private val dao: BadmintonDao) {

    val allPlayers: Flow<List<PlayerEntity>> = dao.getAllPlayers()
    val allMatches: Flow<List<MatchEntity>> = dao.getAllMatches()

    suspend fun getPlayerById(id: Long): PlayerEntity? = dao.getPlayerById(id)

    suspend fun insertPlayer(player: PlayerEntity): Long = dao.insertPlayer(player)

    suspend fun updatePlayer(player: PlayerEntity) = dao.updatePlayer(player)

    suspend fun deletePlayer(player: PlayerEntity) = dao.deletePlayer(player)

    suspend fun insertMatch(match: MatchEntity): Long = dao.insertMatch(match)

    suspend fun deleteMatch(match: MatchEntity) = dao.deleteMatch(match)

    suspend fun seedInitialDataIfNeeded() {
        if (dao.getPlayerCount() == 0) {
            val p1Id = dao.insertPlayer(
                PlayerEntity(
                    name = "Petr Svoboda",
                    hand = "Pravák",
                    style = "Útočný",
                    skillLevel = "Pokročilý",
                    colorHex = "#1976D2",
                    notes = "Rychlé smeče na síti"
                )
            )
            val p2Id = dao.insertPlayer(
                PlayerEntity(
                    name = "Jana Nováková",
                    hand = "Pravák",
                    style = "Všestranný",
                    skillLevel = "Pokročilý",
                    colorHex = "#00897B",
                    notes = "Přesný klír a drop shoty"
                )
            )
            val p3Id = dao.insertPlayer(
                PlayerEntity(
                    name = "Tomáš Dvořák",
                    hand = "Levák",
                    style = "Obranný",
                    skillLevel = "Profesionál",
                    colorHex = "#7B1FA2",
                    notes = "Těžko překonatelný v obraně"
                )
            )
            val p4Id = dao.insertPlayer(
                PlayerEntity(
                    name = "Lucie Černá",
                    hand = "Pravák",
                    style = "Všestranný",
                    skillLevel = "Začátečník",
                    colorHex = "#E65100",
                    notes = "Zapálená badmintonistka"
                )
            )

            // Seed sample match results over time
            val now = System.currentTimeMillis()
            val day = 86400000L

            dao.insertMatch(
                MatchEntity(
                    matchType = "SINGLES",
                    category = "Turnaj",
                    player1Id = p1Id,
                    player2Id = p2Id,
                    setsWinner = 1,
                    scoreSetsPlayer1 = 2,
                    scoreSetsPlayer2 = 1,
                    set1Player1 = 21,
                    set1Player2 = 18,
                    set2Player1 = 19,
                    set2Player2 = 21,
                    set3Player1 = 21,
                    set3Player2 = 15,
                    courtType = "Hala / Mat",
                    durationMinutes = 42,
                    notes = "Napínavé finále klubového turnaje",
                    timestamp = now - (14 * day)
                )
            )

            dao.insertMatch(
                MatchEntity(
                    matchType = "SINGLES",
                    category = "Přátelský",
                    player1Id = p3Id,
                    player2Id = p1Id,
                    setsWinner = 1,
                    scoreSetsPlayer1 = 2,
                    scoreSetsPlayer2 = 0,
                    set1Player1 = 21,
                    set1Player2 = 14,
                    set2Player1 = 21,
                    set2Player2 = 16,
                    courtType = "Hala",
                    durationMinutes = 28,
                    notes = "Tomášův skvělý zápas levorukým podáním",
                    timestamp = now - (10 * day)
                )
            )

            dao.insertMatch(
                MatchEntity(
                    matchType = "SINGLES",
                    category = "Liga",
                    player1Id = p2Id,
                    player2Id = p4Id,
                    setsWinner = 1,
                    scoreSetsPlayer1 = 2,
                    scoreSetsPlayer2 = 0,
                    set1Player1 = 21,
                    set1Player2 = 12,
                    set2Player1 = 21,
                    set2Player2 = 10,
                    courtType = "Hala",
                    durationMinutes = 22,
                    notes = "Tréninkové ligové kolo",
                    timestamp = now - (7 * day)
                )
            )

            dao.insertMatch(
                MatchEntity(
                    matchType = "DOUBLES",
                    category = "Turnaj",
                    player1Id = p1Id,
                    player2Id = p3Id,
                    player3Id = p2Id,
                    player4Id = p4Id,
                    setsWinner = 1,
                    scoreSetsPlayer1 = 2,
                    scoreSetsPlayer2 = 1,
                    set1Player1 = 21,
                    set1Player2 = 19,
                    set2Player1 = 18,
                    set2Player2 = 21,
                    set3Player1 = 21,
                    set3Player2 = 17,
                    courtType = "Hala / Mat",
                    durationMinutes = 50,
                    notes = "Čtyřhra: Petr & Jana vs Tomáš & Lucie",
                    timestamp = now - (2 * day)
                )
            )
        }
    }
}
