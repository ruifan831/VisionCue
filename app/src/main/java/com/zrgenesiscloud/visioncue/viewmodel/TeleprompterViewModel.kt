package com.zrgenesiscloud.visioncue.viewmodel

import androidx.lifecycle.ViewModel
import com.zrgenesis.teleprompter.model.TeleprompterSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class TeleprompterViewModel : ViewModel() {
    private val _settings = MutableStateFlow(TeleprompterSettings())
    val settings: StateFlow<TeleprompterSettings> = _settings.asStateFlow()
    
    fun updateSettings(newSettings: TeleprompterSettings) {
        _settings.update { newSettings }
    }
    
    fun updateFontSize(fontSize: Float) {
        _settings.update { it.copy(fontSize = fontSize) }
    }
    
    fun updateTextColor(color: ULong) {
        _settings.update { it.copy(textColor = color) }
    }
    
    fun updateBackgroundColor(color: ULong) {
        _settings.update { it.copy(backgroundColor = color) }
    }
    
    fun updateScrollSpeed(speed: Float) {
        _settings.update { it.copy(scrollSpeed = speed) }
    }
    
    fun updateLineSpacing(spacing: Float) {
        _settings.update { it.copy(lineSpacing = spacing) }
    }
    
    fun updateDisplayWidth(width: Float) {
        _settings.update { it.copy(displayWidth = width) }
    }
    
    fun toggleMirrorMode() {
        _settings.update { it.copy(isMirrorMode = !it.isMirrorMode) }
    }
    
    fun toggleHighlightCurrentLine() {
        _settings.update { it.copy(isHighlightCurrentLine = !it.isHighlightCurrentLine) }
    }
    
    fun toggleVerticalMirrorMode() {
        _settings.update { it.copy(isVerticalMirrorMode = !it.isVerticalMirrorMode) }
    }
} 