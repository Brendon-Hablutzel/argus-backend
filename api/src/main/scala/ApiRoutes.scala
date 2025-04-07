package api

import cats.effect.Concurrent
import cats.syntax.all.*
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import cats.effect.IO
import org.http4s.*
import org.http4s.implicits.*
import org.http4s.circe.*
import org.http4s.Method.*
import org.http4s.circe.CirceEntityCodec.circeEntityDecoder
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder

object ApiRoutes:

  def metricsRoutes(M: Metrics): HttpRoutes[IO] =
    val dsl = new Http4sDsl[IO] {}
    import dsl.*

    HttpRoutes.of[IO] { case req @ GET -> Root / "metrics" / LongVar(since) =>
      Ok(M.get(since))
    }
