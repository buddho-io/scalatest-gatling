package io.buddho.scalatest.gatling

import scala.concurrent.duration.Duration
import scala.language.implicitConversions


object DurationConversions {
  implicit def durationToMillis(duration: Duration): Int = duration.toMillis.toInt
}
