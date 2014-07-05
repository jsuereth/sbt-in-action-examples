// specs2 libraries.

libraryDependencies += "org.specs2" %% "specs2" % "1.14" % "test"

libraryDependencies += "org.pegdown" % "pegdown" % "1.0.2" % "test"
                            
testOptions += Tests.Argument(TestFrameworks.Specs2, "html")

javaOptions in Test += "-Dspecs2.outDir=" + (target.value / "generated/test-reports").getAbsolutePath

fork in Test := true

// scalacheck

libraryDependencies += "org.scalacheck" % "scalacheck_2.10.0" % "1.10.0" % "test"
                            
testOptions += Tests.Argument(TestFrameworks.ScalaCheck, "-s", "500")
