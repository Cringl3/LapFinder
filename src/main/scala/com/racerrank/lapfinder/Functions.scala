package com.racerrank.lapfinder

import java.awt.geom.Line2D
import java.sql.Timestamp
import java.time.{Duration, Instant, OffsetDateTime}
import scala.io.Source

object Functions {
  case class Point(x: Double, y: Double, t: Timestamp = Timestamp.from(Instant.EPOCH))
  case class LineSegment(p1: Point, p2: Point)
  case class Lap(startTime:Timestamp, sectorTimes: List[Long])

  /**
   * Read a number of csv rows into a list of TelemetryPoints
   * @param source source wrapping a csv list of telemetry strings
   * @return list of telemetry points read from source
   */
  def readPointsFromSource(source: Source): List[Point] = {
    val telemetryPoints = try source.mkString.split("\n") finally source.close()
    telemetryPoints.tail.map(s => {
      val tokens = s.split(",")
      Point(tokens(0).toDouble, tokens(1).toDouble, Timestamp.from(OffsetDateTime.parse(tokens(2)).toInstant))
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
   * @param sectorLines list of sector lines to use when calculating lap time
   * @return a list of laps present in points
   */
  def findLaps(points: List[Point], sectorLines: List[LineSegment]): List[Lap] = {
    if(points.size == 1)
      List[Lap]()
    else {
      def calculateSectorTimes(sectorCrossingPoints: List[Point]): List[Long] = {
        sectorCrossingPoints.sliding(2, 1).map(i => (i(1).t.toInstant.toEpochMilli - i(0).t.toInstant.toEpochMilli) / 1000).toList
      }

      val sectorCrossingPoints = points.sliding(2, 1).map(i => LineSegment(i(0), i(1)))
        .flatMap(lineSegment => findSectorLineIntersect(lineSegment, sectorLines))

      val laps = for {sectorGrouping <- sectorCrossingPoints.sliding(sectorLines.size + 1, sectorLines.size)
                      if sectorGrouping.size > 1}
      yield Lap(sectorGrouping.head.t, calculateSectorTimes(sectorGrouping.toList))

      laps.toList
    }
  }

  /**
   * Determine if and where a given telemetry segment crossed one of the given sector lines
   * @param segment telemetry segment to test
   * @param sectorLines the sector lines to check
   * @return a telemetry point for the place and time the segment crossed one of the sector lines or none if it did not
   */
  def findSectorLineIntersect(segment: LineSegment, sectorLines: List[LineSegment]): Option[Point] = {
    for (line <- sectorLines;
         intersect = findSectorLineIntersect(segment, line)) {
      if (intersect.isDefined) return intersect
    }

    None
  }

  /**
   * Determine if and where a given telemetry segment crossed the given sector line
   * @param segment telemetry segment to test
   * @param sectorLine the sector line to check
   * @return a telemetry point for the place and time the segment crossed the given sector line or none if it did not
   */
  def findSectorLineIntersect(segment: LineSegment, sectorLine: LineSegment): Option[Point] = {
    def linesIntersect(line1: LineSegment, line2: LineSegment): Boolean = {
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


    if (!linesIntersect(segment, sectorLine))
      None
    else {
      val slope1 = (segment.p2.y - segment.p1.y) / (segment.p2.x - segment.p1.x)
      val slope2 = (sectorLine.p2.y - sectorLine.p1.y) / (sectorLine.p2.x - sectorLine.p1.x)

      val gatedSlope1 = if (slope1.isInfinity) Double.MaxValue else slope1
      val gatedSlope2 = if (slope2.isInfinity) Double.MaxValue else slope2

      //y = mx + b
      //y - mx = b
      // b = y - mx
      val b1 = segment.p1.y - gatedSlope1 * segment.p1.x
      val b2 = sectorLine.p1.y - gatedSlope2 * sectorLine.p1.x

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

        Some(Point(
          xIntercept,
          yIntercept,
          Timestamp.from(segment.p1.t.toInstant.plusMillis(millisBetweenLastPointAndIntersect.toLong)))
        )
      }
    }
  }
}
