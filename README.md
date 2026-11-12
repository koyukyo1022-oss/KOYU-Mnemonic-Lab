# KOYU Mnemonic Lab 1.0

双端首版工程：Windows 桌面程序（Python/Tkinter）+ Android 原生工程（Java）。

## 当前版本的功能
- 理想化随机空间碰撞实验：可设置位数、试验次数，记录首次碰撞。
- 理论生日悖论概率估算。
- BTC / ETH / BNB / SOL 的**模拟标识符**生成。
- CSV 导出实验记录。
- 本地运行，不联网、不扫描链上地址、不收集助记词或私钥。

## 重要边界
本版本的 `BTC-TEST-SIM`、`ETH-TEST-SIM`、`BNB-TEST-SIM`、`SOL-TEST-SIM` 是用于 UI 和统计测试的模拟标识符，**不是有效链上地址，不对应真实私钥，也不能收发资产**。当前首版不实现真实主网钱包碰撞或余额筛查。

## Windows 10
1. 安装 Python 3.10 或更新版本。
2. 解压本工程。
3. 进入 `windows` 文件夹，运行：
   `python koyu_lab.py`
4. 如需打包 EXE：
   `python -m pip install pyinstaller`
   `pyinstaller --onefile --windowed --name KOYU-Mnemonic-Lab koyu_lab.py`
5. EXE 位于 `windows/dist/`。

## Android 11 / Android Studio
1. 在 Windows 电脑安装 Android Studio。
2. 选择 Open，打开本工程的 `android` 文件夹。
3. 等待 Gradle Sync 完成。
4. 连接 Android 手机并开启 USB 调试，或使用模拟器。
5. 点击 Run。生成 APK：Build > Build Bundle(s) / APK(s) > Build APK(s)。
6. APK 通常位于 `android/app/build/outputs/apk/debug/app-debug.apk`。

工程使用 Android Gradle Plugin 8.7.3、Gradle 8.9、JDK 17、compileSdk 35。
首次构建需要网络下载 Android Gradle 插件和 Android SDK；本交付包不包含 SDK/JDK。

## 数学模型
对 b 位均匀随机空间，做 n 次独立抽样，至少出现一次重复的近似概率：
`P ≈ 1 - exp(-n(n-1) / (2 * 2^b))`
这是生日悖论近似值。程序实际实验会检测当前运行中的重复值。

## 目录
- `windows/koyu_lab.py`：Windows 桌面程序
- `android/`：Android 原生工程

## 云端一键构建（适合只用手机操作）
工程包含 `.github/workflows/build.yml`。将整个文件夹上传到 GitHub 仓库后：
1. 打开仓库的 Actions 页面。
2. 选择 `Build KOYU Mnemonic Lab`。
3. 点击 Run workflow（或推送到 main 后自动构建）。
4. 构建完成后，在该次运行页面底部的 Artifacts 下载 Android APK 和 Windows EXE。

首次使用 GitHub Actions 需要允许仓库运行 Actions。APK 是 debug 测试版，安装时 Android 可能提示允许该来源安装应用。


## 手机端完成 APK / Windows EXE 构建（无需本地电脑）
本工程提供 GitHub Actions 自动构建配置：`.github/workflows/build.yml`。

1. 在 GitHub 创建一个空仓库，例如 `KOYU-Mnemonic-Lab`。
2. 用手机浏览器打开仓库，选择 Add file → Upload files，把本 ZIP 解压后的**全部文件和文件夹**上传到仓库根目录（包括隐藏目录 `.github`）。
3. 提交到 `main` 分支。进入仓库顶部的 **Actions**。
4. 打开 `Build KOYU Mnemonic Lab`，等待 Android 和 Windows 两个任务完成。
5. 在本次运行页面的 Artifacts 区域，分别下载 `KOYU-Mnemonic-Lab-Android-APK` 和 `KOYU-Mnemonic-Lab-Windows`。
6. Android 下载的 artifact ZIP 解压后安装 `app-debug.apk`；Windows 下载的 artifact ZIP 解压后运行 `KOYU-Mnemonic-Lab.exe`。

首次构建需 GitHub Actions 可用并允许仓库运行 Actions。此方式生成的是 Android debug APK 和 Windows x64 EXE。

## 当前功能边界
多链模块目前只生成带 `TEST-SIM` 标记的随机模拟标识符，用于本地多链实验界面与 CSV 流程验证；它们不是 BTC、Ethereum、BNB Chain 或 Solana 的真实测试网地址。当前版本不含真实助记词到链地址派生，也不含链上 RPC 余额查询。不要向程序输入真实助记词、私钥或钱包密码。
