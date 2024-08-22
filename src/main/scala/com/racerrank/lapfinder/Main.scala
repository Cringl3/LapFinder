package com.racerrank.lapfinder

import com.racerrank.lapfinder.Functions.{TelemetryPoint, TelemetrySegment, findLaps, readPointsFromSource}

import scala.io.Source

object Main extends App {
  //read in telemetry points from csv file
  val points = readPointsFromSource(Source.fromResource("Reference_Lap.csv"))

  //define the start line
  val startLine = TelemetrySegment(TelemetryPoint(-97.65781, 31.04673), TelemetryPoint(-97.65682, 31.04648))
  val sectorLine = TelemetrySegment(TelemetryPoint(-97.65468, 31.05224), TelemetryPoint(-97.65604, 31.05259))

  //find laps
  val laps = findLaps(points, List(startLine, sectorLine))
  for { l <- laps } println(l)
}