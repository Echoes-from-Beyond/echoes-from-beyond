package org.echoesfrombeyond.gradle.plugin

import com.diffplug.gradle.spotless.SpotlessExtension
import com.diffplug.spotless.LineEnding
import org.echoesfrombeyond.gradle.task.GeneratePackageInfo
import org.echoesfrombeyond.gradle.task.GeneratePackageTree
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.api.tasks.testing.Test
import org.gradle.external.javadoc.CoreJavadocOptions
import org.gradle.jvm.tasks.Jar
import org.gradle.jvm.toolchain.JavaLanguageVersion
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
        target.plugins.apply("java")
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

        val mainSourceSet = (target.extensions
            .getByName("sourceSets") as SourceSetContainer)
            .named("main")

        target.extensions.configure<SpotlessExtension>("spotless") {
            it.lineEndings = LineEnding.UNIX
            it.encoding = Charsets.UTF_8
            it.java { java ->
                java.target(mainSourceSet.map { set -> set.java.sourceDirectories })

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

        val generatePackageInfoDir = target.layout.buildDirectory.map { buildDir ->
            buildDir.dir("generated/sources/packageInfo")
        }

        val generatePackageInfoSrcDir = generatePackageInfoDir.map { dir ->
            dir.dir("src/main/java")
        }

        target.extensions.configure<SourceSetContainer>("sourceSets") {
            val main = it.getByName("main")

            val generated = it.create("generatedPackageInfo") { set ->
                set.java.setSrcDirs(listOf(generatePackageInfoSrcDir))
                set.compileClasspath = main.compileClasspath
            }

            val generatedOutput = generated.output

            main.output.dir(generatedOutput)
            main.compileClasspath += generatedOutput
            main.runtimeClasspath += generatedOutput
        }

        val generatePackageTree = target.tasks.register("generatePackageTree", GeneratePackageTree::class.java) {
            it.sourceDirectories.set(mainSourceSet.map { set -> set.java.sourceDirectories })
            it.packagesFile.set(generatePackageInfoDir.map { dir -> dir.file("packages") })
        }

        val generatePackageInfo = target.tasks.register("generatePackageInfo", GeneratePackageInfo::class.java) {
            it.generatedSourceDirectory.set(generatePackageInfoSrcDir)
            it.packagesFile.set(generatePackageTree.flatMap { t -> t.packagesFile })
            it.packageInfoGenerator.set { packageName ->
                """
                @NotNullByDefault
                package $packageName;

                import org.jetbrains.annotations.NotNullByDefault;
                """.trimIndent()
            }
        }

        target.tasks.named("compileGeneratedPackageInfoJava").configure {
            it.inputs.files(generatePackageInfo)
        }
    }
}