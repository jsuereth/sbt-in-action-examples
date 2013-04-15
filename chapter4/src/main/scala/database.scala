package org.usedkittens.database;

import anorm._
import anorm.SqlParser._
import play.api.db._
import play.api.Play.current

case class Kitten(id: Long, name: String)
case class SelectKitten(select1: String, select2: String, select3: String)
case class Attribute(id: Long, label: String)
case class KittenAttribute(id: Long, kittenId: Long, attributeId: Long)

object Kitten {
  val kitten = {
    get[Long]("id") ~ 
    get[String]("name") map {
      case id~name => Kitten(id, name)
    }
  }

  def all(): List[Kitten] = DB.withConnection { implicit c =>
    SQL("select * from kitten").as(kitten *)
  }

  def create(name: String) {
    DB.withConnection { implicit c =>
      SQL("insert into kitten (name) values ({name})").on(
        'name -> name
      ).executeUpdate()
    }
  }

  def delete(id: Long) {
    DB.withConnection { implicit c =>
      SQL("delete from kitten where id = {id}").on(
        'id -> id
      ).executeUpdate()
    }
  }
  
}

object Attribute {
  val attribute = {
    get[Long]("id") ~ 
    get[String]("label") map {
      case id~label => Attribute(id, label)
    }
  }

  def all(): List[Attribute] = DB.withConnection { implicit c =>
    SQL("select * from attribute").as(attribute *)
  }

  def allForSelect(): Seq[(String, String)] = all().sortBy(_.label).map(a => (a.id + "", a.label))
}


object KittenAttribute {
  val kittenAttribute = {
    get[Long]("id") ~ 
    get[Long]("kitten_id") ~ 
    get[Long]("attribute_id") map {
      case id~kittenId~attributeId => KittenAttribute(id, kittenId, attributeId)
    }
  }

  def all(): List[KittenAttribute] = DB.withConnection { implicit c =>
    SQL("select * from kitten_attribute").as(kittenAttribute *)
  }

  def allForKitten(kitten: Kitten): Seq[KittenAttribute] = all().filter(ka => ka.kittenId == kitten.id)
}
