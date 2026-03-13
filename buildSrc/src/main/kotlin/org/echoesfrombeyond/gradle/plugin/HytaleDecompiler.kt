package org.echoesfrombeyond.gradle.plugin

import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.java.decompiler.api.Decompiler
import org.jetbrains.java.decompiler.main.decompiler.SingleFileSaver

class HytaleDecompiler : Plugin<Project> {
  override fun apply(target: Project) {
    target.tasks.register("decompileHytale", DefaultTask::class.java) { task ->
      task.outputs.file(target.layout.buildDirectory.file("hytaleSource/HytaleServer.jar"))

      task.doFirst {
        Decompiler.builder()
            .inputs(task.inputs.files.singleFile)
            .output(SingleFileSaver(task.outputs.files.singleFile))
            .allowedPrefixes("com/hypixel/hytale")
            .option("bytecode-source-mapping", true)
            .option("dump-code-lines", true)
            .option(
                "banner",
                "//\n" +
                    "// Source code recreated from a .class file\n" +
                    "// (powered by Vineflower decompiler https://vineflower.org)\n" +
                    "//\n\n",
            )
            .build()
            .decompile()
      }
    }
  }
}
