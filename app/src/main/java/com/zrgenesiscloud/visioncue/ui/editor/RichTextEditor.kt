package com.zrgenesiscloud.visioncue.ui.editor

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatAlignLeft
import androidx.compose.material.icons.automirrored.filled.FormatAlignRight
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.zrgenesiscloud.visioncue.R
import com.zrgenesiscloud.visioncue.ui.theme.Custom

@Composable
fun RichTextEditor(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = TextStyle.Default,
    onTextAlignmentChange: (TextAlign) -> Unit = {},
    onFormattingsChange: (List<FormattingInfo>) -> Unit = {},
    initialAlignment: TextAlign = TextAlign.Left,
    initialFormattings: List<FormattingInfo> = emptyList(),
    onImportClick: () -> Unit = {}
) {
    var textFieldValueState by remember { mutableStateOf(value) }
    var textAlignment by remember { mutableStateOf(initialAlignment) }
    
    // Keep track of applied formatting to preserve them across edits
    val appliedFormattings = remember { mutableStateListOf<FormattingInfo>() }

    // Track if current selection has formatting
    var isBoldActive by remember { mutableStateOf(false) }
    var isItalicActive by remember { mutableStateOf(false) }
    
    // Update local state when the value changes from parent
    LaunchedEffect(value) {
        textFieldValueState = value
    }

    // Initialize with any existing formattings
    LaunchedEffect(initialFormattings, initialAlignment) {
        appliedFormattings.clear()
        appliedFormattings.addAll(initialFormattings)
        val formattedText = buildFormattedText(textFieldValueState.text, appliedFormattings)
        textFieldValueState = TextFieldValue(
            annotatedString = formattedText,
            selection = textFieldValueState.selection,
            composition = textFieldValueState.composition
        )
        onValueChange(textFieldValueState)
        textAlignment = initialAlignment
    }
    
    // Notify about formatting changes
    LaunchedEffect(appliedFormattings.toList()) {
        onFormattingsChange(appliedFormattings.toList())
    }
    
    // Check if current selection has formatting
    LaunchedEffect(textFieldValueState.selection) {
        isBoldActive = isFormattingApplied(textFieldValueState, TextFormatType.BOLD, appliedFormattings)
        isItalicActive = isFormattingApplied(textFieldValueState, TextFormatType.ITALIC, appliedFormattings)
    }

    Column(modifier = modifier.fillMaxWidth()) {
        EditorToolbar(
            onBoldClick = {
                if (!textFieldValueState.selection.collapsed) {
                    // Toggle bold formatting based on current state
                    if (isBoldActive) {
                        // Remove bold formatting from selection
                        removeFormatting(textFieldValueState.selection, TextFormatType.BOLD, appliedFormattings)
                    } else {
                        // Add new formatting
                        appliedFormattings.add(
                            FormattingInfo(
                                start = textFieldValueState.selection.start,
                                end = textFieldValueState.selection.end,
                                formatType = TextFormatType.BOLD
                            )
                        )
                    }
                    
                    val formattedText = buildFormattedText(textFieldValueState.text, appliedFormattings)
                    textFieldValueState = TextFieldValue(
                        annotatedString = formattedText,
                        selection = textFieldValueState.selection,
                        composition = textFieldValueState.composition
                    )
                    onValueChange(textFieldValueState)
                    isBoldActive = !isBoldActive
                }
            },
            isBoldActive = isBoldActive,
            onItalicClick = {
                if (!textFieldValueState.selection.collapsed) {
                    // Toggle italic formatting based on current state
                    if (isItalicActive) {
                        // Remove italic formatting from selection
                        removeFormatting(textFieldValueState.selection, TextFormatType.ITALIC, appliedFormattings)
                    } else {
                        // Add new formatting
                        appliedFormattings.add(
                            FormattingInfo(
                                start = textFieldValueState.selection.start,
                                end = textFieldValueState.selection.end,
                                formatType = TextFormatType.ITALIC
                            )
                        )
                    }
                    
                    val formattedText = buildFormattedText(textFieldValueState.text, appliedFormattings)
                    textFieldValueState = TextFieldValue(
                        annotatedString = formattedText,
                        selection = textFieldValueState.selection,
                        composition = textFieldValueState.composition
                    )
                    onValueChange(textFieldValueState)
                    isItalicActive = !isItalicActive
                }
            },
            isItalicActive = isItalicActive,
            onBulletListClick = {
                if (!textFieldValueState.selection.collapsed) {
                    val newState = applyFormatting(textFieldValueState, TextFormatType.BULLETS)
                    // Store this formatting operation
                    appliedFormattings.add(
                        FormattingInfo(
                            start = textFieldValueState.selection.start,
                            end = textFieldValueState.selection.end,
                            formatType = TextFormatType.BULLETS
                        )
                    )
                    val formattedText = buildFormattedText(newState.text, appliedFormattings)
                    textFieldValueState = TextFieldValue(
                        annotatedString = formattedText,
                        selection = newState.selection,
                        composition = newState.composition
                    )
                    onValueChange(textFieldValueState)
                }
            },
            onAlignLeftClick = {
                textAlignment = TextAlign.Start
                onTextAlignmentChange(TextAlign.Start)
            },
            onAlignCenterClick = {
                textAlignment = TextAlign.Center
                onTextAlignmentChange(TextAlign.Center)
            },
            onAlignRightClick = {
                textAlignment = TextAlign.End
                onTextAlignmentChange(TextAlign.End)
            },
            onImportClick = onImportClick
        )

        BasicTextField(
            value = textFieldValueState,
            onValueChange = { newValue ->
                if (newValue.text == textFieldValueState.text) {
                    isBoldActive = isFormattingApplied(textFieldValueState, TextFormatType.BOLD, appliedFormattings)
                    isItalicActive = isFormattingApplied(textFieldValueState, TextFormatType.ITALIC, appliedFormattings)
                    // Only the selection changed, preserve the annotated string
                    textFieldValueState = TextFieldValue(
                        annotatedString = textFieldValueState.annotatedString,
                        selection = newValue.selection,
                        composition = newValue.composition
                    )
                } else {
                    // Text content changed - we need to reapply all stored formatting
                    // First capture new text changes
                    val newText = newValue.text
                    val newSelection = newValue.selection
                    
                    // Now rebuild the annotated string with all saved formatting
                    val formattedText = buildFormattedText(newText, appliedFormattings)
                    
                    textFieldValueState = TextFieldValue(
                        annotatedString = formattedText,
                        selection = newSelection,
                        composition = newValue.composition
                    )
                }
                onValueChange(textFieldValueState)
            },
            modifier = Modifier.fillMaxWidth(),
            textStyle = textStyle.copy(textAlign = textAlignment),
            decorationBox = { innerTextField ->
                if (textFieldValueState.text.isEmpty()) {
                    Text(
                        text = stringResource(R.string.script_content_placeholder),
                        style = textStyle,
                        color = Color.Gray.copy(alpha = 0.6f)
                    )
                }
                innerTextField()
            }
        )
    }
}

