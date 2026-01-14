import org.echoesfrombeyond.gradle.plugin.JavaConventionPlugin
import org.echoesfrombeyond.gradle.plugin.hytale
import org.echoesfrombeyond.gradle.plugin.projectImplementation

apply<JavaConventionPlugin>()

dependencies {
    projectImplementation(":util")
    hytale()
}