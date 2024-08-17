package com.racerrank.lapfinder

import java.awt.geom.Line2D
import java.sql.Timestamp
import java.time.{Duration, Instant, OffsetDateTime}
import scala.io.Source

object Functions {
  case class TelemetryPoint(x: Double, y: Double, t: Timestamp = Timestamp.from(Instant.EPOCH))
  case class TelemetrySegment(p1: TelemetryPoint, p2: TelemetryPoint)
  case class Lap(startTime: Timestamp, lapTime: Long)

  /**
   * Read a number of csv rows into a list of TelemetryPoints
   * @param source source wrapping a csv list of telemetry strings
   * @return list of telemetry points read from source
   */
  def readPointsFromSource(source: Source): List[TelemetryPoint] = {
    val telemetryPoints = try source.mkString.split("\n") finally source.close()
    telemetryPoints.tail.map(s => {
      val tokens = s.split(",")
      TelemetryPoint(tokens(0).toDouble, tokens(1).toDouble, Timestamp.from(OffsetDateTime.parse(tokens(2)).toInstant))
    }).toList
  }

  /**
   * Helper method to check if two doubles are close to each other
   * @param x a double to compare
   * @param y a double to compare
   * @param precision the precision to compare the two doubles
   * @return true if abs(x - y) < precision
   */
  def ~=(x: Double, y: Double, precision: Double) = {
    if ((x - y).abs < precision) true else false
  }

  /**
   * Generate a list of laps from a list of telemetry points and a starting line
   * @param points telemetry points
   * @param startLine the start line
   * @return a list of laps
   */
  def findLaps(points: List[TelemetryPoint], startLine: TelemetrySegment): List[Lap] = {
    val lineSegments = (0 until points.length - 1).map(i =>
      TelemetrySegment(points(i), points(i+1))
    )

    val crossings = lineSegments.flatMap(lineSegment => findStartLineIntersect(lineSegment, startLine))

    (0 until crossings.length - 1).map(i =>
      Lap(crossings(i).t, (crossings(i + 1).t.toInstant.toEpochMilli - (crossings(i).t.toInstant.toEpochMilli))/1000)
    ).toList
  }

  /**
   * Determine if and where a given telemtry segment crossed the finish line
   * @param segment telemetry segment to test
   * @param startLine the start line to check
   * @return a telemetry point for the place and time the segment crossed the start line or none if it did not cross
   */
  def findStartLineIntersect(segment: TelemetrySegment, startLine: TelemetrySegment): Option[TelemetryPoint] = {
    def linesIntersect(line1: TelemetrySegment, line2: TelemetrySegment): Boolean = {
      val l1 = new Line2D.Double(line1.p1.x, line1.p1.y, line1.p2.x, line1.p2.y)
      val l2 = new Line2D.Double(line2.p1.x, line2.p1.y, line2.p2.x, line2.p2.y)

      l1.intersectsLine(l2)
    }

    def haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double) = {
      // distance between latitudes and longitudes
      val dLat = Math.toRadians(lat2 - lat1)
      val dLon = Math.toRadians(lon2 - lon1)
      // convert to radians
      val lat1Rad = Math.toRadians(lat1)
      val lat2Rad = Math.toRadians(lat2)
      // apply formulae
      val a = Math.pow(Math.sin(dLat / 2), 2) + Math.pow(Math.sin(dLon / 2), 2) * Math.cos(lat1Rad) * Math.cos(lat2Rad)
      val rad = 6371
      val c = 2 * Math.asin(Math.sqrt(a))
      rad * c
    }


    if (!linesIntersect(segment, startLine))
      None
    else {
      val slope1 = (segment.p2.y - segment.p1.y) / (segment.p2.x - segment.p1.x)
      val slope2 = (startLine.p2.y - startLine.p1.y) / (startLine.p2.x - startLine.p1.x)

      val gatedSlope1 = if (slope1.isInfinity) Double.MaxValue else slope1
      val gatedSlope2 = if (slope2.isInfinity) Double.MaxValue else slope2

      //y = mx + b
      //y - mx = b
      // b = y - mx
      val b1 = segment.p1.y - gatedSlope1 * segment.p1.x
      val b2 = startLine.p1.y - gatedSlope2 * startLine.p1.x

      if (~=(gatedSlope1, gatedSlope2, 0.0001))
        None
      else {
        val xIntercept = (b2 - b1) / (gatedSlope1 - gatedSlope2)
        val yIntercept = gatedSlope1 * ((b2 - b1) / (gatedSlope1 - gatedSlope2)) + b1
        val totalDist = haversine(segment.p1.x, segment.p1.y, segment.p2.x, segment.p2.y)
        val distToIntersect = haversine(segment.p1.x, segment.p1.y, xIntercept, yIntercept)
        val multiplier = distToIntersect / totalDist
        val millisBetweenLastPointAndIntersect =
          Duration.between(segment.p1.t.toInstant, segment.p2.t.toInstant).toMillis * multiplier

        Some(TelemetryPoint(
          xIntercept,
          yIntercept,
          Timestamp.from(segment.p1.t.toInstant.plusMillis(millisBetweenLastPointAndIntersect.toLong)))
        )
      }
    }
  }
}
