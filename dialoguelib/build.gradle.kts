import org.echoesfrombeyond.gradle.plugin.JavaConventionPlugin
import org.echoesfrombeyond.gradle.plugin.withHytalePlugin
import org.echoesfrombeyond.gradle.plugin.withPublishedPlugin

version = "0.1.0"

apply<JavaConventionPlugin>()

withHytalePlugin("DialogueLib")

withPublishedPlugin(
    "dialogue-lib",
    "A plugin facilitating NPC dialogue and other useful features.",
    "https://github.com/Echoes-from-Beyond/echoes-from-beyond/tree/main/dialogue",
)

dependencies { add("compileOnly", "org.echoesfrombeyond:codec-helper:0.1.0") }
