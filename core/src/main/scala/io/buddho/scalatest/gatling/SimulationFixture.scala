package io.buddho.scalatest.gatling

import java.lang.System._

import akka.util.Timeout
import com.typesafe.scalalogging.StrictLogging
import io.gatling.charts.report.{ReportsGenerationInputs, ReportsGenerator}
import io.gatling.core.akka.GatlingActorSystem
import io.gatling.core.assertion.AssertionValidator
import io.gatling.core.config.GatlingConfiguration
import io.gatling.core.config.GatlingConfiguration._
import io.gatling.core.controller.{Controller, Run}
import io.gatling.core.result.reader.DataReader
import io.gatling.core.scenario.{Simulation => GatlingSimulation}
import org.scalatest.{fixture, _}

import scala.collection.mutable
import scala.concurrent.duration._
import scala.concurrent.{Await, Promise, TimeoutException}
import scala.util.{Failure, Success}


trait SimulationFixture extends BeforeAndAfterAllConfigMap with StrictLogging {
  this: fixture.Suite =>

  type FixtureParam = Simulation

  implicit private def system = GatlingActorSystem.instance

  override protected def beforeAll(configMap: ConfigMap) = {
    super.beforeAll(configMap)

    // Read configuration values provided to ScalaTest
    val resultsDirectory = configMap.getWithDefault("gatling.results", "gatling-results")
    val reportsDirectory = configMap.getWithDefault("gatling.reports", "gatling-reports")
    val generateReports = configMap.getWithDefault("gatling.generator-reports", true)

    // Initialize gatling configuration
    GatlingConfiguration.setUp(mutable.Map(
      "gatling.charting.noReports" -> !generateReports,
      "gatling.core.directory.results" -> resultsDirectory,
      "gatling.core.directory.reportsOnly" -> reportsDirectory
    ))
  }

  def withFixture(test: OneArgTest): Outcome = {
    val simulation = new Simulation(simulationName(test))
    val noArgTest = test.toNoArgTest(simulation)
    val testRunner = () => withFixture(noArgTest)
    gatling( simulation, testRunner )
  }


  private def gatling(simulation: Simulation, testRunner:() => Outcome): Outcome = {

    // used for synchronizing gatling termination with the completion of the simulation
    val termSignal = Promise[Unit]()

    GatlingActorSystem.start()
    system.registerOnTermination(termSignal.success())

    System.gc()
    System.gc()
    System.gc()

    Controller.start()

    try {
      val outcome = testRunner()
      if (outcome.isSucceeded) {
        run(simulation)
      }
      outcome
    } catch {
      case t: TimeoutException => throw new TimeoutException(s"Reach simulation timeout of ${simulation.timeout} seconds")
    } finally {
      GatlingActorSystem.shutdown()
      // block until gatling actor system terminates
      Await.result(termSignal.future, 10 seconds)
    }
  }

  private def run(simulation: Simulation): Unit = {
    simulation.simulation.setUp()

    implicit val timeOut = Timeout(simulation.timeout.seconds)
    val futureResult = Controller ? Run(simulation.simulation, simulation.name, simulation.name, simulation.simulation.timings)

    val result = Await.result(futureResult, simulation.timeout.seconds)

    val runId = result match {
      case Success(runId: String) => runId
      case Failure(t) => throw t
      case unexpected => throw new UnsupportedOperationException(s"Controller replied an unexpected message $unexpected")
    }

    val start = currentTimeMillis

    val dataReader = DataReader.newInstance(runId)

    val assertionResults = AssertionValidator.validateAssertions(dataReader)

    val reportsGenerationInputs = ReportsGenerationInputs(runId, dataReader, assertionResults)
    if (reportsGenerationEnabled) generateReports(reportsGenerationInputs, start)

    val failures = assertionResults.filter(!_.result)
    if (failures.nonEmpty) {
      val message = s"${failures.size} Gatling assertion(s) failed: (\n\t${failures.map(_.message).mkString("\n\t")}\n)"
      val stackDepth = 1
      throw new TestFailedException(message, stackDepth)
        .modifyPayload(_ => Some(failures))
    }
  }

  private def reportsGenerationEnabled =
    configuration.data.fileDataWriterEnabled && !configuration.charting.noReports

  private def generateReports(reportsGenerationInputs: ReportsGenerationInputs, start: Long): Unit = {
    logger.info("Generating reports...")
    val indexFile = ReportsGenerator.generateFor(reportsGenerationInputs)
    logger.info(s"Reports generated in ${(currentTimeMillis - start) / 1000}s.")
    logger.info(s"Please open the following file: ${indexFile.toAbsolutePath.toFile}")
  }

  private def simulationName(test: OneArgTest): String =
    test.name.replaceAll(" ", "_").toLowerCase

}
