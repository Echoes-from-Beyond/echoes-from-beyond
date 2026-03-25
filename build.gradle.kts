import com.diffplug.spotless.LineEnding
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import java.io.BufferedInputStream
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.util.jar.Manifest
import org.echoesfrombeyond.gradle.plugin.HytaleDecompiler

plugins { id("com.diffplug.spotless") version "8.3.0" }

apply<HytaleDecompiler>()

val hytaleDotfile: RegularFile = layout.projectDirectory.file(".hytale")
val runDirectory: Directory = layout.projectDirectory.dir("run")

val hytalePath: Provider<File> = provider {
  val hytaleDotfile: File = hytaleDotfile.asFile

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

val copySdkTask: TaskProvider<Copy> =
    tasks.register("copySdk", Copy::class.java) {
      from(serverJar, serverAot, assetsZip).into(runDirectory)
    }

val pluginJarFiles: Provider<List<Provider<File>>> = provider {
  subprojects
      .filter { sub -> sub.extra.has("hasPlugin") }
      .filter { sub -> sub.extra.get("hasPlugin") as? Boolean ?: false }
      .map { sub ->
        sub.tasks.named("shadowJar").map { shadowJarTask -> shadowJarTask.outputs.files.singleFile }
      }
}

val pluginZipTrees: Provider<List<Provider<FileCollection>>> =
    pluginJarFiles.map { providerList ->
      providerList.flatMap { provider ->
        listOf(
            provider.map { file -> files(file) },
            provider.map { file ->
              zipTree(file).matching {
                include("META-INF/MANIFEST.MF")
                include("manifest.json")
              }
            },
        )
      }
    }

val versionReport: TaskProvider<DefaultTask> =
    tasks.register("versionReport", DefaultTask::class.java) {
      inputs.files(pluginZipTrees)
      outputs.file(layout.buildDirectory.file("versionReport.json"))

      doLast {
        val gson = GsonBuilder().disableHtmlEscaping().serializeNulls().create()

        val reports = JsonArray()
        val iterator = inputs.files.iterator()

        var save: File? = null
        while (iterator.hasNext() || save != null) {
          val jarFile =
              if (save == null) {
                iterator.next()
              } else {
                val copy = save
                save = null
                copy
              }

          val lookahead = mutableMapOf<String, File>()
          while (iterator.hasNext()) {
            val next = iterator.next()
            if (next.extension == "jar") {
              save = next
              break
            } else {
              lookahead[next.extension] = next
            }
          }

          val manifestFile = lookahead["MF"]
          val jsonFile = lookahead["json"]

          val report = JsonObject()

          report.addProperty("FileName", jarFile.name)
          report.add("JsonVersion", JsonNull.INSTANCE)
          report.add("ManifestVersion", JsonNull.INSTANCE)
          report.add("HytaleVersion", JsonNull.INSTANCE)

          if (manifestFile != null) {
            BufferedInputStream(
                    Files.newInputStream(manifestFile.toPath(), StandardOpenOption.READ)
                )
                .use { inputStream ->
                  val manifest = Manifest(inputStream)

                  val attributes = manifest.mainAttributes

                  val implementationVersion = attributes.getValue("Implementation-Version")
                  val hytaleVersion = attributes.getValue("Hytale-Version")

                  report.addProperty("ManifestVersion", implementationVersion)
                  report.addProperty("HytaleVersion", hytaleVersion)
                }
          }

          if (jsonFile != null) {
            Files.newBufferedReader(jsonFile.toPath(), StandardCharsets.UTF_8).use { reader ->
              report.addProperty(
                  "JsonVersion",
                  gson.fromJson(reader, JsonObject::class.java).get("Version")?.asString,
              )
            }
          }

          reports.add(report)
        }

        val sortedReports = JsonArray(reports.size())

        reports
            .sortedBy { element -> element.asJsonObject["FileName"].asString }
            .forEach { sorted -> sortedReports.add(sorted) }

        gson
            .newJsonWriter(
                Files.newBufferedWriter(
                    outputs.files.singleFile.toPath(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                )
            )
            .use { jsonWriter -> gson.toJson(sortedReports, jsonWriter) }
      }
    }

class Util {
  companion object {
    fun maybeThrowErrors(errors: MutableList<Pair<String, String>>) {
      if (!errors.isEmpty())
          throw GradleException(
              errors.joinToString("\n") { pair -> "[${pair.first}] ${pair.second}" }
          )
    }

    fun checkVersion(
        report: JsonObject,
        errors: MutableList<Pair<String, String>>,
        clientHytaleVersion: String?,
    ) {
      val fileName = report.get("FileName").asString
      val jsonVersion = report.get("JsonVersion")
      val manifestVersion = report.get("ManifestVersion").asString
      val hytaleVersion = report.get("HytaleVersion").asString

      if (jsonVersion.isJsonNull) {
        errors.add(Pair(fileName, "`manifest.json` is missing the Version field"))
        return
      }

      if (jsonVersion.asString != manifestVersion) {
        errors.add(
            Pair(
                fileName,
                "Plugin version mismatch: `manifest.json` specifies $manifestVersion while Gradle specifies ${jsonVersion.asString}",
            )
        )
        return
      }

      if (clientHytaleVersion != null && clientHytaleVersion != hytaleVersion) {
        errors.add(
            Pair(
                fileName,
                "Hytale version mismatch: plugin was built for $hytaleVersion but client is on $clientHytaleVersion",
            )
        )
      }
    }
  }
}

val validateVersions: TaskProvider<DefaultTask> =
    tasks.register("validateVersions", DefaultTask::class.java) {
      group = "verification"
      inputs.files(versionReport)

      doLast {
        val gson = Gson()
        val errorMessages = mutableListOf<Pair<String, String>>()

        Files.newBufferedReader(inputs.files.singleFile.toPath(), StandardCharsets.UTF_8)
            .use { reader -> gson.fromJson(reader, JsonArray::class.java) }
            .map { element -> element.asJsonObject }
            .forEach { report -> Util.checkVersion(report, errorMessages, null) }

        Util.maybeThrowErrors(errorMessages)
      }
    }

tasks.check.configure { dependsOn(validateVersions) }

val validateHytaleVersions: TaskProvider<DefaultTask> =
    tasks.register("validateHytaleVersions", DefaultTask::class.java) {
      group = "verification"
      inputs.file(
          serverJar.map { jar ->
            zipTree(jar).matching { include("META-INF/MANIFEST.MF") }.singleFile
          }
      )
      inputs.files(versionReport)

      doLast {
        val gson = Gson()

        val fileMap = mutableMapOf<String, File>()
        inputs.files.forEach { file -> fileMap[file.extension] = file }

        val hytaleManifest = fileMap["MF"]
        val versionReport = fileMap["json"]

        if (hytaleManifest == null || versionReport == null)
            throw GradleException("Internal error: missing Hytale manifest or version report file")

        val hytaleImplementationVersion: String?
        BufferedInputStream(Files.newInputStream(hytaleManifest.toPath(), StandardOpenOption.READ))
            .use { inputStream ->
              hytaleImplementationVersion =
                  Manifest(inputStream).mainAttributes.getValue("Implementation-Version")
            }

        if (hytaleImplementationVersion == null)
            throw GradleException("Unable to determine the Hytale client version")

        val errorMessages = mutableListOf<Pair<String, String>>()
        Files.newBufferedReader(versionReport.toPath(), StandardCharsets.UTF_8)
            .use { reader -> gson.fromJson(reader, JsonArray::class.java) }
            .map { element -> element.asJsonObject }
            .forEach { report ->
              Util.checkVersion(report, errorMessages, hytaleImplementationVersion)
            }

        Util.maybeThrowErrors(errorMessages)
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
  dependsOn(validateHytaleVersions)

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
    targetExclude(".*/**/*")
    targetExclude("run/**/*")

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
    licenseHeaderFile(layout.projectDirectory.file("LICENSE_HEADER"))
  }
}

tasks.named("decompileHytale").configure { inputs.file(serverJar) }
