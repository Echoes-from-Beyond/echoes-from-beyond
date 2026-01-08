package org.echoesfrombeyond.gradle.task

import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.nio.file.Files
import kotlin.io.path.extension
import kotlin.io.path.name

/**
 * Generates a "packages file" that can be used by [GeneratePackageInfo] to generate
 * package-info.java files.
 */
@CacheableTask
abstract class GeneratePackageTree : DefaultTask() {
    /**
     * The source directories to use as inputs.
     */
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sourceDirectories: Property<FileCollection>

    /**
     * The sole output file of this task.
     *
     * After task execution, the file will contain a newline (LF) separated series of Java package
     * names sorted in alphabetical order.
     */
    @get:OutputFile
    abstract val packagesFile: RegularFileProperty

    @TaskAction
    fun run() {
        val sourceDirectories = sourceDirectories.get()
        val packagesFile = packagesFile.asFile.get()

        val packages = sourceDirectories.flatMap { sourceDirectory -> sourceDirectory.walkTopDown()
            .filter(File::isDirectory)
            .filter { dir ->
                Files.newDirectoryStream(dir.toPath()).use check@ { stream ->
                    var foundJavaFile = false
                    for (path in stream) {
                        // If we already have a package-info.java file in the source, don't
                        // generate one. This allows packages to "override" the generated default.
                        if (path.name == "package-info.java") return@check false

                        if (!foundJavaFile && path.extension == "java") foundJavaFile = true
                    }

                    foundJavaFile
                }
            }
            .map { file -> file.relativeTo(sourceDirectory)
                .normalize()
                .invariantSeparatorsPath
                .replace('/', '.')
            }
            .filter(String::isNotBlank)

            // This is more of a sanity check than anything. Java package names should never contain
            // newlines, and if they do we have worse problems! But this will at least prevent the
            // packages file from getting mucked up.
            .filter { pack -> !pack.contains("\n") }
        }.toSortedSet()

        // Write to the packages file. We always use LF to separate packages no matter the platform!
        packagesFile.bufferedWriter(Charsets.UTF_8).use { out ->
            packages.forEach { pack ->
                out.write(pack)
                out.write("\n")
            }

            out.flush()
        }
    }
}