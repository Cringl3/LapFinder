package com.racerrank.lapfinder

import com.racerrank.lapfinder.Functions.{Point, LineSegment, findSectorLineIntersect, ~=}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

class IntersectionFunctionSpec extends AnyFlatSpec with should.Matchers{

  "An intersection function" should "detect when two lines intersect" in {
    val line1 = LineSegment(Point(-1,0), Point(1,0))
    val line2 = LineSegment(Point(0,1), Point(0,-1))

    val intersectPoint = findSectorLineIntersect(line1, line2)

    intersectPoint.nonEmpty should be (true)
    ~=(intersectPoint.get.x, 0, 0.001) should be (true)
    ~=(intersectPoint.get.y, 0, 0.001) should be (true)
  }

  "An intersection function" should "not detect when two lines intersect outside of the current segment" in {
    val line1 = LineSegment(Point(-1,0), Point(1,0))
    val line2 = LineSegment(Point(0,5), Point(0,2))

    val intersectPoint = findSectorLineIntersect(line1, line2)

    intersectPoint.isEmpty should be (true)
  }
}
