import org.specs2.mutable.Specification

object LogicSpec extends Specification {
  "The 'matchLikelihood' method" should {
    "be 100% when all attributes match" in {
      val tabby = Kitten("1", List("male", "tabby"))
      val prefs = BuyerPreferences(List("male", "tabby"))
      Logic.matchLikelihood(tabby, prefs) must beGreaterThan(.999)
    }
    "be 0% when no attributes match" in {
      val tabby = Kitten("1", List("male", "tabby"))
      val prefs = BuyerPreferences(List("female", "calico"))
      val result = Logic.matchLikelihood(tabby, prefs) 
      result must beLessThan(.001)
    }
  }
}
