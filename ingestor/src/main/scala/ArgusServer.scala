package ingestor

import cats.effect.Async
import cats.syntax.all.*
import com.comcast.ip4s.*
import fs2.io.net.Network
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits.*
import org.http4s.server.middleware.Logger

object ArgusServer:

  def run[F[_]: Async: Network]: F[Nothing] = {
    val ingestionAlg = Ingest.impl[F]

    val httpApp = (
      ArgusRoutes.ingestionRoutes[F](ingestionAlg)
    ).orNotFound

    val finalHttpApp = Logger.httpApp(true, true)(httpApp)

    EmberServerBuilder
      .default[F]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"8080")
      .withHttpApp(finalHttpApp)
      .build
  }.useForever
