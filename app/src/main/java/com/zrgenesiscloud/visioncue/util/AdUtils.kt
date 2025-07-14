package com.zrgenesiscloud.visioncue.util

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.bytedance.sdk.openadsdk.AdSlot
import com.bytedance.sdk.openadsdk.CSJAdError
import com.bytedance.sdk.openadsdk.CSJSplashAd
import com.bytedance.sdk.openadsdk.TTAdConstant
import com.bytedance.sdk.openadsdk.TTAdNative
import com.bytedance.sdk.openadsdk.TTAdSdk
import com.bytedance.sdk.openadsdk.TTFullScreenVideoAd
import com.bytedance.sdk.openadsdk.TTNativeExpressAd
import com.bytedance.sdk.openadsdk.mediation.ad.IMediationNativeAdInfo
import com.bytedance.sdk.openadsdk.mediation.ad.MediationAdSlot
import com.bytedance.sdk.openadsdk.mediation.ad.MediationNativeToBannerListener
import com.zrgenesiscloud.visioncue.manager.AdManager

object AdUtils {
    private const val TAG = "AdUtils"
    
    // Default banner ad dimensions in dp
    private const val DEFAULT_BANNER_WIDTH_DP = 300
    private const val DEFAULT_BANNER_HEIGHT_DP = 150
    
    // Default code IDs
    private const val DEFAULT_SPLASH_CODE_ID = "103432981"
    private const val DEFAULT_BANNER_CODE_ID = "103455587"

    // 构造开屏广告的Adslot
    fun buildSplashAdslot(codeId: String = DEFAULT_SPLASH_CODE_ID): AdSlot {
        Log.d(TAG, "Building splash ad slot with code ID: $codeId")
        return AdSlot.Builder()
            .setCodeId(codeId) // 广告位ID
            .build()
    }

    // 构造banner的Adslot - 添加参数使其更灵活
    fun buildBannerAdslot(
        codeId: String = DEFAULT_BANNER_CODE_ID,
        widthDp: Int = DEFAULT_BANNER_WIDTH_DP,
        heightDp: Int = DEFAULT_BANNER_HEIGHT_DP,
        context: Context
    ): AdSlot {
        // 将dp转换为像素
        val widthPx = dpToPx(widthDp, context)
        val heightPx = dpToPx(heightDp, context)
        
        Log.d(TAG, "Building banner ad slot with code ID: $codeId, size: ${widthPx}x${heightPx}px (${widthDp}x${heightDp}dp)")
        
        return AdSlot.Builder()
            .setCodeId(codeId)  // 广告位ID
            .setExpressViewAcceptedSize(widthDp.toFloat(),heightDp.toFloat())
            .setImageAcceptedSize(widthPx, heightPx)  // 设置广告宽高 单位px
            .setMediationAdSlot(
                MediationAdSlot.Builder()
                    /**
                     * banner混出自渲染信息流时，需要提供该转换listener，将信息流自渲染素材转成view。模板类型无需处理
                     * 如果未使用banner混出信息流功能，则无需设置MediationNativeToBannerListener。
                     * 如要使用混出功能，具体可参考接入文档
                     */
                    .setMediationNativeToBannerListener(object : MediationNativeToBannerListener() {
                        override fun getMediationBannerViewFromNativeAd(adInfo: IMediationNativeAdInfo): View? {
                            return null
                        }
                    })
                    .build()
            )
            .build()
    }

    //构造插全屏广告的Adlsot
    fun buildInterstitialFullAdslot(): AdSlot {
        return AdSlot.Builder()
            .setCodeId("103455687")  //广告位ID
            .setOrientation(TTAdConstant.VERTICAL)  //设置方向
            .setMediationAdSlot(
                MediationAdSlot.Builder()
                    .setMuted(false)
                    .build()
            )
            .build()
    }

    // 加载开屏广告
    fun loadSplashAd(
        activity: Activity, 
        container: ViewGroup, 
        onAdClosed: () -> Unit,
        codeId: String = DEFAULT_SPLASH_CODE_ID
    ) {
        Log.d(TAG, "Starting to load splash ad with code ID: $codeId")
        val adNativeLoader = TTAdSdk.getAdManager().createAdNative(activity)
        adNativeLoader.loadSplashAd(buildSplashAdslot(codeId), object : TTAdNative.CSJSplashAdListener {
            override fun onSplashLoadSuccess(p0: CSJSplashAd?) {
                Log.d(TAG, "Splash ad loaded successfully")
                if (p0 == null) {
                    Log.w(TAG, "Splash ad is null even though load was successful")
                }
            }

            override fun onSplashLoadFail(error: CSJAdError?) {
                Log.e(TAG, "Splash ad failed to load: ${error?.code} - ${error?.msg}")
                // 广告加载失败
                onAdClosed()
            }

            override fun onSplashRenderSuccess(csjSplashAd: CSJSplashAd?) {
                Log.d(TAG, "Splash ad rendered successfully")
                // 广告渲染成功，在此展示广告
                showSplashAd(csjSplashAd, container, onAdClosed)
            }

            override fun onSplashRenderFail(ad: CSJSplashAd?, error: CSJAdError?) {
                Log.e(TAG, "Splash ad failed to render: ${error?.code} - ${error?.msg}")
                // 广告渲染失败
                onAdClosed()
            }
        }, 3500)
        Log.d(TAG, "Splash ad load request sent with 3500ms timeout")
    }

