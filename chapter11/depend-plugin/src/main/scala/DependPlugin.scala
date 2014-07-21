package org.preownedkittens.sbt

import sbt._
import sbt.Keys._

object DependPlugin extends sbt.AutoPlugin {
  import autoImport._

  override def projectSettings = Seq(
    extTask <<= inputTask {
      (argTask: TaskKey[Seq[String]]) => {
        (argTask, streams) map {
          (args, streams) => { streams.log.info("Hello depend " + args.mkString(",")) }
        }
      }
    }
  )

  override def trigger = Plugins.allRequirements

  override def requires = ScalastylePlugin

  object autoImport {
    lazy val extTask = InputKey[Unit]("depend", "Prints hello.")
  }
}
