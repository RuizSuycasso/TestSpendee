// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    // Giữ nguyên khai báo cho Android Application plugin
    alias(libs.plugins.androidApplication) apply false

    // **THÊM:** Khai báo cho Kotlin Android plugin (lấy version từ libs.versions.toml)
    alias(libs.plugins.kotlinAndroid) apply false

    // **THÊM:** Khai báo cho Kotlin Kapt plugin (lấy version từ libs.versions.toml)
    alias(libs.plugins.kotlinKapt) apply false
}