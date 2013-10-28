import sbt._
import Keys._

class StartStop {
  var _process: Option[Process] = None
  println("ss")

  def start(process: Process): Process = {
    println("start")
    _process = Some(process)
    process
  }
  def stop(): Unit = {
    println("stop")
    _process match {
      case Some(x) => x.destroy()
      case None =>
    }
  }
}

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