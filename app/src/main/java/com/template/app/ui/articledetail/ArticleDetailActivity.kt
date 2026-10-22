package com.template.app.ui.articledetail

import android.content.Intent
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.core.app.ShareCompat
import com.template.app.R
import com.template.app.data.model.Article
import com.template.app.data.model.ArticleDetail
import com.template.app.databinding.ActivityArticleDetailBinding
import com.template.app.ui.webview.WebViewActivity
import com.template.core.base.BaseActivity
import com.template.core.ext.collectEffects
import com.template.core.ext.collectFlow
import com.template.core.ext.toastShort
import com.template.core.state.UiEffect
import com.template.core.state.UiState
import com.template.core.state.asString
import dagger.hilt.android.AndroidEntryPoint

/**
 * 文章详情页（原生）。
 *
 * 与 WebView 版并存：这里演示「原生详情 + 收藏 + 分享」，WebView 版演示「第三方页面 + JS Bridge」。
 * 真实项目里通常是二选一，取决于正文是自家 HTML 还是外部站点。
 */
@AndroidEntryPoint
class ArticleDetailActivity : BaseActivity<ActivityArticleDetailBinding>() {

    companion object {
        const val EXTRA_ARTICLE_ID = "article_id"

        fun intent(context: android.content.Context, articleId: Long): Intent =
            Intent(context, ArticleDetailActivity::class.java).putExtra(EXTRA_ARTICLE_ID, articleId)
    }

    private val viewModel: ArticleDetailViewModel by viewModels()

    /** 详情加载完后缓存一份，供分享 / 打开原文使用 */
    private var loaded: ArticleDetail? = null

    override fun inflateBinding(inflater: LayoutInflater) = ActivityArticleDetailBinding.inflate(inflater)

    override fun getTitleBarId(): Int = R.id.titleBar

    override fun initView() {
        binding.ivBack.setOnClickListener { finish() }
        binding.ivShare.setOnClickListener { shareArticle() }
        binding.btnFavorite.setOnClickListener { viewModel.toggleFavorite() }
        binding.btnOpenSource.setOnClickListener { openSource() }

        viewModel.load(intent.getLongExtra(EXTRA_ARTICLE_ID, 0L))
    }

    override fun initObserver() {
        collectFlow(viewModel.state, ::render)
        collectFlow(viewModel.isFavorite, ::renderFavorite)
        collectEffects(viewModel) { effect ->
            when (effect) {
                is UiEffect.ShowMessage -> toastShort(effect.message.asString(this))
                is UiEffect.Close -> finish()
                is UiEffect.SessionExpired -> Unit
            }
        }
    }

    private fun render(state: UiState<ArticleDetail>) {
        when (state) {
            is UiState.Idle -> Unit
            is UiState.Loading -> binding.stateLayout.showLoading()
            is UiState.Empty -> binding.stateLayout.showEmpty(getString(R.string.article_detail_not_found))
            is UiState.Error -> binding.stateLayout.showError(
                message = state.message.asString(this),
                onRetry = { viewModel.load(intent.getLongExtra(EXTRA_ARTICLE_ID, 0L)) },
            )
            is UiState.Success -> {
                loaded = state.data
                binding.stateLayout.showContent()
                bindDetail(state.data)
            }
        }
    }

    private fun bindDetail(detail: ArticleDetail) {
        val article = detail.article
        binding.tvArticleTitle.text = article.title
        binding.tvMeta.text = getString(R.string.article_detail_meta, article.author, article.createTime)
        binding.tvContent.text = detail.content.ifBlank { getString(R.string.article_detail_load_failed) }
        // 长文回到顶部，否则从收藏页进来会停在上一篇文章的滚动位置
        binding.scrollView.scrollTo(0, 0)
    }

    private fun renderFavorite(isFavorite: Boolean) {
        binding.ivFavorite.setImageResource(
            if (isFavorite) R.drawable.ic_star_filled else R.drawable.ic_star_outline,
        )
        binding.tvFavorite.setText(
            if (isFavorite) R.string.article_detail_favorited else R.string.article_detail_favorite,
        )
    }

    private fun shareArticle() {
        val article: Article = loaded?.article ?: return
        ShareCompat.IntentBuilder(this)
            .setType("text/plain")
            .setChooserTitle(getString(R.string.article_detail_share_title))
            .setText(getString(R.string.article_detail_share_via, article.url))
            .startChooser()
    }

    private fun openSource() {
        val article: Article = loaded?.article ?: return
        startActivity(
            Intent(this, WebViewActivity::class.java).apply {
                putExtra(WebViewActivity.EXTRA_URL, article.url)
                putExtra(WebViewActivity.EXTRA_TITLE, article.title)
            },
        )
    }
}
