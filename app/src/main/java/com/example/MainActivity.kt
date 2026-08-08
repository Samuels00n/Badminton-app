package com.example

import android.accounts.AccountManager
import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SportsTennis
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.example.ui.components.GroupConnectCard
import com.example.ui.components.PlayerAvatar
import com.example.ui.screens.AddMatchScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.theme.CoralRedLoss
import com.example.ui.screens.MatchesScreen
import com.example.ui.screens.PlayersScreen
import com.example.ui.screens.StatsScreen
import com.example.ui.theme.BadmintonTheme
import com.example.ui.theme.ForestGreenContainer
import com.example.ui.theme.ForestGreenDark
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.NaturalCardBorder
import com.example.ui.theme.NaturalSurfaceVariant
import com.example.ui.theme.OliveAccent
import com.example.ui.viewmodel.BadmintonViewModel
import com.example.ui.viewmodel.GoogleAccountState

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
    var showGoogleSyncDialog by remember { mutableStateOf(false) }

    val players by viewModel.players.collectAsState()
    val matches by viewModel.matches.collectAsState()
    val liveMatchState by viewModel.liveMatch.collectAsState()
    val categoryFilter by viewModel.selectedCategoryFilter.collectAsState()
    val googleAccountState by viewModel.googleAccount.collectAsState()

    // Calculated stats list for all players
    val playerStatsList = remember(players, matches) {
        players.map { player ->
            viewModel.calculatePlayerStats(player, matches)
        }
    }

    if (googleAccountState.syncRoomId.isBlank()) {
        WelcomeScreen(
            onConnectGroup = { code ->
                viewModel.signInWithGoogle("uzivatel@skupina", code)
            }
        )
    } else {
        if (showGoogleSyncDialog) {
            GoogleSyncDialog(
                accountState = googleAccountState,
                onDismiss = { showGoogleSyncDialog = false },
                onSignIn = { email, groupCode -> viewModel.signInWithGoogle(email, groupCode) },
                onSignOut = { viewModel.signOutGoogle() },
                onUpdateRoom = { room -> viewModel.setSyncRoomId(room) }
            )
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                NaturalTopAppBar(
                    accountState = googleAccountState,
                    onOpenGoogleSync = { showGoogleSyncDialog = true }
                )
            },
            bottomBar = {
                NaturalBottomNavigationBar(
                    currentDestination = currentDestination,
                    onNavigate = { destination ->
                        currentDestination = destination
                    }
                )
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
                    googleAccountState = googleAccountState,
                    onOpenGoogleSync = { showGoogleSyncDialog = true },
                    onConnectGroup = { code -> viewModel.signInWithGoogle("uzivatel@skupina", code) },
                    onNavigateToAddMatch = { currentDestination = AppNavDestination.ADD_MATCH },
                    onNavigateToPlayers = { currentDestination = AppNavDestination.PLAYERS },
                    onNavigateToStats = { currentDestination = AppNavDestination.STATS },
                    onNavigateToMatches = { currentDestination = AppNavDestination.MATCHES },
                    onUpdateMatch = { viewModel.updateMatch(it) },
                    onDeleteMatch = { viewModel.deleteMatch(it) }
                )

                AppNavDestination.MATCHES -> MatchesScreen(
                    matches = matches,
                    players = players,
                    selectedCategory = categoryFilter,
                    onCategorySelected = { viewModel.setCategoryFilter(it) },
                    onUpdateMatch = { viewModel.updateMatch(it) },
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
                    onAddPlayer = { name, hand, style, skill, color, notes, avatarIcon ->
                        viewModel.addPlayer(name, hand, style, skill, color, notes, avatarIcon)
                    },
                    onUpdatePlayer = { viewModel.updatePlayer(it) },
                    onDeletePlayer = { viewModel.deletePlayer(it) }
                )
            }
        }
    }
}
}

@Composable
fun WelcomeScreen(
    onConnectGroup: (String) -> Unit
) {
    var groupCodeInput by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A180E))
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text("🏸", fontSize = 32.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Badminton Pro",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            GroupConnectCard(
                currentRoomInput = groupCodeInput,
                onRoomInputChange = { groupCodeInput = it },
                onConnectGroup = { code -> onConnectGroup(code) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun NaturalTopAppBar(
    accountState: GoogleAccountState,
    onOpenGoogleSync: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(ForestGreenContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SportsTennis,
                        contentDescription = null,
                        tint = ForestGreenPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column {
                    Text(
                        text = "Badminton Pro",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        letterSpacing = (-0.5).sp
                    )
                }
            }

            // Google Account / Cloud Sync Pill Button
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (accountState.syncRoomId.isNotBlank()) ForestGreenContainer else NaturalSurfaceVariant)
                    .border(1.dp, if (accountState.syncRoomId.isNotBlank()) ForestGreenPrimary else NaturalCardBorder, CircleShape)
                    .clickable { onOpenGoogleSync() }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .testTag("google_sync_top_btn")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (accountState.syncRoomId.isNotBlank()) Icons.Default.CloudDone else Icons.Default.CloudSync,
                        contentDescription = "Google Sync",
                        tint = ForestGreenPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (accountState.syncRoomId.isNotBlank()) "Skupina: ${accountState.syncRoomId}" else "Připojit skupinu",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForestGreenPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun NaturalBottomNavigationBar(
    currentDestination: AppNavDestination,
    onNavigate: (AppNavDestination) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = ForestGreenDark,
            tonalElevation = 8.dp,
            shadowElevation = 6.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp, horizontal = 2.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppNavDestination.values().forEach { dest ->
                    val isSelected = currentDestination == dest

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onNavigate(dest) }
                            .padding(vertical = 4.dp, horizontal = 1.dp)
                            .testTag(dest.testTag)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) OliveAccent else Color.Transparent)
                                .padding(horizontal = 10.dp, vertical = 3.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = dest.icon,
                                contentDescription = dest.title,
                                tint = if (isSelected) Color(0xFF1A1C19) else Color.White.copy(alpha = 0.65f),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = dest.title,
                            fontSize = 10.5.sp,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                            color = if (isSelected) OliveAccent else Color.White.copy(alpha = 0.65f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GoogleSyncDialog(
    accountState: GoogleAccountState,
    onDismiss: () -> Unit,
    onSignIn: (email: String, groupCode: String) -> Unit,
    onSignOut: () -> Unit,
    onUpdateRoom: (String) -> Unit
) {
    var roomInput by remember { mutableStateOf(accountState.syncRoomId) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0F1E14),
        titleContentColor = Color.White,
        textContentColor = Color.White,
        shape = RoundedCornerShape(24.dp),
        title = null,
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (accountState.syncRoomId.isBlank()) {
                    GroupConnectCard(
                        currentRoomInput = roomInput,
                        onRoomInputChange = { roomInput = it },
                        onConnectGroup = { code ->
                            onSignIn("uzivatel@skupina", code)
                            onDismiss()
                        }
                    )
                } else {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF142E1D)),
                        border = BorderStroke(1.dp, ForestGreenPrimary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Groups,
                                    contentDescription = null,
                                    tint = ForestGreenPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Skupina: ${accountState.syncRoomId}",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ForestGreenPrimary
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Všechny změny se v reálném čase přenášejí do mobilní i webové aplikace.",
                                fontSize = 12.5.sp,
                                color = Color.White.copy(alpha = 0.75f),
                                lineHeight = 17.sp
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            Button(
                                onClick = {
                                    onSignOut()
                                    onUpdateRoom("")
                                    onDismiss()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CoralRedLoss,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                            ) {
                                Text(
                                    text = "Odpojit skupinu",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Zavřít", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    )
}
