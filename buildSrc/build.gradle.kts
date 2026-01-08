import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
}

dependencies {
    val libs = versionCatalogs.named("libs")

    // These are plugins that we depend upon as `implementation`. They're not used as plugins for
    // this build script, but they are for build scripts that apply our convention plugin.
    implementation(libs.findBundle("build").get())
}

kotlin {
    jvmToolchain(25)

    // TODO: remove this once Gradle 9.4 comes out
    compilerOptions {
        apiVersion.set(KotlinVersion.KOTLIN_2_2)
        jvmTarget.set(JvmTarget.JVM_24)
    }
}

// TODO: remove this once Gradle 9.4 comes out
java {
    sourceCompatibility = JavaVersion.VERSION_24
    targetCompatibility = JavaVersion.VERSION_24
}