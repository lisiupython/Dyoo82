package o.dyoo.hook.impl

import android.util.Log
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import o.dyoo.core.config.ModuleConfig

/**
 * 去水印 Hook
 *
 * 策略：
 * Hook okhttp3.Request.Builder.url() - OkHttp 稳定 API
 * 拦截包含水印参数的 URL，移除水印标记
 * 水印参数: watermark, wm_aid, wm_tt 等
 */
object WatermarkHook {
    private const val TAG = "Dyoo.WatermarkHook"

    fun setup(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (!ModuleConfig.isWatermarkRemoveEnabled) {
            Log.i(TAG, "去水印功能已禁用")
            return
        }
        Log.i(TAG, "初始化去水印 Hook")

        try {
            hookUrlBuilder(lpparam)
            Log.i(TAG, "去水印 Hook 成功")
        } catch (e: Throwable) {
            Log.e(TAG, "去水印 Hook 失败: ${e.message}")
            XposedBridge.log(e)
        }
    }

    /**
     * Hook OkHttp URL Builder - 拦截请求构建过程
     * 这是最可靠的去水印方案：在请求发出前修改 URL
     */
    private fun hookUrlBuilder(lpparam: XC_LoadPackage.LoadPackageParam) {
        // 尝试多个可能的类名
        val builderClasses = listOf(
            "okhttp3.Request\$Builder",
            "okhttp3.RequestBuilder"
        )

        for (className in builderClasses) {
            try {
                val builderClass = XposedHelpers.findClassIfExists(className, lpparam.classLoader)
                if (builderClass != null) {
                    XposedBridge.hookAllMethods(builderClass, "url", object : XC_MethodHook() {
                        override fun beforeHookedMethod(param: MethodHookParam) {
                            val arg = param.args[0]
                            if (arg is String) {
                                val modified = removeWatermarkParams(arg)
                                if (modified != arg) {
                                    param.args[0] = modified
                                    Log.d(TAG, "去除水印: $arg -> $modified")
                                }
                            }
                        }
                    })
                    Log.i(TAG, "去水印 Hook 成功: $className")
                    return
                }
            } catch (_: Throwable) {}
        }
        Log.w(TAG, "未找到 Request.Builder 类")
    }

    /**
     * 移除 URL 中的水印参数
     */
    private fun removeWatermarkParams(url: String): String {
        var result = url
        // 常见水印参数
        val watermarkParams = listOf(
            "watermark=1", "wm_aid=1", "wm_tt=1",
            "watermark_type", "wm_logo", "watermark_logo",
            "wm_aid=1", "wm_tt=1"
        )
        for (param in watermarkParams) {
            if (result.contains(param)) {
                result = result.replace(param, "")
            }
        }
        // 清理多余的 & 符号
        result = result.replace("&&", "&")
            .replace("?&", "?")
            .replace("&?", "?")
            .trimEnd('&')
        return result
    }
}