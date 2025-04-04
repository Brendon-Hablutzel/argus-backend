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
  .enablePlugins(RevolverPlugin, DockerPlugin)
  .settings(
    name                             := "ingestor",
    libraryDependencies ++= Seq(
      "org.scalameta" %% "munit"               % "1.0.0" % Test,
      "org.http4s"    %% "http4s-ember-client" % http4sVersion,
      "org.http4s"    %% "http4s-ember-server" % http4sVersion,
      "org.http4s"    %% "http4s-circe"        % http4sVersion,
      "org.http4s"    %% "http4s-dsl"          % http4sVersion
    ),
    assembly / assemblyMergeStrategy := {
      case PathList("META-INF", _ @_*) => MergeStrategy.discard
      case _                           => MergeStrategy.first
    },
    docker / dockerfile              := {
      val artifact: File     = assembly.value
      val artifactTargetPath = s"/app/${artifact.name}"

      new Dockerfile {
        from("eclipse-temurin:17-jdk-jammy")
        add(artifact, artifactTargetPath)
        entryPoint("java", "-jar", artifactTargetPath)
      }
    },
    docker / imageNames              := Seq(
      ImageName(s"argus-ingestor:latest")
    ),
    Compile / packageBin             := assembly.value
  )

lazy val root = project
  .in(file("."))
  .aggregate(common, ingestor)
  .settings(
    name := "argus-backend"
  )
