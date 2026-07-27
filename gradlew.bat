@echo off
where gradle >nul 2>nul
if %ERRORLEVEL% EQU 0 (
  gradle %*
  exit /b %ERRORLEVEL%
)
echo Gradle belum tersedia. Jalankan project melalui GitHub Actions atau Android Studio.
exit /b 1
