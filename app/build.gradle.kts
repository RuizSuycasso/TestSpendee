plugins {
    // Sử dụng alias từ libs.versions.toml đã được khai báo ở cấp project
    alias(libs.plugins.androidApplication) // Hoặc android-application nếu bạn giữ tên cũ
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.kotlinKapt)
}

// Biến phiên bản Room (có thể bỏ nếu dùng alias cho thư viện Room)
// val roomVersion = "2.6.1"

android {
    namespace = "com.example.spendee"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.spendee"
        minSdk = 24
        targetSdk = 35
        versionCode = 2
        versionName = "1.1"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    // Cấu hình Kotlin options (giờ đây sẽ được nhận diện)
    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // Sử dụng alias từ libs.versions.toml
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.activity.ktx) // Đã sửa alias trong libs.versions.toml
    implementation(libs.cardview)    // Đã thêm alias trong libs.versions.toml

    // Lifecycle Components
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)

    // Navigation Component (Sử dụng alias KTX)
    implementation(libs.navigation.fragment.ktx)
    implementation(libs.navigation.ui.ktx)

    // Room Persistence Library (Sử dụng alias)
    implementation(libs.room.runtime)
    kapt(libs.room.compiler) // Dùng kapt và alias
    implementation(libs.room.guava)
    implementation(libs.room.paging)

    // Charting Library (Sử dụng alias)
    implementation(libs.mpandroidchart)

    // Testing Dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    testImplementation(libs.room.testing) // Dùng alias


    // Khuyến nghị bảo mật (Giữ nguyên)
    // implementation("at.favre.lib:bcrypt:0.10.2")
    // implementation("de.mkammerer:argon2-jvm:2.11")
}