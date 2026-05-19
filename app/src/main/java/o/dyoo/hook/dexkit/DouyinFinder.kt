package o.dyoo.hook.dexkit

import android.util.Log
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * 抖音类搜索器
 *
 * 由于使用原生 Xposed API，不再依赖 DexKit
 * 这里提供简化的类搜索功能，基于 Android 稳定 API
 *
 * 注意：主要功能已通过 Android 系统级 API 实现，
 * 此组件主要用于高级功能扩展
 */
object DouyinFinder {
    private const val TAG = "Dyoo.Finder"

    // 缓存的搜索结果
    private var _videoModelClass: String? = null
    private var _playerControllerClass: String? = null
    private var _downloadHelperClass: String? = null
    private var _watermarkBuilderClass: String? = null

    /** 视频数据模型类 (包含播放地址字段) */
    val videoModelClass: String? get() = _videoModelClass

    /** 视频播放控制器类 */
    val playerControllerClass: String? get() = _playerControllerClass

    /** 下载辅助类 */
    val downloadHelperClass: String? get() = _downloadHelperClass

    /** 水印 URL 构建类 */
    val watermarkBuilderClass: String? get() = _watermarkBuilderClass

    private var initialized = false

    /**
     * 初始化搜索器
     * 在原生 Xposed 环境中，不需要复杂的 DexKit 初始化
     */
    fun init(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (initialized) return
        initialized = true
        Log.i(TAG, "开始初始化类搜索器...")

        try {
            // 由于主要功能已通过 Android 稳定 API 实现
            // 这里使用备用的类名方案
            useFallbackNames()
            Log.i(TAG, "搜索完成")
        } catch (e: Throwable) {
            Log.e(TAG, "搜索失败: ${e.message}")
            useFallbackNames()
        }
    }

    /**
     * 备用方案: 使用硬编码的常见类名
     * 这些类名在抖音各版本中相对稳定
     */
    private fun useFallbackNames() {
        Log.w(TAG, "使用备用类名")
        // 抖音视频数据模型
        _videoModelClass = "com.ss.android.ugc.aweme.feed.model.Aweme"
        _playerControllerClass = null // 运行时发现
        _downloadHelperClass = null
        _watermarkBuilderClass = null
    }

    /**
     * 重置搜索状态 (版本更新时使用)
     */
    fun reset() {
        initialized = false
        _videoModelClass = null
        _playerControllerClass = null
        _downloadHelperClass = null
        _watermarkBuilderClass = null
    }
}