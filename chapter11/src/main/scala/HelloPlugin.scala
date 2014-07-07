
package org.preownedkittens.sbt

import sbt._
import sbt.Keys._

object HelloPlugin extends sbt.AutoPlugin {
  lazy val helloTask = InputKey[Unit]("hello", "Prints Hello world.")
  lazy val helloKey = SettingKey[String]("default message for hello")
 
  override def projectSettings = Seq(
    helloTask <<= inputTask {
      (argTask: TaskKey[Seq[String]]) => {
        (argTask, scalaSource in Compile, helloKey, streams) map {
          (args, sourceDir, helloKeyValue, streams) => { streams.log.info("Hello " + helloKeyValue + " " + args.mkString(",") + " " + sourceDir.getAbsolutePath) }
        }
      }
    },
    helloKey := "default message"
  )
}
