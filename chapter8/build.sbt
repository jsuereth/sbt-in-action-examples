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

libraryDependencies += "org.scalatest" % "scalatest_2.10" % "2.0" % "it"

libraryDependencies += "org.seleniumhq.selenium" % "selenium-java" % "2.31.0" % "it"

libraryDependencies += "org.pegdown" % "pegdown" % "1.0.2" % "it"

javaOptions in IntegrationTest += "-Dwebdriver.chrome.driver=" + (baseDirectory.value / "src/it/resources/chromedriver.exe").getAbsolutePath

testOptions += Tests.Argument(TestFrameworks.ScalaTest, "-h", (target.value / "html-test-report").getAbsolutePath)

val gitHeadCommitSha = taskKey[String]("Determines the current git commit SHA")

gitHeadCommitSha := Process("git rev-parse HEAD").lines.head

val dependentJarDirectory = settingKey[File]("location of the unpacked dependent jars")

dependentJarDirectory := target.value / "dependent-jars"

val createDependentJarDirectory = taskKey[File]("create the dependent-jars directory")

createDependentJarDirectory := {
  sbt.IO.createDirectory(dependentJarDirectory.value)
  dependentJarDirectory.value
}

val excludes = List("meta-inf", "license", "play.plugins", "reference.conf")

def unpackFilter(target: File) = new NameFilter {
  def accept(name: String) = {
    !excludes.exists(x => name.toLowerCase().startsWith(x)) &&
      !file(target.getAbsolutePath + "/" + name).exists
  }
}

def unpack(target: File, f: File, log: Logger) = {
  log.debug("unpacking " + f.getName)
  if (!f.isDirectory) sbt.IO.unzip(f, target, unpackFilter(target))
}

val unpackJars = taskKey[Seq[_]]("unpacks a dependent jars into target/dependent-jars")

unpackJars := {
  val dir = createDependentJarDirectory.value
  val log = streams.value.log
  Build.data((dependencyClasspath in Runtime).value).map ( f => unpack(dir, f, log))
}

val createUberJar = taskKey[File]("create jar which we will run")

createUberJar := {
  val ignored = unpackJars.value
  create (dependentJarDirectory.value, (classDirectory in Compile).value, target.value / "build.jar");
  target.value / "build.jar"
}

def create(depDir: File, binDir: File, buildJar: File) = {
  def files(dir: File) = {
    val fs = (dir ** "*").get.filter(d => d != dir)
    fs.map(x => (x, x.relativeTo(dir).get.getPath))
  }
  sbt.IO.zip(files(binDir) ++ files(depDir), buildJar)
}

val uberJarRunner = taskKey[UberJarRunner]("run the uber jar")

uberJarRunner := new MyUberJarRunner(createUberJar.value)

(test in IntegrationTest) := {
  val x = (test in Test).value
  val y = createUberJar.value
  (test in IntegrationTest).value
}

testOptions in IntegrationTest += Tests.Setup { () => uberJarRunner.value.start() }

testOptions in IntegrationTest += Tests.Cleanup { _ => uberJarRunner.value.stop() }

org.scalastyle.sbt.ScalastylePlugin.Settings

seq(com.github.retronym.SbtOneJar.oneJarSettings: _*)

val scalastyleReport = taskKey[File]("creates a report from Scalastyle")

scalastyleReport := {
  val result = org.scalastyle.sbt.PluginKeys.scalastyle.toTask("").value
  val file = ScalastyleReport.report(target.value / "html-test-report",
                    "scalastyle-report.html",
                    baseDirectory.value / "project/scalastyle-report.html",
                    target.value / "scalastyle-result.xml")
  println("created report " + file.getAbsolutePath)
  file
}
