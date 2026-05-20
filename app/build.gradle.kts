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


    // 建议用来替换原有的 applicationVariants.all 块。在 java 关键字前后加上反引号（键盘 Esc 键下方的波浪线键），明确告诉 Kotlin 编译器这是一个包名：
    androidComponents {
        onVariants { variant ->
            variant.outputs.forEach { output ->
                val date = `java`.text.SimpleDateFormat("yyyyMMdd", `java`.util.Locale.getDefault()).format(`java`.util.Date())
                output.outputFileName.set("Dyoo_v${defaultConfig.versionName}_${date}.apk")
            }
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

    // YukiHookAPI (已移除，改用 Legacy Xposed API 82)
    // implementation(libs.yukihookapi)
    // ksp(libs.yukihookapi.ksp)
    // Legacy Xposed API 82
    compileOnly("de.robv.android.xposed:api:82")

    // OkHttp (网络请求 + WebDav)
    implementation(libs.okhttp)

    // Coroutines
    implementation(libs.coroutines)
}
