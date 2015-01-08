
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

libraryDependencies += "org.scalatest" %% "scalatest" % "2.0" % "it"

libraryDependencies += "org.seleniumhq.selenium" % "selenium-java" % "2.31.0" % "it"

libraryDependencies += "org.pegdown" % "pegdown" % "1.0.2" % "it"

// TODO - cross platform driver.
javaOptions in IntegrationTest += "-Dwebdriver.chrome.driver=" + (baseDirectory.value / "src/it/resources/chromedriver.exe").getAbsolutePath

testOptions += Tests.Argument(TestFrameworks.ScalaTest, "-h", (target.value / "html-test-report").getAbsolutePath)

// ----------------
// Onejar packaging
// ----------------

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
  val allFiles = (files(binDir) ++ files(depDir)).distinct
  // grab distinct files, prevent duplicate entries (happening for some reason)
  val distinctFiles = allFiles.map {
    case (file, name) => name -> file
  }.toMap.map {
    case (name, file) => file -> name
  }.toSeq
  sbt.IO.zip(distinctFiles, buildJar)
}

val uberJarRunner = taskKey[UberJarRunner]("run the uber jar")

uberJarRunner := new MyUberJarRunner(createUberJar.value)

testOptions in IntegrationTest += Tests.Setup { () => uberJarRunner.value.start() }

testOptions in IntegrationTest += Tests.Cleanup { _ => uberJarRunner.value.stop() }

com.github.retronym.SbtOneJar.oneJarSettings
