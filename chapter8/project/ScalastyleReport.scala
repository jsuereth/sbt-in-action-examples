
import java.io._
import scala.xml._
import org.apache.velocity.VelocityContext
import org.apache.velocity.app.Velocity
import scala.collection.convert.WrapAsJava._
import java.util.HashMap
import java.util.Collection

object ScalastyleReport {
  case class ScalastyleError(name: String, line: String, level: String, message: String)

  def report(outputDir: File, outputFile: String, templateFile: File, reportXml: File): File = {
    def attr(node: Node, name: String) = (node \\ ("@" + name)).text

    val xml = XML.loadFile(reportXml)

    val errors = asJavaCollection((xml \\ "checkstyle" \\ "file").map(f => {
      val name = attr(f, "name")
      (f \\ "error").map { e =>
        val line = attr(e, "line")
        val severity = attr(e, "severity")
        val message = attr(e, "message")
        ScalastyleError(name, line, severity, message)
      }
    }).flatten)

    sbt.IO.createDirectory(outputDir)

    val objects = new HashMap[String, Any]()
    objects.put("results", errors)

    val sw = new StringWriter()
    val template = sbt.IO.read(templateFile)
    Velocity.evaluate(new VelocityContext(objects), sw, "velocity", template)

    val file = new File(outputDir, outputFile)
    sbt.IO.write(file, sw.toString())
    file
  }
}