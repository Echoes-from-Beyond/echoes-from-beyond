import org.echoesfrombeyond.gradle.plugin.JavaConventionPlugin
import org.echoesfrombeyond.gradle.plugin.projectImplementation
import org.echoesfrombeyond.gradle.plugin.withHytalePlugin

version = "0.2.0"

val versionProvider: Provider<String> = provider { version as String }

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
      from(components["shadow"])

      groupId = "org.echoesfrombeyond"
      artifactId = "codec-helper"
      version = versionProvider.get()

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
      }
    }
  }
}

signing {
  useGpgCmd()
  sign(publishing.publications["mavenJava"])
}
