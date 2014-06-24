name := "preowned-kittens"

// Custom keys for this build.

val gitHeadCommitSha = taskKey[String]("Determines the current git commit SHA")

val makeVersionProperties = taskKey[Seq[File]]("Creates a version.properties file we can find at runtime.")


// Common settings/definitions for the build

def preownedKittenProject(name: String): Project = (
  Project(name, file(name))
  settings(
    version := "1.0",
    organization := "com.preownedkittens",
    libraryDependencies += "org.specs2" % "specs2_2.10" % "1.14" % "test"
  )
)

gitHeadCommitSha in ThisBuild := Process("git rev-parse HEAD").lines.head


// Projects in this build

lazy val common = (
  preownedKittenProject("common")
  settings(
    makeVersionProperties := {
      val propFile = (resourceManaged in Compile).value / "version.properties"
      val content = "version=%s" format (gitHeadCommitSha.value)
      IO.write(propFile, content)
      Seq(propFile)
    },
    resourceGenerators in Compile <+= makeVersionProperties
  )
)

val analytics = (
  preownedKittenProject("analytics")
  dependsOn(common)
  settings()
)

val website = (
  preownedKittenProject("website")
  dependsOn(common)
  settings()
)




