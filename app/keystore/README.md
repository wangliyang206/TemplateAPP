# 签名目录

本目录存放**两套完全不同的密钥**，处理方式相反，别搞混：

| 文件 | 是否入库 | 说明 |
|---|---|---|
| `debug.jks` | ✅ **入库**（`.gitignore` 白名单例外） | 团队统一的 debug 证书，口令 `android` / `androiddebugkey` |
| `release.jks` | ⚠ **模板阶段仅本机使用，不入库** | 当前是**模板占位密钥**（`CN=beanSproutAssistant`），曾公开于 git 历史，只能用于内部演示 |
| 新项目的 `release.jks` | ❌ **绝不入库** | 由 `local.properties` / CI Secrets 指向 |

完整原理与排错见 **[../../docs/SIGNING.md](../../docs/SIGNING.md)**。

## ⚠ release.jks 是占位密钥

它现在能让 `assembleProdRelease` 出已签名包，方便模板自测；但**任何拿到本仓库的人都能拿它签出
看起来是你家的包**。所以模板加了一道闸门：

- `gradle.properties` 里 `template.usePlaceholderReleaseKey=true` → 允许使用，`verifyReleaseSigning` 打印告警
- 改成 `false` 且密钥指纹没变 → `verifyReleaseSigning` **直接失败**

**新项目开工第一件事**：改开关为 `false` + 重新生成密钥，步骤见
[SIGNING.md §七](../../docs/SIGNING.md#七新项目替换占位密钥必须做)。

## 为什么 debug.jks 要入库

不管它的话，AGP 会在每台机器上各自生成 `~/.android/debug.keystore`，于是同一包名在不同电脑上
签名不同，导致：

- 同事之间覆盖安装报 `INSTALL_FAILED_UPDATE_INCOMPATIBLE`
- 微信登录/分享、地图、推送、一键登录等按「包名 + 签名」登记的 SDK 在别人电脑上失效

`debug.jks` 是标准 Android Debug 证书（`CN=Android Debug`，RSA 1024，有效期至 2046-04-20），
不含业务机密，允许随仓库分发。**真正需要保护的是 release 密钥。**

如果公司规范不允许 debug 证书入库，就把它放到内部制品库，各人用 `local.properties` 覆盖：

```properties
DEBUG_STORE_FILE=D:/secrets/debug.jks
DEBUG_STORE_PASSWORD=android
DEBUG_KEY_ALIAS=androiddebugkey
DEBUG_KEY_PASSWORD=android
```

## release 配置

在项目根目录 `local.properties` 中追加（该文件不入库），相对路径以项目根目录为基准：

```properties
RELEASE_STORE_FILE=app/keystore/release.jks
RELEASE_STORE_PASSWORD=你的store口令
RELEASE_KEY_ALIAS=你的alias
RELEASE_KEY_PASSWORD=你的key口令
```

CI 里注入同名环境变量即可，`app/build.gradle.kts` 会自动读取。

## 没有 release 密钥时

- 所有 `*Debug` 变体照常可用（签团队 `debug.jks`）
- `assembleProdRelease` 产出未签名包
- `./gradlew verifyReleaseSigning` 直接失败，用于 CI 卡口

## 生成 release 密钥

```bash
keytool -genkeypair -v -keystore app/keystore/release.jks -alias app \
        -keyalg RSA -keysize 2048 -validity 10000
```

生成后立即做两件事：① 放进团队密码管理器；② 存一份离线备份。丢了就无法更新已上架应用。

## 校验一致性

```bash
./gradlew :app:signingReport
```

所有 `*Debug` 变体的 SHA-256 应为
`74:5E:5D:89:E7:A2:64:2A:92:69:92:0B:8A:5B:78:42:B1:03:30:0A:40:C3:FE:1B:6E:1D:73:40:75:2A:E1:2A`。
