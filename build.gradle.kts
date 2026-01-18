import java.nio.file.Paths

val file: File? = rootProject.file(".hytale")

if (file?.exists() ?: false) {
    val path = file.readText(Charsets.UTF_8).trim()

    allprojects {
        dependencies.ext["hytale"] = Paths.get(path).toFile()
    }
}
