package com.template.app.ui.webview

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.template.app.BuildConfig
import com.template.app.R
import com.template.app.databinding.ActivityWebviewBinding
import com.template.core.base.BaseActivity
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

/**
 * WebView 容器 Activity 示例
 *
 * 特性：
 * - 加载 URL
 * - 标题栏返回 + 关闭
 * - 进度条
 * - 拦截外部链接（其他 App 打开）
 * - JS Bridge 接口预留
 *
 * 用法（推荐走伴生方法，自动补 H5_HOST）：
 * WebViewActivity.start(context, "/activity/detail?id=1", "活动详情")   // 相对路径
 * WebViewActivity.start(context, "https://example.com", "详情")        // 绝对地址
 *
 * 也可直接传 Intent：
 * startActivity(Intent(context, WebViewActivity::class.java).apply {
 *     putExtra(WebViewActivity.EXTRA_URL, "https://example.com")
 *     putExtra(WebViewActivity.EXTRA_TITLE, "详情")
 * })
 */
@AndroidEntryPoint
class WebViewActivity : BaseActivity<ActivityWebviewBinding>() {

    private var pageUrl: String = "about:blank"
    private var pageTitle: String = ""

    override fun getTitleBarId(): Int = R.id.titleBar

    override fun inflateBinding(inflater: LayoutInflater) =
        ActivityWebviewBinding.inflate(inflater)

    @SuppressLint("SetJavaScriptEnabled")
    override fun initView() {
        pageUrl = resolveUrl(intent.getStringExtra(EXTRA_URL) ?: "about:blank")
        pageTitle = intent.getStringExtra(EXTRA_TITLE) ?: ""

        binding.titleBar.ivBack.setOnClickListener { onBackPressed() }
        binding.titleBar.tvTitle.text = pageTitle

        binding.webView.apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.setSupportZoom(false)
            settings.builtInZoomControls = false
            settings.useWideViewPort = true
            settings.loadWithOverviewMode = true

            // 添加 JS Bridge
            addJavascriptInterface(WebViewJsBridge(this@WebViewActivity), "AndroidBridge")

            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    val url = request?.url?.toString() ?: return false
                    // 拦截外部协议（如 alipays:// weixin://）
                    return if (url.startsWith("http://") || url.startsWith("https://")) {
                        false
                    } else {
                        try {
                            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        } catch (e: ActivityNotFoundException) {
                            // 设备上没有能处理该 Scheme 的 App（如未装支付宝），记录而不是静默吞掉
                            Timber.w(e, "无法打开外部 Scheme: %s", url)
                        }
                        true
                    }
                }
            }

            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    binding.progressBar.progress = newProgress
                    binding.progressBar.visibility =
                        if (newProgress == 100) android.view.View.GONE
                        else android.view.View.VISIBLE
                }

                override fun onReceivedTitle(view: WebView?, title: String?) {
                    if (pageTitle.isEmpty() && !title.isNullOrEmpty()) {
                        binding.titleBar.tvTitle.text = title ?: ""
                    }
                }
            }

            loadUrl(pageUrl)
        }
    }

    override fun onBackPressed() {
        if (binding.webView.canGoBack()) {
            binding.webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        binding.webView.apply {
            stopLoading()
            destroy()
        }
        super.onDestroy()
    }

    companion object {
        const val EXTRA_URL = "url"
        const val EXTRA_TITLE = "title"

        /**
         * 打开 WebView。
         *
         * [url] 以 `/` 开头时视为 H5 站内路径，用当前 flavor 的 `BuildConfig.H5_HOST` 拼接；
         * 否则按原样加载。这样切换 dev / staging / prod 时页面地址跟着变，业务代码不用改。
         */
        fun start(context: Context, url: String, title: String = "") {
            context.startActivity(
                Intent(context, WebViewActivity::class.java).apply {
                    putExtra(EXTRA_URL, url)
                    putExtra(EXTRA_TITLE, title)
                }
            )
        }

        private fun resolveUrl(url: String): String = when {
            url.isBlank() -> "about:blank"
            url.startsWith("/") -> BuildConfig.H5_HOST.trimEnd('/') + url
            else -> url
        }
    }
}