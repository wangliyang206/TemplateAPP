#!/usr/bin/env bash
# ---------------------------------------------------------------------------
# 文档与代码一致性检查
#
#   ./scripts/check-docs.sh
#
# 做的事：把 *.md 里所有「反引号包裹的代码路径」抽出来，逐个验证是否真实存在。
# 为什么需要它：文档比代码更容易腐化——代码删了，文档里那行还在教人用（本仓库踩过，
# 例如已删除的 MMKVManager / ui/splash / requestPhone 都还留在文档里）。
#
# 匹配规则（避免误报，文档常写简写路径）：
#   1. 先按仓库根目录的相对路径找；
#   2. 找不到时按「路径后缀」匹配任意已跟踪文件（`core/ext/GlideExt.kt` 能命中真实文件）；
#   3. 跳过裸扩展名（`.jks`、`.pro`）、占位路径（含 xxx / ...）与命令行片段（含空格）；
#   4. 跳过「描述历史」的文档：CHANGELOG、模板审计报告、docs/ADR ——
#      它们本来就要写"哪个文件被删了"，属于合法引用。
#
# 退出码 0 = 全部存在；1 = 有缺失（可直接接进 CI）
# ---------------------------------------------------------------------------
set -uo pipefail
cd "$(dirname "${BASH_SOURCE[0]}")/.." || exit 1

if [ -t 1 ]; then
  RED=$'\033[31m'; GREEN=$'\033[32m'; YELLOW=$'\033[33m'; RESET=$'\033[0m'
else
  RED=""; GREEN=""; YELLOW=""; RESET=""
fi

DOCS=$(ls ./*.md ./docs/*.md 2>/dev/null | grep -vE '(^|/)(CHANGELOG\.md|模板审计报告.*\.md)$')
if [ -z "$DOCS" ]; then
  echo "${YELLOW}未找到任何 md 文档${RESET}"
  exit 0
fi

# 已跟踪 + 未跟踪但未被忽略的文件（排除 build 产物，靠 .gitignore）
INDEX_FILE=$(mktemp)
trap 'rm -f "$INDEX_FILE"' EXIT
git ls-files --cached --others --exclude-standard > "$INDEX_FILE" 2>/dev/null || find . -type f -not -path "*/build/*" > "$INDEX_FILE"

PATHS=$(grep -ohE '`[^`]+`' $DOCS \
  | tr -d '`' \
  | grep -E '\.(kt|kts|xml|sh|jks|pro|json|toml|yml|example)$' \
  | grep -vE '^\.' \
  | grep -vE 'xxx|\.\.\.' \
  | grep -vE '[ <>*]' \
  | sort -u)

MISSING=0
CHECKED=0
for p in $PATHS; do
  CHECKED=$((CHECKED + 1))
  if grep -qxF "$p" "$INDEX_FILE"; then continue; fi
  # 后缀匹配：兼容文档里的简写路径
  if grep -qE "/${p//./\\.}$" "$INDEX_FILE"; then continue; fi
  echo "${RED}MISSING${RESET}: $p"
  MISSING=$((MISSING + 1))
done

if [ "$MISSING" -eq 0 ]; then
  echo "${GREEN}OK${RESET}: 文档引用的 $CHECKED 个代码路径全部存在"
  exit 0
fi

echo ""
echo "${RED}共 $MISSING 处文档引用了不存在的文件${RESET}（检查 $CHECKED 处）"
echo "处理方式：删掉该段描述，或把文档改成真实路径 —— 不要留「以前有、现在没有」的说明"
exit 1
