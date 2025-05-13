package ingestor

import cats.effect.Async
import com.comcast.ip4s.IpLiteralSyntax
import fs2.io.net.Network
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.middleware.Logger

object ArgusServer {

  def run[F[_]: Async: Network]: F[Nothing] = {

    val composedResource = for {
      producer <- KafkaClient.producerResource
      server   <- {
        val ingestionAlg = Ingest.impl[F](producer)
        val httpApp      = (ArgusRoutes.ingestionRoutes[F](ingestionAlg)).orNotFound
        val finalHttpApp = Logger.httpApp(true, true)(httpApp)

        EmberServerBuilder
          .default[F]
          .withHost(ipv4"0.0.0.0")
          .withPort(port"8080")
          .withHttpApp(finalHttpApp)
          .build
      }
    } yield server

    composedResource.useForever
  }
}
