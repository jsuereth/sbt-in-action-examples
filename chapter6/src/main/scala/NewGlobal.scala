package foo;

import java.io.File

import controllers.Assets
import play.api.libs.json.Json
import play.api.GlobalSettings
import play.core.StaticApplication
import play.navigator._
import play.api.mvc.RequestHeader
import controllers.Scalate
import global.Routes


object NewGlobal extends App with GlobalSettings {

Thread.sleep(100000)

  new play.core.server.NettyServer(new StaticApplication(new File(".")), 9000)
  override def onRouteRequest(request: RequestHeader) = Routes.onRouteRequest(request)
  override def onHandlerNotFound(request: RequestHeader) = Routes.onHandlerNotFound(request)
}