    // 加载Banner广告 - 添加参数使其更灵活
    fun loadBannerAd(
        activity: Activity, 
        container: ViewGroup,
        widthDp: Int = DEFAULT_BANNER_WIDTH_DP,
        heightDp: Int = DEFAULT_BANNER_HEIGHT_DP
    ) {
        val codeId = if (AdManager.getRandomAdId("banner") != null) AdManager.getRandomAdId("banner") else DEFAULT_BANNER_CODE_ID

        Log.d(TAG, "Starting to load banner ad with code ID: $codeId, size: ${widthDp}x${heightDp}dp")
        val adNativeLoader = TTAdSdk.getAdManager().createAdNative(activity)

        adNativeLoader.loadBannerExpressAd(
            buildBannerAdslot(codeId.toString(), widthDp, heightDp, activity),
            object : TTAdNative.NativeExpressAdListener {
                override fun onNativeExpressAdLoad(ads: MutableList<TTNativeExpressAd>?) {
                    Log.d(TAG, "Banner ad loaded successfully")
                    if (ads.isNullOrEmpty()) {
                        Log.w(TAG, "Banner ad list is null or empty even though load was successful")
                        return
                    }
                    //广告加载成功
                    ads.let {
                        if (it.size > 0) {
                            val ad: TTNativeExpressAd = it[0]
                            showBannerAd(activity,ad, container) //注 ：bannerContainer为展示Banner广告的容器
                        }
                    }
                    Log.d(TAG, "Banner ad load request sent")
                }

                override fun onError(code: Int, message: String?) {
                    //广告加载失败
                    Log.e(TAG, "Banner ad failed to load: $code - $message")
                }
            })
    }

    //加载插全屏广告
    fun loadInterstitialFullAd(activity: Activity, onAdClosed: () -> Unit) {
        Log.d(TAG, "Starting to load interstitial full ad")
        val adNativeLoader = TTAdSdk.getAdManager().createAdNative(activity)
        adNativeLoader.loadFullScreenVideoAd(
            buildInterstitialFullAdslot(), object : TTAdNative.FullScreenVideoAdListener {
                override fun onError(code: Int, message: String?) {
                    Log.e(TAG, "Interstitial full ad failed to load: $code - $message")
                    // 广告加载失败时回调
                    onAdClosed()
                }

                override fun onFullScreenVideoAdLoad(ad: TTFullScreenVideoAd?) {
                    Log.d(TAG, "Interstitial full ad loaded successfully")
                    // 广告加载成功
                    if (ad == null) {
                        Log.w(TAG, "Interstitial full ad is null even though load was successful")
                        onAdClosed()
                        return
                    }
                }

                override fun onFullScreenVideoCached() {
                    // 广告缓存成功，此回调已经废弃，请使用onFullScreenVideoCached(ad: TTFullScreenVideoAd?)
                    Log.d(TAG, "Interstitial full ad cached (deprecated callback)")
                }

                override fun onFullScreenVideoCached(ad: TTFullScreenVideoAd?) {
                    Log.d(TAG, "Interstitial full ad cached successfully")
                    // 广告缓存成功，在此回调中展示广告
                    showInterstitialFullAd(activity, ad, onAdClosed)
                }
            })
    }

    // 展示开屏广告
    private fun showSplashAd(ad: CSJSplashAd?, container: ViewGroup, onAdClosed: () -> Unit) {
        if (ad == null) {
            Log.e(TAG, "Cannot show splash ad: ad is null")
            onAdClosed()
            return
        }
        
        Log.d(TAG, "Setting up splash ad listener before showing")
        ad.setSplashAdListener(object : CSJSplashAd.SplashAdListener {
            override fun onSplashAdShow(csjSplashAd: CSJSplashAd?) {
                Log.d(TAG, "Splash ad is now being displayed")
                // 广告展示
                // 获取展示广告相关信息，需要再show回调之后进行获取
                val manager = ad.mediationManager
                if (manager != null && manager.showEcpm != null) {
                    val ecpm = manager.showEcpm.ecpm // 展示广告的价格
                    val sdkName = manager.showEcpm.sdkName  // 展示广告的adn名称
                    val slotId = manager.showEcpm.slotId // 展示广告的代码位ID
                    Log.d(TAG, "Ad metrics - ECPM: $ecpm, SDK: $sdkName, SlotID: $slotId")
                } else {
                    Log.d(TAG, "No mediation manager or ECPM data available")
                }
            }

            override fun onSplashAdClick(csjSplashAd: CSJSplashAd?) {
                Log.d(TAG, "Splash ad was clicked")
                // 广告点击
            }

            override fun onSplashAdClose(csjSplashAd: CSJSplashAd?, closeType: Int) {
                Log.d(TAG, "Splash ad was closed with close type: $closeType")
                // 广告关闭
                onAdClosed()
            }
        })
        
        Log.d(TAG, "Attempting to show splash ad in container")
        try {
            ad.showSplashView(container) // 展示开屏广告
            Log.d(TAG, "Splash ad showSplashView called successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error showing splash ad: ${e.message}", e)
            onAdClosed()
        }
    }