// Function to check if a specific formatting is applied to the selection
fun isFormattingApplied(
    state: TextFieldValue,
    formatType: TextFormatType,
    formattings: List<FormattingInfo>
): Boolean {
    if (state.selection.collapsed) return false
    
    // Get all format info that overlaps with the current selection
    val overlappingFormats = formattings.filter { format ->
        format.formatType == formatType &&
        format.start < state.selection.end &&
        format.end > state.selection.start
    }
    
    // If there's any overlapping format, return true
    return overlappingFormats.isNotEmpty()
}

// Function to remove a specific formatting from the selection
fun removeFormatting(
    selection: TextRange,
    formatType: TextFormatType,
    formattings: MutableList<FormattingInfo>
) {
    // First identify all formats that overlap with the selection
    val overlappingFormats = formattings.filter { format ->
        format.formatType == formatType &&
        format.start < selection.end &&
        format.end > selection.start
    }.toMutableList()
    
    // Remove the overlapping formats
    formattings.removeAll(overlappingFormats)
    
    // For each overlapping format, we might need to add back partial formats
    for (format in overlappingFormats) {
        // If format starts before selection, keep the beginning part
        if (format.start < selection.start) {
            formattings.add(
                FormattingInfo(
                    start = format.start,
                    end = selection.start,
                    formatType = formatType
                )
            )
        }
        
        // If format ends after selection, keep the ending part
        if (format.end > selection.end) {
            formattings.add(
                FormattingInfo(
                    start = selection.end,
                    end = format.end,
                    formatType = formatType
                )
            )
        }
    }
}

