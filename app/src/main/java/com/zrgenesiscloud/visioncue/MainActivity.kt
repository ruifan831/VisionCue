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

class MainActivity : ComponentActivity() {
    private lateinit var privacyPolicyManager: PrivacyPolicyManager
    
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(updateLocale(newBase, LocaleManager(newBase).getLocale()))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize privacy policy manager
        privacyPolicyManager = PrivacyPolicyManager(applicationContext)

        // Create the repository
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
                                
                                // Initialize the Pangle SDK after privacy consent
                                initMediationAdSdk(applicationContext)
                            },
                            onDecline = {
                                // Exit the app
                                finish()
                                exitProcess(0)
                            }
                        )
                    } else {
                        // Initialize the Pangle SDK (only if privacy policy accepted)
                        initMediationAdSdk(applicationContext)
                        
                        // Continue with the main app
                        TeleprompterApp(scriptRepository)
                    }
                }
            }
        }
    }

    // 初始化聚合sdk
    private fun initMediationAdSdk(context: Context) {
        TTAdSdk.init(context, buildConfig(context))
        TTAdSdk.start(object : TTAdSdk.Callback {
            override fun success() {
                // 初始化成功
                // 在初始化成功回调之后进行广告加载
            }

            override fun fail(code: Int, msg: String?) {
                // 初始化失败
            }
        })
    }

    // 构造TTAdConfig
    private fun buildConfig(context: Context): TTAdConfig {
        return TTAdConfig.Builder()
            .appId("5682546") // APP ID
            .appName("灵犀提词") // APP Name
            .useMediation(true)  // 开启聚合功能
            .debug(false)  // 关闭debug开关
            .themeStatus(0)  // 正常模式  0是正常模式；1是夜间模式；
            /**
             * 多进程增加注释说明：V>=5.1.6.0支持多进程，如需开启可在初始化时设置.supportMultiProcess(true) ，默认false；
             * 注意：开启多进程开关时需要将ADN的多进程也开启，否则广告展示异常，影响收益。
             * CSJ、gdt无需额外设置，KS、baidu、Sigmob、Mintegral需要在清单文件中配置各家ADN激励全屏xxxActivity属性android:multiprocess="true"
             */
            .supportMultiProcess(false)  // 不支持
            .customController(getTTCustomController())  // 设置隐私权
            .build()
    }

    // 设置隐私合规
    private fun getTTCustomController(): TTCustomController? {
        return object : TTCustomController() {
            override fun isCanUseLocation(): Boolean {  // 是否授权位置权限
                return true
            }

            override fun isCanUsePhoneState(): Boolean {  // 是否授权手机信息权限
                return true
            }

            override fun isCanUseWifiState(): Boolean {  // 是否授权wifi state权限
                return true
            }

            override fun isCanUseWriteExternal(): Boolean {  // 是否授权写外部存储权限
                return true
            }

            override fun isCanUseAndroidId(): Boolean {  // 是否授权Android Id权限
                return true
            }

            override fun getMediationPrivacyConfig(): MediationPrivacyConfig? {
                return object : MediationPrivacyConfig() {
                    override fun isLimitPersonalAds(): Boolean {  // 是否限制个性化广告
                        return false
                    }

                    override fun isProgrammaticRecommend(): Boolean {  // 是否开启程序化广告推荐
                        return true
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
