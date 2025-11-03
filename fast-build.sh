#!/bin/bash

# Fast Build Script for CLYRedNote
# This script provides optimized build commands for faster development

echo "🚀 CLYRedNote Fast Build Helper"
echo "================================"

# Function to run quick compilation check
quick_compile() {
    echo "📦 Running quick Kotlin compilation check..."
    ./gradlew compileDebugKotlin --quiet --parallel --no-daemon --build-cache --configuration-cache
}

# Function to run fast debug build
fast_debug() {
    echo "🔨 Running fast debug build..."
    ./gradlew assembleDebug --quiet --parallel --daemon --build-cache --configuration-cache --offline
}

# Function to run incremental build
incremental() {
    echo "⚡ Running incremental build..."
    ./gradlew build --quiet --parallel --daemon --build-cache --configuration-cache
}

# Function to run lint with minimal checks
quick_lint() {
    echo "🔍 Running quick lint check..."
    ./gradlew lintDebug --quiet --parallel --daemon --continue
}

# Function to clean and rebuild
clean_build() {
    echo "🧹 Cleaning and rebuilding..."
    ./gradlew clean assembleDebug --quiet --parallel --daemon --build-cache
}

# Check command line argument
case "$1" in
    "compile"|"c")
        quick_compile
        ;;
    "debug"|"d")
        fast_debug
        ;;
    "incremental"|"i")
        incremental
        ;;
    "lint"|"l")
        quick_lint
        ;;
    "clean"|"rebuild"|"r")
        clean_build
        ;;
    *)
        echo "Usage: $0 {compile|debug|incremental|lint|clean}"
        echo ""
        echo "Commands:"
        echo "  compile (c)     - Quick Kotlin compilation check"
        echo "  debug (d)       - Fast debug build"
        echo "  incremental (i) - Incremental build"
        echo "  lint (l)        - Quick lint check"
        echo "  clean (r)       - Clean and rebuild"
        echo ""
        echo "Examples:"
        echo "  $0 compile"
        echo "  $0 c"
        echo "  $0 debug"
        echo "  $0 d"
        exit 1
        ;;
esac

echo "✅ Build command completed!"