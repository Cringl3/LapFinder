package com.racerrank.lapfinder

import com.racerrank.lapfinder.Functions.{TelemetryPoint, TelemetrySegment, findLaps, readPointsFromSource}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

import java.time.OffsetDateTime
import scala.io.Source

class LapDetectionFunctionSpec extends AnyFlatSpec with should.Matchers{

  "A lap detection function" should "detect when the test vehicle crossed the finish line" in {
    val structuredTelemetryPoints = readPointsFromSource(Source.fromResource("Reference_Lap.csv"))
    val startLine = TelemetrySegment(TelemetryPoint(-97.65781, 31.04673), TelemetryPoint(-97.65682, 31.04648))

    val laps = findLaps(structuredTelemetryPoints, startLine)

    laps.size should be (3)

    laps(0).startTime.toInstant should be (OffsetDateTime.parse("2024-04-05T05:21:59.759Z").toInstant)
    laps(0).lapTime should be (243L)

    laps(1).startTime.toInstant should be (OffsetDateTime.parse("2024-04-05T05:26:03.276Z").toInstant)
    laps(1).lapTime should be (246L)

    laps(2).startTime.toInstant should be (OffsetDateTime.parse("2024-04-05T05:30:09.666Z").toInstant)
    laps(2).lapTime should be (242L)
  }
}
