package com.prodownload.manager

import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.prodownload.manager.data.*
import com.prodownload.manager.service.DownloadManager
import com.prodownload.manager.service.DownloadService
import com.prodownload.manager.ui.theme.ProDownloadManagerTheme
import com.prodownload.manager.utils.FileUtils
import com.prodownload.manager.utils.PermissionUtils
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : ComponentActivity() {
    private lateinit var downloadManager: DownloadManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        downloadManager = DownloadManager.getInstance(this)
        
        // Request permissions
        if (!PermissionUtils.hasStoragePermission(this) || !PermissionUtils.hasNotificationPermission(this)) {
            PermissionUtils.requestAllPermissions(this)
        }

        setContent {
            ProDownloadManagerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(downloadManager)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(downloadManager: DownloadManager) {
    var selectedTab by remember { mutableStateOf(0) }
    var showAddDialog by remember { mutableStateOf(false) }
    val tabs = listOf("الكل", "نشط", "مكتمل", "متوقف")
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("مدير التنزيلات الاحترافي") },
                actions = {
                    IconButton(onClick = { /* Statistics */ }) {
                        Icon(Icons.Filled.BarChart, "إحصائيات")
                    }
                    IconButton(onClick = { /* Settings */ }) {
                        Icon(Icons.Filled.Settings, "الإعدادات")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Filled.Add, "إضافة تنزيل", tint = Color.White)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            // Download List
            val downloads by when (selectedTab) {
                1 -> downloadManager.getDownloadsByStatus(DownloadStatus.DOWNLOADING)
                    .collectAsState(initial = emptyList())
                2 -> downloadManager.getDownloadsByStatus(DownloadStatus.COMPLETED)
                    .collectAsState(initial = emptyList())
                3 -> downloadManager.getDownloadsByStatus(DownloadStatus.PAUSED)
                    .collectAsState(initial = emptyList())
                else -> downloadManager.getAllDownloads().collectAsState(initial = emptyList())
            }

            DownloadList(
                downloads = downloads,
                downloadManager = downloadManager
            )
        }

        if (showAddDialog) {
            AddDownloadDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { url, fileName, threads ->
                    showAddDialog = false
                    // Add download logic will be here
                }
            )
        }
    }
}

@Composable
fun DownloadList(
    downloads: List<DownloadItem>,
    downloadManager: DownloadManager
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(downloads) { download ->
            DownloadItemCard(
                download = download,
                onPause = {
                    scope.launch {
                        DownloadService.pauseDownload(context, download.id)
                    }
                },
                onResume = {
                    scope.launch {
                        DownloadService.resumeDownload(context, download.id)
                    }
                },
                onDelete = {
                    scope.launch {
                        downloadManager.deleteDownload(download.id, deleteFile = true)
                    }
                }
            )
        }

        if (downloads.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "لا توجد تنزيلات",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun DownloadItemCard(
    download: DownloadItem,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // File name
            Text(
                text = download.fileName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Progress bar
            if (download.status == DownloadStatus.DOWNLOADING || download.status == DownloadStatus.PAUSED) {
                LinearProgressIndicator(
                    progress = download.progress / 100f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Progress info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${download.progress}%",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = download.formatDownloaded() + " / " + download.formatSize(),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                if (download.status == DownloadStatus.DOWNLOADING) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "السرعة: ${download.formatSpeed()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "المتبقي: ${download.formatETA()}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Status and actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status chip
                StatusChip(download.status)

                // Action buttons
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    when (download.status) {
                        DownloadStatus.DOWNLOADING -> {
                            IconButton(onClick = onPause) {
                                Icon(
                                    Icons.Filled.Pause,
                                    "إيقاف",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        DownloadStatus.PAUSED, DownloadStatus.QUEUED -> {
                            IconButton(onClick = onResume) {
                                Icon(
                                    Icons.Filled.PlayArrow,
                                    "استئناف",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        DownloadStatus.FAILED -> {
                            IconButton(onClick = onResume) {
                                Icon(
                                    Icons.Filled.Refresh,
                                    "إعادة",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        else -> {}
                    }

                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Filled.Delete,
                            "حذف",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatusChip(status: DownloadStatus) {
    val (text, color) = when (status) {
        DownloadStatus.DOWNLOADING -> "جاري التنزيل" to Color(0xFF2196F3)
        DownloadStatus.COMPLETED -> "مكتمل" to Color(0xFF4CAF50)
        DownloadStatus.PAUSED -> "متوقف" to Color(0xFFFF9800)
        DownloadStatus.FAILED -> "فشل" to Color(0xFFF44336)
        DownloadStatus.QUEUED -> "في الانتظار" to Color(0xFF9E9E9E)
        DownloadStatus.CANCELLED -> "ملغي" to Color(0xFF757575)
    }

    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDownloadDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, Int) -> Unit
) {
    var url by remember { mutableStateOf("") }
    var fileName by remember { mutableStateOf("") }
    var threads by remember { mutableStateOf(4f) }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إضافة تنزيل جديد") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("رابط التنزيل") },
                    placeholder = { Text("https://example.com/file.zip") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = fileName,
                    onValueChange = { fileName = it },
                    label = { Text("اسم الملف (اختياري)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Column {
                    Text("عدد الأجزاء: ${threads.toInt()}")
                    Slider(
                        value = threads,
                        onValueChange = { threads = it },
                        valueRange = 1f..16f,
                        steps = 14
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (url.isNotBlank()) {
                        val finalFileName = fileName.ifBlank {
                            FileUtils.getFileNameFromUrl(url)
                        }
                        
                        // Start download
                        val downloadManager = DownloadManager.getInstance(context)
                        val downloadPath = Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_DOWNLOADS
                        ).absolutePath
                        
                        kotlinx.coroutines.MainScope().launch {
                            try {
                                val downloadId = downloadManager.addDownload(
                                    url = url,
                                    fileName = finalFileName,
                                    filePath = downloadPath,
                                    numberOfThreads = threads.toInt()
                                )
                                DownloadService.startDownload(context, downloadId)
                                Toast.makeText(context, "تم إضافة التنزيل", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Toast.makeText(context, "خطأ: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                        
                        onConfirm(url, finalFileName, threads.toInt())
                    } else {
                        Toast.makeText(context, "يرجى إدخال رابط صحيح", Toast.LENGTH_SHORT).show()
                    }
                }
            ) {
                Text("إضافة")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}
