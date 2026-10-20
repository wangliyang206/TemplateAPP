#!/usr/bin/env bash
# ---------------------------------------------------------------------------
# 新建页面脚手架
#
#   ./scripts/new-feature.sh profile
#
# 生成（遵循模板统一范式，不需要手抄）：
#   ui/<feature>/<Feature>Fragment.kt
#   ui/<feature>/<Feature>ViewModel.kt
#   ui/<feature>/<Feature>Repository.kt
#   res/layout/fragment_<feature>.xml
#
# 生成后还需要手动做一步：在 mobile_navigation.xml 里注册目的地。
# ---------------------------------------------------------------------------
set -euo pipefail

if [[ $# -lt 1 ]]; then
  echo "用法: $0 <feature-name>    例: $0 profile"
  exit 1
fi

RAW_NAME="$1"
# profile_detail -> ProfileDetail
FEATURE=$(echo "$RAW_NAME" | awk -F'_' '{for(i=1;i<=NF;i++){printf toupper(substr($i,1,1)) substr($i,2)}}')
SNAKE=$(echo "$RAW_NAME" | tr '[:upper:]' '[:lower:]')

# 包名从 build.gradle.kts 的 namespace 读取，改名后本脚本无需修改
read_namespace() {
  grep -m1 -oE 'namespace[[:space:]]*=[[:space:]]*"[^"]+"' "$1" \
    | sed -E 's/.*"([^"]+)".*/\1/'
}

APP_PKG=$(read_namespace app/build.gradle.kts)
CORE_PKG=$(read_namespace core/build.gradle.kts)

if [[ -z "$APP_PKG" || -z "$CORE_PKG" ]]; then
  echo "无法从 app/core 的 build.gradle.kts 读取 namespace" >&2
  exit 1
fi

APP_PKG_DIR=${APP_PKG//.//}
PKG_DIR="app/src/main/java/${APP_PKG_DIR}/ui/${SNAKE}"
LAYOUT_DIR="app/src/main/res/layout"

if [[ -d "$PKG_DIR" ]]; then
  echo "目录已存在: $PKG_DIR"
  exit 1
fi

mkdir -p "$PKG_DIR"

cat > "${PKG_DIR}/${FEATURE}Repository.kt" <<EOF
package ${APP_PKG}.ui.${SNAKE}

import ${APP_PKG}.data.api.ApiService
import ${CORE_PKG}.base.BaseRepository
import ${CORE_PKG}.network.ApiRequestFactory
import ${CORE_PKG}.network.BaseResponse
import javax.inject.Inject

/**
 * ${FEATURE} 数据源。所有网络调用都走这里，ViewModel 不允许直接依赖 ApiService。
 */
class ${FEATURE}Repository @Inject constructor(
    private val apiService: ApiService,
    private val requestFactory: ApiRequestFactory
) : BaseRepository() {

    suspend fun load(): List<String> = apiCall {
        safeApi {
            // TODO 替换为真实接口：
            //   apiService.getXxx(requestFactory.build(XxxParams(...)))
            BaseResponse(version = 1, errorInfo = null, data = emptyList())
        }
    }
}
EOF

cat > "${PKG_DIR}/${FEATURE}ViewModel.kt" <<EOF
package ${APP_PKG}.ui.${SNAKE}

import ${CORE_PKG}.base.BaseViewModel
import ${CORE_PKG}.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class ${FEATURE}ViewModel @Inject constructor(
    private val repository: ${FEATURE}Repository
) : BaseViewModel() {

    private val _state = MutableStateFlow<UiState<List<String>>>(UiState.Idle)
    val state: StateFlow<UiState<List<String>>> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        _state.launchWithState { repository.load() }
    }
}
EOF

cat > "${PKG_DIR}/${FEATURE}Fragment.kt" <<EOF
package ${APP_PKG}.ui.${SNAKE}

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import ${APP_PKG}.R
import ${APP_PKG}.databinding.Fragment${FEATURE}Binding
import ${CORE_PKG}.base.BaseFragment
import ${CORE_PKG}.ext.collectFlow
import ${CORE_PKG}.state.UiState
import ${CORE_PKG}.state.asString
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ${FEATURE}Fragment : BaseFragment<Fragment${FEATURE}Binding>() {

    private val viewModel: ${FEATURE}ViewModel by viewModels()

    override fun getTitleBarId(): Int = R.id.titleBar

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        Fragment${FEATURE}Binding.inflate(inflater, container, false)

    override fun initObserver() {
        viewLifecycleOwner.collectFlow(viewModel.state, ::render)
    }

    private fun render(state: UiState<List<String>>) {
        when (state) {
            is UiState.Idle -> Unit
            is UiState.Loading -> binding.stateLayout.showLoading()
            is UiState.Empty -> binding.stateLayout.showEmpty()
            is UiState.Error -> binding.stateLayout.showError(
                message = state.message.asString(requireContext()),
                onRetry = viewModel::load
            )
            is UiState.Success -> binding.stateLayout.showContent()
        }
    }
}
EOF

cat > "${LAYOUT_DIR}/fragment_${SNAKE}.xml" <<EOF
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical">

    <LinearLayout
        android:id="@+id/titleBar"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:background="@color/white"
        android:gravity="center"
        android:paddingVertical="12dp">

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="${FEATURE}"
            android:textColor="@color/text_primary"
            android:textSize="@dimen/text_title"
            android:textStyle="bold" />
    </LinearLayout>

    <${CORE_PKG}.widget.StatefulLayout
        android:id="@+id/stateLayout"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1">

        <FrameLayout
            android:layout_width="match_parent"
            android:layout_height="match_parent" />
    </${CORE_PKG}.widget.StatefulLayout>
</LinearLayout>
EOF

echo "已生成 ${FEATURE} 页面："
echo "  ${PKG_DIR}/${FEATURE}Fragment.kt"
echo "  ${PKG_DIR}/${FEATURE}ViewModel.kt"
echo "  ${PKG_DIR}/${FEATURE}Repository.kt"
echo "  ${LAYOUT_DIR}/fragment_${SNAKE}.xml"
echo ""
echo "下一步：在 app/src/main/res/navigation/mobile_navigation.xml 注册 fragment 目的地。"
