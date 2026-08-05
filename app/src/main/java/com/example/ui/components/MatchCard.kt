package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.ui.theme.CoralRedLoss
import com.example.ui.theme.ForestGreenContainer
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.NaturalCardBorder
import com.example.ui.theme.NaturalWarmChip
import com.example.ui.theme.OliveAccent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MatchCard(
    match: MatchEntity,
    playersMap: Map<Long, PlayerEntity>,
    onDeleteClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val p1 = playersMap[match.player1Id]
    val p2 = playersMap[match.player2Id]
    val p3 = match.player3Id?.let { playersMap[it] }
    val p4 = match.player4Id?.let { playersMap[it] }

    val nameTeam1 = if (p3 != null) "${p1?.name ?: "Hráč 1"} & ${p3.name}" else (p1?.name ?: "Hráč 1")
    val nameTeam2 = if (p4 != null) "${p2?.name ?: "Hráč 2"} & ${p4.name}" else (p2?.name ?: "Hráč 2")

    val dateFormat = SimpleDateFormat("d. MMMM yyyy", Locale("cs", "CZ"))
    val dateStr = dateFormat.format(Date(match.timestamp))

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, NaturalCardBorder, RoundedCornerShape(20.dp))
            .testTag("match_card_${match.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(ForestGreenContainer)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = match.category,
                            color = ForestGreenPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = if (match.matchType == "DOUBLES") "Čtyřhra" else "Dvouhra",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = dateStr,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )

                    if (onDeleteClick != null) {
                        IconButton(
                            onClick = onDeleteClick,
                            modifier = Modifier
                                .padding(start = 4.dp)
                                .testTag("delete_match_btn_${match.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Smazat zápas",
                                tint = CoralRedLoss.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Scoreboard Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Team 1
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    PlayerAvatar(
                        name = p1?.name ?: "H1",
                        colorHex = p1?.colorHex ?: "#386641",
                        size = 40.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = nameTeam1,
                            fontWeight = if (match.setsWinner == 1) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 15.sp,
                            color = if (match.setsWinner == 1) ForestGreenPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                        if (match.setsWinner == 1) {
                            Text("Vítěz", fontSize = 11.sp, color = ForestGreenPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Score Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "${match.scoreSetsPlayer1} : ${match.scoreSetsPlayer2}",
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        color = ForestGreenPrimary
                    )
                }

                // Team 2
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = nameTeam2,
                            fontWeight = if (match.setsWinner == 2) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 15.sp,
                            color = if (match.setsWinner == 2) ForestGreenPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                        if (match.setsWinner == 2) {
                            Text("Vítěz", fontSize = 11.sp, color = ForestGreenPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    PlayerAvatar(
                        name = p2?.name ?: "H2",
                        colorHex = p2?.colorHex ?: "#BC4749",
                        size = 40.dp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Set Breakdown
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "1. set: ${match.set1Player1}:${match.set1Player2}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (match.set2Player1 != null && match.set2Player2 != null) {
                        Text(
                            text = "2. set: ${match.set2Player1}:${match.set2Player2}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    if (match.set3Player1 != null && match.set3Player2 != null) {
                        Text(
                            text = "3. set: ${match.set3Player1}:${match.set3Player2}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Text(
                    text = "${match.durationMinutes} min • ${match.courtType}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            if (match.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Poznámka: ${match.notes}",
                    fontSize = 12.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}
