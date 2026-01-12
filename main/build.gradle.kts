import org.echoesfrombeyond.gradle.plugin.JavaConventionPlugin
import org.gradle.jvm.tasks.Jar

apply<JavaConventionPlugin>()

dependencies {
    add("implementation", project(":util"))
}

tasks.named<Jar>("jar").configure {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    // Packages all (runtime) dependencies in the jar file, including the compiled output of project
    // dependencies.
    from(configurations.named<Configuration>("runtimeClasspath").map { configuration ->
        configuration.map { file -> if (file.isDirectory) file else zipTree(file) }
    })
}