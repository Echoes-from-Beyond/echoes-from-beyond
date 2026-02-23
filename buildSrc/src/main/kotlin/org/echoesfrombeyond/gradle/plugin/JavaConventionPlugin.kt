package org.echoesfrombeyond.gradle.plugin

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import java.net.URI
import kotlin.jvm.java
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.api.tasks.testing.Test
import org.gradle.external.javadoc.CoreJavadocOptions
import org.gradle.internal.extensions.core.extra
import org.gradle.jvm.tasks.Jar
import org.gradle.jvm.toolchain.JavaLanguageVersion

/**
 * Convention plugin applied to all Gradle projects in this repository. Applies the java and
 * Spotless plugins.
 *
 * This is used instead of a precompiled plugin script to increase flexibility and dodge many
 * headaches associated with kotlin-dsl (rationale inspired by this
 * [blog post](https://mbonnin.net/2025-07-10_the_case_against_kotlin_dsl/)). It should also be
 * marginally faster.
 */
class JavaConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    target.plugins.apply("java-library")
    target.plugins.apply("com.gradleup.shadow")

    target.repositories.add(target.repositories.mavenCentral())

    val libs =
        (target.extensions.getByName("versionCatalogs") as VersionCatalogsExtension).named("libs")

    fun Project.setupDependencyConfiguration(configurationName: String) {
      val global = libs.findBundle(configurationName)
      val local = libs.findBundle("$name-$configurationName")

      global.ifPresent { provider -> dependencies.add(configurationName, provider) }
      local.ifPresent { provider -> dependencies.add(configurationName, provider) }
    }

    target.setupDependencyConfiguration("api")
    target.setupDependencyConfiguration("implementation")
    target.setupDependencyConfiguration("compileOnly")
    target.setupDependencyConfiguration("compileOnlyApi")
    target.setupDependencyConfiguration("runtimeOnly")
    target.setupDependencyConfiguration("shadow")
    target.setupDependencyConfiguration("testImplementation")
    target.setupDependencyConfiguration("testCompileOnly")
    target.setupDependencyConfiguration("testRuntimeOnly")

    target.extensions.configure<JavaPluginExtension>("java") {
      it.toolchain.languageVersion.set(JavaLanguageVersion.of(25))
      it.withSourcesJar()
      it.withJavadocJar()
    }

    target.tasks.withType(Test::class.java).configureEach {
      it.jvmArgs("--sun-misc-unsafe-memory-access=allow")
      it.maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).coerceAtLeast(1)
      it.useJUnitPlatform()
    }

    target.tasks.withType(ShadowJar::class.java).configureEach {
      it.from(target.rootProject.layout.projectDirectory.file("LICENSE").asFile) { spec ->
        spec.into("META-INF")
      }
    }

    target.tasks.withType(JavaCompile::class.java).configureEach { it.options.encoding = "UTF-8" }

    target.tasks.withType(Javadoc::class.java).configureEach {
      it.options.encoding = "UTF-8"

      val core = it.options as? CoreJavadocOptions
      core?.addBooleanOption("Xdoclint:all,-missing", true)
    }

    target.tasks.named("shadowJar", ShadowJar::class.java).configure {
      it.archiveClassifier.set("")
      it.enableAutoRelocation.set(true)
      it.relocationPrefix.set(
          target.provider {
            "${target.group.toString().replace('.', '/')}/${target.name}/internaldep"
          }
      )
      it.minimizeJar.set(true)
    }

    target.tasks.named("jar", Jar::class.java).configure { it.archiveClassifier.set("thin") }
  }
}

/** Add another project as an implementation dependency. */
fun DependencyHandler.projectImplementation(path: String) {
  add("implementation", project(mapOf("path" to path)))
}

/**
 * Adds a dependency on Hytale, but does not make this project a plugin like [withHytalePlugin]
 * would.
 */
fun Project.withHytaleDependency() {
  if (plugins.withType(JavaConventionPlugin::class.java).isEmpty())
      throw GradleException("Hytale plugin projects must apply JavaConventionPlugin!")

  repositories.exclusiveContent { exclusive ->
    exclusive.forRepository {
      repositories.maven { maven ->
        maven.name = "hytale-pre-release"
        maven.url = URI.create("https://maven.hytale.com/pre-release")
      }
    }
    exclusive.filter { filter -> filter.includeGroup("com.hypixel.hytale") }
  }

  val hytale = "com.hypixel.hytale:Server:latest.integration"
  dependencies.add("compileOnly", hytale)
  dependencies.add("testImplementation", hytale)
}

/**
 * Specifies that this project produces a Hytale plugin. Implies [withHytaleDependency].
 *
 * @param name the name of the plugin
 */
fun Project.withHytalePlugin(name: String) {
  withHytaleDependency()

  val baseNameProperty = provider { version }.map { version -> "$name-$version" }

  tasks.named("shadowJar", ShadowJar::class.java).configure { jar ->
    jar.archiveFileName.set(baseNameProperty.map { property -> "$property.jar" })
  }

  tasks.named("sourcesJar", Jar::class.java).configure { jar ->
    jar.archiveFileName.set(baseNameProperty.map { property -> "$property-sources.jar" })
  }

  tasks.named("javadocJar", Jar::class.java).configure { jar ->
    jar.archiveFileName.set(baseNameProperty.map { property -> "$property-javadoc.jar" })
  }

  extra["hasPlugin"] = true
}
