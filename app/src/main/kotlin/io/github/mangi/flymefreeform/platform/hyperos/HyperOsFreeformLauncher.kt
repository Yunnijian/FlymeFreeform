package io.github.mangi.flymefreeform.platform.hyperos

import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import io.github.mangi.flymefreeform.platform.common.FreeformLauncher
import io.github.mangi.flymefreeform.platform.common.FreeformLaunchResult
import java.lang.reflect.Method

/**
 * HyperOS 小窗启动：官方闸门入口 → 官方构造（跳过闸门）→ 手工兜底。
 * 三级都使用 AOSP FREEFORM 窗口模式；任何失败只记日志，不退化为全屏启动。
 */
@SuppressLint("PrivateApi")
internal class HyperOsFreeformLauncher(
    private val context: Context,
    private val logger: (Int, String, Throwable?) -> Unit,
) : FreeformLauncher {
    override fun launch(component: ComponentName): FreeformLaunchResult {
        if (!isLaunchable(component)) return FreeformLaunchResult.TargetUnavailable

        val intent =
            Intent(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_LAUNCHER)
                .setComponent(component)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val options =
            createLaunchOptions(component.packageName)
                ?: return FreeformLaunchResult.Failed(
                    "FREEFORM_LAUNCH_OPTIONS_UNAVAILABLE",
                    IllegalStateException("MIUI freeform options unavailable"),
                )

        return try {
            context.startActivity(intent, options)
            FreeformLaunchResult.Started
        } catch (exception: SecurityException) {
            FreeformLaunchResult.Failed("FREEFORM_LAUNCH_SECURITY_REJECTED", exception)
        } catch (exception: RuntimeException) {
            FreeformLaunchResult.Failed("FREEFORM_LAUNCH_START_FAILED", exception)
        }
    }

    private fun createLaunchOptions(packageName: String): Bundle? {
        attempt("FREEFORM_LAUNCH_GATED_OPTIONS_FAILED") { gatedOptions(packageName) }?.let { return it }
        attempt("FREEFORM_LAUNCH_MAKE_OPTIONS_FAILED") { makeOptions(packageName) }?.let { return it }
        return attempt("FREEFORM_LAUNCH_MANUAL_OPTIONS_FAILED") { manualOptions() }
    }

    /** 官方入口：闸门按用户/白名单判断，被拒时交给下一级。 */
    private fun gatedOptions(packageName: String): Bundle? {
        val options =
            utility(
                "getActivityOptions",
                Context::class.java,
                String::class.java,
                Boolean::class.javaPrimitiveType!!,
                Boolean::class.javaPrimitiveType!!,
            ).invoke(null, context, packageName, true, false) as? ActivityOptions
        if (options == null) {
            logger(Log.WARN, "FREEFORM_LAUNCH_GATE_REJECTED", null)
            return null
        }
        return options.toBundle()
    }

    /** 官方构造：跳过白名单闸门，但仍由 MIUI 计算尺寸、缩放与正常小窗标记。 */
    private fun makeOptions(packageName: String): Bundle {
        val options =
            utility(
                "makeActivityOptions",
                Context::class.java,
                String::class.java,
                Int::class.javaPrimitiveType!!,
                Int::class.javaPrimitiveType!!,
            ).invoke(null, context, packageName, Int.MIN_VALUE, Int.MIN_VALUE) as? ActivityOptions
                ?: throw IllegalStateException("makeActivityOptions returned null")
        return options.toBundle()
    }

    /** 手工兜底：不依赖 MIUI 工具类，最小参数集合。 */
    private fun manualOptions(): Bundle {
        val options = ActivityOptions.makeBasic()
        ActivityOptions::class.java
            .getMethod("setLaunchWindowingMode", Int::class.javaPrimitiveType)
            .invoke(options, FREEFORM_WINDOWING_MODE)
        ActivityOptions::class.java
            .getMethod("setMiuiConfigFlag", Int::class.javaPrimitiveType)
            .invoke(options, MIUI_CONFIG_FLAG_FREEFORM)
        applyFreeformInjector(options)
        return options.toBundle()
    }

    /** 注入器属于补强参数；缺失不阻断启动。 */
    private fun applyFreeformInjector(options: ActivityOptions) {
        try {
            val injector =
                ActivityOptions::class.java.getMethod("getActivityOptionsInjector").invoke(options)
                    ?: return
            injector.javaClass
                .getMethod("setFreeformScale", Float::class.javaPrimitiveType)
                .invoke(injector, 1f)
            injector.javaClass
                .getMethod("setNormalFreeForm", Boolean::class.javaPrimitiveType)
                .invoke(injector, true)
        } catch (exception: ReflectiveOperationException) {
            logger(Log.WARN, "FREEFORM_LAUNCH_INJECTOR_UNAVAILABLE", exception)
        } catch (exception: RuntimeException) {
            logger(Log.WARN, "FREEFORM_LAUNCH_INJECTOR_UNAVAILABLE", exception)
        }
    }

    private fun attempt(code: String, block: () -> Bundle?): Bundle? =
        try {
            block()
        } catch (exception: ReflectiveOperationException) {
            logger(Log.WARN, code, exception)
            null
        } catch (exception: RuntimeException) {
            logger(Log.WARN, code, exception)
            null
        }

    private fun utility(name: String, vararg parameterTypes: Class<*>): Method =
        Class.forName(MIUI_MULTI_WINDOW_UTILS_CLASS, false, context.classLoader)
            .getMethod(name, *parameterTypes)

    private fun isLaunchable(component: ComponentName): Boolean =
        try {
            context.packageManager
                .getActivityInfo(component, PackageManager.ComponentInfoFlags.of(0))
                .let { info -> info.enabled && info.applicationInfo.enabled && info.exported }
        } catch (_: PackageManager.NameNotFoundException) {
            false
        } catch (_: RuntimeException) {
            false
        }

    private companion object {
        const val MIUI_MULTI_WINDOW_UTILS_CLASS = "android.util.MiuiMultiWindowUtils"
        const val FREEFORM_WINDOWING_MODE = 5
        const val MIUI_CONFIG_FLAG_FREEFORM = 2
    }
}