package com.template.app.ui.webview

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.viewModels
import com.template.app.databinding.ActivityWebviewBinding
import com.template.core.base.BaseActivity
import dagger.hilt.android.AndroidEntryPoint

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
 * 用法：
 * startActivity(Intent(context, WebViewActivity::class.java).apply {
 *     putExtra("url", "https://example.com")
 *     putExtra("title", "详情")
 * })
 */
@AndroidEntryPoint
class WebViewActivity : BaseActivity<ActivityWebviewBinding>() {

    private lateinit var url: String
    private lateinit var title: String

    override fun inflateBinding(inflater: LayoutInflater) =
        ActivityWebviewBinding.inflate(inflater)

    @SuppressLint("SetJavaScriptEnabled")
    override fun initView() {
        url = intent.getStringExtra("url") ?: "about:blank"
        title = intent.getStringExtra("title") ?: ""

        binding.ivBack.setOnClickListener { onBackPressed() }
        binding.ivClose.setOnClickListener { finish() }
        binding.tvTitle.text = title

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
                        } catch (_: Exception) { }
                        true
                    }
                }
            }

            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    binding.progressBar.progress = newProgress
                    if (newProgress == 100) {
                        binding.progressBar.visibility = android.view.View.GONE
                    } else {
                        binding.progressBar.visibility = android.view.View.VISIBLE
                    }
                }

                override fun onReceivedTitle(view: WebView?, pageTitle: String?) {
                    if (title.isEmpty() && !pageTitle.isNullOrEmpty()) {
                        binding.tvTitle.text = pageTitle
                    }
                }
            }

            loadUrl(url)
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
}
