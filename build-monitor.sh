#!/bin/bash

# Build Performance Monitor
# Measures build times and provides optimization suggestions

echo "📊 CLYRedNote Build Performance Monitor"
echo "======================================"

# Function to measure build time
measure_build() {
    local build_type=$1
    local command=$2
    
    echo "🕐 Measuring $build_type build time..."
    start_time=$(date +%s)
    
    eval $command
    
    end_time=$(date +%s)
    duration=$((end_time - start_time))
    
    echo "⏱️  $build_type completed in: ${duration}s"
    
    # Performance analysis
    if [ $duration -lt 30 ]; then
        echo "✅ Excellent performance!"
    elif [ $duration -lt 60 ]; then
        echo "⚡ Good performance"
    elif [ $duration -lt 120 ]; then
        echo "⚠️  Moderate performance - consider optimizations"
    else
        echo "🐌 Slow performance - check system resources"
    fi
    
    echo ""
}

# Function to check system resources
check_resources() {
    echo "🖥️  System Resources:"
    echo "==================="
    
    # Check available memory (works on Linux/Mac)
    if command -v free >/dev/null 2>&1; then
        echo "Memory usage:"
        free -h
    fi
    
    # Check disk space
    echo "Disk space:"
    df -h . | head -2
    
    # Check Gradle daemon status
    echo "Gradle daemon status:"
    ./gradlew --status
    
    echo ""
}

# Function to provide optimization tips
optimization_tips() {
    echo "💡 Build Optimization Tips:"
    echo "=========================="
    echo "1. Ensure your system has at least 8GB RAM"
    echo "2. Use SSD for better I/O performance"
    echo "3. Close unnecessary applications during build"
    echo "4. Use incremental builds during development"
    echo "5. Enable 'Offline work' in IDE when dependencies are stable"
    echo "6. Consider using --parallel flag for multi-module projects"
    echo ""
}

# Main execution
case "$1" in
    "compile"|"c")
        measure_build "Kotlin Compilation" "./gradlew compileDebugKotlin --parallel --build-cache --quiet"
        ;;
    "debug"|"d")
        measure_build "Debug Build" "./gradlew assembleDebug --parallel --daemon --build-cache --quiet"
        ;;
    "clean"|"r")
        measure_build "Clean Build" "./gradlew clean assembleDebug --parallel --daemon --build-cache --quiet"
        ;;
    "resources"|"system")
        check_resources
        ;;
    "tips")
        optimization_tips
        ;;
    *)
        echo "Usage: $0 {compile|debug|clean|resources|tips}"
        echo ""
        echo "Commands:"
        echo "  compile     - Measure Kotlin compilation time"
        echo "  debug       - Measure debug build time"
        echo "  clean       - Measure clean build time"
        echo "  resources   - Check system resources"
        echo "  tips        - Show optimization tips"
        ;;
esac