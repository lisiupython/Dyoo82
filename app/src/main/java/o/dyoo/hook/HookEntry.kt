package o.dyoo.hook

import de.robv.android.xposed.IXposedHookLoadPackage          // ← legacy api82
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import o.dyoo.hook.dexkit.DouyinFinder
import o.dyoo.hook.impl.*

// 无任何注解 — legacy api82 通过 xposed_init 文件发现入口类
class HookEntry : IXposedHookLoadPackage {
 
    override fun handleLoadPackage(lpparam: LoadPackageParam) {
        if (lpparam.packageName != "com.ss.android.ugc.aweme") return
 
        // DexKit 搜索混淆类（保留原有逻辑）
        DouyinFinder.init(lpparam.classLoader)
 
        // 各模块 Hook（传入 classLoader 替代 PackageParam）
        VideoHook.setup(lpparam.classLoader)
        ImageHook.setup(lpparam.classLoader)
        WatermarkHook.setup(lpparam.classLoader)
        PopupHook.setup(lpparam.classLoader)
        CleanModeHook.setup(lpparam.classLoader)
    }
}