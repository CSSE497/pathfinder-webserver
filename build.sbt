name := """pathfinder-webserver"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  javaJdbc,
  cache,
  javaWs,
  "org.postgresql" % "postgresql" % "9.4-1203-jdbc42"
)

routesGenerator := InjectedRoutesGenerator

// Docker Configuration
maintainer := "Pathfinder Team"
dockerExposedPorts in Docker := Seq(9000, 9443)
