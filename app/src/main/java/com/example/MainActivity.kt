package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsTennis
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.AddMatchScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.MatchesScreen
import com.example.ui.screens.PlayersScreen
import com.example.ui.screens.StatsScreen
import com.example.ui.theme.BadmintonTheme
import com.example.ui.theme.ForestGreenContainer
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.NaturalCardBorder
import com.example.ui.theme.NaturalSurfaceVariant
import com.example.ui.theme.OliveAccent
import com.example.ui.viewmodel.BadmintonViewModel

enum class AppNavDestination(
    val title: String,
    val icon: ImageVector,
    val testTag: String
) {
    DASHBOARD("Přehled", Icons.Default.Home, "nav_dashboard"),
    MATCHES("Zápasy", Icons.Default.SportsTennis, "nav_matches"),
    ADD_MATCH("Zapsat", Icons.Default.Add, "nav_add_match"),
    STATS("Statistiky", Icons.Default.Analytics, "nav_stats"),
    PLAYERS("Hráči", Icons.Default.Groups, "nav_players")
}

class MainActivity : ComponentActivity() {

    private val viewModel: BadmintonViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            BadmintonTheme {
                MainAppContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: BadmintonViewModel) {
    var currentDestination by remember { mutableStateOf(AppNavDestination.DASHBOARD) }

    val players by viewModel.players.collectAsState()
    val matches by viewModel.matches.collectAsState()
    val liveMatchState by viewModel.liveMatch.collectAsState()
    val categoryFilter by viewModel.selectedCategoryFilter.collectAsState()

    // Calculated stats list for all players
    val playerStatsList = remember(players, matches) {
        players.map { player ->
            viewModel.calculatePlayerStats(player, matches)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            NaturalTopAppBar()
        },
        bottomBar = {
            NaturalBottomNavigationBar(
                currentDestination = currentDestination,
                onNavigate = { destination ->
                    currentDestination = destination
                }
            )
        },
        floatingActionButton = {
            if (currentDestination != AppNavDestination.ADD_MATCH) {
                FloatingActionButton(
                    onClick = { currentDestination = AppNavDestination.ADD_MATCH },
                    containerColor = OliveAccent,
                    contentColor = Color(0xFF1A1C19),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.testTag("fab_add_match")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Přidat zápas",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (currentDestination) {
                AppNavDestination.DASHBOARD -> DashboardScreen(
                    players = players,
                    matches = matches,
                    playerStatsList = playerStatsList,
                    onNavigateToAddMatch = { currentDestination = AppNavDestination.ADD_MATCH },
                    onNavigateToPlayers = { currentDestination = AppNavDestination.PLAYERS },
                    onNavigateToStats = { currentDestination = AppNavDestination.STATS },
                    onNavigateToMatches = { currentDestination = AppNavDestination.MATCHES },
                    onDeleteMatch = { viewModel.deleteMatch(it) }
                )

                AppNavDestination.MATCHES -> MatchesScreen(
                    matches = matches,
                    players = players,
                    selectedCategory = categoryFilter,
                    onCategorySelected = { viewModel.setCategoryFilter(it) },
                    onDeleteMatch = { viewModel.deleteMatch(it) }
                )

                AppNavDestination.ADD_MATCH -> AddMatchScreen(
                    players = players,
                    liveMatchState = liveMatchState,
                    onStartLiveMatch = { p1, p2, p3, p4, type, cat, court ->
                        viewModel.startLiveMatch(p1, p2, p3, p4, type, cat, court)
                    },
                    onAddPoint = { team -> viewModel.addPointToTeam(team) },
                    onUndoPoint = { viewModel.undoLastPoint() },
                    onSaveLiveMatch = {
                        viewModel.saveLiveMatchToHistory()
                        currentDestination = AppNavDestination.DASHBOARD
                    },
                    onCancelLiveMatch = { viewModel.cancelLiveMatch() },
                    onSaveManualMatch = { match ->
                        viewModel.addMatch(match)
                        currentDestination = AppNavDestination.DASHBOARD
                    }
                )

                AppNavDestination.STATS -> StatsScreen(
                    players = players,
                    matches = matches,
                    viewModel = viewModel
                )

                AppNavDestination.PLAYERS -> PlayersScreen(
                    players = players,
                    onAddPlayer = { name, hand, style, skill, color, notes ->
                        viewModel.addPlayer(name, hand, style, skill, color, notes)
                    },
                    onDeletePlayer = { viewModel.deletePlayer(it) }
                )
            }
        }
    }
}

@Composable
fun NaturalTopAppBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(ForestGreenContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SportsTennis,
                    contentDescription = null,
                    tint = ForestGreenPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Text(
                text = "Badminton Pro",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                letterSpacing = (-0.5).sp
            )
        }

        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(NaturalSurfaceVariant)
                .border(1.dp, NaturalCardBorder, CircleShape)
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = ForestGreenPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "PRO",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = ForestGreenPrimary
                )
            }
        }
    }
}

@Composable
fun NaturalBottomNavigationBar(
    currentDestination: AppNavDestination,
    onNavigate: (AppNavDestination) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .background(NaturalSurfaceVariant)
            .border(width = 1.dp, color = NaturalCardBorder)
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppNavDestination.values().forEach { dest ->
            val isSelected = currentDestination == dest

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onNavigate(dest) }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .testTag(dest.testTag)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) Color(0xFFD4E0B8) else Color.Transparent)
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = dest.icon,
                        contentDescription = dest.title,
                        tint = if (isSelected) ForestGreenPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = dest.title,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) ForestGreenPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}
