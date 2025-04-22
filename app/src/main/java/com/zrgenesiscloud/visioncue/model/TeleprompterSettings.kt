package com.zrgenesis.teleprompter.model

import kotlinx.serialization.Serializable

@Serializable
data class TeleprompterSettings(
    // 文本设置
    val fontSize: Float = 24f,
    val textColor: ULong = 0xFF1F2937UL, // 深色文本 (#1F2937)
    val lineSpacing: Float = 1.5f,
    
    // 显示设置
    val backgroundColor: ULong = 0xFFFFFFFFUL, // 白色 (#FFFFFF)
    val displayWidth: Float = 0.85f, // 显示宽度，屏幕宽度的百分比
    
    // 滚动设置
    val scrollSpeed: Float = 30f, // 每秒滚动的像素数
    val acceleration: Float = 1.0f,
    
    // 其他设置
    val isMirrorMode: Boolean = false,
    val isVerticalMirrorMode: Boolean = false,
    val isHighlightCurrentLine: Boolean = true
) 