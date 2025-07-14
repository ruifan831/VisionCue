package com.zrgenesiscloud.visioncue

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.zrgenesiscloud.visioncue.repository.AndroidScriptRepository
import com.zrgenesiscloud.visioncue.ui.screens.ScriptEditScreen
import com.zrgenesiscloud.visioncue.ui.screens.ScriptListScreen
import com.zrgenesiscloud.visioncue.ui.screens.WelcomeScreen
import com.zrgenesiscloud.visioncue.ui.screens.SettingsScreen
import com.zrgenesiscloud.visioncue.ui.theme.TeleprompterTheme
import com.zrgenesiscloud.visioncue.repository.ScriptRepository
import com.zrgenesiscloud.visioncue.util.LocaleManager
import com.zrgenesiscloud.visioncue.util.PrivacyPolicyManager
import com.zrgenesiscloud.visioncue.ui.components.PrivacyPolicyDialog
import android.content.Context
import android.app.Activity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import java.util.Locale
import com.bytedance.sdk.openadsdk.TTAdConfig
import com.bytedance.sdk.openadsdk.TTAdSdk
import com.bytedance.sdk.openadsdk.TTCustomController
import com.bytedance.sdk.openadsdk.mediation.init.MediationPrivacyConfig
import com.bytedance.sdk.openadsdk.AdSlot
import com.bytedance.sdk.openadsdk.CSJAdError
import com.bytedance.sdk.openadsdk.CSJSplashAd
import com.bytedance.sdk.openadsdk.TTAdNative
import kotlin.system.exitProcess
import com.zrgenesiscloud.visioncue.util.BuildConfigTest
import com.zrgenesiscloud.visioncue.manager.AdManager

class MainActivity : ComponentActivity() {
    private lateinit var privacyPolicyManager: PrivacyPolicyManager
    private lateinit var adManager: AdManager
    
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(updateLocale(newBase, LocaleManager(newBase).getLocale()))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Log build configuration for debugging
        BuildConfigTest.logBuildConfiguration()
        
        // Initialize managers
        privacyPolicyManager = PrivacyPolicyManager(applicationContext)

        // Create the repositories
        val scriptRepository = AndroidScriptRepository(applicationContext)

        setContent {
            TeleprompterTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Check if privacy policy has been accepted
                    var showPrivacyDialog by remember { mutableStateOf(!privacyPolicyManager.hasAcceptedPrivacyPolicy()) }
                    
                    if (showPrivacyDialog) {
                        // Show privacy policy dialog
                        PrivacyPolicyDialog(
                            onAccept = {
                                // Save acceptance
                                privacyPolicyManager.setPrivacyPolicyAccepted(true)
                                showPrivacyDialog = false
                            },
                            onDecline = {
                                // Exit the app
                                finish()
                                exitProcess(0)
                            }
                        )
                    } else {
                        
                        // Continue with the main app
                        TeleprompterApp(scriptRepository)
                    }
                }
            }
        }
    }

    

    

    private fun updateLocale(context: Context, locale: Locale): Context {
        Locale.setDefault(locale)
        val config = context.resources.configuration
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }
}

@Composable
fun TeleprompterApp(scriptRepository: ScriptRepository) {
    val navController = rememberNavController()
    val context = LocalContext.current

    NavHost(navController = navController, startDestination = "welcome") {
        composable("welcome") {
            WelcomeScreen(
                onStartClick = { navController.navigate("script_list") },
                onLearnMoreClick = { /* TODO: Implement more info page */ }
            )
        }
        composable("script_list") {
            ScriptListScreen(
                scriptRepository = scriptRepository,
                onScriptClick = { scriptId -> navController.navigate("script_edit/$scriptId") },
                onNewScriptClick = { navController.navigate("script_edit/new") },
                onSettingsClick = { navController.navigate("settings") }
            )
        }
        composable(
            "script_edit/{scriptId}",
            arguments = listOf(navArgument("scriptId") { type = NavType.StringType })
        ) { backStackEntry ->
            val scriptId = backStackEntry.arguments?.getString("scriptId")
            ScriptEditScreen(
                scriptId = scriptId,
                scriptRepository = scriptRepository,
                onBackClick = { navController.popBackStack() },
                onStartPrompting = {
                    navController.navigate("teleprompter/$scriptId")
                },
                onSaveScript = { script ->
                    navController.popBackStack()
                }
            )
        }

        // Settings screen
        composable("settings") {
            SettingsScreen(
                onBackClick = { navController.popBackStack() },
                onLanguageChanged = {
                    // Recreate the activity to apply the new locale
                    (context as? Activity)?.recreate()
                }
            )
        }

        // Additional screens will be implemented in future steps
    }
}
