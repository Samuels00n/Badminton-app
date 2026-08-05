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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.SportsTennis
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.MatchEntity
import com.example.data.entity.PlayerEntity
import com.example.ui.components.BarChartItem
import com.example.ui.components.CustomBarChart
import com.example.ui.components.PlayerAvatar
import com.example.ui.theme.CoralRedLoss
import com.example.ui.theme.ForestGreenContainer
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.NaturalCardBorder
import com.example.ui.viewmodel.BadmintonViewModel
import com.example.ui.viewmodel.PlayerStats
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Pro zobrazovaní statistik musíte mít alespoň jednoho hráče.")
        }
        return
    }

    var selectedPlayer by remember(players) { mutableStateOf(players.firstOrNull()) }
    var selectedOpponent by remember(players) { mutableStateOf(players.getOrNull(1)) }

    val activePlayer = selectedPlayer ?: players.first()
    val playerStat = viewModel.calculatePlayerStats(activePlayer, matches)
    val monthlyStats = viewModel.calculateMonthlyStatsForPlayer(activePlayer, matches)

    val h2h = if (selectedOpponent != null && selectedOpponent?.id != activePlayer.id) {
        viewModel.calculateHeadToHead(activePlayer, selectedOpponent!!, matches)
    } else null

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
                text = "Statistiky & Analýza",
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

        // Summary Performance Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Win Rate Card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = ForestGreenContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Úspěšnost", fontSize = 12.sp, color = ForestGreenPrimary, fontWeight = FontWeight.Bold)
                        Text(
                            text = "${playerStat.winRate.toInt()}%",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = ForestGreenPrimary
                        )
                        Text(
                            text = "${playerStat.wins} výher / ${playerStat.losses} proher",
                            fontSize = 11.sp,
                            color = ForestGreenPrimary.copy(alpha = 0.8f)
                        )
                    }
                }

                // Sets & Points Card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Skóre Sety", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = "${playerStat.setsWon} : ${playerStat.setsLost}",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Body: ${playerStat.pointsScored} / ${playerStat.pointsConceded}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }

        // Monthly Trend Bar Chart
        if (monthlyStats.isNotEmpty()) {
            item {
                val chartItems = monthlyStats.map {
                    BarChartItem(
                        label = it.monthYear,
                        value = it.winRate
                    )
                }

                CustomBarChart(
                    items = chartItems,
                    title = "Forma v čase (% výher za měsíc)",
                    unit = "%",
                    barColor = ForestGreenPrimary
                )
            }
        }

        // Head to Head Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                                imageVector = Icons.Default.ShowChart,
                                contentDescription = null,
                                tint = ForestGreenPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Vzájemná Bilance (H2H)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Porovnat s hráčem:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    PlayerSelectBox(
                        players = players.filter { it.id != activePlayer.id },
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
                                .background(MaterialTheme.colorScheme.surfaceVariant)
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Výčet všech vzájemných zápasů (${h2h.matches.size}):",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

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

                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(if (activePlayerWon) ForestGreenContainer else Color(0xFFFFEBEE))
                                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = if (activePlayerWon) "Výhra ${activePlayer.name}" else "Výhra ${selectedOpponent?.name}",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (activePlayerWon) ForestGreenPrimary else CoralRedLoss
                                                    )
                                                }
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
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                val s1Str = if (isP1Team1) "${match.set1Player1}:${match.set1Player2}" else "${match.set1Player2}:${match.set1Player1}"
                                                SetScoreChip(label = "1. set", score = s1Str)

                                                if (match.set2Player1 != null && match.set2Player2 != null) {
                                                    val s2Str = if (isP1Team1) "${match.set2Player1}:${match.set2Player2}" else "${match.set2Player2}:${match.set2Player1}"
                                                    SetScoreChip(label = "2. set", score = s2Str)
                                                }

                                                if (match.set3Player1 != null && match.set3Player2 != null) {
                                                    val s3Str = if (isP1Team1) "${match.set3Player1}:${match.set3Player2}" else "${match.set3Player2}:${match.set3Player1}"
                                                    SetScoreChip(label = "3. set", score = s3Str)
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

        item {
            Spacer(modifier = Modifier.height(24.dp))
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
