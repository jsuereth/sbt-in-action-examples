import sbt._
import Keys._

object KittenBuild extends Build {
  lazy val root =
    Project("root", file("."))
      .configs( IntegrationTest )
      .settings( Defaults.itSettings : _*)

  //val foo = taskKey[Seq[sbt.ModuleID]]("stuff")

  //foo := {
    //val filter = ScopeFilter(  configurations = inConfigurations(Compile) )
    //val allSources: Seq[Seq[sbt.ModuleID]] = libraryDependencies.value
    //allSources.flatten
  //}

}