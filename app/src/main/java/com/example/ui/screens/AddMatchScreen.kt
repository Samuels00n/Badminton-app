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
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.material3.MaterialTheme
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
            text = "Záznam Badmintonového Zápasu",
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
                text = { Text("Ruční Zadání", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("tab_manual_entry")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Živé Skóre", fontWeight = FontWeight.Bold) },
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

    var set1P1 by remember { mutableStateOf("21") }
    var set1P2 by remember { mutableStateOf("18") }

    var set2P1 by remember { mutableStateOf("21") }
    var set2P2 by remember { mutableStateOf("15") }

    var set3P1 by remember { mutableStateOf("") }
    var set3P2 by remember { mutableStateOf("") }

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

                    // Set 1 Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("1. Set:", fontWeight = FontWeight.Bold, modifier = Modifier.width(55.dp))
                        OutlinedTextField(
                            value = set1P1,
                            onValueChange = { set1P1 = it },
                            label = { Text("Hráč 1") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("set1_p1_input")
                        )
                        Text(":", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                        OutlinedTextField(
                            value = set1P2,
                            onValueChange = { set1P2 = it },
                            label = { Text("Hráč 2") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("set1_p2_input")
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Set 2 Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("2. Set:", fontWeight = FontWeight.Bold, modifier = Modifier.width(55.dp))
                        OutlinedTextField(
                            value = set2P1,
                            onValueChange = { set2P1 = it },
                            label = { Text("Hráč 1") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("set2_p1_input")
                        )
                        Text(":", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                        OutlinedTextField(
                            value = set2P2,
                            onValueChange = { set2P2 = it },
                            label = { Text("Hráč 2") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("set2_p2_input")
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Set 3 Row (Optional)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("3. Set:", fontWeight = FontWeight.Bold, modifier = Modifier.width(55.dp))
                        OutlinedTextField(
                            value = set3P1,
                            onValueChange = { set3P1 = it },
                            label = { Text("Volitelné") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("set3_p1_input")
                        )
                        Text(":", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                        OutlinedTextField(
                            value = set3P2,
                            onValueChange = { set3P2 = it },
                            label = { Text("Volitelné") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("set3_p2_input")
                        )
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

                            val s1P1 = set1P1.toIntOrNull() ?: 21
                            val s1P2 = set1P2.toIntOrNull() ?: 18

                            val s2P1 = set2P1.toIntOrNull() ?: 21
                            val s2P2 = set2P2.toIntOrNull() ?: 15

                            val s3P1 = set3P1.toIntOrNull()
                            val s3P2 = set3P2.toIntOrNull()

                            var setsP1 = 0
                            var setsP2 = 0

                            if (s1P1 > s1P2) setsP1++ else setsP2++
                            if (s2P1 > s2P2) setsP1++ else setsP2++
                            if (s3P1 != null && s3P2 != null) {
                                if (s3P1 > s3P2) setsP1++ else setsP2++
                            }

                            val winner = if (setsP1 > setsP2) 1 else 2

                            val match = MatchEntity(
                                matchType = matchType,
                                category = category,
                                player1Id = p1.id,
                                player2Id = p2.id,
                                setsWinner = winner,
                                scoreSetsPlayer1 = setsP1,
                                scoreSetsPlayer2 = setsP2,
                                set1Player1 = s1P1,
                                set1Player2 = s1P2,
                                set2Player1 = s2P1,
                                set2Player2 = s2P2,
                                set3Player1 = s3P1,
                                set3Player2 = s3P2,
                                courtType = courtType,
                                notes = notes,
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
