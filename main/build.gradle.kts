import org.echoesfrombeyond.gradle.plugin.JavaConventionPlugin

apply<JavaConventionPlugin>()

dependencies {
    add("implementation", project(":util"))
}