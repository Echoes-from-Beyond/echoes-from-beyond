import java.nio.file.Files
import kotlin.io.path.extension

plugins {
    id("java")

    // Code formatting plugin.
    alias(libs.plugins.spotless)
}

group = "org.echoesfrombeyond"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

// Actual dependencies are defined in `gradle/libs.versions.toml`.
dependencies {
    compileOnly(libs.bundles.compileOnly)
    implementation(libs.bundles.implementation)
    runtimeOnly(libs.bundles.runtimeOnly)

    testImplementation(libs.bundles.testImplementation)
    testRuntimeOnly(libs.bundles.testRuntimeOnly)
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

tasks.build {
    dependsOn("generatePackageInfo")
}

tasks.register("generatePackageHierarchy") {
    inputs.files(sourceSets.main.map { set -> set.output.classesDirs })
    outputs.file("build/pkgs.txt")

    doLast {
        outputs.files.singleFile.bufferedWriter(Charsets.UTF_8).use { out ->
            inputs.files.flatMap { root ->
                root.walkTopDown()
                    .filter(File::isDirectory)
                    .filter { dir ->
                        Files.newDirectoryStream(dir.toPath()).use { stream ->
                            stream.filter(Files::isRegularFile)
                                .any { path -> path.extension == "class" }
                        }
                    }
                    .map { file -> file.relativeTo(root) }
            }.forEach { file ->
                out.write(file.invariantSeparatorsPath.replace('/', '.'))
                out.write("\n")
            }

            out.flush()
        }
    }
}

tasks.register("cleanPackageInfo") {
    inputs.files({ tasks.getByName("generatePackageHierarchy") })
    outputs.dirs({
        sourceSets.getByName("generatedPackageInfo").java.sourceDirectories
    })

    doLast {
        val packages = inputs.files.singleFile.readLines(Charsets.UTF_8).map { line ->
            var base = File("")
            line.split('.').forEach { pack ->
                base = base.resolve(pack)
            }

            base
        }

        outputs.files.forEach { outputDir ->
            outputDir.walkBottomUp()
                .filter(File::isDirectory)
                .forEach { dir ->
                    val relative = dir.relativeTo(outputDir)
                    println(relative.path)

                    if (packages.none { p -> p.startsWith(relative) }) {
                        dir.deleteRecursively()
                    }
                }
        }
    }
}

tasks.register("generatePackageInfo") {
    group = "other"
    description = "Generates package-info.java files corresponding to the package tree."

    inputs.files({ tasks.getByName("generatePackageHierarchy") })
    outputs.dirs({
        sourceSets.getByName("generatedPackageInfo").java.sourceDirectories
    })

    doLast {
        val packages = inputs.files.singleFile.readLines(Charsets.UTF_8)

        outputs.files.forEach { outputDir ->
            packages.forEach { pkg ->
                var path = outputDir
                pkg.split('.').forEach { dir -> path = path.resolve(dir) }

                path.mkdirs()
                path.resolve("package-info.java")
                    .writeText("/* AUTOGENERATED, DO NOT EDIT */ " +
                        "@org.jetbrains.annotations.NotNullByDefault " +
                        "package $pkg;")
            }
        }
    }
}