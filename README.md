# Rekapan Juaskuyy Android

Aplikasi WebView profesional untuk https://juaskuyy.my.id

- Nama aplikasi: Rekapan Juaskuyy
- Package: com.rekapanjuaskuyy.app
- Splash screen hitam-emas
- Dukungan JavaScript, login, upload file, download, tombol kembali
- Link WhatsApp/Telegram/telepon/email dibuka ke aplikasi terkait

## Build lewat Android Studio
1. Buka folder project ini di Android Studio.
2. Tunggu Gradle selesai sinkronisasi.
3. Pilih Build > Build APK(s).
4. APK debug berada di app/build/outputs/apk/debug/app-debug.apk.

## Build lewat GitHub dari HP
1. Buat repository GitHub baru.
2. Upload semua isi ZIP ini ke repository.
3. Buka tab Actions > Build Android APK > Run workflow.
4. Setelah selesai, buka hasil workflow lalu unduh artifact `Rekapan-Juaskuyy-APK`.

Catatan: untuk rilis publik/Play Store, buat Signed APK/AAB dengan keystore sendiri.

## Versi 1.4.0
- Mendukung unduhan PDF, Excel, CSV, gambar, dan file lain dari website.
- Mendukung tautan file biasa serta file yang dibuat oleh browser (`blob:`).
- File otomatis tersimpan ke folder **Download** di HP.
