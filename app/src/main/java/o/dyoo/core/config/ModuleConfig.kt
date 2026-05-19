package o.dyoo.core.config

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * 模块配置
 * 管理所有设置项
 * 
 * 注意：在 Xposed 环境中需要正确初始化 Context
 */
object ModuleConfig {

    private const val TAG = "Dyoo.Config"
    private const val PREF_NAME = "dyoo_prefs"
    
    private var _prefs: SharedPreferences? = null
    private var _context: Context? = null

    /**
     * 在 Xposed Hook 环境中初始化
     */
    fun init(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            // 通过反射获取 Context
            val activityThreadClass = Class.forName("android.app.ActivityThread")
            val currentActivityThread = activityThreadClass.getMethod("currentActivityThread").invoke(null)
            val application = activityThreadClass.getMethod("getApplication").invoke(currentActivityThread) as Context
            
            _context = application
            _prefs = application.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            Log.i(TAG, "SharedPreferences 初始化成功")
        } catch (e: Throwable) {
            Log.e(TAG, "初始化 SharedPreferences 失败: ${e.message}")
            throw e
        }
    }

    /**
     * 获取 SharedPreferences
     */
    private val prefs: SharedPreferences
        get() {
            if (_prefs == null) {
                throw IllegalStateException("ModuleConfig 未初始化，请先调用 init()")
            }
            return _prefs!!
        }

    // 功能开关
    var isVideoDownloadEnabled: Boolean
        get() = prefs.getBoolean("video_download", true)
        set(v) = prefs.edit().putBoolean("video_download", v).apply()

    var isImageDownloadEnabled: Boolean
        get() = prefs.getBoolean("image_download", true)
        set(v) = prefs.edit().putBoolean("image_download", v).apply()

    var isWatermarkRemoveEnabled: Boolean
        get() = prefs.getBoolean("watermark_remove", true)
        set(v) = prefs.edit().putBoolean("watermark_remove", v).apply()

    var isWebDavEnabled: Boolean
        get() = prefs.getBoolean("webdav_enabled", false)
        set(v) = prefs.edit().putBoolean("webdav_enabled", v).apply()

    // WebDav
    var webDavUrl: String
        get() = prefs.getString("webdav_url", "") ?: ""
        set(v) = prefs.edit().putString("webdav_url", v).apply()

    var webDavUsername: String
        get() = prefs.getString("webdav_username", "") ?: ""
        set(v) = prefs.edit().putString("webdav_username", v).apply()

    var webDavPassword: String
        get() = prefs.getString("webdav_password", "") ?: ""
        set(v) = prefs.edit().putString("webdav_password", v).apply()

    // 保存路径
    var savePath: String
        get() = prefs.getString("save_path", "") ?: ""
        set(v) = prefs.edit().putString("save_path", v).apply()

    // 悬浮窗
    var showFloatingButton: Boolean
        get() = prefs.getBoolean("floating_button", true)
        set(v) = prefs.edit().putBoolean("floating_button", v).apply()

    // 下载质量 (0=高清 1=标清)
    var downloadQuality: Int
        get() = prefs.getInt("download_quality", 0)
        set(v) = prefs.edit().putInt("download_quality", v).apply()

    // 清爽模式
    var isCleanModeEnabled: Boolean
        get() = prefs.getBoolean("clean_mode", false)
        set(v) = prefs.edit().putBoolean("clean_mode", v).apply()

    // 定时退出（分钟，0=关闭）
    var autoExitMinutes: Int
        get() = prefs.getInt("auto_exit", 0)
        set(v) = prefs.edit().putInt("auto_exit", v).apply()
}