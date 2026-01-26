import org.gradle.kotlin.dsl.dependencies
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.devtools.ksp)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.kotlin.serialization)
    id("qytech.android.hilt")
}

android {
    namespace = "com.qytech.tidalplayer"
    compileSdk {
        version = release(36)
    }

    signingConfigs {
        getByName("debug") {
            storeFile = file("..\\SystemSignature.jks")
            storePassword = "qytech1688"
            keyAlias = "qytech"
            keyPassword = "qytech1688"
        }
    }

    defaultConfig {
        applicationId = "com.qytech.tidalplayer"
        minSdk = 30
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            abiFilters.add("arm64-v8a")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
            val applicationName = "QYTidalPlayer"
            val versionName = defaultConfig.versionName
            val date = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
            setProperty("archivesBaseName", "$applicationName-v$versionName-$date")
        }

        debug {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("debug")
            val applicationName = "QYTidalPlayer"
            val versionName = defaultConfig.versionName
            val date = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
            setProperty("archivesBaseName", "$applicationName-v$versionName-$date")
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/**"
            excludes += "/*.properties"
            excludes += "DebugProbesKt.bin"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
        aidl = true
    }
}

dependencies {
    api(
        fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar", "*.aar")))
    )

    implementation(libs.hilt.android)
    implementation(libs.hilt.work)
    implementation(libs.androidx.navigation.runtime.ktx)
    implementation(libs.navigation.compose)
    implementation(libs.androidx.ui.graphics)
    ksp(libs.hilt.android.compiler)

    implementation(libs.timber)
    implementation(libs.mmkv)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    implementation(libs.androidx.constraintlayout.compose)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
    implementation(libs.retrofit)
    implementation(project(":tidal"))
    implementation(libs.androidx.paging.compose)
    implementation(libs.auth)
//    implementation(libs.player)
    implementation(libs.eventproducer)
}
