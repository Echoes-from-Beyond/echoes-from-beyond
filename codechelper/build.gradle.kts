import org.echoesfrombeyond.gradle.plugin.JavaConventionPlugin
import org.echoesfrombeyond.gradle.plugin.projectImplementation
import org.echoesfrombeyond.gradle.plugin.withHytalePlugin
import org.echoesfrombeyond.gradle.plugin.withPublishedPlugin

version = "0.2.1"

apply<JavaConventionPlugin>()

withHytalePlugin("CodecHelper", "2026.02.19-1a311a592")

withPublishedPlugin(
    "codec-helper",
    "An annotation-based library for generating codecs.",
    "https://github.com/Echoes-from-Beyond/echoes-from-beyond/tree/main/codec",
)

dependencies { projectImplementation(":util") }
