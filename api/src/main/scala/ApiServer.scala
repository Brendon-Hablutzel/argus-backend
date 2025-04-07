package api

import cats.effect.Async
import cats.syntax.all.*
import com.comcast.ip4s.*
import fs2.io.net.Network
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits.*
import org.http4s.server.middleware.Logger
import org.slf4j.LoggerFactory
import cats.effect.IO

object ApiServer:
  private val logger = LoggerFactory.getLogger(getClass)

  def run: IO[Nothing] =
    val composedResource = for {
      transactor <- DbClient.sessionResource
      server     <- {
        val metricsAlg   = Metrics.impl(transactor)
        val httpApp      = (ApiRoutes.metricsRoutes(metricsAlg)).orNotFound
        val finalHttpApp = Logger.httpApp(true, true)(httpApp)

        EmberServerBuilder
          .default[IO]
          .withHost(ipv4"0.0.0.0")
          .withPort(port"8081")
          .withHttpApp(finalHttpApp)
          .build
      }
    } yield server

    composedResource.useForever
