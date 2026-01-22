import org.gradle.internal.extensions.core.extra
import java.io.BufferedInputStream
import java.io.FileInputStream
import java.util.jar.Manifest
import java.nio.file.Paths

val hytalePath: Provider<File> = provider {
    val hytaleDotfile: File = rootProject.file(".hytale")

    if (!hytaleDotfile.exists())
        throw GradleException("Missing .hytale file! Please read the # Install section in the " +
                "README for setup details.")

    var hytalePath: File = Paths.get(hytaleDotfile.readText(Charsets.UTF_8).trim()).toFile()

    if (!hytalePath.isDirectory)
        throw GradleException("The path specified in .hytale does not exist or is not the right " +
                "type (must be a directory)!")

    if (!hytalePath.isAbsolute)
        throw GradleException("The path specified in .hytale is not absolute!")

    hytalePath
}

val serverJar: Provider<File> = hytalePath.map { file -> file.resolve("Server").resolve("HytaleServer.jar") }
val assetsZip: Provider<File> = hytalePath.map { file -> file.resolve("Assets.zip") }

val runDirectory: Directory = rootProject.layout.projectDirectory.dir("run")

val collectHytaleRevisions: TaskProvider<Task> = tasks.register("collectHytaleRevisions") {
    inputs.file(serverJar.map { jar ->
        resources.text.fromArchiveEntry(jar, "META-INF/MANIFEST.MF").asFile()
    }).withPathSensitivity(PathSensitivity.NONE)

    inputs.files(provider {
        project("main").configurations["compileClasspath"].asFileTree.matching {
            include("**/Server-*.jar")
        }.map { file -> resources.text.fromArchiveEntry(file, "META-INF/MANIFEST.MF").asFile() }
    }).withPathSensitivity(PathSensitivity.NONE)

    outputs.file(rootProject.layout.buildDirectory.file("hytaleRevisions"))

    doLast {
        val entries = inputs.files.toList().asSequence().map { file ->
            BufferedInputStream(FileInputStream(file)).use { stream ->
                Manifest(stream).mainAttributes
            }
        }

        val revisionIds = mutableSetOf<String>()
        entries.forEach { entry ->
            val value = entry.getValue("Implementation-Revision-Id")
            if (value != null) revisionIds.add(value)
        }

        val sorted = revisionIds.toSortedSet()
        outputs.files.singleFile.writeText(sorted.joinToString("\n"), Charsets.UTF_8)
    }
}

val validateHytaleRevisions: TaskProvider<Task> = tasks.register("validateHytaleRevisions") {
    inputs.files(collectHytaleRevisions)

    doLast {
        val revisionIds = inputs.files.singleFile.readText(Charsets.UTF_8)
            .split("\n")
            .map(String::trim)
            .filter(CharSequence::isNotEmpty)

        if (revisionIds.size > 1) {
            throw GradleException("Identified a mismatch between your local Hytale version and " +
                    "the latest downloaded from Maven. All revision IDs must agree exactly.\n\n" +
                    "All revision IDs found on classpath:\n${revisionIds.joinToString("\n")}\n\n" +
                    "To resolve this error, please update your Hytale client to the latest " +
                    "pre-release.")
        }
    }
}

val copySdkTask: TaskProvider<Copy> = tasks.register("copySdk", Copy::class.java) {
    dependsOn(validateHytaleRevisions)
    from(serverJar, assetsZip).into(runDirectory)
}

val syncPluginsTask: TaskProvider<Sync> = tasks.register("syncPlugins", Sync::class.java) {
    // Copy from all subprojects that have the `hasPlugin` property set to `true`. This is only the
    // case when their build script includes `withHytalePlugin`.
    from(subprojects.filter { sub -> sub.extra.has("hasPlugin") }
        .filter { sub -> sub.extra.get("hasPlugin") as? Boolean ?: false }
        .map { sub -> sub.tasks.named("jar") })
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

    jvmArgs = listOf("-Xms6G", "-Xmx6G", "--enable-native-access=ALL-UNNAMED")
    args = listOf("--disable-sentry", "--assets", "Assets.zip")
}

tasks.register("cleanRunDir", Delete::class.java) {
    delete(runDirectory.dir("logs"))
    delete(runDirectory.dir("mods"))
    delete(runDirectory.dir("universe"))
    delete(runDirectory.asFileTree.matching {
        include("*")
        exclude("*.json")
        exclude("Assets.zip")
        exclude("auth.enc")
        exclude("HytaleServer.jar")
    })
}