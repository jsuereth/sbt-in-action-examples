import sbt._
import Keys._

trait UberJarRunner {
  def start(): Unit
  def stop(): Unit
}

class MyUberJarRunner(uberJar: File) extends UberJarRunner {
  var p: Option[Process] = None
  def start(): Unit = {
    p = Some(Fork.java.fork(ForkOptions(),
             Seq("-jar", uberJar.getAbsolutePath)))
  }
  def stop(): Unit = p foreach (_.destroy())
}