package io.buddho.scalatest.gatling.example.google.scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.buddho.scalatest.gatling.example.google.scenarios.FeederSupport.searchTerms

object GoogleActions {

  val homePageAction =
    http("Home")
      .get("/")
      .check(status.is(200))

  val searchAction =
    http("Search")
      .get("""/search?output=search&q=${term}""")
      .check(regex("${regex}").exists)

}

object GoogleScenarios {

  import GoogleActions._

  def homePage = scenario("Google Home Page")
    .exec(homePageAction)

  def search(termFeeder: Feeder[String]) = scenario("Google Search")
    .exec(homePageAction)
    .pause(10)
    .feed(termFeeder)
    .exec(searchAction)
}
