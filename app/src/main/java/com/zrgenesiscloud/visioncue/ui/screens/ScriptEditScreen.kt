package com.zrgenesiscloud.visioncue.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.zrgenesiscloud.visioncue.R
import com.zrgenesiscloud.visioncue.ui.components.PrimaryButton
import com.zrgenesiscloud.visioncue.ui.editor.FormattingInfo
import com.zrgenesiscloud.visioncue.ui.editor.RichTextEditor
import com.zrgenesiscloud.visioncue.ui.theme.CustomColors
import com.zrgenesiscloud.visioncue.model.Script
import com.zrgenesiscloud.visioncue.model.TeleprompterSettings
import com.zrgenesiscloud.visioncue.repository.ScriptRepository
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private const val TAG = "ScriptEditScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScriptEditScreen(
    scriptId: String?,
    scriptRepository: ScriptRepository,
    onBackClick: () -> Unit,
    onStartPrompting: () -> Unit,
    onSaveScript: (Script) -> Unit
) {
    Log.d(TAG, "ScriptEditScreen called with scriptId: $scriptId")
    var title by remember { mutableStateOf("") }
    var richTextState by remember { 
        mutableStateOf(TextFieldValue(AnnotatedString("")))
    }
    var textAlignment by remember { mutableStateOf(TextAlign.Left) }
    var appliedFormattings by remember { mutableStateOf(listOf<FormattingInfo>()) }
    var isPrompting by remember { mutableStateOf(false) }
    var showImportScreen by remember { mutableStateOf(false) }
    val defaultSettings = remember { TeleprompterSettings() }
    
    val coroutineScope = rememberCoroutineScope()
    
    // If scriptId is not null and not "new", load the existing script
    LaunchedEffect(scriptId) {
        Log.d(TAG, "LaunchedEffect triggered with scriptId: $scriptId")
        if (scriptId == "new") {
            Log.d(TAG, "Creating new script")
            title = ""
            richTextState = TextFieldValue(AnnotatedString("欢迎使用ProPrompter提词器应用！\n\n这是一个示例脚本，您可以随意编辑。\n\n• 点击工具栏按钮可以应用不同的格式\n• 您可以创建多个段落和列表\n• 导入文件按钮允许您从其他文档导入内容\n\n祝您使用愉快！"))
            textAlignment = TextAlign.Left
            appliedFormattings = emptyList()
        } else if (scriptId != null) {
            // Load existing script from repository
            Log.d(TAG, "Loading existing script with ID: $scriptId")
            coroutineScope.launch {
                val script = scriptRepository.getScript(scriptId)
                Log.d(TAG, "Script loaded: ${script != null}")
                if (script != null) {
                    Log.d(TAG, "Script title: ${script.title}")
                    Log.d(TAG, "Script content length: ${script.content.length}")
                    Log.d(TAG, "Formatting data: ${script.formattingData}")
                    
                    title = script.title
                    
                    // Restore formatting
                    textAlignment = getTextAlignFromString(script.textAlignment)
                    appliedFormattings = if (script.formattingData.isNotEmpty()) {
                        try {
                            Log.d(TAG, "Parsing formatting data: ${script.formattingData}")
                            Json.decodeFromString<List<FormattingInfo>>(script.formattingData)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing formatting data", e)
                            emptyList()
                        }
                    } else {
                        emptyList()
                    }
                    
                    // Set the text AFTER setting the formatting info
                    richTextState = TextFieldValue(AnnotatedString(script.content))
                    
                    Log.d(TAG, "Script loaded successfully, alignment: $textAlignment, formattings: ${appliedFormattings.size}")
                } else {
                    Log.e(TAG, "Failed to load script with ID: $scriptId")
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(stringResource(id = R.string.edit_script), color = Color.White)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            // Serialize the formatting information and text alignment
                            val formattingDataJson = serializeFormattings(appliedFormattings)
                            val alignmentString = textAlignment.toAlignmentString()

                            val script = Script(
                                id = if (scriptId!=null && scriptId != "new") scriptId else generateUUID(),
                                title = title.ifEmpty { "未命名脚本" },
                                content = richTextState.text.toString(),
                                formattingData = formattingDataJson,
                                textAlignment = alignmentString,
                                updatedAt = Clock.System.now()
                            )
                            
                            // Save script to repository
                            coroutineScope.launch {
                                scriptRepository.saveScript(script)
                                onSaveScript(script)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CustomColors.primaryButtonColor()
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(stringResource(id = R.string.save), color = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Title input
            TextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                placeholder = { Text(stringResource(id = R.string.script_title_placeholder)) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.LightGray,
                ),
                singleLine = true
            )

            // Rich text editor
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp,0.dp,16.dp,16.dp)
                    .weight(1f)
            ) {
                Log.d("ScriptEditScreen", "RichTextEditor called with textAlignment: ${textAlignment}, appliedFormattings: ${appliedFormattings}")
                RichTextEditor(
                    value = richTextState,
                    onValueChange = { newText ->
                        richTextState = newText
                    },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodyLarge,
                    onTextAlignmentChange = { alignment ->
                        textAlignment = alignment
                    },
                    onFormattingsChange = { formattings ->
                        appliedFormattings = formattings
                    },
                    initialAlignment = textAlignment,
                    initialFormattings = appliedFormattings,
                    onImportClick = {
                        showImportScreen = true
                    }
                )
            }

            // Start prompting button
            PrimaryButton(
                text = stringResource(id = R.string.start_prompting),
                onClick = { isPrompting = true },
                leadingIcon = Icons.Default.PlayArrow
            )
        }
    }

    if (isPrompting) {
        ScriptPrompterScreen(
            title = title,
            content = richTextState,
            settings = defaultSettings,
            textAlignment = textAlignment,
            onClose = { isPrompting = false }
        )
    }
    
    if (showImportScreen) {
        ImportScreen(
            onBackClick = { showImportScreen = false },
            onImportText = { importedText ->
                // Insert the imported text at the current cursor position
                val currentPos = richTextState.selection.start
                val beforeText = richTextState.text.take(currentPos)
                val afterText = richTextState.text.drop(currentPos)
                val newText = beforeText + importedText + afterText
                
                // Update the text field with the imported content
                val newAnnotatedString = AnnotatedString(newText)
                richTextState = TextFieldValue(
                    annotatedString = newAnnotatedString,
                    selection = TextRange(currentPos + importedText.length)
                )
                
                showImportScreen = false
            }
        )
    }
}

// Helper function to generate UUID for Android
fun generateUUID(): String {
    return java.util.UUID.randomUUID().toString()
}

// Helper functions for text alignment and formatting
private fun TextAlign.toAlignmentString(): String {
    return when (this) {
        TextAlign.Left, TextAlign.Start -> "Left"
        TextAlign.Center -> "Center"
        TextAlign.Right, TextAlign.End -> "Right"
        else -> "Left"
    }
}

private fun serializeFormattings(formattings: List<FormattingInfo>): String {
    return try {
        Json.encodeToString(formattings)
    } catch (e: Exception) {
        ""
    }
}

private fun getTextAlignFromString(alignString: String): TextAlign {
    return when (alignString) {
        "Center" -> TextAlign.Center
        "Right" -> TextAlign.End
        else -> TextAlign.Start
    }
} 