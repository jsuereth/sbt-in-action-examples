
// These are dependencies for Play

libraryDependencies ++= Seq(
  "play" %% "play" % "2.1.1",
  "eu.teamon" %% "play-navigator" % "0.4.0",
  "org.webjars" % "jquery" % "1.9.1",
  "play" %% "anorm" % "2.1.1",
  "play" %% "play-jdbc" % "2.1.1",
  "org.fusesource.scalate" %% "scalate-core" % "1.6.1"
)

// scalatest

fork in IntegrationTest := true

libraryDependencies += "org.scalatest" %% "scalatest" % "2.0" % "it"

libraryDependencies += "org.seleniumhq.selenium" % "selenium-java" % "2.31.0" % "it"

libraryDependencies += "org.pegdown" % "pegdown" % "1.0.2" % "it"

def chromeDriver = if (System.getProperty("os.name").startsWith("Windows")) "chromedriver.exe" else "chromedriver"

javaOptions in IntegrationTest += "-Dwebdriver.chrome.driver=" + (baseDirectory.value / "src/it/resources" / chromeDriver).getAbsolutePath

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

def isLocal(f: File, base: File) = sbt.IO.relativize(base, f).isDefined

def unpack(target: File, f: File) = {
  if (f.isDirectory) sbt.IO.copyDirectory(f, target)
  sbt.IO.unzip(f, target, unpackFilter(target))
}

def unpackJarSeq(files: Seq[File], target: File, base: File, local: Boolean) = {
  files.filter(f => local == isLocal(f, base)).map(f => unpack(target, f))
}

val unpackJars = taskKey[Seq[_]]("unpacks dependent jars into target/dependent-jars")

unpackJars := {
  val dir = createDependentJarDirectory.value
  val bd = (baseDirectory in ThisBuild).value
  val classpathJars = Build.data((dependencyClasspath in Runtime).value)

  unpackJarSeq(classpathJars, dir, bd, false)
}

val createUberJar = taskKey[File]("create jar which we will run")

createUberJar := {
  val ignored = unpackJars.value
  val bd = (baseDirectory in ThisBuild).value
  val output = target.value / "build.jar"
  val classpathJars = Build.data((dependencyClasspath in Runtime).value)

  sbt.IO.withTemporaryDirectory ( td => {
    unpackJarSeq(classpathJars, td, bd, true)
    create (dependentJarDirectory.value, td, (baseDirectory.value / "src/main/uber"), output)
  })

  output
}

def create(depDir: File, localDir: File, extraDir: File, buildJar: File) = {
  def files(dir: File) = {
    val fs = (dir ** "*").get.filter(d => d != dir)
    fs.map(x => (x, x.relativeTo(dir).get.getPath))
  }
  val allFiles = (files(localDir) ++ files(depDir) ++ files(extraDir)).distinct
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

val runUberJar = taskKey[Int]("run the uber jar")

runUberJar := {
    val uberJar = createUberJar.value
    val options = ForkOptions()
    val arguments = Seq("-jar", uberJar.getAbsolutePath)
    Fork.java(options, arguments)
}

