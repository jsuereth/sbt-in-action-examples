package org.usedkittens;

object Logic {
  /** Determines the match liklihood and returns % match. */
  def matchLiklihood(kitten: Kitten, buyer: BuyerPreferences): Double = {
    val matches = buyer.attributes.toList map { attribute => 
      kitten.attributes contains attribute
    }
    val nums = matches map { b => if(b) 1.0 else 0.0 }
    nums.sum / nums.size

  }
}
