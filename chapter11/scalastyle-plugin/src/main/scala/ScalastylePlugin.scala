
package org.preownedkittens.sbt


import java.util.Date
import java.util.jar.JarEntry
import java.util.jar.JarFile
import scala.io.Codec
import sbt._
import sbt.Test
import sbt.ConfigKey.configurationToKey
import sbt.File
import sbt.IO._
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
import org.scalastyle._
import sbt.Cache._

object ScalastylePlugin extends sbt.AutoPlugin {
  override def projectSettings = Seq(
    scalastyleConfig := file("scalastyle-config.xml"),
    incremental := false,
    scalastyleTask <<= inputTask {
      (argTask: TaskKey[Seq[String]]) => {
        (argTask, scalaSource in Compile, scalastyleConfig in Compile, incremental, target in Compile, streams) map {
          (args, sourceDir, configValue, inc, targetValue, streams) => { doScalastyle(configValue, sourceDir, inc, targetValue, streams.log) }
        }
      }
    },
    scalastyleTask in Test := inputTask {
      (argTask: TaskKey[Seq[String]]) => {
        (argTask, scalaSource in Test, scalastyleConfig in Test, incremental, target in Test, streams) map {
          (args, sourceDir, configValue, inc, targetValue, streams) => { doScalastyle(configValue, sourceDir, inc, targetValue, streams.log) }
        }
      }
    },
    scalastyle2 := {
      val sourceDir = (scalaSource in Compile).value
      val configValue = (scalastyleConfig in Compile).value
      val inc = incremental.value
      val targetValue = (target in Compile).value
      val streamsValue = streams.value

      doScalastylePrevious(configValue, sourceDir, inc, scalastyle2.previous, streamsValue.log)
    } 
  )

  lazy val scalastyleTask = InputKey[Unit]("scalastyle", "Runs scalastyle.")
  lazy val scalastyleConfig = SettingKey[File]("scalastyleConfig", "configuration file for scalastyle")
  lazy val incremental = SettingKey[Boolean]("incremental", "scalastyle does incremental checks")
  lazy val scalastyle2 = taskKey[Long]("Runs scalastyle.")

  private def lastModified(lastRun: Long)(file: File) = file.lastModified > lastRun

  private def changedFiles(sourceDir: File, lastRunFile: File) = {
    val lastRunDate = try {
        read(lastRunFile).trim().toLong
      } catch {
        case _: Exception => 0L
      }
    (sourceDir ** "*.scala").get.filter(lastModified(lastRunDate))
  }
  
  def doScalastyle(config: File, sourceDir: File, incremental: Boolean, targetDir: File, logger: Logger): Long = {
    val lastRunFile = targetDir / "scalastyle.lastrun"

    if (config.exists) {
      val sources = if (incremental) changedFiles(sourceDir, lastRunFile)
      else List(sourceDir)

      val messages = runScalastyle(config, Directory.getFiles(None, sources))
      val errors = messages.collect{ case x: StyleError[_] => 1}.size

      sbt.IO.write(lastRunFile, "" + new java.util.Date().getTime())

      logger.info(errors + " errors found")
      new java.util.Date().getTime()
    } else {
      sys.error("%s does not exist".format(config))
    }
  }

  private def changedFilesPrevious(sourceDir: File, lastRun: Option[Long]) = {
    val lastRunDate = lastRun.getOrElse(0L);
    (sourceDir ** "*.scala").get.filter(lastModified(lastRunDate))
  }
  
  def doScalastylePrevious(config: File, sourceDir: File, incremental: Boolean, lastRun: Option[Long], logger: Logger): Long = {
    if (config.exists) {
      val sources = if (incremental) changedFilesPrevious(sourceDir, lastRun)
      else List(sourceDir)

      val messages = runScalastyle(config, Directory.getFiles(None, sources))
      val errors = messages.collect{ case x: StyleError[_] => 1}.size

      logger.info(errors + " errors found")
      new java.util.Date().getTime()
    } else {
      sys.error("%s does not exist".format(config))
    }
  }

  private[this] def runScalastyle(config: File, sources: List[org.scalastyle.FileSpec]) = {
    val configuration = ScalastyleConfiguration.readFromXml(config.absolutePath)
    new ScalastyleChecker().checkFiles(configuration, sources)
  }


}
