package ingestor;

import cats.effect.{IO, IOApp}

object Main extends IOApp.Simple:
  val run = ArgusServer.run[IO]
