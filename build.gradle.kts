import com.diffplug.spotless.LineEnding
import java.nio.file.Files
import java.security.MessageDigest
import kotlin.io.path.extension
import kotlin.math.min

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

// Actual dependencies are defined in `gradle/libs.versions.toml`. They should not be
// added here.
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
    maxParallelForks = min(Runtime.getRuntime().availableProcessors() - 1, 1)

    useJUnitPlatform()
}

tasks.javadoc {
    val core = options as? CoreJavadocOptions

    // See https://docs.oracle.com/en/java/javase/25/docs/specs/man/javadoc.html#doclint
    // Unfortunately there's seemingly no way to only disable missing javadoc
    // warnings for private methods. Setting it up this way will at least lint for
    // other errors.
    core?.addBooleanOption("Xdoclint:all,-missing", true)
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

tasks.withType<Javadoc>().configureEach {
    options.encoding = "UTF-8"
}

spotless {
    // This is currently necessary to prevent Gradle config cache invalidation:
    // https://github.com/gradle/gradle/issues/25469#issuecomment-3444231151. Also
    // make sure that any files formatted by Spotless are using LF.
    lineEndings = LineEnding.UNIX

    // This is the default, but it's nice to be explicit.
    encoding = Charsets.UTF_8

    java {
        target(sourceSets.main.map { set -> set.allJava.sourceDirectories })

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

        googleJavaFormat("1.33.0")
            .reflowLongStrings()

            // We already reordered imports according to our own scheme, so disable
            // Google's import reordering.
            .reorderImports(false)

        formatAnnotations()
        licenseHeaderFile("LICENSE_HEADER")
    }
}

val generatePackageInfoDir = project.layout.buildDirectory.map { buildDir ->
    buildDir.dir("generatePackageInfo")
}

// We need to define a source set for generating package-info.java files into.
sourceSets {
    val generated = create("generatedPackageInfo") {
        java {
            // Need to call setSrcDirs otherwise Gradle's "helpful" default path
            // src/main/java, will be present.
            setSrcDirs(arrayOf(generatePackageInfoDir.map { dir ->
                dir.dir("src").dir("main").dir("java")
            }).asIterable())
        }
    }

    main {
        // For compilation purposes, the generated code has the exact same classpath
        // as `main`.
        generated.compileClasspath = compileClasspath

        val generatedOutput = generated.output

        // This makes sure that (compiled) generated code is included in our
        // resulting build.
        output.dir(generatedOutput)

        // This makes sure that anything in the generated code can be referenced by
        // anything in `main`. `runtimeClasspath` is not strictly necessary as long
        // as we're only generating annotations.
        compileClasspath += generatedOutput
        runtimeClasspath += generatedOutput
    }
}

tasks.named("compileGeneratedPackageInfoJava").configure {
    // We must generate the source before trying to compile it.
    inputs.files({ tasks.named("generatePackageInfo") })
}

val generatePackageHierarchy = tasks.register("generatePackageHierarchy") {
    group = "other"
    description = "Generates a file listing all non-empty Java packages in the input."

    doLast {
        // It only makes sense for us to have a single output file.
        outputs.files.singleFile.bufferedWriter(Charsets.UTF_8).use { out ->
            // We can have multiple source directories, though. Treat all of them as
            // one big directory. We de-duplicate identical packages and sort them
            // for consistent output.
            inputs.files.asSequence().flatMap { root ->
                root.walkTopDown()
                    .filter(File::isDirectory)
                    .filter { dir ->
                        // Check for any .java files. Directories that don't contain
                        // .java files do not need a corresponding package-info.java.
                        Files.newDirectoryStream(dir.toPath()).use { stream ->
                            stream.filter(Files::isRegularFile)
                                .any { path -> path.extension == "java" }
                        }
                    }
                    // All paths are relativized to their respective roots.
                    .map { file -> file.relativeTo(root) }
            }.map { file ->
                file.invariantSeparatorsPath.replace('/', '.')
            }.filter { name ->
                // Java package names can't contain newline characters. If they do,
                // we have far worse problems. But we can reasonably avoid mucking
                // up the formatting of our output file by skipping such pathological
                // input.
                !name.contains('\n')
            }.toSortedSet().forEach { name ->
                out.write(name)
                out.write("\n")
            }

            out.flush()
        }
    }
}

val generatePackageInfo = tasks.register("generatePackageInfo") {
    group = "other"
    description = "Generates package-info.java files corresponding to the package tree."

    doLast {
        // Read the `packages` file into a list of Pairs, each of which contains the
        // original package name (without newlines) and the relative file path
        // starting from the source root.
        //
        // We call `readText` and `split` instead of something like `useLines` to
        // keep behavior the same regardless of platform: `packages` always uses LF
        // as the line separator!
        val packages = inputs.files.singleFile
            .readText(Charsets.UTF_8)
            .splitToSequence('\n')
            .filter(String::isNotBlank)
            .map { line -> Pair(line, line.split('.')
                .fold(File("")) { file, name ->
                    file.resolve(name)
                })
        }.toList()

        val md = MessageDigest.getInstance("SHA-1")

        outputs.files.forEach { outputDir ->
            // Delete unnecessary files; i.e. those that do not correspond to or
            // contain any active packages.
            outputDir.walkBottomUp()
                // Our deletion targets are all relative to the output directory.
                .map { base -> Pair(base, base.relativeTo(outputDir)) }
                .map { (base, relative) ->
                    // For the purposes of determining if a package is used, check
                    // the current directory if we're a directory, or the file's
                    // parent if we're a file.
                    Pair(base,
                        if (base.isDirectory) relative
                        else relative.parentFile)
                }
                .filter { (_, relative) ->
                    // Check if this directory corresponds to a package that contains
                    // at least one .java file.
                    //
                    // `relative` may be null if we encounter a file at the top level
                    // of the source tree. We always want to delete such files.
                    relative == null || packages.none { (_, file) ->
                        file.startsWith(relative)
                    }
                }
                .forEach { (base, _) -> base.deleteRecursively() }

            // Generate package-info.java files based on our input list. Create all
            // directories as necessary.
            packages
                .map { (name, file) ->
                    Pair(name, outputDir
                        .resolve(file)
                        .resolve("package-info.java"))
                }
                .forEach inner@ { (name, file) ->
                    // The code to generate in each package. Always encoded in UTF-8
                    // bytes.
                    val textBytes = ("/* AUTOGENERATED, DO NOT EDIT */ " +
                            "@org.jetbrains.annotations.NotNullByDefault " +
                            "package $name;").toByteArray(Charsets.UTF_8)

                    val exists = file.exists()

                    // If our length stayed the same, we run a checksum to determine
                    // if we need to actually write the contents.
                    if (exists && (file.length() == textBytes.size.toLong())) {
                        md.update(textBytes)
                        val expected = md.digest()

                        md.update(file.readBytes())
                        val actual = md.digest()

                        // Same checksum. We have nothing to write.
                        if (expected.contentEquals(actual)) return@inner
                    }

                    // If the file exists, its parents definitely exist, so don't
                    // bother running `mkdirs`.
                    if (!exists) {
                        file.parentFile.mkdirs()
                    }

                    file.writeBytes(textBytes)
                }
        }
    }
}

generatePackageHierarchy.configure {
    inputs.files(sourceSets.main.map { set -> set.allJava.sourceDirectories })
    outputs.file(generatePackageInfoDir.map { dir -> dir.file("packages") })
}

generatePackageInfo.configure {
    inputs.files(generatePackageHierarchy)
    outputs.dirs({
        sourceSets.named("generatedPackageInfo").map { set ->
            set.allJava.sourceDirectories
        }
    })
}