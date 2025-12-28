import java.nio.file.Files
import kotlin.io.path.extension

plugins {
    id("java")

    // Code formatting plugin.
    id("com.diffplug.spotless") version "8.1.0"
}

group = "org.echoesfrombeyond"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.jetbrains:annotations:26.0.2-1")

    testImplementation("org.junit.jupiter:junit-jupiter:5.7.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }

    withSourcesJar()
    withJavadocJar()
}

tasks.test {
    useJUnitPlatform()
}

tasks.javadoc {
    val core = options as? CoreJavadocOptions

    // See https://docs.oracle.com/en/java/javase/17/docs/specs/man/javadoc.html#additional-options-provided-by-the-standard-doclet.
    // Unfortunately there's seemingly no way to only disable missing javadoc warnings for private
    // methods. Setting it up this way will at least lint for other errors.
    core?.addBooleanOption("Xdoclint:all,-missing", true)
}

spotless {
    java {
        target(sourceSets.main.get().java, sourceSets.test.get().java)

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
            "\\#org.echoesfrombeyond"
        )

        // Use Eclipse JDT to reorder members. This helps to reduce the risk of merge
        // conflicts.
        eclipse()
            .sortMembersEnabled(true)
            .sortMembersVisibilityOrderEnabled(true)

            // Don't sort fields, this can cause compiler errors.
            .sortMembersDoNotSortFields(true)

            // T = (nested) types, SF = static fields, SI = static initializers,
            // F = fields, I = initializers, C = constructors, SM = static methods,
            // M = methods
            .sortMembersOrder("T,SF,SI,F,I,C,SM,M")

            // V = private, R = protected, D = package-private, B = public
            .sortMembersVisibilityOrder("V,R,D,B")

        googleJavaFormat("1.33.0")
            .reflowLongStrings()

            // We already reordered imports according to our own scheme, so disable
            // Google's import reordering.
            .reorderImports(false)

        formatAnnotations()
        licenseHeaderFile("LICENSE_HEADER")
    }
}

sourceSets {
    val set = create("generatedPackageInfo") {
        java {
            srcDir("src/generatedPackageInfo/java")
        }

        compileClasspath = sourceSets.main.get().compileClasspath
    }

    main {
        // This makes sure that (compiled) generated code is included in our
        // resulting build.
        output.dir(set.output)

        // This makes sure that anything in the generated code can be referenced by
        // anything in `main`.
        compileClasspath += set.output
        runtimeClasspath += set.output
    }
}

tasks.compileJava {
    // Compilation depends on the generated files.
    inputs.files({ tasks.getByName("generatePackageInfo") })
}

tasks.getByName("compileGeneratedPackageInfoJava") {
    // We need to generate sources before we compile them.
    inputs.files({ tasks.getByName("generatePackageInfo") })
}

tasks.register("generatePackageInfo") {
    group = "other"
    description = "Generates package-info.java files corresponding to the package tree."

    inputs.files({ sourceSets.main.get().java.sourceDirectories })
    outputs.dir({
        sourceSets.getByName("generatedPackageInfo").java.sourceDirectories
    })

    doLast {
        // `outputDirs` are all folders that must be constructed.
        val outputDirs = inputs.files.flatMap { inputRoot ->
            inputRoot.walkTopDown().filter(File::isDirectory).filter { dir ->
                // We use newDirectoryStream because it lazily loads directory
                // entries, improving performance if there are many entries.
                Files.newDirectoryStream(dir.toPath()).use { stream ->
                    stream
                        .filter { path -> path.extension == "java" }
                        .any(Files::isRegularFile)
                }
            }.map { dir -> dir.relativeTo(inputRoot) }
        }.flatMap { path ->
            outputs.files.map { file -> Pair(file, file.resolve(path)) }
        }.toCollection(mutableSetOf())

        // Clean up all files in the output directory that are not needed anymore.
        outputs.files.flatMap { outputRoot ->
            outputRoot.walkBottomUp().filter(File::isDirectory).filter { dir ->
                // All empty directories match, as well as "orphan" directories that
                // aren't in our source tree.
                Files.newDirectoryStream(dir.toPath()).use { stream ->
                    !stream.iterator().hasNext()
                } || outputDirs.none { (_, outputDir) -> outputDir.startsWith(dir) }
            }
        }.forEach { deletionTarget -> deletionTarget.deleteRecursively() }

        // Generate required package-info.java files.
        outputDirs
            // Don't re-create already present files.
            .filter { (_, outputDir) -> !outputDir.exists() }
            .forEach { (root, outputDir) ->
                val packageName = outputDir.relativeTo(root).path
                    .replace('/', '.')

                outputDir.mkdirs()
                outputDir.resolve("package-info.java")
                    .writeText("/* AUTOGENERATED, DO NOT EDIT */ " +
                            "@org.jetbrains.annotations.NotNullByDefault " +
                            "package $packageName;", Charsets.UTF_8)
        }
    }
}