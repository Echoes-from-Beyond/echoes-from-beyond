import org.gradle.internal.extensions.core.extra
import java.nio.file.Paths

allprojects {
    group = "org.echoesfrombeyond"
}

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
val serverAot: Provider<File> = hytalePath.map { file -> file.resolve("Server").resolve("HytaleServer.aot") }
val assetsZip: Provider<File> = hytalePath.map { file -> file.resolve("Assets.zip") }

val runDirectory: Directory = rootProject.layout.projectDirectory.dir("run")

val copySdkTask: TaskProvider<Copy> = tasks.register("copySdk", Copy::class.java) {
    from(serverJar, serverAot, assetsZip).into(runDirectory)
}

val syncPluginsTask: TaskProvider<Sync> = tasks.register("syncPlugins", Sync::class.java) {
    // Copy from all subprojects that have the `hasPlugin` property set to `true`. This is only the
    // case when their build script includes `withHytalePlugin`.
    from(subprojects.filter { sub -> sub.extra.has("hasPlugin") }
        .filter { sub -> sub.extra.get("hasPlugin") as? Boolean ?: false }
        .map { sub -> sub.tasks.named("shadowJar") })
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

    jvmArgs = listOf("-Xms6G",
        "-Xmx6G",
        "-Xlog:aot",
        "-XX:+UseCompactObjectHeaders",
        "-XX:AOTCache=HytaleServer.aot",
        "--enable-native-access=ALL-UNNAMED",
        "-ea")
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
        exclude("HytaleServer.aot")
    })
}