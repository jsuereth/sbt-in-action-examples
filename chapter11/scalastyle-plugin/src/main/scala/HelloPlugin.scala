
package org.preownedkittens.sbt

import sbt._
import sbt.Keys._
import complete.DefaultParsers._

object HelloPlugin extends sbt.AutoPlugin {
  lazy val hello = inputKey[Unit]("Prints Hello world.")
  lazy val helloKey = SettingKey[String]("default message for hello")
 
  override def projectSettings = Seq(
    hello := {
      val args: Seq[String] = spaceDelimited("<arg>").parsed
      val sourceDir = (scalaSource in Compile).value
      streams.value.log.info("Hello " + helloKey.value + " " + args.mkString(",") + " " + sourceDir.getAbsolutePath)
    },
    helloKey := "default message"
  )
}
