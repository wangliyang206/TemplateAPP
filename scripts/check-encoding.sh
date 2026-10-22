#!/usr/bin/env bash
# ---------------------------------------------------------------------------
# 文件编码检查
#
#   ./scripts/check-encoding.sh
#
# 做的事：检查源文件是否带 UTF-8 BOM（\xEF\xBB\xBF）。
# 为什么需要它：BOM 会让 Kotlin 的 `@file:Suppress(...)` **静默失效**。
#   本仓库踩过——MockApiService.kt 首行是 BOM，文件头写的 `@file:Suppress("MagicNumber")`
#   完全没有生效，干净构建下 detekt 报 8 条 MagicNumber；
#   而去掉 BOM 后同样的代码立刻通过。这类问题在增量构建里看不出来（任务 UP-TO-DATE），
#   等 CI 干净构建才炸，排查成本很高，所以直接做成门禁。
#
# 排除目录：build / .git / .gradle / .idea / .workbuddy
# 退出码 0 = 无 BOM；1 = 存在带 BOM 的源文件（可直接接进 CI）
# ---------------------------------------------------------------------------
set -uo pipefail
cd "$(dirname "${BASH_SOURCE[0]}")/.." || exit 1

if [ -t 1 ]; then
  RED=$'\033[31m'; GREEN=$'\033[32m'; RESET=$'\033[0m'
else
  RED=""; GREEN=""; RESET=""
fi

# shellcheck disable=SC2207
FILES=($(git ls-files 2>/dev/null || find . -type f))
[ ${#FILES[@]} -eq 0 ] && { echo "${GREEN}OK${RESET}: 无需检查"; exit 0; }

OFFENDERS=()
CHECKED=0
for f in "${FILES[@]}"; do
  case "$f" in
    build/*|*/build/*|.git/*|.gradle/*|.idea/*|.workbuddy/*) continue ;;
    *.kt|*.kts|*.xml|*.md|*.toml|*.yml|*.yaml|*.pro|*.properties|*.sh|*.py) ;;
    *) continue ;;
  esac
  [ -f "$f" ] || continue
  CHECKED=$((CHECKED + 1))
  if [ "$(head -c 3 "$f" | od -An -tx1 | tr -d ' \n')" = "efbbbf" ]; then
    OFFENDERS+=("$f")
  fi
done

if [ ${#OFFENDERS[@]} -eq 0 ]; then
  echo "${GREEN}OK${RESET}: 检查 $CHECKED 个文件，无 BOM"
  exit 0
fi

echo "${RED}发现 ${#OFFENDERS[@]} 个文件带 UTF-8 BOM${RESET}（这会让 @file:Suppress 失效）："
for f in "${OFFENDERS[@]}"; do
  echo "  $f"
done
echo ""
echo "处理方式："
echo "  python -c \"import sys; p=sys.argv[1]; d=open(p,'rb').read(); open(p,'wb').write(d[3:] if d[:3]==b'\\xef\\xbb\\xbf' else d)\" <文件>"
echo "  或在 IDE 里「文件编码 → UTF-8（无 BOM）→ 转换」后保存"
exit 1
