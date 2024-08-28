package com.racerrank.lapfinder

import com.racerrank.lapfinder.Functions.{Point, LineSegment, findLaps, readPointsFromSource}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

import java.time.OffsetDateTime
import scala.io.Source

class LapDetectionFunctionSpec extends AnyFlatSpec with should.Matchers{

  "A lap detection function" should "detect when the test vehicle crossed the finish line" in {
    val structuredTelemetryPoints = readPointsFromSource(Source.fromResource("Reference_Lap.csv"))
    val startLine = LineSegment(Point(-97.65781, 31.04673), Point(-97.65682, 31.04648))
    val sectorLine = LineSegment(Point(-97.65468, 31.05224), Point(-97.65604, 31.05259))

    val laps = findLaps(structuredTelemetryPoints, List(startLine, sectorLine))

    laps.size should be (4)

    laps(0).startTime.toInstant should be (OffsetDateTime.parse("2024-04-05T05:21:59.759Z").toInstant)
    laps(0).sectors(0).sectorTime should be (32L)
    laps(0).sectors(1).sectorTime should be (210L)

    laps(1).startTime.toInstant should be (OffsetDateTime.parse("2024-04-05T05:26:03.276Z").toInstant)
    laps(1).sectors(0).sectorTime should be (33L)
    laps(1).sectors(1).sectorTime should be (213L)

    laps(2).startTime.toInstant should be (OffsetDateTime.parse("2024-04-05T05:30:09.666Z").toInstant)
    laps(2).sectors(0).sectorTime should be (33L)
    laps(2).sectors(1).sectorTime should be (209L)

    laps(3).startTime.toInstant should be (OffsetDateTime.parse("2024-04-05T05:34:12.49Z").toInstant)
    laps(3).sectors(0).sectorTime should be (35L)
  }


  "A lap detection function" should "not crash when given partial input" in {
    val structuredTelemetryPoints = readPointsFromSource(Source.fromResource("Reference_Lap.csv"))
    val startLine = LineSegment(Point(-97.65781, 31.04673), Point(-97.65682, 31.04648))
    val sectorLine = LineSegment(Point(-97.65468, 31.05224), Point(-97.65604, 31.05259))

    val laps = findLaps(structuredTelemetryPoints.take(1), List(startLine, sectorLine))

    laps.size should be (0)
  }
}
