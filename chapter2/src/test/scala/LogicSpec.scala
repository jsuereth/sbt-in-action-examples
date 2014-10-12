package org.preownedkittens

import org.specs2.mutable.Specification

object LogicSpec extends Specification {
  "The 'matchLikelihood' method" should {
    "be 100% when all attributes match" in {
      val tabby = Kitten("1", List("male", "tabby"))
      val prefs = BuyerPreferences(List("male", "tabby"))
      Logic.matchLikelihood(tabby, prefs) must beGreaterThan(0.999)
    }
    "be 100% when all attributes match (with duplicates)" in {
      val tabby = Kitten("1", List("male", "tabby", "male"))
      val prefs = BuyerPreferences(List("male", "tabby", "tabby"))
      Logic.matchLikelihood(tabby, prefs) must beGreaterThan(0.999)
    }
    "be 0% when no attributes match" in {
      val tabby = Kitten("1", List("male", "tabby"))
      val prefs = BuyerPreferences(List("female", "calico"))
      val result = Logic.matchLikelihood(tabby, prefs) 
      result must beLessThan(0.001)
    }
    "be 66% when two from three attributes match" in {
      val tabby = Kitten("1", List("female", "calico", "thin"))
      val prefs = BuyerPreferences(List("female", "calico", "overweight"))
      val result = Logic.matchLikelihood(tabby, prefs) 
      result must beBetween(0.66, 0.67)
    }
  }
}
