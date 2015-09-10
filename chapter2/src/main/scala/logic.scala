package org.preownedkittens

object Logic {
  /** Determines the match likelihood and returns % match. */
  def matchLikelihood(kitten: Kitten, buyer: BuyerPreferences): Double = {
    val matches = buyer.attributes map { attribute =>
      kitten.attributes contains attribute
    }
    val nums = matches map { b => if(b) 1.0 else 0.0 }
    if (nums.length > 0) nums.sum / nums.length else 0.0
  }
}
