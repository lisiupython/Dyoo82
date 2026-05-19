plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "o.dyoo"
    compileSdk = 34

    defaultConfig {
        applicationId = "o.dyoo"
        minSdk = 24
        targetSdk = 34
        versionCode = 16
        versionName = "1.2.0"
        
        ndk {
            abiFilters += listOf("arm64-v8a", "armeabi-v7a", "x86", "x86_64")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    // AndroidX
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.livedata)
    implementation("androidx.activity:activity-ktx:1.8.2")

    // Xposed API (Legacy API 82)
    compileOnly("de.robv.android.xposed:api:82")

    // OkHttp (网络请求)
    implementation(libs.okhttp)

    // Coroutines
    implementation(libs.coroutines)
}