package o.dyoo.hook.impl

import android.app.Activity
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import o.dyoo.core.config.ModuleConfig

/**
 * 清爽模式 Hook
 *
 * 策略：
 * 1. Hook MediaPlayer.start() - 检测视频播放状态 (稳定 Android API)
 * 2. Hook MediaPlayer.pause() - 检测视频暂停
 * 3. Hook MediaPlayer.stop() - 检测视频停止
 * 4. 遍历视图树，隐藏/显示非视频 UI 组件
 * 5. 触摸屏幕时临时显示 UI 3 秒
 *
 * 全部基于 Android 稳定 API，不依赖任何混淆类名
 */
object CleanModeHook {
    private const val TAG = "Dyoo.CleanMode"
    private val handler = Handler(Looper.getMainLooper())
    private var isCleanMode = false
    private var isPlaying = false
    private var lastActivity: Activity? = null

    /** 临时显示 UI 后自动隐藏的 Runnable */
    private val autoHideRunnable = Runnable {
        if (isCleanMode && isPlaying) {
            hideAllUI()
        }
    }

    fun setup(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (!ModuleConfig.isCleanModeEnabled) {
            Log.i(TAG, "清爽模式功能已禁用")
            return
        }
        Log.i(TAG, "初始化清爽模式 Hook")
        isCleanMode = true

        try {
            hookMediaPlayer(lpparam)
            hookActivityLifecycle(lpparam)
            Log.i(TAG, "清爽模式 Hook 成功")
        } catch (e: Throwable) {
            Log.e(TAG, "清爽模式 Hook 失败: ${e.message}")
            XposedBridge.log(e)
        }
    }

    /**
     * Hook MediaPlayer 播放状态 - 稳定 Android API
     * start() = 播放 → 隐藏 UI
     * pause()/stop() = 暂停/停止 → 显示 UI
     */
    private fun hookMediaPlayer(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            val mediaPlayerClass = XposedHelpers.findClass("android.media.MediaPlayer", lpparam.classLoader)

            // Hook start()
            XposedBridge.hookAllMethods(mediaPlayerClass, "start", object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    if (!isCleanMode) return@afterHookedMethod
                    isPlaying = true
                    Log.d(TAG, "视频开始播放 - 隐藏 UI")
                    hideAllUI()
                }
            })

            // Hook pause()
            XposedBridge.hookAllMethods(mediaPlayerClass, "pause", object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    isPlaying = false
                    Log.d(TAG, "视频暂停 - 显示 UI")
                    showAllUI()
                }
            })

            // Hook stop()
            XposedBridge.hookAllMethods(mediaPlayerClass, "stop", object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    isPlaying = false
                    Log.d(TAG, "视频停止 - 显示 UI")
                    showAllUI()
                }
            })

            Log.i(TAG, "MediaPlayer hook 成功")
        } catch (e: Throwable) {
            Log.e(TAG, "MediaPlayer hook 失败: ${e.message}")
        }
    }

    /**
     * Hook Activity 生命周期
     * - onCreate: 设置触摸监听
     * - onDestroy: 清理状态
     */
    private fun hookActivityLifecycle(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            val activityClass = XposedHelpers.findClass("android.app.Activity", lpparam.classLoader)

            XposedBridge.hookAllMethods(activityClass, "onCreate", object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    val activity = param.thisObject as? Activity
                    if (activity?.packageName != "com.ss.android.ugc.aweme") return@afterHookedMethod
                    lastActivity = activity
                    setupTouchListener(activity)
                    Log.d(TAG, "Activity 创建: ${activity.javaClass.simpleName}")
                }
            })

            Log.i(TAG, "Activity 生命周期 hook 成功")
        } catch (e: Throwable) {
            Log.e(TAG, "Activity hook 失败: ${e.message}")
        }
    }

    /**
     * 设置触摸监听
     * 清爽模式下：触摸屏幕 → 显示 UI 3 秒 → 自动隐藏
     */
    private fun setupTouchListener(activity: Activity) {
        try {
            val decorView = activity.window?.decorView ?: return
            decorView.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    if (isCleanMode && isPlaying) {
                        Log.d(TAG, "触摸屏幕 - 临时显示 UI 3 秒")
                        showAllUI()
                        handler.removeCallbacks(autoHideRunnable)
                        handler.postDelayed(autoHideRunnable, 3000)
                    }
                }
                false // 不拦截事件
            }
        } catch (e: Throwable) {
            Log.e(TAG, "设置触摸监听失败: ${e.message}")
        }
    }

    /**
     * 启动清爽模式
     */
    fun activate() {
        isCleanMode = true
        Log.i(TAG, "清爽模式已激活")
        if (isPlaying) hideAllUI()
    }

    /**
     * 停止清爽模式
     */
    fun deactivate() {
        isCleanMode = false
        isPlaying = false
        handler.removeCallbacks(autoHideRunnable)
        showAllUI()
        Log.i(TAG, "清爽模式已关闭")
    }

    /**
     * 隐藏所有非视频 UI 组件
     */
    private fun hideAllUI() {
        try {
            val activity = lastActivity ?: return
            val root = activity.window?.decorView as? ViewGroup ?: return
            applyVisibility(root, View.INVISIBLE)
        } catch (e: Throwable) {
            Log.e(TAG, "隐藏 UI 失败: ${e.message}")
        }
    }

    /**
     * 显示所有 UI 组件
     */
    private fun showAllUI() {
        try {
            val activity = lastActivity ?: return
            val root = activity.window?.decorView as? ViewGroup ?: return
            applyVisibility(root, View.VISIBLE)
        } catch (e: Throwable) {
            Log.e(TAG, "显示 UI 失败: ${e.message}")
        }
    }

    /**
     * 遍历视图树，设置非视频组件的可见性
     *
     * 判断逻辑：
     * - ID 包含 video/player/surface/texture → 跳过（视频播放器）
     * - 其他所有组件 → 设置可见性
     */
    private fun applyVisibility(view: View, visibility: Int) {
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val child = view.getChildAt(i) ?: continue
                applyVisibility(child, visibility)
            }
        }

        try {
            val idName = view.resources?.getResourceEntryName(view.id) ?: ""
            val isVideoPlayer = idName.contains("video") ||
                    idName.contains("player") ||
                    idName.contains("surface") ||
                    idName.contains("texture")

            if (!isVideoPlayer) {
                view.visibility = visibility
            }
        } catch (_: Throwable) {
            // 无 ID 的视图也隐藏/显示
            view.visibility = visibility
        }
    }
}