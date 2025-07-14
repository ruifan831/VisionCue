package com.zrgenesiscloud.visioncue.ad

import android.content.Context
import android.util.Log
import com.bytedance.sdk.openadsdk.TTAdConfig
import com.bytedance.sdk.openadsdk.TTAdSdk
import com.bytedance.sdk.openadsdk.TTCustomController
import com.bytedance.sdk.openadsdk.mediation.init.MediationPrivacyConfig
import com.zrgenesiscloud.visioncue.manager.AdManager
import java.util.concurrent.CompletableFuture

object TTAdSdkManager {
    private const val TAG = "TTAdSdkManager"
    @Volatile
    private var initialized = false
    @Volatile
    private var initializing = false
    private var initFuture: CompletableFuture<Boolean>? = null

    /**
     * Check if SDK is initialized
     */
    fun isInitialized(): Boolean = initialized

    /**
     * Ensure SDK is initialized asynchronously
     * @return CompletableFuture<Boolean> that completes with true if initialized successfully
     */
    fun ensureInitializedAsync(context: Context): CompletableFuture<Boolean> {
        if (initialized) {
            return CompletableFuture.completedFuture(true)
        }
        
        synchronized(this) {
            if (initialized) {
                return CompletableFuture.completedFuture(true)
            }
            
            if (initializing) {
                return initFuture ?: CompletableFuture.completedFuture(false)
            }
            
            initializing = true
            initFuture = CompletableFuture()
            
            TTAdSdk.init(context.applicationContext, buildConfig(context))
            TTAdSdk.start(object : TTAdSdk.Callback {
                override fun success() {
                    initialized = true
                    initializing = false
                    Log.d(TAG, "TTAdSdk initialized successfully")
                    initFuture?.complete(true)
                }
                override fun fail(code: Int, msg: String?) {
                    initializing = false
                    Log.e(TAG, "TTAdSdk initialization failed: $code, $msg")
                    initFuture?.complete(false)
                }
            })
            
            return initFuture!!
        }
    }

    /**
     * Reset for testing or recovery
     */
    fun reset() {
        synchronized(this) {
            initialized = false
            initializing = false
            initFuture = null
        }
    }

    private fun buildConfig(context: Context): TTAdConfig {
        // You can fetch appId/appName from AdManager if needed
        val appId = if (AdManager.getPangleAppId() != null) AdManager.getPangleAppId() else "5682546"
        val appName = if (AdManager.getAppName() != null) AdManager.getAppName() else "default"
        Log.d(TAG, "appId: ${appId}, appName: ${appName}")
        return TTAdConfig.Builder()
            .appId(appId)
            .appName(appName)
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
}