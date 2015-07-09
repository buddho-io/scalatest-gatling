package org.scalatest.fixture

import org.scalatest.{Suite, Finders}


@Finders(Array("org.scalatest.finders.SimulationWithConfigFunSuiteFinder"))
abstract class SimulationWithConfigFunSuite extends SimulationWithConfigFunSuiteLike {

  /**
   * Returns a user friendly string for this suite, composed of the
   * simple name of the class (possibly simplified further by removing dollar signs if added by the Scala interpeter)
   * and, if this suite contains nested suites, the result of invoking <code>toString</code> on each of the
   * nested suites, separated by commas and surrounded by parentheses.
   *
   * @return a user-friendly string for this suite
   */
  override def toString: String = Suite.suiteToString(None, this)
}
