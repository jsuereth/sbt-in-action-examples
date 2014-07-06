package org.preownedkittens

import java.io._
import java.util.Properties

object Analytics {
  def main(args: Array[String]): Unit = {
    val propsFile = new File(sys.props("analytics.properties"))
    val props = new Properties
    val in = new FileInputStream(propsFile)
    try props.load(in)
    finally in.close()
  	println("Running analytics....")
  	println(s"* database: ${props.get("database.url")}")
  }
}