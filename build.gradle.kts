val file: File? = rootProject.file(".hytale")

if (file?.exists() ?: false) {
    val path = file.readText(Charsets.UTF_8).trim()

    subprojects {
        dependencies.ext["hytaleSdk"] = files(path)
    }
}
