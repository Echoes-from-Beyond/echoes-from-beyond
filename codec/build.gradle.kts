import groovy.util.Node
import groovy.util.NodeList
import org.echoesfrombeyond.gradle.plugin.JavaConventionPlugin
import org.echoesfrombeyond.gradle.plugin.projectImplementation
import org.echoesfrombeyond.gradle.plugin.withHytalePlugin

version = "0.1.0"

apply<JavaConventionPlugin>()

plugins {
  signing
  `maven-publish`
}

withHytalePlugin("CodecHelper")

dependencies { projectImplementation(":util") }

publishing {
  publications {
    repositories { maven { url = uri(layout.buildDirectory.dir("repos/releases")) } }

    create<MavenPublication>("mavenJava") {
      from(components["java"])

      groupId = "org.echoesfrombeyond"
      artifactId = "codec-helper"
      version = "0.1.0"

      pom {
        name = "org.echoesfrombeyond:codec-helper"
        description = "An annotation-based library for generating codecs."
        url = "https://github.com/Echoes-from-Beyond/echoes-from-beyond/tree/main/codec"

        licenses {
          license {
            name = "The GNU General Public License v3.0"
            url = "https://www.gnu.org/licenses/gpl-3.0.en.html"
          }
        }

        developers {
          developer {
            name = "Kyle Prewitt"
            email = "chemky2000@gmail.com"
            organization = "Echoes from Beyond"
            organizationUrl = "https://github.com/Echoes-from-Beyond"
          }
        }

        scm {
          connection = "scm:git:git://github.com/Echoes-from-Beyond/echoes-from-beyond.git"
          developerConnection = "scm:git:ssh://github.com:Echoes-from-Beyond/echoes-from-beyond.git"
          url = "https://github.com/Echoes-from-Beyond/echoes-from-beyond/tree/main/codec"
        }

        withXml {
          var outerNode = asNode()
          var deps = outerNode["dependencies"] as NodeList

          deps.forEach { node ->
            if (node !is Node) return@forEach

            var dep = node["dependency"]
            if (dep !is NodeList) return@forEach

            var gid = dep.getAt("groupId")?.text()
            var artifactId = dep.getAt("artifactId")?.text()

            var remove = gid == "echoes-from-beyond" && artifactId == "util"
            println(remove)

            outerNode.remove(node)
            return@forEach
          }
        }
      }
    }
  }
}

signing {
  useGpgCmd()
  sign(publishing.publications["mavenJava"])
}
