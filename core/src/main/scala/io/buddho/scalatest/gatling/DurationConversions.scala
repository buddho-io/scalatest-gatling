package io.buddho.scalatest.gatling

import scala.concurrent.duration.Duration


object DurationConversions {
  implicit def durationToMillis(duration: Duration): Int = duration.toMillis.toInt
}
