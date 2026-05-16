import org.echoesfrombeyond.gradle.plugin.JavaConventionPlugin
import org.echoesfrombeyond.gradle.plugin.projectImplementation
import org.echoesfrombeyond.gradle.plugin.withHytalePlugin

version = "0.1.0"

apply<JavaConventionPlugin>()

withHytalePlugin("EchoesFromBeyond", "2026.03.26-89796e57b")

dependencies {
  projectImplementation(":util")
  projectImplementation(":modutil")
}
