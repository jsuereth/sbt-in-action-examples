package controllers

import play.api._
import http.{Writeable, ContentTypeOf, ContentTypes}
import mvc.Codec
import play.api.Play.current
import org.fusesource.scalate.layout.DefaultLayoutStrategy

object Scalate {

  import org.fusesource.scalate._
  import org.fusesource.scalate.util._

  var format = Play.configuration.getString("scalate.format") match {
    case Some(configuredFormat) => configuredFormat
    case _ => "scaml"
  }

  lazy val scalateEngine = {
    val engine = new TemplateEngine
    engine.resourceLoader = new FileResourceLoader(Some(Play.getFile("app/views")))
    engine.layoutStrategy = new DefaultLayoutStrategy(engine, "app/views/layouts/default." + format)
    engine.classpath = "target/tmp/classes"
    engine.workingDirectory = Play.getFile("target/tmp")
    engine.combinedClassPath = true
    engine.classLoader = Play.classloader
    engine
  }

  def apply(template: String) = Template(template)

  case class Template(name: String) {

    def render(args: (Symbol, Any)*) = {
      ScalateContent{
        scalateEngine.layout(name, args.map {
          case (k, v) => k.name -> v
        } toMap)
      }
    }

  }

  case class ScalateContent(val cont: String)

  def foobar(codec: Codec)(scalate: ScalateContent): Array[Byte] = codec.encode(scalate.cont)

  implicit def writeableOf_ScalateContent(implicit codec: Codec): Writeable[ScalateContent] = {
    Writeable[ScalateContent](foobar(codec) _)
  }

  implicit def contentTypeOf_ScalateContent(implicit codec: Codec): ContentTypeOf[ScalateContent] = {
    ContentTypeOf[ScalateContent](Some(ContentTypes.HTML))
  }
}
