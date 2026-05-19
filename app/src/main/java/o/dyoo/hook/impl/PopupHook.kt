package o.dyoo.hook.impl

import android.app.Activity
import android.util.Log
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import o.dyoo.core.config.ModuleConfig
import o.dyoo.core.ui.FloatingView

/**
 * 悬浮窗 Hook
 *
 * Hook Activity 生命周期，管理悬浮窗的显示和隐藏
 */
object PopupHook {
    private const val TAG = "Dyoo.PopupHook"
    private const val TARGET_PACKAGE = "com.ss.android.ugc.aweme"

    fun setup(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (!ModuleConfig.showFloatingButton) {
            Log.i(TAG, "悬浮窗功能已禁用")
            return
        }
        Log.i(TAG, "初始化悬浮窗 Hook")

        try {
            val activityClass = XposedHelpers.findClass("android.app.Activity", lpparam.classLoader)

            // Hook onResume - 显示悬浮窗
            XposedBridge.hookAllMethods(activityClass, "onResume", object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    val activity = param.thisObject as? Activity
                    if (activity?.packageName == TARGET_PACKAGE) {
                        FloatingView.show(activity)
                    }
                }
            })

            // Hook onPause - 隐藏悬浮窗
            XposedBridge.hookAllMethods(activityClass, "onPause", object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val activity = param.thisObject as? Activity
                    if (activity?.packageName == TARGET_PACKAGE) {
                        FloatingView.hide()
                    }
                }
            })

            Log.i(TAG, "悬浮窗 Hook 成功")
        } catch (e: Throwable) {
            Log.e(TAG, "悬浮窗 Hook 失败: ${e.message}")
            XposedBridge.log(e)
        }
    }
}