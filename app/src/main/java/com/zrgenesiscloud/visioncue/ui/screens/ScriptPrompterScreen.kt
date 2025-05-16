package com.zrgenesiscloud.visioncue.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zrgenesiscloud.visioncue.R
import com.zrgenesiscloud.visioncue.ui.components.SettingsDialog
import com.zrgenesiscloud.visioncue.ui.editor.FormattingInfo
import com.zrgenesiscloud.visioncue.model.TeleprompterSettings
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScriptPrompterScreen(
    title: String,
    content: TextFieldValue,
    textAlignment: TextAlign = TextAlign.Left,
    formattings: List<FormattingInfo> = emptyList(),
    settings: TeleprompterSettings,
    onClose: () -> Unit,
    onSettingsChanged: (TeleprompterSettings) -> Unit = {}
) {
    // Keep a local copy of settings that can be modified
    var currentSettings by remember { mutableStateOf(settings) }
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val localDensity = LocalDensity.current
    
    // Update local settings when prop changes
    LaunchedEffect(settings) {
        currentSettings = settings
    }
    
    // Safe color conversion from hexadecimal ULong to Compose Color
    val backgroundColor = remember(currentSettings.backgroundColor) { 
        Color(currentSettings.backgroundColor.toLong())
    }
    val textColor = remember(currentSettings.textColor) { 
        Color(currentSettings.textColor.toLong())
    }
    
    // State variables for controlling the prompter
    var isPlaying by remember { mutableStateOf(false) }
    var isControlsVisible by remember { mutableStateOf(true) }
    var showSettings by remember { mutableStateOf(false) }
    
    // Content positioning
    var contentHeight by remember { mutableStateOf(0) }
    var containerHeight by remember { mutableStateOf(0) }
    var isInitialPositionSet by remember { mutableStateOf(false) }
    
    // Create a unique key that changes whenever speed changes in settings
    val scrollKey = remember(isPlaying, currentSettings.scrollSpeed) { "${isPlaying}_${currentSettings.scrollSpeed}" }
    
    
    // Process content based on vertical mirror mode
    val displayContent = remember(content.text, currentSettings.isVerticalMirrorMode) {
        if (currentSettings.isVerticalMirrorMode) {
            // Reverse the lines for vertical mirroring
            val lines = content.text.split("\n")
            val reversedLines = lines.reversed().joinToString("\n")
            
            // Create a new TextFieldValue with reversed content
            TextFieldValue(
                text = reversedLines,
                selection = TextRange(0),
                composition = null
            )
        } else {
            content
        }
    }

    LaunchedEffect(currentSettings.isVerticalMirrorMode) {
        if (currentSettings.isVerticalMirrorMode) {
            scrollState.scrollTo(scrollState.maxValue)
        } else {
            scrollState.scrollTo(0)
        }
    }
    
    // Enhanced auto-scroll effect when playing
    LaunchedEffect(scrollKey) {
        if (isPlaying) {
            var lastTimestamp = System.currentTimeMillis()
            
            // Determine initial conditions based on mirroring
            if (currentSettings.isVerticalMirrorMode) {
                // For vertical mirroring, start at the bottom and scroll up (decreasing scroll value)
                if (scrollState.value == 0) {
                    scrollState.scrollTo(scrollState.maxValue)
                }
                
                // Loop for mirrored scrolling (decreasing values)
                while (isPlaying && scrollState.value > 0) {
                    val currentTime = System.currentTimeMillis()
                    val deltaTime = currentTime - lastTimestamp
                    lastTimestamp = currentTime
                    
                    // Enhanced scroll calculation with quadratic scaling
                    val speedFactor = currentSettings.scrollSpeed / 30f
                    val accelerationFactor = currentSettings.acceleration
                    
                    // Apply quadratic scaling to amplify differences and apply acceleration
                    val effectiveSpeed = (speedFactor * speedFactor) * accelerationFactor * 30f
                    
                    // For vertical mirroring, scroll downward (decrease value)
                    val scrollAmount = (effectiveSpeed * deltaTime / 1000f).toInt().coerceAtLeast(1)
                    scrollState.scrollTo((scrollState.value - scrollAmount).coerceAtLeast(0))
                    
                    // Check if we've reached the start
                    if (scrollState.value <= 0) {
                        isPlaying = false
                        break
                    }
                    
                    delay(16) // ~60fps
                }
            } else {
                // Normal scrolling (increasing values)
                while (isPlaying && scrollState.value < scrollState.maxValue) {
                    val currentTime = System.currentTimeMillis()
                    val deltaTime = currentTime - lastTimestamp
                    lastTimestamp = currentTime
                    
                    // Enhanced scroll calculation with quadratic scaling
                    val speedFactor = currentSettings.scrollSpeed / 30f
                    val accelerationFactor = currentSettings.acceleration
                    
                    // Apply quadratic scaling to amplify differences and apply acceleration
                    val effectiveSpeed = (speedFactor * speedFactor) * accelerationFactor * 30f
                    
                    // Calculate scroll amount with enhanced sensitivity
                    val scrollAmount = (effectiveSpeed * deltaTime / 1000f).toInt().coerceAtLeast(1)
                    scrollState.scrollTo((scrollState.value + scrollAmount).coerceAtMost(scrollState.maxValue))
                    
                    // Check if we've reached the end
                    if (scrollState.value >= scrollState.maxValue) {
                        isPlaying = false
                        break
                    }
                    
                    delay(16) // ~60fps
                }
            }
        }
    }
    
    // Hide controls after 3 seconds of inactivity
    LaunchedEffect(isControlsVisible) {
        if (isControlsVisible) {
            delay(3000)
            isControlsVisible = false
        }
    }
    
    // Main prompter layout
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        isControlsVisible = !isControlsVisible
                    }
                )
            }
            .onGloballyPositioned { coordinates ->
                containerHeight = coordinates.size.height
            }
    ) {
        // Script content with extra space before and after for proper scrolling
        Column(
            modifier = Modifier
                .fillMaxSize()
                // Calculate the horizontal padding based on the desired width percentage
                .padding(
                    horizontal = with(LocalDensity.current) {
                        (LocalConfiguration.current.screenWidthDp.dp * (1 - currentSettings.displayWidth) / 2)
                    }
                )
                .verticalScroll(scrollState)
                .onGloballyPositioned { coordinates ->
                    contentHeight = coordinates.size.height
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Add space at the top to allow content to be centered initially
            Spacer(modifier = Modifier.height(containerHeight.toDp(localDensity) / 2))
            
            // Script title
            if (!currentSettings.isVerticalMirrorMode) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = textColor,
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                        .graphicsLayer(
                            scaleX = if (currentSettings.isMirrorMode) -1f else 1f,
                            scaleY = if (currentSettings.isVerticalMirrorMode) -1f else 1f
                        )
                )
            }
            
            // Script content
            Text(
                text = displayContent.annotatedString,
                style = TextStyle(
                    fontSize = currentSettings.fontSize.sp,
                    color = textColor,
                    lineHeight = (currentSettings.fontSize * currentSettings.lineSpacing).sp,
                    textAlign = textAlignment
                ),
                modifier = Modifier.fillMaxWidth()
                    .graphicsLayer(
                        scaleX = if (currentSettings.isMirrorMode) -1f else 1f,
                        scaleY = if (currentSettings.isVerticalMirrorMode) -1f else 1f
                    )
            )

            if (currentSettings.isVerticalMirrorMode) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = textColor,
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                        .graphicsLayer(
                            scaleX = if (currentSettings.isMirrorMode) -1f else 1f,
                            scaleY = if (currentSettings.isVerticalMirrorMode) -1f else 1f
                        )
                )
            }
            
            // Add space at the bottom to allow content to be centered initially
            Spacer(modifier = Modifier.height(containerHeight.toDp(localDensity) / 2))
        }
        
        // Controls overlay
        AnimatedVisibility(
            visible = isControlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.7f))
                    .padding(16.dp)
            ) {
                // Progress indicator
                LinearProgressIndicator(
                    progress = {
                        if (scrollState.maxValue > 0) {
                            scrollState.value.toFloat() / scrollState.maxValue.toFloat()
                        } else {
                            0f
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                )
                
                // Playback controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Speed down
                    IconButton(onClick = {
                        val newSpeed = (currentSettings.scrollSpeed * 0.8f).coerceAtLeast(5f)
                        currentSettings = currentSettings.copy(scrollSpeed = newSpeed)
                        onSettingsChanged(currentSettings)
                    }) {
                        Icon(
                            imageVector = Icons.Default.SlowMotionVideo,
                            contentDescription = "减速",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    // Rewind
                    IconButton(onClick = {
                        coroutineScope.launch {
                            scrollState.scrollTo((scrollState.value - 500).coerceAtLeast(0))
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.FastRewind,
                            contentDescription = "后退",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    // Play/Pause
                    IconButton(
                        onClick = {
                            isPlaying = !isPlaying
                            // When starting playback, reset to appropriate position based on mirror mode
                            if (isPlaying && currentSettings.isVerticalMirrorMode && scrollState.value == 0) {
                                coroutineScope.launch {
                                    scrollState.scrollTo(scrollState.maxValue)
                                }
                            } else if (isPlaying && !currentSettings.isVerticalMirrorMode && scrollState.value == scrollState.maxValue) {
                                coroutineScope.launch {
                                    scrollState.scrollTo(0)
                                }
                            }
                        },
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) 
                                stringResource(id = R.string.pause) else stringResource(id = R.string.play),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    // Forward
                    IconButton(onClick = {
                        coroutineScope.launch {
                            scrollState.scrollTo((scrollState.value + 500).coerceAtMost(scrollState.maxValue))
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.FastForward,
                            contentDescription = stringResource(id = R.string.forward),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    // Speed up
                    IconButton(onClick = {
                        val newSpeed = (currentSettings.scrollSpeed * 1.2f).coerceAtMost(100f)
                        currentSettings = currentSettings.copy(scrollSpeed = newSpeed)
                        onSettingsChanged(currentSettings)
                    }) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = stringResource(id = R.string.speed_up),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    // Settings button
                    IconButton(onClick = { showSettings = true }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(id = R.string.settings),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
        
        // Close button
        AnimatedVisibility(
            visible = isControlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f), shape = MaterialTheme.shapes.small)
                    .padding(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(id = R.string.close),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        
        // Reset button
        AnimatedVisibility(
            visible = isControlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            IconButton(
                onClick = {
                    isPlaying = false
                    coroutineScope.launch {
                        if (currentSettings.isVerticalMirrorMode) {
                            // For vertical mirroring, the start position is at the bottom
                            scrollState.scrollTo(scrollState.maxValue)
                        } else {
                            // For normal view, the start position is at the top
                            scrollState.scrollTo(0)
                        }
                    }
                },
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f), shape = MaterialTheme.shapes.small)
                    .padding(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = stringResource(id = R.string.reset),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
    
    // Settings dialog
    if (showSettings) {
        SettingsDialog(
            settings = currentSettings,
            onSettingsChanged = { newSettings ->
                currentSettings = newSettings
                onSettingsChanged(newSettings)
            },
            onDismiss = { showSettings = false }
        )
    }
}

// Extension function to convert Int pixels to Dp
private fun Int.toDp(density: androidx.compose.ui.unit.Density) = with(density) { this@toDp.toDp() } 