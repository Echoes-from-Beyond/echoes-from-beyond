import org.echoesfrombeyond.gradle.plugin.JavaConventionPlugin
import org.echoesfrombeyond.gradle.plugin.withHytaleDependency

apply<JavaConventionPlugin>()

withHytaleDependency("0.5.6")
