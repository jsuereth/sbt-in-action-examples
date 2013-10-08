package global

import java.io.File

import controllers.Assets
import play.api.libs.json.Json
import play.api.mvc.{Controller, Action, RequestHeader}
import play.api.GlobalSettings
import play.core.StaticApplication
import play.navigator._
import play.navigator.{PlayNavigator, PlayResourcesController}
import scala.collection.mutable
import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import org.preownedkittens.database._
import controllers.Scalate

object Routes extends PlayNavigator {
  val index = GET on root to redirect("kittens")
  val kittens = GET on "kittens" to { () => Application.kittens }
  val selected = POST on "selected" to { () => Application.selected }
  val purchase = POST on "purchase" to { () => Application.purchase }
}

object Application extends Controller {
  val kittenSelectForm = Form[SelectKitten](
    mapping(
      "select1" -> nonEmptyText,
      "select2" -> nonEmptyText,
      "select3" -> nonEmptyText
    )(SelectKitten.apply)(SelectKitten.unapply)
  )

  def kittens = Action {
    Ok(Scalate("app/views/kittens.scaml").render('title -> "Kitten List",
                'kittens -> Kitten.all(), 'attributes -> Attribute.all()))
  }

  def purchase = TODO

  def selected = Action { request =>
    val body: Option[Map[String, Seq[String]]] = request.body.asFormUrlEncoded

    body.map { map =>
      showSelectedKittens(map.get("select1").get.head, map.get("select2").get.head, map.get("select3").get.head)
    }.getOrElse{
      BadRequest("Expecting form url encoded body")
    }
  }

  def showSelectedKittens(id1: String, id2: String, id3: String) = {
    import org.preownedkittens.Logic._
    val buyerPreferences = org.preownedkittens.BuyerPreferences(Set(id1, id2, id3))

    val kittensWithLikelihood = Kitten.all().map{ k =>
      (k, matchLikelihood(org.preownedkittens.Kitten(k.id, KittenAttribute.allForKitten(k).map("" + _.attributeId).toSet), buyerPreferences))
    }.sortWith((d1, d2) => d1._2 > d2._2).filter(_._2 > 0.5)

    Ok(Scalate("app/views/selected.scaml").render('title -> "Selected kittens", 'kittens -> kittensWithLikelihood))
  }
}

