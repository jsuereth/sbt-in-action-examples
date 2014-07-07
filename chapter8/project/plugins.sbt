addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "0.4.0")

resolvers += "sonatype-releases" at "https://oss.sonatype.org/content/repositories/releases/"

addSbtPlugin("org.scala-sbt.plugins" % "sbt-onejar" % "0.8")

libraryDependencies ++= Seq(
  "org.apache.velocity" % "velocity" % "1.7"
)