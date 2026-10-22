#!/usr/bin/env bash
# ---------------------------------------------------------------------------
# 统计测试用例数（文档里的"当前 N 个用例"一律以本脚本输出为准）
#
#   ./scripts/test-count.sh
#
# 为什么不从 build/test-results 读：那份结果包含每个变体的重复条目，
# 且没跑过测试时不存在。数源码里的 @Test 更稳定，也不依赖构建。
# ---------------------------------------------------------------------------
set -uo pipefail
cd "$(dirname "${BASH_SOURCE[0]}")/.." || exit 1

count() {
  local dir="$1"
  if [ -d "$dir" ]; then
    grep -rho "@Test" "$dir" --include=*.kt 2>/dev/null | wc -l | tr -d ' '
  else
    echo 0
  fi
}

CORE=$(count "core/src/test")
APP=$(count "app/src/test")
ANDROID=$(count "app/src/androidTest")
CORE_ANDROID=$(count "core/src/androidTest")

echo "core 单元测试      : $CORE"
echo "app  单元测试      : $APP"
echo "app  仪器化测试    : $ANDROID"
echo "core 仪器化测试    : $CORE_ANDROID"
echo "---------------------------------"
echo "JVM 用例合计       : $((CORE + APP))"
echo "需真机的用例合计   : $((ANDROID + CORE_ANDROID))"
