import sbinary.DefaultProtocol._

version := "1.0"

sbtPlugin := true

organization := "org.preownedkittens.sbt"

name := "scalastyle-sbt-plugin"

libraryDependencies ++= Seq("org.scalastyle" %% "scalastyle" % "0.4.0")

val rmpl = taskKey[Unit]("rmpl")

rmpl := println(Process("rm -rf /c/users/mfarwell/.ivy2/local/org.preownedkittens.sbt").lines)

ScriptedPlugin.scriptedSettings

scriptedLaunchOpts <++= version apply { version =>
  Seq("-Xmx1024M", "-XX:MaxPermSize=256M", "-Dplugin.version=" + version)
}

scriptedBufferLog := false


