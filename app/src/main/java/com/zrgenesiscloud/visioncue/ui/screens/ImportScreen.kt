package com.zrgenesiscloud.visioncue.ui.screens

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zrgenesiscloud.visioncue.ui.theme.CustomColors
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*

enum class ImportFileType {
    PDF, WORD, TXT
}

data class RecentImportFile(
    val id: String,
    val name: String,
    val size: String,
    val date: String,
    val type: ImportFileType
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportScreen(
    onBackClick: () -> Unit,
    onImportText: (String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    // Initialize PDF library (should be done once in your app's initialization)
    LaunchedEffect(Unit) {
        PDFBoxResourceLoader.init(context)
    }
    
    // State for the selected file type tab
    var selectedFileType by remember { mutableStateOf(ImportFileType.PDF) }
    var showDirectTextInput by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Dummy recent files for demonstration
    val recentFiles = remember {
        listOf(
            RecentImportFile(
                id = "1",
                name = "演讲稿.pdf",
                size = "2MB",
                date = "昨天",
                type = ImportFileType.PDF
            ),
            RecentImportFile(
                id = "2",
                name = "会议提纲.docx",
                size = "1.5MB",
                date = "3天前",
                type = ImportFileType.WORD
            )
        )
    }
    
    // Function to extract text based on file type
    suspend fun extractTextFromUri(uri: Uri): String = withContext(Dispatchers.IO) {
        try {
            val contentResolver = context.contentResolver
            val mimeType = contentResolver.getType(uri)
            
            when {
                mimeType?.contains("pdf", ignoreCase = true) == true -> {
                    // Handle PDF files
                    contentResolver.openInputStream(uri)?.use { inputStream ->
                        val document = PDDocument.load(inputStream)
                        val stripper = PDFTextStripper()
                        val text = stripper.getText(document)
                        document.close()
                        return@withContext text
                    }
                }
                mimeType?.contains("word", ignoreCase = true) == true || 
                mimeType?.contains("docx", ignoreCase = true) == true -> {
                    // Handle Word files - requires additional dependency
//                    contentResolver.openInputStream(uri)?.use { inputStream ->
//                        val document = XWPFDocument(inputStream)
//                        val extractor = XWPFWordExtractor(document)
//                        val text = extractor.text
//                        extractor.close()
//                        return@withContext text
//                    }
                    return@withContext "asasfa"
                }
                else -> {
                    // Handle plain text and other file types
                    contentResolver.openInputStream(uri)?.use { inputStream ->
                        return@withContext BufferedReader(InputStreamReader(inputStream)).use { it.readText() }
                    }
                }
            }
            
            throw Exception("Could not read file content")
        } catch (e: Exception) {
            throw Exception("Error reading file: ${e.message}")
        }
    }
    
    // Handle system back button
    BackHandler(onBack = onBackClick)
    
    // A more elegant solution using arrays of MIME types
    val getMimeTypesForFileType: (ImportFileType) -> Array<String> = { fileType ->
        when (fileType) {
            ImportFileType.PDF -> arrayOf("application/pdf")
            ImportFileType.WORD -> arrayOf(
                "application/msword",  // .doc
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document", // .docx
                "application/vnd.ms-word.document.macroenabled.12"  // .docm
            )
            ImportFileType.TXT -> arrayOf("text/plain")
        }
    }

    // File picker launcher
    val pickFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            isLoading = true
            errorMessage = null
            
            coroutineScope.launch {
                try {
                    val content = extractTextFromUri(uri)
                    Log.d("ImportScreen", "Content: ${content}")
                    // Import the content
                    // onImportText(content)
                    // Go back to the editor
                    onBackClick()
                } catch (e: Exception) {
                    errorMessage = e.message
                    Toast.makeText(context, "导入失败: ${e.message}", Toast.LENGTH_LONG).show()
                } finally {
                    isLoading = false
                }
            }
        }
        // No need to do anything if uri is null - user cancelled the picker
    }

    // Function to launch file picker with appropriate mime type
    fun launchFilePicker(mimeType: String) {
        pickFileLauncher.launch(getMimeTypesForFileType(selectedFileType))
        Toast.makeText(context, "选择文件后，可以按返回键取消", Toast.LENGTH_SHORT).show()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("导入文件") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // File type tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF5F5F5))
                    .padding(horizontal = 8.dp)
            ) {
                ImportTypeTab(
                    title = "PDF",
                    isSelected = selectedFileType == ImportFileType.PDF,
                    onClick = { selectedFileType = ImportFileType.PDF }
                )
                ImportTypeTab(
                    title = "Word",
                    isSelected = selectedFileType == ImportFileType.WORD,
                    onClick = { selectedFileType = ImportFileType.WORD }
                )
                ImportTypeTab(
                    title = "TXT",
                    isSelected = selectedFileType == ImportFileType.TXT,
                    onClick = { selectedFileType = ImportFileType.TXT }
                )
            }
            
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                    Text(
                        text = "正在提取文件内容...",
                        modifier = Modifier.padding(top = 48.dp)
                    )
                }
            } else {
                // Upload area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .border(
                            width = 2.dp,
                            color = Color(0xFFBBDEFB),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .background(Color(0xFFE3F2FD), RoundedCornerShape(8.dp))
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            val mimeType = when (selectedFileType) {
                                ImportFileType.PDF -> "application/pdf"
                                ImportFileType.WORD -> "*/*"  // Replace with this for now
                                ImportFileType.TXT -> "text/plain"
                            }
                            launchFilePicker(mimeType)
                        }
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Upload,
                            contentDescription = "上传",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "点击此处选择文件",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "或者",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                val mimeType = when (selectedFileType) {
                                    ImportFileType.PDF -> "application/pdf"
                                    ImportFileType.WORD -> "*/*"  // Replace with this for now
                                    ImportFileType.TXT -> "text/plain"
                                }
                                launchFilePicker(mimeType)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("浏览文件")
                        }
                    }
                }
                
                
                if (errorMessage != null) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        color = Color(0xFFFFEBEE),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "导入错误: $errorMessage",
                            color = Color.Red,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
            
            // Recent imports section
            Text(
                text = "最近导入的文件",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp)
            )
            
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                items(recentFiles) { file ->
                    RecentFileItem(file = file) {
                        // Simulate selecting a recent file
                        isLoading = true
                        coroutineScope.launch {
                            // Simulate file loading
                            delay(1000)
                            isLoading = false
                            // Sample content for demonstration
                            val sampleContent = when (file.type) {
                                ImportFileType.PDF -> "这是从PDF文件中提取的示例内容。\n\n这是第二段落。"
                                ImportFileType.WORD -> "这是从Word文档中提取的示例内容。\n\n• 项目一\n• 项目二"
                                ImportFileType.TXT -> "这是纯文本文件的内容示例。"
                            }
                            onImportText(sampleContent)
                            onBackClick()
                        }
                    }
                }
            }
            
            // Cancel button at bottom
            Button(
                onClick = onBackClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Gray
                )
            ) {
                Text("取消导入")
            }
        }
    }
    
}

@Composable
fun ImportTypeTab(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
            modifier = Modifier.padding(vertical = 12.dp)
        )
        if (isSelected) {
            Box(
                modifier = Modifier
                    .height(2.dp)
                    .width(40.dp)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}

@Composable
fun RecentFileItem(
    file: RecentImportFile,
    onSelectFile: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        when (file.type) {
                            ImportFileType.PDF -> Color(0xFFFFEBEE)
                            ImportFileType.WORD -> Color(0xFFE3F2FD)
                            ImportFileType.TXT -> Color(0xFFF1F8E9)
                        },
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = "文件",
                    tint = when (file.type) {
                        ImportFileType.PDF -> Color(0xFFE53935)
                        ImportFileType.WORD -> Color(0xFF1E88E5)
                        ImportFileType.TXT -> Color(0xFF43A047)
                    }
                )
            }
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    text = file.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${file.size} · ${file.date}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            
            Button(
                onClick = onSelectFile,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE3F2FD),
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("选择此文件")
            }
        }
    }
} 