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
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.ui.platform.LocalContext
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
import androidx.compose.ui.text.style.TextOverflow
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

    if (showGoogleSyncDialog) {
        GoogleSyncDialog(
            accountState = googleAccountState,
            onDismiss = { showGoogleSyncDialog = false },
            onSignIn = { email, groupCode -> viewModel.signInWithGoogle(email, groupCode) },
            onSignOut = { viewModel.signOutGoogle() },
            onUpdateRoom = { room -> viewModel.setSyncRoomId(room) },
            onSyncNow = { viewModel.triggerCloudSync() }
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
                    Text(
                        text = if (accountState.isSignedIn) "Google Cloud Sync Aktivní" else "Místní databáze",
                        fontSize = 11.sp,
                        color = if (accountState.isSignedIn) ForestGreenPrimary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Google Account / Cloud Sync Pill Button
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (accountState.isSignedIn) ForestGreenContainer else NaturalSurfaceVariant)
                    .border(1.dp, if (accountState.isSignedIn) ForestGreenPrimary else NaturalCardBorder, CircleShape)
                    .clickable { onOpenGoogleSync() }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .testTag("google_sync_top_btn")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (accountState.isSignedIn) Icons.Default.CloudDone else Icons.Default.CloudSync,
                        contentDescription = "Google Sync",
                        tint = ForestGreenPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (accountState.isSignedIn) "Google Účet" else "Přihlásit Google",
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
    onUpdateRoom: (String) -> Unit,
    onSyncNow: () -> Unit
) {
    val context = LocalContext.current
    var roomInput by remember { mutableStateOf(accountState.syncRoomId) }
    var emailInput by remember { mutableStateOf(accountState.email ?: "") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val accountPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val accountName = result.data?.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
            if (!accountName.isNullOrBlank()) {
                emailInput = accountName
                errorMessage = null
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CloudSync,
                    contentDescription = null,
                    tint = ForestGreenPrimary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Google Účet & Cloud Sync",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (!accountState.isSignedIn) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Autorizace Google Účtu (OAuth 2.0)",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Pro synchronizaci si vyberte Google účet registrovaný v systému Android a zadejte kód sdílené skupiny. Heslo se z bezpečnostních důvodů zadává pouze v oficiálním dialogu Google/Androidu.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                                lineHeight = 16.sp
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            try {
                                val intent = AccountManager.newChooseAccountIntent(
                                    null, null, arrayOf("com.google"), false, null, null, null, null
                                )
                                accountPickerLauncher.launch(intent)
                            } catch (e: Exception) {
                                errorMessage = "Výběr účtu z systému Android selhal."
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.AccountBox, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Vybrat Google účet ze systému Android", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }

                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = {
                            emailInput = it
                            errorMessage = null
                        },
                        label = { Text("Vybraný Google E-mail") },
                        placeholder = { Text("uzivatel@gmail.com") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = roomInput,
                        onValueChange = {
                            roomInput = it
                            errorMessage = null
                        },
                        label = { Text("Kód sdílené skupiny / klubu") },
                        placeholder = { Text("např. BADMINTON-2026") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    if (errorMessage != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CoralRedLoss.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = CoralRedLoss,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = errorMessage ?: "",
                                color = CoralRedLoss,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Button(
                        onClick = {
                            when {
                                !emailInput.contains("@") || !emailInput.contains(".") -> {
                                    errorMessage = "Zadejte platný Google e-mail."
                                }
                                roomInput.trim().isBlank() -> {
                                    errorMessage = "Zadejte kód sdílené skupiny."
                                }
                                else -> {
                                    onSignIn(emailInput.trim(), roomInput.trim())
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.AccountCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Přihlásit se přes Google Účet", fontWeight = FontWeight.Bold)
                    }
                } else {
                    // Profile Info Card
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            PlayerAvatar(
                                name = accountState.displayName ?: "Google",
                                colorHex = "#386641",
                                size = 44.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = accountState.displayName ?: "Google Uživatel",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = accountState.email ?: "",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                            TextButton(onClick = onSignOut) {
                                Text("Odhlásit", color = CoralRedLoss, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Sync Status Banner
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (accountState.isSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.CloudDone,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = accountState.syncStatusMessage,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    // Shared Sync Code Input
                    Column {
                        Text(
                            text = "Kód sdílené skupiny / klubu:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = roomInput,
                                onValueChange = { roomInput = it },
                                placeholder = { Text("např. BADMINTON-2026") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )

                            Button(
                                onClick = { onUpdateRoom(roomInput) },
                                colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Uložit")
                            }
                        }
                    }

                    Text(
                        text = "⚡ Veškeré změny (přidání hráče, zapsání zápasu) se ukládají a synchronizují automaticky v reálném čase. Zadejte stejný kód na více zařízeních pro sdílení dat v klubu.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                        lineHeight = 16.sp
                    )

                    OutlinedButton(
                        onClick = onSyncNow,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Ruční re-synchronizace",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Zavřít", fontWeight = FontWeight.Bold)
            }
        }
    )
}