@Composable
fun EditorToolbar(
    onBoldClick: () -> Unit,
    isBoldActive: Boolean = false,
    onItalicClick: () -> Unit,
    isItalicActive: Boolean = false,
    onBulletListClick: () -> Unit,
    onAlignLeftClick: () -> Unit,
    onAlignCenterClick: () -> Unit,
    onAlignRightClick: () -> Unit,
    onImportClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row {
            IconButton(onClick = onBulletListClick) {
                Icon(imageVector = Icons.AutoMirrored.Filled.List, contentDescription = "项目符号")
            }
            IconButton(onClick = onBoldClick) {
                Icon(
                    imageVector = Icons.Default.FormatBold, 
                    contentDescription = "粗体",
                    tint = if (isBoldActive) MaterialTheme.colorScheme.primary else LocalContentColor.current
                )
            }
            IconButton(onClick = onItalicClick) {
                Icon(
                    imageVector = Icons.Default.FormatItalic, 
                    contentDescription = "斜体",
                    tint = if (isItalicActive) MaterialTheme.colorScheme.primary else LocalContentColor.current
                )
            }
        }

        HorizontalDivider(
            modifier = Modifier
                .width(1.dp)
                .height(24.dp)
                .align(Alignment.CenterVertically),
            color = Color.LightGray
        )

        Row {
            IconButton(onClick = onAlignLeftClick) {
                Icon(imageVector = Icons.AutoMirrored.Filled.FormatAlignLeft, contentDescription = "左对齐")
            }
            IconButton(onClick = onAlignCenterClick) {
                Icon(imageVector = Icons.Default.FormatAlignCenter, contentDescription = "居中对齐")
            }
            IconButton(onClick = onAlignRightClick) {
                Icon(imageVector = Icons.AutoMirrored.Filled.FormatAlignRight, contentDescription = "右对齐")
            }
        }
//        TO BE IMPLEMENT: Import PDF, Word, Or Text file.
//        Divider(
//            modifier = Modifier
//                .width(1.dp)
//                .height(24.dp)
//                .align(Alignment.CenterVertically),
//            color = Color.LightGray
//        )
//
//        Row {
//            IconButton(onClick = onImportClick) {
//                Icon(imageVector = Icons.Custom.Upload, contentDescription = "导入", modifier = Modifier.size(20.dp))
//            }
//        }
    }
}

fun applyFormatting(
    state: TextFieldValue,
    formatType: TextFormatType
): TextFieldValue {
    Log.d("RichTextEditor", "Applying formatting: ${formatType}")
    Log.d("RichTextEditor", "Current selection: ${state.selection}")

    if (state.selection.collapsed) return state // No selection to format

    val newText = buildAnnotatedString {
        append(state.text.subSequence(0, state.selection.start))

        when (formatType) {
            TextFormatType.BOLD -> {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(state.text.subSequence(state.selection.start, state.selection.end))
                }
            }
            TextFormatType.ITALIC -> {
                withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                    append(state.text.subSequence(state.selection.start, state.selection.end))
                }
            }
            TextFormatType.BULLETS -> {
                // Handle bullet formatting if needed
                append(state.text.subSequence(state.selection.start, state.selection.end))
            }
            TextFormatType.ALIGN_LEFT,
            TextFormatType.ALIGN_CENTER,
            TextFormatType.ALIGN_RIGHT -> {
                // Alignment does not change the text, so we just append it
                append(state.text.subSequence(state.selection.start, state.selection.end))
            }
        }

        append(state.text.subSequence(state.selection.end, state.text.length))
    }

    return TextFieldValue(newText, state.selection)
}

// Function to rebuild text with all stored formatting
fun buildFormattedText(text: String, formattings: List<FormattingInfo>): AnnotatedString {
    return buildAnnotatedString {
        append(text)
        
        // Apply each formatting to the text
        for (format in formattings) {
            // Skip invalid ranges
            if (format.start < 0 || format.end > text.length || format.start >= format.end) {
                continue
            }
            
            // Apply the specific formatting type
            when (format.formatType) {
                TextFormatType.BOLD -> {
                    addStyle(SpanStyle(fontWeight = FontWeight.Bold), format.start, format.end)
                }
                TextFormatType.ITALIC -> {
                    addStyle(SpanStyle(fontStyle = FontStyle.Italic), format.start, format.end)
                }
                TextFormatType.BULLETS -> {
                    // Handle bullet formatting if implemented
                }
                else -> {
                    // Handle other formatting types
                }
            }
        }
    }
}