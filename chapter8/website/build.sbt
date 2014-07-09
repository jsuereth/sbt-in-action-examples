import AssemblyKeys._

// These are dependencies for Play

libraryDependencies ++= Seq(
  "play" %% "play" % "2.1.1",
  "eu.teamon" %% "play-navigator" % "0.4.0",
  "org.webjars" % "jquery" % "1.9.1",
  "play" %% "anorm" % "2.1.1",
  "play" %% "play-jdbc" % "2.1.1",
  "org.fusesource.scalate" %% "scalate-core" % "1.6.1",
  "org.apache.derby" % "derby" % "10.10.1.1"
)

// scalatest

fork in IntegrationTest := true

libraryDependencies += "org.scalatest" % "scalatest_2.10" % "2.0" % "it"

libraryDependencies += "org.seleniumhq.selenium" % "selenium-java" % "2.31.0" % "it"

libraryDependencies += "org.pegdown" % "pegdown" % "1.0.2" % "it"

// TODO - cross platform driver.
javaOptions in IntegrationTest += "-Dwebdriver.chrome.driver=" + (baseDirectory.value / "src/it/resources/chromedriver.exe").getAbsolutePath

testOptions += Tests.Argument(TestFrameworks.ScalaTest, "-h", (target.value / "html-test-report").getAbsolutePath)

// ------------------
// Assembly packaging
// ------------------

assemblySettings

mainClass in assembly := Some("Global")

mergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) =>
  {
    case "application.conf" => MergeStrategy.concat
    case "reference.conf" => MergeStrategy.concat
    case "META-INF/spring.tooling" => MergeStrategy.concat
    case "overview.html" => MergeStrategy.rename
    case x => old(x)
  }
}

excludedJars in assembly <<= (fullClasspath in assembly) map { cp => 
  cp filter { f =>
    (f.data.getName contains "commons-logging") ||
    (f.data.getName contains "sbt-link")
  }
}

val uberJarRunner = taskKey[UberJarRunner]("run the uber jar")

uberJarRunner := new MyUberJarRunner(assembly.value)

(test in IntegrationTest) := {
  val x = (test in Test).value
  (test in IntegrationTest).value
}

testOptions in IntegrationTest += Tests.Setup { () => uberJarRunner.value.start() }

testOptions in IntegrationTest += Tests.Cleanup { _ => uberJarRunner.value.stop() }
