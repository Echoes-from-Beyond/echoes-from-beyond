import org.echoesfrombeyond.gradle.plugin.JavaConventionPlugin
import org.echoesfrombeyond.gradle.plugin.withHytaleDependency

apply<JavaConventionPlugin>()

apply<JavaTestFixturesPlugin>()

withHytaleDependency("0.5.6")
