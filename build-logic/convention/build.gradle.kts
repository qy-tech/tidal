import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
}

group = "com.qytech.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

dependencies {
    implementation(libs.android.gradleApiPlugin)
    implementation(libs.android.tools.common)
    implementation(libs.compose.gradlePlugin)
    implementation(libs.kotlin.gradlePlugin)
    implementation(libs.ksp.gradlePlugin)
    implementation(libs.room.gradlePlugin)
    implementation(libs.kotlin.serialization.gradlePlugin)
    implementation("com.vanniktech:gradle-maven-publish-plugin:0.28.0")
}

gradlePlugin {
    plugins {
        // 1. 定义基础 Android Library 插件
        register("androidLibrary") {
            id = "qytech.android.library"
            implementationClass = "com.qytech.convention.AndroidLibraryConventionPlugin"
        }
        // 2. 定义 Compose 插件
        register("androidCompose") {
            id = "qytech.android.library.compose"
            implementationClass = "com.qytech.convention.AndroidComposeConventionPlugin"
        }
        // 3. 定义 Hilt 插件
        register("androidHilt") {
            id = "qytech.android.hilt"
            implementationClass = "com.qytech.convention.AndroidHiltConventionPlugin"
        }
        // 4. 定义 Room 插件
        register("androidRoom") {
            id = "qytech.android.room"
            implementationClass = "com.qytech.convention.AndroidRoomConventionPlugin"
        }
    }
}
