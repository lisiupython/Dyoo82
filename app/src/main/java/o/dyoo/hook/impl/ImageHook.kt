package o.dyoo.hook.impl

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import o.dyoo.core.config.ModuleConfig
import o.dyoo.core.download.Downloader

/**
 * 图片下载 Hook
 *
 * 策略：
 * Hook ImageView.setImageURI(Uri) - Android 稳定 API
 * 抖音图片通过 Glide/自定义加载器传入 URI，Hook 捕获图片 URL
 */
object ImageHook {
    private const val TAG = "Dyoo.ImageHook"
    var lastImageUrl: String? = null

    fun setup(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (!ModuleConfig.isImageDownloadEnabled) {
            Log.i(TAG, "图片下载功能已禁用")
            return
        }
        Log.i(TAG, "初始化图片下载 Hook")

        try {
            val imageViewClass = XposedHelpers.findClass("android.widget.ImageView", lpparam.classLoader)

            XposedBridge.hookAllMethods(imageViewClass, "setImageURI", object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val uri = param.args[0] as? Uri
                    uri?.toString()?.let { url ->
                        if (url.startsWith("http")) {
                            lastImageUrl = url
                            Log.d(TAG, "捕获图片URL: $url")
                        }
                    }
                }
            })
            Log.i(TAG, "图片 Hook 成功")
        } catch (e: Throwable) {
            Log.e(TAG, "图片 Hook 失败: ${e.message}")
            XposedBridge.log(e)
        }
    }

    fun saveCurrentImage(context: Context) {
        lastImageUrl?.let { Downloader.downloadImage(it, context) }
            ?: Toast.makeText(context, "未捕获到图片链接", Toast.LENGTH_SHORT).show()
    }
}