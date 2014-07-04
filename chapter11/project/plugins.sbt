resolvers += "typesafe-release" at "http://typesafe.artifactoryonline.com/typesafe/repo/"

libraryDependencies <+= (sbtVersion) { sv =>
  "org.scala-sbt" % "scripted-plugin" % sv
}

