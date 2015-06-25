package io.buddho.scalatest.gatling.example.google.scenarios

import io.gatling.core.feeder.Feeder


object FeederSupport {

  def searchTerms: Feeder[String] = {
    val terms = List(
      "USA vs Belgium", "Argentina vs Switzerland", "Gosta Rica vs Greece", "France vs Nigeria",
      "Brazil vs Chile", "Columbia vs Uruguay", "NHL Draft 2014", "World Cup", "NBA Draft", "USA vs Germany"
    )
    terms.map(term => Map(
      "term" -> term.replaceAll(" ", "+"),
      "regex" -> s"[$term]"
    )).toIterator
  }

}
