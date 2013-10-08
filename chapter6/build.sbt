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

// For packaging
val dependentJarDirectory = settingKey[File]("location of the unpacked dependent jars")

dependentJarDirectory := target.value / "dependent-jars"

val createDependentJarDirectory = taskKey[Unit]("create the dependent-jars directory")

createDependentJarDirectory in ThisBuild := {sbt.IO.createDirectory(dependentJarDirectory.value)}

val createDependentJarDirectory2 = taskKey[Unit]("create the dependent-jars directory")

createDependentJarDirectory2 in ThisBuild := Process("echo mkdir " + dependentJarDirectory.value + " > /temp/foobar")

val unpackJars = taskKey[Seq[_]]("unpacks a dependent jars into target/dependent-jars")

unpackJars in ThisBuild := {Build.data((dependencyClasspath in Runtime).value).map ( f => unpack(dependentJarDirectory.value, f))}

def unpack(target: File, f: File) = {
  if (f.isDirectory) {println("f=" + f); sbt.IO.copyDirectory(f, target) }
  else sbt.IO.unzip(f, target, filter = new NameFilter { def accept(name: String) = !name.toLowerCase().startsWith("meta-inf") && !name.toLowerCase().startsWith("license") && !new File(name).exists })
}

val createUberJar = taskKey[File]("create jar which we will run")

createUberJar in ThisBuild := {
  val unpack: Seq[_] = unpackJars.value
  val log = streams.value.log
  val uberJar = target.value / "build.jar"
  create (log, dependentJarDirectory.value, uberJar)
}

def create(log: Logger, dir: File, buildJar: File) = {
  val files = (dir ** "*").get.filter(_ != dir)
  val filesWithPath = files.map(x => (x, x.relativeTo(dir).get.getPath))
  filesWithPath.foreach(fp => log.debug("copying " + fp._1 + " zip(" + fp._2 + ")"))
  sbt.IO.zip(filesWithPath, buildJar)
  buildJar
}

val deleteDependentJarsDirectory = taskKey[Unit]("delete the dependent jars directory")

deleteDependentJarsDirectory in ThisBuild := { sbt.IO.delete(dependentJarDirectory.value) }

// tasks to experiment with dependencies

val taskA = taskKey[String]("taskA")

val taskB = taskKey[String]("taskB")

val taskC = taskKey[String]("taskC")

taskA := { val b = taskB.value; val c = taskC.value; "taskA" }

taskB := { val c = taskC.value; Thread.sleep(5000); "taskB" }

taskC := { Thread.sleep(5000);  "taskC" }


// tasks to run the jar

val runUberJar = taskKey[Int]("run the uber jar")

runUberJar in ThisBuild := {
  val uberJar = createUberJar.value
  val options = ForkOptions(bootJars = Seq(uberJar))
  val arguments = Seq()
  val mainClass = "foo.NewGlobal"
  val exitCode: Int = Fork.java(options, mainClass +: arguments)
  exitCode
}

(test in IntegrationTest) <<= (test in IntegrationTest) dependsOn (test in Test)

(test in IntegrationTest) <<= (test in IntegrationTest) dependsOn (runUberJar in ThisBuild)
