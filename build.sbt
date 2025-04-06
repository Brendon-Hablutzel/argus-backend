name                     := "argus-backend"
ThisBuild / scalaVersion := "3.6.4"

val circeVersion = "0.14.12"

lazy val common = project
  .in(file("common"))
  .settings(
    name := "common",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-effect"   % "3.6.0",
      "io.circe"      %% "circe-core"    % circeVersion,
      "io.circe"      %% "circe-generic" % circeVersion,
      "io.circe"      %% "circe-parser"  % circeVersion
    )
  )

val http4sVersion = "0.23.30"

lazy val ingestor = project
  .in(file("ingestor"))
  .dependsOn(common)
  .enablePlugins(RevolverPlugin, DockerPlugin)
  .settings(
    name                             := "ingestor",
    resolvers += "Confluent" at "https://packages.confluent.io/maven/",
    libraryDependencies ++= Seq(
      "org.scalameta"   %% "munit"               % "1.0.0" % Test,
      "org.http4s"      %% "http4s-ember-client" % http4sVersion,
      "org.http4s"      %% "http4s-ember-server" % http4sVersion,
      "org.http4s"      %% "http4s-circe"        % http4sVersion,
      "org.http4s"      %% "http4s-dsl"          % http4sVersion,
      "org.apache.kafka" % "kafka-clients"       % "7.9.0-ce",
      "ch.qos.logback"   % "logback-classic"     % "1.5.18",
      "org.slf4j"        % "slf4j-api"           % "2.0.17"
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

lazy val processor = project
  .in(file("processor"))
  .dependsOn(common)
  .enablePlugins(RevolverPlugin)
  .settings(
    name := "processor",
    resolvers += "Confluent" at "https://packages.confluent.io/maven/",
    libraryDependencies ++= Seq(
      // "org.scalameta"   %% "munit"               % "1.0.0" % Test,
      // "org.http4s"      %% "http4s-ember-client" % http4sVersion,
      // "org.http4s"      %% "http4s-ember-server" % http4sVersion,
      // "org.http4s"      %% "http4s-circe"        % http4sVersion,
      // "org.http4s"      %% "http4s-dsl"          % http4sVersion,
      "org.apache.kafka" % "kafka-clients"   % "7.9.0-ce",
      "org.typelevel"   %% "cats-effect"     % "3.6.0",
      "io.circe"        %% "circe-core"      % circeVersion,
      "io.circe"        %% "circe-generic"   % circeVersion,
      "io.circe"        %% "circe-parser"    % circeVersion,
      "ch.qos.logback"   % "logback-classic" % "1.5.18",
      "org.slf4j"        % "slf4j-api"       % "2.0.17"
    )
  )

lazy val root = project
  .in(file("."))
  .aggregate(common, ingestor, processor)
  .settings(
    name := "argus-backend"
  )
