name := "used-kittens"

version := "1.0"


// specs2

libraryDependencies += "org.specs2" % "specs2_2.10" % "1.12.3" % "test"

libraryDependencies += "org.pegdown" % "pegdown" % "1.0.2" % "test"
                            
testOptions += Tests.Argument(TestFrameworks.Specs2, "html")

javaOptions in Test += "-Dspecs2.outDir=" + target.value + "/generated/test-reports"

fork in Test := true


// junit

libraryDependencies += "junit" % "junit" % "4.11" % "test"

libraryDependencies += "com.novocode" % "junit-interface" % "0.10-M3" % "test"

testOptions += Tests.Argument(TestFrameworks.JUnit, "-v", "-n", "--run-listener=com.usedkittens.sbt.JUnitListener")

javaOptions in Test += "-Djunit.output.file=" + target.value + "/generated/junit.html"

javaHome := Some(file("/dev/java/jdk1.7.0_09"))

javacOptions in Compile ++= Seq("-target", "1.6", "-source", "1.6")


// scalacheck

libraryDependencies += "org.scalacheck" % "scalacheck_2.10.0" % "1.10.0" % "test"
                            
testOptions += Tests.Argument(TestFrameworks.ScalaCheck, "-s", "500")


// scalatest

libraryDependencies += "org.scalatest" % "scalatest_2.10" % "1.9.1" % "test"
