name := "used-kittens"

version := "1.0"

testOptions in Test += Tests.Argument("html")

libraryDependencies ++= Seq("junit" % "junit" % "4.11" % "test",
                            "org.specs2" % "specs2_2.10" % "1.12.3" % "test",
		            "org.pegdown" % "pegdown" % "1.0.2" % "test")
							
javaOptions in Test += "-Dspecs2.outDir=" + target.value + "/generated/test-reports"

fork in Test := true
