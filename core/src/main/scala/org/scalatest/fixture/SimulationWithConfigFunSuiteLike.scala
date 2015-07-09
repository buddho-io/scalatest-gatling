package org.scalatest.fixture

import java.io.File

import com.typesafe.config.{ConfigFactory, Config}
import org.scalatest.ConfigMap


trait SimulationWithConfigFunSuiteLike extends SimulationFunSuiteLike {

  protected val defaultConfig: Config
  protected var extConfig: Config = _

  /**
   * Load simulation configuration
   *
   * @param configMap
   */
  override protected def beforeAll(configMap: ConfigMap): Unit = {
    extConfig = configMap.getOptional[String]("simulation.config")
      .map(s => ConfigFactory.parseFile(new File(s)).withFallback(defaultConfig))
      .getOrElse(defaultConfig)

    super.beforeAll(configMap)
  }

}
