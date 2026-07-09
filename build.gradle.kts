import com.diffplug.gradle.spotless.SpotlessExtension
import com.diffplug.gradle.spotless.SpotlessPlugin
import com.diffplug.spotless.LineEnding
import java.nio.file.Paths
import org.echoesfrombeyond.gradle.plugin.HytaleDecompiler

apply<HytaleDecompiler>()

apply<SpotlessPlugin>()

val hytaleDotfile: RegularFile = layout.projectDirectory.file(".hytale")
val runDirectory: Directory = layout.projectDirectory.dir("run")

val hytalePath: Provider<String> = provider {
  val hytaleDotfile: File = hytaleDotfile.asFile

  if (!hytaleDotfile.exists()) {
    val os = org.gradle.internal.os.OperatingSystem.current()

    val basedir =
        if (os.isWindows) {
          "AppData/Roaming"
        } else if (os.isMacOsX) {
          "Library/Application Support"
        } else if (os.isLinux) {
          ".var/app/com.hypixel.HytaleLauncher/data"
        } else {
          throw GradleException(
              "Unsupported operating system! Please add a file named .hytale containing the absolute path to your Hytale installation."
          )
        }

    return@provider "${System.getProperty("user.home")}/$basedir/Hytale/install/release/package/game/latest"
  }

  var hytalePath: File = Paths.get(hytaleDotfile.readText(Charsets.UTF_8).trim()).toFile()

  if (!hytalePath.isDirectory)
      throw GradleException(
          "The path specified in .hytale does not exist or is not the right " +
              "type (must be a directory)!"
      )

  if (!hytalePath.isAbsolute)
      throw GradleException("The path specified in .hytale is not absolute!")

  hytalePath.absolutePath
}

val serverJar: Provider<File> =
    hytalePath.map { file -> File(file).resolve("Server").resolve("HytaleServer.jar") }
val serverAot: Provider<File> =
    hytalePath.map { file -> File(file).resolve("Server").resolve("HytaleServer.aot.config") }
val assetsZip: Provider<File> = hytalePath.map { file -> File(file).resolve("Assets.zip") }

val checkHytalePath: TaskProvider<DefaultTask> =
    tasks.register("checkHytalePath", DefaultTask::class.java) {
      inputs.files(serverJar, serverAot, assetsZip)
      outputs.files(serverJar, serverAot, assetsZip)

      doLast {
        if (inputs.files.any { file -> !file.exists() })
            throw GradleException(
                "One or more required server files could not be found; if " +
                    "you are using a .hytale file check that its contents" +
                    "point to a valid installation."
            )
      }
    }

val copySdkTask: TaskProvider<Copy> =
    tasks.register("copySdk", Copy::class.java) { from(checkHytalePath).into(runDirectory) }

val pluginJarFiles: Provider<List<Provider<File>>> = provider {
  subprojects
      .filter { sub -> sub.extra.has("hasPlugin") }
      .filter { sub -> sub.extra.get("hasPlugin") as? Boolean ?: false }
      .map { sub ->
        sub.tasks.named("shadowJar").map { shadowJarTask -> shadowJarTask.outputs.files.singleFile }
      }
}

val syncPluginsTask: TaskProvider<Sync> =
    tasks.register("syncPlugins", Sync::class.java) {
      // Copy from all subprojects that have the `hasPlugin` property set to `true`. This is only
      // the case when their build script includes `withHytalePlugin`.
      from(pluginJarFiles).into(runDirectory.dir("mods"))

      // Preserve everything except run/mods/*.jar.
      preserve {
        include { _ -> true }
        exclude("*.jar")
      }
    }

tasks.register("runDevServer", JavaExec::class.java) {
  inputs.files(copySdkTask, syncPluginsTask)

  // Pass through commands to the Hytale server.
  standardInput = System.`in`

  classpath = files(runDirectory.file("HytaleServer.jar"))
  workingDir = runDirectory.asFile

  jvmArgs =
      listOf(
          "-Xms6G",
          "-Xmx6G",
          "-Xlog:aot",
          "-XX:+UseCompactObjectHeaders",
          "-XX:AOTCache=HytaleServer.aot",
          "--enable-native-access=ALL-UNNAMED",
          "--sun-misc-unsafe-memory-access=allow",
          "-ea",
      )
  args = listOf("--disable-sentry", "--assets", "Assets.zip")
}

tasks.register("cleanRunDir", Delete::class.java) {
  delete(runDirectory.dir("logs"))
  delete(runDirectory.dir("mods"))
  delete(runDirectory.dir("universe"))
  delete(
      fileTree(runDirectory).matching {
        include("*")
        exclude("*.json")
        exclude("Assets.zip")
        exclude("auth.enc")
        exclude("HytaleServer.jar")
        exclude("HytaleServer.aot")
      }
  )
}

tasks.named("decompileHytale").configure { inputs.file(serverJar) }

repositories { mavenCentral() }

extensions.configure(SpotlessExtension::class.java) {
  lineEndings = LineEnding.UNIX
  encoding = Charsets.UTF_8

  kotlinGradle {
    target("*.gradle.kts")
    ktfmt("0.61")
  }
}
