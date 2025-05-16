package com.zrgenesiscloud.visioncue.ui.components
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.zrgenesiscloud.visioncue.model.TeleprompterSettings

@Composable
fun SettingsDialog(
    settings: TeleprompterSettings,
    onSettingsChanged: (TeleprompterSettings) -> Unit,
    onDismiss: () -> Unit
) {
    var currentSettings by remember { mutableStateOf(settings) }
    var showTextColorPicker by remember { mutableStateOf(false) }
    var showBackgroundColorPicker by remember { mutableStateOf(false) }
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "提词器设置",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Font size slider
                Text(
                    text = "字体大小: ${currentSettings.fontSize.toInt()}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Slider(
                    value = currentSettings.fontSize,
                    onValueChange = { 
                        currentSettings = currentSettings.copy(fontSize = it)
                    },
                    valueRange = 16f..48f,
                    steps = 32,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Text color selection
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "文本颜色",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(currentSettings.textColor.toLong()))
                            .border(2.dp, MaterialTheme.colorScheme.onSurface.copy(0.2f), CircleShape)
                            .clickable { showTextColorPicker = true }
                    )
                }
                
                // Background color selection
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "背景颜色",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(currentSettings.backgroundColor.toLong()))
                            .border(2.dp, MaterialTheme.colorScheme.onSurface.copy(0.2f), CircleShape)
                            .clickable { showBackgroundColorPicker = true }
                    )
                }
                
                // Line spacing slider
                Text(
                    text = "行间距: ${String.format("%.1f", currentSettings.lineSpacing)}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Slider(
                    value = currentSettings.lineSpacing,
                    onValueChange = { 
                        currentSettings = currentSettings.copy(lineSpacing = it)
                    },
                    valueRange = 1f..3f,
                    steps = 20,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Scroll speed slider with adjusted range and fewer steps for more noticeable changes
                Text(
                    text = "滚动速度: ${currentSettings.scrollSpeed.toInt()}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Slider(
                    value = currentSettings.scrollSpeed,
                    onValueChange = { 
                        currentSettings = currentSettings.copy(scrollSpeed = it)
                    },
                    // Narrower range with fewer steps makes each step more noticeable
                    valueRange = 5f..100f,
                    steps = 19, // 5-pixel increments
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Display width slider
                Text(
                    text = "显示宽度: ${String.format("%.0f", currentSettings.displayWidth * 100)}%",
                    style = MaterialTheme.typography.bodyMedium
                )
                Slider(
                    value = currentSettings.displayWidth,
                    onValueChange = { 
                        currentSettings = currentSettings.copy(displayWidth = it)
                    },
                    valueRange = 0.5f..1f,
                    steps = 10,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Toggle switches
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "水平镜像模式",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Switch(
                        checked = currentSettings.isMirrorMode,
                        onCheckedChange = {
                            currentSettings = currentSettings.copy(isMirrorMode = it)
                        }
                    )
                }
                
                // Vertical mirror mode toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "垂直镜像模式",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Switch(
                        checked = currentSettings.isVerticalMirrorMode,
                        onCheckedChange = {
                            currentSettings = currentSettings.copy(isVerticalMirrorMode = it)
                        }
                    )
                }
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "高亮当前行",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Switch(
                        checked = currentSettings.isHighlightCurrentLine,
                        onCheckedChange = {
                            currentSettings = currentSettings.copy(isHighlightCurrentLine = it)
                        }
                    )
                }
                
                // Preview box with mirroring effects and current colors
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .background(
                            Color(currentSettings.backgroundColor.toLong()),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "预览文本效果",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = currentSettings.fontSize.sp,
                            color = Color(currentSettings.textColor.toLong()),
                            lineHeight = (currentSettings.fontSize * currentSettings.lineSpacing).sp
                        ),
                        modifier = Modifier.graphicsLayer(
                            scaleX = if (currentSettings.isMirrorMode) -1f else 1f,
                            scaleY = if (currentSettings.isVerticalMirrorMode) -1f else 1f
                        )
                    )
                }
                
                // Action buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("取消")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { 
                            onSettingsChanged(currentSettings)
                            onDismiss()
                        }
                    ) {
                        Text("应用")
                    }
                }
            }
        }
    }
    
    // Show color picker dialogs when needed
    if (showTextColorPicker) {
        ColorPickerDialog(
            initialColor = currentSettings.textColor,
            onColorSelected = { color ->
                currentSettings = currentSettings.copy(textColor = color)
            },
            onDismiss = { showTextColorPicker = false }
        )
    }
    
    if (showBackgroundColorPicker) {
        ColorPickerDialog(
            initialColor = currentSettings.backgroundColor,
            onColorSelected = { color ->
                currentSettings = currentSettings.copy(backgroundColor = color)
            },
            onDismiss = { showBackgroundColorPicker = false }
        )
    }
}