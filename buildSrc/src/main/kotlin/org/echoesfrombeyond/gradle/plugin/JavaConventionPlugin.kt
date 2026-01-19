package org.echoesfrombeyond.gradle.plugin

import com.diffplug.gradle.spotless.SpotlessExtension
import com.diffplug.spotless.LineEnding
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.file.FileCollection
import org.gradle.api.plugins.ExtraPropertiesExtension
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.specs.Spec
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.api.tasks.testing.Test
import org.gradle.external.javadoc.CoreJavadocOptions
import org.gradle.jvm.tasks.Jar
import org.gradle.jvm.toolchain.JavaLanguageVersion
import java.io.File
import kotlin.jvm.java

/**
 * Convention plugin applied to all Gradle projects in this repository. Applies the java and
 * Spotless plugins.
 *
 * This is used instead of a precompiled plugin script to increase flexibility and dodge many
 * headaches associated with kotlin-dsl (rationale inspired by this [blog post](https://mbonnin.net/2025-07-10_the_case_against_kotlin_dsl/)).
 * It should also be marginally faster.
 */
class JavaConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.plugins.apply("java-library")
        target.plugins.apply("com.diffplug.spotless")

        target.repositories.add(target.repositories.mavenCentral())

        val libs = (target.extensions.getByName("versionCatalogs") as VersionCatalogsExtension)
            .named("libs")

        target.dependencies.add("compileOnly", libs.findBundle("compileOnly").get())

        target.dependencies.add("implementation", libs.findBundle("implementation").get())
        target.dependencies.add("runtimeOnly", libs.findBundle("runtimeOnly").get())

        target.dependencies.add("testImplementation", libs.findBundle("testImplementation").get())
        target.dependencies.add("testRuntimeOnly", libs.findBundle("testRuntimeOnly").get())

        target.extensions.configure<JavaPluginExtension>("java") {
            it.toolchain.languageVersion.set(JavaLanguageVersion.of(25))
            it.withSourcesJar()
            it.withJavadocJar()
        }

        target.tasks.withType(Test::class.java).configureEach {
            it.maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).coerceAtLeast(1)
            it.useJUnitPlatform()
        }

        target.tasks.withType(Jar::class.java).configureEach {
            it.from(target.rootProject.layout.projectDirectory.file("LICENSE").asFile) { spec ->
                spec.into("META-INF")
            }
        }

        target.tasks.withType(JavaCompile::class.java).configureEach {
            it.options.encoding = "UTF-8"
        }

        target.tasks.withType(Javadoc::class.java).configureEach {
            it.options.encoding = "UTF-8"

            val core = it.options as? CoreJavadocOptions
            core?.addBooleanOption("Xdoclint:all,-missing", true)
        }

        val sourceSetContainer = (target.extensions.getByName("sourceSets") as SourceSetContainer)

        target.extensions.configure<SpotlessExtension>("spotless") {
            it.lineEndings = LineEnding.UNIX
            it.encoding = Charsets.UTF_8

            it.json { json ->
                json.target(sourceSetContainer
                    .filter { set -> set.name == "main" || set.name == "test" }
                    .map { set -> set.resources.include("**/*.json") }.toTypedArray())

                json.gson().indentWithSpaces(2).version("2.13.2")
            }

            it.java { java ->
                java.target(sourceSetContainer
                    .filter { set -> set.name == "main" || set.name == "test" }
                    .map { set -> set.java }.toTypedArray())

                // Always clean these up first.
                java.removeUnusedImports()

                // Order useful imports according to the outline below.
                //
                // - Non-static imports:
                //   - Anything in `java` or `javax`
                //   - Everything else that isn't specified
                //   - Anything in `org.echoesfrombeyond`
                // - Static imports:
                //   - Anything in `java` or `javax`
                //   - Everything else that isn't specified
                //   - Anything in `org.echoesfrombeyond`
                java.importOrder(
                    "java|javax",
                    "",
                    "org.echoesfrombeyond",
                    "\\#java\\#javax",
                    "\\#",
                    "\\#org.echoesfrombeyond"
                )

                java.googleJavaFormat("1.33.0")
                    .reflowLongStrings()

                    // We already reordered imports according to our own scheme, so disable Google's import
                    // reordering.
                    .reorderImports(false)

                java.formatAnnotations()
                java.licenseHeaderFile(target.rootProject.layout.projectDirectory.file("LICENSE_HEADER").asFile)
            }
        }

        target.tasks.named("jar", Jar::class.java).configure {
            it.duplicatesStrategy = DuplicatesStrategy.EXCLUDE

            val runtimeClasspath = target.configurations.named("runtimeClasspath")

            it.dependsOn(runtimeClasspath)

            it.from(runtimeClasspath.map { configuration ->
                configuration
                    .filter { file -> file.extension == "jar" }
                    .map { jar -> target.zipTree(jar) }
            })
        }

        val includes = setOf("implementation", "api")

        // We want to include sources and Javadoc from project dependencies declared under
        // `implementation` or `api`. This function runs that check.
        val spec = { name: String -> includes.contains(name) }

        target.tasks.named("sourcesJar", Jar::class.java).configure {
            it.duplicatesStrategy = DuplicatesStrategy.EXCLUDE

            // This is horribly ugly, but what it does is actually simple: collect all project
            // dependencies and the files outputted by their `sourcesJar` tasks. This only includes
            // the direct project dependencies, but this is fine since those also have this
            // configuration applied.
            it.from({
                dependentProjects(target, spec) { project ->
                    project.tasks.named("sourcesJar", Jar::class.java)
                }.map { source ->
                    target.zipTree(source.map { jar -> jar.outputs.files.singleFile })
                }
            })
        }

        // Instead of configuring `javadocJar`, configure `javadoc` as we can directly add new
        // source inputs.
        target.tasks.named("javadoc", Javadoc::class.java).configure {
            // Similar logic to the configuration of `sourcesJar`.
            it.source(dependentProjects(target, spec) { project ->
                project.tasks.named("javadoc", Javadoc::class.java).map { javadoc ->
                    javadoc.source
                }
            })
        }
    }
}

/**
 * Return a [List] made by iterating all [ProjectDependency] found in [target]'s configurations that
 * match [nameFilter], after applying the [transform] mapping function to each.
 */
private fun <R> dependentProjects(target: Project,
                                  nameFilter: Spec<String>,
                                  transform: (Project) -> R): List<R> {
    return target.configurations.named(nameFilter).flatMap { config ->
        config.dependencies.withType(ProjectDependency::class.java).map { dependency ->
            transform(target.project(dependency.path))
        }
    }
}

/**
 * Add another project as an implementation dependency.
 */
fun DependencyHandler.projectImplementation(path: String) {
    add("implementation", project(mapOf("path" to path)))
}

private fun missingSdkException(): GradleException {
    return GradleException("Using the `hytale()` dependency requires you to create a file " +
            "named `.hytale` in the project root. This file must contain a path to the " +
            "HytaleServer.jar file from your installation. If relative, the path is resolved " +
            "relative to the root project directory.")
}

fun Project.hytaleSdkProperty(): DirectoryProperty {
    return objects.directoryProperty().fileProvider(provider {
        val extra = dependencies.extensions.getByName("ext") as ExtraPropertiesExtension
        if (!extra.has("hytale")) throw missingSdkException()

        extra["hytale"] as File
    })
}

/**
 * Add a compile-only dependency on Hytale.
 */
fun DependencyHandler.hytale(files: FileCollection) {
    add("compileOnly", files)
}
