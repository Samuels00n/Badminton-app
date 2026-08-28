package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.MatchEntity
import com.example.data.entity.PlayerEntity
import com.example.ui.components.PlayerAvatar
import com.example.ui.theme.CoralRedLoss
import com.example.ui.theme.ForestGreenContainer
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.NaturalCardBorder
import com.example.ui.viewmodel.AchievementItem
import com.example.ui.viewmodel.AchievementTier
import com.example.ui.viewmodel.BadmintonViewModel
import com.example.ui.viewmodel.FormMatchItem
import com.example.ui.viewmodel.PlayerAdvancedStats
import com.example.ui.viewmodel.PlayerStats
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class LeaderboardEntry(
    val player: PlayerEntity,
    val wins: Int = 0,
    val total: Int = 0,
    val winRate: Float = 0f
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    players: List<PlayerEntity>,
    matches: List<MatchEntity>,
    viewModel: BadmintonViewModel,
    modifier: Modifier = Modifier
) {
    if (players.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, NaturalCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(ForestGreenContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShowChart,
                            contentDescription = null,
                            tint = ForestGreenPrimary,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Žádné statistiky k zobrazení",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Pro zobrazování statistik a analýz musíte mít v aplikaci vytvořeného alespoň jednoho hráče.\n\nPřejděte do karty Hráči a přidejte nového hráče.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }
            }
        }
        return
    }

    var selectedPlayer by remember(players) { mutableStateOf(players.firstOrNull()) }
    var selectedOpponent by remember(players) { mutableStateOf(players.getOrNull(1)) }

    val activePlayer = selectedPlayer ?: players.first()
    val playerStat = viewModel.calculatePlayerStats(activePlayer, matches)
    val last30Stats = viewModel.calculateLast30DaysStats(activePlayer, matches)
    val advStats = viewModel.calculateAdvancedStats(activePlayer, players, matches)

    val h2h = if (selectedOpponent != null && selectedOpponent?.id != activePlayer.id) {
        viewModel.calculateHeadToHead(activePlayer, selectedOpponent!!, matches)
    } else null

    val leaderboardEntries = remember(players, matches) {
        val statsMap = players.associate { p ->
            p.id to LeaderboardEntry(
                player = p,
                wins = 0,
                total = 0
            )
        }.toMutableMap()

        matches.forEach { m ->
            val isP1Win = (m.setsWinner == 1)
            val p1Team = listOfNotNull(m.player1Id, m.player3Id)
            val p2Team = listOfNotNull(m.player2Id, m.player4Id)

            p1Team.forEach { pId ->
                statsMap[pId]?.let { entry ->
                    statsMap[pId] = entry.copy(
                        total = entry.total + 1,
                        wins = entry.wins + (if (isP1Win) 1 else 0)
                    )
                }
            }
            p2Team.forEach { pId ->
                statsMap[pId]?.let { entry ->
                    statsMap[pId] = entry.copy(
                        total = entry.total + 1,
                        wins = entry.wins + (if (!isP1Win) 1 else 0)
                    )
                }
            }
        }

        statsMap.values.map { entry ->
            val rate = if (entry.total > 0) (entry.wins.toFloat() / entry.total) * 100f else 0f
            entry.copy(winRate = rate)
        }.sortedWith(compareByDescending<LeaderboardEntry> { it.winRate }.thenByDescending { it.wins })
    }

    val dateFormat = remember { SimpleDateFormat("d. MMMM yyyy", Locale("cs", "CZ")) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Statistiky & analýza",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Player Selector Dropdown
            Text("Vyberte hráče pro detail:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(4.dp))
            PlayerSelectBox(
                players = players,
                selected = activePlayer,
                onSelect = { selectedPlayer = it }
            )
        }

        // Section 1: Celkový přehled
        item {
            Column {
                Text(
                    text = "Celkový přehled",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Win Rate Card ("Úspěšnost")
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = ForestGreenContainer),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ForestGreenPrimary.copy(alpha = 0.2f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp, horizontal = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Úspěšnost",
                                fontSize = 12.sp,
                                color = ForestGreenPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${playerStat.winRate.toInt()}%",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = ForestGreenPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                color = ForestGreenPrimary.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "${playerStat.wins} výher / ${playerStat.losses} proher",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ForestGreenPrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    // Sets & Points Card ("Sety")
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NaturalCardBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp, horizontal = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Sety",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${playerStat.setsWon} : ${playerStat.setsLost}",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, NaturalCardBorder)
                            ) {
                                Text(
                                    text = "Body: ${playerStat.pointsScored} : ${playerStat.pointsConceded}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section 2: Statistiky za minulý měsíc (30 dní)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, NaturalCardBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            tint = ForestGreenPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Statistiky za minulý měsíc",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (last30Stats.totalMatches == 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "V posledních 30 dnech nebyl odehrán žádný zápas.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    } else {
                        // Grid of 4 key metrics
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Počet zápasů
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "Počet zápasů",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${last30Stats.totalMatches}",
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${last30Stats.wins} výher / ${last30Stats.losses} proher",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }

                            // Úspěšnost
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(ForestGreenContainer)
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "Úspěšnost",
                                        fontSize = 11.sp,
                                        color = ForestGreenPrimary,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${last30Stats.winRate.toInt()}%",
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ForestGreenPrimary
                                    )
                                    Text(
                                        text = if (last30Stats.winRate >= 50f) "Aktivní forma" else "Příležitost k růstu",
                                        fontSize = 10.sp,
                                        color = ForestGreenPrimary.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Sety
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "Sety",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${last30Stats.setsWon} : ${last30Stats.setsLost}",
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    val diffSets = last30Stats.setsWon - last30Stats.setsLost
                                    Text(
                                        text = "Bilance: ${if (diffSets >= 0) "+$diffSets" else "$diffSets"}",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }

                            // Body
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "Body",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${last30Stats.pointsScored} : ${last30Stats.pointsConceded}",
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    val diffPts = last30Stats.pointsScored - last30Stats.pointsConceded
                                    Text(
                                        text = "Skóre bodů: ${if (diffPts >= 0) "+$diffPts" else "$diffPts"}",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section 3: Forma & Série
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, NaturalCardBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = ForestGreenPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Forma & Série",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (advStats.totalMatches == 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Zatím žádné odehrané zápasy pro vyhodnocení formy.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    } else {
                        val isWinStreak = (advStats.currentStreakType == "W" && advStats.currentStreakCount > 0)
                        val isLossStreak = (advStats.currentStreakType == "L" && advStats.currentStreakCount > 0)

                        // 2 Cards: Current streak & Record win streak
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Current Streak
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        if (isWinStreak) ForestGreenContainer
                                        else if (isLossStreak) CoralRedLoss.copy(alpha = 0.12f)
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    )
                                    .border(
                                        1.dp,
                                        if (isWinStreak) ForestGreenPrimary.copy(alpha = 0.3f)
                                        else if (isLossStreak) CoralRedLoss.copy(alpha = 0.3f)
                                        else NaturalCardBorder,
                                        RoundedCornerShape(14.dp)
                                    )
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "AKTUÁLNÍ SÉRIE",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isWinStreak) ForestGreenPrimary else if (isLossStreak) CoralRedLoss else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (isWinStreak) "🔥 ${advStats.currentStreakCount} výher"
                                               else if (isLossStreak) "❄️ ${advStats.currentStreakCount} proher"
                                               else "—",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "v řadě za sebou",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }

                            // Record Win Streak
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .border(1.dp, NaturalCardBorder, RoundedCornerShape(14.dp))
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "REKORDNÍ SÉRIE",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "🏆 ${advStats.maxWinStreak} výher",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFFD4AF37)
                                    )
                                    Text(
                                        text = "nejdelší vítězná šňůra",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Form Guide: Posledních 5 zápasů
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(12.dp)
                        ) {
                            Column {
                                val winsCount = advStats.recentForm.count { it.isWin }
                                val lossCount = advStats.recentForm.count { !it.isWin }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Poslední zápasy (nejnovější vlevo):",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Bilance: ${winsCount}V - ${lossCount}P",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    advStats.recentForm.forEach { formItem ->
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (formItem.isWin) ForestGreenPrimary
                                                    else CoralRedLoss
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = if (formItem.isWin) "V" else "P",
                                                color = Color.White,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // 3 Micro Metric Cards (3-set battles, Clean sweeps, Comebacks)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // 3-setové bitvy
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                                    .border(1.dp, NaturalCardBorder.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "3-setové bitvy",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${advStats.threeSetsWon}/${advStats.threeSetsPlayed}",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = ForestGreenPrimary
                                    )
                                    Text(
                                        text = "${advStats.threeSetsWinRate.toInt()}% výher",
                                        fontSize = 9.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }

                            // Čisté konto 2:0
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                                    .border(1.dp, NaturalCardBorder.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Čisté konto 2:0",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${advStats.cleanSweepsCount}x",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "bez ztráty setu",
                                        fontSize = 9.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }

                            // Obraty 0:1 ➔ 2:1
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                                    .border(1.dp, NaturalCardBorder.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Obraty 0:1➔2:1",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${advStats.comebackWins}x",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFFD4AF37)
                                    )
                                    Text(
                                        text = "otočený zápas",
                                        fontSize = 9.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section 4: Milníky & Ocenění hráče (Achievements)
        item {
            val unlockedCount = advStats.achievements.count { it.tierInfo.unlocked }
            val totalAchievements = advStats.achievements.size

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, NaturalCardBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = Color(0xFFD4AF37),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Milníky & Ocenění hráče",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Surface(
                            color = ForestGreenPrimary.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "$unlockedCount / $totalAchievements Odemčeno",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = ForestGreenPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        advStats.achievements.forEach { achievement ->
                            AchievementCardItem(achievement = achievement)
                        }
                    }
                }
            }
        }

        // Section 5: Zajímavosti o soupeřích
        if (advStats.mostFrequentRival != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NaturalCardBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = ForestGreenPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Zajímavosti o soupeřích",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Most Frequent Rival
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "⚔️ NEJČASTĚJŠÍ RIVAL",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = advStats.mostFrequentRival.name,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${advStats.mostFrequentRival.matches} zápasů (${advStats.mostFrequentRival.wins}V - ${advStats.mostFrequentRival.losses}P)",
                                        fontSize = 10.sp,
                                        color = ForestGreenPrimary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            // Favorite Opponent
                            if (advStats.bestOpponent != null) {
                                val rate = ((advStats.bestOpponent.wins.toFloat() / advStats.bestOpponent.matches) * 100f).toInt()
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(ForestGreenContainer)
                                        .border(1.dp, ForestGreenPrimary.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
                                        .padding(12.dp)
                                    ) {
                                    Column {
                                        Text(
                                            text = "🎯 OBLÍBENÝ SOUPEŘ",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ForestGreenPrimary
                                        )
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Text(
                                            text = advStats.bestOpponent.name,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = ForestGreenPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "Úspěšnost $rate% (${advStats.bestOpponent.wins}/${advStats.bestOpponent.matches})",
                                            fontSize = 10.sp,
                                            color = ForestGreenPrimary.copy(alpha = 0.9f),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section 6: Vzájemná bilance (H2H)
        item {
            val availableOpponents = players.filter { it.id != activePlayer.id }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, NaturalCardBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = null,
                                tint = ForestGreenPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Vzájemná Bilance (H2H)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (availableOpponents.isEmpty()) {
                        Text(
                            text = "Pro porovnání vzájemné bilance je potřeba mít v aplikaci alespoň 2 hráče.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        Text("Porovnat s hráčem:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        PlayerSelectBox(
                            players = availableOpponents,
                            selected = selectedOpponent,
                            onSelect = { selectedOpponent = it }
                        )

                        if (h2h != null) {
                            Spacer(modifier = Modifier.height(16.dp))

                            // Overall H2H Banner
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    PlayerAvatar(
                                        name = activePlayer.name,
                                        colorHex = activePlayer.colorHex,
                                        avatarIcon = activePlayer.avatarIcon,
                                        size = 40.dp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(activePlayer.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("${h2h.player1Wins} výher", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = ForestGreenPrimary)
                                    Text("${h2h.player1Sets} setů", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                }

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("VS", fontWeight = FontWeight.Black, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                                    Text("${h2h.matches.size} zápasů", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ForestGreenPrimary)
                                }

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    PlayerAvatar(
                                        name = selectedOpponent?.name ?: "S",
                                        colorHex = selectedOpponent?.colorHex ?: "#BC4749",
                                        avatarIcon = selectedOpponent?.avatarIcon ?: "🏸",
                                        size = 40.dp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(selectedOpponent?.name ?: "Soupeř", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("${h2h.player2Wins} výher", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = CoralRedLoss)
                                    Text("${h2h.player2Sets} setů", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                }
                            }

                            // Detailed list of H2H matches
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                text = "Výčet všech vzájemných zápasů (${h2h.matches.size}):",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            if (h2h.matches.isEmpty()) {
                                Text(
                                    text = "Mezi těmito hráči nebyly odehrány žádné vzájemné zápasy.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    h2h.matches.forEach { match ->
                                        val isP1Team1 = (match.player1Id == activePlayer.id || match.player3Id == activePlayer.id)
                                        val activePlayerWon = if (isP1Team1) (match.setsWinner == 1) else (match.setsWinner == 2)
                                        val activePlayerSets = if (isP1Team1) match.scoreSetsPlayer1 else match.scoreSetsPlayer2
                                        val opponentSets = if (isP1Team1) match.scoreSetsPlayer2 else match.scoreSetsPlayer1

                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                                .border(1.dp, NaturalCardBorder, RoundedCornerShape(14.dp))
                                                .padding(12.dp)
                                        ) {
                                            Column {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = "${dateFormat.format(Date(match.timestamp))} • ${match.category} (${match.courtType})",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                                    )
                                                }

                                                Spacer(modifier = Modifier.height(8.dp))

                                                // Score summary row
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier.weight(1f)
                                                    ) {
                                                        Text(
                                                            text = activePlayer.name,
                                                            fontWeight = if (activePlayerWon) FontWeight.ExtraBold else FontWeight.Normal,
                                                            fontSize = 13.sp
                                                        )
                                                        Text(
                                                            text = " vs ",
                                                            fontSize = 12.sp,
                                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                                        )
                                                        Text(
                                                            text = selectedOpponent?.name ?: "Soupeř",
                                                            fontWeight = if (!activePlayerWon) FontWeight.ExtraBold else FontWeight.Normal,
                                                            fontSize = 13.sp
                                                        )
                                                    }

                                                    Text(
                                                        text = "$activePlayerSets : $opponentSets",
                                                        fontSize = 16.sp,
                                                        fontWeight = FontWeight.Black,
                                                        color = if (activePlayerWon) ForestGreenPrimary else CoralRedLoss
                                                    )
                                                }

                                                Spacer(modifier = Modifier.height(6.dp))

                                                // Set scores breakdown chips
                                                val allSets = match.getAllSetScores()
                                                if (allSets.isNotEmpty()) {
                                                    Row(
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        allSets.forEachIndexed { index, pair ->
                                                            val sStr = if (isP1Team1) "${pair.first}:${pair.second}" else "${pair.second}:${pair.first}"
                                                            SetScoreChip(label = "${index + 1}. set", score = sStr)
                                                        }
                                                    }
                                                }

                                                if (match.isRetired) {
                                                    val retName = if (match.retiringPlayer == 1) {
                                                        if (isP1Team1) activePlayer.name else (selectedOpponent?.name ?: "Soupeř")
                                                    } else {
                                                        if (isP1Team1) (selectedOpponent?.name ?: "Soupeř") else activePlayer.name
                                                    }
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .clip(RoundedCornerShape(8.dp))
                                                            .background(CoralRedLoss.copy(alpha = 0.12f))
                                                            .border(1.dp, CoralRedLoss.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                                            .padding(horizontal = 8.dp, vertical = 5.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Warning,
                                                            contentDescription = null,
                                                            tint = CoralRedLoss,
                                                            modifier = Modifier.size(13.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text(
                                                            text = "Zápas nedohrán – hráč $retName skrečoval",
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Medium,
                                                            color = MaterialTheme.colorScheme.onSurface
                                                        )
                                                    }
                                                }

                                                if (match.notes.isNotBlank()) {
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = "Poznámka: ${match.notes}",
                                                        fontSize = 11.sp,
                                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section 7: Celkový žebříček skupiny
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, NaturalCardBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFD4AF37),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Celkový žebříček skupiny",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (leaderboardEntries.isEmpty()) {
                        Text(
                            text = "Žádná data pro žebříček.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            leaderboardEntries.forEachIndexed { idx, item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = "#${idx + 1}",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 13.sp,
                                            color = ForestGreenPrimary,
                                            modifier = Modifier.width(26.dp)
                                        )
                                        PlayerAvatar(
                                            name = item.player.name,
                                            colorHex = item.player.colorHex,
                                            avatarIcon = item.player.avatarIcon,
                                            size = 34.dp
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = item.player.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "${item.wins} výher / ${item.total} zápasů",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                            )
                                        }
                                    }

                                    Text(
                                        text = "${String.format(Locale.US, "%.1f", item.winRate)} %",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 14.sp,
                                        color = ForestGreenPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun AchievementCardItem(achievement: AchievementItem) {
    val info = achievement.tierInfo
    val isUnlocked = info.unlocked

    val tierColor = when (info.tier) {
        AchievementTier.DIAMOND -> Color(0xFF00D2FF)
        AchievementTier.GOLD -> Color(0xFFFFD700)
        AchievementTier.SILVER -> Color(0xFFC0C0C0)
        AchievementTier.BRONZE -> Color(0xFFCD7F32)
        AchievementTier.CHALLENGE -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (isUnlocked) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
            .border(
                1.dp,
                if (isUnlocked) tierColor.copy(alpha = 0.4f) else NaturalCardBorder.copy(alpha = 0.4f),
                RoundedCornerShape(14.dp)
            )
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Icon
            Text(
                text = achievement.icon,
                fontSize = 26.sp,
                modifier = Modifier.padding(end = 12.dp, top = 2.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = achievement.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Surface(
                        color = tierColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = info.label,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = tierColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = achievement.desc,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    lineHeight = 14.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Progress Bar
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction = (info.progress / 100f).coerceIn(0f, 1f))
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    if (isUnlocked) ForestGreenPrimary
                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                                )
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = if (info.next != null) "${achievement.current} / ${info.next}" else "${achievement.current} ✓ Max",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isUnlocked) ForestGreenPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
private fun SetScoreChip(label: String, score: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, NaturalCardBorder, RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = "$label $score",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlayerSelectBox(
    players: List<PlayerEntity>,
    selected: PlayerEntity?,
    onSelect: (PlayerEntity) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selected?.name ?: "Vyberte hráče",
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            players.forEach { p ->
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            PlayerAvatar(
                                name = p.name,
                                colorHex = p.colorHex,
                                avatarIcon = p.avatarIcon,
                                size = 24.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(p.name, fontWeight = FontWeight.Bold)
                        }
                    },
                    onClick = {
                        onSelect(p)
                        expanded = false
                    }
                )
            }
        }
    }
}
