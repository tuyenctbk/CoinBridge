# CoinBridge — Google Play Store Listing Package

This document contains all store listing metadata, copy text, graphic assets references, category/tags, and keystore signing fingerprints ready for Google Play Console submission.

---

## 1. Store Assets Reference

| Asset | File Location | Specifications |
| :--- | :--- | :--- |
| **App Icon** | [`ic_launcher_512.png`](file:///Users/user/antigravity/CoinBridge/store_assets/ic_launcher_512.png) | 512 × 512 px, 32-bit PNG |
| **Feature Graphic** | [`feature_graphic.png`](file:///Users/user/antigravity/CoinBridge/store_assets/feature_graphic.png) | 1024 × 500 px, 24-bit RGB PNG (no alpha) |
| **Phone Screenshots (8)** | [`store_assets/screenshots_phone/`](file:///Users/user/antigravity/CoinBridge/store_assets/screenshots_phone/) | 1080 × 2400 px, 8 feature showcases |
| **Android TV Screenshots (4)** | [`store_assets/screenshots_tv/`](file:///Users/user/antigravity/CoinBridge/store_assets/screenshots_tv/) | 1920 × 1080 px, 16:9 Leanback showcases |
| **Signed Release App Bundle** | [`app-release.aab`](file:///Users/user/antigravity/CoinBridge/app/build/outputs/bundle/release/app-release.aab) | ~16 MB, Signed with `common_release_key.jks` |

---

## 2. Store Categorization & Tags

- **Primary Category**: **Finance**
- **Secondary / Alternative**: **Tools** or **Travel & Local**
- **Official Google Play Console Tags (Select 5)**:
  1. `Currency exchange`
  2. `Financial calculator`
  3. `Cryptocurrency`
  4. `Travel`
  5. `Tools`
- **Content Rating**: Everyone (PEGI 3 / USK 0 / ESRB Everyone)
  - Zero violent or adult content
  - Zero user-to-user interactions
  - Zero personal data sold or tracked
- **Target Audience**: 18+ (Travelers, Finance Professionals, Shoppers, Crypto Enthusiasts)

---

## 3. Store Listing Copy

### App Title (30 characters max)
```
CoinBridge: Currency Converter
```

### Short Description (80 characters max)
```
Fast, offline currency & crypto converter with live rate trends & bill split.
```

### Full Description (Markdown for Google Play Console)
```markdown
CoinBridge is an ultra-fast, offline-first currency and cryptocurrency converter built for seamless travel, global finance, and everyday conversions. Whether traveling abroad, tracking live crypto rates, or dining out with friends, CoinBridge calculates exchange rates instantly—even without an internet connection.

✨ KEY FEATURES:

● 170+ WORLD CURRENCIES & CRYPTO
Convert between all global fiat currencies (USD, EUR, GBP, JPY, CAD, AUD, CHF, and more) plus major cryptocurrencies including Bitcoin (BTC), Ethereum (ETH), Solana (SOL), and XRP with up-to-date live exchange rates.

● 100% OFFLINE-FIRST ARCHITECTURE
Heading onto an airplane or into roaming dead zones? CoinBridge automatically caches the latest rates so you can calculate conversions offline anytime, anywhere.

● MULTI-TARGET INSTANT CONVERSIONS
Stop switching back and forth between single currency pairs. Add multiple target currencies to calculate what your money is worth in euros, pounds, yen, and bitcoin simultaneously with a single tap.

● TRAVEL BILL & TIP SPLITTER
Dining abroad with friends? Enter the local restaurant bill, pick custom tip percentages (0%, 10%, 15%, 18%, 20%), set the number of people, and instantly see what each person pays in both the foreign currency AND your home currency.

● INTERACTIVE HISTORICAL RATE TRENDS
Visualize exchange rate movements across 7-day, 30-day, 90-day, and 1-year timeframes with low, high, and average price benchmarks to time your transactions wisely.

● ALL-IN-ONE PHYSICAL UNIT CONVERTER
Need more than money? Switch over to the built-in Unit Converter for Weight (kg, lb, oz), Length (m, ft, in, mi, km), Volume (L, gal, ml), Temperature (°C, °F, K), Speed, and Area.

● PRIVACY & BIOMETRIC APP LOCK
Protect your private financial logs and calculation history with fingerprint or face biometric unlock. Built with zero tracking, zero third-party analytics, and 100% ad-free experience.

● FULL ANDROID TV & LEANBACK SUPPORT
Optimized with high-contrast D-pad focus indicators and responsive remote control navigation for smart TVs and large screens.

Download CoinBridge today for the cleanest, fastest, and most reliable offline currency converter on Android!
```

---

## 4. Signing Certificate Fingerprints

- **Keystore**: `common_release_key.jks`
- **Key Alias**: `dpad_hero_alias`
- **Owner**: `CN=DpadHero, OU=Games, O=TDPham, L=Saigon, ST=VN, C=VN`
- **SHA-1 Fingerprint**:
  ```
  CB:FD:F6:F3:B4:DF:F0:74:94:55:C8:54:7E:28:B1:27:EF:48:5C:0E
  ```
- **SHA-256 Fingerprint**:
  ```
  84:64:6C:C1:0A:AE:A5:2B:89:2E:78:19:0E:A8:49:4E:28:50:1E:8C:70:CF:50:5A:B4:95:B0:54:15:45:E2:BA
  ```
