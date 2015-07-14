package org.scalatest.fixture

import io.buddho.scalatest.gatling.SimulationFixture
import org.scalatest.OutcomeOf._
import org.scalatest.Suite.autoTagClassAnnotations
import org.scalatest._

import scala.collection.immutable.ListSet


@Finders(Array("org.scalatest.finders.SimulationFunSuiteFinder"))
trait SimulationFunSuiteLike extends fixture.Suite with Informing with Alerting with Documenting with SimulationFixture { thisSuite =>

  protected val engine = new FixtureEngine[FixtureParam]("concurrentFixtureSpecMod", "SimulationFunSuiteLike")
  import engine._

  private[scalatest] val sourceFileName = "SimulationFunSuiteLike.scala"

  protected def info: Informer = atomicInformer.get

  protected def note: Notifier = atomicNotifier.get

  protected def alert: Alerter = atomicAlerter.get

  protected def markup: Documenter = atomicDocumenter.get

  protected final class SimulateWord(name: String) {

    def in(simulationFun: FixtureParam => Any): Unit = {
      registerTestToRun(name, List(), "simulate", TestTransformer(simulationFun))
    }

    def ignore(simulationFun: FixtureParam => Any): Unit = {
      registerTestToIgnore(name, Nil, "ignore", TestTransformer(simulationFun))
    }

    def taggedAs(firstTestTag: Tag, otherTestTags: Tag*): SimulateWordTaggedAs[Nothing] = {
      val tagList = firstTestTag :: otherTestTags.toList
      new SimulateWordTaggedAs(name, tagList)
    }

    def withTiers[Tier](tiers: List[Tier]): SimulateWordWithTiers[Tier] = withTiers(tiers.head, tiers.tail: _*)

    def withTiers[Tier](firstTier: Tier, otherTiers: Tier*): SimulateWordWithTiers[Tier] = {
      val tierList = firstTier :: otherTiers.toList
      new SimulateWordWithTiers[Tier](name, tierList)
    }
  }

  protected final class SimulateWordWithTiers[Tier](name: String, tierList: List[Tier]) {

    def in(simulationFun: (FixtureParam, Tier) => Any): Unit = {
      tierList.foldLeft(0)((c, tier) => {
        registerTestToRun(s"$name tier $c", List(), "simulate", TestWithTierTransformer(simulationFun, tier))
        c + 1
      })
    }

    def ignore(simulationFun: (FixtureParam, Tier) => Any): Unit = {
      tierList.foldLeft(0)((c, tier) => {
        registerTestToIgnore(s"$name tier $c", Nil, "ignore", TestWithTierTransformer(simulationFun, tier))
        c + 1
      })
    }

    def taggedAs(firstTestTag: Tag, otherTestTags: Tag*): SimulateWordWithTiersTaggedAs[Tier] = {
      val tagList = firstTestTag :: otherTestTags.toList
      new SimulateWordWithTiersTaggedAs(name, tagList, tierList)
    }
  }

  protected final class SimulateWordTaggedAs[Tier](name: String, tagList: List[Tag]) {
    def in(simulationFun: FixtureParam => Any): Unit =
      registerTestToRun(name, tagList, "simulate", TestTransformer(simulationFun))

    def ignore(simulationFun: FixtureParam => Any): Unit = registerTestToIgnore(name, tagList, "ignore", TestTransformer(simulationFun))

    def withTiers(tiers: List[Tier]): SimulateWordWithTiersTaggedAs[Tier] = withTiers(tiers.head, tiers.tail: _*)

    def withTiers(firstTier: Tier, otherTiers: Tier*): SimulateWordWithTiersTaggedAs[Tier] = {
      val tierList = firstTier :: otherTiers.toList
      new SimulateWordWithTiersTaggedAs[Tier](name, tagList, tierList)
    }
  }

  protected final class SimulateWordWithTiersTaggedAs[Tier](name: String, tagList: List[Tag], tierList: List[Tier]) {
    def in(simulationFun: (FixtureParam, Tier) => Any): Unit = {
      tierList.foldLeft(0)((c, tier) => {
        registerTestToRun(s"$name tier $c", tagList, "simulate", TestWithTierTransformer(simulationFun, tier))
        c + 1
      })
    }

    def ignore(simulationFun: (FixtureParam, Tier) => Any): Unit = {
      tierList.foldLeft(0)((c, tier) => {
        registerTestToIgnore(s"$name tier $c", tagList, "ignore", TestWithTierTransformer(simulationFun, tier))
        c + 1
      })
    }
  }

  def simulate(name: String): SimulateWord = new SimulateWord(name)

  /**
   * Transforms a test function to proper test signature.
   *
   * @param testFun
   * @param tier
   */
  private case class TestWithTierTransformer[Tier](testFun: (FixtureParam, Tier) => Any, tier: Tier) extends (FixtureParam => Outcome) {
    def apply(fixture: FixtureParam): Outcome = outcomeOf( testFun(fixture, tier) )
  }

  /**
   * Transforms a test function to proper test signature.
   *
   * @param testFun
   */
  private case class TestTransformer[Tier](testFun: FixtureParam => Any) extends (FixtureParam => Outcome) {
    def apply(fixture: FixtureParam): Outcome = outcomeOf( testFun(fixture) )
  }

  private def registerTestToIgnore(specText: String, testTags: List[Tag], methodName: String, testFun: FixtureParam => Outcome): Unit = {
    registerIgnoredTest(specText, testFun, "ignoreCannotAppearInsideAnIn", sourceFileName, methodName, 6, -2, None, testTags: _*)
  }

  private def registerTestToRun(specText: String, testTags: List[Tag], methodName: String, testFun: FixtureParam => Outcome) {
    registerTest(specText, testFun, "simulateCannotAppearInsideAnotherSimulate", sourceFileName, methodName, 4, -3, None, None, None, testTags: _*)
  }

  override def testNames: Set[String] = {
    // Returning a ListSet so test will run in registration order
    ListSet(atomic.get.testNamesList.toArray: _*)
  }

  protected override def runTest(testName: String, args: Args): Status = {

    def invokeWithFixture(theTest: TestLeaf): Outcome =
      withFixture(new TestFunAndConfigMap(testName, theTest.testFun, args.configMap))

    runTestImpl(thisSuite, testName, args, true, invokeWithFixture)
  }

  override def tags: Map[String, Set[String]] = autoTagClassAnnotations(atomic.get.tagsMap, this)

  protected override def runTests(testName: Option[String], args: Args): Status = {
    runTestsImpl(thisSuite, testName, args, info, true, runTest)
  }

  override def run(testName: Option[String], args: Args): Status = {
    runImpl(thisSuite, testName, args: Args, super.run)
  }

  import scala.language.implicitConversions

  override def testDataFor(testName: String, theConfigMap: ConfigMap = ConfigMap.empty): TestData =
    createTestDataFor(testName, theConfigMap, this)

  final override val styleName: String = "org.scalatest.fixture.SimulationFunSuite"
}
