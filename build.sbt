
lazy val commonSettings = Seq(
  organization := "io.buddho.scalatest",
  version := "0.1.0",
  scalaVersion := "2.11.6",
  resolvers ++= Seq(
    "Maven Central Server"    at "http://repo1.maven.org/maven2",
    "Sonatype OSS"            at "https://oss.sonatype.org/content/groups/public/",
    "Sonatype OSS Snapshots"  at "https://oss.sonatype.org/content/repositories/snapshots/"
  )
)

val gatlingVersion = "2.1.6"

lazy val root = (project in file(".")).
  aggregate(core, examples).
  settings(
    name := "scalatest-gatling"
  )

lazy val core = (project in file("core")).
  settings(commonSettings: _*).
  settings(
    name := "scalatest-gatling-core",
    libraryDependencies ++= {
      Seq(
        "io.gatling" % "gatling-core" % gatlingVersion,
        "io.gatling" % "gatling-http" % gatlingVersion,
        "io.gatling" % "gatling-charts" % gatlingVersion,
        "org.scalatest" %% "scalatest" % "2.2.4"
      )
    }
  )

lazy val examples = (project in file("examples")).
  dependsOn(core).
  settings(commonSettings: _*).
  settings(
    name := "scalatest-gatling-examples",
    libraryDependencies ++= {
      Seq(
        "io.gatling"            %  "gatling-core"               % gatlingVersion,
        "io.gatling"            %  "gatling-http"               % gatlingVersion,
        "io.gatling.highcharts" %  "gatling-charts-highcharts"  % gatlingVersion,
        "org.scalatest"         %% "scalatest"                  % "2.2.4" % "test",
        "org.pegdown"           %  "pegdown"                    % "1.0.2" % "test" // required for ScalaTest HTML reports
      )
    }
  )