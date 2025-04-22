package com.zrgenesiscloud.visioncue.ui.editor

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle

data class RichTextState(
    val text: AnnotatedString = AnnotatedString(""),
    val selection: TextRange = TextRange.Zero
) {
    companion object {
        fun from(plainText: String): RichTextState {
            return RichTextState(AnnotatedString(plainText))
        }
    }
    
    fun applyStyle(style: SpanStyle): RichTextState {
        if (selection.collapsed) return this
        
        val newText = buildAnnotatedString {
            append(text.subSequence(0, selection.min))
            
            withStyle(style) {
                append(text.subSequence(selection.min, selection.max))
            }
            
            append(text.subSequence(selection.max, text.length))
        }
        
        return copy(text = newText)
    }
    
    fun toPlainText(): String {
        return text.toString()
    }
    
    fun updateText(newText: TextFieldValue): RichTextState {
        return copy(text = newText.annotatedString)
    }

    fun updateSelection(newSelection: TextRange): RichTextState {
        return copy(selection = newSelection)
    }
}

enum class TextFormatType {
    BOLD,
    ITALIC,
    BULLETS,
    ALIGN_LEFT,
    ALIGN_CENTER,
    ALIGN_RIGHT
} 