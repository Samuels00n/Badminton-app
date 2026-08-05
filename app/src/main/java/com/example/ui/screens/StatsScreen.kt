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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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

                    Text("Porovnat s hráče:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    PlayerSelectBox(
                        players = players.filter { it.id != activePlayer.id },
                        selected = selectedOpponent,
                        onSelect = { selectedOpponent = it }
                    )

                    if (h2h != null) {
                        Spacer(modifier = Modifier.height(16.dp))

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
                                PlayerAvatar(name = activePlayer.name, colorHex = activePlayer.colorHex, size = 36.dp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(activePlayer.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("${h2h.player1Wins} výher", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = ForestGreenPrimary)
                            }

                            Text("VS", fontWeight = FontWeight.Black, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                PlayerAvatar(name = selectedOpponent?.name ?: "S", colorHex = selectedOpponent?.colorHex ?: "#BC4749", size = 36.dp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(selectedOpponent?.name ?: "Soupeř", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("${h2h.player2Wins} výher", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = CoralRedLoss)
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
                            PlayerAvatar(name = p.name, colorHex = p.colorHex, size = 24.dp)
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
