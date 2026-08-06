package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.MatchEntity
import com.example.data.entity.PlayerEntity
import com.example.ui.theme.CoralRedLoss
import com.example.ui.theme.ForestGreenContainer
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.NaturalCardBorder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MatchCard(
    match: MatchEntity,
    playersMap: Map<Long, PlayerEntity>,
    onEditClick: (() -> Unit)? = null,
    onDeleteClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val p1 = playersMap[match.player1Id]
    val p2 = playersMap[match.player2Id]
    val p3 = match.player3Id?.let { playersMap[it] }
    val p4 = match.player4Id?.let { playersMap[it] }

    val nameTeam1 = if (p3 != null) "${p1?.name ?: "Hráč 1"} & ${p3.name}" else (p1?.name ?: "Hráč 1")
    val nameTeam2 = if (p4 != null) "${p2?.name ?: "Hráč 2"} & ${p4.name}" else (p2?.name ?: "Hráč 2")

    val isTeam1Winner = (match.setsWinner == 1)
    val isTeam2Winner = (match.setsWinner == 2)

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
            // Header Row: Category chip, match type, date, delete button
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

                    if (onEditClick != null) {
                        IconButton(
                            onClick = onEditClick,
                            modifier = Modifier
                                .size(32.dp)
                                .padding(start = 4.dp)
                                .testTag("edit_match_btn_${match.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Upravit zápas",
                                tint = ForestGreenPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    if (onDeleteClick != null) {
                        IconButton(
                            onClick = onDeleteClick,
                            modifier = Modifier
                                .size(32.dp)
                                .padding(start = 6.dp)
                                .testTag("delete_match_btn_${match.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Smazat zápas",
                                tint = CoralRedLoss.copy(alpha = 0.7f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Teams & Score Section - Clean structured layout
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    .padding(12.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // TEAM 1 ROW
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            // Avatars
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                PlayerAvatar(
                                    name = p1?.name ?: "H1",
                                    colorHex = p1?.colorHex ?: "#386641",
                                    avatarIcon = p1?.avatarIcon ?: "🏸",
                                    size = 36.dp
                                )
                                if (p3 != null) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    PlayerAvatar(
                                        name = p3.name,
                                        colorHex = p3.colorHex,
                                        avatarIcon = p3.avatarIcon,
                                        size = 36.dp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = nameTeam1,
                                    fontWeight = if (isTeam1Winner) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 15.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (isTeam1Winner) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.EmojiEvents,
                                        contentDescription = "Vítěz",
                                        tint = Color(0xFFD4AF37),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        // Team 1 Sets Score
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isTeam1Winner) ForestGreenContainer else Color.Transparent)
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${match.scoreSetsPlayer1}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                color = if (isTeam1Winner) ForestGreenPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }

                    // Divider
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(NaturalCardBorder.copy(alpha = 0.6f))
                    )

                    // TEAM 2 ROW
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            // Avatars
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                PlayerAvatar(
                                    name = p2?.name ?: "H2",
                                    colorHex = p2?.colorHex ?: "#BC4749",
                                    avatarIcon = p2?.avatarIcon ?: "🏸",
                                    size = 36.dp
                                )
                                if (p4 != null) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    PlayerAvatar(
                                        name = p4.name,
                                        colorHex = p4.colorHex,
                                        avatarIcon = p4.avatarIcon,
                                        size = 36.dp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = nameTeam2,
                                    fontWeight = if (isTeam2Winner) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 15.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (isTeam2Winner) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.EmojiEvents,
                                        contentDescription = "Vítěz",
                                        tint = Color(0xFFD4AF37),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        // Team 2 Sets Score
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isTeam2Winner) ForestGreenContainer else Color.Transparent)
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${match.scoreSetsPlayer2}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                color = if (isTeam2Winner) ForestGreenPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Set Points Breakdown
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                val allSets = match.getAllSetScores()
                FlowRow(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    allSets.forEachIndexed { index, (p1Score, p2Score) ->
                        Text(
                            text = "${index + 1}. set: $p1Score:$p2Score",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = match.courtType,
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
