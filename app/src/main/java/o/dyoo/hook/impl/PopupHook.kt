package o.dyoo.hook.impl

import android.app.Activity
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import o.dyoo.core.config.ModuleConfig
import o.dyoo.core.ui.FloatingView

object PopupHook {
    fun setup(classLoader: ClassLoader) {
        if (!ModuleConfig.showFloatingButton) return
        try {
            XposedHelpers.findAndHookMethod(
                Activity::class.java,
                "onResume",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        (param.thisObject as? Activity)?.let {
                            if (it.packageName == "com.ss.android.ugc.aweme") FloatingView.show(it)
                        }
                    }
                }
            )
            XposedHelpers.findAndHookMethod(
                Activity::class.java,
                "onPause",
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        (param.thisObject as? Activity)?.let {
                            if (it.packageName == "com.ss.android.ugc.aweme") FloatingView.hide()
                        }
                    }
                }
            )
        } catch (e: Throwable) {
            android.util.Log.e("Dyoo.PopupHook", "Popup hook 失败: ${e.message}")
        }
    }
}
