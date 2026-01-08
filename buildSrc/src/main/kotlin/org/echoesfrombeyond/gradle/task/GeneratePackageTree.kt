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

@CacheableTask
abstract class GeneratePackageTree : DefaultTask() {
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sourceDirectories: Property<FileCollection>

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
            .filter { pack -> !pack.contains("\n") }
        }.toSortedSet()

        packagesFile.bufferedWriter(Charsets.UTF_8).use { out ->
            packages.forEach { pack ->
                out.write(pack)
                out.write("\n")
            }

            out.flush()
        }
    }
}