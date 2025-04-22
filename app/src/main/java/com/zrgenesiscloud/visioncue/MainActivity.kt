package com.zrgenesiscloud.visioncue

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
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
import com.zrgenesis.teleprompter.repository.ScriptRepository
import com.zrgenesiscloud.visioncue.util.LocaleManager
import android.content.Context
import android.app.Activity
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(updateLocale(newBase, LocaleManager(newBase).getLocale()))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create the repository
        val scriptRepository = AndroidScriptRepository(applicationContext)

        setContent {
            TeleprompterTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TeleprompterApp(scriptRepository)
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
