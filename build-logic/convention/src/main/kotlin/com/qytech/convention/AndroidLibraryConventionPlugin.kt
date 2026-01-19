package com.qytech.convention

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension
import org.gradle.kotlin.dsl.apply

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
                apply(plugin = "com.android.library")
                apply(plugin = "org.jetbrains.kotlin.android")
                apply(plugin = "kotlin-parcelize")
                apply(plugin = "org.jetbrains.kotlin.plugin.serialization")

            extensions.configure<LibraryExtension> {
                compileSdk = 35 // 或者从 catalog 读取

                defaultConfig {
                    minSdk = 29
                    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                    consumerProguardFiles("consumer-rules.pro")
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
                    sourceCompatibility = JavaVersion.VERSION_17
                    targetCompatibility = JavaVersion.VERSION_17
                }

                buildFeatures {
                    buildConfig = true
                }
            }

            extensions.configure<KotlinAndroidProjectExtension> {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_17)
                }
            }

            // 添加通用依赖
            val libs = extensions.getByType<org.gradle.api.artifacts.VersionCatalogsExtension>()
                .named("libs")
            dependencies {
                "implementation"(libs.findLibrary("timber").get())
                "implementation"(libs.findLibrary("androidx.core.ktx").get())
                "implementation"(libs.findLibrary("androidx.lifecycle.runtime.ktx").get())
                "implementation"(libs.findLibrary("serialization-json").get())
                "implementation"(libs.findLibrary("androidx-work-ktx").get())
            }
        }
    }
}