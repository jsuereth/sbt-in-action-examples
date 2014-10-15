
import java.io._
import scala.xml._
import org.apache.velocity.VelocityContext
import org.apache.velocity.app.Velocity
import scala.collection.convert.WrapAsJava._
import java.util.HashMap

object ScalastyleReport {
  case class ScalastyleError(name: String, line: String, level: String, message: String)

  def report(outputDir: File, outputFile: String, templateFile: File, reportXml: File): File = {
    // get text contents of an attribute
    def attr(node: Node, name: String) = (node \\ ("@" + name)).text

    val xml = XML.loadFile(reportXml)

    // get scalastyle errors from XML
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

    val context = new HashMap[String, Any]()
    context.put("results", errors)

    val sw = new StringWriter()
    val template = sbt.IO.read(templateFile)
    Velocity.evaluate(new VelocityContext(context), sw, "velocity", template)

    val reportFile = new File(outputDir, outputFile)
    sbt.IO.write(reportFile, sw.toString())
    reportFile
  }
}