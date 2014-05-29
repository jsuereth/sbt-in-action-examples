
package org.preownedkittens.sbt


import java.util.Date
import java.util.jar.JarEntry
import java.util.jar.JarFile
import scala.io.Codec
import sbt.Compile
import sbt.ConfigKey.configurationToKey
import sbt.File
import sbt.IO
import sbt.InputKey
import sbt.Keys.scalaSource
import sbt.Keys.streams
import sbt.Keys.target
import sbt.Logger
import sbt.Plugin
import sbt.Project
import sbt.Scoped.t3ToTable3
import sbt.Scoped.t6ToTable6
import sbt.SettingKey
import sbt.TaskKey
import sbt.file
import sbt.inputTask
import sbt.richFile
import sbt.std.TaskStreams
import sbt.ScopedKey
import sbt.AutoPlugin
import sbt.PluginTrigger
import sbt.Plugins

object HelloPlugin extends sbt.AutoPlugin {
  // override def trigger: PluginTrigger = Plugins.allRequirements

  // import autoImport._
 
  override def projectSettings = Seq(
    helloTask <<= inputTask {
      (argTask: TaskKey[Seq[String]]) => {
        (argTask, scalaSource, helloKey, streams) map {
          (args, sourceDir, helloKeyValue, streams) => { streams.log.info("Hello " + sourceDir.getAbsolutePath) }
        }
      }
    },
    helloKey := "default value"
  )

  //object autoImport {
		lazy val helloTask = InputKey[Unit]("hello", "Prints Hello world.")
    lazy val helloKey = SettingKey[String]("default value for hello world")
  //} 
}
