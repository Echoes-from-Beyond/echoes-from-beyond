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
    create("generatedPackageInfo") {
        java {
            srcDir("src/generatedPackageInfo/java")
        }

        compileClasspath = sourceSets.main.get().compileClasspath
    }

    main {
        val set = sourceSets.getByName("generatedPackageInfo")

        // This makes sure that (compiled) generated code is included in our
        // resulting build.
        output.dir(set.output)

        compileClasspath += set.output
        runtimeClasspath += set.output
    }
}

tasks.getByName("compileGeneratedPackageInfoJava")
    .dependsOn("generatePackageInfo")

tasks.getByName("compileJava")
    .dependsOn(
        // Run Spotless automatically before compilation.
        "spotlessApply",

        // Make sure we generate & compile sources in `generatedPackageInfo`.
        "generatePackageInfo",
        "compileGeneratedPackageInfoJava"
    )

// Source set tracked by `generatePackageInfo`.
val targetSourceSet = sourceSets.main

tasks.register("generatePackageInfo") {
    group = "other"
    description = "Generates package-info.java files corresponding to the package tree."

    inputs.files({ targetSourceSet.get().java.sourceDirectories })

    inputs.property("generatedSourceDirs", {
        sourceSets.getByName("generatedPackageInfo").java.sourceDirectories.files
    })

    outputs.dirs({
        @Suppress("UNCHECKED_CAST")
        val out = inputs.properties["generatedSourceDirs"] as Iterable<File>

        out.flatMap { outDirectory ->
            targetSourceSet.get().java.sourceDirectories.flatMap { sourceDirectory ->
                sourceDirectory.walkTopDown().filter(File::isDirectory).filter { dir ->
                    dir.walkTopDown().maxDepth(1).filter(File::isFile).any { file ->
                        file.extension == "java"
                    }
                }.map { file -> outDirectory.resolve(file.relativeTo(sourceDirectory)) }
            }
        }
    })

    doLast {
        val fileSet = outputs.files.files

        @Suppress("UNCHECKED_CAST")
        val out = inputs.properties["generatedSourceDirs"] as Iterable<File>

        out.forEach { outSourceDir ->
            // Clean up files in our generated folder that shouldn't exist anymore.
            outSourceDir.walkBottomUp().filter(File::isDirectory).filter { file ->
                fileSet.none { set -> set.startsWith(file) }
            }.forEach { file -> file.deleteRecursively() }

            // Write package-info.java files to the output source tree.
            fileSet.filter { file -> file.startsWith(outSourceDir)  }
                .map { file -> Pair(file, file.relativeTo(outSourceDir).path) }
                .map { (file, path) -> Pair(file, path.replace('/', '.')) }
                .forEach { (file, packageName) ->
                    val packageInfo = file.resolve("package-info.java")
                    packageInfo.writeText("/* AUTOGENERATED, do not edit */ " +
                            "@org.jetbrains.annotations.NotNullByDefault " +
                            "package $packageName;", Charsets.UTF_8)
                }
        }
    }
}