package org.usedkittens;

import org.scalatest._
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.events._
import org.scalatest.selenium._
import org.openqa.selenium.WebDriver


import org.scalatest._
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.events._
import org.scalatest.selenium._
import org.scalatest.junit._
import org.openqa.selenium.WebDriver

class SeleniumSpec extends FlatSpec with ShouldMatchers with BeforeAndAfter with BeforeAndAfterAll with Chrome {
  val homePage: String = "http://localhost:9000"

  
  "Home page" should "redirect to kitten list" in {
    go to "http://localhost:9000"
    currentUrl should startWith ("http://localhost:9000/kittens")
  }

  it should "show three dropdown lists of attributes in sorted order" in {
    def select(name: String) = findAll(xpath("//select[@name='" + name + "']/option")).map { _.text }.toList
    def assertListCompleteAndIsSorted(list: Seq[String]) = {
      list.size should be(20)
      list.sorted should be(list)
    }

    go to homePage + "/kittens"

    assertListCompleteAndIsSorted(select("select1"))
    assertListCompleteAndIsSorted(select("select2"))
    assertListCompleteAndIsSorted(select("select3"))
  }

  private def purchaseForms() = findAll(xpath("//form/li/input[@id='purchase']/..")).map { _.text }.toList

  override def afterAll() {
    webDriver.quit()
  }
}
