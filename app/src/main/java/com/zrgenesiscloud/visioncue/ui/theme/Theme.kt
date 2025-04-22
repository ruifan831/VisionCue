package com.zrgenesiscloud.visioncue.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// 主色 - 专业蓝色系 (60-30-10规则中的30%)
private val Blue900 = Color(0xFF1E3A8A) // 深海蓝 - 主色调，来自设计规范
private val Blue800 = Color(0xFF1E40AF) // 湛蓝
private val Blue700 = Color(0xFF1D4ED8) // 标准蓝
private val Blue600 = Color(0xFF2563EB) // 明亮蓝
private val Blue500 = Color(0xFF3B82F6) // 鲜亮蓝
private val Blue400 = Color(0xFF60A5FA) // 中亮蓝
private val Blue300 = Color(0xFF93C5FD) // 天空蓝
private val Blue200 = Color(0xFFBFDBFE) // 浅蓝
private val Blue100 = Color(0xFFDCEDFF) // 极浅蓝，几乎白色

// 强调色/辅助色 - 琥珀金色调 (60-30-10规则中的10%)
private val AccentAmber500 = Color(0xFFF97316) // 琥珀金，来自设计规范
private val AccentAmber400 = Color(0xFFFB923C) // 浅琥珀金

// 功能色 - 用于特定UI状态
private val Green500 = Color(0xFF22C55E) // 确认绿，来自设计规范
private val Green400 = Color(0xFF34D399) // 浅绿色
private val Red500 = Color(0xFFEF4444) // 错误红
private val Red300 = Color(0xFFFCA5A5) // 浅红色
private val Teal500 = Color(0xFF14B8A6) // 青碧色
private val Teal400 = Color(0xFF2DD4BF) // 浅青碧色

// 中性色调 - 背景和表面 (60-30-10规则中的60%)
private val Gray900 = Color(0xFF111827) // 近黑色
private val Gray800 = Color(0xFF1F2937) // 深灰色，设计规范中的文本色
private val Gray700 = Color(0xFF374151) // 中深灰色
private val Gray600 = Color(0xFF4B5563) // 中灰色
private val Gray200 = Color(0xFFE5E7EB) // 浅灰色
private val Gray100 = Color(0xFFF3F4F6) // 近白色，设计规范中的背景色
private val Gray50 = Color(0xFFF9FAFB) // 精致的米白色

// 自定义颜色类，包含主题中未包含的额外颜色
object CustomColors {
    @Composable
    fun primaryButtonColor(): Color = if (isSystemInDarkTheme()) AccentAmber400 else Blue500
}

// 浅色主题 - 保持对比度>4.5:1确保WCAG AA级可访问性
private val LightColorScheme = lightColorScheme(
    // 主色
    primary = Blue900,           // 深蓝色主色调
    onPrimary = Color.White,     // 主色上的文字 - 高对比度
    primaryContainer = Blue100,  // 主色容器
    onPrimaryContainer = Blue900,// 容器上的文字 - 高对比度
    
    // 辅助色
    secondary = Teal500,         // 辅助UI元素
    onSecondary = Color.White,   // 辅助色上的文字
    secondaryContainer = Color(0xFFB2F5EA),
    onSecondaryContainer = Color(0xFF014D40),
    
    // 强调色
    tertiary = AccentAmber500,   // 强调、突出元素
    onTertiary = Color.White,    // 强调色上的文字
    tertiaryContainer = Color(0xFFFFECD5),
    onTertiaryContainer = Color(0xFF7A4100),
    
    // 错误色
    error = Red500,              // 错误状态
    onError = Color.White,       // 错误色上的文字
    errorContainer = Red300,     // 错误容器
    onErrorContainer = Color(0xFF7F1D1D), // 错误容器上的文字
    
    // 背景色
    background = Gray50,         // 应用背景色
    onBackground = Gray800,      // 背景上的主要文字
    
    // 表面色
    surface = Gray100,           // 卡片、对话框等表面
    onSurface = Gray800,         // 表面上的文字
    surfaceVariant = Color(0xFFDCEDFF), // 表面变体，与Blue100协调
    onSurfaceVariant = Gray600,  // 表面变体上的文字
    
    // 边框色
    outline = Color(0xFF64748B), // 边框
    outlineVariant = Color(0xFFC7D2FE), // 边框变体
    
    // 遮罩
    scrim = Color(0xFF000000)    // 遮罩层
)

// 深色主题 - 同样确保对比度满足WCAG标准
private val DarkColorScheme = darkColorScheme(
    // 主色 - 深色模式下使用更亮的蓝色提高可见性
    primary = Blue400,           // 更亮的蓝色作为主色
    onPrimary = Color.Black,     // 深色文字在亮色上更可读
    primaryContainer = Blue800,  // 深色容器
    onPrimaryContainer = Blue200,// 浅色文字在深色容器上
    
    // 辅助色
    secondary = Teal400,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF115E59),
    onSecondaryContainer = Color(0xFFA7F3D0),
    
    // 强调色
    tertiary = AccentAmber400,
    onTertiary = Color.Black,
    tertiaryContainer = Color(0xFF92400E),
    onTertiaryContainer = Color(0xFFFFECD5),
    
    // 错误色
    error = Color(0xFFF87171),   // 深色模式下使用更亮的红色
    onError = Color.Black,
    errorContainer = Color(0xFF8C1D18),
    onErrorContainer = Color(0xFFF2B8B5),
    
    // 背景色
    background = Gray900,        // 深色背景
    onBackground = Color.White,  // 白色文字在深色背景上
    
    // 表面色
    surface = Gray800,           // 深色表面
    onSurface = Color.White,     // 白色文字
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFFBFDBFE), // 使用Blue200保持一致性
    
    // 边框色
    outline = Color(0xFF94A3B8),
    outlineVariant = Color(0xFF334155),
    
    // 遮罩
    scrim = Color(0xFF000000)
)

@Composable
fun TeleprompterTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
} 