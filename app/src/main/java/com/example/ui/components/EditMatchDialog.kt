package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.window.Dialog
import com.example.data.entity.MatchEntity
import com.example.data.entity.PlayerEntity
import com.example.ui.theme.CoralRedLoss
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.NaturalCardBorder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMatchDialog(
    match: MatchEntity,
    players: List<PlayerEntity>,
    onSave: (MatchEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var category by remember { mutableStateOf(match.category) }
    var courtType by remember { mutableStateOf(match.courtType) }

    val initialP1 = players.find { it.id == match.player1Id } ?: players.firstOrNull()
    val initialP2 = players.find { it.id == match.player2Id } ?: players.getOrNull(1)

    var selectedP1 by remember { mutableStateOf<PlayerEntity?>(initialP1) }
    var selectedP2 by remember { mutableStateOf<PlayerEntity?>(initialP2) }

    // Reconstruct set scores from match
    val initialSets = match.getAllSetScores().map { (p1Score, p2Score) ->
        Pair(p1Score.toString(), p2Score.toString())
    }.ifEmpty {
        listOf(Pair("21", "18"), Pair("21", "15"))
    }

    var setsList by remember { mutableStateOf(initialSets) }
    var isRetired by remember { mutableStateOf(match.isRetired) }
    var retiringPlayer by remember { mutableIntStateOf(match.retiringPlayer ?: 1) }
    var notes by remember { mutableStateOf(match.notes) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = ForestGreenPrimary
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Upravit odehraný zápas",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Hráč / Tým 1:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(4.dp))
                EditPlayerDropdown(
                    players = players,
                    selectedPlayer = selectedP1,
                    onSelect = { selectedP1 = it },
                    label = "Hráč 1"
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("Hráč / Tým 2 (Soupeř):", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(4.dp))
                EditPlayerDropdown(
                    players = players.filter { it.id != selectedP1?.id },
                    selectedPlayer = selectedP2,
                    onSelect = { selectedP2 = it },
                    label = "Hráč 2"
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Kategorie", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        EditSimpleSelect(
                            options = listOf("Přátelský", "Turnaj", "Liga"),
                            selected = category,
                            onSelect = { category = it }
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Povrch", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        EditSimpleSelect(
                            options = listOf("Hala", "Venku", "Jiné"),
                            selected = courtType,
                            onSelect = { courtType = it }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Skóre po setech (body):", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))

                setsList.forEachIndexed { index, pair ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "${index + 1}. Set:",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(55.dp)
                        )
                        OutlinedTextField(
                            value = pair.first,
                            onValueChange = { newValue ->
                                val updated = setsList.toMutableList()
                                updated[index] = Pair(newValue, pair.second)
                                setsList = updated
                            },
                            label = { Text("Hráč 1") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
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
                            modifier = Modifier.weight(1f)
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
                    onClick = { setsList = setsList + Pair("", "") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Přidat další set", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Retirement Section
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isRetired) Color(0xFFFFF7ED) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isRetired) Color(0xFFF59E0B) else NaturalCardBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isRetired = !isRetired }
                        ) {
                            Checkbox(
                                checked = isRetired,
                                onCheckedChange = { isRetired = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = Color(0xFFD97706)
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = "Zápas byl skrečován (předčasně ukončen)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (isRetired) Color(0xFF9A3412) else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Označte, pokud hráč ze zdravotních či jiných důvodů vzdal",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }

                        if (isRetired) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(Color(0xFFF59E0B).copy(alpha = 0.3f))
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Kdo zápas skrečoval (vzdal)?",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = Color(0xFF9A3412),
                                modifier = Modifier.padding(start = 4.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                val p1Name = selectedP1?.name ?: "Hráč 1"
                                val p2Name = selectedP2?.name ?: "Hráč 2"

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (retiringPlayer == 1) Color(0xFFFFEDD5) else Color.Transparent)
                                        .border(1.dp, if (retiringPlayer == 1) Color(0xFFF59E0B) else Color.Transparent, RoundedCornerShape(10.dp))
                                        .clickable { retiringPlayer = 1 }
                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    RadioButton(
                                        selected = (retiringPlayer == 1),
                                        onClick = { retiringPlayer = 1 },
                                        colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFD97706))
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = p1Name,
                                        fontWeight = if (retiringPlayer == 1) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 12.sp
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (retiringPlayer == 2) Color(0xFFFFEDD5) else Color.Transparent)
                                        .border(1.dp, if (retiringPlayer == 2) Color(0xFFF59E0B) else Color.Transparent, RoundedCornerShape(10.dp))
                                        .clickable { retiringPlayer = 2 }
                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    RadioButton(
                                        selected = (retiringPlayer == 2),
                                        onClick = { retiringPlayer = 2 },
                                        colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFD97706))
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = p2Name,
                                        fontWeight = if (retiringPlayer == 2) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Poznámky k zápasu") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Zrušit")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
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

                            val winner = if (isRetired) {
                                if (retiringPlayer == 1) 2 else 1
                            } else {
                                if (setsP1 >= setsP2) 1 else 2
                            }
                            val setsSequence = finalSets.joinToString(",") { "${it.first}:${it.second}" }

                            val updatedMatch = match.copy(
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
                                isRetired = isRetired,
                                retiringPlayer = if (isRetired) retiringPlayer else null
                            )

                            onSave(updatedMatch)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Uložit změny", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditPlayerDropdown(
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
            leadingIcon = if (selectedPlayer != null) {
                {
                    PlayerAvatar(
                        name = selectedPlayer.name,
                        colorHex = selectedPlayer.colorHex,
                        avatarIcon = selectedPlayer.avatarIcon,
                        size = 28.dp
                    )
                }
            } else null,
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
                                size = 28.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
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
private fun EditSimpleSelect(
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
