name := """pathfinder-webserver"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava, PlayEbean)

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  javaJdbc,
  cache,
  javaWs,
  filters,
  "org.postgresql" % "postgresql" % "9.4-1203-jdbc42",
  "org.avaje.ebeanorm" % "avaje-ebeanorm-api" % "3.1.1",
  "javax.el" % "javax.el-api" % "3.0.0",
  "com.sun.el" % "el-ri" % "1.0",
  "com.novocode" % "junit-interface" % "0.11" % "test"
)

testOptions += Tests.Argument(TestFrameworks.JUnit, "-v", "-q", "-a")
routesGenerator := InjectedRoutesGenerator

// Docker Configuration
packageName in Docker := "pathfinder-webserver"
version in Docker := "0.1.3"
maintainer := "Pathfinder Team"
dockerRepository := Some("beta.gcr.io/phonic-aquifer-105721")
dockerExposedPorts in Docker := Seq(9000, 9443)

// Determines which scala files not to measure with Coveralls.
scoverage.ScoverageSbtPlugin.ScoverageKeys.coverageExcludedPackages := "<empty>;controllers\\..*Reverse.*;router\\..*Routes.*"
