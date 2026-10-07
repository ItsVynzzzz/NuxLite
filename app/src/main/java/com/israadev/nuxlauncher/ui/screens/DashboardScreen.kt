package com.israadev.nuxlauncher.ui.screens

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.drawable.AnimatedImageDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.israadev.nuxlauncher.R
import com.israadev.nuxlauncher.core.account.AccountManager
import com.israadev.nuxlauncher.core.crash.CrashManager
import com.israadev.nuxlauncher.core.download.MinecraftDownloader
import com.israadev.nuxlauncher.core.instance.InstanceManager
import com.israadev.nuxlauncher.core.launch.GameLauncher
import com.israadev.nuxlauncher.core.mods.NuxAddonImportManager
import com.israadev.nuxlauncher.core.models.Instance
import com.israadev.nuxlauncher.core.renderer.NuxRendererInfo
import com.israadev.nuxlauncher.core.renderer.NuxRendererRegistry
import com.israadev.nuxlauncher.core.runtime.JavaRuntimeManager
import com.israadev.nuxlauncher.core.settings.SettingsManager
import com.israadev.nuxlauncher.ui.components.NuxButton
import com.israadev.nuxlauncher.ui.components.NuxCard
import com.israadev.nuxlauncher.ui.components.NuxDialog
import com.israadev.nuxlauncher.ui.components.NuxSidebar
import com.israadev.nuxlauncher.ui.dialogs.NuxAboutDialog
import com.israadev.nuxlauncher.ui.dialogs.NuxAddInstanceDialog
import com.israadev.nuxlauncher.ui.dialogs.NuxAddonImportDialog
import com.israadev.nuxlauncher.ui.dialogs.NuxCrashDialog
import com.israadev.nuxlauncher.ui.dialogs.NuxDeleteInstanceDialog
import com.israadev.nuxlauncher.ui.dialogs.NuxDownloadProgressDialog
import com.israadev.nuxlauncher.ui.dialogs.NuxEditInstanceDialog
import com.israadev.nuxlauncher.ui.dialogs.NuxRendererWarningDialog
import com.israadev.nuxlauncher.ui.theme.NuxColors
import com.israadev.nuxlauncher.ui.theme.NuxSizes
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    var showInstancePicker by remember { mutableStateOf(false) }
    var instanceToDelete by remember { mutableStateOf<Instance?>(null) }
    var pendingLaunchInstance by remember { mutableStateOf<Instance?>(null) }
    var unsupportedRendererInfo by remember { mutableStateOf<NuxRendererInfo?>(null) }
    var showAboutDialog by remember { mutableStateOf(false) }
    val pendingImport by NuxAddonImportManager.pendingImport.collectAsState()

    // Download progress state
    var isDownloading by remember { mutableStateOf(false) }
    var downloadTargetName by remember { mutableStateOf("") }
    var downloadProgress by remember { mutableFloatStateOf(0f) }
    var downloadMessage by remember { mutableStateOf("") }

    val downloader = remember { MinecraftDownloader(context) }

    // Latar belakang Home yang bisa diganti (foto atau GIF dari galeri)
    val bgFile = remember { File(context.filesDir, BG_FILE_NAME) }
    var hasCustomBg by remember { mutableStateOf(bgFile.exists()) }
    var bgVersion by remember { mutableIntStateOf(0) }
    var showBgDialog by remember { mutableStateOf(false) }
    val pickBackground = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            scope.launch {
                val ok = withContext(Dispatchers.IO) { importBackground(context, uri, bgFile) }
                if (ok) {
                    hasCustomBg = true
                    bgVersion++
                    Toast.makeText(context, "Latar belakang diganti", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Gambar tidak bisa dipakai (maksimal 30 MB, format foto atau GIF)", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // Status unduhan instance terpilih: dihitung ulang hanya saat instance berganti
    // atau saat proses unduh selesai (hindari cek file di tiap recomposition).
    val isInstanceReady = remember(selectedInstance, isDownloading) {
        selectedInstance?.let { it.isDownloaded && InstanceManager.isInstanceDownloaded(context, it) } ?: false
    }

    // Tombol PLAY / UNDUH: unduh dulu bila belum lengkap, siapkan Java bila belum ada, lalu luncurkan.
    fun startInstance(inst: Instance) {
        val account = currentAccount
        if (account == null) {
            Toast.makeText(context, "Silakan buat atau pilih akun terlebih dahulu!", Toast.LENGTH_SHORT).show()
            currentTab = "accounts"
            return
        }

        val isFullyDownloaded = inst.isDownloaded && InstanceManager.isInstanceDownloaded(context, inst)
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
            val targetRuntime = JavaRuntimeManager.getRecommendedRuntime(inst.mcVersion)
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
                return
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

    if (currentTab == "gui_editor") {
        CustomGuiEditorScreen(
            onNavigateBack = { currentTab = "settings" },
            modifier = Modifier.fillMaxSize()
        )
    } else {
        // Area poni/kamera di sisi layar diisi rel hitam (kiri) atau penutup hitam (kanan),
        // jadi tidak ada ruang kosong. Sisanya: bingkai kuning + kartu panel bersudut bulat.
        val cutout = WindowInsets.displayCutout.asPaddingValues()
        val layoutDir = LocalLayoutDirection.current
        val leftInset = cutout.calculateLeftPadding(layoutDir)
        val rightInset = cutout.calculateRightPadding(layoutDir)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(NuxColors.Yellow)
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                NuxSidebar(
                    activeTab = currentTab,
                    onTabSelected = { tabId -> currentTab = tabId },
                    startInset = leftInset,
                    onOpenAbout = { showAboutDialog = true }
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        // Kanan & bawah lebih lebar 3dp untuk ruang bayangan keras kartu.
                        .padding(start = 8.dp, top = 8.dp, end = 11.dp, bottom = 11.dp)
                ) {
                    NuxCard(
                        modifier = Modifier.fillMaxSize(),
                        backgroundColor = NuxColors.Background,
                        cornerRadius = 26.dp,
                        fillMaxHeight = true
                    ) {
                        when (currentTab) {
                            "accounts" -> AccountsScreen(
                                onNavigateBack = { currentTab = "home" },
                                modifier = Modifier.fillMaxSize()
                            )
                            "mods" -> ModsScreen(
                                onNavigateBack = { currentTab = "home" },
                                modifier = Modifier.fillMaxSize()
                            )
                            "settings" -> SettingsScreen(
                                onNavigateBack = { currentTab = "home" },
                                onOpenGuiEditor = { currentTab = "gui_editor" },
                                modifier = Modifier.fillMaxSize()
                            )
                            else -> HomeScene(
                                instance = selectedInstance,
                                isReady = isInstanceReady,
                                accountName = currentAccount?.username,
                                onOpenAccounts = { currentTab = "accounts" },
                                onOpenAbout = { showAboutDialog = true },
                                onPickInstance = { showInstancePicker = true },
                                onPlay = { selectedInstance?.let { startInstance(it) } },
                                onCreateInstance = { showAddDialog = true },
                                backgroundFile = if (hasCustomBg) bgFile else null,
                                backgroundVersion = bgVersion,
                                onChangeBackground = { showBgDialog = true },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }

                if (rightInset > 0.dp) {
                    Box(
                        modifier = Modifier
                            .width(rightInset)
                            .fillMaxHeight()
                            .background(
                                NuxColors.Ink,
                                RoundedCornerShape(topStart = 26.dp, bottomStart = 26.dp)
                            )
                    )
                }
            }
        }

        // Dialog ganti latar belakang
        if (showBgDialog) {
            BackgroundDialog(
                hasCustom = hasCustomBg,
                onPick = {
                    showBgDialog = false
                    pickBackground.launch("image/*")
                },
                onReset = {
                    showBgDialog = false
                    bgFile.delete()
                    hasCustomBg = false
                    bgVersion++
                    Toast.makeText(context, "Latar belakang dikembalikan ke bawaan", Toast.LENGTH_SHORT).show()
                },
                onDismiss = { showBgDialog = false }
            )
        }

        // Pemilih instance (ganti, tambah, ubah, hapus, buka folder)
        if (showInstancePicker) {
            InstancePickerDialog(
                instances = instances,
                selectedId = selectedInstance?.id,
                onSelect = { inst ->
                    InstanceManager.selectInstance(inst)
                    showInstancePicker = false
                },
                onOpenFolder = { inst ->
                    showInstancePicker = false
                    Toast.makeText(context, "Membuka folder instance...", Toast.LENGTH_SHORT).show()
                    InstanceManager.openInstanceFolder(context, inst)
                },
                onEdit = { inst ->
                    showInstancePicker = false
                    InstanceManager.selectInstance(inst)
                    showEditInstanceDialog = true
                },
                onDelete = { inst ->
                    showInstancePicker = false
                    instanceToDelete = inst
                },
                onAdd = {
                    showInstancePicker = false
                    showAddDialog = true
                },
                onDismiss = { showInstancePicker = false }
            )
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
 * Layar Home: gambar latar penuh + dua chip kaca di atas (akun, label lisensi)
 * + "dock" kaca hitam di bawah berisi instance aktif dan tombol PLAY kuning.
 */
@Composable
private fun HomeScene(
    instance: Instance?,
    isReady: Boolean,
    accountName: String?,
    onOpenAccounts: () -> Unit,
    onOpenAbout: () -> Unit,
    onPickInstance: () -> Unit,
    onPlay: () -> Unit,
    onCreateInstance: () -> Unit,
    backgroundFile: File?,
    backgroundVersion: Int,
    onChangeBackground: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        // Latar gambar/GIF memenuhi panel
        HomeBackground(
            customFile = backgroundFile,
            version = backgroundVersion,
            modifier = Modifier.fillMaxSize()
        )

        // Peredup lembut di atas dan bawah supaya chip dan dock terbaca
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to NuxColors.Ink.copy(alpha = 0.40f),
                        0.28f to Color.Transparent,
                        0.55f to Color.Transparent,
                        1f to NuxColors.Ink.copy(alpha = 0.60f)
                    )
                )
        )

        // ---- BARIS ATAS ----
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
                .padding(start = 14.dp, end = 14.dp, top = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Chip akun (ketuk = ganti / tambah akun)
            GlassChip(onClick = onOpenAccounts, minHeight = 42.dp) {
                Box(
                    modifier = Modifier
                        .padding(start = 6.dp)
                        .size(30.dp)
                        .background(NuxColors.Yellow, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = accountName?.firstOrNull()?.uppercase() ?: "+",
                        color = NuxColors.Ink,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = accountName ?: "Tambah akun",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.widthIn(max = 150.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Ganti akun",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
            }

            Spacer(modifier = Modifier.weight(1f))

            // Label wajib GPL-3.0 (ketuk = Tentang & Lisensi)
            GlassChip(onClick = onOpenAbout, minHeight = 30.dp) {
                Spacer(modifier = Modifier.width(10.dp))
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(NuxColors.Amber, CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "UNOFFICIAL MODIFIED VERSION",
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.6.sp,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = "Tentang & Lisensi",
                    tint = Color.White,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Tombol ganti latar belakang
            GlassChip(onClick = onChangeBackground, minHeight = 42.dp) {
                Spacer(modifier = Modifier.width(10.dp))
                Icon(
                    imageVector = Icons.Outlined.Image,
                    contentDescription = "Ganti latar belakang",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
            }
        }

        // ---- DOCK BAWAH ----
        val dockShape = RoundedCornerShape(28.dp)
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(start = 12.dp, end = 12.dp, bottom = 12.dp)
                .clip(dockShape)
                .background(NuxColors.Ink.copy(alpha = 0.76f), dockShape)
                .border(1.5.dp, Color.White.copy(alpha = 0.16f), dockShape)
                .padding(start = 10.dp, end = 16.dp, top = 10.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (instance != null) {
                val javaLabel = remember(instance.id, instance.mcVersion, instance.javaRuntime) {
                    val rt = if (instance.javaRuntime != "auto") instance.javaRuntime
                    else JavaRuntimeManager.getRecommendedRuntime(instance.mcVersion)
                    rt.replace("jre-", "Java ")
                }

                // Kiri: ubin inisial + nama + tag (ketuk = ganti instance)
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onPickInstance() },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val tileShape = RoundedCornerShape(18.dp)
                    Box(
                        modifier = Modifier
                            .size(58.dp)
                            .background(NuxColors.Yellow, tileShape)
                            .border(NuxSizes.BorderWidth, NuxColors.CardBorder, tileShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = instance.name.firstOrNull()?.uppercase() ?: "?",
                            color = NuxColors.Ink,
                            fontWeight = FontWeight.Black,
                            fontSize = 28.sp
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = instance.name,
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 24.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(Color.White.copy(alpha = 0.16f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Ganti instance",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(5.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            MetaTag(text = instance.loader.replaceFirstChar { it.uppercase() }, filled = true)
                            MetaTag(text = instance.mcVersion)
                            MetaTag(text = javaLabel)
                        }
                    }
                }

                // Kanan: tombol PLAY / UNDUH
                NuxButton(
                    onClick = onPlay,
                    backgroundColor = NuxColors.Yellow,
                    contentColor = NuxColors.Ink,
                    cornerRadius = 32.dp,
                    shadowOffset = 4.dp,
                    contentPadding = PaddingValues(horizontal = 26.dp, vertical = 12.dp),
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    Icon(
                        imageVector = if (isReady) Icons.Default.PlayArrow else Icons.Default.Download,
                        contentDescription = null,
                        modifier = Modifier.size(30.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isReady) "PLAY" else "UNDUH",
                        fontWeight = FontWeight.Black,
                        fontSize = 24.sp,
                        letterSpacing = 1.sp,
                        maxLines = 1
                    )
                }
            } else {
                // Belum ada instance
                val tileShape = RoundedCornerShape(18.dp)
                Box(
                    modifier = Modifier
                        .size(58.dp)
                        .background(NuxColors.Yellow, tileShape)
                        .border(NuxSizes.BorderWidth, NuxColors.CardBorder, tileShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = NuxColors.Ink,
                        modifier = Modifier.size(30.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Belum ada instance",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 22.sp,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Buat instance Minecraft pertamamu untuk mulai bermain.",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp,
                        maxLines = 2
                    )
                }
                NuxButton(
                    onClick = onCreateInstance,
                    backgroundColor = NuxColors.Yellow,
                    contentColor = NuxColors.Ink,
                    cornerRadius = 32.dp,
                    shadowOffset = 4.dp,
                    contentPadding = PaddingValues(horizontal = 22.dp, vertical = 12.dp),
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "TAMBAH",
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        letterSpacing = 0.5.sp,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

private const val BG_FILE_NAME = "home_bg"
private const val BG_MAX_BYTES = 30L * 1024 * 1024
private const val BG_MAX_SIDE = 1600

/** Hasil muat latar: drawable null berarti gagal. */
private class BgState(val drawable: Drawable?)

/**
 * Decode foto/GIF/WebP jadi Drawable. Gambar dikecilkan (sisi terpanjang <= 1600 px)
 * supaya hemat RAM; GIF/WebP animasi menjadi AnimatedImageDrawable (Android 9 ke atas).
 */
private fun decodeBackground(file: File): Drawable? = try {
    if (Build.VERSION.SDK_INT >= 28) {
        ImageDecoder.decodeDrawable(ImageDecoder.createSource(file)) { decoder, info, _ ->
            val w = info.size.width
            val h = info.size.height
            val longSide = maxOf(w, h)
            if (longSide > BG_MAX_SIDE) {
                val scale = BG_MAX_SIDE.toFloat() / longSide
                decoder.setTargetSize(maxOf(1, (w * scale).toInt()), maxOf(1, (h * scale).toInt()))
            }
        }
    } else {
        val opts = BitmapFactory.Options().apply { inSampleSize = 2 }
        BitmapFactory.decodeFile(file.absolutePath, opts)?.let {
            BitmapDrawable(android.content.res.Resources.getSystem(), it)
        }
    }
} catch (e: Throwable) {
    null
}

/** Salin gambar pilihan ke penyimpanan aplikasi, uji decode, lalu pasang sebagai latar. */
private fun importBackground(context: Context, uri: Uri, target: File): Boolean {
    val tmp = File(target.parentFile, "$BG_FILE_NAME.tmp")
    return try {
        val input = context.contentResolver.openInputStream(uri) ?: return false
        input.use { ins ->
            tmp.outputStream().use { out ->
                val buf = ByteArray(64 * 1024)
                var total = 0L
                while (true) {
                    val n = ins.read(buf)
                    if (n < 0) break
                    total += n
                    if (total > BG_MAX_BYTES) throw java.io.IOException("terlalu besar")
                    out.write(buf, 0, n)
                }
            }
        }
        if (decodeBackground(tmp) == null) {
            tmp.delete()
            return false
        }
        if (target.exists()) target.delete()
        tmp.renameTo(target)
    } catch (e: Throwable) {
        tmp.delete()
        false
    }
}

/** Latar Home: gambar bawaan, atau foto/GIF pilihan pengguna bila ada. */
@Composable
private fun HomeBackground(customFile: File?, version: Int, modifier: Modifier = Modifier) {
    if (customFile == null) {
        Image(
            painter = painterResource(id = R.drawable.mc_hero_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
        return
    }

    val state by produceState<BgState?>(initialValue = null, customFile, version) {
        value = BgState(withContext(Dispatchers.IO) { decodeBackground(customFile) })
    }
    val st = state
    val drawable = st?.drawable
    when {
        st == null -> Box(modifier = modifier.background(NuxColors.Ink))
        drawable == null -> Image(
            painter = painterResource(id = R.drawable.mc_hero_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
        else -> {
            // GIF: berhenti otomatis saat keluar dari layar Home
            DisposableEffect(drawable) {
                onDispose {
                    if (Build.VERSION.SDK_INT >= 28 && drawable is AnimatedImageDrawable) drawable.stop()
                }
            }
            AndroidView(
                factory = { ctx -> ImageView(ctx).apply { scaleType = ImageView.ScaleType.CENTER_CROP } },
                update = { iv ->
                    iv.setImageDrawable(drawable)
                    if (Build.VERSION.SDK_INT >= 28 && drawable is AnimatedImageDrawable) {
                        drawable.repeatCount = AnimatedImageDrawable.REPEAT_INFINITE
                        drawable.start()
                    }
                },
                modifier = modifier
            )
        }
    }
}

/** Dialog kecil: pilih foto/GIF dari galeri atau kembali ke latar bawaan. */
@Composable
private fun BackgroundDialog(
    hasCustom: Boolean,
    onPick: () -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {
    NuxDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.widthIn(max = 460.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "LATAR BELAKANG",
                    color = NuxColors.Ink,
                    fontWeight = FontWeight.Black,
                    fontSize = 17.sp,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.weight(1f)
                )
                PickerIconButton(
                    icon = Icons.Default.Close,
                    description = "Tutup",
                    tint = NuxColors.Ink,
                    onClick = onDismiss
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Pilih foto atau GIF dari galeri untuk jadi latar Home. " +
                    "Gambar dikecilkan otomatis agar ringan. GIF yang bergerak memakai baterai sedikit lebih banyak.",
                color = NuxColors.GrayNeutral,
                fontSize = 12.sp,
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            NuxButton(
                onClick = onPick,
                backgroundColor = NuxColors.Yellow,
                contentColor = NuxColors.Ink,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Image,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "PILIH FOTO / GIF",
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    letterSpacing = 0.5.sp
                )
            }

            if (hasCustom) {
                Spacer(modifier = Modifier.height(10.dp))
                NuxButton(
                    onClick = onReset,
                    backgroundColor = NuxColors.SurfaceWhite,
                    contentColor = NuxColors.Ink,
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = "KEMBALI KE BAWAAN",
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}

/** Chip kaca: latar hitam transparan, tepi putih tipis, isi di dalam Row. */
@Composable
private fun GlassChip(
    onClick: () -> Unit,
    minHeight: androidx.compose.ui.unit.Dp,
    content: @Composable RowScope.() -> Unit
) {
    val shape = RoundedCornerShape(22.dp)
    Row(
        modifier = Modifier
            .clip(shape)
            .background(NuxColors.Ink.copy(alpha = 0.62f), shape)
            .border(1.5.dp, Color.White.copy(alpha = 0.18f), shape)
            .clickable { onClick() }
            .defaultMinSize(minHeight = minHeight),
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}

/** Tag kecil di dock: terisi kuning (loader) atau hanya garis putih (versi, Java). */
@Composable
private fun MetaTag(text: String, filled: Boolean = false) {
    val shape = RoundedCornerShape(10.dp)
    Text(
        text = text,
        color = if (filled) NuxColors.Ink else Color.White,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        maxLines = 1,
        modifier = Modifier
            .then(
                if (filled) Modifier.background(NuxColors.Yellow, shape)
                else Modifier.border(1.dp, Color.White.copy(alpha = 0.4f), shape)
            )
            .padding(horizontal = 8.dp, vertical = 2.dp)
    )
}

/**
 * Dialog pemilih instance: ketuk baris untuk memilih; ikon di kanan untuk
 * buka folder, ubah, atau hapus; tombol kuning di bawah untuk menambah instance.
 */
@Composable
private fun InstancePickerDialog(
    instances: List<Instance>,
    selectedId: String?,
    onSelect: (Instance) -> Unit,
    onOpenFolder: (Instance) -> Unit,
    onEdit: (Instance) -> Unit,
    onDelete: (Instance) -> Unit,
    onAdd: () -> Unit,
    onDismiss: () -> Unit
) {
    NuxDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.widthIn(max = 460.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PILIH INSTANCE",
                    color = NuxColors.Ink,
                    fontWeight = FontWeight.Black,
                    fontSize = 17.sp,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.weight(1f)
                )
                PickerIconButton(
                    icon = Icons.Default.Close,
                    description = "Tutup",
                    tint = NuxColors.Ink,
                    onClick = onDismiss
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (instances.isEmpty()) {
                Text(
                    text = "Belum ada instance. Buat yang pertama di bawah.",
                    color = NuxColors.GrayNeutral,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(vertical = 14.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 210.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(instances, key = { it.id }) { inst ->
                        val isSelected = inst.id == selectedId
                        val rowShape = RoundedCornerShape(16.dp)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(rowShape)
                                .background(if (isSelected) NuxColors.YellowPale else NuxColors.SurfaceWhite, rowShape)
                                .border(
                                    NuxSizes.BorderWidth,
                                    if (isSelected) NuxColors.Ink else NuxColors.LightGray,
                                    rowShape
                                )
                                .clickable { onSelect(inst) }
                                .padding(start = 14.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = inst.name,
                                    color = NuxColors.Ink,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${inst.mcVersion} · ${inst.loader.replaceFirstChar { it.uppercase() }}",
                                    color = NuxColors.GrayNeutral,
                                    fontSize = 12.sp,
                                    maxLines = 1
                                )
                            }
                            PickerIconButton(
                                icon = Icons.Outlined.Folder,
                                description = "Buka folder",
                                tint = NuxColors.Ink,
                                onClick = { onOpenFolder(inst) }
                            )
                            PickerIconButton(
                                icon = Icons.Outlined.Tune,
                                description = "Ubah instance",
                                tint = NuxColors.Ink,
                                onClick = { onEdit(inst) }
                            )
                            PickerIconButton(
                                icon = Icons.Outlined.DeleteOutline,
                                description = "Hapus instance",
                                tint = NuxColors.ErrorRed,
                                onClick = { onDelete(inst) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            NuxButton(
                onClick = onAdd,
                backgroundColor = NuxColors.Yellow,
                contentColor = NuxColors.Ink,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "TAMBAH INSTANCE",
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

/** Tombol ikon bulat 40dp (target sentuh nyaman) untuk dialog pemilih instance. */
@Composable
private fun PickerIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    tint: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = tint,
            modifier = Modifier.size(22.dp)
        )
    }
}
