package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.ui.components.EditMatchDialog
import com.example.ui.components.MatchCard
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.NaturalCardBorder
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchesScreen(
    matches: List<MatchEntity>,
    players: List<PlayerEntity>,
    selectedCategory: String?,
    onCategorySelected: (String?) -> Unit,
    onUpdateMatch: (MatchEntity) -> Unit,
    onDeleteMatch: (MatchEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val playersMap = players.associateBy { it.id }
    val categories = listOf("Vše", "Přátelský", "Turnaj", "Liga")
    val czechMonths = listOf("Leden", "Únor", "Březen", "Duben", "Květen", "Červen", "Červenec", "Srpen", "Září", "Říjen", "Listopad", "Prosinec")

    var selectedMonthKey by remember { mutableStateOf("all") }
    var selectedPlayerId by remember { mutableStateOf<Long?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    var matchToEdit by remember { mutableStateOf<MatchEntity?>(null) }

    // Derive month options from matches
    val monthOptions = remember(matches) {
        val monthMap = mutableMapOf<String, String>()
        val cal = Calendar.getInstance()
        matches.forEach { m ->
            if (m.timestamp > 0) {
                cal.timeInMillis = m.timestamp
                val y = cal.get(Calendar.YEAR)
                val mIdx = cal.get(Calendar.MONTH)
                val key = String.format("%d-%02d", y, mIdx + 1)
                val label = "${czechMonths[mIdx]} $y"
                monthMap[key] = label
            }
        }
        val now = Calendar.getInstance()
        val nowKey = String.format("%d-%02d", now.get(Calendar.YEAR), now.get(Calendar.MONTH) + 1)
        if (!monthMap.containsKey(nowKey)) {
            monthMap[nowKey] = "${czechMonths[now.get(Calendar.MONTH)]} ${now.get(Calendar.YEAR)}"
        }
        monthMap.toList().sortedByDescending { it.first }
    }

    val filteredMatches = matches.filter { m ->
        // Category
        val matchesCategory = if (selectedCategory.isNullOrBlank() || selectedCategory == "Vše") true
        else m.category.equals(selectedCategory, ignoreCase = true)

        // Month
        val matchesMonth = if (selectedMonthKey == "all") true else {
            val cal = Calendar.getInstance().apply { timeInMillis = m.timestamp }
            val key = String.format("%d-%02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)
            key == selectedMonthKey
        }

        // Player
        val matchesPlayer = if (selectedPlayerId == null) true else {
            m.player1Id == selectedPlayerId ||
            m.player2Id == selectedPlayerId ||
            m.player3Id == selectedPlayerId ||
            m.player4Id == selectedPlayerId
        }

        // Search
        val matchesSearch = if (searchQuery.isBlank()) true else {
            val query = searchQuery.trim().lowercase()
            val p1Name = playersMap[m.player1Id]?.name?.lowercase() ?: ""
            val p2Name = playersMap[m.player2Id]?.name?.lowercase() ?: ""
            val p3Name = playersMap[m.player3Id]?.name?.lowercase() ?: ""
            val p4Name = playersMap[m.player4Id]?.name?.lowercase() ?: ""
            val cat = m.category.lowercase()
            val court = m.courtType.lowercase()
            val notes = (m.notes ?: "").lowercase()

            p1Name.contains(query) || p2Name.contains(query) || p3Name.contains(query) ||
            p4Name.contains(query) || cat.contains(query) || court.contains(query) || notes.contains(query)
        }

        matchesCategory && matchesMonth && matchesPlayer && matchesSearch
    }.sortedByDescending { it.timestamp }

    fun resetFilters() {
        onCategorySelected(null)
        selectedMonthKey = "all"
        selectedPlayerId = null
        searchQuery = ""
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Historie zápasů",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            if (!selectedCategory.isNullOrBlank() || selectedMonthKey != "all" || selectedPlayerId != null || searchQuery.isNotBlank()) {
                TextButton(
                    onClick = { resetFilters() },
                    modifier = Modifier.testTag("reset_filters_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Obnovit filtr",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Obnovit filtr",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Category Filter Chips Row
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(categories) { cat ->
                val isSelected = (selectedCategory == cat) || (cat == "Vše" && (selectedCategory.isNullOrBlank() || selectedCategory == "Vše"))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) ForestGreenPrimary else MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, if (isSelected) ForestGreenPrimary else NaturalCardBorder, RoundedCornerShape(12.dp))
                        .clickable { onCategorySelected(if (cat == "Vše") null else cat) }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .testTag("filter_chip_$cat")
                ) {
                    Text(
                        text = cat,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Filter Controls Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                // Row 1: Month and Player selects
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Month Dropdown
                    Box(modifier = Modifier.weight(1f)) {
                        var expandedMonth by remember { mutableStateOf(false) }
                        val selectedMonthLabel = if (selectedMonthKey == "all") "Měsíce" else (monthOptions.find { it.first == selectedMonthKey }?.second ?: "Měsíce")

                        ExposedDropdownMenuBox(
                            expanded = expandedMonth,
                            onExpandedChange = { expandedMonth = !expandedMonth }
                        ) {
                            OutlinedTextField(
                                value = selectedMonthLabel,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Měsíc", fontSize = 11.sp) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMonth) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = expandedMonth,
                                onDismissRequest = { expandedMonth = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Měsíce (vše)") },
                                    onClick = {
                                        selectedMonthKey = "all"
                                        expandedMonth = false
                                    }
                                )
                                monthOptions.forEach { opt ->
                                    DropdownMenuItem(
                                        text = { Text(opt.second) },
                                        onClick = {
                                            selectedMonthKey = opt.first
                                            expandedMonth = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Player Dropdown
                    Box(modifier = Modifier.weight(1f)) {
                        var expandedPlayer by remember { mutableStateOf(false) }
                        val selectedPlayerLabel = if (selectedPlayerId == null) "Hráči" else (playersMap[selectedPlayerId]?.name ?: "Hráči")

                        ExposedDropdownMenuBox(
                            expanded = expandedPlayer,
                            onExpandedChange = { expandedPlayer = !expandedPlayer }
                        ) {
                            OutlinedTextField(
                                value = selectedPlayerLabel,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Hráč", fontSize = 11.sp) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPlayer) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = expandedPlayer,
                                onDismissRequest = { expandedPlayer = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Hráči (všichni)") },
                                    onClick = {
                                        selectedPlayerId = null
                                        expandedPlayer = false
                                    }
                                )
                                players.forEach { p ->
                                    val icon = if (p.avatarIcon.isBlank() || p.avatarIcon == "Iniciály") p.name.take(2).uppercase() else p.avatarIcon
                                    DropdownMenuItem(
                                        text = { Text("$icon ${p.name}") },
                                        onClick = {
                                            selectedPlayerId = p.id
                                            expandedPlayer = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Search text field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Hledat", fontSize = 11.sp) },
                    placeholder = { Text("Jméno, poznámka...", fontSize = 11.sp) },
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Smazat text",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredMatches.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterAlt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Žádné zápasy neodpovídají zvoleným filtrům.",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = { resetFilters() }) {
                        Text("Vymazat filtry", color = ForestGreenPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filteredMatches, key = { it.id }) { match ->
                    MatchCard(
                        match = match,
                        playersMap = playersMap,
                        onEditClick = { matchToEdit = match },
                        onDeleteClick = { onDeleteMatch(match) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        matchToEdit?.let { match ->
            EditMatchDialog(
                match = match,
                players = players,
                onSave = { updatedMatch ->
                    onUpdateMatch(updatedMatch)
                    matchToEdit = null
                },
                onDismiss = { matchToEdit = null }
            )
        }
    }
}
