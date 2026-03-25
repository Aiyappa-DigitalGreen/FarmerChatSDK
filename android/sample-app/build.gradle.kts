plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.farmerchat.sample"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.farmerchat.sample"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }
    buildFeatures {
        compose = true
        viewBinding = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    implementation(project(":farmerchat-sdk"))

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons)
    implementation(libs.activity.compose)
    implementation(libs.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.material)
    debugImplementation(libs.compose.ui.tooling)
}
