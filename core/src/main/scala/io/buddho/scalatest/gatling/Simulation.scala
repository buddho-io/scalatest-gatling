package io.buddho.scalatest.gatling

import io.gatling.core.assertion.Assertion
import io.gatling.core.config.GatlingConfiguration._
import io.gatling.core.config.{Protocol, Protocols}
import io.gatling.core.controller.Timings
import io.gatling.core.controller.throttle.{Throttling, ThrottlingProtocol}
import io.gatling.core.pause._
import io.gatling.core.scenario.{Simulation => GatlingSimulation, Scenario}
import io.gatling.core.session._
import io.gatling.core.structure.PopulatedScenarioBuilder

import scala.concurrent.duration.{Duration, FiniteDuration}


class Simulation(simulationName: String, simulationTimeOut: Int = configuration.core.timeOut.simulation) {

  private var _assertions = Seq.empty[Assertion]
  private var _scenarios: List[PopulatedScenarioBuilder] = Nil
  private var _globalProtocols = Protocols()
  private var _maxDuration: Option[FiniteDuration] = None
  private var _globalThrottling: Option[ThrottlingProtocol] = None

  val timeout: Int = simulationTimeOut
  val name: String = simulationName

  val simulation = new GatlingSimulation {

    override def assertions: Seq[Assertion] = _assertions

    // TODO figure out what to do with the protected build method
//    override def scenarios: List[Scenario] = {
//      require(_scenarios.nonEmpty, "No scenario set up")
//      _scenarios.foreach(scn => require(scn.scenarioBuilder.actionBuilders.nonEmpty, s"Scenario ${scn.scenarioBuilder.name} is empty"))
//      _scenarios.map(_.build(_globalProtocols))
//    }

    override def timings: Timings = {
      // TODO clean up
      val perScenarioThrottlings: Map[String, ThrottlingProtocol] = _scenarios
        .map(scn => scn
        .populationProtocols.getProtocol[ThrottlingProtocol]
        .map(throttling => scn.scenarioBuilder.name -> throttling)).flatten.toMap
      Timings(_maxDuration, _globalThrottling, perScenarioThrottlings)
    }
    def setUp(): GatlingSimulation#SetUp = super.setUp(_scenarios)
  }

  def apply(scenarios: PopulatedScenarioBuilder*): Simulation = {
    _scenarios = scenarios.toList
    this
  }

  def assertions(assertion: Assertion*): Simulation = {
    _assertions = _assertions ++ assertion
    this
  }

  def protocols(ps: Protocol*): Simulation = protocols(ps.toIterable)
  def protocols(ps: Iterable[Protocol]): Simulation = {
    _globalProtocols = _globalProtocols ++ ps
    this
  }

  def maxDuration(duraction: FiniteDuration): Simulation = {
    _maxDuration = Some(duraction)
    this
  }

  def throttle(throttlingBuilders: Throttling*): Simulation = throttle(throttlingBuilders.toIterable)

  def throttle(throttlingBuilders: Iterable[Throttling]): Simulation = {

    val steps = throttlingBuilders.toList.map(_.steps).reverse.flatten
    val throttling = Throttling(steps).protocol
    _globalThrottling = Some(throttling)
    _globalProtocols = _globalProtocols + throttling
    this
  }

  def disablePauses: Simulation = pauses(Disabled)
  def constantPauses: Simulation = pauses(Constant)
  def exponentialPauses: Simulation = pauses(Exponential)
  def customPauses(custom: Expression[Long]): Simulation = pauses(Custom(custom))
  def uniformPauses(plusOrMinus: Double): Simulation = pauses(UniformPercentage(plusOrMinus))
  def uniformPauses(plusOrMinus: Duration): Simulation = pauses(UniformDuration(plusOrMinus))
  def pauses(pauseType: PauseType): Simulation = protocols(PauseProtocol(pauseType))

}
