package com.template.app.ui.webview

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
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

    /** 当前是否处于加载失败态：onPageFinished 只在失败后恢复 Content，避免正常首载多一次状态切换 */
    private var loadFailed = false

    override fun getTitleBarId(): Int = R.id.titleBar

    override fun inflateBinding(inflater: LayoutInflater) = ActivityWebviewBinding.inflate(inflater)

    // 网页可后退时先退网页，否则交给系统回退（onBackPressed 已废弃，走 OnBackPressedDispatcher）
    private lateinit var backCallback: OnBackPressedCallback

    @SuppressLint("SetJavaScriptEnabled")
    override fun initView() {
        backCallback = onBackPressedDispatcher.addCallback(this, enabled = false) {
            binding.webView.goBack()
        }
        pageUrl = resolveUrl(intent.getStringExtra(EXTRA_URL) ?: "about:blank")
        pageTitle = intent.getStringExtra(EXTRA_TITLE) ?: ""

        binding.titleBar.ivBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
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
                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
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

                override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                    super.onReceivedError(view, request, error)
                    // 只对主 frame 展示错误态：子资源（图片/统计脚本）挂了不影响页面可用性。
                    // 外链内容失效（404/站点下线/断网）是常态，容器必须给兜底而不是白屏。
                    if (request?.isForMainFrame == true) {
                        showLoadError()
                    }
                }

                override fun onReceivedHttpError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    errorResponse: WebResourceResponse?,
                ) {
                    super.onReceivedHttpError(view, request, errorResponse)
                    // 404 等 HTTP 错误：WebView 会渲染服务端错误页，但站点下线时是空白/丑陋页面，统一兜底
                    if (request?.isForMainFrame == true && (errorResponse?.statusCode ?: 0) >= HTTP_ERROR_MIN) {
                        showLoadError()
                    }
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    // 错误态重试成功后恢复内容；正常首载本来就是 Content 态，此调用幂等
                    if (loadFailed) {
                        loadFailed = false
                        binding.stateLayout.showContent()
                    }
                    backCallback.isEnabled = binding.webView.canGoBack()
                }
            }

            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    binding.progressBar.progress = newProgress
                    binding.progressBar.visibility =
                        if (newProgress == 100) {
                            android.view.View.GONE
                        } else {
                            android.view.View.VISIBLE
                        }
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

    /** 展示加载失败态，点重试重新加载当前 URL */
    private fun showLoadError() {
        loadFailed = true
        binding.stateLayout.showError(getString(R.string.webview_load_failed)) {
            binding.webView.reload()
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

        /** HTTP >= 400 视为加载失败（主 frame） */
        private const val HTTP_ERROR_MIN = 400

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
                },
            )
        }

        private fun resolveUrl(url: String): String = when {
            url.isBlank() -> "about:blank"
            url.startsWith("/") -> BuildConfig.H5_HOST.trimEnd('/') + url
            else -> url
        }
    }
}
