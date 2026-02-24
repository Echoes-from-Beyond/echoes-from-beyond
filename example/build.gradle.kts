import org.echoesfrombeyond.gradle.plugin.JavaConventionPlugin
import org.echoesfrombeyond.gradle.plugin.withHytalePlugin

version = "0.1.0"

apply<JavaConventionPlugin>()

withHytalePlugin("ExamplePlugin")
