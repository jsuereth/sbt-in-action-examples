package org.usedkittens;

import org.scalatest._
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.events._
import org.scalatest.selenium._
import org.openqa.selenium.WebDriver


class SeleniumSpec extends FlatSpec with ShouldMatchers with BeforeAndAfter with BeforeAndAfterAll with HtmlUnit {
  val homePage: String = "http://localhost:9000"

  "Home page" should "redirect to kitten list and display all available kittens" in {
    go to (homePage)
    currentUrl should startWith (homePage + "/kittens")
  }
  
  it should "show three dropdown lists of attributes in sorted order, with a default of -- Select --" in {
    def select(id: String) = findAll(xpath("//select[@id='" + id + "']/option")).map{_.text}.toList
    def assertList(list: Seq[String]) = assert(list.size > 1 && list(0) == "-- Select --" && list.sorted == list)

    go to homePage + "/kittens"

    assertList(select("select1"))
    assertList(select("select2"))
    assertList(select("select3"))
  }

  "When I select three options and click Find" should "display a list of matching kittens with the option to buy them" in {
    go to (homePage + "/kittens")

    singleSel("select1").value = "17" // Happy
    singleSel("select2").value = "10" // Overweight
    singleSel("select3").value = "19" // Sleepy

    click on "findKitten"

    currentUrl should startWith(homePage + "/show_selected_kittens")

    purchaseForms() should contain ("Fred")
    purchaseForms() should contain ("Dionysus")
    purchaseForms() should not  ("Sleepy")
  }

  private def purchaseForms() = findAll(xpath("//form/input[@id='purchase']/../li")).map{_.text}.toList

  override def afterAll() {
    // webDriver.quit()
  }
}
