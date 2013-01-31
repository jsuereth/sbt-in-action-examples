import org.specs2.mutable.Specification

object LogicSpec extends Specification {
  "The 'matchLiklihood' method" should {
    "be 100% when all attributes match" in {
      val tabby = Kitten("1", List("male", "tabby"))
      val prefs = BuyerPreferences(List("male", "tabby"))
      Logic.matchLiklihood(tabby, prefs) must beGreaterThan(.999)
    }
  }
}
