import org.echoesfrombeyond.gradle.plugin.JavaConventionPlugin
import org.echoesfrombeyond.gradle.plugin.projectImplementation
import org.echoesfrombeyond.gradle.plugin.withHytalePlugin
import org.echoesfrombeyond.gradle.plugin.withPublishedPlugin

version = "0.2.2"

apply<JavaConventionPlugin>()

withHytalePlugin("CodecHelper", "2026.03.26-89796e57b")

withPublishedPlugin(
    "codec-helper",
    "An annotation-based library for generating codecs.",
    "https://github.com/Echoes-from-Beyond/echoes-from-beyond/tree/main/codec",
)

dependencies { projectImplementation(":util") }
