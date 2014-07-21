import org.preownedkittens.sbt.ScalastylePlugin._

version := "1.0"

organization := "org.preownedkittens"

name := "sbt-test"

org.preownedkittens.sbt.HelloPlugin.projectSettings

org.preownedkittens.sbt.HelloPlugin.helloKey := "new message"

org.preownedkittens.sbt.ScalastylePlugin.projectSettings

scalastyleConfig in Test :=  file("test.xml")

scalastyleConfig in Compile :=  file("scalastyle_config.xml")

incremental :=  true

