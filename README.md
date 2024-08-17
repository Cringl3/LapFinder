# Lap Finder

This is a simple functional library for detecting lap times given a collection of telemetry points.


## Usage/Examples

Steps
1. Create a list of TelemetryPoint objects
2. Define a starting line
3. Make a call to the _findLaps(points, startingLine)_ function


*Note*: There is a convenience helper function that can create a list of TelemetryPoint objects from a csv Source 
with the following schema: 

| Column index | Column name | Type   | Example Value        
|--------------|-------------|--------|----------------------
 1             | Lon         | Double | -97.65785718662204   
 2             | Lat         | Double | 31.04494996157486    
 3             | Time        | String | 2024-04-05T05:21:49Z 
                 


```scala
//read in telemetry points from csv file
val pathToMyCSV = ???
val points = readPointsFromSource(Source.fromFile(pathToMyCSV))

//define the start line
val startingLineLat1 = ???
val startingLineLon1 = ???
val startingLineLat2 = ???
val startingLineLon2 = ???

val startLine = TelemetrySegment(
  TelemetryPoint(startingLineLon1, startingLineLat1), 
  TelemetryPoint(startingLineLon2, startingLineLat2)
)

//find laps and print them to the screen
for { l <- findLaps(points, startLine) } println(l)
```

## Running Tests

To run tests, run the following command

```bash
  sbt test
```