    // 展示Banner广告
    private fun showBannerAd(act: Activity, bannerAd: TTNativeExpressAd?, container: ViewGroup) {
        Log.d(TAG, "Preparing to show banner ad")
        
        if (bannerAd == null) {
            Log.e(TAG, "Cannot show banner ad: ad is null")
            return
        }
        
        try {
            // 设置广告事件监听
            bannerAd.setExpressInteractionListener(object :
                TTNativeExpressAd.ExpressAdInteractionListener {
                override fun onAdClicked(view: View?, type: Int) {
                    Log.d(TAG, "Banner ad was clicked, type: $type")
                    //广告点击
                }

                override fun onAdShow(view: View?, type: Int) {
                    Log.d(TAG, "Banner ad is now showing, type: $type")
                    //广告展示
                    //获取展示广告相关信息，需要再show回调之后进行获取
                    val manager = bannerAd.mediationManager
                    if (manager != null && manager.showEcpm != null) {
                        val ecpm = manager.showEcpm.ecpm //展示广告的价格
                        val sdkName = manager.showEcpm.sdkName  //展示广告的adn名称
                        val slotId = manager.showEcpm.slotId //展示广告的代码位ID
                        Log.d(TAG, "Banner metrics - ECPM: $ecpm, SDK: $sdkName, SlotID: $slotId")
                    } else {
                        Log.d(TAG, "No ECPM data available for banner ad")
                    }
                }

                override fun onRenderFail(view: View?, msg: String?, code: Int) {
                    Log.e(TAG, "Banner ad failed to render: $code - $msg")
                    //广告渲染失败
                }

                override fun onRenderSuccess(view: View?, width: Float, height: Float) {
                    Log.d(TAG, "Banner ad rendered successfully, size: ${width}x${height}")
                    //广告渲染成功
                }
            })
            
            // Get the banner view
            val bannerView = bannerAd.expressAdView
            if (bannerView != null) {
                Log.d(TAG, "Adding banner view to container")
                // Clear container first to avoid adding multiple banner views
                container.removeAllViews()
                
                // Add banner view to container
                container.addView(bannerView)
                Log.d(TAG, "Banner view added to container successfully")
            } else {
                Log.e(TAG, "Banner view is null, cannot display ad")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error showing banner ad: ${e.message}", e)
        }
    }

    //展示插全屏广告
    fun showInterstitialFullAd(activity: Activity, ad: TTFullScreenVideoAd?, onAdClosed: () -> Unit) {
        Log.d(TAG, "Preparing to show interstitial full ad")
        
        if (ad == null) {
            Log.e(TAG, "Cannot show interstitial full ad: ad is null")
            onAdClosed()
            return
        }
        
        try {
            ad.let {
                if (it.mediationManager.isReady) {
                    it.setFullScreenVideoAdInteractionListener(object :
                        TTFullScreenVideoAd.FullScreenVideoAdInteractionListener {
                        override fun onAdShow() {
                            Log.d(TAG, "Interstitial full ad is now showing")
                            // 广告展示
                            // 获取展示广告相关信息
                            val manager = it.mediationManager
                            if (manager != null && manager.showEcpm != null) {
                                val ecpm = manager.showEcpm.ecpm // 展示广告的价格
                                val sdkName = manager.showEcpm.sdkName // 展示广告的adn名称
                                val slotId = manager.showEcpm.slotId // 展示广告的代码位ID
                                Log.d(TAG, "Ad metrics - ECPM: $ecpm, SDK: $sdkName, SlotID: $slotId")
                            } else {
                                Log.d(TAG, "No mediation manager or ECPM data available")
                            }
                        }

                        override fun onAdVideoBarClick() {
                            Log.d(TAG, "Interstitial full ad video bar was clicked")
                            // 广告点击
                        }

                        override fun onAdClose() {
                            Log.d(TAG, "Interstitial full ad was closed")
                            // 广告关闭
                            onAdClosed()
                        }

                        override fun onVideoComplete() {
                            Log.d(TAG, "Interstitial full ad video completed")
                            // 视频播放完成
                        }

                        override fun onSkippedVideo() {
                            Log.d(TAG, "Interstitial full ad video was skipped")
                            // 视频跳过
                        }
                    })
                    
                    Log.d(TAG, "Showing interstitial full ad")
                    it.showFullScreenVideoAd(activity)
                } else {
                    Log.e(TAG, "Interstitial full ad is not ready to show")
                    onAdClosed()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error showing interstitial full ad: ${e.message}", e)
            onAdClosed()
        }
    }

    // Convert dp to pixel
    fun dpToPx(dp: Int, context: Context): Int {
        val density = context.resources.displayMetrics.density
        return (dp * density + 0.5f).toInt()
    }
} 