package io.buddho.scalatest.gatling.example.google

import io.buddho.scalatest.gatling.DurationConversions._
import io.buddho.scalatest.gatling.example.google.scenarios.FeederSupport._
import io.buddho.scalatest.gatling.example.google.scenarios.GoogleScenarios._
import io.buddho.scalatest.gatling.example.protocol.BrowserProtocols._
import io.gatling.core.Predef._
import org.scalatest.fixture

import scala.concurrent.duration._


class GoogleSimulation extends fixture.SimulationFunSuite {

  // shared tier data fixture
  val tiers = (1 to 12 by 4).toList

  /**
   * Example simulation that loads the Google home page
   */
   simulate("Google Home Page") in { simulation =>
    simulation(
      homePage
        .inject(atOnceUsers(1))
        .protocols(
          chromeBrowser.baseURL("https://www.google.com")
        )
    )
  }

  /**
   * Example simulation that performs a tiered Google search with feeder data.
   * In this example the tier data is applied to the number of concurrent users.
   */
  simulate("Google Search") withTiers tiers in { (simulation, tier) =>

    simulation(
      search(searchTerms)
        .inject(atOnceUsers(tier))
        .protocols(
          chromeBrowser.baseURL("https://www.google.com")
        )
      )
  }

  /**
   * Example simulation that loads the Google home page, performs a Google search in parallel
   * and uses Gatling assertions to set simulation acceptance criteria.
   */
  simulate("Parallel Homepage/Search with Assertions") in { simulation =>

    simulation(
      homePage
        .inject(atOnceUsers(5))
        .protocols(
          chromeBrowser.baseURL("https://www.google.com")
        ),
      search(searchTerms)
        .inject(atOnceUsers(5))
        .protocols(
          chromeBrowser.baseURL("https://www.google.com")
        )
    ).assertions(
        global.responseTime.max lessThan 3.seconds,
        details("Home").responseTime.max lessThan 1.8.seconds,
        details("Search").responseTime.max lessThan 2.seconds
      )

  }


  /**
   * Example simulation that fails on Gatling assertions.
   */
  simulate("Search with Assertion Failures") in { simulation =>

    simulation(
      search(searchTerms)
        .inject(atOnceUsers(1))
        .protocols(
          chromeBrowser.baseURL("https://www.google.com")
        )
    ).assertions(
        global.responseTime.max lessThan 1.millis
      )
  }

}
