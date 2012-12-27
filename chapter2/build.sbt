name := "used-kittens"

version := "1.0"

libraryDependencies ++= Seq("junit" % "junit" % "4.11" % "test",
                            "org.specs2" % "specs2_2.9.1" % "1.10" % "test")

val gitHeadCommit = taskKey[String]("Determines the current git commit SHA")

gitHeadCommit := Process("git rev-parse HEAD").lines.head

val makeVersionProperties = taskKey[Seq[File]]("Creates a version.properties file we can find at runtime.")

makeVersionProperties := {
  val propFile = (resourceManaged in Compile).value / "version.properties"
  val content = "version=%s" format (gitHeadCommit.value)
  IO.write(propFile, content)
  Seq(propFile)
}

// TODO - Ask mark to tweak macros so this can work with `<` if possible...
(resourceGenerators in Compile) <+= makeVersionProperties




