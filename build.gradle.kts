// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
}

// Clean task to help with build performance
tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}

// Global build configuration
allprojects {
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        kotlinOptions {
            // Enable incremental compilation
            incremental = true
            // Suppress warnings for faster compilation
            allWarningsAsErrors = false
            // Optimize compilation
            freeCompilerArgs += listOf(
                "-Xjvm-default=all"
            )
        }
    }
}