import com.qytech.convention.extensions.loadLocalProperties
import org.gradle.kotlin.dsl.dependencies


plugins {
    id("qytech.android.library")
    id("qytech.android.library.compose")
    id("qytech.android.hilt")
}

android {
    namespace = "com.qytech.tidal"
    compileSdk {
        version = release(36)
    }

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        minSdk = 29

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        val localProperties = loadLocalProperties()

        val tidalClientId = "tidal.clientid"
        val clientId = localProperties[tidalClientId]
        val tidalClientSecret = "tidal.clientsecret"
        val clientSecret = localProperties[tidalClientSecret]
        val tidalClientRedirectUri = "tidal.clientredirecturi"
        val clientRedirectUri = localProperties[tidalClientRedirectUri]
        val tidalClientScopes = "tidal.clientscopes"
        val clientScopes = localProperties[tidalClientScopes]

        buildConfigField("String", "TIDAL_CLIENT_ID",  "$clientId")
        buildConfigField("String", "TIDAL_CLIENT_SECRET", "$clientSecret")
        buildConfigField("String", "TIDAL_CLIENT_REDIRECT_URI", "$clientRedirectUri")
        buildConfigField("String", "TIDAL_CLIENT_SCOPES", "$clientScopes")
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
    kotlinOptions {
        jvmTarget = "11"
    }
    packaging {
        resources {
            excludes += "/META-INF/**"
            excludes += "/*.properties"
            excludes += "DebugProbesKt.bin"
        }
    }

}

dependencies {
    api(
        fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar", "*.aar")))
    )

    api(libs.tidal.exoPlayer.core)
    implementation(libs.tidal.exoPlayer.dash)
    implementation(libs.tidal.exoPlayer.hls)
    implementation(libs.tidal.exoPlayer.extension.flac)
    implementation(libs.tidal.exoPlayer.extension.okhttp)

    implementation(libs.retrofit)
    // 如果需要 Gson 转换器：
    implementation(libs.converter.gson)
    implementation(libs.okhttp.v4120)
    implementation(libs.logging.interceptor.v4120)

    implementation("androidx.datastore:datastore-preferences:1.0.0")

    implementation(libs.mmkv)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.auth)
    implementation(libs.eventproducer)
//    implementation(libs.player)
}