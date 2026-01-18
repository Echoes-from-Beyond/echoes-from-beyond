import org.echoesfrombeyond.gradle.plugin.JavaConventionPlugin
import org.echoesfrombeyond.gradle.plugin.hytale
import org.echoesfrombeyond.gradle.plugin.hytaleSdkProperty
import org.echoesfrombeyond.gradle.plugin.projectImplementation

apply<JavaConventionPlugin>()

val sdk = hytaleSdkProperty()

val serverJar: Provider<RegularFile> = sdk.map { dir ->
    dir.dir("Server").file("HytaleServer.jar")
}
val assetsZip: Provider<RegularFile> = sdk.map { directory ->
    directory.file("Assets.zip")
}

val runDirectory: Directory = rootProject.layout.projectDirectory.dir("run")

dependencies {
    projectImplementation(":util")
    hytale(files(serverJar))
}

tasks.register<Sync>("syncPlugins") {
    from(tasks.named("jar")).into(runDirectory.dir("mods"))

    preserve {
        // Preserve all subdirectories, we only want to remove stale plugin jars.
        include("*/**/*")
    }
}

tasks.register<Copy>("copySdk") {
    from(serverJar, assetsZip).into(runDirectory)
}

fun runConfig(isDebug: Boolean): JavaExec.() -> Unit {
    return {
        dependsOn("syncPlugins", "copySdk")

        // Pass through commands to the Hytale server.
        standardInput = System.`in`

        classpath = files(runDirectory.file("HytaleServer.jar"))
        workingDir = runDirectory.asFile

        jvmArgs = listOf("-Xms6G", "-Xmx6G")
        args = listOf("--disable-sentry", "--assets", "Assets.zip")

        if (isDebug) {
            // Let IDEs attach to the build.
            debugOptions {
                enabled = true
                server = true
                suspend = true
            }
        }
    }
}

tasks.register<JavaExec>("runDevServer", runConfig(false))
tasks.register<JavaExec>("runDevServerDebug", runConfig(true))