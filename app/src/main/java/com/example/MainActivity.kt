package com.example

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DoNotDisturbOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.ActivityLogView
import com.example.ui.components.LocationSearchBar
import com.example.ui.components.PermissionCard
import com.example.ui.components.RadiusSliderCard
import com.example.ui.components.StatusDashboardCard
import com.example.ui.map.OsmMapView
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.EmeraldNeon
import com.example.ui.theme.IndigoNeon
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NavySurface
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletDnd
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val context = LocalContext.current
                val lifecycleOwner = LocalLifecycleOwner.current
                val snackbarHostState = remember { SnackbarHostState() }

                // Collect ViewModel state with lifecycle safety
                val zone by viewModel.zoneState.collectAsStateWithLifecycle()
                val isAutomationEnabled by viewModel.isAutomationEnabled.collectAsStateWithLifecycle()
                val wasDndPriorActive by viewModel.wasDndPriorActive.collectAsStateWithLifecycle()
                val currentTransitionState by viewModel.currentTransitionState.collectAsStateWithLifecycle()
                val activityLogs by viewModel.activityLogs.collectAsStateWithLifecycle()
                val permissions by viewModel.permissionsState.collectAsStateWithLifecycle()
                val isDndActive by viewModel.isDndActive.collectAsStateWithLifecycle()
                val dndStatusDescription by viewModel.dndStatusDescription.collectAsStateWithLifecycle()
                val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
                val isSearching by viewModel.isSearching.collectAsStateWithLifecycle()
                val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
                val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

                // Show error snackbars
                LaunchedEffect(errorMessage) {
                    errorMessage?.let {
                        snackbarHostState.showSnackbar(it)
                        viewModel.clearError()
                    }
                }

                // Check permissions & DND state when returning from system Settings
                DisposableEffect(lifecycleOwner) {
                    val observer = LifecycleEventObserver { _, event ->
                        if (event == Lifecycle.Event.ON_RESUME) {
                            viewModel.checkPermissions()
                            viewModel.refreshDndState()
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose {
                        lifecycleOwner.lifecycle.removeObserver(observer)
                    }
                }

                // Permission Launchers
                val locationPermissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestMultiplePermissions()
                ) { result ->
                    viewModel.checkPermissions()
                    val granted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true
                    if (granted) {
                        viewModel.fetchCurrentGpsLocation()
                    }
                }

                val backgroundLocationLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) {
                    viewModel.checkPermissions()
                }

                val notificationPermissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) {
                    viewModel.checkPermissions()
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = ObsidianBg,
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    topBar = {
                        TopAppBar(
                            title = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = EmeraldNeon.copy(alpha = 0.15f),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldNeon.copy(alpha = 0.4f)),
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Security,
                                                contentDescription = "ZoneQuiet Logo",
                                                tint = EmeraldNeon,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "ZoneQuiet",
                                            style = MaterialTheme.typography.titleLarge.copy(
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 0.5.sp,
                                                color = TextPrimary
                                            )
                                        )
                                        Text(
                                            text = "Geofence Do Not Disturb",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 11.sp,
                                                color = TextMuted
                                            )
                                        )
                                    }
                                }
                            },
                            actions = {
                                // Live DND badge
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = if (isDndActive) VioletDnd.copy(alpha = 0.2f) else NavySurface,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isDndActive) VioletDnd else BorderSubtle
                                    ),
                                    modifier = Modifier.padding(end = 4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(if (isDndActive) VioletDnd else EmeraldNeon)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (isDndActive) "DND Active" else "DND Normal",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isDndActive) VioletDnd else EmeraldNeon
                                            )
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = {
                                        viewModel.checkPermissions()
                                        viewModel.refreshDndState()
                                    },
                                    modifier = Modifier.testTag("refresh_status_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Refresh Status",
                                        tint = TextSecondary
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = ObsidianBg
                            ),
                            modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
                        )
                    }
                ) { innerPadding ->
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // 1. Permission Guidance Card
                        item {
                            PermissionCard(
                                permissions = permissions,
                                onRequestLocation = {
                                    locationPermissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                },
                                onRequestBackgroundLocation = {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                        backgroundLocationLauncher.launch(
                                            Manifest.permission.ACCESS_BACKGROUND_LOCATION
                                        )
                                    }
                                },
                                onRequestDndPolicy = {
                                    val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                                    context.startActivity(intent)
                                },
                                onRequestNotifications = {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        notificationPermissionLauncher.launch(
                                            Manifest.permission.POST_NOTIFICATIONS
                                        )
                                    }
                                }
                            )
                        }

                        // 2. Status & Master Automation Card
                        item {
                            StatusDashboardCard(
                                isAutomationEnabled = isAutomationEnabled,
                                isDndActive = isDndActive,
                                dndStatusDescription = dndStatusDescription,
                                currentTransitionState = currentTransitionState,
                                zoneLabel = zone.label,
                                wasDndPriorActive = wasDndPriorActive,
                                onToggleAutomation = { enabled ->
                                    viewModel.toggleAutomation(enabled)
                                }
                            )
                        }

                        // 3. Location Search Bar
                        item {
                            LocationSearchBar(
                                query = searchQuery,
                                onQueryChange = { viewModel.onSearchQueryChange(it) },
                                onSearch = { viewModel.performSearch() },
                                isSearching = isSearching,
                                searchResults = searchResults,
                                onSelectResult = { viewModel.selectSearchResult(it) },
                                onClear = { viewModel.clearSearch() }
                            )
                        }

                        // 4. Interactive OpenStreetMap Canvas
                        item {
                            Column {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 4.dp, vertical = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Interactive Zone Perimeter",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                    )
                                    Text(
                                        text = "Tap or drag map to set pin",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = TextMuted,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                OsmMapView(
                                    targetLatitude = zone.latitude,
                                    targetLongitude = zone.longitude,
                                    radiusMeters = zone.radiusMeters,
                                    isInsideZone = currentTransitionState == com.example.data.model.TransitionState.INSIDE,
                                    onLocationSelected = { lat, lon ->
                                        viewModel.updateLocation(lat, lon)
                                    },
                                    onUseCurrentLocation = {
                                        viewModel.fetchCurrentGpsLocation()
                                    },
                                    modifier = Modifier.height(300.dp)
                                )
                            }
                        }

                        // 5. Zone Label & Radius Configuration Card
                        item {
                            RadiusSliderCard(
                                zoneLabel = zone.label,
                                radiusMeters = zone.radiusMeters,
                                onLabelChange = { viewModel.updateLabel(it) },
                                onRadiusChange = { viewModel.updateRadius(it) }
                            )
                        }

                        // 6. Automation Activity Log
                        item {
                            ActivityLogView(
                                logs = activityLogs,
                                onClearLogs = { viewModel.clearLogs() }
                            )
                        }

                        // Bottom Spacer to prevent navigation bar overlapping
                        item {
                            Spacer(
                                modifier = Modifier
                                    .windowInsetsPadding(WindowInsets.navigationBars)
                                    .height(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
