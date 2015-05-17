import sbt._

trait UberJarRunner {
  def start(): Unit
  def stop(): Unit
}

class MyUberJarRunner(uberJar: File) extends UberJarRunner {
  var p: Option[Process] = None
  def start(): Unit = {
    p = Some(Fork.java.fork(ForkOptions(),
             Seq("-cp", uberJar.getAbsolutePath, "Global")))
  }
  def stop(): Unit = p foreach (_.destroy())
}
