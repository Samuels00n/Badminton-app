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

    suspend fun deleteAllData() {
        dao.deleteAllMatches()
        dao.deleteAllPlayers()
    }
}
