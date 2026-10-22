#!/usr/bin/env bash
# ---------------------------------------------------------------------------
# 一键用模板创建新项目
#
#   ./scripts/init-project.sh com.yourcompany.myapp MyApp
#
# 做的事（对应 docs/NEW_PROJECT.md 第 1、2、3 步，自动完成）：
#   1. 把模板复制到 ../MyApp（排除 .git / build / .gradle / local.properties）
#   2. 包名替换：com.template.app -> com.yourcompany.myapp
#                com.template.core -> com.yourcompany.myapp.core
#   3. 移动源码目录到新包路径
#   4. 工程名 / 应用名替换
#   5. 跑 assembleDevDebug 验证
#
# 之后仍需手工做的（见 docs/NEW_PROJECT.md）：
#   - 用 Android Studio 替换应用图标（New -> Image Asset）
#   - 重新生成 release 签名（docs/SIGNING.md）
#   - 按业务改 base_url / H5_HOST
# ---------------------------------------------------------------------------
set -euo pipefail

usage() {
  echo "用法: $0 <new.package.name> <NewProjectName>"
  echo "示例: $0 com.yourcompany.myapp MyApp"
  exit 1
}

[[ $# -eq 2 ]] || usage

NEW_PKG="$1"            # com.yourcompany.myapp
NEW_NAME="$2"           # MyApp

# 基本合法性检查：包名至少两段、全小写字母数字点
if ! [[ "$NEW_PKG" =~ ^[a-z][a-z0-9_]+(\.[a-z][a-z0-9_]+)+$ ]]; then
  echo "错误: 包名不合法（示例: com.yourcompany.myapp）: $NEW_PKG"
  exit 1
fi
if ! [[ "$NEW_NAME" =~ ^[A-Za-z][A-Za-z0-9_]*$ ]]; then
  echo "错误: 工程名不合法（示例: MyApp）: $NEW_NAME"
  exit 1
fi

TEMPLATE_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
TARGET_DIR="$(dirname "$TEMPLATE_DIR")/$NEW_NAME"

if [[ -e "$TARGET_DIR" ]]; then
  echo "错误: 目标目录已存在: $TARGET_DIR"
  exit 1
fi

echo "==> 1/5 复制工程到 $TARGET_DIR"
mkdir -p "$TARGET_DIR"
(
  cd "$TEMPLATE_DIR"
  tar --exclude=.git --exclude=.gradle --exclude=build --exclude='*/build' \
      --exclude=.idea --exclude=.kotlin --exclude=.workbuddy \
      -cf - . | (cd "$TARGET_DIR" && tar -xf -)
)
# local.properties 不入库，但新项目需要 sdk.dir 才能构建验证。
# 只继承 sdk.dir：RELEASE_* / SIGN_SECRET 一律不带走，
# 否则新项目会"看起来配好了签名"，实际用的是模板的占位密钥。
if [[ -f "$TEMPLATE_DIR/local.properties" ]]; then
  grep -E '^[[:space:]]*sdk\.dir[[:space:]]*=' "$TEMPLATE_DIR/local.properties" \
    > "$TARGET_DIR/local.properties" 2>/dev/null || true
  echo "    已继承 sdk.dir（签名口令不继承）"
fi

cd "$TARGET_DIR"

echo "==> 2/5 替换包名（先 core 后 app，避免前缀互相拆坏）"
if command -v grep >/dev/null && command -v sed >/dev/null; then
  grep -rl "com\.template\.core" --include='*.kt' --include='*.kts' --include='*.xml' . \
    | xargs sed -i "s/com\.template\.core/${NEW_PKG}.core/g"
  grep -rl "com\.template\.app" --include='*.kt' --include='*.kts' --include='*.xml' . \
    | xargs sed -i "s/com\.template\.app/${NEW_PKG}/g"
else
  echo "错误: 需要 grep / sed（Windows 请在 Git Bash 中运行）"
  exit 1
fi

echo "==> 3/5 移动源码目录"
mkdir -p "app/src/main/java/$(echo "$NEW_PKG" | tr '.' '/')" \
         "core/src/main/java/$(echo "$NEW_PKG" | tr '.' '/')"
mv "app/src/main/java/com/template/app" \
   "app/src/main/java/$(echo "$NEW_PKG" | tr '.' '/')/"
mv "core/src/main/java/com/template/core" \
   "core/src/main/java/$(echo "$NEW_PKG" | tr '.' '/')/"
rm -rf app/src/main/java/com/template core/src/main/java/com/template
# 测试目录同步搬移
for testRoot in app/src/test app/src/androidTest core/src/test core/src/androidTest; do
  if [[ -d "$testRoot/java/com/template" ]]; then
    sub=$(ls "$testRoot/java/com/template")
    mkdir -p "$testRoot/java/$(echo "$NEW_PKG" | tr '.' '/')"
    mv "$testRoot/java/com/template/$sub" \
       "$testRoot/java/$(echo "$NEW_PKG" | tr '.' '/')/"
    rm -rf "$testRoot/java/com/template"
  fi
done

echo "==> 4/5 替换工程名与应用名"
sed -i "s/rootProject\.name = .*/rootProject.name = \"${NEW_NAME}\"/" settings.gradle.kts
sed -i "s/<string name=\"app_name\">.*<\/string>/<string name=\"app_name\">${NEW_NAME}<\/string>/" \
  app/src/main/res/values/strings.xml app/src/main/res/values-en/strings.xml
# flavor 的 resValue("string", "app_name", "TemplateAPP") 也一并替换
sed -i "s/\"app_name\", *\"TemplateAPP\"/\"app_name\", \"${NEW_NAME}\"/g" app/build.gradle.kts

echo "==> 4.1 同步文档里的工程名与包名"
# 文档不同步 = 新项目自带一套"教你把 TemplateAPP 再改一次名"的旧文档
for doc in ./*.md ./docs/*.md ./docs/ADR/*.md; do
  [[ -f "$doc" ]] || continue
  sed -i "s/TemplateAPP/${NEW_NAME}/g; s/com\.template\.core/${NEW_PKG}.core/g; s/com\.template\.app/${NEW_PKG}/g" "$doc"
done
# 模板本身的评测/审计记录对新项目没有意义，留着只会误导
rm -f ./项目评估.md ./项目复核报告.md ./模板审计报告_*.md

# 新项目必须自带"换密钥"的强制动作：
# 关掉占位密钥开关后，未换密钥时 verifyReleaseSigning 会失败（这是刻意设计）
sed -i 's/^template\.usePlaceholderReleaseKey=.*/template.usePlaceholderReleaseKey=false/' gradle.properties

echo "==> 5/5 构建验证（约 1-3 分钟）"
./gradlew :app:assembleDevDebug --console=plain -q

echo ""
echo "==> 收尾：初始化版本控制"
if command -v git >/dev/null 2>&1; then
  git init -q
  git add -A
  if git config user.email >/dev/null 2>&1; then
    git commit -q -m "chore: 由模板初始化 ${NEW_NAME}"
    echo "    已创建首个提交"
  else
    echo "    已 git init；未配置 user.name/user.email，跳过首次提交"
  fi
fi

echo ""
echo "完成！新项目位于: $TARGET_DIR"
echo "接下来（详见 docs/NEW_PROJECT.md）:"
echo "  1. 替换应用图标（Android Studio -> New -> Image Asset）"
echo "  2. 重新生成 release 签名（docs/SIGNING.md）—— 占位开关已自动置 false，换密钥前打包会失败"
echo "  3. 按业务修改 base_url / H5_HOST"
echo "  4. 删除用不到的示例页面（ui/search、ui/settings 等），并同步移动端导航"
