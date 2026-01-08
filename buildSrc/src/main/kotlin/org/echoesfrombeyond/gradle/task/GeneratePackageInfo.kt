package org.echoesfrombeyond.gradle.task

import org.echoesfrombeyond.gradle.work.GeneratePackageInfoWorkAction
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.workers.WorkerExecutor
import java.io.File
import java.util.function.Function
import javax.inject.Inject
import kotlin.io.path.invariantSeparatorsPathString
import kotlin.io.relativeTo
import kotlin.io.walkBottomUp
import kotlin.sequences.forEach

@CacheableTask
abstract class GeneratePackageInfo : DefaultTask() {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val packagesFile: RegularFileProperty

    @get:OutputDirectory
    abstract val generatedSourceDirectory: DirectoryProperty

    @get:Internal
    abstract val packageInfoGenerator: Property<Function<String, String>>

    @get:Inject
    abstract val workerExecutor: WorkerExecutor

    init {
        packageInfoGenerator.convention { name -> "package $name;" }
    }

    @TaskAction
    fun run() {
        val packagesFile = packagesFile.asFile.get()
        val generatedSourceDirectory = generatedSourceDirectory.asFile.get()

        val packages = readPackagesFile(packagesFile)
        val queue = workerExecutor.noIsolation()

        generatedSourceDirectory.walkBottomUp()
            .filter { base ->
                val dir = if (base.isFile) base.parentFile else base

                dir == null || packages.none { info ->
                    info.path.startsWith(dir.relativeTo(generatedSourceDirectory).toPath())
                }
            }
            .forEach(File::deleteRecursively)

        for (info in packages) {
            val packageInfo = info.path.resolve("package-info.java")
            val file = this.generatedSourceDirectory.file(packageInfo.invariantSeparatorsPathString)

            queue.submit(GeneratePackageInfoWorkAction::class.java) {
                contents.set(packageInfoGenerator.map { g -> g.apply(info.name) })
                targetFile.set(file)
            }
        }
    }
}