import org.echoesfrombeyond.gradle.plugin.JavaConventionPlugin
import org.echoesfrombeyond.gradle.plugin.projectImplementation
import org.echoesfrombeyond.gradle.plugin.withHytalePlugin
import org.echoesfrombeyond.gradle.plugin.withPublishedPlugin

version = "0.3.1"

apply<JavaConventionPlugin>()

withHytalePlugin("CodecHelper", "0.5.6")

withPublishedPlugin(
    "codec-helper",
    "An annotation-based library for generating codecs.",
    "https://github.com/Echoes-from-Beyond/echoes-from-beyond/tree/main/codec",
)

dependencies { projectImplementation(":util") }
