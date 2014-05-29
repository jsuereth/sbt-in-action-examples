resolvers += "Local Maven Repository" at "file:///dev/repo"

{
  val pluginVersion = System.getProperty("plugin.version")
  if(pluginVersion == null)
    throw new RuntimeException("""|The system property 'plugin.version' is not defined.
                                  |Specify this property using the scriptedLaunchOpts -D.""".stripMargin)
  else addSbtPlugin("org.preownedkittens.sbt" % "scalastyle-sbt-plugin" % pluginVersion)
}