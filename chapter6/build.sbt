name := "preowned-kittens"

version := "1.0"

// These are dependencies for Play

libraryDependencies ++= Seq(
  "play" %% "play" % "2.1.1",
  "eu.teamon" %% "play-navigator" % "0.4.0",
  "org.webjars" % "jquery" % "1.9.1",
  "play" %% "anorm" % "2.1.1",
  "play" %% "play-jdbc" % "2.1.1",
  "org.fusesource.scalate" %% "scalate-core" % "1.6.1"
)

resolvers ++= Seq(
  "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
  "teamon.eu Repo" at "http://repo.teamon.eu/"
)

// specs2

libraryDependencies += "org.specs2" %% "specs2" % "1.14" % "test"

libraryDependencies += "org.pegdown" % "pegdown" % "1.0.2" % "test"
                            
testOptions += Tests.Argument(TestFrameworks.Specs2, "html")

javaOptions in Test += "-Dspecs2.outDir=" + (target.value / "generated/test-reports").getAbsolutePath

fork in Test := true

// junit

libraryDependencies += "junit" % "junit" % "4.11" % "test"

libraryDependencies += "com.novocode" % "junit-interface" % "0.10-M3" % "test"

testOptions += Tests.Argument(TestFrameworks.JUnit, "-v", "-n", "--run-listener=com.preownedkittens.sbt.JUnitListener")

javaOptions in Test += "-Djunit.output.file=" + (target.value / "generated/junit.html").getAbsolutePath

javacOptions in Compile ++= Seq("-target", "1.6", "-source", "1.6")

// scalacheck

libraryDependencies += "org.scalacheck" % "scalacheck_2.10.0" % "1.10.0" % "test"
                            
testOptions += Tests.Argument(TestFrameworks.ScalaCheck, "-s", "500")

// scalatest

fork in IntegrationTest := true

libraryDependencies += "org.scalatest" % "scalatest_2.10" % "2.0.M6-SNAP8" % "it"

libraryDependencies += "org.seleniumhq.selenium" % "selenium-java" % "2.31.0" % "it"

libraryDependencies += "org.pegdown" % "pegdown" % "1.0.2" % "it"

javaOptions in IntegrationTest += "-Dwebdriver.chrome.driver=" + (baseDirectory.value / "src/it/resources/chromedriver.exe").getAbsolutePath

testOptions += Tests.Argument(TestFrameworks.ScalaTest, "-h", (target.value / "html-test-report").getAbsolutePath)

val gitHeadCommitSha = taskKey[String]("Determines the current git commit SHA")

gitHeadCommitSha := Process("git rev-parse HEAD").lines.head

val dependentJarDirectory = settingKey[File]("location of the unpacked dependent jars")

dependentJarDirectory := target.value / "dependent-jars"

val createDependentJarDirectory = taskKey[Unit]("create the dependent-jars directory")

createDependentJarDirectory := {sbt.IO.createDirectory(dependentJarDirectory.value)}

def unpack(target: File, f: File) = {
  val excludes = List("meta-inf", "license", "play.plugins", "reference.conf")
  if (!f.isDirectory) sbt.IO.unzip(f, target, filter = new NameFilter { def accept(name: String) = { !excludes.exists(x => name.toLowerCase().startsWith(x)) && !file(target.getAbsolutePath + "/" + name).exists}})
}

val unpackJars = taskKey[Seq[_]]("unpacks a dependent jars into target/dependent-jars")

unpackJars := {Build.data((dependencyClasspath in Runtime).value).map ( f => unpack(dependentJarDirectory.value, f))}

val createUberJar = taskKey[File]("create jar which we will run")

createUberJar := {
  create (dependentJarDirectory.value, (classDirectory in Compile).value, target.value / "build.jar");
  target.value / "build.jar"
}

def create(depDir: File, binDir: File, buildJar: File) = {
  def getFiles(dir: File) = {
    val files = (dir ** "*").get.filter(d => d != dir)
    files.map(x => (x, x.relativeTo(dir).get.getPath))
  }
  sbt.IO.zip(getFiles(binDir) ++ getFiles(depDir), buildJar)
}

