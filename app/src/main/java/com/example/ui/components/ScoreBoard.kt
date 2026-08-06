package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.SportsTennis
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.ForestGreenDark
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.NaturalCardBorder
import com.example.ui.theme.OliveAccent
import com.example.ui.viewmodel.LiveMatchState

@Composable
fun ScoreBoard(
    liveMatch: LiveMatchState,
    onAddPoint: (Int) -> Unit,
    onUndo: () -> Unit,
    onSaveMatch: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val p1 = liveMatch.player1
    val p2 = liveMatch.player2
    val p3 = liveMatch.player3
    val p4 = liveMatch.player4

    val name1 = if (p3 != null) "${p1?.name} & ${p3.name}" else (p1?.name ?: "Tým A")
    val name2 = if (p4 != null) "${p2?.name} & ${p4.name}" else (p2?.name ?: "Tým B")

    val curP1Score = when (liveMatch.currentSetIndex) {
        1 -> liveMatch.set1ScoreP1
        2 -> liveMatch.set2ScoreP1
        else -> liveMatch.set3ScoreP1
    }
    val curP2Score = when (liveMatch.currentSetIndex) {
        1 -> liveMatch.set1ScoreP2
        2 -> liveMatch.set2ScoreP2
        else -> liveMatch.set3ScoreP2
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, NaturalCardBorder, RoundedCornerShape(24.dp))
            .testTag("live_scoreboard_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "SET ${liveMatch.currentSetIndex} (z 3)",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                Text(
                    text = "Sety: ${liveMatch.setsWonP1} - ${liveMatch.setsWonP2}",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = ForestGreenPrimary
                )

                if (liveMatch.pointHistory.isNotEmpty() && !liveMatch.isMatchFinished) {
                    IconButton(
                        onClick = onUndo,
                        modifier = Modifier.testTag("undo_point_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Undo,
                            contentDescription = "Zpět bod",
                            tint = ForestGreenPrimary
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(48.dp))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Main Digital Scoreboards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Team 1 Digital Board
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    PlayerAvatar(
                        name = p1?.name ?: "T1",
                        colorHex = p1?.colorHex ?: "#386641",
                        size = 48.dp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = name1,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .size(100.dp, 110.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(ForestGreenDark)
                            .border(
                                width = if (liveMatch.currentServer == 1) 3.dp else 1.dp,
                                color = if (liveMatch.currentServer == 1) OliveAccent else Color.Transparent,
                                shape = RoundedCornerShape(20.dp)
                            )
                            .clickable(enabled = !liveMatch.isMatchFinished) { onAddPoint(1) }
                            .testTag("add_point_team1_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$curP1Score",
                                fontWeight = FontWeight.ExtraBold,
                                style = MaterialTheme.typography.displayMedium,
                                color = OliveAccent
                            )
                            if (liveMatch.currentServer == 1 && !liveMatch.isMatchFinished) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.SportsTennis,
                                        contentDescription = "Podání",
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("Podání", style = MaterialTheme.typography.labelSmall, color = Color.White)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (!liveMatch.isMatchFinished) {
                        Button(
                            onClick = { onAddPoint(1) },
                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                            shape = CircleShape,
                            modifier = Modifier.testTag("team1_plus_point_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "+1 Bod")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("+1 Bod", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Text(
                    text = ":",
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                // Team 2 Digital Board
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    PlayerAvatar(
                        name = p2?.name ?: "T2",
                        colorHex = p2?.colorHex ?: "#BC4749",
                        size = 48.dp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = name2,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .size(100.dp, 110.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(ForestGreenDark)
                            .border(
                                width = if (liveMatch.currentServer == 2) 3.dp else 1.dp,
                                color = if (liveMatch.currentServer == 2) OliveAccent else Color.Transparent,
                                shape = RoundedCornerShape(20.dp)
                            )
                            .clickable(enabled = !liveMatch.isMatchFinished) { onAddPoint(2) }
                            .testTag("add_point_team2_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$curP2Score",
                                fontWeight = FontWeight.ExtraBold,
                                style = MaterialTheme.typography.displayMedium,
                                color = OliveAccent
                            )
                            if (liveMatch.currentServer == 2 && !liveMatch.isMatchFinished) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.SportsTennis,
                                        contentDescription = "Podání",
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("Podání", style = MaterialTheme.typography.labelSmall, color = Color.White)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (!liveMatch.isMatchFinished) {
                        Button(
                            onClick = { onAddPoint(2) },
                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                            shape = CircleShape,
                            modifier = Modifier.testTag("team2_plus_point_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "+1 Bod")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("+1 Bod", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Completed Sets Log Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("1. Set", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    Text("${liveMatch.set1ScoreP1} : ${liveMatch.set1ScoreP2}", fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("2. Set", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    Text("${liveMatch.set2ScoreP1} : ${liveMatch.set2ScoreP2}", fontWeight = FontWeight.Bold)
                }
                if (liveMatch.currentSetIndex == 3 || liveMatch.set3ScoreP1 > 0 || liveMatch.set3ScoreP2 > 0) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("3. Set", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        Text("${liveMatch.set3ScoreP1} : ${liveMatch.set3ScoreP2}", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Winner Banner when match finishes
            AnimatedVisibility(visible = liveMatch.isMatchFinished) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val winnerName = if (liveMatch.winnerTeam == 1) name1 else name2
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(OliveAccent.copy(alpha = 0.25f))
                            .border(2.dp, ForestGreenPrimary, RoundedCornerShape(16.dp))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = ForestGreenPrimary,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Konec utkání!", fontWeight = FontWeight.Bold, color = ForestGreenPrimary)
                                Text("Vítěz: $winnerName", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = onCancel,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Zrušit")
                        }
                        Button(
                            onClick = onSaveMatch,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ForestGreenPrimary,
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("save_finished_live_match_btn")
                        ) {
                            Text("Uložit výsledky", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            if (!liveMatch.isMatchFinished) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Ukončit živé skóre")
                }
            }
        }
    }
}
