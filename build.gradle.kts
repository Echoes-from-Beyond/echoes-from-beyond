import com.diffplug.gradle.spotless.SpotlessExtension
import com.diffplug.gradle.spotless.SpotlessPlugin
import com.diffplug.spotless.LineEnding
import java.nio.file.Paths
import java.util.Locale
import org.echoesfrombeyond.gradle.plugin.HytaleDecompiler

apply<HytaleDecompiler>()

apply<SpotlessPlugin>()

val hytaleDotfile: RegularFile = layout.projectDirectory.file(".hytale")
val runDirectory: Directory = layout.projectDirectory.dir("run")

val hytalePath: Provider<String> = provider {
  val hytaleDotfile: File = hytaleDotfile.asFile

  if (!hytaleDotfile.exists()) {
    val osName = System.getProperty("os.name").lowercase(Locale.ROOT)
    val basedir =
        if (osName.contains("windows")) {
          "AppData/Roaming"
        } else if (
            osName.contains("mac os x") || osName.contains("darwin") || osName.contains("osx")
        ) {
          "Library/Application Support"
        } else if (osName.contains("linux")) {
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
val assetsZip: Provider<File> = hytalePath.map { file -> File(file).resolve("Assets.zip") }

val checkHytalePath: TaskProvider<DefaultTask> =
    tasks.register("checkHytalePath", DefaultTask::class.java) {
      inputs.files(serverJar, assetsZip)
      outputs.files(serverJar, assetsZip)

      doLast {
        if (inputs.files.any { file -> !file.exists() })
            throw GradleException(
                "One or more required server files could not be found; if " +
                    "you are using a .hytale file check that its contents" +
                    "point to a valid installation."
            )
      }
    }

subprojects.forEach { sub ->
  sub.extra.set("assetsZip", assetsZip)
  sub.extra.set("runDir", runDirectory)
}

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
  inputs.files(checkHytalePath, syncPluginsTask)

  // Pass through commands to the Hytale server.
  standardInput = System.`in`

  classpath = files(serverJar)
  workingDir = runDirectory.asFile

  jvmArgs =
      listOf(
          "-Xms6G",
          "-Xmx6G",
          "-XX:+UseCompactObjectHeaders",
          "--enable-native-access=ALL-UNNAMED",
          "--sun-misc-unsafe-memory-access=allow",
          "-ea",
      )
  args("--disable-sentry", "--assets", assetsZip.get().absolutePath)
}

tasks.register("cleanRunDir", Delete::class.java) {
  delete(runDirectory.dir("logs"))
  delete(runDirectory.dir("mods"))
  delete(runDirectory.dir("universe"))
  delete(
      fileTree(runDirectory).matching {
        include("*")
        exclude("*.json")
        exclude("auth.enc")
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
