package api

import cats.effect.IO
import com.comcast.ip4s._
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.middleware.Logger

object ApiServer {

  def run: IO[Nothing] = {

    val composedResource = for {
      transactor <- DbClient.sessionResource
      server     <- {
        val metricsAlg   = Metrics.impl(transactor)
        val httpApp      = ApiRoutes.metricsRoutes(metricsAlg).orNotFound
        val finalHttpApp = Logger.httpApp(logHeaders = true, logBody = true)(httpApp)

        EmberServerBuilder
          .default[IO]
          .withHost(ipv4"0.0.0.0")
          .withPort(port"8081")
          .withHttpApp(finalHttpApp)
          .build
      }
    } yield server

    composedResource.useForever
  }
}
