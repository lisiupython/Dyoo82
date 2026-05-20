package o.dyoo.hook.impl

import android.app.Activity
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
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

    private val autoHideRunnable = Runnable {
        if (isCleanMode && isPlaying) {
            hideAllUI()
        }
    }

    fun setup(classLoader: ClassLoader) {
        if (!ModuleConfig.isCleanModeEnabled) return
        Log.i(TAG, "初始化清爽模式 Hook")
        hookMediaPlayer()
        hookActivityLifecycle()
    }

    private fun hookMediaPlayer() {
        try {
            XposedHelpers.findAndHookMethod(
                MediaPlayer::class.java,
                "start",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        if (!isCleanMode) return
                        isPlaying = true
                        Log.d(TAG, "视频开始播放 - 隐藏 UI")
                        hideAllUI()
                    }
                }
            )
            XposedHelpers.findAndHookMethod(
                MediaPlayer::class.java,
                "pause",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        isPlaying = false
                        Log.d(TAG, "视频暂停 - 显示 UI")
                        showAllUI()
                    }
                }
            )
            XposedHelpers.findAndHookMethod(
                MediaPlayer::class.java,
                "stop",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        isPlaying = false
                        Log.d(TAG, "视频停止 - 显示 UI")
                        showAllUI()
                    }
                }
            )
            Log.i(TAG, "MediaPlayer hook 成功")
        } catch (e: Throwable) {
            Log.e(TAG, "MediaPlayer hook 失败: ${e.message}")
        }
    }

    private fun hookActivityLifecycle() {
        try {
            XposedHelpers.findAndHookMethod(
                Activity::class.java,
                "onCreate",
                Bundle::class.java,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val activity = param.thisObject as? Activity ?: return
                        if (activity.packageName != "com.ss.android.ugc.aweme") return
                        lastActivity = activity
                        isCleanMode = true
                        setupTouchListener(activity)
                        Log.d(TAG, "Activity 创建: ${activity.javaClass.simpleName}")
                    }
                }
            )
            Log.i(TAG, "Activity 生命周期 hook 成功")
        } catch (e: Throwable) {
            Log.e(TAG, "Activity hook 失败: ${e.message}")
        }
    }

    private fun setupTouchListener(activity: Activity) {
        try {
            val decorView = activity.window?.decorView ?: return
            decorView.setOnTouchListener { _, event ->
                if (event.action == android.view.MotionEvent.ACTION_DOWN) {
                    if (isCleanMode && isPlaying) {
                        Log.d(TAG, "触摸屏幕 - 临时显示 UI 3 秒")
                        showAllUI()
                        handler.removeCallbacks(autoHideRunnable)
                        handler.postDelayed(autoHideRunnable, 3000)
                    }
                }
                false
            }
        } catch (e: Throwable) {
            Log.e(TAG, "设置触摸监听失败: ${e.message}")
        }
    }

    fun activate() {
        isCleanMode = true
        Log.i(TAG, "清爽模式已激活")
        if (isPlaying) hideAllUI()
    }

    fun deactivate() {
        isCleanMode = false
        isPlaying = false
        handler.removeCallbacks(autoHideRunnable)
        showAllUI()
        Log.i(TAG, "清爽模式已关闭")
    }

    private fun hideAllUI() {
        try {
            val activity = lastActivity ?: return
            val root = activity.window?.decorView as? ViewGroup ?: return
            applyVisibility(root, View.INVISIBLE)
        } catch (e: Throwable) {
            Log.e(TAG, "隐藏 UI 失败: ${e.message}")
        }
    }

    private fun showAllUI() {
        try {
            val activity = lastActivity ?: return
            val root = activity.window?.decorView as? ViewGroup ?: return
            applyVisibility(root, View.VISIBLE)
        } catch (e: Throwable) {
            Log.e(TAG, "显示 UI 失败: ${e.message}")
        }
    }

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
            view.visibility = visibility
        }
    }
}
