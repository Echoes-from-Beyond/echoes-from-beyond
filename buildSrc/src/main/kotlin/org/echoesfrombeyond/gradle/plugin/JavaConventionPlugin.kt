package org.echoesfrombeyond.gradle.plugin

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import java.net.URI
import kotlin.jvm.java
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.AbstractArchiveTask
import org.gradle.api.tasks.bundling.Zip
import org.gradle.api.tasks.bundling.ZipEntryCompression
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.api.tasks.testing.Test
import org.gradle.external.javadoc.CoreJavadocOptions
import org.gradle.internal.extensions.core.extra
import org.gradle.jvm.tasks.Jar
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.plugins.signing.SigningExtension

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
    target.group = "org.echoesfrombeyond"

    target.plugins.apply("java-library")
    target.plugins.apply("com.gradleup.shadow")

    target.repositories.add(target.repositories.mavenCentral())

    val libs =
        (target.extensions.getByName("versionCatalogs") as VersionCatalogsExtension).named("libs")

    fun Project.setupDependencyConfiguration(libs: VersionCatalog, configurationName: String) {
      val global = libs.findBundle(configurationName)
      val local = libs.findBundle("$name-$configurationName")

      global.ifPresent { provider -> dependencies.add(configurationName, provider) }
      local.ifPresent { provider -> dependencies.add(configurationName, provider) }
    }

    target.setupDependencyConfiguration(libs, "api")
    target.setupDependencyConfiguration(libs, "implementation")
    target.setupDependencyConfiguration(libs, "compileOnly")
    target.setupDependencyConfiguration(libs, "compileOnlyApi")
    target.setupDependencyConfiguration(libs, "runtimeOnly")
    target.setupDependencyConfiguration(libs, "shadow")
    target.setupDependencyConfiguration(libs, "testImplementation")
    target.setupDependencyConfiguration(libs, "testCompileOnly")
    target.setupDependencyConfiguration(libs, "testRuntimeOnly")

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
      it.from(target.rootDir.resolve("LICENSE")) { spec -> spec.into("META-INF") }
    }

    target.tasks.withType(AbstractArchiveTask::class.java).configureEach {
      it.isPreserveFileTimestamps = false
      it.isReproducibleFileOrder = true

      it.eachFile { file -> file.permissions { permissions -> permissions.unix("644") } }
      it.dirPermissions { dir -> dir.unix("755") }
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

/**
 * Specifies that the project should publish a plugin that can be uploaded to Maven Central or
 * another repository.
 *
 * @param artifactId the artifact identifier; should be written in lower-kebab-case
 * @param description a short description of the plugin
 * @param url a url pointing to the plugin source, usually a subdirectory of the main repo
 */
fun Project.withPublishedPlugin(artifactId: String, description: String, url: String) {
  plugins.apply("signing")
  plugins.apply("maven-publish")

  val groupIdProvider = provider { group.toString() }
  val versionProvider = provider { version.toString() }
  val releaseDirectory = layout.buildDirectory.dir("repos/releases")

  extensions.configure(PublishingExtension::class.java) { publishing ->
    publishing.repositories { repositories ->
      repositories.maven { maven ->
        maven.name = "LocalRelease"
        maven.url = uri(releaseDirectory)
      }
    }

    publishing.publications { publications ->
      publications.create("mavenJava", MavenPublication::class.java) { mavenJava ->
        mavenJava.from(components.getByName("shadow"))
        mavenJava.artifact(tasks.named("sourcesJar"))
        mavenJava.artifact(tasks.named("javadocJar"))

        val groupId = groupIdProvider.get()
        val version = versionProvider.get()

        mavenJava.groupId = groupId
        mavenJava.artifactId = artifactId
        mavenJava.version = version

        mavenJava.pom { pom ->
          pom.name.set("$groupId:$artifactId")
          pom.description.set(description)
          pom.url.set(url)

          pom.licenses { licenses ->
            licenses.license { license ->
              license.name.set("The GNU General Public License v3.0")
              license.url.set("https://www.gnu.org/licenses/gpl-3.0.en.html")
            }
          }

          pom.developers { developers ->
            developers.developer { developer ->
              developer.name.set("Kyle Prewitt")
              developer.email.set("chemky2000@gmail.com")
              developer.organization.set("Echoes from Beyond")
              developer.organizationUrl.set("https://github.com/Echoes-from-Beyond")
            }
          }

          pom.scm { scm ->
            scm.connection.set("scm:git:git://github.com/Echoes-from-Beyond/echoes-from-beyond.git")
            scm.developerConnection.set(
                "scm:git:ssh://github.com:Echoes-from-Beyond/echoes-from-beyond.git"
            )
            scm.url.set(url)
          }
        }
      }
    }
  }

  extensions.configure(SigningExtension::class.java) { signing ->
    signing.useGpgCmd()
    signing.sign(
        extensions.getByType(PublishingExtension::class.java).publications.getByName("mavenJava")
    )
  }

  val publishTask = tasks.named("publishMavenJavaPublicationToLocalReleaseRepository")
  publishTask.configure { publish -> publish.outputs.dir(releaseDirectory) }

  tasks.register("prepareDistribution", Zip::class.java) { zipTask ->
    zipTask.group = "publishing"
    zipTask.description = "Produces a zip file suitable for uploading to Maven Central."
    zipTask.entryCompression = ZipEntryCompression.DEFLATED

    zipTask.archiveFileName.set(
        groupIdProvider.zip(versionProvider) { gid, version -> "$gid:$artifactId:$version.zip" }
    )

    zipTask.from(publishTask)
  }
}
