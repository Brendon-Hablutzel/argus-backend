name                     := "argus-backend"
ThisBuild / scalaVersion := "3.6.4"

lazy val common = project
  .in(file("common"))
  .settings(
    name := "common"
  )

val http4sVersion = "0.23.30"

lazy val ingestor = project
  .in(file("ingestor"))
  .dependsOn(common)
  .enablePlugins(RevolverPlugin)
  .settings(
    name := "ingestor",
    libraryDependencies ++= Seq(
      "org.scalameta" %% "munit"               % "1.0.0" % Test,
      "org.http4s"    %% "http4s-ember-client" % http4sVersion,
      "org.http4s"    %% "http4s-ember-server" % http4sVersion,
      "org.http4s"    %% "http4s-circe"        % http4sVersion,
      "org.http4s"    %% "http4s-dsl"          % http4sVersion
    )
  )

lazy val root = project
  .in(file("."))
  .aggregate(common, ingestor)
  .settings(
    name := "argus-backend"
  )
