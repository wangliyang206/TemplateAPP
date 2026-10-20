package com.template.app.ui.webview

import android.content.Context
import android.webkit.JavascriptInterface
import com.template.core.ext.toastShort

/**
 * JS Bridge 接口类
 * 通过 window.AndroidBridge.methodName() 在 H5 端调用
 */
class WebViewJsBridge(private val context: Context) {

    /**
     * H5 调用：显示 Toast
     */
    @JavascriptInterface
    fun toast(message: String) {
        context.toastShort(message)
    }

    /**
     * H5 调用：返回上一页
     */
    @JavascriptInterface
    fun goBack() {
        (context as? android.app.Activity)?.runOnUiThread {
            (context as? androidx.appcompat.app.AppCompatActivity)?.onBackPressedDispatcher?.onBackPressed()
        }
    }
}
