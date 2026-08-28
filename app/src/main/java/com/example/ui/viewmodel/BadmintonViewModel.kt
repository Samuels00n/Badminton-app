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
import org.json.JSONArray
import org.json.JSONObject
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions

data class GoogleAccountState(
    val isSignedIn: Boolean = false,
    val displayName: String? = null,
    val email: String? = null,
    val photoUrl: String? = null,
    val syncRoomId: String = "",
    val lastSyncTimestamp: Long? = null,
    val isSyncing: Boolean = false,
    val syncStatusMessage: String = "Automatické ukládání aktivní"
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

enum class AchievementTier(val label: String, val colorHex: String) {
    CHALLENGE("Výzva", "#8ca393"),
    BRONZE("Bronz", "#cd7f32"),
    SILVER("Stříbro", "#c0c0c0"),
    GOLD("Zlato", "#ffd700"),
    DIAMOND("Diamant", "#00d2ff")
}

data class TierInfo(
    val tier: AchievementTier,
    val label: String,
    val next: Int?,
    val target: Int,
    val progress: Int,
    val unlocked: Boolean
)

data class AchievementItem(
    val id: String,
    val icon: String,
    val title: String,
    val desc: String,
    val tierInfo: TierInfo,
    val current: Int,
    val unit: String
)

data class FormMatchItem(
    val matchId: Long,
    val isWin: Boolean,
    val mySets: Int,
    val oppSets: Int,
    val oppName: String,
    val dateStr: String,
    val isRetired: Boolean
)

data class RivalStat(
    val id: Long,
    val name: String,
    val matches: Int,
    val wins: Int,
    val losses: Int
)

data class PlayerAdvancedStats(
    val totalMatches: Int = 0,
    val currentStreakType: String = "W",
    val currentStreakCount: Int = 0,
    val maxWinStreak: Int = 0,
    val recentForm: List<FormMatchItem> = emptyList(),
    val threeSetsPlayed: Int = 0,
    val threeSetsWon: Int = 0,
    val threeSetsWinRate: Float = 0f,
    val cleanSweepsCount: Int = 0,
    val comebackWins: Int = 0,
    val clutchSetsWon: Int = 0,
    val blowoutSetsWon: Int = 0,
    val maxMatchPoints: Int = 0,
    val maxMatchPointsDetail: String? = null,
    val retiredCount: Int = 0,
    val mostFrequentRival: RivalStat? = null,
    val bestOpponent: RivalStat? = null,
    val achievements: List<AchievementItem> = emptyList()
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

    private var firestorePlayersListener: ListenerRegistration? = null
    private var firestoreMatchesListener: ListenerRegistration? = null

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

        // Clear all players as requested
        viewModelScope.launch {
            if (!prefs.getBoolean("cleared_all_players_v3", false)) {
                repository.deleteAllData()
                prefs.edit().putBoolean("cleared_all_players_v3", true).apply()
            }
        }

        val initialAccount = _googleAccount.value
        if (initialAccount.syncRoomId.isNotBlank()) {
            setupFirestoreListeners(initialAccount.syncRoomId)
        }
    }

    private fun setupFirestoreListeners(roomId: String) {
        firestorePlayersListener?.remove()
        firestoreMatchesListener?.remove()

        if (roomId.isBlank()) return

        try {
            val db = FirebaseFirestore.getInstance()

            firestorePlayersListener = db.collection("groups")
                .document(roomId)
                .collection("players")
                .addSnapshotListener { snapshot, e ->
                    if (e != null || snapshot == null) return@addSnapshotListener
                    viewModelScope.launch {
                        val remotePlayerIds = snapshot.documents.mapNotNull {
                            it.getLong("id") ?: it.id.toLongOrNull()
                        }.toSet()
                        for (doc in snapshot.documents) {
                            val pId = doc.getLong("id") ?: doc.id.toLongOrNull() ?: continue
                            val name = doc.getString("name") ?: continue
                            val hand = doc.getString("hand") ?: "Pravák"
                            val style = doc.getString("style") ?: "Univerzál"
                            val skillLevel = doc.getString("skillLevel") ?: "Pokročilý"
                            val colorHex = doc.getString("colorHex") ?: "#2E7D32"
                            val notes = doc.getString("notes") ?: ""
                            val avatarIcon = doc.getString("avatarIcon") ?: "🏸"

                            repository.insertPlayer(
                                PlayerEntity(
                                    id = pId,
                                    name = name,
                                    hand = hand,
                                    style = style,
                                    skillLevel = skillLevel,
                                    colorHex = colorHex,
                                    notes = notes,
                                    avatarIcon = avatarIcon
                                )
                            )
                        }

                        val localPlayers = players.value
                        for (localP in localPlayers) {
                            if (localP.id !in remotePlayerIds) {
                                repository.deletePlayer(localP)
                            }
                        }
                    }
                }

            firestoreMatchesListener = db.collection("groups")
                .document(roomId)
                .collection("matches")
                .addSnapshotListener { snapshot, e ->
                    if (e != null || snapshot == null) return@addSnapshotListener
                    viewModelScope.launch {
                        val remoteMatchIds = snapshot.documents.mapNotNull { doc ->
                            doc.getLong("id") ?: doc.id.toLongOrNull() ?: doc.getLong("timestamp")
                        }.toSet()
                        for (doc in snapshot.documents) {
                            val mId = doc.getLong("id")
                                ?: doc.id.toLongOrNull()
                                ?: doc.getLong("timestamp")
                                ?: Math.abs(doc.id.hashCode().toLong())
                            val matchType = doc.getString("matchType") ?: "SINGLES"
                            val category = doc.getString("category") ?: "Přátelský"
                            val p1Id = doc.getLong("player1Id") ?: continue
                            val p2Id = doc.getLong("player2Id") ?: continue
                            val p3Id = doc.getLong("player3Id")
                            val p4Id = doc.getLong("player4Id")
                            val scoreSetsPlayer1 = doc.getLong("scoreSetsPlayer1")?.toInt() ?: 0
                            val scoreSetsPlayer2 = doc.getLong("scoreSetsPlayer2")?.toInt() ?: 0
                            val setsWinner = doc.getLong("setsWinner")?.toInt()
                                ?: if (scoreSetsPlayer1 >= scoreSetsPlayer2) 1 else 2
                            val set1Player1 = doc.getLong("set1Player1")?.toInt() ?: 0
                            val set1Player2 = doc.getLong("set1Player2")?.toInt() ?: 0
                            val set2Player1 = doc.getLong("set2Player1")?.toInt()
                            val set2Player2 = doc.getLong("set2Player2")?.toInt()
                            val set3Player1 = doc.getLong("set3Player1")?.toInt()
                            val set3Player2 = doc.getLong("set3Player2")?.toInt()
                            val courtType = doc.getString("courtType") ?: "Hala"
                            val durationMinutes = doc.getLong("durationMinutes")?.toInt() ?: 30
                            val notes = doc.getString("notes") ?: ""
                            val setsSequence = doc.getString("setsSequence") ?: ""
                            val isRetired = doc.getBoolean("isRetired") ?: false
                            val retiringPlayer = doc.getLong("retiringPlayer")?.toInt()
                            val timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()

                            repository.insertMatch(
                                MatchEntity(
                                    id = mId,
                                    matchType = matchType,
                                    category = category,
                                    player1Id = p1Id,
                                    player2Id = p2Id,
                                    player3Id = p3Id,
                                    player4Id = p4Id,
                                    setsWinner = setsWinner,
                                    scoreSetsPlayer1 = scoreSetsPlayer1,
                                    scoreSetsPlayer2 = scoreSetsPlayer2,
                                    set1Player1 = set1Player1,
                                    set1Player2 = set1Player2,
                                    set2Player1 = set2Player1,
                                    set2Player2 = set2Player2,
                                    set3Player1 = set3Player1,
                                    set3Player2 = set3Player2,
                                    courtType = courtType,
                                    durationMinutes = durationMinutes,
                                    notes = notes,
                                    setsSequence = setsSequence,
                                    isRetired = isRetired,
                                    retiringPlayer = retiringPlayer,
                                    timestamp = timestamp
                                )
                            )
                        }

                        val localMatches = matches.value
                        for (localM in localMatches) {
                            if (localM.id !in remoteMatchIds) {
                                repository.deleteMatch(localM)
                            }
                        }
                    }
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun deleteAllData() {
        viewModelScope.launch {
            repository.deleteAllData()
            triggerCloudSync()
        }
    }

    private fun loadInitialGoogleAccountState(): GoogleAccountState {
        // Clear all pre-saved account details so the user starts strictly signed out
        if (!prefs.getBoolean("auth_reset_v2", false)) {
            prefs.edit()
                .putBoolean("is_signed_in", false)
                .remove("display_name")
                .remove("email")
                .remove("sync_room_id")
                .putBoolean("auth_reset_v2", true)
                .apply()
        }

        val isSignedIn = prefs.getBoolean("is_signed_in", false)
        val name = prefs.getString("display_name", null)
        val email = prefs.getString("email", null)
        var roomId = prefs.getString("sync_room_id", "") ?: ""
        if (roomId == "BADMINTON-KLUB-2026") {
            roomId = ""
            prefs.edit().remove("sync_room_id").apply()
        }
        val lastSync = prefs.getLong("last_sync_timestamp", 0L).let { if (it > 0) it else null }

        return GoogleAccountState(
            isSignedIn = isSignedIn,
            displayName = name,
            email = email,
            syncRoomId = roomId,
            lastSyncTimestamp = lastSync,
            syncStatusMessage = if (isSignedIn) (if (roomId.isNotBlank()) "Synchronizováno s Google účtem ($roomId)" else "Synchronizováno s Google účtem") else "Nepřihlášeno"
        )
    }

    fun signInWithGoogle(emailInput: String, groupCode: String) {
        val email = if (emailInput.isBlank()) "uzivatel@skupina" else emailInput.trim()
        val displayName = if (email.contains("@")) {
            email.substringBefore("@").replace(".", " ").replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
        } else {
            email
        }
        val cleanedRoom = groupCode.trim().uppercase(Locale.ROOT)
        prefs.edit()
            .putBoolean("is_signed_in", true)
            .putString("display_name", displayName)
            .putString("email", email)
            .putString("sync_room_id", cleanedRoom)
            .apply()

        _googleAccount.value = _googleAccount.value.copy(
            isSignedIn = true,
            displayName = displayName,
            email = email,
            syncRoomId = cleanedRoom,
            syncStatusMessage = if (cleanedRoom.isNotBlank()) "Připojeno ke skupině $cleanedRoom" else "Připojeno"
        )

        viewModelScope.launch {
            repository.deleteAllData()
            setupFirestoreListeners(cleanedRoom)
            triggerCloudSync()
        }
    }

    fun signOutGoogle() {
        firestorePlayersListener?.remove()
        firestoreMatchesListener?.remove()
        firestorePlayersListener = null
        firestoreMatchesListener = null

        prefs.edit()
            .putBoolean("is_signed_in", false)
            .remove("display_name")
            .remove("email")
            .remove("sync_room_id")
            .apply()

        _googleAccount.value = _googleAccount.value.copy(
            isSignedIn = false,
            displayName = null,
            email = null,
            syncRoomId = "",
            syncStatusMessage = "Odpojeno ze skupiny"
        )

        viewModelScope.launch {
            repository.deleteAllData()
        }
    }

    fun setSyncRoomId(roomId: String) {
        val cleaned = roomId.trim().uppercase(Locale.ROOT)
        if (cleaned.isBlank()) {
            signOutGoogle()
            return
        }

        prefs.edit().putString("sync_room_id", cleaned).apply()
        _googleAccount.value = _googleAccount.value.copy(
            syncRoomId = cleaned,
            syncStatusMessage = "Kód skupiny nastaven na: $cleaned"
        )

        viewModelScope.launch {
            repository.deleteAllData()
            setupFirestoreListeners(cleaned)
            triggerCloudSync()
        }
    }

    fun triggerCloudSync() {
        val roomId = _googleAccount.value.syncRoomId
        setupFirestoreListeners(roomId)

        viewModelScope.launch {
            _googleAccount.value = _googleAccount.value.copy(
                isSyncing = true,
                syncStatusMessage = "Probíhá synchronizace dat s cloudem..."
            )

            if (roomId.isNotBlank()) {
                try {
                    val db = FirebaseFirestore.getInstance()
                    val playersList = players.value
                    val matchesList = matches.value

                    for (p in playersList) {
                        val map = mapOf(
                            "id" to p.id,
                            "name" to p.name,
                            "hand" to p.hand,
                            "style" to p.style,
                            "skillLevel" to p.skillLevel,
                            "colorHex" to p.colorHex,
                            "notes" to p.notes,
                            "avatarIcon" to p.avatarIcon
                        )
                        db.collection("groups").document(roomId).collection("players")
                            .document(p.id.toString()).set(map, SetOptions.merge())
                    }

                    for (m in matchesList) {
                        val map = mutableMapOf<String, Any?>(
                            "id" to m.id,
                            "matchType" to m.matchType,
                            "category" to m.category,
                            "player1Id" to m.player1Id,
                            "player2Id" to m.player2Id,
                            "player3Id" to m.player3Id,
                            "player4Id" to m.player4Id,
                            "setsWinner" to m.setsWinner,
                            "scoreSetsPlayer1" to m.scoreSetsPlayer1,
                            "scoreSetsPlayer2" to m.scoreSetsPlayer2,
                            "set1Player1" to m.set1Player1,
                            "set1Player2" to m.set1Player2,
                            "set2Player1" to m.set2Player1,
                            "set2Player2" to m.set2Player2,
                            "set3Player1" to m.set3Player1,
                            "set3Player2" to m.set3Player2,
                            "courtType" to m.courtType,
                            "durationMinutes" to m.durationMinutes,
                            "notes" to m.notes,
                            "setsSequence" to m.setsSequence,
                            "isRetired" to m.isRetired,
                            "retiringPlayer" to m.retiringPlayer,
                            "timestamp" to m.timestamp
                        )
                        db.collection("groups").document(roomId).collection("matches")
                            .document(m.id.toString()).set(map, SetOptions.merge())
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                delay(800)
            }

            val now = System.currentTimeMillis()
            prefs.edit().putLong("last_sync_timestamp", now).apply()

            _googleAccount.value = _googleAccount.value.copy(
                isSyncing = false,
                lastSyncTimestamp = now,
                syncStatusMessage = "Všechna data jsou aktuální (${SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(now))})"
            )
        }
    }

    fun exportDataJson(onResult: (String) -> Unit) {
        viewModelScope.launch {
            val currentPlayers = players.value
            val currentMatches = matches.value
            
            val rootJson = JSONObject()
            rootJson.put("version", 1)
            rootJson.put("groupCode", _googleAccount.value.syncRoomId)
            rootJson.put("exportedAt", System.currentTimeMillis())
            
            val playersArray = JSONArray()
            currentPlayers.forEach { p ->
                val pObj = JSONObject()
                pObj.put("id", p.id)
                pObj.put("name", p.name)
                pObj.put("hand", p.hand)
                pObj.put("style", p.style)
                pObj.put("skillLevel", p.skillLevel)
                pObj.put("colorHex", p.colorHex)
                pObj.put("notes", p.notes)
                pObj.put("avatarIcon", p.avatarIcon)
                playersArray.put(pObj)
            }
            rootJson.put("players", playersArray)

            val matchesArray = JSONArray()
            currentMatches.forEach { m ->
                val mObj = JSONObject()
                mObj.put("id", m.id)
                mObj.put("matchType", m.matchType)
                mObj.put("category", m.category)
                mObj.put("player1Id", m.player1Id)
                mObj.put("player2Id", m.player2Id)
                mObj.put("player3Id", m.player3Id ?: JSONObject.NULL)
                mObj.put("player4Id", m.player4Id ?: JSONObject.NULL)
                mObj.put("setsWinner", m.setsWinner)
                mObj.put("scoreSetsPlayer1", m.scoreSetsPlayer1)
                mObj.put("scoreSetsPlayer2", m.scoreSetsPlayer2)
                mObj.put("set1Player1", m.set1Player1)
                mObj.put("set1Player2", m.set1Player2)
                mObj.put("set2Player1", m.set2Player1 ?: JSONObject.NULL)
                mObj.put("set2Player2", m.set2Player2 ?: JSONObject.NULL)
                mObj.put("set3Player1", m.set3Player1 ?: JSONObject.NULL)
                mObj.put("set3Player2", m.set3Player2 ?: JSONObject.NULL)
                mObj.put("courtType", m.courtType)
                mObj.put("durationMinutes", m.durationMinutes)
                mObj.put("notes", m.notes)
                mObj.put("setsSequence", m.setsSequence)
                mObj.put("isRetired", m.isRetired)
                mObj.put("retiringPlayer", m.retiringPlayer ?: JSONObject.NULL)
                mObj.put("timestamp", m.timestamp)
                matchesArray.put(mObj)
            }
            rootJson.put("matches", matchesArray)

            onResult(rootJson.toString(2))
        }
    }

    fun importDataJson(jsonString: String, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val rootJson = JSONObject(jsonString)
                val playersArray = rootJson.optJSONArray("players") ?: JSONArray()
                val matchesArray = rootJson.optJSONArray("matches") ?: JSONArray()

                var importedPlayers = 0
                var importedMatches = 0

                for (i in 0 until playersArray.length()) {
                    val pObj = playersArray.getJSONObject(i)
                    val p = PlayerEntity(
                        id = pObj.optLong("id", 0L),
                        name = pObj.getString("name"),
                        hand = pObj.optString("hand", "Pravák"),
                        style = pObj.optString("style", "Univerzál"),
                        skillLevel = pObj.optString("skillLevel", "Pokročilý"),
                        colorHex = pObj.optString("colorHex", "#2E7D32"),
                        notes = pObj.optString("notes", ""),
                        avatarIcon = pObj.optString("avatarIcon", "🏸")
                    )
                    repository.insertPlayer(p)
                    importedPlayers++
                }

                for (i in 0 until matchesArray.length()) {
                    val mObj = matchesArray.getJSONObject(i)
                    val p3 = if (mObj.isNull("player3Id")) null else mObj.optLong("player3Id")
                    val p4 = if (mObj.isNull("player4Id")) null else mObj.optLong("player4Id")
                    val s2p1 = if (mObj.isNull("set2Player1")) null else mObj.optInt("set2Player1")
                    val s2p2 = if (mObj.isNull("set2Player2")) null else mObj.optInt("set2Player2")
                    val s3p1 = if (mObj.isNull("set3Player1")) null else mObj.optInt("set3Player1")
                    val s3p2 = if (mObj.isNull("set3Player2")) null else mObj.optInt("set3Player2")
                    val retPlayer = if (mObj.isNull("retiringPlayer")) null else mObj.optInt("retiringPlayer")
                    val m = MatchEntity(
                        id = mObj.optLong("id", 0L),
                        matchType = mObj.optString("matchType", "SINGLES"),
                        category = mObj.optString("category", "Přátelský"),
                        player1Id = mObj.getLong("player1Id"),
                        player2Id = mObj.getLong("player2Id"),
                        player3Id = p3,
                        player4Id = p4,
                        setsWinner = mObj.optInt("setsWinner", 1),
                        scoreSetsPlayer1 = mObj.optInt("scoreSetsPlayer1", 0),
                        scoreSetsPlayer2 = mObj.optInt("scoreSetsPlayer2", 0),
                        set1Player1 = mObj.optInt("set1Player1", 0),
                        set1Player2 = mObj.optInt("set1Player2", 0),
                        set2Player1 = s2p1,
                        set2Player2 = s2p2,
                        set3Player1 = s3p1,
                        set3Player2 = s3p2,
                        courtType = mObj.optString("courtType", "Hala"),
                        durationMinutes = mObj.optInt("durationMinutes", 30),
                        notes = mObj.optString("notes", ""),
                        setsSequence = mObj.optString("setsSequence", ""),
                        isRetired = mObj.optBoolean("isRetired", false),
                        retiringPlayer = retPlayer,
                        timestamp = mObj.optLong("timestamp", System.currentTimeMillis())
                    )
                    repository.insertMatch(m)
                    importedMatches++
                }

                triggerCloudSync()
                onComplete(true, "Úspěšně importováno $importedPlayers hráčů a $importedMatches zápasů!")
            } catch (e: Exception) {
                onComplete(false, "Chyba při zpracování zálohy: ${e.localizedMessage}")
            }
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
            val player = PlayerEntity(
                name = name.trim(),
                hand = hand,
                style = style,
                skillLevel = skillLevel,
                colorHex = colorHex,
                notes = notes.trim(),
                avatarIcon = avatarIcon
            )
            val generatedId = repository.insertPlayer(player)
            val playerWithId = player.copy(id = generatedId)

            val roomId = _googleAccount.value.syncRoomId
            if (roomId.isNotBlank()) {
                try {
                    val map = mapOf(
                        "id" to playerWithId.id,
                        "name" to playerWithId.name,
                        "hand" to playerWithId.hand,
                        "style" to playerWithId.style,
                        "skillLevel" to playerWithId.skillLevel,
                        "colorHex" to playerWithId.colorHex,
                        "notes" to playerWithId.notes,
                        "avatarIcon" to playerWithId.avatarIcon
                    )
                    FirebaseFirestore.getInstance()
                        .collection("groups").document(roomId)
                        .collection("players").document(playerWithId.id.toString())
                        .set(map, SetOptions.merge())
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun deletePlayer(player: PlayerEntity) {
        viewModelScope.launch {
            repository.deletePlayer(player)
            val roomId = _googleAccount.value.syncRoomId
            if (roomId.isNotBlank()) {
                try {
                    FirebaseFirestore.getInstance()
                        .collection("groups").document(roomId)
                        .collection("players").document(player.id.toString())
                        .delete()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun updatePlayer(player: PlayerEntity) {
        viewModelScope.launch {
            repository.updatePlayer(player)
            val roomId = _googleAccount.value.syncRoomId
            if (roomId.isNotBlank()) {
                try {
                    val map = mapOf(
                        "id" to player.id,
                        "name" to player.name,
                        "hand" to player.hand,
                        "style" to player.style,
                        "skillLevel" to player.skillLevel,
                        "colorHex" to player.colorHex,
                        "notes" to player.notes,
                        "avatarIcon" to player.avatarIcon
                    )
                    FirebaseFirestore.getInstance()
                        .collection("groups").document(roomId)
                        .collection("players").document(player.id.toString())
                        .set(map, SetOptions.merge())
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun addMatch(match: MatchEntity) {
        viewModelScope.launch {
            val generatedId = repository.insertMatch(match)
            val matchWithId = if (match.id == 0L) match.copy(id = generatedId) else match
            val roomId = _googleAccount.value.syncRoomId
            if (roomId.isNotBlank()) {
                try {
                    val m = matchWithId
                    val map = mapOf<String, Any?>(
                        "id" to m.id,
                        "matchType" to m.matchType,
                        "category" to m.category,
                        "player1Id" to m.player1Id,
                        "player2Id" to m.player2Id,
                        "player3Id" to m.player3Id,
                        "player4Id" to m.player4Id,
                        "setsWinner" to m.setsWinner,
                        "scoreSetsPlayer1" to m.scoreSetsPlayer1,
                        "scoreSetsPlayer2" to m.scoreSetsPlayer2,
                        "set1Player1" to m.set1Player1,
                        "set1Player2" to m.set1Player2,
                        "set2Player1" to m.set2Player1,
                        "set2Player2" to m.set2Player2,
                        "set3Player1" to m.set3Player1,
                        "set3Player2" to m.set3Player2,
                        "courtType" to m.courtType,
                        "durationMinutes" to m.durationMinutes,
                        "notes" to m.notes,
                        "setsSequence" to m.setsSequence,
                        "isRetired" to m.isRetired,
                        "retiringPlayer" to m.retiringPlayer,
                        "timestamp" to m.timestamp
                    )
                    FirebaseFirestore.getInstance()
                        .collection("groups").document(roomId)
                        .collection("matches").document(m.id.toString())
                        .set(map, SetOptions.merge())
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun updateMatch(match: MatchEntity) {
        viewModelScope.launch {
            repository.insertMatch(match)
            val roomId = _googleAccount.value.syncRoomId
            if (roomId.isNotBlank()) {
                try {
                    val m = match
                    val map = mapOf<String, Any?>(
                        "id" to m.id,
                        "matchType" to m.matchType,
                        "category" to m.category,
                        "player1Id" to m.player1Id,
                        "player2Id" to m.player2Id,
                        "player3Id" to m.player3Id,
                        "player4Id" to m.player4Id,
                        "setsWinner" to m.setsWinner,
                        "scoreSetsPlayer1" to m.scoreSetsPlayer1,
                        "scoreSetsPlayer2" to m.scoreSetsPlayer2,
                        "set1Player1" to m.set1Player1,
                        "set1Player2" to m.set1Player2,
                        "set2Player1" to m.set2Player1,
                        "set2Player2" to m.set2Player2,
                        "set3Player1" to m.set3Player1,
                        "set3Player2" to m.set3Player2,
                        "courtType" to m.courtType,
                        "durationMinutes" to m.durationMinutes,
                        "notes" to m.notes,
                        "setsSequence" to m.setsSequence,
                        "isRetired" to m.isRetired,
                        "retiringPlayer" to m.retiringPlayer,
                        "timestamp" to m.timestamp
                    )
                    FirebaseFirestore.getInstance()
                        .collection("groups").document(roomId)
                        .collection("matches").document(m.id.toString())
                        .set(map, SetOptions.merge())
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun deleteMatch(match: MatchEntity) {
        viewModelScope.launch {
            repository.deleteMatch(match)
            val roomId = _googleAccount.value.syncRoomId
            if (roomId.isNotBlank()) {
                try {
                    FirebaseFirestore.getInstance()
                        .collection("groups").document(roomId)
                        .collection("matches").document(match.id.toString())
                        .delete()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
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

            val allSets = m.getAllSetScores()
            val mPtsP1 = allSets.sumOf { it.first }
            val mPtsP2 = allSets.sumOf { it.second }

            if (isP1Team) {
                setsWon += m.scoreSetsPlayer1
                setsLost += m.scoreSetsPlayer2
                ptsScored += mPtsP1
                ptsConceded += mPtsP2
            } else {
                setsWon += m.scoreSetsPlayer2
                setsLost += m.scoreSetsPlayer1
                ptsScored += mPtsP2
                ptsConceded += mPtsP1
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

    fun calculateLast30DaysStats(player: PlayerEntity, matchOptions: List<MatchEntity>): PlayerStats {
        val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000L)
        val recentMatches = matchOptions.filter { it.timestamp >= thirtyDaysAgo }
        return calculatePlayerStats(player, recentMatches)
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

    fun calculateAdvancedStats(
        player: PlayerEntity,
        playersList: List<PlayerEntity>,
        matchOptions: List<MatchEntity>
    ): PlayerAdvancedStats {
        val playerMatches = matchOptions.filter {
            it.player1Id == player.id || it.player2Id == player.id ||
            it.player3Id == player.id || it.player4Id == player.id
        }

        if (playerMatches.isEmpty()) {
            return PlayerAdvancedStats(
                totalMatches = 0,
                achievements = listOf(
                    createAchievement("veteran", "🚀", "Veterán kurtů", "Celkem odehráno 0 zápasů", getTier(0, 5, 15, 30, 60), 0, "zápasů"),
                    createAchievement("streak_king", "🔥", "Nezastavitelná série", "Rekordní série: 0 výher v řadě", getTier(0, 2, 4, 7, 12), 0, "výher"),
                    createAchievement("three_set_king", "👑", "Král třísetových bitev", "0 vyhraných rozhodujících setů z 0", getTier(0, 1, 3, 7, 15), 0, "výher"),
                    createAchievement("comeback_master", "🔄", "Mistr obratů", "0x otočený zápas ze stavu 0:1 na sety", getTier(0, 1, 3, 6, 12), 0, "obratů"),
                    createAchievement("crusher", "⚡", "Drtivý válec", "0 setů vyhraných drtivým rozdílem (10+ bodů)", getTier(0, 1, 4, 10, 20), 0, "setů"),
                    createAchievement("clutch_master", "🎯", "Pevné nervy v koncovce", "0 vyhraných dramatických setů v prodloužení (22:20+)", getTier(0, 1, 3, 7, 15), 0, "setů"),
                    createAchievement("clean_sweep", "🧹", "Čisté konto", "0 výher bez jediné ztráty setu (2:0)", getTier(0, 2, 6, 15, 30), 0, "výher"),
                    createAchievement("pan_ztp", "♿", "Pan ZTP", "0x skrečovaný (předčasně vzdanný) zápas", getTier(0, 1, 2, 4, 8), 0, "skrečů"),
                    createAchievement("iron_man", "🛡️", "Železný hráč", "Maratonské bodové bitvy", getTier(0, 65, 85, 105, 125), 0, "bodů")
                )
            )
        }

        val sortedMatches = playerMatches.sortedBy { it.timestamp }
        val dateFmt = SimpleDateFormat("d. M.", Locale("cs", "CZ"))

        var maxWinStreak = 0
        var tempWinStreak = 0
        var currentStreakType: String? = null
        var currentStreakCount = 0

        var threeSetsPlayed = 0
        var threeSetsWon = 0
        var cleanSweepsCount = 0
        var comebackWins = 0
        var clutchSetsWon = 0
        var blowoutSetsWon = 0
        var maxMatchPoints = 0
        var maxMatchPointsDetail: String? = null
        var retiredCount = 0

        val oppMap = mutableMapOf<Long, RivalStat>()

        val processedMatches = sortedMatches.map { m ->
            val isP1Team = (m.player1Id == player.id || m.player3Id == player.id)
            val w1 = if (m.isRetired) (m.setsWinner == 1) else (m.scoreSetsPlayer1 > m.scoreSetsPlayer2)
            val isWin = if (isP1Team) w1 else !w1
            val mySets = if (isP1Team) m.scoreSetsPlayer1 else m.scoreSetsPlayer2
            val oppSets = if (isP1Team) m.scoreSetsPlayer2 else m.scoreSetsPlayer1
            val oppId = if (isP1Team) m.player2Id else m.player1Id
            val oppObj = playersList.find { it.id == oppId }
            val oppName = oppObj?.name ?: "Hráč #$oppId"

            if (m.isRetired) {
                val didSurrender = (m.retiringPlayer == 1 && isP1Team) ||
                                   (m.retiringPlayer == 2 && !isP1Team) ||
                                   (m.retiringPlayer == null && !isWin)
                if (didSurrender) {
                    retiredCount++
                }
            }

            // Win streak tracking
            if (isWin) {
                tempWinStreak++
                if (tempWinStreak > maxWinStreak) maxWinStreak = tempWinStreak
            } else {
                tempWinStreak = 0
            }

            // Current streak tracking
            if (currentStreakType == null) {
                currentStreakType = if (isWin) "W" else "L"
                currentStreakCount = 1
            } else if ((isWin && currentStreakType == "W") || (!isWin && currentStreakType == "L")) {
                currentStreakCount++
            } else {
                currentStreakType = if (isWin) "W" else "L"
                currentStreakCount = 1
            }

            val rawSets = m.getAllSetScores()
            var totalPtsInMatch = 0

            if (rawSets.isNotEmpty()) {
                if (rawSets.size >= 3 || (mySets + oppSets >= 3)) {
                    threeSetsPlayed++
                    if (isWin) threeSetsWon++
                }

                if (isWin && oppSets == 0 && mySets >= 1) {
                    cleanSweepsCount++
                }

                val firstSet = rawSets[0]
                val myFirstSetScore = if (isP1Team) firstSet.first else firstSet.second
                val oppFirstSetScore = if (isP1Team) firstSet.second else firstSet.first
                if (myFirstSetScore < oppFirstSetScore && isWin) {
                    comebackWins++
                }

                rawSets.forEach { pair ->
                    val myScore = if (isP1Team) pair.first else pair.second
                    val oppScore = if (isP1Team) pair.second else pair.first
                    totalPtsInMatch += (pair.first + pair.second)

                    if (myScore > oppScore) {
                        if ((myScore >= 21 && oppScore >= 20 && (myScore - oppScore <= 2)) || (myScore >= 22)) {
                            clutchSetsWon++
                        }
                        if (oppScore <= 10 || (myScore - oppScore >= 10)) {
                            blowoutSetsWon++
                        }
                    }
                }
            } else {
                if (mySets + oppSets >= 3) {
                    threeSetsPlayed++
                    if (isWin) threeSetsWon++
                }
                if (isWin && oppSets == 0) {
                    cleanSweepsCount++
                }
            }

            if (totalPtsInMatch > maxMatchPoints) {
                maxMatchPoints = totalPtsInMatch
                maxMatchPointsDetail = "$mySets:$oppSets (vs $oppName)"
            }

            if (oppId != 0L) {
                val currentRival = oppMap[oppId] ?: RivalStat(oppId, oppName, 0, 0, 0)
                oppMap[oppId] = currentRival.copy(
                    matches = currentRival.matches + 1,
                    wins = currentRival.wins + (if (isWin) 1 else 0),
                    losses = currentRival.losses + (if (!isWin) 1 else 0)
                )
            }

            FormMatchItem(
                matchId = m.id,
                isWin = isWin,
                mySets = mySets,
                oppSets = oppSets,
                oppName = oppName,
                dateStr = if (m.timestamp > 0) dateFmt.format(Date(m.timestamp)) else "",
                isRetired = m.isRetired
            )
        }

        val recentForm = processedMatches.takeLast(5).reversed()

        val rivalsList = oppMap.values.toList()
        var mostFrequentRival: RivalStat? = null
        var bestOpponent: RivalStat? = null

        if (rivalsList.isNotEmpty()) {
            mostFrequentRival = rivalsList.maxByOrNull { it.matches }
            val qualifiedRivals = rivalsList.filter { it.matches >= 2 }
            if (qualifiedRivals.isNotEmpty()) {
                bestOpponent = qualifiedRivals.maxByOrNull { (it.wins.toFloat() / it.matches) * 100f }
            }
        }

        val achievements = listOf(
            createAchievement("veteran", "🚀", "Veterán kurtů", "Celkem odehráno ${playerMatches.size} zápasů", getTier(playerMatches.size, 5, 15, 30, 60), playerMatches.size, "zápasů"),
            createAchievement("streak_king", "🔥", "Nezastavitelná série", "Rekordní série: $maxWinStreak výher v řadě", getTier(maxWinStreak, 2, 4, 7, 12), maxWinStreak, "výher"),
            createAchievement("three_set_king", "👑", "Král třísetových bitev", "$threeSetsWon vyhraných rozhodujících setů z $threeSetsPlayed", getTier(threeSetsWon, 1, 3, 7, 15), threeSetsWon, "výher"),
            createAchievement("comeback_master", "🔄", "Mistr obratů", "${comebackWins}x otočený zápas ze stavu 0:1 na sety", getTier(comebackWins, 1, 3, 6, 12), comebackWins, "obratů"),
            createAchievement("crusher", "⚡", "Drtivý válec", "$blowoutSetsWon setů vyhraných drtivým rozdílem (10+ bodů)", getTier(blowoutSetsWon, 1, 4, 10, 20), blowoutSetsWon, "setů"),
            createAchievement("clutch_master", "🎯", "Pevné nervy v koncovce", "$clutchSetsWon vyhraných dramatických setů v prodloužení (22:20+)", getTier(clutchSetsWon, 1, 3, 7, 15), clutchSetsWon, "setů"),
            createAchievement("clean_sweep", "🧹", "Čisté konto", "$cleanSweepsCount výher bez jediné ztráty setu (2:0)", getTier(cleanSweepsCount, 2, 6, 15, 30), cleanSweepsCount, "výher"),
            createAchievement("pan_ztp", "♿", "Pan ZTP", "${retiredCount}x skrečovaný (předčasně vzdanný) zápas", getTier(retiredCount, 1, 2, 4, 8), retiredCount, "skrečů"),
            createAchievement("iron_man", "🛡️", "Železný hráč", if (maxMatchPoints > 0) "Rekord: $maxMatchPoints odehraných bodů v 1 zápase" else "Maratonské bodové bitvy", getTier(maxMatchPoints, 65, 85, 105, 125), maxMatchPoints, "bodů")
        )

        return PlayerAdvancedStats(
            totalMatches = playerMatches.size,
            currentStreakType = currentStreakType ?: "W",
            currentStreakCount = currentStreakCount,
            maxWinStreak = maxWinStreak,
            recentForm = recentForm,
            threeSetsPlayed = threeSetsPlayed,
            threeSetsWon = threeSetsWon,
            threeSetsWinRate = if (threeSetsPlayed > 0) (threeSetsWon.toFloat() / threeSetsPlayed) * 100f else 0f,
            cleanSweepsCount = cleanSweepsCount,
            comebackWins = comebackWins,
            clutchSetsWon = clutchSetsWon,
            blowoutSetsWon = blowoutSetsWon,
            maxMatchPoints = maxMatchPoints,
            maxMatchPointsDetail = maxMatchPointsDetail,
            retiredCount = retiredCount,
            mostFrequentRival = mostFrequentRival,
            bestOpponent = bestOpponent,
            achievements = achievements
        )
    }

    private fun createAchievement(
        id: String,
        icon: String,
        title: String,
        desc: String,
        tierInfo: TierInfo,
        current: Int,
        unit: String
    ): AchievementItem {
        return AchievementItem(
            id = id,
            icon = icon,
            title = title,
            desc = desc,
            tierInfo = tierInfo,
            current = current,
            unit = unit
        )
    }

    private fun getTier(current: Int, t1: Int, t2: Int, t3: Int, t4: Int): TierInfo {
        return when {
            current >= t4 -> TierInfo(AchievementTier.DIAMOND, "Diamant", null, t4, 100, true)
            current >= t3 -> TierInfo(AchievementTier.GOLD, "Zlato", t4, t3, minOf(100, ((current.toFloat() / t4) * 100).toInt()), true)
            current >= t2 -> TierInfo(AchievementTier.SILVER, "Stříbro", t3, t2, minOf(100, ((current.toFloat() / t3) * 100).toInt()), true)
            current >= t1 -> TierInfo(AchievementTier.BRONZE, "Bronz", t2, t1, minOf(100, ((current.toFloat() / t2) * 100).toInt()), true)
            else -> TierInfo(AchievementTier.CHALLENGE, "Výzva", t1, t1, minOf(100, ((current.toFloat() / t1) * 100).toInt()), false)
        }
    }
}
