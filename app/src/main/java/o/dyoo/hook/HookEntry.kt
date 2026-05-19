package o.dyoo.hook

import android.util.Log
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import o.dyoo.core.config.ModuleConfig
import o.dyoo.core.ui.FloatingView
import o.dyoo.hook.impl.*

/**
 * Dyoo Xposed 模块入口
 * 使用原生 Xposed Legacy API 82
 */
class HookEntry : IXposedHookLoadPackage {

    companion object {
        private const val TAG = "Dyoo.HookEntry"
        private const val TARGET_PACKAGE = "com.ss.android.ugc.aweme"
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (lpparam.packageName != TARGET_PACKAGE) return

        Log.i(TAG, "Dyoo: 加载目标应用 ${lpparam.packageName}")

        try {
            // 初始化配置
            initConfig()

            // 设置各模块 Hook
            VideoHook.setup(lpparam)
            ImageHook.setup(lpparam)
            WatermarkHook.setup(lpparam)
            PopupHook.setup(lpparam)
            CleanModeHook.setup(lpparam)

            Log.i(TAG, "Dyoo: 所有模块 Hook 注册完成")
        } catch (e: Throwable) {
            Log.e(TAG, "Dyoo: 初始化失败: ${e.message}")
            XposedBridge.log(e)
        }
    }

    /**
     * 初始化模块配置
     */
    private fun initConfig() {
        try {
            // 在 Hook 环境中初始化 SharedPreferences
            ModuleConfig.init(lpparam)
            Log.i(TAG, "Dyoo: 配置初始化完成")
        } catch (e: Throwable) {
            Log.e(TAG, "Dyoo: 配置初始化失败: ${e.message}")
        }
    }
}