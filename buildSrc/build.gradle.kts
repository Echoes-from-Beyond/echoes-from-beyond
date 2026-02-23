import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

plugins { id("org.jetbrains.kotlin.jvm") version "2.3.0" }

repositories { gradlePluginPortal() }

dependencies {
  val libs = project.extensions.getByName<VersionCatalogsExtension>("versionCatalogs").named("libs")

  // These are plugins that we depend upon as `implementation`. They're not used as plugins for
  // this build script, but they are for build scripts that apply our convention plugin.
  add("implementation", libs.findBundle("build").get())
}

project.extensions.configure<KotlinJvmProjectExtension>("kotlin") { jvmToolchain(25) }
