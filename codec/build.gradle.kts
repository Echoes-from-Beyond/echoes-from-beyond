import org.echoesfrombeyond.gradle.plugin.JavaConventionPlugin
import org.echoesfrombeyond.gradle.plugin.projectImplementation
import org.echoesfrombeyond.gradle.plugin.withHytaleDependency

apply<JavaConventionPlugin>()

withHytaleDependency()

dependencies { projectImplementation(":util") }
