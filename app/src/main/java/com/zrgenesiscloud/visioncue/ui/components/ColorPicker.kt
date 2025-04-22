package com.zrgenesiscloud.visioncue.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlin.math.roundToInt

@Composable
fun ColorPickerDialog(
    initialColor: ULong,
    onColorSelected: (ULong) -> Unit,
    onDismiss: () -> Unit
) {
    val initialColorCompose = Color(initialColor.toLong())
    var currentColor by remember { mutableStateOf(initialColorCompose) }
    val scrollState = rememberScrollState()

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .heightIn(max = 500.dp) // Set maximum height to prevent oversized dialog
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                // Title and scrollable content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(scrollState)
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "选择颜色",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    // Color preview
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(currentColor)
                            .border(2.dp, MaterialTheme.colorScheme.onSurface.copy(0.2f), CircleShape)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // HSV Color Picker
                    HsvColorPicker(
                        initialColor = initialColorCompose,
                        onColorChanged = { color -> 
                            currentColor = color
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Hex code of selected color
                    Text(
                        text = "#${Integer.toHexString(currentColor.toArgb()).uppercase().padStart(8, '0').substring(2)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                // Action buttons - keep outside of scroll area to always be visible
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = { onDismiss() },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("取消")
                    }
                    
                    Button(onClick = { 
                        // Convert the Color to ULong for the settings format
                        val colorULong = currentColor.toArgb().toULong() and 0xFFFFFFFFUL
                        onColorSelected(colorULong)
                        onDismiss()
                    }) {
                        Text("确定")
                    }
                }
            }
        }
    }
}

@Composable
fun HsvColorPicker(
    initialColor: Color,
    onColorChanged: (Color) -> Unit,
    modifier: Modifier = Modifier
) {
    var hue by remember { mutableStateOf(0f) }
    var satVal by remember { mutableStateOf(Offset(1f, 1f)) } // x = saturation, y = value
    var size by remember { mutableStateOf(IntSize(0, 0)) }
    
    // Extract HSV values from initial color and set them
    LaunchedEffect(initialColor) {
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(initialColor.toArgb(), hsv)
        hue = hsv[0]
        satVal = Offset(hsv[1], hsv[2])
    }
    
    Column(modifier = modifier) {
        // Hue selection bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Red, Color.Yellow, Color.Green,
                            Color.Cyan, Color.Blue, Color.Magenta, Color.Red
                        )
                    )
                )
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        hue = (offset.x / size.width) * 360f
                        
                        val hsv = floatArrayOf(hue, satVal.x, satVal.y)
                        val newColor = Color(android.graphics.Color.HSVToColor(hsv))
                        onColorChanged(newColor)
                    }
                }
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        hue = (change.position.x.coerceIn(0f, size.width.toFloat()) / size.width) * 360f
                        
                        val hsv = floatArrayOf(hue, satVal.x, satVal.y)
                        val newColor = Color(android.graphics.Color.HSVToColor(hsv))
                        onColorChanged(newColor)
                    }
                }
                .onSizeChanged { size = it }
        ) {
            // Hue selector indicator
            Box(
                modifier = Modifier
                    .offset(x = (hue / 360f * size.width).dp, y = 0.dp)
                    .size(4.dp, 30.dp)
                    .background(Color.White)
                    .border(1.dp, Color.Black)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Saturation/Value selection box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(4.dp))
                .onSizeChanged { size = it }
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        satVal = Offset(
                            offset.x / size.width,
                            offset.y / size.height
                        )
                        
                        val hsv = floatArrayOf(hue, satVal.x, 1f - satVal.y)
                        val newColor = Color(android.graphics.Color.HSVToColor(hsv))
                        onColorChanged(newColor)
                    }
                }
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        satVal = Offset(
                            change.position.x.coerceIn(0f, size.width.toFloat()) / size.width,
                            change.position.y.coerceIn(0f, size.height.toFloat()) / size.height
                        )
                        
                        val hsv = floatArrayOf(hue, satVal.x, 1f - satVal.y)
                        val newColor = Color(android.graphics.Color.HSVToColor(hsv))
                        onColorChanged(newColor)
                    }
                }
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                // Draw the base color with full saturation/value
                val hsv = floatArrayOf(hue, 1f, 1f)
                val baseColor = android.graphics.Color.HSVToColor(hsv)
                
                // Draw saturation gradient (white to base color)
                val saturationGradient = Brush.horizontalGradient(
                    colors = listOf(Color.White, Color(baseColor))
                )
                
                // Draw value gradient (transparent to black)
                val valueGradient = Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color.Black)
                )
                
                // Apply gradients
                drawRect(brush = saturationGradient)
                drawRect(brush = valueGradient)
                
                // Draw selector circle
                drawCircle(
                    color = Color.White,
                    radius = 8f,
                    center = Offset(
                        satVal.x * size.width,
                        satVal.y * size.height
                    ),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
                )
            }
        }
    }
} 