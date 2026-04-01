import com.diffplug.spotless.LineEnding
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

plugins {
  alias(libs.plugins.kotlin)
  alias(libs.plugins.spotless)
}

repositories {
  gradlePluginPortal()
  mavenCentral()
}

dependencies {
  val libs = project.extensions.getByName<VersionCatalogsExtension>("versionCatalogs").named("libs")

  // These are plugins that we depend upon as `implementation`. They're not used as plugins for
  // this build script, but they are for build scripts that apply our convention plugin.
  add("implementation", libs.findBundle("build").get())
}

project.extensions.configure<KotlinJvmProjectExtension>("kotlin") { jvmToolchain(25) }

spotless {
  lineEndings = LineEnding.UNIX
  encoding = Charsets.UTF_8

  kotlin {
    target("src/*/kotlin/**/*.kt")
    ktfmt("0.61")
  }

  kotlinGradle {
    target("*.gradle.kts")
    ktfmt("0.61")
  }
}
