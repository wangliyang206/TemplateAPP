# 打包环境与签名

> ## ⚠ 当前状态：模板阶段，release 用"占位密钥"
>
> 本模板自带 `app/keystore/release.jks`（`CN=beanSproutAssistant`，RSA 2048）。
> 这个密钥**曾经出现在公开的 git 历史里**，等于人人都有，只能当占位用。
>
> - `gradle.properties` 里 `template.usePlaceholderReleaseKey=true` → 允许它工作，`verifyReleaseSigning` 会打印告警。
> - 只要把它改成 `false`，而 release 密钥指纹仍是占位密钥的，`verifyReleaseSigning` **直接失败**。
>
> **新项目第一件事：重新生成密钥 + 把开关改成 false。**流程见 [§七](#七新项目替换占位密钥必须做)。

## 一、环境（flavor）有 3 种

维度名 `env`，定义在 `app/build.gradle.kts` 的 `productFlavors`。

| flavor | applicationId | 应用名 | 接口地址 | 数据源 | 用途 |
|---|---|---|---|---|---|
| `dev` | `com.template.app.dev` | TemplateAPP-Dev | `http://www.dagongji.xin/`（可明文） | Mock | 日常开发、UI 联调 |
| `staging` | `com.template.app.staging` | TemplateAPP-Staging | `https://api-staging.template.com/` | Mock | 测试同学验证、预发演练 |
| `prod` | `com.template.app` | TemplateAPP | `https://api.template.com/` | 真实接口 | 应用商店正式包 |

三者 `applicationIdSuffix` 不同，**可以同时装在一台手机上**，互不覆盖。

> 注意：`debug` buildType 还额外带 `applicationIdSuffix = ".debug"`，
> 所以 `devDebug` 的包名是 `com.template.app.dev.debug` 而不是 `com.template.app.dev`。
> 好处是同一 flavor 的 debug 与 release 也能共存；`aapt2 dump badging` 可验。
> 完整 6 变体包名表见 [README「环境与构建」](../README.md#环境与构建)。

再叠加 buildType（`debug` / `release`），一共 **6 个构建变体**：

| 任务 | 说明 |
|---|---|
| `assembleDevDebug` | 开发包，可调试，签团队 debug 证书 |
| `assembleDevRelease` | 开发环境的混淆包，一般不用于分发 |
| `assembleStagingDebug` | 测试环境的调试包 |
| `assembleStagingRelease` | **测试分发包**（需要 release 签名） |
| `assembleProdDebug` | 线上配置的调试包，排查线上问题用 |
| `assembleProdRelease` | **正式上架包**（需要 release 签名） |

> 关键点：flavor 决定"连哪个后端、叫什么名字、包名是什么"，buildType 决定"是否混淆、用什么密钥签名"。

## 二、签名：两种变体，两套密钥

| 变体 | 密钥文件 | 口令来源 | 是否入库 | 缺失时 |
|---|---|---|---|---|
| 所有 `*Debug` | `app/keystore/debug.jks` | 代码内默认值（`android` / `androiddebugkey`） | ✅ **入库**（标准 Android Debug 证书，非机密） | 回退到本机 `~/.android/debug.keystore` 并打警告 |
| 所有 `*Release` | `app/keystore/release.jks`（**当前是模板占位密钥**） | `local.properties` 或 CI 环境变量 `RELEASE_*` | ⚠ 模板阶段文件不入库，仅本机 `local.properties` 指向 | 产出未签名 APK，`verifyReleaseSigning` 直接失败 |

> 正式项目里 release 密钥**绝不入库**。当前之所以还在用 `app/keystore/release.jks`，
> 是因为这个项目只当模板自用，方便随时出一个已签名包做演示/联调。见顶部状态说明。

当前占位密钥的实际信息（`keytool -list -v` 实测）：

| 项 | 值 |
|---|---|
| 别名 | `beansproutassistant` |
| 证书主体 | `CN=beanSproutAssistant` |
| 密钥规格 | RSA 2048（满足应用商店 ≥2048 要求） |
| 有效期至 | 2049-04-22 |
| SHA-256 | `D5:BC:46:0C:72:2C:98:01:9E:15:16:77:A6:EA:BC:FB:E8:8D:F3:92:BA:C4:22:2F:C9:56:01:D5:80:1C:85:91` |
| 口令 | 见本机 `local.properties`（不入库，也不写进文档） |

配置项（`app/build.gradle.kts` 顶部）：

```properties
# local.properties（本地）或 CI Secrets（流水线）
RELEASE_STORE_FILE=app/keystore/release.jks
RELEASE_STORE_PASSWORD=***
RELEASE_KEY_ALIAS=***
RELEASE_KEY_PASSWORD=***

# 可选：想用别的 debug 密钥时覆盖
# DEBUG_STORE_FILE=app/keystore/debug.jks
# DEBUG_STORE_PASSWORD=android
# DEBUG_KEY_ALIAS=androiddebugkey
# DEBUG_KEY_PASSWORD=android
```

## 三、为什么 debug 密钥必须入库

如果不管，每个开发者第一次构建时 AGP 会**在本机**生成 `~/.android/debug.keystore`。
于是同一个包名在不同机器上签名不同，表现为：

- 同事 A 的包装不上同事 B 的手机：`INSTALL_FAILED_UPDATE_INCOMPATIBLE`
- 测试机上的包必须"先卸载再装"，覆盖安装永远失败
- 按「包名 + 签名指纹」校验的第三方能力（微信登录/分享、地图、推送、一键登录）在别人电脑上突然失效

所以本项目把 `debug.jks` 提交进仓库（`.gitignore` 里对它做了白名单例外）。
它是 Android 官方格式的标准 debug 证书，泄露不构成业务风险——**真正要保护的是 release 密钥**。

如果公司安全规范连 debug 证书也不允许入库，就走"外部分发 + 覆盖路径"：
把 `debug.jks` 放进内部制品库，每个人在 `local.properties` 配 `DEBUG_STORE_FILE` 指向它即可。

## 四、多机一致性怎么校验

### 4.1 看配置（最快，构建前就能查）

AGP 自带 `signingReport`，会列出每个变体用的密钥指纹：

```bash
./gradlew :app:signingReport
```

在**任意两台电脑**上执行，逐项比对：

| 变体 | 期望 SHA-256 |
|---|---|
| `devDebug` / `stagingDebug` / `prodDebug` / `*DebugAndroidTest` | `74:5E:5D:89:E7:A2:64:2A:92:69:92:0B:8A:5B:78:42:B1:03:30:0A:40:C3:FE:1B:6E:1D:73:40:75:2A:E1:2A` |
| `*Release`（模板阶段） | `D5:BC:46:0C:72:2C:98:01:9E:15:16:77:A6:EA:BC:FB:E8:8D:F3:92:BA:C4:22:2F:C9:56:01:D5:80:1C:85:91`（占位密钥） |
| `*Release`（新项目） | 团队统一的那一个；未配置时为 `Config: null` |

指纹一致 = 可以互相覆盖安装 = 第三方 SDK 校验通过。

> 本仓库已实测：所有 `*Debug` 变体的 `Store` 都指向 `app/keystore/debug.jks`、
> `Alias` 为 `androiddebugkey`、SHA-256 与上表完全一致，证书有效期至 **2046-04-20**。
> `prodRelease` 指向 `app/keystore/release.jks`、`Alias` 为 `beansproutassistant`，
> 即上表的占位密钥指纹。

### 4.2 看真实产物（最权威，构建后）

`signingReport` 只反映 Gradle 配置，最终以 APK 里的证书为准：

```bash
./gradlew :app:assembleDevDebug

# 路径换成你的 SDK 位置
$ANDROID_HOME/build-tools/35.0.0/apksigner verify --print-certs -v \
    app/build/outputs/apk/dev/debug/app-dev-debug.apk
```

debug 包实测输出（节选）：

```
Verified using v1 scheme (JAR signing): false
Verified using v2 scheme (APK Signature Scheme v2): true
Signer #1 certificate DN: C=US, O=Android, CN=Android Debug
Signer #1 certificate SHA-256 digest: 745e5d89e7a2642a9269920b8a5b7842b103300a40c3fe1b6e1d7340752ae12a
Signer #1 key size (bits): 1024
```

release 包实测输出（`assembleProdRelease` → `app-prod-release.apk`，注意**没有** `-unsigned` 后缀）：

```
Verified using v1 scheme (JAR signing): false
Verified using v2 scheme (APK Signature Scheme v2): true
Signer #1 certificate DN: CN=beanSproutAssistant
Signer #1 certificate SHA-256 digest: d5bc460c722c98019e151677a6eabcfbe88df392bac4222fc95601d5801c8591
Signer #1 key size (bits): 2048
```

几点读法：

- `SHA-256 digest` 去掉冒号就是上表的指纹，**一致即通过**。
- **debug 包只有 v2 是正常的**：v1（JAR 签名）只服务 API < 24 的设备，本项目 `minSdk = 24`，AGP 自动跳过。
- **release 包其实也有 v1**（`app/build.gradle.kts` 里 `enableV1Signing = true`，APK 内能看到 `META-INF/CERT.SF`），
  但 `apksigner` 在 `minSdk ≥ 24` 时**跳过 v1 校验**，所以报 `false`——这是"不适用"，不是"签名缺失"。
  想确认 v1 存在：加 `--min-sdk-version 21` 再验一次，会变成 `true`。
- **1024 位是 AOSP debug 证书的固有规格**，仅用于调试无妨；应用商店要求 release 密钥 **≥ 2048 位**。

### 4.3 只看文件

```bash
keytool -list -v -keystore app/keystore/debug.jks -storepass android | grep SHA256
```

## 五、常见故障对照

| 现象 | 原因 | 处理 |
|---|---|---|
| `INSTALL_FAILED_UPDATE_INCOMPATIBLE` | 同包名、签名不同 | 统一密钥；临时可卸载重装 |
| 应用商店拒绝更新 / 老用户装不上新版 | 换了 release 密钥 | 找回原密钥，否则只能换包名重新上架 |
| 微信/地图/推送在别人电脑上失效 | 后台按签名指纹登记，debug 签名不一致 | 用团队 debug 证书 |
| `signingReport` 里 `*Release` 显示 `Config: null` | 本机没配 `RELEASE_*` | 正常现象；要出 release 包才需补配置 |
| 构建日志出现 `[signing] 未找到团队 debug 密钥` | `app/keystore/debug.jks` 丢失（如旧 `.gitignore` 生效时 clone 的） | `git checkout app/keystore/debug.jks`，或 `git pull` 后重新同步 |
| `verifyReleaseSigning` 失败，提示"缺少 release 签名配置" | 没配 `RELEASE_*` | 按第二节补齐，或从密码管理器取 |
| `verifyReleaseSigning` 失败，提示"仍是模板占位密钥" | 开关已关，但密钥没换 | 按 §七 重新生成；CI 就是靠这条拦住"复制模板忘换签名" |
| `verifyReleaseSigning` 通过但打印 `⚠ 模板占位密钥` | `template.usePlaceholderReleaseKey=true` | 模板阶段属正常；发版前按 §七 换掉 |
| `assembleProdRelease` 产出 `-unsigned.apk` | 没配 release 签名 | 同上；CI 会因 `verifyReleaseSigning` 拦住 |

## 六、release 密钥的保管规矩

1. **一处生成，多处备份**：至少 2 个离线位置（密码管理器 + 加密移动介质），丢了就无法更新已上架应用。
2. **只给需要发版的人**：日常开发不需要 release 密钥。
3. **CI 用 Secrets**：`RELEASE_KEYSTORE_BASE64` + 三个口令，见 `.github/workflows/android-ci.yml`。
4. **每季度轮换一次口令**（密钥本身不建议轮换）。

## 七、新项目替换占位密钥（必须做）

模板自带的 `release.jks` 指纹是公开的，任何拿到本仓库的人都能用它签出"看起来是你家的包"。
下面四步是开新项目时的强制动作，**漏掉第 2 步 CI 会直接失败**（这是故意设计的）。

**1. 生成自己的密钥**

```bash
keytool -genkeypair -v -keystore app/keystore/release.jks -alias youralias \
        -keyalg RSA -keysize 2048 -validity 10000
```

> 生成前先把旧的 `release.jks` 备份改名（例如 `release.jks.template.bak`），
> 免得新密钥生成失败时把唯一副本弄丢。

**2. 关掉占位开关**

`gradle.properties`：

```properties
template.usePlaceholderReleaseKey=false
```

此时如果密钥还是旧的那把，`./gradlew verifyReleaseSigning` 会报
"当前 release 密钥仍是模板占位密钥…已中止"。这一步就是防止"以为换了其实没换"。

**3. 更新 `local.properties`**

```properties
RELEASE_STORE_FILE=app/keystore/release.jks
RELEASE_STORE_PASSWORD=新store口令
RELEASE_KEY_ALIAS=youralias
RELEASE_KEY_PASSWORD=新key口令
```

**4. 复核**

```bash
./gradlew :app:verifyReleaseSigning     # 应输出 [signing] release 使用自有密钥，SHA-256: ...
./gradlew :app:assembleProdRelease      # 应产出 app-prod-release.apk（无 -unsigned）
```

`verifyReleaseSigning` 打印的指纹要和 `keytool -list -v` 看到的**完全一致**，
并把它登记到 `docs/CHECKLIST.md` 或团队 wiki，方便其他人核对。

> 如果这把占位密钥已经签过某个**已上架**应用，那不能直接换——应用商店只认原签名，
> 需走商店的"密钥重置/上传密钥"流程。本模板场景不存在这个问题。
