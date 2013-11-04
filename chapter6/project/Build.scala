import sbt._
import Keys._

object KittenBuild extends Build {
  lazy val root =
    Project("root", file("."))
      .configs( IntegrationTest )
      .settings( Defaults.itSettings : _*)
}

trait UberJarRunner {
  def start(): Unit
  def stop(): Unit
}

class MyUberJarRunner(uberJar: File) extends UberJarRunner {
  var p: Option[Process] = None
  def start(): Unit = {
    p = Some(Fork.java.fork(ForkOptions(),
             Seq("-cp", uberJar.getAbsolutePath, "global.Global")))
  }
  def stop(): Unit = p foreach (_.destroy())
}