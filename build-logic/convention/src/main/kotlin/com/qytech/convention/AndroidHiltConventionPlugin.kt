package com.qytech.convention

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

class AndroidHiltConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.google.devtools.ksp")
                apply("dagger.hilt.android.plugin")
            }

            val libs = extensions.getByType<org.gradle.api.artifacts.VersionCatalogsExtension>().named("libs")
            dependencies {
                "implementation"(libs.findLibrary("hilt.android").get())
                "ksp"(libs.findLibrary("hilt.android.compiler").get())
                "implementation"(libs.findLibrary("hilt.navigation.compose").get())
                "implementation"(libs.findLibrary("hilt.work").get())
                "ksp"(libs.findLibrary("hilt.compiler").get())
            }
        }
    }
}