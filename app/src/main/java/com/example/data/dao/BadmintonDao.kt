package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.MatchEntity
import com.example.data.entity.PlayerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BadmintonDao {

    // --- PLAYERS ---
    @Query("SELECT * FROM players ORDER BY name ASC")
    fun getAllPlayers(): Flow<List<PlayerEntity>>

    @Query("SELECT * FROM players WHERE id = :id")
    suspend fun getPlayerById(id: Long): PlayerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayer(player: PlayerEntity): Long

    @Update
    suspend fun updatePlayer(player: PlayerEntity)

    @Delete
    suspend fun deletePlayer(player: PlayerEntity)

    @Query("DELETE FROM players")
    suspend fun deleteAllPlayers()

    // --- MATCHES ---
    @Query("SELECT * FROM matches ORDER BY timestamp DESC")
    fun getAllMatches(): Flow<List<MatchEntity>>

    @Query("SELECT * FROM matches WHERE id = :id")
    suspend fun getMatchById(id: Long): MatchEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatch(match: MatchEntity): Long

    @Delete
    suspend fun deleteMatch(match: MatchEntity)

    @Query("DELETE FROM matches")
    suspend fun deleteAllMatches()

    @Query("SELECT COUNT(*) FROM players")
    suspend fun getPlayerCount(): Int
}
