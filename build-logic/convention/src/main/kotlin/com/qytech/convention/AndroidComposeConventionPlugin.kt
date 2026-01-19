package com.qytech.convention

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

class AndroidComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

            val extension = extensions.getByType<LibraryExtension>()
            extension.apply {
                buildFeatures {
                    compose = true
                }
            }

            val libs = extensions.getByType<org.gradle.api.artifacts.VersionCatalogsExtension>()
                .named("libs")
            dependencies {
                val bom = libs.findLibrary("androidx.compose.bom").get()
                "implementation"(platform(bom))
                "implementation"(libs.findLibrary("androidx.compose.ui").get())
                "implementation"(libs.findLibrary("androidx.compose.ui.graphics").get())
                "implementation"(libs.findLibrary("androidx.compose.ui.tooling.preview").get())
                "implementation"(libs.findLibrary("androidx.compose.material3").get())
                "implementation"(libs.findLibrary("androidx.material.icons").get())
                "implementation"(libs.findLibrary("androidx.material.icons.extended").get())
                "implementation"(libs.findLibrary("androidx.paging").get())
                "implementation"(libs.findLibrary("androidx.paging.compose").get())
            }
        }
    }
}