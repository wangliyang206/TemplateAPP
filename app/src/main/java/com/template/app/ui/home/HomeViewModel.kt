package com.template.app.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.template.app.data.api.ApiService
import com.template.app.data.model.Banner
import com.template.app.data.model.BannerListParams
import com.template.app.data.model.Category
import com.template.app.data.model.CategoryListParams
import com.template.core.network.ApiRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 首页 ViewModel - 管理轮播图和分类数据
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    /**
     * 轮播图数据
     */
    private val _bannerList = MutableLiveData<List<Banner>>()
    val bannerList: LiveData<List<Banner>> = _bannerList

    /**
     * 分类数据
     */
    private val _categoryList = MutableLiveData<List<Category>>()
    val categoryList: LiveData<List<Category>> = _categoryList

    /**
     * 加载首页数据（轮播图 + 分类）
     */
    fun loadHomeData() {
        viewModelScope.launch {
            // 加载轮播图
            launch {
                try {
                    val response = apiService.getBannerList(ApiRequest.build(BannerListParams(0)))
                    _bannerList.postValue(response.data ?: emptyList())
                } catch (_: Exception) { }
            }
            // 加载分类
            launch {
                try {
                    val response = apiService.getCategoryList(ApiRequest.build(CategoryListParams(0)))
                    _categoryList.postValue(response.data ?: emptyList())
                } catch (_: Exception) { }
            }
        }
    }
}