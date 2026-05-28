import org.echoesfrombeyond.gradle.plugin.JavaConventionPlugin
import org.echoesfrombeyond.gradle.plugin.projectImplementation
import org.echoesfrombeyond.gradle.plugin.withHytalePlugin

version = "0.1.0"

apply<JavaConventionPlugin>()

withHytalePlugin("EchoesFromBeyond", "0.5.2")

dependencies {
  projectImplementation(":util")
  projectImplementation(":modutil")
}
