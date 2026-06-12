# Typecho Manager — Compose

<p align="center">
  <img src="https://img.shields.io/badge/Version-2.0.1-brightgreen?style=flat-square" alt="Version">
  <img src="https://img.shields.io/badge/License-MIT-blue?style=flat-square" alt="License">
  <img src="https://img.shields.io/badge/Platform-Android-3DDC84?style=flat-square&logo=android" alt="Platform">
  <img src="https://img.shields.io/badge/Kotlin-1.9+-7F52FF?style=flat-square&logo=kotlin" alt="Kotlin">
</p>

A modern, Material 3 Expressive Android client for managing your [Typecho](https://typecho.org/) blog on the go.

<!-- Screenshot placeholder — replace with actual screenshot -->
<p align="center">
  <!-- <img src="screenshots/hero.png" width="300" alt="App Screenshot"> -->
</p>

## ✨ Features

- **Article Management** — Create, edit, preview, and publish posts directly from your device
- **Media Library** — Browse, upload, and manage attachments with a clean gallery view
- **Category & Tag Filtering** — Quickly navigate posts by category or tag
- **Material 3 Expressive** — Full M3 dynamic color theming that adapts to your wallpaper
- **Dark & Light Themes** — Automatic switching based on system settings, plus manual toggle
- **Native Android Experience** — Built with Jetpack Compose for smooth, responsive UI
- **Offline Drafts** — Write posts offline; they sync when you're back online

## 🏗️ Tech Stack

| Layer | Technology |
|---|---|
| UI | Jetpack Compose + Material 3 Expressive |
| Architecture | MVVM + Repository Pattern |
| DI | Hilt |
| Networking | OkHttp |
| Serialization | kotlinx.serialization |
| Image Loading | Coil |
| Local Storage | DataStore |
| Async | Kotlin Coroutines + Flow |

## 📦 Installation

1. **Download** the latest APK from [Releases](../../releases/latest)
2. **Install** on your Android device (enable "Install from unknown sources" if needed)
3. **Open** the app and enter your Typecho blog URL, username, and password

### Requirements

- Android 8.0 (API 26) or higher
- A running [Typecho](https://typecho.org/) instance with XML-RPC enabled

## 🚀 Getting Started

### From Source

```bash
# Clone the repository
git clone https://github.com/LucasJX/typecho-manager-compose.git

# Open in Android Studio and sync Gradle
# Build and run on your device or emulator
```

### Build Variants

- `debug` — Development build with logging enabled
- `release` — Optimized production build (requires signing config)

## 📁 Project Structure

```
app/src/main/java/
├── data/          # Data layer — API, DB, DataStore
├── di/            # Hilt modules
├── domain/        # Use cases and repository interfaces
├── ui/            # Compose screens and components
│   ├── articles/  # Article list and editor
│   ├── media/     # Media gallery
│   ├── settings/  # App settings
│   └── components/ # Shared UI components
└── utils/         # Extensions and helpers
```

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- [Typecho](https://typecho.org/) — The amazing blog platform
- [Jetpack Compose](https://developer.android.com/jetpack/compose) — Modern Android UI toolkit
- [Material 3](https://m3.material.io/) — Google's design system
