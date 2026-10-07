package com.israadev.nuxlauncher.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.foundation.layout.windowInsetsPadding
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
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import kotlinx.coroutines.launch

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
        // Bingkai kuning di tepi layar + panel putih bersudut bulat + sidebar pil.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(NuxColors.Yellow)
                .windowInsetsPadding(WindowInsets.displayCutout)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    // Kanan & bawah lebih lebar 3dp untuk ruang bayangan keras kartu.
                    .padding(start = 8.dp, top = 8.dp, end = 11.dp, bottom = 11.dp),
                horizontalArrangement = Arrangement.spacedBy(11.dp)
            ) {
                NuxSidebar(
                    activeTab = currentTab,
                    onTabSelected = { tabId -> currentTab = tabId }
                )

                NuxCard(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
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
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
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

/** Bayangan teks keras ala kartun supaya teks putih terbaca di atas gambar apa pun. */
private val HardTextShadow = TextStyle(
    shadow = Shadow(color = NuxColors.Ink, offset = Offset(5f, 5f), blurRadius = 0f)
)

/**
 * Layar Home: gambar latar penuh, pil akun (kiri atas), pil versi (kanan atas),
 * nama instance + loader (kiri bawah), tombol PLAY besar (kanan bawah).
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
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        // Latar gambar memenuhi panel
        Image(
            painter = painterResource(id = R.drawable.mc_hero_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Peredup bagian bawah supaya teks putih terbaca
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color.Transparent,
                        0.4f to Color.Transparent,
                        1f to NuxColors.Ink.copy(alpha = 0.80f)
                    )
                )
        )

        // ---- BARIS ATAS ----
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
                .padding(start = 14.dp, end = 17.dp, top = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Pil akun (ketuk = ganti / tambah akun)
            NuxButton(
                onClick = onOpenAccounts,
                backgroundColor = NuxColors.Yellow,
                contentColor = NuxColors.Ink,
                cornerRadius = 22.dp,
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = accountName ?: "Tambah akun",
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.widthIn(max = 150.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Ganti akun",
                    modifier = Modifier.size(20.dp)
                )
            }

            // Label wajib GPL-3.0 (ketuk = Tentang & Lisensi)
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                val chipShape = RoundedCornerShape(12.dp)
                Row(
                    modifier = Modifier
                        .clip(chipShape)
                        .background(NuxColors.Ink.copy(alpha = 0.72f), chipShape)
                        .clickable { onOpenAbout() }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(NuxColors.Amber, CircleShape)
                    )
                    Text(
                        text = "UNOFFICIAL MODIFIED VERSION",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.6.sp,
                        maxLines = 1
                    )
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = "Tentang & Lisensi",
                        tint = Color.White,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }

            // Pil versi Minecraft (lencana, bukan tombol)
            if (instance != null) {
                val versionShape = RoundedCornerShape(22.dp)
                Box(
                    modifier = Modifier
                        .background(NuxColors.Yellow, versionShape)
                        .border(NuxSizes.BorderWidth, NuxColors.CardBorder, versionShape)
                        .padding(horizontal = 16.dp, vertical = 9.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = instance.mcVersion,
                        color = NuxColors.Ink,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        maxLines = 1
                    )
                }
            }
        }

        // ---- BARIS BAWAH ----
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(start = 20.dp, end = 25.dp, bottom = 25.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (instance != null) {
                Column(modifier = Modifier.weight(1f)) {
                    // Pil loader
                    val loaderShape = RoundedCornerShape(14.dp)
                    Box(
                        modifier = Modifier
                            .background(NuxColors.Yellow, loaderShape)
                            .border(NuxSizes.BorderWidth, NuxColors.CardBorder, loaderShape)
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = instance.loader.replaceFirstChar { it.uppercase() },
                            color = NuxColors.Ink,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            maxLines = 1
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Nama instance besar (ketuk = ganti instance)
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onPickInstance() }
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = instance.name,
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 34.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = HardTextShadow,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Ganti instance",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Ketuk nama untuk ganti instance",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                    Text(
                        text = if (isReady) "Tekan PLAY untuk menjalankan instance ini"
                        else "Tekan UNDUH untuk memasang instance ini",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }

                // Tombol PLAY besar
                NuxButton(
                    onClick = onPlay,
                    backgroundColor = NuxColors.Yellow,
                    contentColor = NuxColors.Ink,
                    cornerRadius = 36.dp,
                    shadowOffset = 5.dp,
                    contentPadding = PaddingValues(horizontal = 30.dp, vertical = 14.dp),
                    modifier = Modifier.padding(end = 5.dp, bottom = 5.dp)
                ) {
                    Icon(
                        imageVector = if (isReady) Icons.Default.PlayArrow else Icons.Default.Download,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isReady) "PLAY" else "UNDUH",
                        fontWeight = FontWeight.Black,
                        fontSize = 26.sp,
                        letterSpacing = 1.sp,
                        maxLines = 1
                    )
                }
            } else {
                // Belum ada instance
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Belum ada instance",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 30.sp,
                        maxLines = 1,
                        style = HardTextShadow
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Buat instance Minecraft pertamamu untuk mulai bermain.",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 12.sp,
                        maxLines = 2
                    )
                }

                NuxButton(
                    onClick = onCreateInstance,
                    backgroundColor = NuxColors.Yellow,
                    contentColor = NuxColors.Ink,
                    cornerRadius = 36.dp,
                    shadowOffset = 5.dp,
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp),
                    modifier = Modifier.padding(end = 5.dp, bottom = 5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "TAMBAH INSTANCE",
                        fontWeight = FontWeight.Black,
                        fontSize = 17.sp,
                        letterSpacing = 0.5.sp,
                        maxLines = 1
                    )
                }
            }
        }
    }
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
