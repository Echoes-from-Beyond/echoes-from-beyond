import org.echoesfrombeyond.gradle.plugin.JavaConventionPlugin
import org.echoesfrombeyond.gradle.plugin.projectImplementation
import org.echoesfrombeyond.gradle.plugin.withHytalePlugin
import org.echoesfrombeyond.gradle.plugin.withPublishedPlugin

version = "0.2.1"

apply<JavaConventionPlugin>()

withHytalePlugin("DialogueLib", "0.5.2")

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
