# DeepSeek 余额

一个用于查看 DeepSeek 开放平台人民币账户余额的 Android App，支持主屏幕桌面组件。

## 功能

- 显示 DeepSeek API 账户的人民币总余额
- 支持主屏幕桌面组件，无需打开 App 即可查看
- 桌面组件约每 30 分钟自动刷新，点击可立即刷新
- 支持选择本地图片作为桌面组件背景
- API Key 使用 Android Keystore 加密保存在设备本地
- 不依赖自建服务器，不发送常驻通知

## 使用

1. 安装并打开 App。
2. 输入 DeepSeek API Key，点击“保存并刷新”。
3. 在桌面添加“DeepSeek 余额”小组件。
4. 在 App 的“桌面组件背景”中可以选择自定义图片或恢复默认背景。

## 系统要求

- Android 8.0（API 26）或更高版本

## 构建

使用 Android Studio 打开项目，完成 Gradle 同步后运行：

```text
testDebugUnitTest
lintDebug
assembleDebug
```

生成的调试 APK 位于：

```text
app/build/outputs/apk/debug/app-debug.apk
```

## 安全说明

API Key 仅保存在用户设备上，并使用 Android Keystore 加密。App 不收集或上传 API Key，也不经过第三方服务端。
