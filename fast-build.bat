@echo off
REM Fast Build Script for CLYRedNote (Windows)
REM This script provides optimized build commands for faster development

echo 🚀 CLYRedNote Fast Build Helper
echo ================================

if "%1"=="compile" goto compile
if "%1"=="c" goto compile
if "%1"=="debug" goto debug
if "%1"=="d" goto debug
if "%1"=="incremental" goto incremental
if "%1"=="i" goto incremental
if "%1"=="lint" goto lint
if "%1"=="l" goto lint
if "%1"=="clean" goto clean
if "%1"=="rebuild" goto clean
if "%1"=="r" goto clean

echo Usage: %0 {compile^|debug^|incremental^|lint^|clean}
echo.
echo Commands:
echo   compile (c)     - Quick Kotlin compilation check
echo   debug (d)       - Fast debug build
echo   incremental (i) - Incremental build
echo   lint (l)        - Quick lint check
echo   clean (r)       - Clean and rebuild
echo.
echo Examples:
echo   %0 compile
echo   %0 c
echo   %0 debug
echo   %0 d
goto end

:compile
echo 📦 Running quick Kotlin compilation check...
gradlew.bat compileDebugKotlin --quiet --parallel --no-daemon --build-cache --configuration-cache
goto end

:debug
echo 🔨 Running fast debug build...
gradlew.bat assembleDebug --quiet --parallel --daemon --build-cache --configuration-cache
goto end

:incremental
echo ⚡ Running incremental build...
gradlew.bat build --quiet --parallel --daemon --build-cache --configuration-cache
goto end

:lint
echo 🔍 Running quick lint check...
gradlew.bat lintDebug --quiet --parallel --daemon --continue
goto end

:clean
echo 🧹 Cleaning and rebuilding...
gradlew.bat clean assembleDebug --quiet --parallel --daemon --build-cache
goto end

:end
echo ✅ Build command completed!