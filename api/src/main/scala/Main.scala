package api

import cats.effect.{IO, IOApp}

object Main extends IOApp.Simple:
  val run = ApiServer.run
