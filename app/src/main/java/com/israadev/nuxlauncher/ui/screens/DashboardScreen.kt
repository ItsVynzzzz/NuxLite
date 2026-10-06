package com.israadev.nuxlauncher.ui.screens

import android.widget.Toast
import java.io.File
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.israadev.nuxlauncher.R
import com.israadev.nuxlauncher.core.account.AccountManager
import com.israadev.nuxlauncher.core.download.MinecraftDownloader
import com.israadev.nuxlauncher.core.instance.InstanceManager
import com.israadev.nuxlauncher.core.launch.GameLauncher
import com.israadev.nuxlauncher.core.runtime.JavaRuntimeManager
import com.israadev.nuxlauncher.core.crash.CrashManager
import com.israadev.nuxlauncher.core.settings.SettingsManager
import com.israadev.nuxlauncher.ui.components.*
import com.israadev.nuxlauncher.core.renderer.NuxRendererRegistry
import com.israadev.nuxlauncher.core.renderer.NuxRendererInfo
import com.israadev.nuxlauncher.ui.dialogs.NuxRendererWarningDialog
import com.israadev.nuxlauncher.ui.dialogs.NuxAddInstanceDialog
import com.israadev.nuxlauncher.ui.dialogs.NuxEditInstanceDialog
import com.israadev.nuxlauncher.ui.dialogs.NuxCrashDialog
import com.israadev.nuxlauncher.ui.dialogs.NuxDeleteInstanceDialog
import com.israadev.nuxlauncher.ui.dialogs.NuxDownloadProgressDialog
import com.israadev.nuxlauncher.core.mods.NuxAddonImportManager
import com.israadev.nuxlauncher.ui.dialogs.NuxAddonImportDialog
import com.israadev.nuxlauncher.ui.dialogs.NuxAboutDialog
import com.israadev.nuxlauncher.ui.theme.NuxColors
import com.israadev.nuxlauncher.ui.theme.resp
import com.israadev.nuxlauncher.ui.theme.LocalNuxScale
import kotlinx.coroutines.launch
import com.israadev.nuxlauncher.ui.theme.NuxSizes

