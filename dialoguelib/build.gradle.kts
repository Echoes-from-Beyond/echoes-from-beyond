import org.echoesfrombeyond.gradle.plugin.JavaConventionPlugin
import org.echoesfrombeyond.gradle.plugin.projectImplementation
import org.echoesfrombeyond.gradle.plugin.withHytalePlugin
import org.echoesfrombeyond.gradle.plugin.withPublishedPlugin

version = "0.1.0"

apply<JavaConventionPlugin>()

withHytalePlugin("DialogueLib", "2026.03.26-89796e57b")

dependencies {
  projectImplementation(":annotation")
  projectImplementation(":modutil")
  projectImplementation(":util")
}

withPublishedPlugin(
    "dialogue-lib",
    "A plugin facilitating NPC dialogue and other useful features.",
    "https://github.com/Echoes-from-Beyond/echoes-from-beyond/tree/main/dialogue",
)
