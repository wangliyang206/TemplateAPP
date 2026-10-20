package com.template.app.ui.home

import com.template.app.data.api.ApiService
import com.template.app.data.model.Banner
import com.template.app.data.model.BannerListParams
import com.template.app.data.model.Category
import com.template.app.data.model.CategoryListParams
import com.template.core.base.BaseRepository
import com.template.core.network.ApiRequestFactory
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

/**
 * 首页仓储：并发拉取轮播图与分类。
 *
 * 与上一版的区别：上一版 ViewModel 直接注入 ApiService 并静默吞异常，
 * 这个类则是只返回 "首页数据" 的死代码。现在数据获取回到数据层，
 * 错误统一由 [BaseRepository] 归一化成 AppError。
 */
class HomeRepository @Inject constructor(
    private val apiService: ApiService,
    private val requestFactory: ApiRequestFactory,
) : BaseRepository() {

    suspend fun load(): HomeData = coroutineScope {
        val banners = async {
            apiCall {
                safeApi {
                    apiService.getBannerList(requestFactory.build(BannerListParams(position = 0)))
                }
            }
        }
        val categories = async {
            apiCall {
                safeApi {
                    apiService.getCategoryList(requestFactory.build(CategoryListParams(type = 0)))
                }
            }
        }
        HomeData(banners.await(), categories.await())
    }
}

data class HomeData(val banners: List<Banner>, val categories: List<Category>) {
    val isEmpty: Boolean get() = banners.isEmpty() && categories.isEmpty()
}
