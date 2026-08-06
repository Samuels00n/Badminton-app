package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.MatchEntity
import com.example.data.entity.PlayerEntity
import com.example.ui.components.PlayerAvatar
import com.example.ui.components.ScoreBoard
import com.example.ui.theme.CoralRedLoss
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.viewmodel.LiveMatchState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMatchScreen(
    players: List<PlayerEntity>,
    liveMatchState: LiveMatchState,
    onStartLiveMatch: (PlayerEntity, PlayerEntity, PlayerEntity?, PlayerEntity?, String, String, String) -> Unit,
    onAddPoint: (Int) -> Unit,
    onUndoPoint: () -> Unit,
    onSaveLiveMatch: () -> Unit,
    onCancelLiveMatch: () -> Unit,
    onSaveManualMatch: (MatchEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Ruční Zadání, 1 = Živé Skóre

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Záznam badmintonového zápasu",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Tab Selector
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .testTag("add_match_tab_row")
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Ruční zadání", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("tab_manual_entry")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Živé skóre", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("tab_live_score")
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (selectedTab == 0) {
            // TAB 1: Manual Result Entry Form
            ManualMatchForm(
                players = players,
                onSave = onSaveManualMatch
            )
        } else {
            // TAB 2: Live Score
            if (liveMatchState.player1 != null && liveMatchState.player2 != null) {
                // Active Live Scoreboard
                ScoreBoard(
                    liveMatch = liveMatchState,
                    onAddPoint = onAddPoint,
                    onUndo = onUndoPoint,
                    onSaveMatch = onSaveLiveMatch,
                    onCancel = onCancelLiveMatch
                )
            } else {
                // Live Match Setup Form
                LiveMatchSetupForm(
                    players = players,
                    onStart = onStartLiveMatch
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LiveMatchSetupForm(
    players: List<PlayerEntity>,
    onStart: (PlayerEntity, PlayerEntity, PlayerEntity?, PlayerEntity?, String, String, String) -> Unit
) {
    if (players.size < 2) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Pro spuštění zápasu musíte mít vytvořeny alespoň 2 hráče.",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
        return
    }

    var matchType by remember { mutableStateOf("SINGLES") } // SINGLES or DOUBLES
    var category by remember { mutableStateOf("Přátelský") }
    var courtType by remember { mutableStateOf("Hala") }

    var selectedP1 by remember { mutableStateOf<PlayerEntity?>(players.firstOrNull()) }
    var selectedP2 by remember { mutableStateOf<PlayerEntity?>(players.getOrNull(1)) }
    var selectedP3 by remember { mutableStateOf<PlayerEntity?>(null) } // Doubles P1 partner
    var selectedP4 by remember { mutableStateOf<PlayerEntity?>(null) } // Doubles P2 partner

    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Nové živé utkání", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    Spacer(modifier = Modifier.height(12.dp))

                    // Match Type Selector (1v1 or 2v2)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TypeChip(
                            label = "Dvouhra (1v1)",
                            selected = matchType == "SINGLES",
                            onClick = { matchType = "SINGLES" },
                            modifier = Modifier.weight(1f)
                        )
                        TypeChip(
                            label = "Čtyřhra (2v2)",
                            selected = matchType == "DOUBLES",
                            onClick = { matchType = "DOUBLES" },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Player Selection
                    Text("Tým / Hráč 1:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    PlayerDropdown(
                        players = players,
                        selectedPlayer = selectedP1,
                        onSelect = { selectedP1 = it },
                        label = "První hráč"
                    )

                    if (matchType == "DOUBLES") {
                        Spacer(modifier = Modifier.height(8.dp))
                        PlayerDropdown(
                            players = players.filter { it.id != selectedP1?.id },
                            selectedPlayer = selectedP3,
                            onSelect = { selectedP3 = it },
                            label = "Spoluhráč v týmu 1"
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Tým / Hráč 2 (Soupeř):", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    PlayerDropdown(
                        players = players.filter { it.id != selectedP1?.id && it.id != selectedP3?.id },
                        selectedPlayer = selectedP2,
                        onSelect = { selectedP2 = it },
                        label = "Druhý hráč"
                    )

                    if (matchType == "DOUBLES") {
                        Spacer(modifier = Modifier.height(8.dp))
                        PlayerDropdown(
                            players = players.filter { it.id != selectedP1?.id && it.id != selectedP3?.id && it.id != selectedP2?.id },
                            selectedPlayer = selectedP4,
                            onSelect = { selectedP4 = it },
                            label = "Spoluhráč v týmu 2"
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Category & Court
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Kategorie", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            SimpleSelect(
                                options = listOf("Přátelský", "Turnaj", "Liga"),
                                selected = category,
                                onSelect = { category = it }
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Povrch", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            SimpleSelect(
                                options = listOf("Hala", "Hala / Mat", "Venku", "Jiné"),
                                selected = courtType,
                                onSelect = { courtType = it }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            val p1 = selectedP1 ?: return@Button
                            val p2 = selectedP2 ?: return@Button
                            onStart(p1, p2, selectedP3, selectedP4, matchType, category, courtType)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("start_live_match_btn")
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Spustit živé počítadlo skóre", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ManualMatchForm(
    players: List<PlayerEntity>,
    onSave: (MatchEntity) -> Unit
) {
    if (players.size < 2) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("K dispozici musí být alespoň 2 hráči pro zapsání výsledku.", fontWeight = FontWeight.Bold)
            }
        }
        return
    }

    var matchType by remember { mutableStateOf("SINGLES") }
    var category by remember { mutableStateOf("Přátelský") }
    var courtType by remember { mutableStateOf("Hala") }

    var selectedP1 by remember { mutableStateOf<PlayerEntity?>(players.firstOrNull()) }
    var selectedP2 by remember { mutableStateOf<PlayerEntity?>(players.getOrNull(1)) }

    var setsList by remember { mutableStateOf(listOf(Pair("21", "18"), Pair("21", "15"))) }

    var notes by remember { mutableStateOf("") }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Ruční zadání výsledků setů", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Hráč / Tým 1:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    PlayerDropdown(players = players, selectedPlayer = selectedP1, onSelect = { selectedP1 = it }, label = "Hráč 1")

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Hráč / Tým 2 (Soupeř):", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    PlayerDropdown(players = players.filter { it.id != selectedP1?.id }, selectedPlayer = selectedP2, onSelect = { selectedP2 = it }, label = "Hráč 2")

                    Spacer(modifier = Modifier.height(16.dp))

                    // Set Inputs
                    Text("Skóre po setech (body):", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    setsList.forEachIndexed { index, pair ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("${index + 1}. Set:", fontWeight = FontWeight.Bold, modifier = Modifier.width(55.dp))
                            OutlinedTextField(
                                value = pair.first,
                                onValueChange = { newValue ->
                                    val updated = setsList.toMutableList()
                                    updated[index] = Pair(newValue, pair.second)
                                    setsList = updated
                                },
                                label = { Text("Hráč 1") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("set${index + 1}_p1_input")
                            )
                            Text(":", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                            OutlinedTextField(
                                value = pair.second,
                                onValueChange = { newValue ->
                                    val updated = setsList.toMutableList()
                                    updated[index] = Pair(pair.first, newValue)
                                    setsList = updated
                                },
                                label = { Text("Hráč 2") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("set${index + 1}_p2_input")
                            )
                            if (setsList.size > 1) {
                                IconButton(
                                    onClick = {
                                        val updated = setsList.toMutableList()
                                        updated.removeAt(index)
                                        setsList = updated
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Odebrat set",
                                        tint = CoralRedLoss
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    OutlinedButton(
                        onClick = {
                            setsList = setsList + Pair("", "")
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("add_set_button")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("+ Přidat další set", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Poznámky k zápasu") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            val p1 = selectedP1 ?: return@Button
                            val p2 = selectedP2 ?: return@Button

                            val parsedSets = setsList.mapNotNull { (s1Str, s2Str) ->
                                val s1 = s1Str.toIntOrNull()
                                val s2 = s2Str.toIntOrNull()
                                if (s1 != null && s2 != null) Pair(s1, s2) else null
                            }

                            val finalSets = if (parsedSets.isNotEmpty()) parsedSets else listOf(Pair(21, 18), Pair(21, 15))

                            var setsP1 = 0
                            var setsP2 = 0
                            finalSets.forEach { (s1, s2) ->
                                if (s1 > s2) setsP1++ else if (s2 > s1) setsP2++
                            }

                            val winner = if (setsP1 >= setsP2) 1 else 2
                            val setsSequence = finalSets.joinToString(",") { "${it.first}:${it.second}" }

                            val match = MatchEntity(
                                matchType = matchType,
                                category = category,
                                player1Id = p1.id,
                                player2Id = p2.id,
                                setsWinner = winner,
                                scoreSetsPlayer1 = setsP1,
                                scoreSetsPlayer2 = setsP2,
                                set1Player1 = finalSets.getOrNull(0)?.first ?: 21,
                                set1Player2 = finalSets.getOrNull(0)?.second ?: 18,
                                set2Player1 = finalSets.getOrNull(1)?.first,
                                set2Player2 = finalSets.getOrNull(1)?.second,
                                set3Player1 = finalSets.getOrNull(2)?.first,
                                set3Player2 = finalSets.getOrNull(2)?.second,
                                courtType = courtType,
                                notes = notes,
                                setsSequence = setsSequence,
                                timestamp = System.currentTimeMillis()
                            )

                            onSave(match)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("save_manual_match_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Uložit zápas do historie", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun TypeChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.Bold,
            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlayerDropdown(
    players: List<PlayerEntity>,
    selectedPlayer: PlayerEntity?,
    onSelect: (PlayerEntity) -> Unit,
    label: String
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedPlayer?.name ?: "Vyberte hráče",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SimpleSelect(
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selected,
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
            options.forEach { opt ->
                DropdownMenuItem(
                    text = { Text(opt) },
                    onClick = {
                        onSelect(opt)
                        expanded = false
                    }
                )
            }
        }
    }
}
