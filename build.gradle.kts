import com.diffplug.spotless.LineEnding
import java.nio.file.Paths
import org.gradle.internal.extensions.core.extra

plugins { id("com.diffplug.spotless") version "8.2.1" }

allprojects { group = "org.echoesfrombeyond" }

val hytalePath: Provider<File> = provider {
  val hytaleDotfile: File = rootProject.file(".hytale")

  if (!hytaleDotfile.exists())
      throw GradleException(
          "Missing .hytale file! Please read the # Install section in the " +
              "README for setup details."
      )

  var hytalePath: File = Paths.get(hytaleDotfile.readText(Charsets.UTF_8).trim()).toFile()

  if (!hytalePath.isDirectory)
      throw GradleException(
          "The path specified in .hytale does not exist or is not the right " +
              "type (must be a directory)!"
      )

  if (!hytalePath.isAbsolute)
      throw GradleException("The path specified in .hytale is not absolute!")

  hytalePath
}

val serverJar: Provider<File> =
    hytalePath.map { file -> file.resolve("Server").resolve("HytaleServer.jar") }
val serverAot: Provider<File> =
    hytalePath.map { file -> file.resolve("Server").resolve("HytaleServer.aot") }
val assetsZip: Provider<File> = hytalePath.map { file -> file.resolve("Assets.zip") }

val runDirectory: Directory = rootProject.layout.projectDirectory.dir("run")

val copySdkTask: TaskProvider<Copy> =
    tasks.register("copySdk", Copy::class.java) {
      from(serverJar, serverAot, assetsZip).into(runDirectory)
    }

val syncPluginsTask: TaskProvider<Sync> =
    tasks.register("syncPlugins", Sync::class.java) {
      // Copy from all subprojects that have the `hasPlugin` property set to `true`. This is only
      // the
      // case when their build script includes `withHytalePlugin`.
      from(
              subprojects
                  .filter { sub -> sub.extra.has("hasPlugin") }
                  .filter { sub -> sub.extra.get("hasPlugin") as? Boolean ?: false }
                  .map { sub -> sub.tasks.named("shadowJar") }
          )
          .into(runDirectory.dir("mods"))

      // Preserve everything except run/mods/*.jar
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
      runDirectory.asFileTree.matching {
        include("*")
        exclude("*.json")
        exclude("Assets.zip")
        exclude("auth.enc")
        exclude("HytaleServer.jar")
        exclude("HytaleServer.aot")
      }
  )
}

repositories { mavenCentral() }

spotless {
  lineEndings = LineEnding.UNIX
  encoding = Charsets.UTF_8

  kotlin {
    target("*/src/*/kotlin/**/*.kt")
    ktfmt("0.61")
  }

  kotlinGradle {
    target("**/*.gradle.kts")
    ktfmt("0.61")
  }

  json {
    target("*/src/*/resources/**/*.json")
    gson().indentWithSpaces(2).version("2.13.2")
  }

  java {
    target("*/src/*/java/**/*.java")

    // Always clean these up first.
    removeUnusedImports()

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
    importOrder(
        "java|javax",
        "",
        "org.echoesfrombeyond",
        "\\#java\\#javax",
        "\\#",
        "\\#org.echoesfrombeyond",
    )

    googleJavaFormat("1.33.0")
        .reflowLongStrings()

        // We already reordered imports according to our own scheme, so disable Google's import
        // reordering.
        .reorderImports(false)

    formatAnnotations()
    licenseHeaderFile(rootProject.layout.projectDirectory.file("LICENSE_HEADER").asFile)
  }
}
