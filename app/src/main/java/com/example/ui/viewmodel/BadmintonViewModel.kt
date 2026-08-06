package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.BadmintonDatabase
import com.example.data.entity.MatchEntity
import com.example.data.entity.PlayerEntity
import com.example.data.repository.BadmintonRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class GoogleAccountState(
    val isSignedIn: Boolean = false,
    val displayName: String? = null,
    val email: String? = null,
    val photoUrl: String? = null,
    val syncRoomId: String = "BADMINTON-KLUB-2026",
    val lastSyncTimestamp: Long? = null,
    val isSyncing: Boolean = false,
    val syncStatusMessage: String = "Připraveno k synchronizaci"
)

data class PlayerStats(
    val player: PlayerEntity,
    val totalMatches: Int = 0,
    val wins: Int = 0,
    val losses: Int = 0,
    val winRate: Float = 0f,
    val setsWon: Int = 0,
    val setsLost: Int = 0,
    val pointsScored: Int = 0,
    val pointsConceded: Int = 0,
    val currentStreak: Int = 0 // positive for wins, negative for losses
)

data class HeadToHeadStats(
    val player1: PlayerEntity,
    val player2: PlayerEntity,
    val player1Wins: Int = 0,
    val player2Wins: Int = 0,
    val player1Sets: Int = 0,
    val player2Sets: Int = 0,
    val matches: List<MatchEntity> = emptyList()
)

data class MonthlyStat(
    val monthYear: String,
    val matchCount: Int,
    val winRate: Float
)

data class LiveMatchState(
    val matchType: String = "SINGLES", // SINGLES / DOUBLES
    val category: String = "Přátelský",
    val player1: PlayerEntity? = null,
    val player2: PlayerEntity? = null,
    val player3: PlayerEntity? = null, // Doubles Team 1 partner
    val player4: PlayerEntity? = null, // Doubles Team 2 partner
    val currentSetIndex: Int = 1, // Set 1, 2, or 3
    val set1ScoreP1: Int = 0,
    val set1ScoreP2: Int = 0,
    val set2ScoreP1: Int = 0,
    val set2ScoreP2: Int = 0,
    val set3ScoreP1: Int = 0,
    val set3ScoreP2: Int = 0,
    val setsWonP1: Int = 0,
    val setsWonP2: Int = 0,
    val currentServer: Int = 1, // 1 or 2
    val courtType: String = "Hala",
    val isMatchFinished: Boolean = false,
    val winnerTeam: Int = 0, // 1 or 2
    val pointHistory: List<Int> = emptyList(), // 1 for P1 point, 2 for P2 point
    val durationSeconds: Long = 0L
)

class BadmintonViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BadmintonRepository
    private val prefs = application.getSharedPreferences("badminton_account_prefs", Context.MODE_PRIVATE)

    val players: StateFlow<List<PlayerEntity>>
    val matches: StateFlow<List<MatchEntity>>

    // Google Account & Cloud Sync State
    private val _googleAccount = MutableStateFlow(loadInitialGoogleAccountState())
    val googleAccount = _googleAccount.asStateFlow()

    // Filters
    private val _selectedPlayerFilter = MutableStateFlow<Long?>(null)
    val selectedPlayerFilter = _selectedPlayerFilter.asStateFlow()

    private val _selectedCategoryFilter = MutableStateFlow<String?>("Vše")
    val selectedCategoryFilter = _selectedCategoryFilter.asStateFlow()

    // Live Match State
    private val _liveMatch = MutableStateFlow(LiveMatchState())
    val liveMatch = _liveMatch.asStateFlow()

    init {
        val dao = BadmintonDatabase.getDatabase(application).badmintonDao()
        repository = BadmintonRepository(dao)

        players = repository.allPlayers
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

        matches = repository.allMatches
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    }

    fun deleteAllData() {
        viewModelScope.launch {
            repository.deleteAllData()
        }
    }

    private fun loadInitialGoogleAccountState(): GoogleAccountState {
        val isSignedIn = prefs.getBoolean("is_signed_in", false)
        val name = prefs.getString("display_name", null)
        val email = prefs.getString("email", null)
        val roomId = prefs.getString("sync_room_id", "BADMINTON-KLUB-2026") ?: "BADMINTON-KLUB-2026"
        val lastSync = prefs.getLong("last_sync_timestamp", 0L).let { if (it > 0) it else null }

        return GoogleAccountState(
            isSignedIn = isSignedIn,
            displayName = name,
            email = email,
            syncRoomId = roomId,
            lastSyncTimestamp = lastSync,
            syncStatusMessage = if (isSignedIn) "Synchronizováno s Google účtem ($roomId)" else "Nepřihlášeno"
        )
    }

    fun signInWithGoogle(displayName: String, email: String) {
        prefs.edit()
            .putBoolean("is_signed_in", true)
            .putString("display_name", displayName)
            .putString("email", email)
            .apply()

        _googleAccount.value = _googleAccount.value.copy(
            isSignedIn = true,
            displayName = displayName,
            email = email,
            syncStatusMessage = "Přihlášeno jako $email"
        )

        triggerCloudSync()
    }

    fun signOutGoogle() {
        prefs.edit()
            .putBoolean("is_signed_in", false)
            .remove("display_name")
            .remove("email")
            .apply()

        _googleAccount.value = _googleAccount.value.copy(
            isSignedIn = false,
            displayName = null,
            email = null,
            syncStatusMessage = "Odhlášeno z Google účtu"
        )
    }

    fun setSyncRoomId(roomId: String) {
        val cleaned = roomId.trim().uppercase(Locale.ROOT)
        if (cleaned.isBlank()) return

        prefs.edit().putString("sync_room_id", cleaned).apply()
        _googleAccount.value = _googleAccount.value.copy(
            syncRoomId = cleaned,
            syncStatusMessage = "Kód skupiny nastaven na: $cleaned"
        )

        triggerCloudSync()
    }

    fun triggerCloudSync() {
        viewModelScope.launch {
            _googleAccount.value = _googleAccount.value.copy(
                isSyncing = true,
                syncStatusMessage = "Probíhá synchronizace dat s cloudem..."
            )

            delay(1200) // Simulate cloud fetch/merge with other users

            val now = System.currentTimeMillis()
            prefs.edit().putLong("last_sync_timestamp", now).apply()

            _googleAccount.value = _googleAccount.value.copy(
                isSyncing = false,
                lastSyncTimestamp = now,
                syncStatusMessage = "Všechna data jsou aktuální (${SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(now))})"
            )
        }
    }

    fun setPlayerFilter(playerId: Long?) {
        _selectedPlayerFilter.value = playerId
    }

    fun setCategoryFilter(category: String?) {
        _selectedCategoryFilter.value = category
    }

    fun addPlayer(name: String, hand: String, style: String, skillLevel: String, colorHex: String, notes: String, avatarIcon: String = "🏸") {
        viewModelScope.launch {
            repository.insertPlayer(
                PlayerEntity(
                    name = name.trim(),
                    hand = hand,
                    style = style,
                    skillLevel = skillLevel,
                    colorHex = colorHex,
                    notes = notes.trim(),
                    avatarIcon = avatarIcon
                )
            )
        }
    }

    fun deletePlayer(player: PlayerEntity) {
        viewModelScope.launch {
            repository.deletePlayer(player)
        }
    }

    fun updatePlayer(player: PlayerEntity) {
        viewModelScope.launch {
            repository.updatePlayer(player)
        }
    }

    fun addMatch(match: MatchEntity) {
        viewModelScope.launch {
            repository.insertMatch(match)
        }
    }

    fun deleteMatch(match: MatchEntity) {
        viewModelScope.launch {
            repository.deleteMatch(match)
        }
    }

    // --- LIVE MATCH CONTROLS ---
    fun startLiveMatch(
        p1: PlayerEntity,
        p2: PlayerEntity,
        p3: PlayerEntity? = null,
        p4: PlayerEntity? = null,
        matchType: String = "SINGLES",
        category: String = "Přátelský",
        courtType: String = "Hala"
    ) {
        _liveMatch.value = LiveMatchState(
            matchType = matchType,
            category = category,
            player1 = p1,
            player2 = p2,
            player3 = p3,
            player4 = p4,
            courtType = courtType
        )
    }

    fun addPointToTeam(team: Int) {
        val current = _liveMatch.value
        if (current.isMatchFinished || current.player1 == null || current.player2 == null) return

        var s1P1 = current.set1ScoreP1
        var s1P2 = current.set1ScoreP2
        var s2P1 = current.set2ScoreP1
        var s2P2 = current.set2ScoreP2
        var s3P1 = current.set3ScoreP1
        var s3P2 = current.set3ScoreP2
        var setIdx = current.currentSetIndex
        var setsP1 = current.setsWonP1
        var setsP2 = current.setsWonP2

        when (setIdx) {
            1 -> if (team == 1) s1P1++ else s1P2++
            2 -> if (team == 1) s2P1++ else s2P2++
            3 -> if (team == 1) s3P1++ else s3P2++
        }

        val updatedHistory = current.pointHistory + team

        // Check if current set finished (standard BWF: 21 pts, min 2 lead, max 30)
        val (curP1, curP2) = when (setIdx) {
            1 -> s1P1 to s1P2
            2 -> s2P1 to s2P2
            else -> s3P1 to s3P2
        }

        var isSetOver = false
        var setWinner = 0
        if ((curP1 >= 21 || curP2 >= 21) && Math.abs(curP1 - curP2) >= 2) {
            isSetOver = true
            setWinner = if (curP1 > curP2) 1 else 2
        } else if (curP1 >= 30 || curP2 >= 30) {
            isSetOver = true
            setWinner = if (curP1 >= 30) 1 else 2
        }

        if (isSetOver) {
            if (setWinner == 1) setsP1++ else setsP2++

            if (setsP1 == 2 || setsP2 == 2) {
                // Match finished!
                val winner = if (setsP1 == 2) 1 else 2
                _liveMatch.value = current.copy(
                    set1ScoreP1 = s1P1, set1ScoreP2 = s1P2,
                    set2ScoreP1 = s2P1, set2ScoreP2 = s2P2,
                    set3ScoreP1 = s3P1, set3ScoreP2 = s3P2,
                    setsWonP1 = setsP1, setsWonP2 = setsP2,
                    isMatchFinished = true,
                    winnerTeam = winner,
                    currentServer = team,
                    pointHistory = updatedHistory
                )
                return
            } else {
                setIdx++
            }
        }

        _liveMatch.value = current.copy(
            currentSetIndex = setIdx,
            set1ScoreP1 = s1P1, set1ScoreP2 = s1P2,
            set2ScoreP1 = s2P1, set2ScoreP2 = s2P2,
            set3ScoreP1 = s3P1, set3ScoreP2 = s3P2,
            setsWonP1 = setsP1, setsWonP2 = setsP2,
            currentServer = team,
            pointHistory = updatedHistory
        )
    }

    fun undoLastPoint() {
        val current = _liveMatch.value
        if (current.pointHistory.isEmpty()) return

        val lastTeam = current.pointHistory.last()
        val updatedHistory = current.pointHistory.dropLast(1)

        var s1P1 = current.set1ScoreP1
        var s1P2 = current.set1ScoreP2
        var s2P1 = current.set2ScoreP1
        var s2P2 = current.set2ScoreP2
        var s3P1 = current.set3ScoreP1
        var s3P2 = current.set3ScoreP2
        var setIdx = current.currentSetIndex
        var setsP1 = current.setsWonP1
        var setsP2 = current.setsWonP2

        // If match was finished, revert finished state
        var isFinished = false
        var winner = 0

        when (setIdx) {
            3 -> {
                if (s3P1 == 0 && s3P2 == 0) {
                    setIdx = 2
                    if (lastTeam == 1) s2P1-- else s2P2--
                    if (s2P1 > s2P2) setsP1-- else setsP2--
                } else {
                    if (lastTeam == 1) s3P1-- else s3P2--
                }
            }
            2 -> {
                if (s2P1 == 0 && s2P2 == 0) {
                    setIdx = 1
                    if (lastTeam == 1) s1P1-- else s1P2--
                    if (s1P1 > s1P2) setsP1-- else setsP2--
                } else {
                    if (lastTeam == 1) s2P1-- else s2P2--
                }
            }
            1 -> {
                if (lastTeam == 1) s1P1 = maxOf(0, s1P1 - 1) else s1P2 = maxOf(0, s1P2 - 1)
            }
        }

        _liveMatch.value = current.copy(
            currentSetIndex = setIdx,
            set1ScoreP1 = s1P1, set1ScoreP2 = s1P2,
            set2ScoreP1 = s2P1, set2ScoreP2 = s2P2,
            set3ScoreP1 = s3P1, set3ScoreP2 = s3P2,
            setsWonP1 = setsP1, setsWonP2 = setsP2,
            isMatchFinished = isFinished,
            winnerTeam = winner,
            pointHistory = updatedHistory
        )
    }

    fun saveLiveMatchToHistory() {
        val current = _liveMatch.value
        val p1 = current.player1 ?: return
        val p2 = current.player2 ?: return

        val match = MatchEntity(
            matchType = current.matchType,
            category = current.category,
            player1Id = p1.id,
            player2Id = p2.id,
            player3Id = current.player3?.id,
            player4Id = current.player4?.id,
            setsWinner = current.winnerTeam,
            scoreSetsPlayer1 = current.setsWonP1,
            scoreSetsPlayer2 = current.setsWonP2,
            set1Player1 = current.set1ScoreP1,
            set1Player2 = current.set1ScoreP2,
            set2Player1 = if (current.set2ScoreP1 > 0 || current.set2ScoreP2 > 0) current.set2ScoreP1 else null,
            set2Player2 = if (current.set2ScoreP1 > 0 || current.set2ScoreP2 > 0) current.set2ScoreP2 else null,
            set3Player1 = if (current.set3ScoreP1 > 0 || current.set3ScoreP2 > 0) current.set3ScoreP1 else null,
            set3Player2 = if (current.set3ScoreP1 > 0 || current.set3ScoreP2 > 0) current.set3ScoreP2 else null,
            courtType = current.courtType,
            durationMinutes = maxOf(1, (current.pointHistory.size * 0.4).toInt()),
            notes = "Uloženo z živého skóre",
            timestamp = System.currentTimeMillis()
        )

        addMatch(match)
        _liveMatch.value = LiveMatchState() // Reset
    }

    fun cancelLiveMatch() {
        _liveMatch.value = LiveMatchState()
    }

    // --- CALCULATED STATISTICS ---
    fun calculatePlayerStats(player: PlayerEntity, matchOptions: List<MatchEntity>): PlayerStats {
        var totalMatches = 0
        var wins = 0
        var losses = 0
        var setsWon = 0
        var setsLost = 0
        var ptsScored = 0
        var ptsConceded = 0
        val streakList = mutableListOf<Boolean>()

        // Sort matches chronologically for streak calculation
        val sorted = matchOptions.sortedBy { it.timestamp }

        for (m in sorted) {
            val isP1Team = (m.player1Id == player.id || m.player3Id == player.id)
            val isP2Team = (m.player2Id == player.id || m.player4Id == player.id)

            if (!isP1Team && !isP2Team) continue

            totalMatches++
            val isP1Winner = (m.setsWinner == 1)

            val wonMatch = if (isP1Team) isP1Winner else !isP1Winner
            if (wonMatch) {
                wins++
                streakList.add(true)
            } else {
                losses++
                streakList.add(false)
            }

            if (isP1Team) {
                setsWon += m.scoreSetsPlayer1
                setsLost += m.scoreSetsPlayer2
                ptsScored += m.set1Player1 + (m.set2Player1 ?: 0) + (m.set3Player1 ?: 0)
                ptsConceded += m.set1Player2 + (m.set2Player2 ?: 0) + (m.set3Player2 ?: 0)
            } else {
                setsWon += m.scoreSetsPlayer2
                setsLost += m.scoreSetsPlayer1
                ptsScored += m.set1Player2 + (m.set2Player2 ?: 0) + (m.set3Player2 ?: 0)
                ptsConceded += m.set1Player1 + (m.set2Player1 ?: 0) + (m.set3Player1 ?: 0)
            }
        }

        val winRate = if (totalMatches > 0) (wins.toFloat() / totalMatches) * 100f else 0f

        // Calculate current streak
        var streak = 0
        if (streakList.isNotEmpty()) {
            val lastResult = streakList.last()
            for (i in streakList.indices.reversed()) {
                if (streakList[i] == lastResult) {
                    if (lastResult) streak++ else streak--
                } else break
            }
        }

        return PlayerStats(
            player = player,
            totalMatches = totalMatches,
            wins = wins,
            losses = losses,
            winRate = winRate,
            setsWon = setsWon,
            setsLost = setsLost,
            pointsScored = ptsScored,
            pointsConceded = ptsConceded,
            currentStreak = streak
        )
    }

    fun calculateHeadToHead(p1: PlayerEntity, p2: PlayerEntity, matchOptions: List<MatchEntity>): HeadToHeadStats {
        var p1Wins = 0
        var p2Wins = 0
        var p1Sets = 0
        var p2Sets = 0
        val h2hMatches = mutableListOf<MatchEntity>()

        for (m in matchOptions) {
            val p1OnTeam1 = (m.player1Id == p1.id || m.player3Id == p1.id)
            val p2OnTeam2 = (m.player2Id == p2.id || m.player4Id == p2.id)

            val p2OnTeam1 = (m.player1Id == p2.id || m.player3Id == p2.id)
            val p1OnTeam2 = (m.player2Id == p1.id || m.player4Id == p1.id)

            if (p1OnTeam1 && p2OnTeam2) {
                h2hMatches.add(m)
                p1Sets += m.scoreSetsPlayer1
                p2Sets += m.scoreSetsPlayer2
                if (m.setsWinner == 1) p1Wins++ else p2Wins++
            } else if (p2OnTeam1 && p1OnTeam2) {
                h2hMatches.add(m)
                p1Sets += m.scoreSetsPlayer2
                p2Sets += m.scoreSetsPlayer1
                if (m.setsWinner == 1) p2Wins++ else p1Wins++
            }
        }

        return HeadToHeadStats(
            player1 = p1,
            player2 = p2,
            player1Wins = p1Wins,
            player2Wins = p2Wins,
            player1Sets = p1Sets,
            player2Sets = p2Sets,
            matches = h2hMatches.sortedByDescending { it.timestamp }
        )
    }

    fun calculateMonthlyStatsForPlayer(player: PlayerEntity, matchOptions: List<MatchEntity>): List<MonthlyStat> {
        val sdf = SimpleDateFormat("MMM yyyy", Locale("cs", "CZ"))
        val sorted = matchOptions.filter {
            it.player1Id == player.id || it.player2Id == player.id || it.player3Id == player.id || it.player4Id == player.id
        }.sortedBy { it.timestamp }

        val grouped = sorted.groupBy { sdf.format(Date(it.timestamp)) }

        return grouped.map { (monthYear, monthMatches) ->
            var wins = 0
            monthMatches.forEach { m ->
                val isP1Team = (m.player1Id == player.id || m.player3Id == player.id)
                val isP1Winner = (m.setsWinner == 1)
                val won = if (isP1Team) isP1Winner else !isP1Winner
                if (won) wins++
            }
            val winRate = if (monthMatches.isNotEmpty()) (wins.toFloat() / monthMatches.size) * 100f else 0f
            MonthlyStat(monthYear, monthMatches.size, winRate)
        }
    }
}
