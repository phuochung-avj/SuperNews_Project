plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.supernews"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.supernews"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

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
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    // 1. Kiến trúc MVVM (ViewModel & LiveData)
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")
    // 2. Load ảnh (Glide) - Thay thế Picasso
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")
    // 3. UI đẹp (Material Design & Refresh)
    // Lưu ý: Material thường đã có sẵn, kiểm tra xem có dòng 'com.google.android.material:material' chưa
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    // 4. Hiệu ứng xương (Shimmer Loading)
    implementation("com.facebook.shimmer:shimmer:0.5.0")
    // Firebase Bill of Materials (BoM) - Giúp quản lý phiên bản tự động
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    // Thư viện Firestore (Cơ sở dữ liệu)
    implementation("com.google.firebase:firebase-firestore")
    // (Tùy chọn) Firebase Analytics
    implementation("com.google.firebase:firebase-analytics")
    // Thư viện quản lý Đăng nhập
    implementation("com.google.firebase:firebase-auth")
    // Thư viện quản lý thông báo
    implementation("com.google.firebase:firebase-messaging")
    //
    implementation("it.xabaras.android:recyclerview-swipedecorator:1.4")
    // Thư viện đăng nhập Google
    implementation("com.google.android.gms:play-services-auth:21.0.0")
    //
    implementation("com.airbnb.android:lottie:6.0.0")
    //
    implementation("androidx.core:core-splashscreen:1.0.1")
    // Thư viện lưu hình ảnh
    implementation("com.google.firebase:firebase-storage")
    // Thư viện zoom
    implementation("com.github.chrisbanes:PhotoView:2.3.0")
}