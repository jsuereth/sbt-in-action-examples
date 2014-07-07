name := "preowned-kittens"

// Custom keys for this build.

val gitHeadCommitSha = taskKey[String]("Determines the current git commit SHA")

val makeVersionProperties = taskKey[Seq[File]]("Creates a version.properties file we can find at runtime.")

git.baseVersion := "0.1"

val scalastyleReport = taskKey[File]("creates a report from Scalastyle")

// Common settings/definitions for the build

def PreownedKittenProject(name: String): Project = (
  Project(name, file(name))
  .settings( Defaults.itSettings : _*)
  .settings(org.scalastyle.sbt.ScalastylePlugin.Settings: _*)
  .settings(versionWithGit:_*)
  .settings(
    organization := "com.preownedkittens",
    libraryDependencies += "org.specs2" % "specs2_2.10" % "1.14" % "test",
    javacOptions in Compile ++= Seq("-target", "1.6", "-source", "1.6"),
    resolvers ++= Seq(
      "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
      "teamon.eu Repo" at "http://repo.teamon.eu/"
    ),
    exportJars := true,
    scalastyleReport := {
      val result = org.scalastyle.sbt.PluginKeys.scalastyle.toTask("").value
      val file = ScalastyleReport.report(target.value / "html-test-report",
                        "scalastyle-report.html",
                        (baseDirectory in ThisBuild).value / "project/scalastyle-report.html",
                        target.value / "scalastyle-result.xml")
      println("created report " + file.getAbsolutePath)
      file
    },
    org.scalastyle.sbt.PluginKeys.config := { 
      (baseDirectory in ThisBuild).value / "scalastyle-config.xml" 
    }
  )
  .configs(IntegrationTest)
)

gitHeadCommitSha in ThisBuild := Process("git rev-parse HEAD").lines.head


// Projects in this build

lazy val common = (
  PreownedKittenProject("common")
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
  PreownedKittenProject("analytics")
  dependsOn(common)
  settings()
)

val website = (
  PreownedKittenProject("website")
  dependsOn(common, analytics)
  settings()
)
