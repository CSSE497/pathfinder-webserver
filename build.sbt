name := """pathfinder-webserver"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava, PlayEbean)

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  javaJdbc,
  cache,
  javaWs,
  filters,
  "com.fasterxml.jackson.dataformat" % "jackson-dataformat-yaml" % "2.7.0",
  "javax.websocket" % "javax.websocket-client-api" % "1.1",
  "org.glassfish.tyrus" % "tyrus-client" % "1.12",
  "org.glassfish.tyrus" % "tyrus-container-grizzly-client" % "1.12",
  "org.postgresql" % "postgresql" % "9.4-1203-jdbc42",
  "org.avaje.ebeanorm" % "avaje-ebeanorm-api" % "3.1.1",
  "org.avaje.ebeanorm" % "avaje-ebeanorm" % "4.6.3",
  "javax.el" % "javax.el-api" % "3.0.0",
  "com.sun.el" % "el-ri" % "1.0",
  "com.novocode" % "junit-interface" % "0.11" % "test",
  "org.bouncycastle" % "bcprov-jdk15on" % "1.54"
)

javaOptions ++= Seq(
    "-Dhttp.port=80",
    "-Dhttps.port=443",
    "-Dhttps.keyStore=conf/yes.jks",
    "-Dhttps.keyStorePassword=password"
)

testOptions += Tests.Argument(TestFrameworks.JUnit, "-v", "-q", "-a")
routesGenerator := InjectedRoutesGenerator

// Docker Configuration
packageName in Docker := "pathfinder-webserver"
version in Docker := "0.1.16"
maintainer := "Pathfinder Team"
dockerRepository := Some("beta.gcr.io/phonic-aquifer-105721")
dockerExposedPorts in Docker := Seq(9000, 9443)

// Determines which scala files not to measure with Coveralls.
scoverage.ScoverageSbtPlugin.ScoverageKeys.coverageExcludedPackages := "<empty>;controllers\\..*Reverse.*;router\\..*Routes.*"
