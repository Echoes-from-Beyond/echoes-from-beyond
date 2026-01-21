import org.gradle.internal.extensions.core.extra
import java.nio.file.Paths

val hytaleDotfile: File = rootProject.file(".hytale")
var hytalePath: File

if (hytaleDotfile.exists()) {
    hytalePath = Paths.get(hytaleDotfile.readText(Charsets.UTF_8).trim()).toFile()

    if (!hytalePath.exists())
        throw GradleException("The path specified in .hytale does not exist!")

    allprojects { dependencies.ext["hytale"] = hytalePath }
} else {
    throw GradleException("Missing .hytale file! Please read the # Install section in the README " +
            "for setup details.")
}

val runDirectory: Directory = rootProject.layout.projectDirectory.dir("run")

val serverJar = hytalePath.resolve("Server").resolve("HytaleServer.jar")
val assetsZip = hytalePath.resolve("Assets.zip")

val copySdkTask: TaskProvider<Copy> = tasks.register("copySdk", Copy::class.java) {
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