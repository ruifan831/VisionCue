package com.zrgenesiscloud.visioncue.ui.screens

import android.app.Activity
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.zrgenesiscloud.visioncue.R
import com.zrgenesiscloud.visioncue.ad.TTAdSdkManager
import com.zrgenesiscloud.visioncue.util.AdUtils
import kotlinx.coroutines.delay

@Composable
fun WelcomeScreen(
    onStartClick: () -> Unit,
    onLearnMoreClick: () -> Unit
) {
    val context = LocalContext.current
    var showingAd by remember { mutableStateOf(true) }
    
    LaunchedEffect(Unit) {
        // Load interstitial ad when screen is first composed
        TTAdSdkManager.ensureInitializedAsync(context).thenAccept{ success-> 
            AdUtils.loadInterstitialFullAd(
                activity = context as Activity,
                onAdClosed = { showingAd = false }
            )
        }
        
        // Fallback timer in case ad doesn't load or callback fails
        delay(5000) // 5 seconds timeout
        showingAd = false
    }
    
    if (!showingAd) {
        // Original WelcomeScreen content - show this after ad is closed or fails to load
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo image
            Image(
                painter = painterResource(id = R.drawable.app_logo),
                contentDescription = stringResource(id = R.string.welcome_title),
                modifier = Modifier
                    .size(120.dp)
                    .padding(bottom = 24.dp)
            )
            
            // App name
            Text(
                text = stringResource(id = R.string.welcome_title),
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.primary
            )
            
            // Description text
            Text(
                text = stringResource(id = R.string.welcome_description),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 24.dp)
            )
            
            // Button group
            Button(
                onClick = onStartClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Text(stringResource(id = R.string.start_using))
            }
            
//            OutlinedButton(
//                onClick = onLearnMoreClick,
//                modifier = Modifier.fillMaxWidth()
//            ) {
//                Text(stringResource(id = R.string.learn_more))
//            }
        }
    }
} 