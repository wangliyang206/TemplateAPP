package com.template.app.ui.home

import androidx.paging.testing.asSnapshot
import com.template.app.data.api.StubApiService
import com.template.app.data.model.Article
import com.template.app.data.model.ArticleListParams
import com.template.app.data.model.ArticleListResponse
import com.template.app.data.repository.ArticleRepository
import com.template.app.testing.MainDispatcherRule
import com.template.core.network.ApiRequest
import com.template.core.network.BaseResponse
import com.template.core.network.ErrorInfo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * 分页 ViewModel 测试：验证「分类切换 → Pager → PagingSource → ApiService」整条链路。
 *
 * 用 paging-testing 的 asSnapshot 把 PagingData 拉平成 List 断言：
 * - 不带操作 = 只等首屏（initialLoadSize）加载完成；
 * - appendScrollWhile = 模拟滚动触发翻页。
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ArticleListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private class FakeApiService : StubApiService() {

        /** 每页条数（与 ArticleRepository.PAGE_SIZE 保持一致） */
        private val pageSize = 20

        /** 前 2 页有数据，第 3 页起为空，用于验证翻页终止 */
        private val pagesWithData = 2

        override suspend fun getArticleList(request: ApiRequest<ArticleListParams>): BaseResponse<ArticleListResponse> {
            val params = request.data ?: ArticleListParams(1, pageSize, 0)
            val list = if (params.page <= pagesWithData) {
                List(pageSize) { index ->
                    val id = (params.page - 1) * pageSize + index + 1L
                    Article(id = id, title = "分类${params.categoryId}-文章$id", desc = "desc-$id")
                }
            } else {
                emptyList()
            }
            return BaseResponse(1, null as ErrorInfo?, ArticleListResponse(total = pagesWithData * pageSize, list))
        }
    }

    @Test
    fun `首屏加载第一页 20 条`() = runTest {
        val viewModel = ArticleListViewModel(ArticleRepository(FakeApiService()))

        val items = viewModel.articleFlow.asSnapshot()

        assertEquals(20, items.size)
        assertEquals(1L, items.first().id)
    }

    @Test
    fun `滚动触发翻页 加载到第二页共 40 条`() = runTest {
        val viewModel = ArticleListViewModel(ArticleRepository(FakeApiService()))

        val items = viewModel.articleFlow.asSnapshot {
            appendScrollWhile { it.id != 25L }
        }

        assertEquals(40, items.size)
        assertTrue(items.any { it.id == 25L })
    }

    @Test
    fun `切换分类后加载对应分类的数据`() = runTest {
        val viewModel = ArticleListViewModel(ArticleRepository(FakeApiService()))
        viewModel.setCategoryId(7)

        val items = viewModel.articleFlow.asSnapshot()

        assertTrue(items.isNotEmpty())
        assertTrue(items.all { it.title.startsWith("分类7") })
    }
}