@Composable
fun DashboardScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var currentTab by remember { mutableStateOf("home") }

    val activeCrash by CrashManager.activeCrash.collectAsState()

    val instances by InstanceManager.instances.collectAsState()
    val selectedInstance by InstanceManager.selectedInstance.collectAsState()
    val currentAccount by AccountManager.currentAccount.collectAsState()
    val launcherSettings by SettingsManager.settings.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showEditInstanceDialog by remember { mutableStateOf(false) }
    var instanceToDelete by remember { mutableStateOf<com.israadev.nuxlauncher.core.models.Instance?>(null) }
    var pendingLaunchInstance by remember { mutableStateOf<com.israadev.nuxlauncher.core.models.Instance?>(null) }
    var unsupportedRendererInfo by remember { mutableStateOf<NuxRendererInfo?>(null) }
    var showAboutDialog by remember { mutableStateOf(false) }
    val pendingImport by NuxAddonImportManager.pendingImport.collectAsState()

    val handleRequestCreateInstance = { showAddDialog = true }

    // Download progress state
    var isDownloading by remember { mutableStateOf(false) }
    var downloadTargetName by remember { mutableStateOf("") }
    var downloadProgress by remember { mutableFloatStateOf(0f) }
    var downloadMessage by remember { mutableStateOf("") }

    val downloader = remember { MinecraftDownloader(context) }

    if (currentTab == "gui_editor") {
        CustomGuiEditorScreen(
            onNavigateBack = { currentTab = "settings" },
            modifier = Modifier.fillMaxSize()
        )
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(NuxColors.Background)
        ) {
            Row(
                modifier = Modifier.fillMaxSize()
            ) {
                // --- 1. COMPACT LEFT SIDEBAR ---
                NuxSidebar(
                    activeTab = currentTab,
                    onTabSelected = { tabId ->
                        if (tabId == "home" || tabId == "accounts" || tabId == "settings" || tabId == "mods") {
                            currentTab = tabId
                        } else {
                            Toast.makeText(context, "Fitur ${tabId.replaceFirstChar { it.uppercase() }} segera hadir di mobile!", Toast.LENGTH_SHORT).show()
                        }
                    },
                )

                // --- 2. MAIN CONTENT AREA ---
                if (currentTab == "accounts") {
                    AccountsScreen(
                        onNavigateBack = { currentTab = "home" },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                } else if (currentTab == "mods") {
                    ModsScreen(
                        onNavigateBack = { currentTab = "home" },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                } else if (currentTab == "settings") {
                    SettingsScreen(
                        onNavigateBack = { currentTab = "home" },
                        onOpenGuiEditor = { currentTab = "gui_editor" },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                } else {
                    // DASHBOARD SCREEN
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(start = (12.dp).resp(), end = (12.dp).resp(), top = (6.dp).resp(), bottom = (6.dp).resp())
                    ) {
                        // TOP BAR (With Unofficial Modified Version label in the center)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = (5.dp).resp())
                        ) {
                            Text(
                                text = "DASHBOARD",
                                color = NuxColors.DarkGray,
                                fontWeight = FontWeight.Black,
                                fontSize = (13.5.sp).resp(),
                                letterSpacing = (0.7.sp).resp(),
                                modifier = Modifier.align(Alignment.CenterStart)
                            )

                            // Center: Unofficial Modified Version Label (Zalith & GPL-3.0 Compliance, Clickable to About)
                            Box(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .background(
                                        NuxColors.DarkGray.copy(alpha = 0.10f),
                                        RoundedCornerShape((6.dp).resp())
                                    )
                                    .border(
                                        1.dp,
                                        NuxColors.DarkGray.copy(alpha = 0.30f),
                                        RoundedCornerShape((6.dp).resp())
                                    )
                                    .clickable { showAboutDialog = true }
                                    .padding(horizontal = (8.dp).resp(), vertical = (3.dp).resp()),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy((5.dp).resp())
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size((5.dp).resp())
                                            .background(NuxColors.Amber, CircleShape)
                                    )
                                    Text(
                                        text = "UNOFFICIAL MODIFIED VERSION",
                                        color = NuxColors.GrayNeutral,
                                        fontSize = (8.sp).resp(),
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = (0.6.sp).resp()
                                    )
                                    Spacer(modifier = Modifier.width((2.dp).resp()))
                                    Icon(
                                        imageVector = Icons.Outlined.Info,
                                        contentDescription = "Tentang & Lisensi",
                                        tint = NuxColors.GrayNeutral,
                                        modifier = Modifier.size((11.dp).resp())
                                    )
                                }
                            }

                            // Chip akun Minecraft aktif (klik untuk membuka halaman Akun)
                            val userShape = RoundedCornerShape((8.dp).resp())
                            Row(
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .clip(userShape)
                                    .background(NuxColors.SurfaceElevated, userShape)
                                    .border(NuxSizes.BorderWidth, NuxColors.CardBorder, userShape)
                                    .clickable { currentTab = "accounts" }
                                    .padding(horizontal = (8.dp).resp(), vertical = (3.5.dp).resp()),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size((6.5.dp).resp())
                                        .background(
                                            if (currentAccount != null) NuxColors.ForestGreen else NuxColors.Amber,
                                            CircleShape
                                        )
                                )
                                Spacer(modifier = Modifier.width((5.dp).resp()))
                                Text(
                                    text = currentAccount?.username ?: "BELUM ADA AKUN",
                                    color = NuxColors.DarkGray,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = (10.5.sp).resp()
                                )
                            }
                        }

                        // MAIN TWO-COLUMN SPLIT (Landscape Optimized)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            horizontalArrangement = Arrangement.spacedBy((10.dp).resp())
                        ) {
                            // LEFT COLUMN: HERO CARD + 3 QUICK ACTION CARDS
                            Column(
                                modifier = Modifier
                                    .weight(1.38f)
                                    .fillMaxHeight(),
                                verticalArrangement = Arrangement.spacedBy((7.dp).resp())
                            ) {
                                // 1. HERO CARD (Matching PC style)
                                val heroShape = RoundedCornerShape((15.dp).resp())
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1.35f)
                                        .clip(heroShape)
                                        .background(NuxColors.SurfaceElevated, heroShape)
                                        .border(NuxSizes.BorderWidth, NuxColors.CardBorder, heroShape)
                                ) {
                                    if (selectedInstance != null) {
                                        val inst = selectedInstance!!
                                        val isFullyDownloaded = inst.isDownloaded && InstanceManager.isInstanceDownloaded(context, inst)

                                        // Latar gambar statis (ringan, tanpa video)
                                        Image(
                                            painter = painterResource(id = R.drawable.mc_hero_bg),
                                            contentDescription = "Minecraft Scenery",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize(),
                                            alpha = 0.35f
                                        )

                                        // Dark gradient overlay
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(
                                                    Brush.horizontalGradient(
                                                        colors = listOf(
                                                            NuxColors.Background.copy(alpha = 0.96f),
                                                            NuxColors.SurfaceInput.copy(alpha = 0.86f),
                                                            NuxColors.SurfaceInput.copy(alpha = 0.33f)
                                                        )
                                                    )
                                                )
                                                .padding(horizontal = (16.dp).resp(), vertical = (10.dp).resp())
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxSize(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(
                                                    modifier = Modifier.weight(1f),
                                                    verticalArrangement = Arrangement.Center
                                                ) {
                                                    // • FABRIC EDITION pill badge
                                                    Row(
                                                        modifier = Modifier
                                                            .background(NuxColors.ForestGreen.copy(alpha = 0.15f), RoundedCornerShape((7.dp).resp()))
                                                            .border(1.dp, NuxColors.ForestGreen.copy(alpha = 0.4f), RoundedCornerShape((7.dp).resp()))
                                                            .padding(horizontal = (7.dp).resp(), vertical = (2.5.dp).resp()),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size((5.dp).resp())
                                                                .background(NuxColors.ForestGreen, CircleShape)
                                                        )
                                                        Spacer(modifier = Modifier.width((5.dp).resp()))
                                                        Text(
                                                            text = "${inst.loader.uppercase()} EDITION",
                                                            color = NuxColors.SageGreen,
                                                            fontSize = (9.5.sp).resp(),
                                                            fontWeight = FontWeight.Bold,
                                                            letterSpacing = (0.7.sp).resp()
                                                        )
                                                    }

                                                    Spacer(modifier = Modifier.height((4.dp).resp()))

                                                    // Large instance title
                                                    Text(
                                                        text = inst.name,
                                                        color = NuxColors.DarkGray,
                                                        fontWeight = FontWeight.Black,
                                                        fontSize = (21.sp).resp(),
                                                        letterSpacing = (-0.4).sp,
                                                        maxLines = 1
                                                    )

                                                    Spacer(modifier = Modifier.height((2.dp).resp()))

                                                    // Emerald gradient accent line
                                                    Box(
                                                        modifier = Modifier
                                                            .size(width = (32.dp).resp(), height = (2.5.dp).resp())
                                                            .background(
                                                                Brush.horizontalGradient(
                                                                    colors = listOf(NuxColors.ForestGreen, NuxColors.MintGreen)
                                                                ),
                                                                CircleShape
                                                            )
                                                    )

                                                    Spacer(modifier = Modifier.height((4.dp).resp()))

                                                    // Subtitle
                                                    Text(
                                                        text = "Version ${inst.mcVersion} — Click PLAY to launch this instance and craft seamlessly.",
                                                        color = NuxColors.GrayNeutral,
                                                        fontSize = (10.sp).resp(),
                                                        lineHeight = (13.sp).resp(),
                                                        maxLines = 2
                                                    )
                                                }

                                                Spacer(modifier = Modifier.width((12.dp).resp()))

                                                // Minimalist Cyber-Glass Play Button (Matching PC)
                                                val playBtnShape = RoundedCornerShape((14.dp).resp())
                                                Row(
                                                    modifier = Modifier
                                                        .clip(playBtnShape)
                                                        .background(NuxColors.SurfaceElevated, playBtnShape)
                                                        .border(
                                                            width = 1.dp,
                                                            color = NuxColors.ForestGreen.copy(alpha = 0.5f),
                                                            shape = playBtnShape
                                                        )
                                                        .clickable {
                                                            val account = currentAccount
                                                            if (account == null) {
                                                                Toast.makeText(context, "Silakan buat atau pilih akun terlebih dahulu!", Toast.LENGTH_SHORT).show()
                                                                currentTab = "accounts"
                                                                return@clickable
                                                            }

                                                            if (!isFullyDownloaded) {
                                                                val targetRuntime = JavaRuntimeManager.getRecommendedRuntime(inst.mcVersion)
                                                                isDownloading = true
                                                                downloadTargetName = inst.name
                                                                downloadProgress = 0f
                                                                downloadMessage = "Menyiapkan OpenJDK (${JavaRuntimeManager.getRuntimeDisplayName(targetRuntime)})..."

                                                                scope.launch {
                                                                    JavaRuntimeManager.extractRuntime(context, targetRuntime) { p, msg ->
                                                                        downloadProgress = p
                                                                        downloadMessage = msg
                                                                    }

                                                                    val res = downloader.downloadInstance(inst) { p, msg ->
                                                                        downloadProgress = p
                                                                        downloadMessage = msg
                                                                    }
                                                                    isDownloading = false
                                                                    if (res.isSuccess) {
                                                                        Toast.makeText(context, "Instalasi selesai! Tekan PLAY untuk bermain.", Toast.LENGTH_SHORT).show()
                                                                    } else {
                                                                        Toast.makeText(context, "Gagal mengunduh: ${res.exceptionOrNull()?.localizedMessage}", Toast.LENGTH_LONG).show()
                                                                    }
                                                                }
                                                            } else {
                                                                val targetRuntime = selectedInstance?.let { JavaRuntimeManager.getRecommendedRuntime(it.mcVersion) } ?: "jre-21"
                                                                if (!JavaRuntimeManager.isRuntimeInstalled(context, targetRuntime)) {
                                                                    isDownloading = true
                                                                    downloadTargetName = inst.name
                                                                    downloadProgress = 0f
                                                                    downloadMessage = "Menyiapkan OpenJDK (${JavaRuntimeManager.getRuntimeDisplayName(targetRuntime)})..."
                                                                    scope.launch {
                                                                        val extRes = JavaRuntimeManager.extractRuntime(context, targetRuntime) { p, msg ->
                                                                            downloadProgress = p
                                                                            downloadMessage = msg
                                                                        }
                                                                        isDownloading = false
                                                                        if (extRes.isSuccess) {
                                                                            Toast.makeText(context, "Meluncurkan ${inst.name}...", Toast.LENGTH_SHORT).show()
                                                                            GameLauncher.launch(context, inst, account)
                                                                        } else {
                                                                            Toast.makeText(context, "Gagal menyiapkan OpenJDK: ${extRes.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                                                                        }
                                                                    }
                                                                    return@clickable
                                                                }

                                                                val currentRendererInfo = NuxRendererRegistry.findRendererById(launcherSettings.selectedRenderer)
                                                                val isSupported = NuxRendererRegistry.isSupportedForVersion(currentRendererInfo, inst.mcVersion)
                                                                if (!isSupported) {
                                                                    unsupportedRendererInfo = currentRendererInfo
                                                                    pendingLaunchInstance = inst
                                                                } else {
                                                                    Toast.makeText(context, "Meluncurkan ${inst.name}...", Toast.LENGTH_SHORT).show()
                                                                    GameLauncher.launch(context, inst, account)
                                                                }
                                                            }
                                                        }
                                                        .padding(horizontal = (14.dp).resp(), vertical = (9.dp).resp()),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy((10.dp).resp())
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size((28.dp).resp())
                                                            .clip(CircleShape)
                                                            .background(NuxColors.ForestGreen),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(
                                                            imageVector = if (isFullyDownloaded) Icons.Default.PlayArrow else Icons.Default.Download,
                                                            contentDescription = "Action",
                                                            tint = NuxColors.DarkGray,
                                                            modifier = Modifier.size((18.dp).resp())
                                                        )
                                                    }

                                                    Text(
                                                        text = if (isFullyDownloaded) "PLAY" else "UNDUH",
                                                        color = NuxColors.DarkGray,
                                                        fontWeight = FontWeight.Black,
                                                        fontSize = (13.5.sp).resp(),
                                                        letterSpacing = (0.8.sp).resp()
                                                    )
                                                }
                                            }
                                        }
                                    } else {
                                        // EMPTY STATE HERO
                                        Row(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(horizontal = 20.dp, vertical = 14.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(
                                                modifier = Modifier.weight(1f),
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                Text(
                                                    text = "MULAI BERMAIN",
                                                    color = NuxColors.SageGreen,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp,
                                                    letterSpacing = 0.5.sp
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "Belum Ada Instance",
                                                    color = NuxColors.DarkGray,
                                                    fontWeight = FontWeight.Black,
                                                    fontSize = 18.sp,
                                                    maxLines = 1
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = "Buat instance Minecraft pertamamu untuk mulai bermain.",
                                                    color = NuxColors.GrayNeutral,
                                                    fontSize = 11.sp,
                                                    maxLines = 2
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(12.dp))

                                            NuxButton(
                                                onClick = { handleRequestCreateInstance() },
                                                backgroundColor = NuxColors.ForestGreen,
                                                contentColor = NuxColors.DarkGray,
                                                cornerRadius = 12.dp,
                                                modifier = Modifier.height(42.dp)
                                            ) {
                                                Text(
                                                    text = "+ BUAT INSTANCE",
                                                    color = NuxColors.DarkGray,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp,
                                                    letterSpacing = 0.5.sp
                                                )
                                            }
                                        }
                                    }
                                }

                                // 2. 3 QUICK ACTION CARDS
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(0.95f),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Card 2: OPEN GAME FOLDER
                                    QuickActionCard(
                                        title = "OPEN GAME\nFOLDER",
                                        description = "Inspect game directory.",
                                        icon = Icons.Outlined.Folder,
                                        accentColor = NuxColors.ForestGreen,
                                        onClick = {
                                            selectedInstance?.let { inst ->
                                                Toast.makeText(context, "Membuka folder instance...", Toast.LENGTH_SHORT).show()
                                                InstanceManager.openInstanceFolder(context, inst)
                                            } ?: run {
                                                Toast.makeText(context, "Pilih instance terlebih dahulu", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                    )

                                    // Card 3: INSTANCE EDIT
                                    val activeRuntimeName = selectedInstance?.let {
                                        if (it.javaRuntime != "auto") it.javaRuntime
                                        else JavaRuntimeManager.getRecommendedRuntime(it.mcVersion)
                                    }
                                    val activeJreDisplay = if (activeRuntimeName != null) {
                                        if (selectedInstance?.javaRuntime == "auto") "Auto (${activeRuntimeName.replace("jre-", "Java ")})"
                                        else activeRuntimeName.replace("jre-", "Java ")
                                    } else "Auto Java"
                                    QuickActionCard(
                                        title = "INSTANCE\nEDIT",
                                        description = selectedInstance?.let { "${it.name} · $activeJreDisplay" } ?: "Pilih instance",
                                        icon = Icons.Outlined.Tune,
                                        accentColor = NuxColors.MintGreen,
                                        onClick = {
                                            if (selectedInstance != null) {
                                                showEditInstanceDialog = true
                                            } else {
                                                Toast.makeText(context, "Pilih instance terlebih dahulu", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                    )

                                    // Card 4: DELETE INSTANCE (Rose Red Theme)
                                    QuickActionCard(
                                        title = "DELETE\nINSTANCE",
                                        description = "Erase this instance.",
                                        icon = Icons.Outlined.DeleteOutline,
                                        accentColor = NuxColors.ErrorRed,
                                        isDestructive = true,
                                        onClick = {
                                            selectedInstance?.let { inst ->
                                                instanceToDelete = inst
                                            } ?: run {
                                                Toast.makeText(context, "Pilih instance terlebih dahulu", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                    )
                                }
                            }

                            // RIGHT COLUMN: STREAMLINED INSTANCES LIST PANEL
                            val panelShape = RoundedCornerShape((15.dp).resp())
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(panelShape)
                                    .background(NuxColors.SurfaceElevated, panelShape)
                                    .border(NuxSizes.BorderWidth, NuxColors.CardBorder, panelShape)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding((10.dp).resp())
                                ) {
                                    // Section Header
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "INSTANCE (${instances.size})",
                                            color = NuxColors.DarkGray,
                                            fontWeight = FontWeight.Black,
                                            fontSize = (11.5.sp).resp(),
                                            letterSpacing = (0.5.sp).resp()
                                        )

                                        val addPillShape = RoundedCornerShape((7.dp).resp())
                                        Box(
                                            modifier = Modifier
                                                .clip(addPillShape)
                                                .background(NuxColors.ForestGreen.copy(alpha = 0.14f), addPillShape)
                                                .border(1.dp, NuxColors.ForestGreen.copy(alpha = 0.45f), addPillShape)
                                                .clickable { handleRequestCreateInstance() }
                                                .padding(horizontal = (8.dp).resp(), vertical = (3.5.dp).resp())
                                        ) {
                                            Text(
                                                text = "+ TAMBAH",
                                                color = NuxColors.SageGreen,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = (9.5.sp).resp()
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height((6.dp).resp()))

                                    // Instance List
                                    if (instances.isEmpty()) {
                                        val emptyListShape = RoundedCornerShape((9.dp).resp())
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .weight(1f)
                                                .clip(emptyListShape)
                                                .background(NuxColors.SurfaceWhite, emptyListShape)
                                                .border(1.dp, NuxColors.DarkGray.copy(alpha = 0.16f), emptyListShape)
                                                .padding((10.dp).resp()),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(text = "📦", fontSize = (18.sp).resp())
                                                Spacer(modifier = Modifier.height((3.dp).resp()))
                                                Text(
                                                    text = "Belum Ada Instance",
                                                    color = NuxColors.DarkGray,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = (11.5.sp).resp()
                                                )
                                                Spacer(modifier = Modifier.height((2.dp).resp()))
                                                Text(
                                                    text = "Klik '+ TAMBAH' untuk membuat.",
                                                    color = NuxColors.GrayNeutral,
                                                    fontSize = (9.5.sp).resp()
                                                )
                                            }
                                        }
                                    } else {
                                        LazyColumn(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .weight(1f),
                                            verticalArrangement = Arrangement.spacedBy((5.dp).resp())
                                        ) {
                                            items(instances) { inst ->
                                                val isSelected = inst.id == selectedInstance?.id
                                                val itemShape = RoundedCornerShape((9.dp).resp())
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clip(itemShape)
                                                        .background(
                                                            if (isSelected) NuxColors.ForestGreen.copy(alpha = 0.12f) else NuxColors.SurfaceWhite,
                                                            itemShape
                                                        )
                                                        .border(
                                                            width = 1.dp,
                                                            color = if (isSelected) NuxColors.ForestGreen.copy(alpha = 0.5f) else NuxColors.DarkGray.copy(alpha = 0.14f),
                                                            shape = itemShape
                                                        )
                                                        .clickable { InstanceManager.selectInstance(inst) }
                                                        .padding(horizontal = (10.dp).resp(), vertical = (6.5.dp).resp()),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    // Streamlined title & metadata
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                            text = inst.name,
                                                            color = NuxColors.DarkGray,
                                                            fontWeight = FontWeight.SemiBold,
                                                            fontSize = (12.5.sp).resp(),
                                                            maxLines = 1
                                                        )
                                                        Spacer(modifier = Modifier.height((1.dp).resp()))
                                                        Text(
                                                            text = "v${inst.mcVersion} · ${inst.loader.uppercase()}",
                                                            color = if (isSelected) NuxColors.SageGreen else NuxColors.GrayNeutral,
                                                            fontSize = (9.5.sp).resp(),
                                                            fontWeight = FontWeight.Medium
                                                        )
                                                    }

                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy((5.dp).resp())
                                                    ) {
                                                        if (isSelected) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .size((16.dp).resp())
                                                                    .background(NuxColors.ForestGreen, CircleShape),
                                                                contentAlignment = Alignment.Center
                                                            ) {
                                                                Icon(
                                                                    imageVector = Icons.Default.Check,
                                                                    contentDescription = "Selected",
                                                                    tint = NuxColors.DarkGray,
                                                                    modifier = Modifier.size((11.dp).resp())
                                                                )
                                                            }
                                                        }

                                                        // Minimalist subtle ghost trash button
                                                        Box(
                                                            modifier = Modifier
                                                                .size((22.dp).resp())
                                                                .clip(RoundedCornerShape((6.dp).resp()))
                                                                .clickable { instanceToDelete = inst },
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Outlined.DeleteOutline,
                                                                contentDescription = "Delete",
                                                                tint = NuxColors.GrayNeutral,
                                                                modifier = Modifier.size((14.dp).resp())
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

        }

        // Add Instance Dialog
        if (showAddDialog) {
            NuxAddInstanceDialog(
                onDismiss = { showAddDialog = false },
                onInstanceCreated = { newInst ->
                    showAddDialog = false
                    InstanceManager.createInstance(context, newInst)
                    Toast.makeText(context, "Instance ${newInst.name} berhasil dibuat!", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // Edit Instance Dialog
        if (showEditInstanceDialog) {
            selectedInstance?.let { inst ->
                NuxEditInstanceDialog(
                    instance = inst,
                    onDismiss = { showEditInstanceDialog = false },
                    onInstanceUpdated = { updated ->
                        InstanceManager.updateInstance(context, updated)
                    },
                    onInstanceDeleted = { toDelete ->
                        InstanceManager.deleteInstance(context, toDelete.id)
                        Toast.makeText(context, "Instance ${toDelete.name} berhasil dihapus!", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }

        // Delete Instance Confirmation Dialog
        instanceToDelete?.let { inst ->
            NuxDeleteInstanceDialog(
                instance = inst,
                onConfirm = {
                    instanceToDelete = null
                    InstanceManager.deleteInstance(context, inst.id)
                    Toast.makeText(context, "Instance ${inst.name} berhasil dihapus!", Toast.LENGTH_SHORT).show()
                },
                onDismiss = { instanceToDelete = null }
            )
        }

        // Download Progress Dialog
        if (isDownloading) {
            NuxDownloadProgressDialog(
                instanceName = downloadTargetName,
                progress = downloadProgress,
                message = downloadMessage
            )
        }

        // Unsupported Renderer Warning Dialog (Persis seperti Zalith)
        val warnRenderer = unsupportedRendererInfo
        val launchInst = pendingLaunchInstance
        if (warnRenderer != null && launchInst != null) {
            NuxRendererWarningDialog(
                renderer = warnRenderer,
                mcVersion = launchInst.mcVersion,
                onConfirm = {
                    val toLaunch = pendingLaunchInstance
                    val account = currentAccount
                    unsupportedRendererInfo = null
                    pendingLaunchInstance = null
                    if (toLaunch != null && account != null) {
                        Toast.makeText(context, "Meluncurkan ${toLaunch.name}...", Toast.LENGTH_SHORT).show()
                        GameLauncher.launch(context, toLaunch, account)
                    }
                },
                onDismiss = {
                    unsupportedRendererInfo = null
                    pendingLaunchInstance = null
                }
            )
        }

        // Game Crash Popup Dialog
        activeCrash?.let { crash ->
            NuxCrashDialog(
                crashInfo = crash,
                onDismiss = {
                    CrashManager.dismissCrash(context)
                }
            )
        }

        // External Addon Import Dialog (Open With from File Manager)
        pendingImport?.let { importItem ->
            NuxAddonImportDialog(
                pendingImport = importItem,
                onDismiss = {
                    NuxAddonImportManager.clearPendingImport()
                }
            )
        }

        // About & Open Source Licenses Dialog (GPL-3.0 & Zalith Compliance)
        if (showAboutDialog) {
            NuxAboutDialog(
                onDismissRequest = { showAboutDialog = false }
            )
        }

    }
}

/**
 * Quick Action Card matching the PC Launcher-Windows aesthetic (Image 2)
 */
@Composable
fun QuickActionCard(
    title: String,
    description: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDestructive: Boolean = false
) {
    val cardShape = RoundedCornerShape((11.dp).resp())
    val bgColor = if (isDestructive) NuxColors.ErrorRed.copy(alpha = 0.10f) else NuxColors.SurfaceWhite
    val borderColor = if (isDestructive) NuxColors.ErrorRed.copy(alpha = 0.25f) else NuxColors.DarkGray.copy(alpha = 0.24f)

    Box(
        modifier = modifier
            .clip(cardShape)
            .background(bgColor, cardShape)
            .border(1.dp, borderColor, cardShape)
            .clickable { onClick() }
            .padding((8.dp).resp())
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                // Top icon container
                val iconShape = RoundedCornerShape((6.dp).resp())
                Box(
                    modifier = Modifier
                        .size((24.dp).resp())
                        .clip(iconShape)
                        .background(accentColor.copy(alpha = 0.12f), iconShape)
                        .border(1.dp, accentColor.copy(alpha = 0.25f), iconShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size((14.dp).resp())
                    )
                }

                Spacer(modifier = Modifier.height((4.dp).resp()))

                // Title
                Text(
                    text = title,
                    color = if (isDestructive) NuxColors.ErrorRed else NuxColors.DarkGray,
                    fontSize = (9.5.sp).resp(),
                    fontWeight = FontWeight.Black,
                    lineHeight = (11.5.sp).resp(),
                    letterSpacing = (0.3.sp).resp()
                )

                Spacer(modifier = Modifier.height((2.dp).resp()))

                // Accent line
                Box(
                    modifier = Modifier
                        .size(width = (16.dp).resp(), height = (1.5.dp).resp())
                        .background(accentColor.copy(alpha = 0.45f), CircleShape)
                )
            }

            // Description
            Text(
                text = description,
                color = NuxColors.GrayNeutral,
                fontSize = (8.sp).resp(),
                lineHeight = (9.5.sp).resp(),
                maxLines = 2
            )
        }
    }
}
