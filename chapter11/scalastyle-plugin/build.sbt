version := "1.0"

sbtPlugin := true

organization := "org.preownedkittens.sbt"

name := "scalastyle-plugin"

libraryDependencies ++= Seq("org.scalastyle" %% "scalastyle" % "0.5.0")

ScriptedPlugin.scriptedSettings

scriptedLaunchOpts <++= version apply { version =>
  Seq("-Xmx1024M", "-XX:MaxPermSize=256M", "-Dplugin.version=" + version)
}

scriptedBufferLog := false


