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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.data.entity.MatchEntity
import com.example.data.entity.PlayerEntity
import com.example.ui.components.EditMatchDialog
import com.example.ui.components.MatchCard
import com.example.ui.theme.ForestGreenContainer
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.NaturalCardBorder

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

    var matchToEdit by remember { mutableStateOf<MatchEntity?>(null) }

    val filteredMatches = matches.filter { m ->
        if (selectedCategory.isNullOrBlank() || selectedCategory == "Vše") true
        else m.category.equals(selectedCategory, ignoreCase = true)
    }.sortedByDescending { it.timestamp }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Historie zápasů",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(12.dp))

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

        Spacer(modifier = Modifier.height(16.dp))

        if (filteredMatches.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "V této kategorii nejsou žádné zápasy.",
                        fontWeight = FontWeight.Bold
                    )
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
