# CoinBridge 🌉🪙

[![Platform](https://img.shields.io/badge/Platform-Android%20%7C%20Android%20TV-green.svg)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-purple.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-blue.svg)](https://developer.android.com/jetpack/compose)
[![License](https://img.shields.io/badge/License-Proprietary-red.svg)](#)

**CoinBridge** is a modern, offline-first currency and cryptocurrency converter built with Jetpack Compose, Room Database, and Material 3 design. Designed for international travelers, finance professionals, and everyday calculations with complete multi-screen support across smartphones, tablets, and Android TV.

---

## ✨ Features

- 🌐 **170+ Currencies & Crypto**: Real-time conversion across all global fiat currencies plus leading cryptocurrencies (Bitcoin, Ethereum, Solana, XRP).
- ⚡ **100% Offline Mode**: Local Room caching ensures full functionality even without cellular data or Wi-Fi.
- 🔄 **Multi-Target Conversion**: Simultaneously convert a base currency into multiple targets at once.
- 🍽️ **Travel Bill & Tip Splitter**: Calculate restaurant bills, split per person, and convert directly to your home currency.
- 📈 **Interactive Rate Trends**: Interactive rate trend charts across 7D, 30D, 90D, and 1-year timeframes with low/avg/high benchmarks.
- 📏 **Physical Unit Converter**: Comprehensive conversion across Weight, Length, Volume, Temperature, Speed, and Area.
- 🔒 **Biometric Privacy & Security**: Protect financial logs and calculation history with Fingerprint / Face Unlock.
- 📳 **Shake-to-Swap**: Shake your phone with adjustable G-force sensitivity to instantly swap base and target currencies.
- 📺 **Android TV / Leanback**: Full D-pad remote navigation with focus rings and dual-column TV layouts.

---

## 🛠️ Tech Stack & Architecture

- **UI Framework**: Jetpack Compose (Material 3)
- **Architecture**: MVVM + Repository pattern with Kotlin Coroutines & StateFlow
- **Local Storage**: Room Database + SQLite for offline exchange rate cache & conversion logs
- **Networking**: Retrofit 2 + OkHttp 3 + Kotlin Serialization / Moshi
- **Biometrics**: AndroidX BiometricPrompt API
- **Hardware Remote**: KeyEvent D-pad navigation handling & Leanback intent filters

---

## 🚀 Building & Running

### Prerequisites
- JDK 21
- Android SDK (API Level 35+, minSdk 24)

### Development Setup
1. Clone the repository:
   ```bash
   git clone <repo-url>
   cd CoinBridge
   ```

2. Create `local.properties` with your SDK path and release signing secrets:
   ```properties
   sdk.dir=/path/to/your/android/sdk
   RELEASE_STORE_FILE=common_release_key.jks
   RELEASE_STORE_PASSWORD=your_password
   RELEASE_KEY_ALIAS=your_alias
   RELEASE_KEY_PASSWORD=your_password
   ```

3. Build and install Debug APK:
   ```bash
   ./gradlew assembleDebug
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```

4. Build Signed Release App Bundle (`.aab`):
   ```bash
   ./gradlew bundleRelease
   ```
   The signed bundle will be located at `app/build/outputs/bundle/release/app-release.aab`.

---

## 📦 Store Listing Assets

All store graphic assets and metadata are located in [`store_assets/`](store_assets/):
- **512 × 512 App Icon**: [`store_assets/ic_launcher_512.png`](store_assets/ic_launcher_512.png)
- **1024 × 500 Feature Graphic**: [`store_assets/feature_graphic.png`](store_assets/feature_graphic.png)
- **Mobile Screenshots (8)**: [`store_assets/screenshots_phone/`](store_assets/screenshots_phone/)
- **Android TV Screenshots (4)**: [`store_assets/screenshots_tv/`](store_assets/screenshots_tv/)
- **Store Listing Metadata**: [`store_assets/STORE_LISTING_METADATA.md`](store_assets/STORE_LISTING_METADATA.md)
