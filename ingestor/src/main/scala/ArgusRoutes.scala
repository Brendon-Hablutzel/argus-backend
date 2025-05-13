package ingestor

import cats.effect.Concurrent
import cats.syntax.all._
import org.http4s._
import org.http4s.circe.CirceEntityCodec.{circeEntityDecoder, circeEntityEncoder}
import org.http4s.dsl._

object ArgusRoutes {
  def ingestionRoutes[F[_]: Concurrent](I: Ingest[F]): HttpRoutes[F] = {

    val dsl = new Http4sDsl[F] {}
    import dsl._

    HttpRoutes.of[F] { case req @ POST -> Root / "ingest" =>
      for {
        activeTabMessage <- req.as[common.ActiveTabMessage].attempt
        resp             <- activeTabMessage match {
                              case Right(body) =>
                                for {
                                  messageReceivedResp <- I.post(body)
                                  resp                <- Ok(messageReceivedResp)
                                } yield resp
                              case Left(err)   => BadRequest(s"invalid request body: ${err.getMessage}")
                            }
      } yield resp
    }
  }
}
