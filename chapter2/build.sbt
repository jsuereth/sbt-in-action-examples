
val gitHeadCommitSha = taskKey[String]("Determines the current git commit SHA")

gitHeadCommitSha in ThisBuild := Process("git rev-parse HEAD").lines.head

val makeVersionProperties = taskKey[Seq[File]]("Creates a version.properties file we can find at runtime.")

makeVersionProperties in ThisBuild := {
  val propFile = (resourceManaged in Compile).value / "version.properties"
  val content = "version=%s" format (gitHeadCommitSha.value)
  IO.write(propFile, content)
  Seq(propFile)
}

val testDependencies = Seq(
  "junit" % "junit" % "4.11" % "test",
  "org.specs2" % "specs2_2.10" % "1.14" % "test"
)

def PreownedKittenProject(name: String): Project = (
  Project(name, file(name))
   settings(
     version := "1.0",
     (resourceGenerators in Compile) <+= (makeVersionProperties in ThisBuild),
     libraryDependencies ++= testDependencies
   )
)

val common = PreownedKittenProject("common")

val analytics = (
  PreownedKittenProject("analytics")
  dependsOn(common)
)

val website = (
  PreownedKittenProject("website")
  dependsOn(common)
)




