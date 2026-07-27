#!/bin/sh
# Launcher sederhana untuk project Rekapan Juaskuyy.
# Di GitHub Actions, Gradle 8.7 dipasang oleh workflow sebelum file ini digunakan.
set -e
if command -v gradle >/dev/null 2>&1; then
  exec gradle "$@"
fi
echo "Gradle belum tersedia. Jalankan project melalui GitHub Actions atau Android Studio." >&2
exit 1
