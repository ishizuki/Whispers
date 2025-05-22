# 📱 Jetpack Compose 対応端末・UI互換性リスト

Jetpack Composeを活用したAndroidアプリ開発において、各種UI（MIUI, One UI, HyperOS, etc.）および端末スペック・アップデート方針を比較しやすく整理しました。

---

## 📑 Table of Contents

- [端末スペック & アップデートポリシー](#端末スペック--アップデートポリシー)
- [UIごとのJetpack Compose対応状況](#uiごとのjetpack-compose対応状況)
- [UI別 推奨Composeバージョン](#ui別-推奨composeバージョン)
- [Jetpack Compose バージョン早見表](#jetpack-compose-バージョン早見表)
- [このデータの用途](#このデータの用途)
- [寄稿・アップデート歓迎](#寄稿アップデート歓迎)
- [Tags](#tags)

---

## 📱 端末スペック & アップデートポリシー

| 📱 Model Name         | 🧩 Initial OS | 🎨 UI      | 🧱 SDK | 🔄 Update Policy                               | 💾 RAM / Storage                   | ⭐ Key Features                                               |
| --------------------- | ------------- | ---------- | ------ | ---------------------------------------------- | ---------------------------------- | ------------------------------------------------------------ |
| **Redmi Note 13 Pro** | Android 13    | MIUI 14    | API 33 | 🔁 2 major Android updates                     | 8GB / 128GB, 12GB / 256GB          | 🛠️ MIUI UI (expected MIUI 15 support)                       |
| **Redmi Note 14 Pro** | Android 14    | HyperOS    | API 34 | 🔁 Up to 3 major updates (estimated)           | 8GB / 128GB, 12GB / 256GB          | ⚡ Lightweight, secure, performance-focused                   |
| **Galaxy A55 5G**     | Android 14    | One UI 6.1 | API 34 | ✅ 4 major updates + 5 yrs security patches     | 8GB / 128GB, 8GB / 256GB           | 💧 IP67, 🛡️ Gorilla Glass Victus+, flagship-grade support   |
| **Galaxy A34 5G**     | Android 13    | One UI 5.1 | API 33 | ✅ 4 major updates + 5 yrs security patches     | 6GB / 128GB, 8GB / 256GB + microSD | 🆙 Android 17 ready, 💽 microSD support                      |
| **Galaxy A35 5G**     | Android 14    | One UI 6.1 | API 34 | ✅ 4 major updates + 5 yrs security patches     | 6GB / 128GB, 8GB / 256GB + microSD | 🆕 Vision Booster display, next-gen A-series                 |
| **Moto G53 5G**       | Android 13    | My UX      | API 33 | 🔁 1 major Android update (typical for Moto G) | 4GB/6GB/8GB + 64GB/128GB + microSD | 💲 Budget 5G, 120Hz, 5000mAh, clean UI, microSD support      |
| **Moto G Pure**       | Android 11    | My UX      | API 30 | 🔁 Up to 1 major update (max)                  | 3GB + 32GB + microSD               | 💲 Very low-cost, 🪫 2-day battery, USB-C, microSD supported |

---

## 💻 UIごとのJetpack Compose対応状況

| 🎨 UI           | Android バージョン | API レベル | Jetpack Compose 対応 | 備考・注意点                                                     |
|----------------|--------------------|------------|------------------------|------------------------------------------------------------------|
| **MIUI 14**    | Android 13         | API 33     | ✅ 対応済み             | 権限UIのカスタマイズ、バッテリー最適化の影響あり。設定除外が推奨される |
| **HyperOS**    | Android 14         | API 34     | ✅ 完全対応             | MIUIより軽量・セキュリティ強化。Composeとの親和性高い             |
| **One UI 6.1** | Android 14         | API 34     | ✅ 完全対応             | Material 3やアニメーション含めてスムーズに動作。開発者向けにも安定した環境 |
| **One UI 5.1** | Android 13         | API 33     | ✅ 対応済み             | Compose 1.3〜1.5系で安定。省電力制御は比較的緩やか             |
| **My UX**      | Android 11〜13     | API 30〜33 | ✅ 対応（制限あり）     | G Pureは1.2〜1.3が限界。G53なら1.5系も問題なし。低スペック端末では注意が必要 |

---

## 🎨 UI別 推奨Composeバージョン

| 🎨 UI          | 対応Composeバージョン目安         | コメント                                                        |
|----------------|--------------------------|-----------------------------------------------------------------|
| **One UI 6.1** | ✅ 1.5〜1.6.x              | Material3とComposeアニメーションもスムーズ                               |
| **HyperOS**    | ✅ 1.5〜1.6.x              | Compose 1.6で安定、軽量端末でも良好な動作                                     |
| **MIUI 14**    | ⚠️ 1.3〜1.5.x             | 新しすぎるComposeは不安定になる場合あり。1.4〜1.5がベスト                      |
| **One UI 5.1** | ✅ 1.3〜1.5.x              | Compose 1.3.1〜1.5系が安定。Material3を使う場合は注意                          |
| **My UX**      | ✅ 1.2〜1.5.x（G Pureは〜1.3） | Moto G Pureは1.6で重くなる。G53は1.5系まで安定                              |
| **ColorOS**    | ⚠️ 1.3〜1.4.x             | Material3テーマとの干渉注意。ColorOS 13なら1.4系までが安心                   |

---

## 🧱 Jetpack Compose バージョン早見表

| 📦 Compose Version | 🗓️ リリース時期 | ✅ 対応 Android | 🧱 推奨 API レベル | 🧩 Material 3 対応 | 🌟 主な特徴と注意点                                                 |
|--------------------|----------------|----------------|------------------|--------------------|--------------------------------------------------------------------|
| **1.6.x**          | 2024 Q1〜       | Android 8〜14   | API 26〜34       | ✅ 完全対応           | Modifier.animateなど強化、Material3完全統合                               |
| **1.5.x**          | 2023 Q3〜       | Android 8〜14   | API 26〜34       | ✅ 高度対応           | Compose Compiler 1.5対応。MaterialTheme3安定                          |
| **1.4.x**          | 2023 Q1〜       | Android 8〜13   | API 26〜33       | ⚠️ β的サポート        | Material3の一部機能未完成。ColorOSなどで注意                                |
| **1.3.x**          | 2022 Q4〜       | Android 8〜13   | API 26〜33       | ⚠️ 限定的             | Material3との互換に課題あり。MIUIなどの制限に留意                           |
| **1.2.x 以下**     | 2022以前         | Android 8〜12   | API 26〜31       | ❌ 非対応            | Material3なし。古いUIでは安定。新機能は使えない                              |

---

## 📂 このデータの用途

- Jetpack Compose 対応端末の選定
- UIごとの最適Composeバージョンの確認
- アプリ動作テスト対象の事前検証
- 長期的なOSアップデート対応・セキュリティ保証の検討

---

## 🛠️ 寄稿・アップデート歓迎

この一覧は継続更新型です。新しいUIや端末、Compose互換性に関する知見があればPR大歓迎です！

---

## 🏷️ Tags

`#Android` `#JetpackCompose` `#UICompatibility` `#MobileDevelopment`  
`#Redmi` `#Samsung` `#Motorola` `#ComposeVersion` `#Material3` `#UpdatePolicy`  
`#MyUX` `#HyperOS` `#MIUI` `#OneUI` `#ComposeSupport`

---
