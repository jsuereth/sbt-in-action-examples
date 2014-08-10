package org.preownedkittens.sbt

import sbt._
import sbt.Keys._
import complete.DefaultParsers._

object DependPlugin extends sbt.AutoPlugin {
  import autoImport._

  override def projectSettings = Seq(
    extTask := {
      val args: Seq[String] = spaceDelimited("<arg>").parsed
      streams.value.log.info("Hello depend " + args.mkString(","))
    }
  )

  override def trigger = Plugins.allRequirements

  override def requires = ScalastylePlugin

  object autoImport {
    lazy val extTask = InputKey[Unit]("depend", "Prints hello.")
  }
}
